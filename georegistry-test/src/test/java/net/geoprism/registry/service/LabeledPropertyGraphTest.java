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
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.constants.ComponentInfo;
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
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.IncrementalLabeledPropertyGraphType;
import net.geoprism.graph.IntervalLabeledPropertyGraphType;
import net.geoprism.graph.JsonGraphVersionPublisher;
import net.geoprism.graph.LabeledPropertyGraphJsonExporter;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeEntry;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.graph.PublishLabeledPropertyGraphTypeVersionJob;
import net.geoprism.graph.PublishLabeledPropertyGraphTypeVersionJobQuery;
import net.geoprism.graph.SingleLabeledPropertyGraphType;
import net.geoprism.graph.TreeStrategyConfiguration;
import net.geoprism.ontology.Classifier;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.LabeledPropertyGraphTypeBuilder;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.classification.ClassificationTypeTest;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.test.SchedulerTestUtils;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestHierarchyTypeInfo;
import net.geoprism.registry.test.USATestData;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class LabeledPropertyGraphTest
{
  private static boolean                     isSetup = false;

  private static String                      CODE    = "Test Term";

  private static ClassificationType          type;

  private static USATestData                 testData;

  private static AttributeTermType           testTerm;

  private static AttributeClassificationType testClassification;

  public static void setupClasses()
  {

    testData = USATestData.newTestData();
    testData.setUpMetadata();

    setUpInReq();

    if (!SchedulerManager.initialized())
    {
      SchedulerManager.start();
    }
    
    isSetup = true;
  }

  @Request
  private static void setUpInReq()
  {
    type = ClassificationType.apply(ClassificationTypeTest.createMock());

    Classification root = Classification.newInstance(type);
    root.setCode(CODE);
    root.setDisplayLabel(new LocalizedValue("Test Classification"));
    root.apply(null);

    testClassification = (AttributeClassificationType) AttributeType.factory("testClassification", new LocalizedValue("testClassificationLocalName"), new LocalizedValue("testClassificationLocalDescrip"), AttributeClassificationType.TYPE, false, false, false);
    testClassification.setClassificationType(type.getCode());
    testClassification.setRootTerm(root.toTerm());

    ServerGeoObjectType got = ServerGeoObjectType.get(USATestData.STATE.getCode());
    testClassification = (AttributeClassificationType) got.createAttributeType(testClassification.toJSON().toString());

    testTerm = (AttributeTermType) AttributeType.factory("testTerm", new LocalizedValue("testTermLocalName"), new LocalizedValue("testTermLocalDescrip"), AttributeTermType.TYPE, false, false, false);
    testTerm = (AttributeTermType) got.createAttributeType(testTerm.toJSON().toString());

    Term term = new Term("TERM_1", new LocalizedValue("Term 1"), new LocalizedValue("Term 1"));

    Classifier classifier = TermConverter.createClassifierFromTerm(testTerm.getRootTerm().getCode(), term);
    term = TermConverter.buildTermFromClassifier(classifier);

    USATestData.COLORADO.setDefaultValue(testClassification.getName(), CODE);
    USATestData.COLORADO.setDefaultValue(testTerm.getName(), term);

    RegistryService.getInstance().refreshMetadataCache();
  }

  @AfterClass
  @Request
  public static void classTearDown()
  {
    if (testData != null)
    {
      testData.tearDownMetadata();
    }

    USATestData.COLORADO.removeDefaultValue(testClassification.getName());
    USATestData.COLORADO.removeDefaultValue(testTerm.getName());

    if (type != null)
    {
      type.delete();
    }
  }

  @Before
  public void setUp()
  {
    // This is a hack to allow for spring injection of classification tasks
    if (!isSetup)
    {
      setupClasses();
    }
    
    
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

    JsonObject json = type.toJSON();
    SingleLabeledPropertyGraphType test = (SingleLabeledPropertyGraphType) LabeledPropertyGraphType.fromJSON(json);

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

    JsonObject json = type.toJSON();
    IntervalLabeledPropertyGraphType test = (IntervalLabeledPropertyGraphType) LabeledPropertyGraphType.fromJSON(json);

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

    JsonObject json = type.toJSON();
    IncrementalLabeledPropertyGraphType test = (IncrementalLabeledPropertyGraphType) LabeledPropertyGraphType.fromJSON(json);

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

    LabeledPropertyGraphType test1 = LabeledPropertyGraphType.apply(json);

    try
    {
      List<LabeledPropertyGraphTypeEntry> entries = test1.getEntries();

      Assert.assertEquals(1, entries.size());

      LabeledPropertyGraphTypeEntry entry = entries.get(0);

      List<LabeledPropertyGraphTypeVersion> versions = entry.getVersions();

      Assert.assertEquals(1, versions.size());

      LabeledPropertyGraphTypeVersion version = versions.get(0);

      List<GeoObjectTypeSnapshot> vertices = version.getTypes();

      Assert.assertEquals(11, vertices.size());

      List<HierarchyTypeSnapshot> edges = version.getHierarchies();

      Assert.assertEquals(1, edges.size());
    }
    finally
    {
      test1.delete();
    }
  }

  @Test
  @Request
  public void testPublish()
  {
    JsonObject json = getJson(USATestData.USA, USATestData.HIER_ADMIN);

    LabeledPropertyGraphType test1 = LabeledPropertyGraphType.apply(json);

    try
    {
      List<LabeledPropertyGraphTypeEntry> entries = test1.getEntries();

      Assert.assertEquals(1, entries.size());

      LabeledPropertyGraphTypeEntry entry = entries.get(0);

      List<LabeledPropertyGraphTypeVersion> versions = entry.getVersions();

      Assert.assertEquals(1, versions.size());

      LabeledPropertyGraphTypeVersion version = versions.get(0);
      version.publishNoAuth();

      GeoObjectTypeSnapshot graphVertex = GeoObjectTypeSnapshot.get(version, USATestData.COUNTRY.getCode());
      MdVertex mdVertex = graphVertex.getGraphMdVertex();

      HierarchyTypeSnapshot graphEdge = HierarchyTypeSnapshot.get(version, USATestData.HIER_ADMIN.getCode());
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
      test1.delete();
    }
  }

  @Test
  @Request
  public void testPublishJob()
  {
    JsonObject json = getJson(USATestData.USA, USATestData.HIER_ADMIN);

    LabeledPropertyGraphType test1 = LabeledPropertyGraphType.apply(json);

    try
    {
      List<LabeledPropertyGraphTypeEntry> entries = test1.getEntries();

      Assert.assertEquals(1, entries.size());

      LabeledPropertyGraphTypeEntry entry = entries.get(0);

      List<LabeledPropertyGraphTypeVersion> versions = entry.getVersions();

      Assert.assertEquals(1, versions.size());

      LabeledPropertyGraphTypeVersion version = versions.get(0);

      PublishLabeledPropertyGraphTypeVersionJob job = new PublishLabeledPropertyGraphTypeVersionJob();
      // job.setRunAsUserId(Session.getCurrentSession().getUser().getOid());
      job.setVersion(version);
      job.setGraphType(version.getGraphType());
      job.apply();

      job.start();

      LabeledPropertyGraphTest.waitUntilPublished(version.getOid());

      GeoObjectTypeSnapshot graphVertex = GeoObjectTypeSnapshot.get(version, USATestData.COUNTRY.getCode());
      MdVertex mdVertex = graphVertex.getGraphMdVertex();

      HierarchyTypeSnapshot graphEdge = HierarchyTypeSnapshot.get(version, USATestData.HIER_ADMIN.getCode());
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
      test1.delete();
    }
  }

  @Test
  @Request
  public void testLabeledPropertyGraphJsonExporter()
  {
    JsonObject json = getJson(USATestData.USA, USATestData.HIER_ADMIN);

    LabeledPropertyGraphType test1 = LabeledPropertyGraphType.apply(json);

    try
    {
      List<LabeledPropertyGraphTypeEntry> entries = test1.getEntries();

      Assert.assertEquals(1, entries.size());

      LabeledPropertyGraphTypeEntry entry = entries.get(0);

      List<LabeledPropertyGraphTypeVersion> versions = entry.getVersions();

      Assert.assertEquals(1, versions.size());

      LabeledPropertyGraphTypeVersion version = versions.get(0);
      version.publishNoAuth();

      LabeledPropertyGraphJsonExporter exporter = new LabeledPropertyGraphJsonExporter(version);
      JsonObject export = exporter.export();

      System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(export));

      version.truncate();

      new JsonGraphVersionPublisher(version).publish(export);

      GeoObjectTypeSnapshot graphVertex = GeoObjectTypeSnapshot.get(version, USATestData.COUNTRY.getCode());
      MdVertex mdVertex = graphVertex.getGraphMdVertex();

      HierarchyTypeSnapshot graphEdge = HierarchyTypeSnapshot.get(version, USATestData.HIER_ADMIN.getCode());
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
          Assert.assertNotNull(child.getObjectValue(testClassification.getName()));
        }
      });
    }
    finally
    {
      test1.delete();
    }
  }

  @Test
  public void testServiceApply()
  {
    JsonObject json = getJson(USATestData.USA, USATestData.HIER_ADMIN);

    LabeledPropertyGraphTypeService service = new LabeledPropertyGraphTypeService();
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

    LabeledPropertyGraphType test1 = LabeledPropertyGraphType.apply(json);

    try
    {
      List<LabeledPropertyGraphTypeEntry> entries = test1.getEntries();

      Assert.assertEquals(1, entries.size());

      LabeledPropertyGraphTypeEntry entry = entries.get(0);
      JsonObject entryJson = entry.toJSON();

      List<LabeledPropertyGraphTypeVersion> versions = entry.getVersions();

      Assert.assertEquals(1, versions.size());

      LabeledPropertyGraphTypeVersion version = versions.get(0);
      JsonObject versionJson = version.toJSON(true);

      System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(versionJson));

      entry.delete();

      entry = LabeledPropertyGraphTypeEntry.create(test1, entryJson);
      version = LabeledPropertyGraphTypeVersion.create(entry, versionJson);

      List<GeoObjectTypeSnapshot> vertices = version.getTypes();

      Assert.assertEquals(11, vertices.size());

      List<HierarchyTypeSnapshot> edges = version.getHierarchies();

      Assert.assertEquals(1, edges.size());

    }
    finally
    {
      test1.delete();
    }
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
  private static void waitUntilPublished(String oid)
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
