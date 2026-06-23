package net.geoprism.registry;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.axonframework.eventhandling.GenericEventMessage;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonObject;
import com.runwaysdk.Pair;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.SchedulerManager;

import net.geoprism.registry.axon.config.RegistryEventStore;
import net.geoprism.registry.axon.event.repository.ServerGeoObjectEventBuilder;
import net.geoprism.registry.axon.projection.RepositoryProjection;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.BusinessEdgeType;
import net.geoprism.registry.graph.BusinessType;
import net.geoprism.registry.graph.DirectedAcyclicGraphType;
import net.geoprism.registry.graph.UndirectedGraphType;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.EdgeDirection;
import net.geoprism.registry.model.graph.VertexComponent;
import net.geoprism.registry.service.business.DirectedAcyclicGraphTypeBusinessServiceIF;
import net.geoprism.registry.service.business.GraphRepoServiceIF;
import net.geoprism.registry.service.business.UndirectedGraphTypeBusinessServiceIF;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.USATestData;
import net.geoprism.registry.view.BusinessEdgeTypeView;
import net.geoprism.registry.view.BusinessGeoEdgeTypeView;
import net.geoprism.registry.view.PublishDTO;

public abstract class EventDatasetTest extends USADatasetTest implements InstanceTestClassListener
{
  @Autowired
  protected RegistryEventStore                        store;

  @Autowired
  protected DirectedAcyclicGraphTypeBusinessServiceIF dagService;

  @Autowired
  protected UndirectedGraphTypeBusinessServiceIF      undirectedService;

  @Autowired
  protected GraphRepoServiceIF                        repoService;

  @Autowired
  protected RepositoryProjection                      projection;

  protected static BusinessType                       btype;

  protected static BusinessEdgeType                   bEdgeType;

  protected static BusinessEdgeType                   bGeoEdgeType;

  protected static DirectedAcyclicGraphType           dagType;

  protected static UndirectedGraphType                undirectedType;

  protected static BusinessObject                     pObject;

  protected static BusinessObject                     cObject;

  @Override
  public void beforeClassSetup() throws Exception
  {
    super.beforeClassSetup();

    setUpInReq();

    if (!SchedulerManager.initialized())
    {
      SchedulerManager.start();
    }
  }

  @Request
  private void setUpInReq()
  {
    JsonObject object = new JsonObject();
    object.addProperty(BusinessType.CODE, "TEST_BUSINESS");
    object.addProperty(BusinessType.ORGANIZATION, USATestData.ORG_PPP.getCode());
    object.add(BusinessType.DISPLAYLABEL, new LocalizedValue("Test Business").toJSON());

    btype = this.bTypeService.apply(object);

    this.bTypeService.createAttributeType(btype, new AttributeBooleanType("testBoolean", new LocalizedValue("Test Boolean"), new LocalizedValue("Test Boolean"), false, false, false));

    bEdgeType = this.bEdgeService.create(BusinessEdgeTypeView.build(USATestData.ORG_PPP.getCode(), "TEST_B_EDGE", new LocalizedValue("TEST_B_EDGE"), new LocalizedValue("TEST_B_EDGE"), btype.getCode(), btype.getCode()));

    bGeoEdgeType = this.bEdgeService.create(BusinessGeoEdgeTypeView.build(USATestData.ORG_PPP.getCode(), "TEST_GEO_EDGE", new LocalizedValue("TEST_GEO_EDGE"), new LocalizedValue("TEST_GEO_EDGE"), btype.getCode(), EdgeDirection.PARENT));

    dagType = this.dagService.create("TEST_DAG", new LocalizedValue("TEST_DAG"), new LocalizedValue("TEST_DAG"), 0L);

    undirectedType = this.undirectedService.create("TEST_UN", new LocalizedValue("TEST_UN"), new LocalizedValue("TEST_UN"), 0L);

    this.repoService.refreshMetadataCache();
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
    if (bGeoEdgeType != null)
    {
      this.bEdgeService.delete(bGeoEdgeType);
    }

    if (bEdgeType != null)
    {
      this.bEdgeService.delete(bEdgeType);
    }

    if (btype != null)
    {
      this.bTypeService.delete(btype);
    }

    if (dagType != null)
    {
      this.dagService.delete(dagType);
    }

    if (undirectedType != null)
    {
      this.undirectedService.delete(undirectedType);
    }

    super.afterClassSetup();
  }

