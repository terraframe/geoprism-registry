/**
 *
 */
package net.geoprism.registry.service;

import java.util.List;
import java.util.Optional;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.session.Request;

import net.geoprism.registry.EventDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.axon.event.repository.BusinessObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.BusinessObjectEventBuilder;
import net.geoprism.registry.axon.event.repository.ConceptObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.ConceptObjectEventBuilder;
import net.geoprism.registry.axon.event.repository.GeoObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectEventBuilder;
import net.geoprism.registry.axon.event.repository.RepositoryEvent;
import net.geoprism.registry.axon.projection.RepositoryProjection;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.ConceptObject;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessEdgeTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.CommitBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptClassBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptClassSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.DataSourceBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.GraphTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.PublishBusinessServiceIF;
import net.geoprism.registry.service.business.PublishEventService;
import net.geoprism.registry.service.business.SourceAuthorityBusinessServiceIF;
import net.geoprism.registry.test.USATestData;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class RepositoryProjectionTest extends EventDatasetTest implements InstanceTestClassListener
{

  @Autowired
  private RepositoryProjection projection;

  @Override
  public void setUp()
  {
  }

  @Override
  public void tearDown()
  {
    super.tearDown();
  }

  @Test
  @Request
  public void testHandleApplyGeoObject() throws InterruptedException
  {
    ServerGeoObjectIF object = this.gObjectService.newInstance(USATestData.COLORADO.getGeoObjectType().getServerObject());
    GeoObjectOverTime dto = this.gObjectService.toGeoObjectOverTime(object);

    USATestData.COLORADO.populate(dto);

    GeoObjectEventBuilder builder = new GeoObjectEventBuilder(gObjectService);
    builder.setObject(dto, true, false);

    List<RepositoryEvent> events = builder.build();

    Assert.assertEquals(1, events.size());

    this.projection.handleApplyGeoObject((GeoObjectApplyEvent) events.get(0));

    ServerGeoObjectIF result = this.gObjectService.getGeoObjectByCode(USATestData.COLORADO.getCode(), USATestData.COLORADO.getGeoObjectType().getCode());

    Assert.assertNotNull(result);
    Assert.assertEquals(USATestData.COLORADO.getDisplayLabel(), result.getDisplayLabel(USATestData.DEFAULT_OVER_TIME_DATE).getLocalizedValue());
  }

  @Test
  @Request
  public void testHandleApplyBusinessObject() throws InterruptedException
  {
    String code = "B_CODE";

    BusinessObject object = this.bObjectService.newInstance(btype);
    object.setCode(code);
    object.setValue("testBoolean", false);
    object.setValue(DefaultAttribute.DATA_SOURCE.getName(), USATestData.SOURCE.getDataSource(), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);

    BusinessObjectEventBuilder builder = new BusinessObjectEventBuilder(bObjectService);
    builder.setObject(object, true);

    List<RepositoryEvent> events = builder.build();

    Assert.assertEquals(1, events.size());

    this.projection.handleApplyBusinessObject((BusinessObjectApplyEvent) events.get(0));

    Optional<BusinessObject> result = this.bObjectService.getByCode(btype, code);

    Assert.assertTrue(result.isPresent());
  }

  @Test
  @Request
  public void testHandleApplyConceptObject() throws InterruptedException
  {
    String code = "B_CODE";

    ConceptObject object = this.cObjectService.newInstance(cClass);
    object.setCode(code);
    object.setValue("testBoolean", false);
    object.setValue(DefaultAttribute.DATA_SOURCE.getName(), USATestData.SOURCE.getDataSource(), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);

    ConceptObjectEventBuilder builder = new ConceptObjectEventBuilder(cObjectService);
    builder.setObject(object, true);

    List<RepositoryEvent> events = builder.build();

    Assert.assertEquals(1, events.size());

    this.projection.handleApplyConceptObject((ConceptObjectApplyEvent) events.get(0));

    Optional<ConceptObject> result = this.cObjectService.getByCode(cClass, code);

    Assert.assertTrue(result.isPresent());
  }

}
