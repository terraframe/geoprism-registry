package net.geoprism.registry.business;

import java.util.HashMap;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.FeatureRow;
import net.geoprism.registry.ProgrammaticType;
import net.geoprism.registry.etl.NullImportProgressListener;
import net.geoprism.registry.etl.upload.ProgrammaticObjectImportConfiguration;
import net.geoprism.registry.etl.upload.ProgrammaticObjectImporter;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.excel.MapFeatureRow;
import net.geoprism.registry.io.Location;
import net.geoprism.registry.io.ParentMatchStrategy;
import net.geoprism.registry.model.ProgrammaticObject;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.test.FastTestDataset;

public class ProgrammaticObjectImporterTest
{
  private static FastTestDataset  testData;

  private static ProgrammaticType type;

  private static AttributeType    attribute;

  @BeforeClass
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();
    testData.setUpMetadata();
    testData.setUpInstanceData();

    setUpClassInRequest();
  }

  @Request
  private static void setUpClassInRequest()
  {
    String code = "TEST_PROG";
    String orgCode = FastTestDataset.ORG_CGOV.getCode();
    String label = "Test Prog";

    JsonObject object = new JsonObject();
    object.addProperty(ProgrammaticType.CODE, code);
    object.addProperty(ProgrammaticType.ORGANIZATION, orgCode);
    object.add(ProgrammaticType.DISPLAYLABEL, new LocalizedValue(label).toJSON());

    type = ProgrammaticType.apply(object);

    attribute = type.createAttributeType(new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test True"), false, false, false));
  }

  @AfterClass
  public static void cleanUpClass()
  {
    cleanUpClassInRequest();

    if (testData != null)
    {
      testData.tearDownInstanceData();
      testData.tearDownMetadata();
    }
  }

  @Request
  private static void cleanUpClassInRequest()
  {
    if (type != null)
    {
      type.delete();
    }
  }

  @Test
  @Request
  public void testImportValue() throws InterruptedException
  {
    String value = "Test Text";
    String rowAttribute = "Bad";

    HashMap<String, Object> row = new HashMap<String, Object>();
    row.put(rowAttribute, value);

    ProgrammaticObjectImportConfiguration configuration = new ProgrammaticObjectImportConfiguration();
    configuration.setImportStrategy(ImportStrategy.NEW_ONLY);
    configuration.setType(type);
    configuration.setDate(FastTestDataset.DEFAULT_END_TIME_DATE);
    configuration.setCopyBlank(false);
    configuration.setFunction(attribute.getName(), new BasicColumnFunction(rowAttribute));

    ProgrammaticObjectImporter importer = new ProgrammaticObjectImporter(configuration, new NullImportProgressListener());
    importer.importRow(new MapFeatureRow(row));

    ProgrammaticObject result = ProgrammaticObject.get(type, attribute.getName(), value);

    try
    {
      Assert.assertNotNull(result);
    }
    finally
    {
      result.delete();
    }
  }

  @Test
  @Request
  public void testSetGeoObject() throws InterruptedException
  {
    ServerHierarchyType hierarchy = FastTestDataset.HIER_ADMIN.getServerObject();
    ServerGeoObjectType got = FastTestDataset.DISTRICT.getServerObject();

    String value = "Test Text";
    String rowAttribute = "Bad";
    String geoAttribute = "Geo Object";

    HashMap<String, Object> row = new HashMap<String, Object>();
    row.put(rowAttribute, value);
    row.put(geoAttribute, FastTestDataset.DIST_CENTRAL.getCode());

    ProgrammaticObjectImportConfiguration configuration = new ProgrammaticObjectImportConfiguration();
    configuration.setImportStrategy(ImportStrategy.NEW_ONLY);
    configuration.setType(type);
    configuration.setHierarchy(hierarchy);
    configuration.setDate(FastTestDataset.DEFAULT_END_TIME_DATE);
    configuration.setCopyBlank(false);
    configuration.setFunction(attribute.getName(), new BasicColumnFunction(rowAttribute));
    configuration.addLocation(new Location(got, hierarchy, new BasicColumnFunction(geoAttribute), ParentMatchStrategy.CODE));

    ProgrammaticObjectImporter importer = new ProgrammaticObjectImporter(configuration, new NullImportProgressListener());
    importer.importRow(new MapFeatureRow(row));

    ProgrammaticObject result = ProgrammaticObject.get(type, attribute.getName(), value);

    try
    {
      Assert.assertNotNull(result);

      VertexServerGeoObject geoObject = result.getGeoObject();

      Assert.assertNotNull(geoObject);
      Assert.assertEquals(FastTestDataset.DIST_CENTRAL.getCode(), geoObject.getCode());
      Assert.assertEquals(got.getCode(), geoObject.getType().getCode());
    }
    finally
    {
      if (result != null)
      {
        result.delete();
      }
    }
  }
}