  @Before
  @Request
  public void setUp()
  {
    cleanUpExtra();

    testData.setUpInstanceData();

    testData.logIn(USATestData.USER_NPS_RA);

    pObject = createBusinessObject("P_CODE");
    cObject = createBusinessObject("C_CODE");

    addBusinessEdge();
    addDirectedAcyclicEdge();
    addUndirectedEdge();
  }

  protected String addUndirectedEdge()
  {
    String edgeUid = UUID.randomUUID().toString();

    ServerGeoObjectEventBuilder builder = new ServerGeoObjectEventBuilder(this.gObjectService);
    builder.setObject(USATestData.COLORADO.getServerObject());
    builder.addEdge(USATestData.CANADA.getServerObject(), undirectedType, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE, edgeUid, USATestData.SOURCE.getDataSource(), ImportStrategy.NEW_ONLY, false);

    builder.build().stream().forEach(event -> {
      gateway.publish(GenericEventMessage.asEventMessage(event));
    });

    return edgeUid;
  }

  protected String addDirectedAcyclicEdge()
  {
    TestGeoObjectInfo source = USATestData.COLORADO;
    TestGeoObjectInfo target = USATestData.CANADA;

    return addDirectedAcyclicEdge(source, target);
  }

  protected String addDirectedAcyclicEdge(TestGeoObjectInfo source, TestGeoObjectInfo target)
  {
    String edgeUid = UUID.randomUUID().toString();

    ServerGeoObjectEventBuilder builder = new ServerGeoObjectEventBuilder(this.gObjectService);
    builder.setObject(source.getServerObject());
    builder.addEdge(target.getServerObject(), dagType, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE, edgeUid, USATestData.SOURCE.getDataSource(), ImportStrategy.NEW_ONLY, false);

    builder.build().stream().forEach(event -> {
      gateway.publish(GenericEventMessage.asEventMessage(event));
    });

    return edgeUid;
  }

  protected void addBusinessEdge()
  {
    List<Pair<VertexComponent, BusinessEdgeType>> targets = Arrays.asList( //
        new Pair<VertexComponent, BusinessEdgeType>(pObject, bEdgeType), //
        new Pair<VertexComponent, BusinessEdgeType>(USATestData.COLORADO.getServerObject(), bGeoEdgeType) //
    );

    createBusinessEdges(cObject, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE, USATestData.SOURCE.getDataSource(), targets);
  }

  protected BusinessObject createBusinessObject(String code)
  {
    return createBusinessObject(code, btype, USATestData.SOURCE.getDataSource());
  }

  @After
  @Request
  public void tearDown()
  {
    if (cObject != null)
    {
      this.bObjectService.delete(cObject);

      cObject = null;
    }

    if (pObject != null)
    {
      this.bObjectService.delete(pObject);

      pObject = null;
    }

    testData.logOut();

    cleanUpExtra();

    testData.tearDownInstanceData();
  }

  @Request
  public void cleanUpExtra()
  {
    TestDataSet.deleteAllListData();

    this.store.truncate();

    this.projection.clearCache();
  }

  protected PublishDTO getPublishDTO()
  {
    PublishDTO dto = new PublishDTO("USA Geospatial Graph", USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
    dto.addGeoObjectType(testData.getManagedGeoObjectTypes().stream().map(t -> t.getCode()).toArray(s -> new String[s]));
    dto.addHierarchyType(testData.getManagedHierarchyTypes().stream().map(t -> t.getCode()).toArray(s -> new String[s]));
    dto.addBusinessType(btype.getCode());
    dto.addBusinessEdgeType(bEdgeType.getCode(), bGeoEdgeType.getCode());
    dto.addDagType(dagType.getCode());
    dto.addUndirectedType(undirectedType.getCode());
    return dto;
  }

}
