/**
 *
 */
package net.geoprism.registry.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.session.Request;

import net.geoprism.registry.EventDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.axon.event.repository.BusinessObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.BusinessObjectEventBuilder;
import net.geoprism.registry.axon.event.repository.ConceptObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.ConceptObjectEventBuilder;
import net.geoprism.registry.axon.event.repository.GeoObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectEventBuilder;
import net.geoprism.registry.axon.event.repository.RepositoryEvent;
import net.geoprism.registry.axon.event.repository.ServerGeoObjectEventBuilder;
import net.geoprism.registry.axon.projection.RepositoryProjection;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.ConceptObject;
import net.geoprism.registry.model.ServerChildGraphNode;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.query.graph.VertexAndEdgeQuery.EdgeQueryObject;
import net.geoprism.registry.service.business.EdgeObjectBusinessService;
import net.geoprism.registry.service.business.EventBusinessService;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.USATestData;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
public class RepositoryProjectionTest extends EventDatasetTest implements InstanceTestClassListener
{

  @Autowired
  private RepositoryProjection      projection;

  @Autowired
  private EventBusinessService      eventService;

  @Autowired
  private EdgeObjectBusinessService eObjectService;

  @BeforeEach
  @Override
  @Request
  public void setUp() throws Exception
  {
    USATestData.COLORADO.removeDefaultValue(testClassification.getCode());

    this.store.truncate();
  }

  @AfterEach
  @Override
  @Request
  public void tearDown() throws Exception
  {
    this.store.truncate();
  }

  public ServerGeoObjectIF createGeoObject(TestGeoObjectInfo info)
  {
    ServerGeoObjectIF object = this.gObjectService.newInstance(info.getGeoObjectType().getServerObject());
    GeoObjectOverTime dto = this.gObjectService.toGeoObjectOverTime(object);

    info.populate(dto);

    GeoObjectEventBuilder builder = new GeoObjectEventBuilder(gObjectService);
    builder.setObject(dto, true, false);

    List<RepositoryEvent> events = builder.build();

    this.projection.handleApplyGeoObject((GeoObjectApplyEvent) events.get(0));

    return this.gObjectService.getGeoObjectByCode(info.getCode(), info.getGeoObjectType().getCode());
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

    Assertions.assertEquals(1, events.size());

    this.projection.handleApplyGeoObject((GeoObjectApplyEvent) events.get(0));

    ServerGeoObjectIF result = this.gObjectService.getGeoObjectByCode(USATestData.COLORADO.getCode(), USATestData.COLORADO.getGeoObjectType().getCode());

    Assertions.assertNotNull(result);
    Assertions.assertEquals(USATestData.COLORADO.getDisplayLabel(), result.getDisplayLabel(USATestData.DEFAULT_OVER_TIME_DATE).getLocalizedValue());
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

    Assertions.assertEquals(1, events.size());

    this.projection.handleApplyBusinessObject((BusinessObjectApplyEvent) events.get(0));

    Optional<BusinessObject> result = this.bObjectService.getByCode(btype, code);

    Assertions.assertTrue(result.isPresent());
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

    Assertions.assertEquals(1, events.size());

    this.projection.handleApplyConceptObject((ConceptObjectApplyEvent) events.get(0));

    Optional<ConceptObject> result = this.cObjectService.getByCode(cClass, code);

    Assertions.assertTrue(result.isPresent());
  }

  @Test
  @Request
  public void testHandleApplyConceptEdge() throws InterruptedException
  {
    ConceptObject parent = this.createConceptObject("P_CONCEPT");
    ConceptObject child = this.createConceptObject("C_CONCEPT");

    this.addConceptEdge(parent, cEdgeType, child);

    List<EdgeQueryObject> results = this.cObjectService.getEdgeChildren(parent, cEdgeType, TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assertions.assertEquals(1, results.size());

    EdgeQueryObject result = results.get(0);

    this.eObjectService.getByOid(cEdgeType, result.getOid()).ifPresent(edge ->
      this.eventService.remove(cEdgeType, edge));

    Assertions.assertEquals(0, this.cObjectService.getEdgeChildren(parent, cEdgeType, TestDataSet.DEFAULT_OVER_TIME_DATE).size());
  }

  @Test
  @Request
  public void testHandleApplyDagEdge() throws InterruptedException
  {
    ServerGeoObjectIF colorado = createGeoObject(USATestData.USA);
    ServerGeoObjectIF canada = createGeoObject(USATestData.CANADA);
    String edgeUid = UUID.randomUUID().toString();

    ServerGeoObjectEventBuilder builder = new ServerGeoObjectEventBuilder(gObjectService);
    builder.setObject(colorado, false, false);
    builder.addEdge(canada, dagType, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_OVER_TIME_DATE, edgeUid, null, ImportStrategy.NEW_AND_UPDATE, true);

    this.eventService.publish(builder.build());

    ServerChildGraphNode results = this.gObjectService.getGraphChildGeoObjects(colorado, dagType, false, TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assertions.assertEquals(1, results.getChildren().size());

    EdgeObject edge = this.eObjectService.getByUid(dagType, edgeUid).get();

    this.eventService.remove(dagType, edge);

    results = this.gObjectService.getGraphChildGeoObjects(colorado, dagType, false, TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assertions.assertEquals(0, results.getChildren().size());
  }

}
