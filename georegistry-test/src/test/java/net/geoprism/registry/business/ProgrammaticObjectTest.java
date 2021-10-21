package net.geoprism.registry.business;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;

import net.geoprism.registry.ProgrammaticType;
import net.geoprism.registry.model.ProgrammaticObject;
import net.geoprism.registry.test.FastTestDataset;

public class ProgrammaticObjectTest
{
  private static FastTestDataset  testData;

  private static ProgrammaticType type;

  private static AttributeType    attribute;

  @BeforeClass
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();
    testData.setUpMetadata();

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
  public void testBasicCreate()
  {
    ProgrammaticObject object = ProgrammaticObject.newInstance(type);
    object.apply();

    Assert.assertNotNull(object.getVertex().getRID());

    object.delete();
  }

  @Test
  @Request
  public void testSetGetValue()
  {
    ProgrammaticObject object = ProgrammaticObject.newInstance(type);
    object.setValue(attribute.getName(), "Test Text");
    object.apply();

    Assert.assertEquals("Test Text", object.getObjectValue(attribute.getName()));

    object.delete();
  }

  @Test
  @Request
  public void testGet()
  {
    ProgrammaticObject object = ProgrammaticObject.newInstance(type);
    object.setValue(attribute.getName(), "Test Text");
    object.apply();

    ProgrammaticObject result = ProgrammaticObject.get(type, attribute.getName(), object.getObjectValue(attribute.getName()));

    Assert.assertEquals((String) object.getObjectValue("oid"), (String) result.getObjectValue("oid"));
  }
}
