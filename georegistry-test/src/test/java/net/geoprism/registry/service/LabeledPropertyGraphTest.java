/**
 *
 */
package net.geoprism.registry.service;

import java.util.List;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.constants.ComponentInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.graph.MdEdgeInfo;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.metadata.MdEdge;
import com.runwaysdk.system.metadata.MdVertex;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryQuery;
import com.runwaysdk.system.scheduler.SchedulerManager;

import net.geoprism.graph.ChangeFrequency;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.GraphTypeReference;
import net.geoprism.graph.GraphTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.IncrementalLabeledPropertyGraphType;
import net.geoprism.graph.IntervalLabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeEntry;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.graph.PublishLabeledPropertyGraphTypeVersionJob;
import net.geoprism.graph.PublishLabeledPropertyGraphTypeVersionJobQuery;
import net.geoprism.graph.SingleLabeledPropertyGraphType;
import net.geoprism.ontology.Classifier;
import net.geoprism.registry.DirectedAcyclicGraphType;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.LabeledPropertyGraphTypeBuilder;
import net.geoprism.registry.LocalRegistryConnectorBuilder;
import net.geoprism.registry.Organization;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.USADatasetTest;
import net.geoprism.registry.classification.ClassificationTypeTest;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.lpg.TreeStrategyConfiguration;
import net.geoprism.registry.lpg.adapter.RegistryConnectorFactory;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.business.ClassificationBusinessServiceIF;
import net.geoprism.registry.service.business.ClassificationTypeBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.GraphRepoServiceIF;
import net.geoprism.registry.service.business.GraphTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.JsonGraphVersionPublisherService;
import net.geoprism.registry.service.business.LabeledPropertyGraphJsonExporterService;
import net.geoprism.registry.service.business.LabeledPropertyGraphSynchronizationBusinessServiceIF;
import net.geoprism.registry.service.business.LabeledPropertyGraphTypeBusinessServiceIF;
import net.geoprism.registry.service.business.LabeledPropertyGraphTypeEntryBusinessServiceIF;
import net.geoprism.registry.service.business.LabeledPropertyGraphTypeVersionBusinessServiceIF;
import net.geoprism.registry.service.request.LabeledPropertyGraphTypeServiceIF;
import net.geoprism.registry.test.SchedulerTestUtils;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestHierarchyTypeInfo;
import net.geoprism.registry.test.USATestData;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class LabeledPropertyGraphTest extends USADatasetTest implements InstanceTestClassListener
{
  private static String                                        CODE = "Test Term";

  private static ClassificationType                            type;

  private static AttributeTermType                             testTerm;

  private static AttributeClassificationType                   testClassification;

  @Autowired
  private LabeledPropertyGraphTypeServiceIF                    service;

  @Autowired
  private LabeledPropertyGraphTypeVersionBusinessServiceIF     versionService;

  @Autowired
  private LabeledPropertyGraphTypeBusinessServiceIF            typeService;

  @Autowired
  private LabeledPropertyGraphTypeEntryBusinessServiceIF       entryService;

  @Autowired
  private LabeledPropertyGraphSynchronizationBusinessServiceIF synchronizationService;

  @Autowired
  private GeoObjectTypeSnapshotBusinessServiceIF               objectService;

  @Autowired
  private HierarchyTypeSnapshotBusinessServiceIF               hierarchyService;
  
  @Autowired
  private GraphTypeSnapshotBusinessServiceIF                   graphSnapshotService;

  @Autowired
  private GeoObjectTypeBusinessServiceIF                       oTypeService;

  @Autowired
  private GraphRepoServiceIF                                   repoService;

  @Autowired
  private JsonGraphVersionPublisherService                     publisherService;

  @Autowired
  private LabeledPropertyGraphJsonExporterService              exporterService;

  @Autowired
  private ClassificationTypeBusinessServiceIF                  cTypeService;

  @Autowired
  private ClassificationBusinessServiceIF                      cService;

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
    type = this.cTypeService.apply(ClassificationTypeTest.createMock());

    Classification root = this.cService.newInstance(type);
    root.setCode(CODE);
    root.setDisplayLabel(new LocalizedValue("Test Classification"));
    this.cService.apply(root, null);

    testClassification = (AttributeClassificationType) AttributeType.factory("testClassification", new LocalizedValue("testClassificationLocalName"), new LocalizedValue("testClassificationLocalDescrip"), AttributeClassificationType.TYPE, false, false, true);
    testClassification.setClassificationType(type.getCode());
    testClassification.setRootTerm(root.toTerm());

    ServerGeoObjectType got = ServerGeoObjectType.get(USATestData.STATE.getCode());
    testClassification = (AttributeClassificationType) this.oTypeService.createAttributeType(got, testClassification.toJSON().toString());

    testTerm = (AttributeTermType) AttributeType.factory("testTerm", new LocalizedValue("testTermLocalName"), new LocalizedValue("testTermLocalDescrip"), AttributeTermType.TYPE, false, false, true);
    testTerm = (AttributeTermType) this.oTypeService.createAttributeType(got, testTerm.toJSON().toString());

    Term term = new Term("TERM_1", new LocalizedValue("Term 1"), new LocalizedValue("Term 1"));

    Classifier classifier = TermConverter.createClassifierFromTerm(testTerm.getRootTerm().getCode(), term);
    term = TermConverter.buildTermFromClassifier(classifier);

    USATestData.COLORADO.setDefaultValue(testClassification.getName(), CODE);
    USATestData.COLORADO.setDefaultValue(testTerm.getName(), term);

    this.repoService.refreshMetadataCache();
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
    super.afterClassSetup();

    USATestData.COLORADO.removeDefaultValue(testClassification.getName());
    USATestData.COLORADO.removeDefaultValue(testTerm.getName());

    if (type != null)
    {
      this.cTypeService.delete(type);
    }
  }

  @Before
  public void setUp()
  {
    cleanUpExtra();

    testData.setUpInstanceData();

    testData.logIn(USATestData.USER_NPS_RA);
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    cleanUpExtra();

    testData.tearDownInstanceData();
  }

  @Request
  public void cleanUpExtra()
  {
    TestDataSet.deleteAllListData();
  }

  @Test
  @Request
  public void testSingleLabeledPropertyGraphTypeSerialization()
  {
    SingleLabeledPropertyGraphType type = new SingleLabeledPropertyGraphType();
    type.setHierarchy(USATestData.HIER_ADMIN.getCode());
    type.getDisplayLabel().setValue("Test List");
    type.setCode("TEST_CODE");
    type.getDescription().setValue("My Overal Description");
    type.setValidOn(USATestData.DEFAULT_OVER_TIME_DATE);
    type.setStrategyType(SingleLabeledPropertyGraphType.TREE);
    type.setOrganization(USATestData.ORG_NPS.getServerObject().getOrganization());

    JsonObject json = type.toJSON();
    SingleLabeledPropertyGraphType test = (SingleLabeledPropertyGraphType) this.typeService.fromJSON(json);

    Assert.assertEquals(type.getDisplayLabel().getValue(), test.getDisplayLabel().getValue());
    Assert.assertEquals(type.getDescription().getValue(), test.getDescription().getValue());
    Assert.assertEquals(type.getCode(), test.getCode());
    Assert.assertEquals(type.getHierarchy(), test.getHierarchy());
    Assert.assertEquals(type.getValidOn(), test.getValidOn());
  }

  @Test
  @Request
  public void testIntervalLabeledPropertyGraphTypeSerialization()
  {
    JsonObject interval = new JsonObject();
    interval.addProperty(IntervalLabeledPropertyGraphType.START_DATE, GeoRegistryUtil.formatDate(USATestData.DEFAULT_OVER_TIME_DATE, false));
    interval.addProperty(IntervalLabeledPropertyGraphType.END_DATE, GeoRegistryUtil.formatDate(USATestData.DEFAULT_END_TIME_DATE, false));

    JsonArray intervalJson = new JsonArray();
    intervalJson.add(interval);

    IntervalLabeledPropertyGraphType type = new IntervalLabeledPropertyGraphType();
    type.getDisplayLabel().setValue("Test List");
    type.setCode("TEST_CODE");
    type.setHierarchy(USATestData.HIER_ADMIN.getCode());
    type.getDescription().setValue("My Overal Description");
    type.setIntervalJson(intervalJson.toString());
    type.setStrategyType(SingleLabeledPropertyGraphType.TREE);
    type.setOrganization(USATestData.ORG_NPS.getServerObject().getOrganization());

    JsonObject json = type.toJSON();
    IntervalLabeledPropertyGraphType test = (IntervalLabeledPropertyGraphType) this.typeService.fromJSON(json);

    Assert.assertEquals(type.getDisplayLabel().getValue(), test.getDisplayLabel().getValue());
    Assert.assertEquals(type.getDescription().getValue(), test.getDescription().getValue());
    Assert.assertEquals(type.getCode(), test.getCode());
    Assert.assertEquals(type.getHierarchy(), test.getHierarchy());
    Assert.assertEquals(type.getIntervalJson(), test.getIntervalJson());
  }

  @Test
  @Request
  public void testIncrementLabeledPropertyGraphTypeSerialization()
  {
    IncrementalLabeledPropertyGraphType type = new IncrementalLabeledPropertyGraphType();
    type.getDisplayLabel().setValue("Test List");
    type.setCode("TEST_CODE");
    type.setHierarchy(USATestData.HIER_ADMIN.getCode());
    type.getDescription().setValue("My Overal Description");
    type.setPublishingStartDate(USATestData.DEFAULT_OVER_TIME_DATE);
    type.addFrequency(ChangeFrequency.ANNUAL);
    type.setStrategyType(SingleLabeledPropertyGraphType.TREE);
    type.setOrganization(USATestData.ORG_NPS.getServerObject().getOrganization());

    JsonObject json = type.toJSON();
    IncrementalLabeledPropertyGraphType test = (IncrementalLabeledPropertyGraphType) this.typeService.fromJSON(json);

    Assert.assertEquals(type.getDisplayLabel().getValue(), test.getDisplayLabel().getValue());
    Assert.assertEquals(type.getDescription().getValue(), test.getDescription().getValue());
    Assert.assertEquals(type.getCode(), test.getCode());
    Assert.assertEquals(type.getHierarchy(), test.getHierarchy());
    Assert.assertEquals(type.getFrequency().get(0), test.getFrequency().get(0));
    Assert.assertEquals(type.getPublishingStartDate(), test.getPublishingStartDate());
  }

  @Test
  @Request
  public void testCreate()
  {
    JsonObject json = getJson(USATestData.USA, USATestData.HIER_ADMIN);

    LabeledPropertyGraphType test1 = this.typeService.apply(json);

    try
    {
      List<LabeledPropertyGraphTypeEntry> entries = this.typeService.getEntries(test1);

      Assert.assertEquals(1, entries.size());

      LabeledPropertyGraphTypeEntry entry = entries.get(0);

      List<LabeledPropertyGraphTypeVersion> versions = this.entryService.getVersions(entry);

      Assert.assertEquals(1, versions.size());

      LabeledPropertyGraphTypeVersion version = versions.get(0);

      List<GeoObjectTypeSnapshot> vertices = this.versionService.getTypes(version);

      Assert.assertEquals(11, vertices.size());

      List<GraphTypeSnapshot> edges = this.versionService.getGraphSnapshots(version);

      Assert.assertEquals(1, edges.size());
    }
    finally
    {
      this.typeService.delete(test1);
    }
  }

  @Test
  @Request
  public void testPublish()
  {
    JsonObject json = getJson(USATestData.USA, USATestData.HIER_ADMIN);

    LabeledPropertyGraphType test1 = this.typeService.apply(json);

    try
    {
      List<LabeledPropertyGraphTypeEntry> entries = this.typeService.getEntries(test1);

      Assert.assertEquals(1, entries.size());

      LabeledPropertyGraphTypeEntry entry = entries.get(0);

      List<LabeledPropertyGraphTypeVersion> versions = this.entryService.getVersions(entry);

      Assert.assertEquals(1, versions.size());

      LabeledPropertyGraphTypeVersion version = versions.get(0);
      this.versionService.publish(version);

      GeoObjectTypeSnapshot graphVertex = this.objectService.get(version, USATestData.COUNTRY.getCode());
      MdVertex mdVertex = graphVertex.getGraphMdVertex();

      HierarchyTypeSnapshot graphEdge = this.hierarchyService.get(version, USATestData.HIER_ADMIN.getCode());
      MdEdge mdEdge = graphEdge.getGraphMdEdge();

      GraphQuery<VertexObject> query = new GraphQuery<VertexObject>("SELECT FROM " + mdVertex.getDbClassName());
      List<VertexObject> results = query.getResults();

      Assert.assertEquals(1, results.size());

      VertexObject result = results.get(0);

      Assert.assertEquals(USATestData.USA.getCode(), result.getObjectValue(DefaultAttribute.CODE.getName()));

      List<VertexObject> children = result.getChildren(mdEdge.definesType(), VertexObject.class);

      Assert.assertEquals(2, children.size());

    }
    finally
    {
      this.typeService.delete(test1);
    }
  }
  
  @Test
  @Request
  public void testPublishDAG()
  {
    MdEdgeDAO mdEdge = MdEdgeDAO.newInstance();
    mdEdge.setValue(MdEdgeInfo.NAME, "TestDag");
    mdEdge.setValue(MdEdgeInfo.PACKAGE, "net.geoprism.registry.service.test.TestDag");
    mdEdge.setStructValue(MdEdgeInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, "TestDag for LabeledPropertyGraphTest");
    mdEdge.setStructValue(MdEdgeInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, "TestDag for LabeledPropertyGraphTest");
    mdEdge.setValue(MdEdgeInfo.PARENT_MD_VERTEX, USATestData.COUNTRY.getServerObject().getMdVertex().getOid());
    mdEdge.setValue(MdEdgeInfo.CHILD_MD_VERTEX, USATestData.STATE.getServerObject().getMdVertex().getOid());
    mdEdge.apply();
    
    DirectedAcyclicGraphType dagType = new DirectedAcyclicGraphType();
    dagType.setCode("TestDag");
    dagType.getDisplayLabel().setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, "TestDag");
    dagType.setMdEdgeId(mdEdge.getOid());
    dagType.apply();
    
    JsonObject json = getJson(dagType, new String[] { USATestData.COUNTRY.getCode(), USATestData.STATE.getCode() }, USATestData.ORG_NPS.getServerObject().getOrganization());

    LabeledPropertyGraphType test1 = this.typeService.apply(json);

    try
    {
      USATestData.USA.getServerObject().getVertex().addChild(USATestData.COLORADO.getServerObject().getVertex(), mdEdge).apply();
      
      List<LabeledPropertyGraphTypeEntry> entries = this.typeService.getEntries(test1);

      Assert.assertEquals(1, entries.size());

      LabeledPropertyGraphTypeEntry entry = entries.get(0);

      List<LabeledPropertyGraphTypeVersion> versions = this.entryService.getVersions(entry);

      Assert.assertEquals(1, versions.size());

      LabeledPropertyGraphTypeVersion version = versions.get(0);
      this.versionService.publish(version);

      GeoObjectTypeSnapshot graphVertex = this.objectService.get(version, USATestData.COUNTRY.getCode());
      MdVertex mdVertex = graphVertex.getGraphMdVertex();

//      GraphTypeSnapshot graphEdge = this.graphSnapshotService.get(version, GraphType.getTypeCode(dagType), dagType.getCode());

      GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>("SELECT FROM " + mdVertex.getDbClassName());
      List<EdgeObject> results = query.getResults();

      Assert.assertEquals(1, results.size());

//      EdgeObject result = results.get(0);
//      
//      String codeValue = result.getObjectValue(DefaultAttribute.CODE.getName());
//      Assert.assertTrue(codeValue.equals(USATestData.USA.getCode()) || codeValue.equals(USATestData.COLORADO.getCode()));
//
//      List<VertexObject> children = result.getChildren(graphEdge.getGraphMdEdge().definesType(), VertexObject.class);
//
//      Assert.assertEquals(2, children.size());
    }
    finally
    {
      this.typeService.delete(test1);
      dagType.delete();
      mdEdge.delete();
    }
  }

  @Test
  @Request
  public void testPublishJob()
  {
    JsonObject json = getJson(USATestData.USA, USATestData.HIER_ADMIN);

    LabeledPropertyGraphType test1 = this.typeService.apply(json);

    try
    {
      List<LabeledPropertyGraphTypeEntry> entries = this.typeService.getEntries(test1);

      Assert.assertEquals(1, entries.size());

      LabeledPropertyGraphTypeEntry entry = entries.get(0);

      List<LabeledPropertyGraphTypeVersion> versions = this.entryService.getVersions(entry);

      Assert.assertEquals(1, versions.size());

      LabeledPropertyGraphTypeVersion version = versions.get(0);

      PublishLabeledPropertyGraphTypeVersionJob job = new PublishLabeledPropertyGraphTypeVersionJob();
      // job.setRunAsUserId(Session.getCurrentSession().getUser().getOid());
      job.setVersion(version);
      job.setGraphType(version.getGraphType());
      job.apply();

      job.start();

      LabeledPropertyGraphTest.waitUntilPublished(version.getOid());

      GeoObjectTypeSnapshot graphVertex = this.objectService.get(version, USATestData.COUNTRY.getCode());
      MdVertex mdVertex = graphVertex.getGraphMdVertex();

      HierarchyTypeSnapshot graphEdge = this.hierarchyService.get(version, USATestData.HIER_ADMIN.getCode());
      MdEdge mdEdge = graphEdge.getGraphMdEdge();

      GraphQuery<VertexObject> query = new GraphQuery<VertexObject>("SELECT FROM " + mdVertex.getDbClassName());
      List<VertexObject> results = query.getResults();

      Assert.assertEquals(1, results.size());

      VertexObject result = results.get(0);

      Assert.assertEquals(USATestData.USA.getCode(), result.getObjectValue(DefaultAttribute.CODE.getName()));

      List<VertexObject> children = result.getChildren(mdEdge.definesType(), VertexObject.class);

      Assert.assertEquals(2, children.size());

    }
    finally
    {
      this.typeService.delete(test1);
    }
  }

  @Test
  @Request
  public void testLabeledPropertyGraphJsonExporter()
  {
    JsonObject json = getJson(USATestData.USA, USATestData.HIER_ADMIN);

    LabeledPropertyGraphType test1 = this.typeService.apply(json);

    try
    {
      List<LabeledPropertyGraphTypeEntry> entries = this.typeService.getEntries(test1);

      Assert.assertEquals(1, entries.size());

      LabeledPropertyGraphTypeEntry entry = entries.get(0);

      List<LabeledPropertyGraphTypeVersion> versions = this.entryService.getVersions(entry);

      Assert.assertEquals(1, versions.size());

      LabeledPropertyGraphTypeVersion version = versions.get(0);
      this.versionService.publish(version);

      JsonObject export = this.exporterService.export(version);

      System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(export));

      this.versionService.truncate(version);

      this.publisherService.publish(null, version, export);

      GeoObjectTypeSnapshot graphVertex = this.objectService.get(version, USATestData.COUNTRY.getCode());
      MdVertex mdVertex = graphVertex.getGraphMdVertex();

      HierarchyTypeSnapshot graphEdge = this.hierarchyService.get(version, USATestData.HIER_ADMIN.getCode());
      MdEdge mdEdge = graphEdge.getGraphMdEdge();

      GraphQuery<VertexObject> query = new GraphQuery<VertexObject>("SELECT FROM " + mdVertex.getDbClassName());
      List<VertexObject> results = query.getResults();

      Assert.assertEquals(1, results.size());

      VertexObject result = results.get(0);

      Assert.assertEquals(USATestData.USA.getCode(), result.getObjectValue(DefaultAttribute.CODE.getName()));

      List<VertexObject> children = result.getChildren(mdEdge.definesType(), VertexObject.class);

      Assert.assertEquals(2, children.size());

      children.forEach(child -> {
        String code = child.getObjectValue(DefaultAttribute.CODE.getName());

        if (code.equals(USATestData.COLORADO.getCode()))
        {
          Assert.assertNotNull(child.getObjectValue(DefaultAttribute.GEOMETRY.getName()));
          Assert.assertNotNull(child.getObjectValue(testClassification.getName()));
        }
      });
    }
    finally
    {
      this.typeService.delete(test1);
    }
  }

  @Test
  public void testServiceApply()
  {
    JsonObject json = getJson(USATestData.USA, USATestData.HIER_ADMIN);

    JsonObject result = service.apply(testData.clientRequest.getSessionId(), json);

    String oid = result.get(ComponentInfo.OID).getAsString();

    try
    {
      LabeledPropertyGraphTest.waitUntilPublished(oid);
    }
    finally
    {
      service.remove(testData.clientRequest.getSessionId(), oid);
    }
  }

  @Test
  @Request
  public void testJsonExportAndImport()
  {
    JsonObject json = getJson(USATestData.USA, USATestData.HIER_ADMIN);

    LabeledPropertyGraphType test1 = this.typeService.apply(json);

    try
    {
      System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(test1.toJSON()));

      List<LabeledPropertyGraphTypeEntry> entries = this.typeService.getEntries(test1);

      Assert.assertEquals(1, entries.size());

      LabeledPropertyGraphTypeEntry entry = entries.get(0);
      JsonObject entryJson = entry.toJSON();

      // System.out.println(new
      // GsonBuilder().setPrettyPrinting().create().toJson(entryJson));

      List<LabeledPropertyGraphTypeVersion> versions = this.entryService.getVersions(entry);

      Assert.assertEquals(1, versions.size());

      LabeledPropertyGraphTypeVersion version = versions.get(0);
      JsonObject versionJson = this.versionService.toJSON(version, true);

      // System.out.println(new
      // GsonBuilder().setPrettyPrinting().create().toJson(versionJson));

      this.entryService.delete(entry);

      entry = this.entryService.create(test1, entryJson);
      version = this.versionService.create(entry, versionJson);

      List<GeoObjectTypeSnapshot> vertices = this.versionService.getTypes(version);

      Assert.assertEquals(11, vertices.size());

      List<GraphTypeSnapshot> edges = this.versionService.getGraphSnapshots(version);

      Assert.assertEquals(1, edges.size());

    }
    finally
    {
      this.typeService.delete(test1);
    }
  }

  @Test
  public void testSynchronization()
  {
    TestDataSet.executeRequestAsUser(USATestData.USER_ADMIN, () -> {
      JsonObject json = getJson(USATestData.USA, USATestData.HIER_ADMIN);

      LabeledPropertyGraphType test1 = this.typeService.apply(json);

      try
      {
        List<LabeledPropertyGraphTypeEntry> entries = this.typeService.getEntries(test1);

        Assert.assertEquals(1, entries.size());

        LabeledPropertyGraphTypeEntry entry = entries.get(0);

        List<LabeledPropertyGraphTypeVersion> versions = this.entryService.getVersions(entry);

        Assert.assertEquals(1, versions.size());

        LabeledPropertyGraphTypeVersion version = versions.get(0);

        RegistryConnectorFactory.setBuilder(new LocalRegistryConnectorBuilder(this.service));

        LabeledPropertyGraphSynchronization synchronization = new LabeledPropertyGraphSynchronization();
        synchronization.setUrl("localhost");
        synchronization.setRemoteType(test1.getOid());
        synchronization.getDisplayLabel().setValue("Test");
        synchronization.setRemoteEntry(entry.getOid());
        synchronization.setForDate(entry.getForDate());
        synchronization.setRemoteVersion(version.getOid());
        synchronization.setVersionNumber(version.getVersionNumber());
        synchronization.apply();

        try
        {
          this.synchronizationService.execute(synchronization);

          synchronization = this.synchronizationService.get(synchronization.getOid());

          version = synchronization.getVersion();

          List<GeoObjectTypeSnapshot> vertices = this.versionService.getTypes(version);

          Assert.assertEquals(11, vertices.size());

          List<GraphTypeSnapshot> edges = this.versionService.getGraphSnapshots(version);

          Assert.assertEquals(1, edges.size());
        }
        finally
        {
          this.synchronizationService.delete(synchronization);
        }

      }
      finally
      {
        this.typeService.delete(test1);
      }
    });
  }
  
  @Request
  public static JsonObject getJson(GraphType graphType, String[] geoObjectTypeCodes, Organization organization)
  {
    LabeledPropertyGraphTypeBuilder builder = new LabeledPropertyGraphTypeBuilder();
    builder.setGraphTypes(new GraphTypeReference[] { new GraphTypeReference(GraphType.getTypeCode(graphType), graphType.getCode()) });
    builder.setGeoObjectTypeCodes(geoObjectTypeCodes);
    builder.setStrategyType(LabeledPropertyGraphType.GRAPH);
    builder.setOrganization(organization);

    return builder.buildJSON();
  }

  @Request
  public static JsonObject getJson(TestGeoObjectInfo root, TestHierarchyTypeInfo ht)
  {
    LabeledPropertyGraphTypeBuilder builder = new LabeledPropertyGraphTypeBuilder();
    builder.setHt(ht);
    builder.setConfiguration(new TreeStrategyConfiguration(root.getCode(), root.getGeoObjectType().getCode()));

    return builder.buildJSON();
  }

  @Request
  public static void waitUntilPublished(String oid)
  {
    List<? extends JobHistory> histories = null;
    int waitTime = 0;

    while (histories == null)
    {
      if (waitTime > 10000)
      {
        Assert.fail("Job was never scheduled. Unable to find any associated history.");
      }

      QueryFactory qf = new QueryFactory();

      PublishLabeledPropertyGraphTypeVersionJobQuery jobQuery = new PublishLabeledPropertyGraphTypeVersionJobQuery(qf);
      jobQuery.WHERE(jobQuery.getVersion().EQ(oid));
      jobQuery.OR(jobQuery.getGraphType().EQ(oid));

      JobHistoryQuery jhq = new JobHistoryQuery(qf);
      jhq.WHERE(jhq.job(jobQuery));

      List<? extends JobHistory> potentialHistories = jhq.getIterator().getAll();

      if (potentialHistories.size() > 0)
      {
        histories = potentialHistories;
      }
      else
      {
        try
        {
          Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
          e.printStackTrace();
          Assert.fail("Interrupted while waiting");
        }

        waitTime += 1000;
      }
    }

    for (JobHistory history : histories)
    {
      try
      {
        SchedulerTestUtils.waitUntilStatus(history.getOid(), AllJobStatus.SUCCESS);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
        Assert.fail("Interrupted while waiting");
      }
    }
  }

}
