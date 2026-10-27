package net.geoprism.registry.service;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.session.Request;

import net.geoprism.registry.Commit;
import net.geoprism.registry.EventDatasetTest;
import net.geoprism.registry.Publish;
import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.service.business.CommitBusinessServiceIF;
import net.geoprism.registry.service.business.PublishBusinessServiceIF;
import net.geoprism.registry.view.CommitDTO;
import net.geoprism.registry.view.PublishDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
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

      Assertions.assertNotNull(commit);
      Assertions.assertEquals(expected.getUid(), commit.getUid());
      Assertions.assertEquals(publish.getOid(), commit.getPublishOid());
      Assertions.assertEquals(expected.getLastOriginGlobalIndex(), commit.getLastOriginGlobalIndex());
      Assertions.assertEquals(expected.getVersionNumber(), commit.getVersionNumber());

      CommitDTO actual = commit.toDTO();

      Assertions.assertEquals(expected.getUid(), actual.getUid());
      Assertions.assertEquals(expected.getPublishId(), actual.getPublishId());
      Assertions.assertEquals(expected.getLastOriginGlobalIndex(), actual.getLastOriginGlobalIndex());
      Assertions.assertEquals(expected.getVersionNumber(), actual.getVersionNumber());

      Assertions.assertEquals(1, this.service.getAll().size());
      Assertions.assertEquals(1, this.service.getCommits(publish).size());
      Assertions.assertTrue(this.service.getCommit(expected.getUid()).isPresent());
      Assertions.assertTrue(this.service.getLatest(publish).isPresent());
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

      Assertions.assertEquals(1, results.size());
      Assertions.assertEquals(first.getUid(), results.get(0).getUid());

      Commit latest = this.service.getLatest(publish).get();
      Assertions.assertEquals(third.getUid(), latest.getUid());
    }
    finally
    {
      this.publishService.delete(publish);
    }
  }

  @Test
  @Request
  public void testDependents()
  {
    Publish publish = this.publishService.create(this.getPublishDTO());

    try
    {
      Commit first = this.service.create(publish, new CommitDTO(UUID.randomUUID().toString(), publish.getUid(), 1, 10L));
      Commit second = this.service.create(publish, new CommitDTO(UUID.randomUUID().toString(), publish.getUid(), 2, 100L));
      Commit third = this.service.create(publish, new CommitDTO(UUID.randomUUID().toString(), publish.getUid(), 3, 1000L));

      second.addDependency(first).apply();
      third.addDependency(second).apply();

      List<Commit> results = this.service.getDependents(second);

      Assertions.assertEquals(1, results.size());
      Assertions.assertEquals(third.getUid(), results.get(0).getUid());
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
    configuration.addConceptClass(cClass.getCode());
    configuration.addConceptEdgeType(cEdgeType.getCode());

    Publish publish = this.publishService.create(configuration);

    try
    {
      Commit commit = this.service.create(publish, 1, 10);

      Assertions.assertNotNull(this.service.getRootType(commit));
      Assertions.assertEquals( ( configuration.getGeoObjectTypes().toList().size() + 1 ), this.service.getTypes(commit).size());
      Assertions.assertEquals(configuration.getBusinessTypes().toList().size(), this.service.getBusinessTypes(commit).size());
      Assertions.assertEquals(configuration.getConceptClasses().toList().size(), this.service.getConceptClasses(commit).size());
      Assertions.assertEquals(configuration.getConceptEdgeTypes().toList().size(), this.service.getConceptEdgeTypes(commit).size());
      Assertions.assertEquals(configuration.getHierarchyTypes().toList().size(), this.service.getHiearchyTypes(commit).size());
      Assertions.assertEquals(configuration.getDagTypes().toList().size(), this.service.getDirectedAcyclicGraphTypes(commit).size());
      Assertions.assertEquals(configuration.getUndirectedTypes().toList().size(), this.service.getUndirectedGraphTypes(commit).size());

      configuration.getGeoObjectTypes().forEach(code ->
        Assertions.assertNotNull(this.service.getSnapshot(commit, code)));

      configuration.getHierarchyTypes().forEach(code ->
        Assertions.assertNotNull(this.service.getHierarchyType(commit, code)));
    }
    finally
    {
      this.publishService.delete(publish);
    }
  }
}
