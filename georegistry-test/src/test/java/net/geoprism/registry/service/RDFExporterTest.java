/**
 *
 */
package net.geoprism.registry.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.sparql.core.Quad;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.gson.JsonObject;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.graph.MdEdgeInfo;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.SchedulerManager;

import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeEntry;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.graph.PublishLabeledPropertyGraphTypeVersionJob;
import net.geoprism.registry.DirectedAcyclicGraphType;
import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.UndirectedGraphType;
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
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class RDFExporterTest extends FastDatasetTest implements InstanceTestClassListener
{
  @Autowired
  private LabeledPropertyGraphTypeBusinessServiceIF            typeService;

  @Autowired
  private LabeledPropertyGraphTypeEntryBusinessServiceIF       entryService;
  
  @Autowired
  private LabeledPropertyGraphRDFExporterService               rdfExporter;

  @Override
  public void beforeClassSetup() throws Exception
  {
    super.beforeClassSetup();

    if (!SchedulerManager.initialized())
    {
      SchedulerManager.start();
    }
  }
  
  @Before
  public void setUp()
  {
    cleanUpExtra();

    testData.setUpInstanceData();

    testData.logIn(FastTestDataset.USER_CGOV_RA);
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
  
//  @Test
  public void testPublishJena() throws Exception
  {
    try (JenaConnector connector = new JenaConnector("http://localhost:3030/ds/data"))
    {
      JenaBridge jena = new JenaBridge(connector);
      
      Path file = Files.createTempFile("jena", ".ttl");
      
      StreamRDF writer = StreamRDFWriter.getWriterStream(Files.newOutputStream(file) , Lang.TURTLE);
      
      writer.start();
//      writer.triple(Triple.create(NodeFactory.createURI("urn:usace:cambodia"), NodeFactory.createURI("urn:usace:country#code"), NodeFactory.createLiteral("Cambodia")));
      writer.quad(Quad.create(NodeFactory.createLiteral("LPG-code"), NodeFactory.createURI("urn:usace:cambodia"), NodeFactory.createURI("urn:usace:country#code"), NodeFactory.createLiteral("Cambodia")));
      writer.finish();
      
      System.out.println(FileUtils.readFileToString(file.toFile(), "UTF-8"));
      
      jena.put("urn:usace:graphName", file.toAbsolutePath().toString());
    }
  }

  @Test
  @Request
  public void testPublishHierarchy() throws IOException
  {
    JsonObject json = LabeledPropertyGraphTest.getJson(FastTestDataset.CAMBODIA, FastTestDataset.HIER_ADMIN);

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
  
  @Test
  @Request
  public void testPublishUndirected() throws IOException
  {
    MdEdgeDAO mdEdge = MdEdgeDAO.newInstance();
    mdEdge.setValue(MdEdgeInfo.NAME, "TestDag");
    mdEdge.setValue(MdEdgeInfo.PACKAGE, "net.geoprism.registry.service.test.TestDag");
    mdEdge.setStructValue(MdEdgeInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, "TestDag for LabeledPropertyGraphTest");
    mdEdge.setStructValue(MdEdgeInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, "TestDag for LabeledPropertyGraphTest");
    mdEdge.setValue(MdEdgeInfo.PARENT_MD_VERTEX, FastTestDataset.COUNTRY.getServerObject().getMdVertex().getOid());
    mdEdge.setValue(MdEdgeInfo.CHILD_MD_VERTEX, FastTestDataset.PROVINCE.getServerObject().getMdVertex().getOid());
    mdEdge.apply();
    
    UndirectedGraphType graphType = new UndirectedGraphType();
    graphType.setCode("TestUndirected");
    graphType.getDisplayLabel().setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, "TestDag");
    graphType.setMdEdgeId(mdEdge.getOid());
    graphType.apply();
    
    JsonObject json = LabeledPropertyGraphTest.getJson(graphType, new String[] { FastTestDataset.COUNTRY.getCode(), FastTestDataset.PROVINCE.getCode() }, FastTestDataset.ORG_CGOV.getServerObject().getOrganization());

    LabeledPropertyGraphType test1 = this.typeService.apply(json);

    try
    {
      FastTestDataset.CAMBODIA.getServerObject().getVertex().addChild(FastTestDataset.PROV_CENTRAL.getServerObject().getVertex(), mdEdge).apply();
      
      List<LabeledPropertyGraphTypeEntry> entries = this.typeService.getEntries(test1);

      Assert.assertEquals(1, entries.size());

      LabeledPropertyGraphTypeEntry entry = entries.get(0);

      List<LabeledPropertyGraphTypeVersion> versions = this.entryService.getVersions(entry);

      Assert.assertEquals(1, versions.size());

      LabeledPropertyGraphTypeVersion version = versions.get(0);

      PublishLabeledPropertyGraphTypeVersionJob job = new PublishLabeledPropertyGraphTypeVersionJob();
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
      graphType.delete();
      mdEdge.delete();
    }
  }
  
  @Test
  @Request
  public void testPublishDAG() throws IOException
  {
    MdEdgeDAO mdEdge = MdEdgeDAO.newInstance();
    mdEdge.setValue(MdEdgeInfo.NAME, "TestDag");
    mdEdge.setValue(MdEdgeInfo.PACKAGE, "net.geoprism.registry.service.test.TestDag");
    mdEdge.setStructValue(MdEdgeInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, "TestDag for LabeledPropertyGraphTest");
    mdEdge.setStructValue(MdEdgeInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, "TestDag for LabeledPropertyGraphTest");
    mdEdge.setValue(MdEdgeInfo.PARENT_MD_VERTEX, FastTestDataset.COUNTRY.getServerObject().getMdVertex().getOid());
    mdEdge.setValue(MdEdgeInfo.CHILD_MD_VERTEX, FastTestDataset.PROVINCE.getServerObject().getMdVertex().getOid());
    mdEdge.apply();
    
    DirectedAcyclicGraphType dagType = new DirectedAcyclicGraphType();
    dagType.setCode("TestDag");
    dagType.getDisplayLabel().setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, "TestDag");
    dagType.setMdEdgeId(mdEdge.getOid());
    dagType.apply();
    
    JsonObject json = LabeledPropertyGraphTest.getJson(dagType, new String[] { FastTestDataset.COUNTRY.getCode(), FastTestDataset.PROVINCE.getCode() }, FastTestDataset.ORG_CGOV.getServerObject().getOrganization());

    LabeledPropertyGraphType test1 = this.typeService.apply(json);

    try
    {
      FastTestDataset.CAMBODIA.getServerObject().getVertex().addChild(FastTestDataset.PROV_CENTRAL.getServerObject().getVertex(), mdEdge).apply();
      
      List<LabeledPropertyGraphTypeEntry> entries = this.typeService.getEntries(test1);

      Assert.assertEquals(1, entries.size());

      LabeledPropertyGraphTypeEntry entry = entries.get(0);

      List<LabeledPropertyGraphTypeVersion> versions = this.entryService.getVersions(entry);

      Assert.assertEquals(1, versions.size());

      LabeledPropertyGraphTypeVersion version = versions.get(0);

      PublishLabeledPropertyGraphTypeVersionJob job = new PublishLabeledPropertyGraphTypeVersionJob();
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
      dagType.delete();
      mdEdge.delete();
    }
  }
}
