package net.geoprism.registry;

import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.SchedulerManager;

import net.geoprism.registry.axon.config.RegistryEventStore;
import net.geoprism.registry.axon.event.repository.BusinessObjectEventBuilder;
import net.geoprism.registry.axon.event.repository.ServerGeoObjectEventBuilder;
import net.geoprism.registry.axon.projection.RepositoryProjection;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.EdgeDirection;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessObjectBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.service.business.DirectedAcyclicGraphTypeBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.GraphRepoServiceIF;
import net.geoprism.registry.service.business.UndirectedGraphTypeBusinessServiceIF;
import net.geoprism.registry.test.TestDataSet;
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
  protected BusinessTypeBusinessServiceIF             bTypeService;

  @Autowired
  protected BusinessEdgeTypeBusinessServiceIF         bEdgeService;

  @Autowired
  protected BusinessObjectBusinessServiceIF           bObjectService;

  @Autowired
  protected GeoObjectBusinessServiceIF                gObjectService;

  @Autowired
  protected CommandGateway                            gateway;

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

    bEdgeType = this.bEdgeService.create(BusinessEdgeTypeView.build(USATestData.ORG_PPP.getCode(), "TEST_B_EDGE", new LocalizedValue("TEST_B_EDGE"), new LocalizedValue("TEST_B_EDGE"), btype.getCode(), btype.getCode()));

    bGeoEdgeType = this.bEdgeService.createGeoEdge(BusinessGeoEdgeTypeView.build(USATestData.ORG_PPP.getCode(), "TEST_GEO_EDGE", new LocalizedValue("TEST_GEO_EDGE"), new LocalizedValue("TEST_GEO_EDGE"), btype.getCode(), EdgeDirection.PARENT));

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

  protected void addUndirectedEdge()
  {
    ServerGeoObjectEventBuilder builder = new ServerGeoObjectEventBuilder(this.gObjectService);
    builder.setObject(USATestData.COLORADO.getServerObject());
    builder.addEdge(USATestData.CANADA.getServerObject(), undirectedType, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE, UUID.randomUUID().toString(), USATestData.SOURCE.getDataSource(), false);

    this.gateway.sendAndWait(builder.build());
  }

  protected void addDirectedAcyclicEdge()
  {
    ServerGeoObjectEventBuilder builder = new ServerGeoObjectEventBuilder(this.gObjectService);
    builder.setObject(USATestData.COLORADO.getServerObject());
    builder.addEdge(USATestData.CANADA.getServerObject(), dagType, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE, UUID.randomUUID().toString(), USATestData.SOURCE.getDataSource(), false);

    this.gateway.sendAndWait(builder.build());
  }

  protected void addBusinessEdge()
  {
    BusinessObjectEventBuilder builder = new BusinessObjectEventBuilder(bObjectService);
    builder.setObject(cObject);
    builder.addParent(pObject, bEdgeType, USATestData.SOURCE.getDataSource(), false);
    builder.addGeoObject(bGeoEdgeType, USATestData.COLORADO.getServerObject(), EdgeDirection.PARENT, USATestData.SOURCE.getDataSource());

    this.gateway.sendAndWait(builder.build());
  }

  protected BusinessObject createBusinessObject(String code)
  {
    BusinessObject object = this.bObjectService.newInstance(btype);
    object.setCode(code);
    object.setValue(DefaultAttribute.DATA_SOURCE.getName(), USATestData.SOURCE.getDataSource());

    BusinessObjectEventBuilder builder = new BusinessObjectEventBuilder(bObjectService);
    builder.setObject(object, true);

    this.gateway.sendAndWait(builder.build());

    return this.bObjectService.getByCode(btype, builder.getCode());
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
    PublishDTO dto = new PublishDTO(USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
    dto.addGeoObjectType(testData.getManagedGeoObjectTypes().stream().map(t -> t.getCode()).toArray(s -> new String[s]));
    dto.addHierarchyType(testData.getManagedHierarchyTypes().stream().map(t -> t.getCode()).toArray(s -> new String[s]));
    dto.addBusinessType(btype.getCode());
    dto.addBusinessEdgeType(bEdgeType.getCode(), bGeoEdgeType.getCode());
    dto.addDagType(dagType.getCode());
    dto.addUndirectedType(undirectedType.getCode());
    return dto;
  }

}
