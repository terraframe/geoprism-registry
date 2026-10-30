/**
 *
 */
package net.geoprism.registry.service;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Request;

import net.geoprism.configuration.GeoprismProperties;
import net.geoprism.registry.Commit;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.Publish;
import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.service.business.CommitBusinessServiceIF;
import net.geoprism.registry.service.business.PublishBusinessServiceIF;
import net.geoprism.registry.test.USATestData;
import net.geoprism.registry.view.CommitDTO;
import net.geoprism.registry.view.PublishDTO;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
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
      Assertions.assertNotNull(publish);

      Assertions.assertEquals(dto.getStartDate(), publish.getStartDate());
      Assertions.assertEquals(dto.getEndDate(), publish.getEndDate());
      Assertions.assertEquals(dto.getDate(), publish.getForDate());
      Assertions.assertEquals(dto.getUid(), publish.getUid());
      Assertions.assertEquals(GeoprismProperties.getOrigin(), publish.getOrigin());

      PublishDTO actual = publish.toDTO();

      Assertions.assertTrue(actual.getBusinessEdgeTypes().toList().containsAll(dto.getBusinessEdgeTypes().toList()));
      Assertions.assertTrue(actual.getBusinessTypes().toList().containsAll(dto.getBusinessTypes().toList()));
      Assertions.assertTrue(actual.getDagTypes().toList().containsAll(dto.getDagTypes().toList()));
      Assertions.assertTrue(actual.getGeoObjectTypes().toList().containsAll(dto.getGeoObjectTypes().toList()));
      Assertions.assertTrue(actual.getHierarchyTypes().toList().containsAll(dto.getHierarchyTypes().toList()));
      Assertions.assertTrue(actual.getUndirectedTypes().toList().containsAll(dto.getUndirectedTypes().toList()));

      Assertions.assertTrue(this.service.getByUid(dto.getUid()).isPresent());
      Assertions.assertEquals(1, this.service.getAll().size());
    }
    finally
    {
      this.service.delete(publish);

    }

    Assertions.assertFalse(this.service.getByUid(dto.getUid()).isPresent());
  }

  @Test
  @Request
  public void testDeleteWithDependents()
  {
    assertThrows(ProgrammingErrorException.class, () -> {
      Publish publish = this.service.create(createDTO("USA Geospatial Graph"));

      try
      {
        Publish dependent = this.service.create(createDTO("Dependent Geospatial Graph"));

        try
        {
          Commit first = this.commitService.create(publish, new CommitDTO(UUID.randomUUID().toString(), dependent.getUid(), 1, 10L));
          Commit second = this.commitService.create(dependent, new CommitDTO(UUID.randomUUID().toString(), dependent.getUid(), 2, 100L));

          second.addDependency(first).apply();

          Assertions.assertEquals(1, this.commitService.getDependencies(second).size());

          // Try to delete a publish which another publish is dependent upon
          this.service.delete(publish);

          Assertions.fail();
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
    });
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

      Assertions.assertEquals(1, results.size());
      Assertions.assertEquals(publish.getOid(), results.get(0).getOid());

      PublishDTO dto = new PublishDTO("USA Geospatial Graph", USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);

      Assertions.assertEquals(0, this.service.getRemoteFor(dto).size());

      dto = new PublishDTO("USA Geospatial Graph", USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      expected.getBusinessEdgeTypes().forEach(dto::addBusinessEdgeType);

      Assertions.assertEquals(1, this.service.getRemoteFor(dto).size());

      dto = new PublishDTO("USA Geospatial Graph", USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      expected.getBusinessTypes().forEach(dto::addBusinessType);

      Assertions.assertEquals(1, this.service.getRemoteFor(dto).size());

      dto = new PublishDTO("USA Geospatial Graph", USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      expected.getDagTypes().forEach(dto::addDagType);

      Assertions.assertEquals(1, this.service.getRemoteFor(dto).size());

      dto = new PublishDTO("USA Geospatial Graph", USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      expected.getGeoObjectTypes().forEach(dto::addGeoObjectType);

      Assertions.assertEquals(1, this.service.getRemoteFor(dto).size());

      dto = new PublishDTO("USA Geospatial Graph", USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      expected.getHierarchyTypes().forEach(dto::addHierarchyType);

      Assertions.assertEquals(1, this.service.getRemoteFor(dto).size());

      dto = new PublishDTO("USA Geospatial Graph", USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      expected.getUndirectedTypes().forEach(dto::addUndirectedType);

      Assertions.assertEquals(1, this.service.getRemoteFor(dto).size());

      Calendar calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
      calendar.setTime(USATestData.DEFAULT_OVER_TIME_DATE);
      calendar.add(Calendar.DAY_OF_YEAR, -1);

      dto = new PublishDTO("USA Geospatial Graph", USATestData.DEFAULT_OVER_TIME_DATE, calendar.getTime(), USATestData.DEFAULT_END_TIME_DATE);
      expected.getUndirectedTypes().forEach(dto::addUndirectedType);

      Assertions.assertEquals(1, this.service.getRemoteFor(dto).size());

      calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
      calendar.setTime(USATestData.DEFAULT_END_TIME_DATE);
      calendar.add(Calendar.DAY_OF_YEAR, 1);

      dto = new PublishDTO("USA Geospatial Graph", calendar.getTime(), calendar.getTime(), calendar.getTime());
      expected.getUndirectedTypes().forEach(dto::addUndirectedType);

      Assertions.assertEquals(0, this.service.getRemoteFor(dto).size());

      calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
      calendar.setTime(USATestData.DEFAULT_OVER_TIME_DATE);
      calendar.add(Calendar.DAY_OF_YEAR, -1);

      dto = new PublishDTO("USA Geospatial Graph", calendar.getTime(), calendar.getTime(), calendar.getTime());
      expected.getUndirectedTypes().forEach(dto::addUndirectedType);

      Assertions.assertEquals(0, this.service.getRemoteFor(dto).size());

      dto = new PublishDTO("USA Geospatial Graph", USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE);
      expected.getUndirectedTypes().forEach(dto::addUndirectedType);

      Assertions.assertEquals(1, this.service.getRemoteFor(dto).size());
    }
    finally
    {
      this.service.delete(publish);
    }
  }

}
