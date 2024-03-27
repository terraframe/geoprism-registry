/**
 *
 */
package net.geoprism.registry.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.SchedulerManager;

import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeEntry;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.graph.PublishLabeledPropertyGraphTypeVersionJob;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.USADatasetTest;
import net.geoprism.registry.lpg.jena.JenaBridge;
import net.geoprism.registry.lpg.jena.JenaConnector;
import net.geoprism.registry.service.business.ClassificationBusinessServiceIF;
import net.geoprism.registry.service.business.ClassificationTypeBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.GraphRepoServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.JsonGraphVersionPublisherService;
import net.geoprism.registry.service.business.LabeledPropertyGraphJsonExporterService;
import net.geoprism.registry.service.business.LabeledPropertyGraphRDFExporterService;
import net.geoprism.registry.service.business.LabeledPropertyGraphSynchronizationBusinessServiceIF;
import net.geoprism.registry.service.business.LabeledPropertyGraphTypeBusinessServiceIF;
import net.geoprism.registry.service.business.LabeledPropertyGraphTypeEntryBusinessServiceIF;
import net.geoprism.registry.service.business.LabeledPropertyGraphTypeVersionBusinessServiceIF;
import net.geoprism.registry.service.request.LabeledPropertyGraphTypeServiceIF;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.USATestData;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class RDFExporterTest extends USADatasetTest implements InstanceTestClassListener
{
  private static String                                        CODE = "Test Term";

//  private static ClassificationType                            type;

//  private static AttributeTermType                             testTerm;
//
//  private static AttributeClassificationType                   testClassification;

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
  
  @Autowired
  private LabeledPropertyGraphRDFExporterService               rdfExporter;

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
//    type = this.cTypeService.apply(ClassificationTypeTest.createMock());

//    Classification root = this.cService.newInstance(type);
//    root.setCode(CODE);
//    root.setDisplayLabel(new LocalizedValue("Test Classification"));
//    this.cService.apply(root, null);

//    testClassification = (AttributeClassificationType) AttributeType.factory("testClassification", new LocalizedValue("testClassificationLocalName"), new LocalizedValue("testClassificationLocalDescrip"), AttributeClassificationType.TYPE, false, false, true);
//    testClassification.setClassificationType(type.getCode());
//    testClassification.setRootTerm(root.toTerm());
//
//    ServerGeoObjectType got = ServerGeoObjectType.get(USATestData.STATE.getCode());
//    testClassification = (AttributeClassificationType) this.oTypeService.createAttributeType(got, testClassification.toJSON().toString());
//
//    testTerm = (AttributeTermType) AttributeType.factory("testTerm", new LocalizedValue("testTermLocalName"), new LocalizedValue("testTermLocalDescrip"), AttributeTermType.TYPE, false, false, true);
//    testTerm = (AttributeTermType) this.oTypeService.createAttributeType(got, testTerm.toJSON().toString());
//
//    Term term = new Term("TERM_1", new LocalizedValue("Term 1"), new LocalizedValue("Term 1"));
//
//    Classifier classifier = TermConverter.createClassifierFromTerm(testTerm.getRootTerm().getCode(), term);
//    term = TermConverter.buildTermFromClassifier(classifier);

//    USATestData.COLORADO.setDefaultValue(testClassification.getName(), CODE);
//    USATestData.COLORADO.setDefaultValue(testTerm.getName(), term);

//    this.repoService.refreshMetadataCache();
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
    super.afterClassSetup();

//    USATestData.COLORADO.removeDefaultValue(testClassification.getName());
//    USATestData.COLORADO.removeDefaultValue(testTerm.getName());
//
//    if (type != null)
//    {
//      this.cTypeService.delete(type);
//    }
  }

//  @Before
  public void setUp()
  {
    cleanUpExtra();

    testData.setUpInstanceData();

    testData.logIn(USATestData.USER_NPS_RA);
  }

//  @After
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
  public void testPublishJena() throws Exception
  {
    try (JenaConnector connector = new JenaConnector("http://localhost:3030/ds/data"))
    {
      JenaBridge jena = new JenaBridge(connector);
      
      Model model = ModelFactory.createDefaultModel();
      
      final String uri = "http://terraframe.com/2024/gpr-rdf/1.0#";
      final Property pCode = model.createProperty(uri, "code");
      
      Resource cambodia = model.createResource("urn:usace:cambodia");
      cambodia.addProperty(pCode, "Cambodia");
      
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      model.write(baos);
      
      String payload = baos.toString(Charset.forName("UTF-8"));
      System.out.println(payload);
      
      jena.put("urn:usace:graphName", model);
    }
  }

//  @Test
  @Request
  public void testPublishJob()
  {
    JsonObject json = LabeledPropertyGraphTest.getJson(USATestData.USA, USATestData.HIER_ADMIN);

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

      rdfExporter.export(version, System.out);
    }
    finally
    {
      this.typeService.delete(test1);
    }
  }
}
