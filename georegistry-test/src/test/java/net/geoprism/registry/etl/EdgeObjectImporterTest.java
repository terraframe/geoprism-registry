/**
 *
 */
package net.geoprism.registry.etl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.SchedulerManager;

import net.geoprism.graph.GraphTypeSnapshot;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ObjectImporterFactory.ObjectImportType;
import net.geoprism.registry.etl.upload.EdgeObjectImportConfiguration;
import net.geoprism.registry.etl.upload.EdgeObjectImporter.ReferenceStrategy;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.jobs.ImportHistory;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.request.ETLService;
import net.geoprism.registry.service.request.EdgeImportService;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.SchedulerTestUtils;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class EdgeObjectImporterTest implements InstanceTestClassListener
{
  public static final String         EDGE_CODE      = FastTestDataset.HIER_ADMIN.getCode();

  public static final String         EDGE_TYPE_CODE = GraphTypeSnapshot.HIERARCHY_TYPE;

  private static int                 IMPORT_COUNT   = 10;

  protected static FastTestDataset   testData;

  @Autowired
  private GeoObjectBusinessServiceIF objectService;

  @Autowired
  private ETLService                 etlService;

  @Autowired
  private EdgeImportService          service;

  @Override
  public void beforeClassSetup() throws Exception
  {
    testData = FastTestDataset.newTestData();
    testData.setUpMetadata();

    if (!SchedulerManager.initialized())
    {
      SchedulerManager.start();
    }
  }

  @Override
  public void afterClassSetup() throws Exception
  {
    testData.tearDownMetadata();
  }

  @Before
  public void setUp()
  {
    testData.setUpInstanceData();

    clearData();

    testData.logIn();
  }

  @After
  public void tearDown() throws IOException
  {
    testData.logOut();

    testData.tearDownInstanceData();

    FileUtils.deleteDirectory(new File(VaultProperties.getPath("vault.default"), "files"));

    clearData();
  }

  @Request
  private void clearData()
  {
    SchedulerTestUtils.clearImportData();

    for (int i = 0; i < IMPORT_COUNT; ++i)
    {
      TestGeoObjectInfo one = testData.newTestGeoObjectInfo(String.valueOf(i), FastTestDataset.DISTRICT, FastTestDataset.SOURCE);
      one.setCode(String.valueOf(i));
      one.delete();
    }
  }

  private ImportHistory importJsonFile(String sessionId, String config) throws InterruptedException
  {
    String retConfig = this.etlService.doImport(sessionId, config).toString();

    EdgeObjectImportConfiguration configuration = (EdgeObjectImportConfiguration) ImportConfiguration.build(retConfig, true);

    String historyId = configuration.getHistoryId();

    Thread.sleep(100);

    return ImportHistory.get(historyId);
  }

  private void generateDistricts()
  {
    System.out.println("Applying " + IMPORT_COUNT + " districts");

    for (int i = 0; i < IMPORT_COUNT; ++i)
    {
      TestGeoObjectInfo one = testData.newTestGeoObjectInfo(String.valueOf(i), FastTestDataset.DISTRICT, FastTestDataset.SOURCE);
      one.setCode(String.valueOf(i));
      one.apply();
    }
  }

  private InputStream generateEdgeJson()
  {
    System.out.println("Generating " + IMPORT_COUNT + " edges");

    JSONArray all = new JSONArray();

    for (int i = 0; i < IMPORT_COUNT; ++i)
    {
      JSONObject jo = new JSONObject();
      jo.put("source", String.valueOf(i));
      jo.put("sourceType", FastTestDataset.DISTRICT.getCode());
      jo.put("target", FastTestDataset.PROV_CENTRAL.getCode());
      jo.put("targetType", FastTestDataset.PROVINCE.getCode());
      all.put(jo);
    }

    return new ByteArrayInputStream(all.toString().getBytes());
  }

  @Test
  @Request
  public void testPerformance() throws InterruptedException
  {
    generateDistricts();
    InputStream istream = generateEdgeJson();

    Assert.assertNotNull(istream);

    EdgeObjectImportConfiguration config = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);

    long start = System.nanoTime();

    ImportHistory hist = importJsonFile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);
    System.out.println("Elapsed: " + ( System.nanoTime() - start ) / 1_000_000_000.0 + " s");

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(IMPORT_COUNT), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(IMPORT_COUNT), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(IMPORT_COUNT), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    ServerGeoObjectIF dist1 = this.objectService.getGeoObjectByCode("1", FastTestDataset.DISTRICT.getCode());
    var parent = dist1.getGraphParents(GraphType.getByCode(EDGE_TYPE_CODE, EDGE_CODE), false, TestDataSet.DEFAULT_OVER_TIME_DATE).getGeoObject();
    Assert.assertNotNull(parent);
    Assert.assertEquals("1", parent.getDisplayLabel(TestDataSet.DEFAULT_OVER_TIME_DATE).getValue());
  }

  private EdgeObjectImportConfiguration getTestConfiguration(InputStream istream, AttributeTermType attributeTerm, ImportStrategy strategy)
  {
    ObjectNode result = service.getJsonImportConfiguration(testData.clientRequest.getSessionId(), EDGE_TYPE_CODE, EDGE_CODE, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, null, "test.json", istream, strategy);

    result.put(ImportConfiguration.FORMAT_TYPE, FormatImporterType.JSON.name());
    result.put(ImportConfiguration.OBJECT_TYPE, ObjectImportType.EDGE_OBJECT.name());

    result.put(EdgeObjectImportConfiguration.EDGE_SOURCE, "source");
    result.put(EdgeObjectImportConfiguration.EDGE_SOURCE_STRATEGY, ReferenceStrategy.CODE.name());
    result.put(EdgeObjectImportConfiguration.EDGE_SOURCE_TYPE, "sourceType");
    result.put(EdgeObjectImportConfiguration.EDGE_SOURCE_TYPE_STRATEGY, ReferenceStrategy.CODE.name());
    result.put(EdgeObjectImportConfiguration.EDGE_TARGET, "target");
    result.put(EdgeObjectImportConfiguration.EDGE_TARGET_STRATEGY, ReferenceStrategy.CODE.name());
    result.put(EdgeObjectImportConfiguration.EDGE_TARGET_TYPE, "targetType");
    result.put(EdgeObjectImportConfiguration.EDGE_TARGET_TYPE_STRATEGY, ReferenceStrategy.CODE.name());

    EdgeObjectImportConfiguration configuration = (EdgeObjectImportConfiguration) ImportConfiguration.build(result.toString(), true);

    return configuration;
  }
}
