/**
 *
 */
package net.geoprism.registry;

import java.util.Calendar;
import java.util.Iterator;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.constants.MdAttributeLocalInfo;

import net.geoprism.registry.service.request.RegistryComponentService;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.USATestData;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class ConversionTest extends USADatasetTest implements InstanceTestClassListener
{
  protected static TestGeoObjectInfo testGo;

  @Autowired
  private RegistryComponentService   service;

  @Override
  public void beforeClassSetup() throws Exception
  {
    super.beforeClassSetup();

    testGo = testData.newTestGeoObjectInfo("ConversionTest", USATestData.STATE, USATestData.SOURCE);
  }

  @Before
  public void setUp()
  {
    testGo.delete();

    testData.logIn(USATestData.USER_NPS_RA);
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    testGo.delete();
  }

  @Test
  public void testAttributeTypeDateTree()
  {
    String sessionId = testData.clientRequest.getSessionId();

    Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.set(2019, Calendar.JANUARY, 12, 20, 21, 32);

    // Add a new custom attribute
    AttributeType testDate = AttributeType.factory("testDate", new LocalizedValue("testDateLocalName"), new LocalizedValue("testDateLocalDescrip"), AttributeDateType.TYPE, false, false, false);
    testDate = service.createAttributeType(sessionId, USATestData.STATE.getCode(), testDate.toJSON().toString());

    // Create a new GeoObject with the custom attribute
    GeoObject geoObj = service.newGeoObjectInstance(sessionId, USATestData.STATE.getCode());
    geoObj.setCode(testGo.getCode());
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
    geoObj.setUid(service.getUIDS(sessionId, 1)[0]);
    geoObj.setValue(testDate.getName(), calendar.getTime());

    service.createGeoObject(sessionId, geoObj.toJSON().toString(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);

    // Get the object with the custom attribute
    GeoObject result = service.getGeoObjectByCode(sessionId, testGo.getCode(), USATestData.STATE.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertNotNull(result);
    Assert.assertEquals(geoObj.getValue(testDate.getName()), result.getValue(testDate.getName()));
  }

  // Heads up: clean up
  // @Test
  // public void testAttributeTypeDateLeaf()
  // {
  // String sessionId = testData.adminClientRequest.getSessionId();
  //
  // Calendar calendar = Calendar.getInstance();
  // calendar.clear();
  // calendar.set(2019, Calendar.JANUARY, 12, 20, 21, 32);
  //
  // // Add a new custom attribute
  // AttributeType testDate = AttributeType.factory("testDate", new
  // LocalizedValue("testDateLocalName"), new
  // LocalizedValue("testDateLocalDescrip"), AttributeDateType.TYPE, false,
  // false, false);
  // testDate =
  // service.createAttributeType(sessionId,
  // USATestData.DISTRICT.getCode(), testDate.toJSON().toString());
  //
  // // Create a new GeoObject with the custom attribute
  // GeoObject geoObj =
  // service.newGeoObjectInstance(sessionId,
  // USATestData.DISTRICT.getCode());
  // geoObj.setCode(testGo.getCode());
  // geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
  // geoObj.setUid(service.getUIDS(sessionId,
  // 1)[0]);
  // geoObj.setValue(testDate.getName(), calendar.getTime());
  //
  // service.createGeoObject(sessionId,
  // geoObj.toJSON().toString());
  //
  // // Get the object with the custom attribute
  // GeoObject result =
  // service.getGeoObjectByCode(sessionId,
  // testGo.getCode(), USATestData.DISTRICT.getCode());
  //
  // Assert.assertNotNull(result);
  // Assert.assertEquals(geoObj.getValue(testDate.getName()),
  // result.getValue(testDate.getName()));
  // }

  @Test
  public void testAttributeTypeBooleanTree()
  {
    String sessionId = testData.clientRequest.getSessionId();

    // Add a new custom attribute
    AttributeType testBoolean = AttributeType.factory("testBoolean", new LocalizedValue("testBooleanLocalName"), new LocalizedValue("testBooleanLocalDescrip"), AttributeBooleanType.TYPE, false, false, false);
    testBoolean = service.createAttributeType(sessionId, USATestData.STATE.getCode(), testBoolean.toJSON().toString());

    // Create a new GeoObject with the custom attribute
    GeoObject geoObj = service.newGeoObjectInstance(sessionId, USATestData.STATE.getCode());
    geoObj.setCode(testGo.getCode());
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
    geoObj.setUid(service.getUIDS(sessionId, 1)[0]);
    geoObj.setValue(testBoolean.getName(), Boolean.valueOf(true));

    service.createGeoObject(sessionId, geoObj.toJSON().toString(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);

    // Get the object with the custom attribute
    GeoObject result = service.getGeoObjectByCode(sessionId, testGo.getCode(), USATestData.STATE.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertNotNull(result);
    Assert.assertEquals(geoObj.getValue(testBoolean.getName()), result.getValue(testBoolean.getName()));
  }

  // Heads up: clean up
  // @Test
  // public void testAttributeTypeBooleanLeaf()
  // {
  // String sessionId = testData.adminClientRequest.getSessionId();
  //
  // // Add a new custom attribute
  // AttributeType testBoolean = AttributeType.factory("testBoolean", new
  // LocalizedValue("testBooleanLocalName"), new
  // LocalizedValue("testBooleanLocalDescrip"), AttributeBooleanType.TYPE,
  // false, false, false);
  // testBoolean =
  // service.createAttributeType(sessionId,
  // USATestData.DISTRICT.getCode(), testBoolean.toJSON().toString());
  //
  // // Create a new GeoObject with the custom attribute
  // GeoObject geoObj =
  // service.newGeoObjectInstance(sessionId,
  // USATestData.DISTRICT.getCode());
  // geoObj.setCode(testGo.getCode());
  // geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
  // geoObj.setUid(service.getUIDS(sessionId,
  // 1)[0]);
  // geoObj.setValue(testBoolean.getName(), Boolean.valueOf(true));
  //
  // service.createGeoObject(sessionId,
  // geoObj.toJSON().toString());
  //
  // // Get the object with the custom attribute
  // GeoObject result =
  // service.getGeoObjectByCode(sessionId,
  // testGo.getCode(), USATestData.DISTRICT.getCode());
  //
  // Assert.assertNotNull(result);
  // Assert.assertEquals(geoObj.getValue(testBoolean.getName()),
  // result.getValue(testBoolean.getName()));
  // }

  @Test
  public void testAttributeTypeFloatTree()
  {
    String sessionId = testData.clientRequest.getSessionId();

    // Add a new custom attribute
    AttributeType testFloat = AttributeType.factory("testFloat", new LocalizedValue("testFloatLocalName"), new LocalizedValue("testFloatLocalDescrip"), AttributeFloatType.TYPE, false, false, false);
    testFloat = service.createAttributeType(sessionId, USATestData.STATE.getCode(), testFloat.toJSON().toString());

    // Create a new GeoObject with the custom attribute
    GeoObject geoObj = service.newGeoObjectInstance(sessionId, USATestData.STATE.getCode());
    geoObj.setCode(testGo.getCode());
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
    geoObj.setUid(service.getUIDS(sessionId, 1)[0]);
    geoObj.setValue(testFloat.getName(), Double.valueOf(234.2));

    service.createGeoObject(sessionId, geoObj.toJSON().toString(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);

    // Get the object with the custom attribute
    GeoObject result = service.getGeoObjectByCode(sessionId, testGo.getCode(), USATestData.STATE.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertNotNull(result);
    Assert.assertEquals(geoObj.getValue(testFloat.getName()), result.getValue(testFloat.getName()));
  }

  // Heads up: clean up
  // @Test
  // public void testAttributeTypeFloatLeaf()
  // {
  // String sessionId = testData.adminClientRequest.getSessionId();
  //
  // // Add a new custom attribute
  // AttributeType testFloat = AttributeType.factory("testFloat", new
  // LocalizedValue("testFloatLocalName"), new
  // LocalizedValue("testFloatLocalDescrip"), AttributeFloatType.TYPE, false,
  // false, false);
  // testFloat =
  // service.createAttributeType(sessionId,
  // USATestData.DISTRICT.getCode(), testFloat.toJSON().toString());
  //
  // // Create a new GeoObject with the custom attribute
  // GeoObject geoObj =
  // service.newGeoObjectInstance(sessionId,
  // USATestData.DISTRICT.getCode());
  // geoObj.setCode(testGo.getCode());
  // geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
  // geoObj.setUid(service.getUIDS(sessionId,
  // 1)[0]);
  // geoObj.setValue(testFloat.getName(), Double.valueOf(234.2));
  //
  // service.createGeoObject(sessionId,
  // geoObj.toJSON().toString());
  //
  // // Get the object with the custom attribute
  // GeoObject result =
  // service.getGeoObjectByCode(sessionId,
  // testGo.getCode(), USATestData.DISTRICT.getCode());
  //
  // Assert.assertNotNull(result);
  // Assert.assertEquals(geoObj.getValue(testFloat.getName()),
  // result.getValue(testFloat.getName()));
  // }

  @Test
  public void testAttributeTypeIntegerTree()
  {
    String sessionId = testData.clientRequest.getSessionId();

    // Add a new custom attribute
    AttributeType testInteger = AttributeType.factory("testInteger", new LocalizedValue("testIntegerLocalName"), new LocalizedValue("testIntegerLocalDescrip"), AttributeIntegerType.TYPE, false, false, false);
    testInteger = service.createAttributeType(sessionId, USATestData.STATE.getCode(), testInteger.toJSON().toString());

    // Create a new GeoObject with the custom attribute
    GeoObject geoObj = service.newGeoObjectInstance(sessionId, USATestData.STATE.getCode());
    geoObj.setCode(testGo.getCode());
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
    geoObj.setUid(service.getUIDS(sessionId, 1)[0]);
    geoObj.setValue(testInteger.getName(), Long.valueOf(123));

    service.createGeoObject(sessionId, geoObj.toJSON().toString(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);

    // Get the object with the custom attribute
    GeoObject result = service.getGeoObjectByCode(sessionId, testGo.getCode(), USATestData.STATE.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertNotNull(result);
    Assert.assertEquals(geoObj.getValue(testInteger.getName()), result.getValue(testInteger.getName()));
  }
  //
  // @Test
  // public void testAttributeTypeIntegerLeaf()
  // {
  // String sessionId = testData.adminClientRequest.getSessionId();
  //
  // // Add a new custom attribute
  // AttributeType testInteger = AttributeType.factory("testInteger", new
  // LocalizedValue("testIntegerLocalName"), new
  // LocalizedValue("testIntegerLocalDescrip"), AttributeIntegerType.TYPE,
  // false, false, false);
  // testInteger =
  // service.createAttributeType(sessionId,
  // USATestData.DISTRICT.getCode(), testInteger.toJSON().toString());
  //
  // // Create a new GeoObject with the custom attribute
  // GeoObject geoObj =
  // service.newGeoObjectInstance(sessionId,
  // USATestData.DISTRICT.getCode());
  // geoObj.setCode(testGo.getCode());
  // geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
  // geoObj.setUid(service.getUIDS(sessionId,
  // 1)[0]);
  // geoObj.setValue(testInteger.getName(), Long.valueOf(234));
  //
  // service.createGeoObject(sessionId,
  // geoObj.toJSON().toString());
  //
  // // Get the object with the custom attribute
  // GeoObject result =
  // service.getGeoObjectByCode(sessionId,
  // testGo.getCode(), USATestData.DISTRICT.getCode());
  //
  // Assert.assertNotNull(result);
  // Assert.assertEquals(geoObj.getValue(testInteger.getName()),
  // result.getValue(testInteger.getName()));
  // }

  @Test
  public void testAttributeTypeCharacterTree()
  {
    String sessionId = testData.clientRequest.getSessionId();

    // Add a new custom attribute
    AttributeType testCharacter = AttributeType.factory("testCharacter", new LocalizedValue("testCharacterLocalName"), new LocalizedValue("testCharacterLocalDescrip"), AttributeCharacterType.TYPE, false, false, false);
    testCharacter = service.createAttributeType(sessionId, USATestData.STATE.getCode(), testCharacter.toJSON().toString());

    // Create a new GeoObject with the custom attribute
    GeoObject geoObj = service.newGeoObjectInstance(sessionId, USATestData.STATE.getCode());
    geoObj.setCode(testGo.getCode());
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
    geoObj.setUid(service.getUIDS(sessionId, 1)[0]);
    geoObj.setValue(testCharacter.getName(), "ABC");

    service.createGeoObject(sessionId, geoObj.toJSON().toString(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);

    // Get the object with the custom attribute
    GeoObject result = service.getGeoObjectByCode(sessionId, testGo.getCode(), USATestData.STATE.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertNotNull(result);
    Assert.assertEquals(geoObj.getValue(testCharacter.getName()), result.getValue(testCharacter.getName()));
  }

  // Heads up: clean up
  // @Test
  // public void testAttributeTypeCharacterLeaf()
  // {
  // String sessionId = testData.adminClientRequest.getSessionId();
  //
  // // Add a new custom attribute
  // AttributeType testCharacter = AttributeType.factory("testCharacter", new
  // LocalizedValue("testCharacterLocalName"), new
  // LocalizedValue("testCharacterLocalDescrip"), AttributeCharacterType.TYPE,
  // false, false, false);
  // testCharacter =
  // service.createAttributeType(sessionId,
  // USATestData.DISTRICT.getCode(), testCharacter.toJSON().toString());
  //
  // // Create a new GeoObject with the custom attribute
  // GeoObject geoObj =
  // service.newGeoObjectInstance(sessionId,
  // USATestData.DISTRICT.getCode());
  // geoObj.setCode(testGo.getCode());
  // geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
  // geoObj.setUid(service.getUIDS(sessionId,
  // 1)[0]);
  // geoObj.setValue(testCharacter.getName(), "ABCZ");
  //
  // service.createGeoObject(sessionId,
  // geoObj.toJSON().toString());
  //
  // // Get the object with the custom attribute
  // GeoObject result =
  // service.getGeoObjectByCode(sessionId,
  // testGo.getCode(), USATestData.DISTRICT.getCode());
  //
  // Assert.assertNotNull(result);
  // Assert.assertEquals(geoObj.getValue(testCharacter.getName()),
  // result.getValue(testCharacter.getName()));
  // }

  @Test
  @SuppressWarnings("unchecked")
  public void testAttributeTypeTermTree()
  {
    String sessionId = testData.clientRequest.getSessionId();

    // Add a new custom attribute
    AttributeTermType testTerm = (AttributeTermType) AttributeType.factory("testTerm", new LocalizedValue("testTermLocalName"), new LocalizedValue("testTermLocalDescrip"), AttributeTermType.TYPE, false, false, false);
    testTerm = (AttributeTermType) service.createAttributeType(sessionId, USATestData.STATE.getCode(), testTerm.toJSON().toString());
    Term rootTerm = testTerm.getRootTerm();

    Term term2 = new Term("termValue2", new LocalizedValue("Term Value 2"), new LocalizedValue(""));
    Term term = service.createTerm(sessionId, rootTerm.getCode(), term2.toJSON().toString());

    TestDataSet.refreshTerms(testTerm);

    try
    {
      // Create a new GeoObject with the custom attribute
      GeoObject geoObj = service.newGeoObjectInstance(sessionId, USATestData.STATE.getCode());
      geoObj.setCode(testGo.getCode());
      geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
      geoObj.setUid(service.getUIDS(sessionId, 1)[0]);
      geoObj.setValue(testTerm.getName(), term.getCode());

      service.createGeoObject(sessionId, geoObj.toJSON().toString(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);

      // Get the object with the custom attribute
      GeoObject result = service.getGeoObjectByCode(sessionId, testGo.getCode(), USATestData.STATE.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

      Assert.assertNotNull(result);
      Iterator<String> expected = (Iterator<String>) geoObj.getValue(testTerm.getName());
      Iterator<String> test = (Iterator<String>) result.getValue(testTerm.getName());

      Assert.assertTrue(expected.hasNext());
      Assert.assertTrue(test.hasNext());

      Assert.assertEquals(expected.next(), test.next());
    }
    finally
    {
      service.deleteTerm(sessionId, rootTerm.getCode(), term.getCode());
    }
  }
  // Heads up: clean up
  // @Test
  // public void testUpdateLeaf()
  // {
  // String sessionId = testData.adminClientRequest.getSessionId();
  // String uid = service.getUIDS(sessionId, 1)[0];
  //
  // // Create a new GeoObject with the custom attribute
  // GeoObject geoObj =
  // service.newGeoObjectInstance(sessionId,
  // USATestData.DISTRICT.getCode());
  // geoObj.setCode(testGo.getCode());
  // geoObj.setDisplayLabel(MdAttributeLocalInfo.DEFAULT_LOCALE, "Test Label");
  // geoObj.setUid(uid);
  //
  // geoObj = service.createGeoObject(sessionId,
  // geoObj.toJSON().toString());
  // geoObj.setDisplayLabel(MdAttributeLocalInfo.DEFAULT_LOCALE, "New Label");
  //
  // geoObj = service.updateGeoObject(sessionId,
  // geoObj.toJSON().toString());
  //
  // // Get the object with the custom attribute
  // GeoObject result =
  // service.getGeoObjectByCode(sessionId,
  // testGo.getCode(), USATestData.DISTRICT.getCode());
  //
  // Assert.assertNotNull(result);
  // Assert.assertEquals("New Label", geoObj.getLocalizedDisplayLabel());
  // }

  @Test
  public void testUpdateTree()
  {
    String sessionId = testData.clientRequest.getSessionId();

    // Create a new GeoObject with the custom attribute
    GeoObject geoObj = service.newGeoObjectInstance(sessionId, USATestData.STATE.getCode());
    geoObj.setCode(testGo.getCode());
    geoObj.setDisplayLabel(MdAttributeLocalInfo.DEFAULT_LOCALE, "Test Label");
    geoObj.setUid(service.getUIDS(sessionId, 1)[0]);

    geoObj = service.createGeoObject(sessionId, geoObj.toJSON().toString(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);
    geoObj.setDisplayLabel(MdAttributeLocalInfo.DEFAULT_LOCALE, "New Label");

    geoObj = service.updateGeoObject(sessionId, geoObj.toJSON().toString(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);

    // Get the object with the custom attribute
    GeoObject result = service.getGeoObjectByCode(sessionId, testGo.getCode(), USATestData.STATE.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertNotNull(result);
    Assert.assertEquals("New Label", geoObj.getLocalizedDisplayLabel());
  }

  // Heads up: clean up
  // @SuppressWarnings("unchecked")
  // @Test
  // public void testAttributeTypeTermLeaf()
  // {
  // String sessionId = testData.adminClientRequest.getSessionId();
  //
  // // Add a new custom attribute
  // AttributeTermType testTerm = (AttributeTermType)
  // AttributeType.factory("testTerm", new LocalizedValue("testTermLocalName"),
  // new LocalizedValue("testTermLocalDescrip"), AttributeTermType.TYPE, false,
  // false, false);
  // testTerm = (AttributeTermType)
  // service.createAttributeType(sessionId,
  // USATestData.DISTRICT.getCode(), testTerm.toJSON().toString());
  // Term rootTerm = testTerm.getRootTerm();
  //
  // Term term = service.createTerm(sessionId,
  // rootTerm.getCode(), new Term("termValue2", new LocalizedValue("Term Value
  // 2"), new LocalizedValue("")).toJSON().toString());
  //
  // TestDataSet.refreshTerms(testTerm);
  //
  // try
  // {
  // // Create a new GeoObject with the custom attribute
  // GeoObject geoObj =
  // service.newGeoObjectInstance(sessionId,
  // USATestData.DISTRICT.getCode());
  // geoObj.setCode(testGo.getCode());
  // geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
  // geoObj.setUid(service.getUIDS(sessionId,
  // 1)[0]);
  // geoObj.setValue(testTerm.getName(), term.getCode());
  //
  // service.createGeoObject(sessionId,
  // geoObj.toJSON().toString());
  //
  // // Get the object with the custom attribute
  // GeoObject result =
  // service.getGeoObjectByCode(sessionId,
  // testGo.getCode(), USATestData.DISTRICT.getCode());
  //
  // Assert.assertNotNull(result);
  // Iterator<String> expected = (Iterator<String>)
  // geoObj.getValue(testTerm.getName());
  // Iterator<String> test = (Iterator<String>)
  // result.getValue(testTerm.getName());
  //
  // Assert.assertTrue(expected.hasNext());
  // Assert.assertTrue(test.hasNext());
  //
  // Assert.assertEquals(expected.next(), test.next());
  // }
  // finally
  // {
  // service.deleteTerm(sessionId, term.getCode());
  // }
  // }
}
