/**
 *
 */
package net.geoprism.registry.business;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.runwaysdk.business.Business;
import com.runwaysdk.dataaccess.DuplicateDataException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutionContext;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.axon.config.RegistryEventStore;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.etl.DataImportJob;
import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.etl.NullImportProgressListener;
import net.geoprism.registry.etl.ObjectImporterFactory.JobHistoryType;
import net.geoprism.registry.etl.upload.ConceptObjectImportConfiguration;
import net.geoprism.registry.etl.upload.ConceptObjectImporter;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.etl.upload.ObjectRecordedErrorException;
import net.geoprism.registry.excel.MapFeatureRow;
import net.geoprism.registry.graph.ConceptClass;
import net.geoprism.registry.jobs.ImportHistory;
import net.geoprism.registry.model.ConceptObject;
import net.geoprism.registry.service.business.ConceptClassBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptObjectBusinessServiceIF;
import net.geoprism.registry.service.business.ETLBusinessService;
import net.geoprism.registry.service.request.ExcelService;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.USATestData;
import net.geoprism.registry.view.ConceptClassDTO;
import net.geoprism.registry.view.ConceptObjectImportConfigurationDTO;
import net.geoprism.registry.view.ImportConfigurationView;
import net.geoprism.registry.view.ImportHistoryView;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class ConceptObjectImporterTest extends FastDatasetTest implements InstanceTestClassListener
{
  private static ConceptClass            type;

  private static AttributeType           attributeType;

  private static AttributeType           attributeTypeOverTime;

  private static String                  TEST_CODE = "testCode";

  @Autowired
  private ConceptClassBusinessServiceIF  cClassService;

  @Autowired
  private ConceptObjectBusinessServiceIF objectService;

  @Autowired
  private ExcelService                   excelService;

  @Autowired
  private ETLBusinessService             etlBusinessService;

  @Autowired
  private RegistryEventStore             store;

  @Override
  public void beforeClassSetup() throws Exception
  {
    super.beforeClassSetup();

    testData.setUpInstanceData();

    setUpClassInRequest();
  }

  @Before
  @Request
  public void before()
  {
    // Clear out the event table
    Database.deleteWhere("domainevententry", "true");
  }

  @Request
  private void setUpClassInRequest()
  {
    String code = "TEST_PROG";
    String orgCode = FastTestDataset.ORG_CGOV.getCode();
    String label = "Test Prog";

    ConceptClassDTO object = new ConceptClassDTO();
    object.setCode(code);
    object.setOrganization(orgCode);
    object.setDisplayLabel(new LocalizedValue(label));

    type = this.cClassService.apply(object);

    attributeType = this.cClassService.createAttributeType(type, new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test True"), false, false, false, false));
    attributeTypeOverTime = this.cClassService.createAttributeType(type, new AttributeCharacterType("testMulti", new LocalizedValue("Test Multi"), new LocalizedValue("Test Multi"), false, false, false, true));
  }

  @Override
  public void afterClassSetup() throws Exception
  {
    cleanUpClassInRequest();

    super.afterClassSetup();

    if (testData != null)
    {
      testData.tearDownInstanceData();
    }
  }

  @Request
  private void cleanUpClassInRequest()
  {
    if (type != null)
    {
      this.cClassService.delete(type);
    }
  }

  @After
  @Request
  public void tearDown()
  {
    this.store.truncate();
  }

  @Test
  public void testImportValueNewOnly()
  {
    TestDataSet.executeRequestAsUser(USATestData.USER_ADMIN, () -> {
      String basicValue = "Test Text";
      String multiValue = "Test Mutli";

      String basicColumn = "Basic";
      String multiColumn = "Multi";

      HashMap<String, Object> row = new HashMap<String, Object>();
      row.put(basicColumn, basicValue);
      row.put(multiColumn, multiValue);
      row.put(ConceptObject.CODE, TEST_CODE);

      ConceptObjectImportConfiguration configuration = new ConceptObjectImportConfiguration();
      configuration.setImportStrategy(ImportStrategy.NEW_ONLY);
      configuration.setType(type);
      configuration.setStartDate(FastTestDataset.DEFAULT_OVER_TIME_DATE);
      configuration.setEndDate(FastTestDataset.DEFAULT_END_TIME_DATE);
      configuration.setCopyBlank(false);
      configuration.setFunction(attributeType.getCode(), new BasicColumnFunction(basicColumn));
      configuration.setFunction(attributeTypeOverTime.getCode(), new BasicColumnFunction(multiColumn));
      configuration.setFunction(ConceptObject.CODE, new BasicColumnFunction(ConceptObject.CODE));

      try (ConceptObjectImporter importer = new ConceptObjectImporter(configuration, new NullImportProgressListener()))
      {
        importer.importRow(new MapFeatureRow(row, 0L));
      }

      ConceptObject result = this.objectService.get(type, attributeType.getCode(), basicValue);

      try
      {
        Assert.assertNotNull(result);

        Assert.assertEquals(multiValue, result.getValue(attributeTypeOverTime.getCode(), FastTestDataset.DEFAULT_OVER_TIME_DATE));
      }
      finally
      {
        if (result != null)
        {
          this.deleteObject(result);
        }
      }
    });
  }

  @Test
  public void testGetHistory()
  {
    TestDataSet.executeRequestAsUser(USATestData.USER_ADMIN, () -> {

      InputStream istream = this.getClass().getResourceAsStream("/business-spreadsheet.xlsx");

      Assert.assertNotNull(istream);

      ConceptObjectImportConfigurationDTO dto = this.getTestConfiguration(istream, ImportStrategy.NEW_AND_UPDATE);

      ConceptObjectImportConfiguration config = (ConceptObjectImportConfiguration) ImportConfiguration.build(dto, true);

      ImportHistory hist = mockImport(config);

      try
      {
        Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

        List<ImportHistoryView> histories = this.etlBusinessService.getHistory(dto.getObjectType().name(), type.getCode());

        Assert.assertEquals(1, histories.size());
      }
      finally
      {
        Business.get(hist.getOid()).delete();
      }
    });
  }

  @Test
  public void testImportValueUpdateAndNew()
  {
    TestDataSet.executeRequestAsUser(USATestData.USER_ADMIN, () -> {

      String value = "Test Text";
      String rowAttribute = "Bad";

      HashMap<String, Object> row = new HashMap<String, Object>();
      row.put(rowAttribute, value);
      row.put(ConceptObject.CODE, TEST_CODE);

      ConceptObjectImportConfiguration configuration = new ConceptObjectImportConfiguration();
      configuration.setImportStrategy(ImportStrategy.NEW_AND_UPDATE);
      configuration.setType(type);
      configuration.setStartDate(FastTestDataset.DEFAULT_OVER_TIME_DATE);
      configuration.setEndDate(FastTestDataset.DEFAULT_END_TIME_DATE);
      configuration.setCopyBlank(false);
      configuration.setFunction(attributeType.getCode(), new BasicColumnFunction(rowAttribute));
      configuration.setFunction(ConceptObject.CODE, new BasicColumnFunction(ConceptObject.CODE));

      try (ConceptObjectImporter importer = new ConceptObjectImporter(configuration, new NullImportProgressListener()))
      {
        importer.importRow(new MapFeatureRow(row, 0L));
      }

      ConceptObject result = this.objectService.get(type, attributeType.getCode(), value);

      try
      {
        Assert.assertNotNull(result);
      }
      finally
      {
        if (result != null)
        {
          this.deleteObject(result);
        }
      }
    });
  }

  @Test
  public void testUpdateValue()
  {
    TestDataSet.executeRequestAsUser(USATestData.USER_ADMIN, () -> {

      ConceptObject object = this.objectService.newInstance(type);
      object.setCode(TEST_CODE);
      this.objectService.apply(object);

      try
      {
        String value = "Test Text";
        String rowAttribute = "Bad";

        HashMap<String, Object> row = new HashMap<String, Object>();
        row.put(rowAttribute, value);
        row.put(ConceptObject.CODE, TEST_CODE);

        ConceptObjectImportConfiguration configuration = new ConceptObjectImportConfiguration();
        configuration.setImportStrategy(ImportStrategy.UPDATE_ONLY);
        configuration.setType(type);
        configuration.setStartDate(FastTestDataset.DEFAULT_OVER_TIME_DATE);
        configuration.setEndDate(FastTestDataset.DEFAULT_END_TIME_DATE);
        configuration.setCopyBlank(false);
        configuration.setFunction(attributeType.getCode(), new BasicColumnFunction(rowAttribute));
        configuration.setFunction(ConceptObject.CODE, new BasicColumnFunction(ConceptObject.CODE));

        try (ConceptObjectImporter importer = new ConceptObjectImporter(configuration, new NullImportProgressListener()))
        {
          importer.importRow(new MapFeatureRow(row, 0L));
        }

        Assert.assertFalse(configuration.hasExceptions());

        ConceptObject result = this.objectService.getByCode(type, TEST_CODE);

        Assert.assertEquals(result.getValue(attributeType.getCode()), value);
      }
      finally
      {
        if (object != null)
        {
          this.deleteObject(object);
        }
      }
    });
  }

  @Test
  public void testImportValueExistingNewOnly()
  {
    TestDataSet.executeRequestAsUser(USATestData.USER_ADMIN, () -> {

      ConceptObject object = this.objectService.newInstance(type);
      object.setCode(TEST_CODE);
      this.objectService.apply(object);

      try
      {
        String value = "Test Text";
        String rowAttribute = "Bad";

        HashMap<String, Object> row = new HashMap<String, Object>();
        row.put(rowAttribute, value);
        row.put(ConceptObject.CODE, TEST_CODE);

        ConceptObjectImportConfiguration configuration = new ConceptObjectImportConfiguration();
        configuration.setImportStrategy(ImportStrategy.NEW_ONLY);
        configuration.setType(type);
        configuration.setStartDate(FastTestDataset.DEFAULT_OVER_TIME_DATE);
        configuration.setEndDate(FastTestDataset.DEFAULT_END_TIME_DATE);
        configuration.setCopyBlank(false);
        configuration.setFunction(attributeType.getCode(), new BasicColumnFunction(rowAttribute));
        configuration.setFunction(ConceptObject.CODE, new BasicColumnFunction(ConceptObject.CODE));

        try (ConceptObjectImporter importer = new ConceptObjectImporter(configuration, new NullImportProgressListener()))
        {
          importer.importRow(new MapFeatureRow(row, 0L));
        }

        Assert.assertTrue(configuration.hasExceptions());

        LinkedList<ObjectRecordedErrorException> exceptions = configuration.getExceptions();

        Assert.assertEquals(1, exceptions.size());

        ObjectRecordedErrorException exception = exceptions.get(0);

        Assert.assertTrue(exception.getError() instanceof DuplicateDataException);
      }
      finally
      {
        if (object != null)
        {
          this.deleteObject(object);
        }
      }
    });
  }

  @Test
  public void testImportSpreadsheet() throws Throwable
  {
    TestDataSet.executeRequestAsUser(USATestData.USER_ADMIN, () -> {

      InputStream istream = this.getClass().getResourceAsStream("/business-spreadsheet.xlsx");

      Assert.assertNotNull(istream);

      ConceptObjectImportConfigurationDTO dto = this.getTestConfiguration(istream, ImportStrategy.NEW_AND_UPDATE);

      ConceptObjectImportConfiguration config = (ConceptObjectImportConfiguration) ImportConfiguration.build(dto, true);

      ImportHistory hist = mockImport(config);
      Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

      hist = ImportHistory.get(hist.getOid());

      try
      {
        Assert.assertEquals(Long.valueOf(2), hist.getWorkTotal());
        Assert.assertEquals(Long.valueOf(2), hist.getWorkProgress());
        Assert.assertEquals(Long.valueOf(2), hist.getImportedRecords());
        Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

        assertAndDelete(this.objectService.get(type, attributeType.getCode(), "0001"));
        assertAndDelete(this.objectService.get(type, attributeType.getCode(), "0002"));
      }
      finally
      {
        Business.get(hist.getOid()).delete();
      }

    });
  }

  public void assertAndDelete(ConceptObject o1)
  {
    try
    {
      Assert.assertNotNull(o1);
      Assert.assertNotNull(o1.getValue(DefaultAttribute.DATA_SOURCE.getName()));
    }
    finally
    {
      if (o1 != null)
      {
        this.deleteObject(o1);
      }
    }
  }

  private ConceptObjectImportConfigurationDTO getTestConfiguration(InputStream istream, ImportStrategy strategy) throws JSONException
  {
    ImportConfigurationView view = ImportConfigurationView.of(JobHistoryType.CONCEPT_OBJECT, type.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getCode(), strategy, false);

    ConceptObjectImportConfigurationDTO configuration = this.excelService.getImportConfiguration(Session.getCurrentSession().getOid(), "business-spreadsheet.xlsx", istream, view);
    configuration.setFormatType(FormatImporterType.EXCEL);

    configuration.getType().getAttributes().stream() //
        .filter(a -> a.getCode().equals(attributeType.getCode()) || a.getCode().equals(ConceptObject.CODE)) //
        .forEach(a -> a.setTarget("Code"));

    return configuration;
  }

  private ImportHistory mockImport(ConceptObjectImportConfiguration config) throws Throwable
  {
    config.setImportStrategy(ImportStrategy.NEW_AND_UPDATE);

    DataImportJob job = new DataImportJob();
    job.apply();
    ImportHistory hist = (ImportHistory) job.createNewHistory();

    config.setHistoryId(hist.getOid());
    config.setJobId(job.getOid());

    ConceptClass type = config.getType();

    hist.appLock();
    hist.setImportFileId(config.getVaultFileId());
    hist.setConfiguration(config.toDTO());
    hist.setOrganization(type.getOrganization().getOrganization());
    hist.setGeoObjectTypeCode(type.getCode());
    hist.apply();

    ExecutionContext context = job.startSynchronously(hist);

    hist = (ImportHistory) context.getHistory();
    return hist;
  }

  private void deleteObject(ConceptObject object)
  {
    for (int i = 0; i < 100; i++)
    {
      try
      {
        this.objectService.delete(object);

        break;
      }
      catch (ProgrammingErrorException e)
      {
        Throwable cause = e.getCause();

        if (cause instanceof OConcurrentModificationException)
        {
          // Do nothing
          try
          {
            Thread.sleep(10);
          }
          catch (InterruptedException e1)
          {
          }
        }
        else
        {
          throw e;
        }
      }
    }
  }

}
