/**
 *
 */
package net.geoprism.registry.etl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
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

import com.runwaysdk.Pair;
import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.SchedulerManager;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.axon.projection.RepositoryProjection;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.etl.upload.EdgeObjectImportConfiguration;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.BusinessEdgeType;
import net.geoprism.registry.graph.BusinessType;
import net.geoprism.registry.jobs.ImportHistory;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.EdgeDirection;
import net.geoprism.registry.model.graph.VertexComponent;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ETLBusinessService;
import net.geoprism.registry.service.business.EdgeImportTestService;
import net.geoprism.registry.service.business.GraphRepoServiceIF;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.SchedulerTestUtils;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.view.BusinessEdgeTypeView;
import net.geoprism.registry.view.BusinessGeoEdgeTypeView;
import net.geoprism.registry.view.BusinessTypeDTO;
import net.geoprism.registry.view.ImportHistoryView;
import net.geoprism.registry.view.TypeClass;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class EdgeObjectImporterTest extends FastDatasetTest implements InstanceTestClassListener
{
  private static int                        IMPORT_COUNT = 10;

  private static BusinessType               btype;

  private static BusinessEdgeType           bEdgeType;

  private static BusinessEdgeType           bGeoEdgeType;

  @Autowired
  private EdgeImportTestService             etlService;

  @Autowired
  private ETLBusinessService                etlBusinessService;

  @Autowired
  private BusinessTypeBusinessServiceIF     bTypeService;

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF bEdgeService;

  @Autowired
  protected GraphRepoServiceIF              repoService;

  @Autowired
  protected RepositoryProjection            projection;

  private BusinessObject                    pObject;

  private BusinessObject                    cObject;

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
    BusinessTypeDTO object = new BusinessTypeDTO();
    object.setCode("TEST_BUSINESS");
    object.setOrganization(FastTestDataset.ORG_CGOV.getCode());
    object.setDisplayLabel(new LocalizedValue("Test Business"));

    btype = this.bTypeService.apply(object);

    this.bTypeService.createAttributeType(btype, new AttributeBooleanType("testBoolean", new LocalizedValue("Test Boolean"), new LocalizedValue("Test Boolean"), false, false, false, false));

    bEdgeType = this.bEdgeService.create(BusinessEdgeTypeView.build(FastTestDataset.ORG_CGOV.getCode(), "TEST_B_EDGE", new LocalizedValue("TEST_B_EDGE"), new LocalizedValue("TEST_B_EDGE"), btype.getCode(), btype.getCode()));

    bGeoEdgeType = this.bEdgeService.create(BusinessGeoEdgeTypeView.build(FastTestDataset.ORG_CGOV.getCode(), "TEST_GEO_EDGE", new LocalizedValue("TEST_GEO_EDGE"), new LocalizedValue("TEST_GEO_EDGE"), btype.getCode(), EdgeDirection.PARENT));

    this.repoService.refreshMetadataCache();
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
    super.afterClassSetup();

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
  }

  @Before
  @Request
  public void setUp()
  {
    testData.setUpInstanceData();

    clearData();

    testData.logIn();

    pObject = createBusinessObject("P_CODE", btype, null);
    cObject = createBusinessObject("C_CODE", btype, null);

    addBusinessEdge();
  }

  protected void addBusinessEdge()
  {
    List<Pair<VertexComponent, BusinessEdgeType>> targets = Arrays.asList( //
        new Pair<VertexComponent, BusinessEdgeType>(pObject, bEdgeType), //
        new Pair<VertexComponent, BusinessEdgeType>(FastTestDataset.CAMBODIA.getServerObject(), bGeoEdgeType) //
    );
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

    SchedulerTestUtils.clearImportData();

    for (int i = 0; i < IMPORT_COUNT; ++i)
    {
      TestGeoObjectInfo one = testData.newTestGeoObjectInfo(String.valueOf(i), FastTestDataset.DISTRICT, FastTestDataset.SOURCE);
      one.setCode(String.valueOf(i));
      one.delete();
    }

    projection.clearCache();
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

  private InputStream generateEdgeJson(VertexComponent source, VertexComponent target)
  {
    JSONArray all = new JSONArray();

    JSONObject jo = new JSONObject();
    jo.put("source", source.getCode());
    jo.put("sourceType", source.getType().getCode());
    jo.put("target", target.getCode());
    jo.put("targetType", target.getType().getCode());
    all.put(jo);

    return new ByteArrayInputStream(all.toString().getBytes());
  }

  // @Test
  // public void testPerformance() throws InterruptedException
  // {
  // TestDataSet.executeRequestAsUser(FastTestDataset.USER_ADMIN, () -> {
  // generateDistricts();
  // InputStream istream = generateEdgeJson();
  //
  // Assert.assertNotNull(istream);
  //
  // EdgeObjectImportConfiguration config =
  // this.etlService.getTestConfiguration(TypeClass.HIERARCHY.getCode(),
  // FastTestDataset.HIER_ADMIN.getCode(), istream,
  // ImportStrategy.NEW_AND_UPDATE);
  //
  // long start = System.nanoTime();
  //
  // ImportHistory hist =
  // this.etlService.importJsonFile(config.toJSON().toString());
  //
  // SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);
  // System.out.println("Elapsed: " + ( System.nanoTime() - start ) /
  // 1_000_000_000.0 + " s");
  //
  // hist = ImportHistory.get(hist.getOid());
  // Assert.assertEquals(Long.valueOf(IMPORT_COUNT), hist.getWorkTotal());
  // Assert.assertEquals(Long.valueOf(IMPORT_COUNT), hist.getWorkProgress());
  // Assert.assertEquals(Long.valueOf(IMPORT_COUNT), hist.getImportedRecords());
  // Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));
  //
  // GraphType hierarchyType =
  // this.typeService.getByCode(TypeClass.HIERARCHY.getCode(),
  // FastTestDataset.HIER_ADMIN.getCode());
  // ServerGeoObjectIF dist1 = this.objectService.getGeoObjectByCode("1",
  // FastTestDataset.DISTRICT.getCode());
  // ServerGraphNode node = dist1.getGraphParents(hierarchyType, false,
  // TestDataSet.DEFAULT_OVER_TIME_DATE);
  //
  // ServerGeoObjectIF parent = node.getGeoObject();
  // Assert.assertNotNull(parent);
  // Assert.assertEquals("1",
  // parent.getDisplayLabel(TestDataSet.DEFAULT_OVER_TIME_DATE).getValue());
  //
  // List<ImportHistoryView> histories =
  // this.etlBusinessService.getHistory(TypeClass.HIERARCHY.getCode(),
  // FastTestDataset.HIER_ADMIN.getCode());
  //
  // Assert.assertEquals(1, histories.size());
  // });
  // }

  @Test
  public void testBusinessEdge() throws InterruptedException
  {
    TestDataSet.executeRequestAsUser(FastTestDataset.USER_ADMIN, () -> {
      generateDistricts();
      InputStream istream = generateEdgeJson(cObject, pObject);

      Assert.assertNotNull(istream);

      EdgeObjectImportConfiguration config = this.etlService.getTestConfiguration(TypeClass.BUSINESS_EDGE.getCode(), bEdgeType.getCode(), istream, ImportStrategy.NEW_AND_UPDATE);

      long start = System.nanoTime();

      ImportHistory hist = this.etlService.importJsonFile(config.toDTO());

      SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);
      System.out.println("Elapsed: " + ( System.nanoTime() - start ) / 1_000_000_000.0 + " s");

      hist = ImportHistory.get(hist.getOid());
      Assert.assertEquals(Long.valueOf(1), hist.getWorkTotal());
      Assert.assertEquals(Long.valueOf(1), hist.getWorkProgress());
      Assert.assertEquals(Long.valueOf(1), hist.getImportedRecords());
      Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

      List<VertexComponent> tagets = this.bObjectService.getChildren(cObject, bEdgeType, TestDataSet.DEFAULT_OVER_TIME_DATE);

      Assert.assertEquals(1, tagets.size());

      List<ImportHistoryView> histories = this.etlBusinessService.getHistory(TypeClass.BUSINESS_EDGE.getCode(), bEdgeType.getCode());

      Assert.assertEquals(1, histories.size());
    });
  }

  @Test
  public void testBusinessGeoEdge() throws InterruptedException
  {
    TestDataSet.executeRequestAsUser(FastTestDataset.USER_ADMIN, () -> {
      generateDistricts();
      InputStream istream = generateEdgeJson(FastTestDataset.CAMBODIA.getServerObject(), cObject);

      Assert.assertNotNull(istream);

      EdgeObjectImportConfiguration config = this.etlService.getTestConfiguration(TypeClass.BUSINESS_EDGE.getCode(), bGeoEdgeType.getCode(), istream, ImportStrategy.NEW_AND_UPDATE);

      long start = System.nanoTime();

      ImportHistory hist = this.etlService.importJsonFile(config.toDTO());

      SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);
      System.out.println("Elapsed: " + ( System.nanoTime() - start ) / 1_000_000_000.0 + " s");

      hist = ImportHistory.get(hist.getOid());
      Assert.assertEquals(Long.valueOf(1), hist.getWorkTotal());
      Assert.assertEquals(Long.valueOf(1), hist.getWorkProgress());
      Assert.assertEquals(Long.valueOf(1), hist.getImportedRecords());
      Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

      List<VertexComponent> tagets = this.bObjectService.getParents(cObject, bGeoEdgeType, TestDataSet.DEFAULT_OVER_TIME_DATE);

      Assert.assertEquals(1, tagets.size());

      List<ImportHistoryView> histories = this.etlBusinessService.getHistory(TypeClass.BUSINESS_EDGE.getCode(), bGeoEdgeType.getCode());

      Assert.assertEquals(1, histories.size());
    });
  }

}
