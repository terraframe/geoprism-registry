package net.geoprism.registry.service;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Request;

import net.geoprism.configuration.GeoprismProperties;
import net.geoprism.registry.Commit;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.Publish;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.service.business.CommitBusinessServiceIF;
import net.geoprism.registry.service.business.PublishBusinessServiceIF;
import net.geoprism.registry.test.USATestData;
import net.geoprism.registry.view.CommitDTO;
import net.geoprism.registry.view.PublishDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class PublishBusinessServiceTest
{
  @Autowired
  private PublishBusinessServiceIF service;

  @Autowired
  private CommitBusinessServiceIF  commitService;

  public PublishDTO createDTO(String code)
  {
    PublishDTO dto = new PublishDTO(code, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
    dto.addBusinessEdgeType("TEST_B_EDGE");
    dto.addBusinessType("TEST_B");
    dto.addDagType("TEST_DAG");
    dto.addGeoObjectType("TEST_G_TYPE");
    dto.addHierarchyType("TEST_H_TYPE");
    dto.addUndirectedType("TEST_U_TYPE");

    return dto;
  }

  @Test
  @Request
  public void testCreateAndDelete()
  {
    PublishDTO dto = createDTO("USA Geospatial Graph");

    Publish publish = this.service.create(dto);

    try
    {
      Assert.assertNotNull(publish);

      Assert.assertEquals(dto.getStartDate(), publish.getStartDate());
      Assert.assertEquals(dto.getEndDate(), publish.getEndDate());
      Assert.assertEquals(dto.getDate(), publish.getForDate());
      Assert.assertEquals(dto.getUid(), publish.getUid());
      Assert.assertEquals(GeoprismProperties.getOrigin(), publish.getOrigin());

      PublishDTO actual = publish.toDTO();

      Assert.assertTrue(actual.getBusinessEdgeTypes().toList().containsAll(dto.getBusinessEdgeTypes().toList()));
      Assert.assertTrue(actual.getBusinessTypes().toList().containsAll(dto.getBusinessTypes().toList()));
      Assert.assertTrue(actual.getDagTypes().toList().containsAll(dto.getDagTypes().toList()));
      Assert.assertTrue(actual.getGeoObjectTypes().toList().containsAll(dto.getGeoObjectTypes().toList()));
      Assert.assertTrue(actual.getHierarchyTypes().toList().containsAll(dto.getHierarchyTypes().toList()));
      Assert.assertTrue(actual.getUndirectedTypes().toList().containsAll(dto.getUndirectedTypes().toList()));

      Assert.assertTrue(this.service.getByUid(dto.getUid()).isPresent());
      Assert.assertEquals(1, this.service.getAll().size());
    }
    finally
    {
      this.service.delete(publish);

    }

    Assert.assertFalse(this.service.getByUid(dto.getUid()).isPresent());
  }

  @Test(expected = ProgrammingErrorException.class)
  @Request
  public void testDeleteWithDependents()
  {
    Publish publish = this.service.create(createDTO("USA Geospatial Graph"));

    try
    {
      Publish dependent = this.service.create(createDTO("Dependent Geospatial Graph"));

      try
      {
        Commit first = this.commitService.create(publish, new CommitDTO(UUID.randomUUID().toString(), dependent.getUid(), 1, 10L));
        Commit second = this.commitService.create(dependent, new CommitDTO(UUID.randomUUID().toString(), dependent.getUid(), 2, 100L));

        second.addDependency(first).apply();

        Assert.assertEquals(1, this.commitService.getDependencies(second).size());

        // Try to delete a publish which another publish is dependent upon
        this.service.delete(publish);

        Assert.fail();
      }
      finally
      {
        this.service.delete(dependent);
      }
    }
    finally
    {
      this.service.delete(publish);
    }
  }

  @Test
  @Request
  public void testGetRemoteFor()
  {
    PublishDTO expected = new PublishDTO("USA Geospatial Graph", USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
    expected.addBusinessEdgeType("TEST_B_EDGE");
    expected.addBusinessType("TEST_B");
    expected.addDagType("TEST_DAG");
    expected.addGeoObjectType("TEST_G_TYPE");
    expected.addHierarchyType("TEST_H_TYPE");
    expected.addUndirectedType("TEST_U_TYPE");
    expected.setOrigin("REMOTE_ORIGIN");

    Publish publish = this.service.create(expected);

    try
    {
      List<Publish> results = this.service.getRemoteFor(expected);

      Assert.assertEquals(1, results.size());
      Assert.assertEquals(publish.getOid(), results.get(0).getOid());

      PublishDTO dto = new PublishDTO("USA Geospatial Graph", USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);

      Assert.assertEquals(0, this.service.getRemoteFor(dto).size());

      dto = new PublishDTO("USA Geospatial Graph", USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      expected.getBusinessEdgeTypes().forEach(dto::addBusinessEdgeType);

      Assert.assertEquals(1, this.service.getRemoteFor(dto).size());

      dto = new PublishDTO("USA Geospatial Graph", USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      expected.getBusinessTypes().forEach(dto::addBusinessType);

      Assert.assertEquals(1, this.service.getRemoteFor(dto).size());

      dto = new PublishDTO("USA Geospatial Graph", USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      expected.getDagTypes().forEach(dto::addDagType);

      Assert.assertEquals(1, this.service.getRemoteFor(dto).size());

      dto = new PublishDTO("USA Geospatial Graph", USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      expected.getGeoObjectTypes().forEach(dto::addGeoObjectType);

      Assert.assertEquals(1, this.service.getRemoteFor(dto).size());

      dto = new PublishDTO("USA Geospatial Graph", USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      expected.getHierarchyTypes().forEach(dto::addHierarchyType);

      Assert.assertEquals(1, this.service.getRemoteFor(dto).size());

      dto = new PublishDTO("USA Geospatial Graph", USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      expected.getUndirectedTypes().forEach(dto::addUndirectedType);

      Assert.assertEquals(1, this.service.getRemoteFor(dto).size());

      Calendar calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
      calendar.setTime(USATestData.DEFAULT_OVER_TIME_DATE);
      calendar.add(Calendar.DAY_OF_YEAR, -1);

      dto = new PublishDTO("USA Geospatial Graph", USATestData.DEFAULT_OVER_TIME_DATE, calendar.getTime(), USATestData.DEFAULT_END_TIME_DATE);
      expected.getUndirectedTypes().forEach(dto::addUndirectedType);

      Assert.assertEquals(1, this.service.getRemoteFor(dto).size());

      calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
      calendar.setTime(USATestData.DEFAULT_END_TIME_DATE);
      calendar.add(Calendar.DAY_OF_YEAR, 1);

      dto = new PublishDTO("USA Geospatial Graph", calendar.getTime(), calendar.getTime(), calendar.getTime());
      expected.getUndirectedTypes().forEach(dto::addUndirectedType);

      Assert.assertEquals(0, this.service.getRemoteFor(dto).size());

      calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
      calendar.setTime(USATestData.DEFAULT_OVER_TIME_DATE);
      calendar.add(Calendar.DAY_OF_YEAR, -1);

      dto = new PublishDTO("USA Geospatial Graph", calendar.getTime(), calendar.getTime(), calendar.getTime());
      expected.getUndirectedTypes().forEach(dto::addUndirectedType);

      Assert.assertEquals(0, this.service.getRemoteFor(dto).size());

      dto = new PublishDTO("USA Geospatial Graph", USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE);
      expected.getUndirectedTypes().forEach(dto::addUndirectedType);

      Assert.assertEquals(1, this.service.getRemoteFor(dto).size());
    }
    finally
    {
      this.service.delete(publish);
    }
  }

}
