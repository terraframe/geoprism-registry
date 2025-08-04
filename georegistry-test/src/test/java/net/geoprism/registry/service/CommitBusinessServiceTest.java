package net.geoprism.registry.service;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.session.Request;

import net.geoprism.registry.Commit;
import net.geoprism.registry.EventDatasetTest;
import net.geoprism.registry.Publish;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.service.business.CommitBusinessServiceIF;
import net.geoprism.registry.service.business.PublishBusinessServiceIF;
import net.geoprism.registry.view.CommitDTO;
import net.geoprism.registry.view.PublishDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class CommitBusinessServiceTest extends EventDatasetTest
{
  @Autowired
  private PublishBusinessServiceIF publishService;

  @Autowired
  private CommitBusinessServiceIF  service;

  @Test
  @Request
  public void testCreateAndDelete()
  {
    Publish publish = this.publishService.create(this.getPublishDTO());

    try
    {
      CommitDTO expected = new CommitDTO(UUID.randomUUID().toString(), publish.getUid(), 1, 10L);

      Commit commit = this.service.create(publish, expected);

      Assert.assertNotNull(commit);
      Assert.assertEquals(expected.getUid(), commit.getUid());
      Assert.assertEquals(publish.getOid(), commit.getPublishOid());
      Assert.assertEquals(expected.getLastOriginGlobalIndex(), commit.getLastOriginGlobalIndex());
      Assert.assertEquals(expected.getVersionNumber(), commit.getVersionNumber());

      CommitDTO actual = commit.toDTO();

      Assert.assertEquals(expected.getUid(), actual.getUid());
      Assert.assertEquals(expected.getPublishId(), actual.getPublishId());
      Assert.assertEquals(expected.getLastOriginGlobalIndex(), actual.getLastOriginGlobalIndex());
      Assert.assertEquals(expected.getVersionNumber(), actual.getVersionNumber());

      Assert.assertEquals(1, this.service.getAll().size());
      Assert.assertEquals(1, this.service.getCommits(publish).size());
      Assert.assertTrue(this.service.getCommit(expected.getUid()).isPresent());
      Assert.assertTrue(this.service.getLatest(publish).isPresent());
    }
    finally
    {
      this.publishService.delete(publish);
    }
  }

  @Test
  @Request
  public void testDependencies()
  {
    Publish publish = this.publishService.create(this.getPublishDTO());

    try
    {
      Commit first = this.service.create(publish, new CommitDTO(UUID.randomUUID().toString(), publish.getUid(), 1, 10L));
      Commit second = this.service.create(publish, new CommitDTO(UUID.randomUUID().toString(), publish.getUid(), 2, 100L));
      Commit third = this.service.create(publish, new CommitDTO(UUID.randomUUID().toString(), publish.getUid(), 3, 1000L));

      second.addDependency(first).apply();
      third.addDependency(second).apply();

      List<Commit> results = this.service.getDependencies(second);

      Assert.assertEquals(1, results.size());
      Assert.assertEquals(first.getUid(), results.get(0).getUid());

      Commit latest = this.service.getLatest(publish).get();
      Assert.assertEquals(third.getUid(), latest.getUid());
    }
    finally
    {
      this.publishService.delete(publish);
    }
  }

  @Test
  @Request
  public void testCreateSnapshots()
  {
    PublishDTO configuration = this.getPublishDTO();

    Publish publish = this.publishService.create(configuration);

    try
    {
      Commit commit = this.service.create(publish, 1, 10);

      Assert.assertNotNull(this.service.getRootType(commit));
      Assert.assertEquals( ( configuration.getGeoObjectTypes().toList().size() + 1 ), this.service.getTypes(commit).size());
      Assert.assertEquals(configuration.getBusinessTypes().toList().size(), this.service.getBusinessTypes(commit).size());
      Assert.assertEquals(configuration.getBusinessEdgeTypes().toList().size(), this.service.getBusinessEdgeTypes(commit).size());
      Assert.assertEquals(configuration.getHierarchyTypes().toList().size(), this.service.getHiearchyTypes(commit).size());
      Assert.assertEquals(configuration.getDagTypes().toList().size(), this.service.getDirectedAcyclicGraphTypes(commit).size());
      Assert.assertEquals(configuration.getUndirectedTypes().toList().size(), this.service.getUndirectedGraphTypes(commit).size());

      configuration.getGeoObjectTypes().forEach(code -> {
        Assert.assertNotNull(this.service.getSnapshot(commit, code));
      });

      configuration.getHierarchyTypes().forEach(code -> {
        Assert.assertNotNull(this.service.getHierarchyType(commit, code));
      });
    }
    finally
    {
      this.publishService.delete(publish);
    }
  }
}
