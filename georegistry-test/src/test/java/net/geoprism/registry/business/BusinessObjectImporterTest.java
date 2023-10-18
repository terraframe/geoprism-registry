/**
 *
 */
package net.geoprism.registry.business;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.gson.JsonObject;
import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.runwaysdk.dataaccess.DuplicateDataException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutionContext;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.etl.DataImportJob;
import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ImportHistory;
import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.etl.NullImportProgressListener;
import net.geoprism.registry.etl.ObjectImporterFactory.ObjectImportType;
import net.geoprism.registry.etl.upload.BusinessObjectImportConfiguration;
import net.geoprism.registry.etl.upload.BusinessObjectImporter;
import net.geoprism.registry.etl.upload.BusinessObjectRecordedErrorException;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.excel.MapFeatureRow;
import net.geoprism.registry.io.Location;
import net.geoprism.registry.io.ParentCodeException;
import net.geoprism.registry.io.ParentMatchStrategy;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.business.BusinessObjectBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.service.request.ExcelService;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.USATestData;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class BusinessObjectImporterTest extends FastDatasetTest implements InstanceTestClassListener
{
  private static BusinessType             type;

  private static AttributeType            attributeType;

  private static String                   TEST_CODE = "testCode";

  @Autowired
  private BusinessTypeBusinessServiceIF   bTypeService;

  @Autowired
  private BusinessObjectBusinessServiceIF bObjectService;

  @Autowired
  private ExcelService                    excelService;

  @Override
  public void beforeClassSetup() throws Exception
  {
    super.beforeClassSetup();

    testData.setUpInstanceData();

    setUpClassInRequest();
  }

  @Request
  private void setUpClassInRequest()
  {
    String code = "TEST_PROG";
    String orgCode = FastTestDataset.ORG_CGOV.getCode();
    String label = "Test Prog";

    JsonObject object = new JsonObject();
    object.addProperty(BusinessType.CODE, code);
    object.addProperty(BusinessType.ORGANIZATION, orgCode);
    object.add(BusinessType.DISPLAYLABEL, new LocalizedValue(label).toJSON());

    type = this.bTypeService.apply(object);

    attributeType = this.bTypeService.createAttributeType(type, new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test True"), false, false, false));
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
      this.bTypeService.delete(type);
    }
  }

  @Test
  public void testImportValueNewOnly() 
  {
    TestDataSet.executeRequestAsUser(USATestData.USER_ADMIN, () -> {
      String value = "Test Text";
      String rowAttribute = "Bad";

      HashMap<String, Object> row = new HashMap<String, Object>();
      row.put(rowAttribute, value);
      row.put(BusinessObject.CODE, TEST_CODE);

      BusinessObjectImportConfiguration configuration = new BusinessObjectImportConfiguration();
      configuration.setImportStrategy(ImportStrategy.NEW_ONLY);
      configuration.setType(type);
      configuration.setDate(FastTestDataset.DEFAULT_END_TIME_DATE);
      configuration.setCopyBlank(false);
      configuration.setFunction(attributeType.getName(), new BasicColumnFunction(rowAttribute));
      configuration.setFunction(BusinessObject.CODE, new BasicColumnFunction(BusinessObject.CODE));

      try (BusinessObjectImporter importer = new BusinessObjectImporter(configuration, new NullImportProgressListener()))
      {
        importer.importRow(new MapFeatureRow(row, 0L));
      }

      BusinessObject result = this.bObjectService.get(type, attributeType.getName(), value);

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
  public void testImportValueUpdateAndNew() 
  {
    TestDataSet.executeRequestAsUser(USATestData.USER_ADMIN, () -> {

      String value = "Test Text";
      String rowAttribute = "Bad";

      HashMap<String, Object> row = new HashMap<String, Object>();
      row.put(rowAttribute, value);
      row.put(BusinessObject.CODE, TEST_CODE);

      BusinessObjectImportConfiguration configuration = new BusinessObjectImportConfiguration();
      configuration.setImportStrategy(ImportStrategy.NEW_AND_UPDATE);
      configuration.setType(type);
      configuration.setDate(FastTestDataset.DEFAULT_END_TIME_DATE);
      configuration.setCopyBlank(false);
      configuration.setFunction(attributeType.getName(), new BasicColumnFunction(rowAttribute));
      configuration.setFunction(BusinessObject.CODE, new BasicColumnFunction(BusinessObject.CODE));

      try (BusinessObjectImporter importer = new BusinessObjectImporter(configuration, new NullImportProgressListener()))
      {
        importer.importRow(new MapFeatureRow(row, 0L));
      }

      BusinessObject result = this.bObjectService.get(type, attributeType.getName(), value);

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

      BusinessObject object = this.bObjectService.newInstance(type);
      object.setCode(TEST_CODE);
      this.bObjectService.apply(object);

      try
      {
        String value = "Test Text";
        String rowAttribute = "Bad";

        HashMap<String, Object> row = new HashMap<String, Object>();
        row.put(rowAttribute, value);
        row.put(BusinessObject.CODE, TEST_CODE);

        BusinessObjectImportConfiguration configuration = new BusinessObjectImportConfiguration();
        configuration.setImportStrategy(ImportStrategy.UPDATE_ONLY);
        configuration.setType(type);
        configuration.setDate(FastTestDataset.DEFAULT_END_TIME_DATE);
        configuration.setCopyBlank(false);
        configuration.setFunction(attributeType.getName(), new BasicColumnFunction(rowAttribute));
        configuration.setFunction(BusinessObject.CODE, new BasicColumnFunction(BusinessObject.CODE));

        try (BusinessObjectImporter importer = new BusinessObjectImporter(configuration, new NullImportProgressListener()))
        {
          importer.importRow(new MapFeatureRow(row, 0L));
        }

        Assert.assertFalse(configuration.hasExceptions());

        BusinessObject result = this.bObjectService.getByCode(type, TEST_CODE);

        Assert.assertEquals(result.getObjectValue(attributeType.getName()), value);
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

      BusinessObject object = this.bObjectService.newInstance(type);
      object.setCode(TEST_CODE);
      this.bObjectService.apply(object);

      try
      {
        String value = "Test Text";
        String rowAttribute = "Bad";

        HashMap<String, Object> row = new HashMap<String, Object>();
        row.put(rowAttribute, value);
        row.put(BusinessObject.CODE, TEST_CODE);

        BusinessObjectImportConfiguration configuration = new BusinessObjectImportConfiguration();
        configuration.setImportStrategy(ImportStrategy.NEW_ONLY);
        configuration.setType(type);
        configuration.setDate(FastTestDataset.DEFAULT_END_TIME_DATE);
        configuration.setCopyBlank(false);
        configuration.setFunction(attributeType.getName(), new BasicColumnFunction(rowAttribute));
        configuration.setFunction(BusinessObject.CODE, new BasicColumnFunction(BusinessObject.CODE));

        try (BusinessObjectImporter importer = new BusinessObjectImporter(configuration, new NullImportProgressListener()))
        {
          importer.importRow(new MapFeatureRow(row, 0L));
        }

        Assert.assertTrue(configuration.hasExceptions());

        LinkedList<BusinessObjectRecordedErrorException> exceptions = configuration.getExceptions();

        Assert.assertEquals(1, exceptions.size());

        BusinessObjectRecordedErrorException exception = exceptions.get(0);

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
  public void testSetGeoObject() 
  {
    TestDataSet.executeRequestAsUser(USATestData.USER_ADMIN, () -> {

      ServerHierarchyType hierarchy = FastTestDataset.HIER_ADMIN.getServerObject();
      ServerGeoObjectType got = FastTestDataset.DISTRICT.getServerObject();

      String value = "Test Text";
      String rowAttribute = "Bad";
      String geoAttribute = "Geo Object";

      HashMap<String, Object> row = new HashMap<String, Object>();
      row.put(rowAttribute, value);
      row.put(geoAttribute, FastTestDataset.DIST_CENTRAL.getCode());
      row.put(BusinessObject.CODE, TEST_CODE);

      BusinessObjectImportConfiguration configuration = new BusinessObjectImportConfiguration();
      configuration.setImportStrategy(ImportStrategy.NEW_ONLY);
      configuration.setType(type);
      configuration.setHierarchy(hierarchy);
      configuration.setDate(FastTestDataset.DEFAULT_END_TIME_DATE);
      configuration.setCopyBlank(false);
      configuration.setFunction(attributeType.getName(), new BasicColumnFunction(rowAttribute));
      configuration.setFunction(BusinessObject.CODE, new BasicColumnFunction(BusinessObject.CODE));
      configuration.addLocation(new Location(got, hierarchy, new BasicColumnFunction(geoAttribute), ParentMatchStrategy.CODE));

      try (BusinessObjectImporter importer = new BusinessObjectImporter(configuration, new NullImportProgressListener()))
      {
        importer.importRow(new MapFeatureRow(row, 0L));
      }

      BusinessObject result = this.bObjectService.get(type, attributeType.getName(), value);

      try
      {
        Assert.assertNotNull(result);

        List<VertexServerGeoObject> results = this.bObjectService.getGeoObjects(result);

        Assert.assertEquals(1, results.size());

        VertexServerGeoObject geoObject = results.get(0);

        Assert.assertNotNull(geoObject);
        Assert.assertEquals(FastTestDataset.DIST_CENTRAL.getCode(), geoObject.getCode());
        Assert.assertEquals(got.getCode(), geoObject.getType().getCode());
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
  public void testUnknownGeoObject() 
  {
    TestDataSet.executeRequestAsUser(USATestData.USER_ADMIN, () -> {

      ServerHierarchyType hierarchy = FastTestDataset.HIER_ADMIN.getServerObject();
      ServerGeoObjectType got = FastTestDataset.DISTRICT.getServerObject();

      String value = "Test Text";
      String rowAttribute = "Bad";
      String geoAttribute = "Geo Object";

      HashMap<String, Object> row = new HashMap<String, Object>();
      row.put(rowAttribute, value);
      row.put(geoAttribute, "Blarg");
      row.put(BusinessObject.CODE, TEST_CODE);

      BusinessObjectImportConfiguration configuration = new BusinessObjectImportConfiguration();
      configuration.setImportStrategy(ImportStrategy.NEW_ONLY);
      configuration.setType(type);
      configuration.setHierarchy(hierarchy);
      configuration.setDate(FastTestDataset.DEFAULT_END_TIME_DATE);
      configuration.setCopyBlank(false);
      configuration.setFunction(attributeType.getName(), new BasicColumnFunction(rowAttribute));
      configuration.setFunction(BusinessObject.CODE, new BasicColumnFunction(BusinessObject.CODE));
      configuration.addLocation(new Location(got, hierarchy, new BasicColumnFunction(geoAttribute), ParentMatchStrategy.CODE));

      try (BusinessObjectImporter importer = new BusinessObjectImporter(configuration, new NullImportProgressListener()))
      {
        importer.importRow(new MapFeatureRow(row, 0L));
      }

      BusinessObject result = this.bObjectService.get(type, attributeType.getName(), value);

      try
      {
        Assert.assertNull(result);

        LinkedList<BusinessObjectRecordedErrorException> exceptions = configuration.getExceptions();

        Assert.assertEquals(1, exceptions.size());

        BusinessObjectRecordedErrorException exception = exceptions.get(0);
        Throwable error = exception.getError();

        Assert.assertTrue(error instanceof ParentCodeException);

        ParentCodeException ex = (ParentCodeException) error;
        Assert.assertEquals("Blarg", ex.getParentCode());
        Assert.assertEquals(got.getCode(), ex.getParentType());
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
  public void testImportSpreadsheet() throws Throwable
  {
    TestDataSet.executeRequestAsUser(USATestData.USER_ADMIN, () -> {

      InputStream istream = this.getClass().getResourceAsStream("/business-spreadsheet.xlsx");

      Assert.assertNotNull(istream);

      JSONObject json = this.getTestConfiguration(istream, ImportStrategy.NEW_AND_UPDATE);

      BusinessObjectImportConfiguration config = (BusinessObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
      config.setHierarchy(FastTestDataset.HIER_ADMIN.getServerObject());

      ImportHistory hist = mockImport(config);
      Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

      hist = ImportHistory.get(hist.getOid());
      Assert.assertEquals(Long.valueOf(2), hist.getWorkTotal());
      Assert.assertEquals(Long.valueOf(2), hist.getWorkProgress());
      Assert.assertEquals(Long.valueOf(2), hist.getImportedRecords());
      Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

      assertAndDelete(this.bObjectService.get(type, attributeType.getName(), "0001"));
      assertAndDelete(this.bObjectService.get(type, attributeType.getName(), "0002"));
    });
  }

  public void assertAndDelete(BusinessObject o1)
  {
    try
    {
      Assert.assertNotNull(o1);
    }
    finally
    {
      if (o1 != null)
      {
        this.deleteObject(o1);
      }
    }
  }

  private JSONObject getTestConfiguration(InputStream istream, ImportStrategy strategy)
  {
    JSONObject result = this.excelService.getBusinessTypeConfiguration(type.getCode(), TestDataSet.DEFAULT_END_TIME_DATE, "business-spreadsheet.xlsx", istream, strategy, false);
    JSONObject type = result.getJSONObject(BusinessObjectImportConfiguration.TYPE);
    JSONArray attributes = type.getJSONArray(BusinessType.JSON_ATTRIBUTES);

    for (int i = 0; i < attributes.length(); i++)
    {
      JSONObject attribute = attributes.getJSONObject(i);

      String attributeName = attribute.getString(AttributeType.JSON_CODE);

      if (attributeName.equals(attributeType.getName()) || attributeName.equals(BusinessObject.CODE))
      {
        attribute.put(BusinessObjectImportConfiguration.TARGET, "Code");
      }
    }

    result.put(BusinessObjectImportConfiguration.FORMAT_TYPE, FormatImporterType.EXCEL);
    result.put(BusinessObjectImportConfiguration.OBJECT_TYPE, ObjectImportType.BUSINESS_OBJECT);
    result.put(BusinessObjectImportConfiguration.IMPORT_STRATEGY, strategy);

    return result;
  }

  private ImportHistory mockImport(BusinessObjectImportConfiguration config) throws Throwable
  {
    if (config.getDate() == null)
    {
      config.setDate(new Date());
    }

    config.setImportStrategy(ImportStrategy.NEW_AND_UPDATE);

    DataImportJob job = new DataImportJob();
    job.apply();
    ImportHistory hist = (ImportHistory) job.createNewHistory();

    config.setHistoryId(hist.getOid());
    config.setJobId(job.getOid());

    BusinessType type = config.getType();

    hist.appLock();
    hist.setImportFileId(config.getVaultFileId());
    hist.setConfigJson(config.toJSON().toString());
    hist.setOrganization(type.getOrganization());
    hist.setGeoObjectTypeCode(type.getCode());
    hist.apply();

    ExecutionContext context = job.startSynchronously(hist);

    hist = (ImportHistory) context.getHistory();
    return hist;
  }

  private void deleteObject(BusinessObject object)
  {
    for (int i = 0; i < 100; i++)
    {
      try
      {
        this.bObjectService.delete(object);

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
