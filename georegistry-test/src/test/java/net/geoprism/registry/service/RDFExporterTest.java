/**
 *
 */
package net.geoprism.registry.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.sparql.core.Quad;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.JsonObject;
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
import net.geoprism.registry.UndirectedGraphType;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.lpg.jena.JenaBridge;
import net.geoprism.registry.lpg.jena.JenaConnector;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.service.business.DirectedAcyclicGraphTypeBusinessServiceIF;
import net.geoprism.registry.service.business.LabeledPropertyGraphRDFExportBusinessServiceIF;
import net.geoprism.registry.service.business.LabeledPropertyGraphRDFExportBusinessServiceIF.GeometryExportType;
import net.geoprism.registry.service.business.LabeledPropertyGraphTypeBusinessServiceIF;
import net.geoprism.registry.service.business.LabeledPropertyGraphTypeEntryBusinessServiceIF;
import net.geoprism.registry.service.business.UndirectedGraphTypeBusinessServiceIF;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.USATestData;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class RDFExporterTest extends FastDatasetTest implements InstanceTestClassListener
{
  @Autowired
  private LabeledPropertyGraphTypeBusinessServiceIF      typeService;

  @Autowired
  private LabeledPropertyGraphTypeEntryBusinessServiceIF entryService;

  @Autowired
  private LabeledPropertyGraphRDFExportBusinessServiceIF rdfExporter;

  @Autowired
  private DirectedAcyclicGraphTypeBusinessServiceIF      dagSerivce;

  @Autowired
  private UndirectedGraphTypeBusinessServiceIF           undirectedSerivce;

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

  // @Test
  public void testPublishJena() throws Exception
  {
    try (JenaConnector connector = new JenaConnector("http://localhost:3030/ds/data"))
    {
      JenaBridge jena = new JenaBridge(connector);

      Path file = Files.createTempFile("jena", ".ttl");

      StreamRDF writer = StreamRDFWriter.getWriterStream(Files.newOutputStream(file), Lang.TURTLE);

      writer.start();
      // writer.triple(Triple.create(NodeFactory.createURI("urn:usace:cambodia"),
      // NodeFactory.createURI("urn:usace:country#code"),
      // NodeFactory.createLiteral("Cambodia")));
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

      rdfExporter.export(null, version, GeometryExportType.NO_GEOMETRIES, System.out);
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
    UndirectedGraphType graphType = this.undirectedSerivce.create("TestUn", new LocalizedValue("TestUn"), new LocalizedValue("TestUn"), 1L);

    try
    {
      JsonObject json = LabeledPropertyGraphTest.getJson(graphType, //
          new String[] { FastTestDataset.COUNTRY.getCode(), FastTestDataset.PROVINCE.getCode() }, //
          new String[] {}, //
          new String[] {}, //
          FastTestDataset.ORG_CGOV.getServerObject().getOrganization());

      LabeledPropertyGraphType test1 = this.typeService.apply(json);

      try
      {
        ServerGeoObjectIF object = FastTestDataset.CAMBODIA.getServerObject();

        object.addGraphChild(FastTestDataset.PROV_CENTRAL.getServerObject(), graphType, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE, UUID.randomUUID().toString(), USATestData.SOURCE.getDataSource(), false);

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

        rdfExporter.export(null, version, GeometryExportType.NO_GEOMETRIES, System.out);
      }
      finally
      {
        this.typeService.delete(test1);
      }
    }
    finally
    {
      this.undirectedSerivce.delete(graphType);
    }

  }

  @Test
  @Request
  public void testPublishDAG() throws IOException
  {
    DirectedAcyclicGraphType dagType = this.dagSerivce.create("TestDag", new LocalizedValue("TestDag"), new LocalizedValue("TestDag"), 1L);
    try
    {
      JsonObject json = LabeledPropertyGraphTest.getJson(dagType, //
          new String[] { FastTestDataset.COUNTRY.getCode(), FastTestDataset.PROVINCE.getCode() }, //
          new String[] {}, //
          new String[] {}, //
          FastTestDataset.ORG_CGOV.getServerObject().getOrganization());

      LabeledPropertyGraphType test1 = this.typeService.apply(json);

      try
      {
        ServerGeoObjectIF object = FastTestDataset.CAMBODIA.getServerObject();
        object.addGraphChild(FastTestDataset.PROV_CENTRAL.getServerObject(), dagType, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE, UUID.randomUUID().toString(), USATestData.SOURCE.getDataSource(), false);

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

        rdfExporter.export(null, version, GeometryExportType.NO_GEOMETRIES, System.out);
      }
      finally
      {
        this.typeService.delete(test1);
      }
    }
    finally
    {
      this.dagSerivce.delete(dagType);
    }

  }
}
