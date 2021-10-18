package net.geoprism.registry;

import java.util.Map;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;

import net.geoprism.registry.test.FastTestDataset;

public class ProgrammaticTypeTest
{
  private static FastTestDataset testData;

  @BeforeClass
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();
    testData.setUpMetadata();
  }

  @AfterClass
  public static void cleanUpClass()
  {
    if (testData != null)
    {
      testData.tearDownMetadata();
    }
  }

  @Test
  @Request
  public void testCreate()
  {
    String code = "TEST_PROG";
    String orgCode = FastTestDataset.ORG_CGOV.getCode();
    String label = "Test Prog";

    JsonObject object = new JsonObject();
    object.addProperty(ProgrammaticType.CODE, code);
    object.addProperty(ProgrammaticType.ORGANIZATION, orgCode);
    object.add(ProgrammaticType.DISPLAYLABEL, new LocalizedValue(label).toJSON());

    ProgrammaticType type = ProgrammaticType.apply(object);

    try
    {
      Assert.assertEquals(code, type.getCode());
      Assert.assertEquals(orgCode, type.getOrganization().getCode());
      Assert.assertEquals(label, type.getDisplayLabel().getValue());
      Assert.assertNotNull(type.getMdVertex());
    }
    finally
    {
      type.delete();
    }
  }

  @Test
  @Request
  public void testUpdate()
  {
    String code = "TEST_PROG";
    String orgCode = FastTestDataset.ORG_CGOV.getCode();
    String label = "Updated Prog";

    JsonObject object = new JsonObject();
    object.addProperty(ProgrammaticType.CODE, code);
    object.addProperty(ProgrammaticType.ORGANIZATION, orgCode);
    object.add(ProgrammaticType.DISPLAYLABEL, new LocalizedValue("Test Prog").toJSON());

    ProgrammaticType type = ProgrammaticType.apply(object);

    try
    {
      JsonObject json = type.toJSON();
      json.add(ProgrammaticType.DISPLAYLABEL, new LocalizedValue(label).toJSON());

      type = ProgrammaticType.apply(json);

      Assert.assertEquals(code, type.getCode());
      Assert.assertEquals(orgCode, type.getOrganization().getCode());
      Assert.assertEquals(label, type.getDisplayLabel().getValue());
      Assert.assertNotNull(type.getMdVertex());
    }
    finally
    {
      type.delete();
    }
  }

  @Test
  @Request
  public void testAddCharacterAttribute()
  {
    String code = "TEST_PROG";
    String orgCode = FastTestDataset.ORG_CGOV.getCode();
    String label = "Test Prog";

    JsonObject object = new JsonObject();
    object.addProperty(ProgrammaticType.CODE, code);
    object.addProperty(ProgrammaticType.ORGANIZATION, orgCode);
    object.add(ProgrammaticType.DISPLAYLABEL, new LocalizedValue(label).toJSON());

    ProgrammaticType type = ProgrammaticType.apply(object);

    try
    {
      AttributeCharacterType expected = new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test True"), false, false, false);

      type.createAttributeType(expected);

      Map<String, AttributeType> attributeMap = type.getAttributeMap();

      Assert.assertTrue(attributeMap.containsKey(expected.getName()));

      AttributeType actual = attributeMap.get(expected.getName());

      Assert.assertTrue(actual instanceof AttributeCharacterType);
      Assert.assertEquals(expected.getName(), actual.getName());
      Assert.assertEquals(expected.getLabel().getValue(), actual.getLabel().getValue());
      Assert.assertEquals(expected.getIsDefault(), actual.getIsDefault());
    }
    finally
    {
      type.delete();
    }
  }

  @Test
  @Request
  public void testAddDateAttribute()
  {
    String code = "TEST_PROG";
    String orgCode = FastTestDataset.ORG_CGOV.getCode();
    String label = "Test Prog";

    JsonObject object = new JsonObject();
    object.addProperty(ProgrammaticType.CODE, code);
    object.addProperty(ProgrammaticType.ORGANIZATION, orgCode);
    object.add(ProgrammaticType.DISPLAYLABEL, new LocalizedValue(label).toJSON());

    ProgrammaticType type = ProgrammaticType.apply(object);

    try
    {
      AttributeDateType expected = new AttributeDateType("testDate", new LocalizedValue("Test Date"), new LocalizedValue("Test True"), false, false, false);

      type.createAttributeType(expected);

      Map<String, AttributeType> attributeMap = type.getAttributeMap();

      Assert.assertTrue(attributeMap.containsKey(expected.getName()));

      AttributeType actual = attributeMap.get(expected.getName());

      Assert.assertTrue(actual instanceof AttributeDateType);
      Assert.assertEquals(expected.getName(), actual.getName());
      Assert.assertEquals(expected.getLabel().getValue(), actual.getLabel().getValue());
      Assert.assertEquals(expected.getIsDefault(), actual.getIsDefault());
    }
    finally
    {
      type.delete();
    }
  }

  @Test
  @Request
  public void testAddIntegerAttribute()
  {
    String code = "TEST_PROG";
    String orgCode = FastTestDataset.ORG_CGOV.getCode();
    String label = "Test Prog";

    JsonObject object = new JsonObject();
    object.addProperty(ProgrammaticType.CODE, code);
    object.addProperty(ProgrammaticType.ORGANIZATION, orgCode);
    object.add(ProgrammaticType.DISPLAYLABEL, new LocalizedValue(label).toJSON());

    ProgrammaticType type = ProgrammaticType.apply(object);

    try
    {
      AttributeIntegerType expected = new AttributeIntegerType("testInteger", new LocalizedValue("Test Integer"), new LocalizedValue("Test True"), false, false, false);

      type.createAttributeType(expected);

      Map<String, AttributeType> attributeMap = type.getAttributeMap();

      Assert.assertTrue(attributeMap.containsKey(expected.getName()));

      AttributeType actual = attributeMap.get(expected.getName());

      Assert.assertTrue(actual instanceof AttributeIntegerType);
      Assert.assertEquals(expected.getName(), actual.getName());
      Assert.assertEquals(expected.getLabel().getValue(), actual.getLabel().getValue());
      Assert.assertEquals(expected.getIsDefault(), actual.getIsDefault());
    }
    finally
    {
      type.delete();
    }
  }

  @Test
  @Request
  public void testAddFloatAttribute()
  {
    String code = "TEST_PROG";
    String orgCode = FastTestDataset.ORG_CGOV.getCode();
    String label = "Test Prog";

    JsonObject object = new JsonObject();
    object.addProperty(ProgrammaticType.CODE, code);
    object.addProperty(ProgrammaticType.ORGANIZATION, orgCode);
    object.add(ProgrammaticType.DISPLAYLABEL, new LocalizedValue(label).toJSON());

    ProgrammaticType type = ProgrammaticType.apply(object);

    try
    {
      AttributeFloatType expected = new AttributeFloatType("testFloat", new LocalizedValue("Test Float"), new LocalizedValue("Test True"), false, false, false);
      expected.setPrecision(10);
      expected.setScale(2);

      type.createAttributeType(expected);

      Map<String, AttributeType> attributeMap = type.getAttributeMap();

      Assert.assertTrue(attributeMap.containsKey(expected.getName()));

      AttributeType actual = attributeMap.get(expected.getName());

      Assert.assertTrue(actual instanceof AttributeFloatType);
      Assert.assertEquals(expected.getName(), actual.getName());
      Assert.assertEquals(expected.getLabel().getValue(), actual.getLabel().getValue());
      Assert.assertEquals(expected.getIsDefault(), actual.getIsDefault());
    }
    finally
    {
      type.delete();
    }
  }

  @Test
  @Request
  public void testAddTermAttribute()
  {
    String code = "TEST_PROG";
    String orgCode = FastTestDataset.ORG_CGOV.getCode();
    String label = "Test Prog";

    JsonObject object = new JsonObject();
    object.addProperty(ProgrammaticType.CODE, code);
    object.addProperty(ProgrammaticType.ORGANIZATION, orgCode);
    object.add(ProgrammaticType.DISPLAYLABEL, new LocalizedValue(label).toJSON());

    ProgrammaticType type = ProgrammaticType.apply(object);

    try
    {
      AttributeTermType expected = new AttributeTermType("testTerm", new LocalizedValue("Test Term"), new LocalizedValue("Test True"), false, false, false);

      type.createAttributeType(expected);

      Map<String, AttributeType> attributeMap = type.getAttributeMap();

      Assert.assertTrue(attributeMap.containsKey(expected.getName()));

      AttributeType actual = attributeMap.get(expected.getName());

      Assert.assertTrue(actual instanceof AttributeTermType);
      Assert.assertNotNull( ( (AttributeTermType) actual ).getRootTerm());
      Assert.assertEquals(expected.getName(), actual.getName());
      Assert.assertEquals(expected.getLabel().getValue(), actual.getLabel().getValue());
      Assert.assertEquals(expected.getIsDefault(), actual.getIsDefault());
    }
    finally
    {
      type.delete();
    }
  }

  @Test
  @Request
  public void testUpdateCharacterAttribute()
  {
    String code = "TEST_PROG";
    String orgCode = FastTestDataset.ORG_CGOV.getCode();
    String label = "Test Prog";

    JsonObject object = new JsonObject();
    object.addProperty(ProgrammaticType.CODE, code);
    object.addProperty(ProgrammaticType.ORGANIZATION, orgCode);
    object.add(ProgrammaticType.DISPLAYLABEL, new LocalizedValue(label).toJSON());

    ProgrammaticType type = ProgrammaticType.apply(object);

    try
    {
      AttributeCharacterType original = new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test True"), false, false, false);

      type.createAttributeType(original);

      AttributeCharacterType expected = new AttributeCharacterType("testCharacter", new LocalizedValue("Test Characterzzzzzz"), new LocalizedValue("Test True"), false, false, false);

      type.updateAttributeType(expected);

      Map<String, AttributeType> attributeMap = type.getAttributeMap();

      Assert.assertTrue(attributeMap.containsKey(expected.getName()));

      AttributeType actual = attributeMap.get(expected.getName());

      Assert.assertTrue(actual instanceof AttributeCharacterType);
      Assert.assertEquals(expected.getName(), actual.getName());
      Assert.assertEquals(expected.getLabel().getValue(), actual.getLabel().getValue());
      Assert.assertEquals(expected.getIsDefault(), actual.getIsDefault());
    }
    finally
    {
      type.delete();
    }
  }

  @Test
  @Request
  public void testListByOrg()
  {
    String code = "TEST_PROG";
    String orgCode = FastTestDataset.ORG_CGOV.getCode();
    String label = "Test Prog";

    JsonObject object = new JsonObject();
    object.addProperty(ProgrammaticType.CODE, code);
    object.addProperty(ProgrammaticType.ORGANIZATION, orgCode);
    object.add(ProgrammaticType.DISPLAYLABEL, new LocalizedValue(label).toJSON());

    ProgrammaticType type = ProgrammaticType.apply(object);

    try
    {
      JsonArray orgs = ProgrammaticType.listByOrg();

      Assert.assertEquals(2, orgs.size());

      JsonObject org = orgs.get(0).getAsJsonObject();
      Assert.assertEquals(orgCode, org.get("code").getAsString());

      JsonArray types = org.get("types").getAsJsonArray();

      Assert.assertEquals(1, types.size());

      JsonObject json = types.get(0).getAsJsonObject();

      Assert.assertEquals(type.getCode(), json.get("code").getAsString());
    }
    finally
    {
      type.delete();
    }
  }

}
