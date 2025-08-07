package net.geoprism.registry.service;

import java.util.Calendar;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.session.Request;

import net.geoprism.configuration.GeoprismProperties;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.Publish;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.service.business.PublishBusinessServiceIF;
import net.geoprism.registry.test.USATestData;
import net.geoprism.registry.view.PublishDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class PublishBusinessServiceTest
{
  @Autowired
  private PublishBusinessServiceIF service;

  @Test
  @Request
  public void testCreateAndDelete()
  {
    PublishDTO expected = new PublishDTO(USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
    expected.addBusinessEdgeType("TEST_B_EDGE");
    expected.addBusinessType("TEST_B");
    expected.addDagType("TEST_DAG");
    expected.addGeoObjectType("TEST_G_TYPE");
    expected.addHierarchyType("TEST_H_TYPE");
    expected.addUndirectedType("TEST_U_TYPE");

    Publish publish = this.service.create(expected);

    try
    {
      Assert.assertNotNull(publish);

      Assert.assertEquals(expected.getStartDate(), publish.getStartDate());
      Assert.assertEquals(expected.getEndDate(), publish.getEndDate());
      Assert.assertEquals(expected.getDate(), publish.getForDate());
      Assert.assertEquals(expected.getUid(), publish.getUid());
      Assert.assertEquals(GeoprismProperties.getOrigin(), publish.getOrigin());

      PublishDTO actual = publish.toDTO();

      Assert.assertTrue(actual.getBusinessEdgeTypes().toList().containsAll(expected.getBusinessEdgeTypes().toList()));
      Assert.assertTrue(actual.getBusinessTypes().toList().containsAll(expected.getBusinessTypes().toList()));
      Assert.assertTrue(actual.getDagTypes().toList().containsAll(expected.getDagTypes().toList()));
      Assert.assertTrue(actual.getGeoObjectTypes().toList().containsAll(expected.getGeoObjectTypes().toList()));
      Assert.assertTrue(actual.getHierarchyTypes().toList().containsAll(expected.getHierarchyTypes().toList()));
      Assert.assertTrue(actual.getUndirectedTypes().toList().containsAll(expected.getUndirectedTypes().toList()));

      Assert.assertTrue(this.service.getByUid(expected.getUid()).isPresent());
      Assert.assertEquals(1, this.service.getAll().size());
    }
    finally
    {
      this.service.delete(publish);

    }

    Assert.assertFalse(this.service.getByUid(expected.getUid()).isPresent());
  }

  @Test
  @Request
  public void testGetRemoteFor()
  {
    PublishDTO expected = new PublishDTO(USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
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

      PublishDTO dto = new PublishDTO(USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);

      Assert.assertEquals(0, this.service.getRemoteFor(dto).size());

      dto = new PublishDTO(USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      expected.getBusinessEdgeTypes().forEach(dto::addBusinessEdgeType);

      Assert.assertEquals(1, this.service.getRemoteFor(dto).size());

      dto = new PublishDTO(USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      expected.getBusinessTypes().forEach(dto::addBusinessType);

      Assert.assertEquals(1, this.service.getRemoteFor(dto).size());

      dto = new PublishDTO(USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      expected.getDagTypes().forEach(dto::addDagType);

      Assert.assertEquals(1, this.service.getRemoteFor(dto).size());

      dto = new PublishDTO(USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      expected.getGeoObjectTypes().forEach(dto::addGeoObjectType);

      Assert.assertEquals(1, this.service.getRemoteFor(dto).size());

      dto = new PublishDTO(USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      expected.getHierarchyTypes().forEach(dto::addHierarchyType);

      Assert.assertEquals(1, this.service.getRemoteFor(dto).size());

      dto = new PublishDTO(USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      expected.getUndirectedTypes().forEach(dto::addUndirectedType);

      Assert.assertEquals(1, this.service.getRemoteFor(dto).size());

      Calendar calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
      calendar.setTime(USATestData.DEFAULT_OVER_TIME_DATE);
      calendar.add(Calendar.DAY_OF_YEAR, -1);

      dto = new PublishDTO(USATestData.DEFAULT_OVER_TIME_DATE, calendar.getTime(), USATestData.DEFAULT_END_TIME_DATE);
      expected.getUndirectedTypes().forEach(dto::addUndirectedType);

      Assert.assertEquals(1, this.service.getRemoteFor(dto).size());

      calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
      calendar.setTime(USATestData.DEFAULT_END_TIME_DATE);
      calendar.add(Calendar.DAY_OF_YEAR, 1);

      dto = new PublishDTO(calendar.getTime(), calendar.getTime(), calendar.getTime());
      expected.getUndirectedTypes().forEach(dto::addUndirectedType);

      Assert.assertEquals(0, this.service.getRemoteFor(dto).size());

      calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
      calendar.setTime(USATestData.DEFAULT_OVER_TIME_DATE);
      calendar.add(Calendar.DAY_OF_YEAR, -1);

      dto = new PublishDTO(calendar.getTime(), calendar.getTime(), calendar.getTime());
      expected.getUndirectedTypes().forEach(dto::addUndirectedType);

      Assert.assertEquals(0, this.service.getRemoteFor(dto).size());

      dto = new PublishDTO(USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE);
      expected.getUndirectedTypes().forEach(dto::addUndirectedType);

      Assert.assertEquals(1, this.service.getRemoteFor(dto).size());
    }
    finally
    {
      this.service.delete(publish);
    }
  }

}
