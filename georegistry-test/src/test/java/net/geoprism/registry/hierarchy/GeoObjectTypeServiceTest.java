package net.geoprism.registry.hierarchy;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.MetadataFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.MdBusinessInfo;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.gis.constants.MdGeoVertexInfo;
import com.runwaysdk.gis.dataaccess.MdGeoVertexDAOIF;
import com.runwaysdk.gis.dataaccess.metadata.graph.MdGeoVertexDAO;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdBusiness;

import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.graph.GeoVertexType;
import net.geoprism.registry.permission.PermissionContext;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.USATestData;

public class GeoObjectTypeServiceTest
{
  public static TestGeoObjectTypeInfo TEST_GOT = new TestGeoObjectTypeInfo("GOTTest_TEST1", FastTestDataset.ORG_CGOV);

  protected static FastTestDataset    testData;

  @BeforeClass
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();
    testData.setUpMetadata();
  }

  @AfterClass
  public static void cleanUpClass()
  {
    testData.tearDownMetadata();
  }

  @Before
  public void setUp()
  {
    testData.setUpInstanceData();

    setUpExtras();

    testData.logIn(FastTestDataset.USER_CGOV_RA);
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    cleanUpExtras();

    testData.tearDownInstanceData();
  }

  private void cleanUpExtras()
  {
    TestDataSet.deleteClassifier("termValue1");
    TestDataSet.deleteClassifier("termValue2");

    TEST_GOT.delete();
  }

  private void setUpExtras()
  {
    cleanUpExtras();
  }

  @Test
  public void testCreateGeoObjectType()
  {
    String organizationCode = FastTestDataset.ORG_CGOV.getCode();

    GeoObjectType province = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true, organizationCode, testData.adapter);

    String gtJSON = province.toJSON().toString();

    testData.adapter.createGeoObjectType(testData.clientSession.getSessionId(), gtJSON);

    checkMdBusinessAttributes(TEST_GOT.getCode());
    checkMdGraphAttributes(TEST_GOT.getCode());
  }

  @Test(expected = SmartExceptionDTO.class)
  public void testCreateGeoObjectTypeAsDifferentOrg()
  {
    String organizationCode = FastTestDataset.ORG_CGOV.getCode();

    GeoObjectType province = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true, organizationCode, testData.adapter);

    String gtJSON = province.toJSON().toString();

    FastTestDataset.runAsUser(FastTestDataset.USER_MOHA_RA, (request, adapter) -> {

      adapter.createGeoObjectType(request.getSessionId(), gtJSON);
    });
  }

  @Test
  public void testUpdateGeoObjectType()
  {
    String organizationCode = FastTestDataset.ORG_CGOV.getCode();

    GeoObjectType province = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), GeometryType.POLYGON, new LocalizedValue("Province Test"), new LocalizedValue("Some Description"), true, organizationCode, testData.adapter);

    String gtJSON = province.toJSON().toString();

    testData.adapter.createGeoObjectType(testData.clientSession.getSessionId(), gtJSON);

    province = testData.adapter.getGeoObjectTypes(new String[] { TEST_GOT.getCode() }, null, PermissionContext.READ)[0];

    province.setLabel(MdAttributeLocalInfo.DEFAULT_LOCALE, "Province Test 2");
    province.setDescription(MdAttributeLocalInfo.DEFAULT_LOCALE, "Some Description 2");

    gtJSON = province.toJSON().toString();
    testData.adapter.updateGeoObjectType(gtJSON);

    province = testData.adapter.getGeoObjectTypes(new String[] { TEST_GOT.getCode() }, null, PermissionContext.READ)[0];

    Assert.assertEquals("Display label was not updated on a GeoObjectType", "Province Test 2", province.getLabel().getValue());
    Assert.assertEquals("Description  was not updated on a GeoObjectType", "Some Description 2", province.getDescription().getValue());
  }

  @Test(expected = SmartExceptionDTO.class)
  public void testUpdateGeoObjectTypeAsDifferentOrg()
  {
    String organizationCode = FastTestDataset.ORG_CGOV.getCode();

    GeoObjectType province = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), GeometryType.POLYGON, new LocalizedValue("Province Test"), new LocalizedValue("Some Description"), true, organizationCode, testData.adapter);

    String gtJSON = province.toJSON().toString();

    testData.adapter.createGeoObjectType(testData.clientSession.getSessionId(), gtJSON);

    province = testData.adapter.getGeoObjectTypes(new String[] { TEST_GOT.getCode() }, null, PermissionContext.READ)[0];

    province.setLabel(MdAttributeLocalInfo.DEFAULT_LOCALE, "Province Test 2");
    province.setDescription(MdAttributeLocalInfo.DEFAULT_LOCALE, "Some Description 2");

    final String updateJSON = province.toJSON().toString();

    FastTestDataset.runAsUser(FastTestDataset.USER_MOHA_RA, (request, adapter) -> {

      adapter.updateGeoObjectType(updateJSON);
    });
  }

  @Test
  public void testGetGeoObjectTypeAsDifferentOrgWithWriteContext()
  {
    String organizationCode = FastTestDataset.ORG_CGOV.getCode();

    GeoObjectType province = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), GeometryType.POLYGON, new LocalizedValue("Province Test"), new LocalizedValue("Some Description"), true, organizationCode, testData.adapter);

    String gtJSON = province.toJSON().toString();

    testData.adapter.createGeoObjectType(testData.clientSession.getSessionId(), gtJSON);

    FastTestDataset.runAsUser(FastTestDataset.USER_MOHA_RA, (request, adapter) -> {
      GeoObjectType[] response = adapter.getGeoObjectTypes(new String[] { TEST_GOT.getCode() }, null, PermissionContext.WRITE);

      Assert.assertEquals(0, response.length);
    });
  }

  @Test
  public void testGetGeoObjectTypeAsDifferentOrgWithReadContext()
  {
    String organizationCode = FastTestDataset.ORG_CGOV.getCode();

    GeoObjectType province = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), GeometryType.POLYGON, new LocalizedValue("Province Test"), new LocalizedValue("Some Description"), true, organizationCode, testData.adapter);

    String gtJSON = province.toJSON().toString();

    testData.adapter.createGeoObjectType(testData.clientSession.getSessionId(), gtJSON);

    FastTestDataset.runAsUser(FastTestDataset.USER_MOHA_RA, (request, adapter) -> {
      GeoObjectType[] response = adapter.getGeoObjectTypes(new String[] { TEST_GOT.getCode() }, null, PermissionContext.READ);

      Assert.assertEquals(1, response.length);
    });
  }

  // Heads up: clean up do we remove these tests?
  // @Test
  // public void testCreateGeoObjectTypePoint()
  // {
  // RegistryAdapterServer registry = new
  // RegistryAdapterServer(RegistryIdService.getInstance());
  //
  // String organizationCode = testData.ORG_CGOV.getCode();
  //
  // GeoObjectType village = MetadataFactory.newGeoObjectType(VILLAGE.getCode(),
  // GeometryType.POINT, new LocalizedValue("Village"), new LocalizedValue(""),
  // true, organizationCode, registry);
  //
  // String villageJSON = village.toJSON().toString();
  //
  // service.createGeoObjectType(testData.adminSession.getSessionId(),
  // villageJSON);
  //
  // checkAttributePoint(VILLAGE.getCode());
  // }
  //
  // @Test
  // public void testCreateGeoObjectTypeLine()
  // {
  // RegistryAdapterServer registry = new
  // RegistryAdapterServer(RegistryIdService.getInstance());
  //
  // String organizationCode = testData.ORG_CGOV.getCode();
  //
  // GeoObjectType river = MetadataFactory.newGeoObjectType(RIVER.getCode(),
  // GeometryType.LINE, new LocalizedValue("River"), new LocalizedValue(""),
  // true, organizationCode, registry);
  //
  // String riverJSON = river.toJSON().toString();
  //
  // service.createGeoObjectType(testData.adminSession.getSessionId(),
  // riverJSON);
  //
  // checkAttributeLine(RIVER.getCode());
  // }
  //
  // @Test
  // public void testCreateGeoObjectTypePolygon()
  // {
  // RegistryAdapterServer registry = new
  // RegistryAdapterServer(RegistryIdService.getInstance());
  //
  // String organizationCode = testData.ORG_CGOV.getCode();
  //
  // GeoObjectType geoObjectType =
  // MetadataFactory.newGeoObjectType(DISTRICT.getCode(), GeometryType.POLYGON,
  // new LocalizedValue("District"), new LocalizedValue(""), true,
  // organizationCode, registry);
  //
  // String gtJSON = geoObjectType.toJSON().toString();
  //
  // service.createGeoObjectType(testData.adminSession.getSessionId(), gtJSON);
  //
  // checkAttributePolygon(DISTRICT.getCode());
  // }
  //
  // @Test
  // public void testCreateGeoObjectTypeMultiPoint()
  // {
  // RegistryAdapterServer registry = new
  // RegistryAdapterServer(RegistryIdService.getInstance());
  //
  // String organizationCode = testData.ORG_CGOV.getCode();
  //
  // GeoObjectType village = MetadataFactory.newGeoObjectType(VILLAGE.getCode(),
  // GeometryType.MULTIPOINT, new LocalizedValue("Village"), new
  // LocalizedValue(""), true, organizationCode, registry);
  //
  // String villageJSON = village.toJSON().toString();
  //
  // service.createGeoObjectType(testData.adminSession.getSessionId(),
  // villageJSON);
  //
  // checkAttributeMultiPoint(VILLAGE.getCode());
  // }
  //
  // @Test
  // public void testCreateGeoObjectTypeMultiLine()
  // {
  // RegistryAdapterServer registry = new
  // RegistryAdapterServer(RegistryIdService.getInstance());
  //
  // String organizationCode = testData.ORG_CGOV.getCode();
  //
  // GeoObjectType river = MetadataFactory.newGeoObjectType(RIVER.getCode(),
  // GeometryType.MULTILINE, new LocalizedValue("River"), new
  // LocalizedValue(""),true, organizationCode, registry);
  //
  // String riverJSON = river.toJSON().toString();
  //
  // service.createGeoObjectType(testData.adminSession.getSessionId(),
  // riverJSON);
  //
  // checkAttributeMultiLine(RIVER.getCode());
  // }
  //
  // @Test
  // public void testCreateGeoObjectTypeMultiPolygon()
  // {
  // RegistryAdapterServer registry = new
  // RegistryAdapterServer(RegistryIdService.getInstance());
  //
  // String organizationCode = testData.ORG_CGOV.getCode();
  //
  // GeoObjectType geoObjectType =
  // MetadataFactory.newGeoObjectType(DISTRICT.getCode(),
  // GeometryType.MULTIPOLYGON, new LocalizedValue("District"), new
  // LocalizedValue(""), true, organizationCode, registry);
  //
  // String gtJSON = geoObjectType.toJSON().toString();
  //
  // service.createGeoObjectType(testData.adminSession.getSessionId(), gtJSON);
  //
  // checkAttributeMultiPolygon(DISTRICT.getCode());
  // }

  /*
   * Utility methods for this test class:
   */

  @Request
  private void checkAttributePoint(String code)
  {
    Universal universal = Universal.getByKey(code);
    MdBusiness mdBusiness = universal.getMdBusiness();

    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF) BusinessFacade.getEntityDAO(mdBusiness);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = mdBusinessDAOIF.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);

    Assert.assertNotNull("A GeoObjectType did not define the proper geometry type attribute: " + RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, mdAttributeConcreteDAOIF);
  }

  @Request
  private void checkAttributeLine(String code)
  {
    Universal universal = Universal.getByKey(code);
    MdBusiness mdBusiness = universal.getMdBusiness();

    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF) BusinessFacade.getEntityDAO(mdBusiness);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = mdBusinessDAOIF.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);

    Assert.assertNotNull("A GeoObjectType did not define the proper geometry type attribute: " + RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, mdAttributeConcreteDAOIF);

  }

  @Request
  private void checkAttributePolygon(String code)
  {
    Universal universal = Universal.getByKey(code);
    MdBusiness mdBusiness = universal.getMdBusiness();

    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF) BusinessFacade.getEntityDAO(mdBusiness);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = mdBusinessDAOIF.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);

    Assert.assertNotNull("A GeoObjectType did not define the proper geometry type attribute: " + RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, mdAttributeConcreteDAOIF);
  }

  @Request
  private void checkAttributeMultiPoint(String code)
  {
    Universal universal = Universal.getByKey(code);
    MdBusiness mdBusiness = universal.getMdBusiness();

    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF) BusinessFacade.getEntityDAO(mdBusiness);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = mdBusinessDAOIF.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);

    Assert.assertNotNull("A GeoObjectType did not define the proper geometry type attribute: " + RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, mdAttributeConcreteDAOIF);
  }

  @Request
  private void checkAttributeMultiLine(String code)
  {
    Universal universal = Universal.getByKey(code);
    MdBusiness mdBusiness = universal.getMdBusiness();

    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF) BusinessFacade.getEntityDAO(mdBusiness);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = mdBusinessDAOIF.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);

    Assert.assertNotNull("A GeoObjectType did not define the proper geometry type attribute: " + RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, mdAttributeConcreteDAOIF);
  }

  @Request
  private void checkAttributeMultiPolygon(String code)
  {
    Universal universal = Universal.getByKey(code);
    MdBusiness mdBusiness = universal.getMdBusiness();

    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF) BusinessFacade.getEntityDAO(mdBusiness);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = mdBusinessDAOIF.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);

    Assert.assertNotNull("A GeoObjectType did not define the proper geometry type attribute: " + RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, mdAttributeConcreteDAOIF);
  }

  @Request
  private void checkMdGraphAttributes(String code)
  {
    MdGeoVertexDAOIF mdGraphClassDAOIF = (MdGeoVertexDAOIF) MdGeoVertexDAO.get(MdGeoVertexInfo.CLASS, GeoVertexType.buildMdGeoVertexKey(code));

    // DefaultAttribute.UID - Defined on the MdBusiness and the values are from
    // the {@code GeoObject#OID};
    try
    {
      mdGraphClassDAOIF.definesAttribute(DefaultAttribute.UID.getName());
    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObject.UID does not exist. It should be defined on the business class");
    }

    // DefaultAttribute.CODE - defined by GeoEntity geoId
    try
    {
      mdGraphClassDAOIF.definesAttribute(DefaultAttribute.CODE.getName());
    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObjectType.CODE does not exist.It should be defined on the business class");
    }

    // DefaultAttribute.CREATED_DATE - The create data on the GeoObject?
    try
    {
      mdGraphClassDAOIF.definesAttribute(MdBusinessInfo.CREATE_DATE);
    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObjectType.CREATED_DATE does not exist.It should be defined on the business class");
    }

    // DefaultAttribute.UPDATED_DATE - The update data on the GeoObject?
    try
    {
      mdGraphClassDAOIF.definesAttribute(MdBusinessInfo.LAST_UPDATE_DATE);
    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObjectType.LAST_UPDATE_DATE does not exist.It should be defined on the business class");
    }

    // DefaultAttribute.STATUS
    try
    {
      mdGraphClassDAOIF.definesAttribute(MdBusinessInfo.LAST_UPDATE_DATE);
    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObjectType.LAST_UPDATE_DATE does not exist.It should be defined on the business class");
    }
  }

  @Request
  private void checkMdBusinessAttributes(String code)
  {
    Universal universal = Universal.getByKey(code);
    MdBusiness mdBusiness = universal.getMdBusiness();

    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF) BusinessFacade.getEntityDAO(mdBusiness);
    // For debugging
    // mdBusinessDAOIF.getAllDefinedMdAttributes().forEach(a ->
    // System.out.println(a.definesAttribute() +" "+a.getType()));

    // DefaultAttribute.UID - Defined on the MdBusiness and the values are from
    // the {@code GeoObject#OID};
    try
    {
      mdBusinessDAOIF.definesAttribute(DefaultAttribute.UID.getName());
    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObject.UID does not exist. It should be defined on the business class");
    }

    // DefaultAttribute.CODE - defined by GeoEntity geoId
    try
    {
      mdBusinessDAOIF.definesAttribute(DefaultAttribute.CODE.getName());
    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObjectType.CODE does not exist.It should be defined on the business class");
    }

    // DefaultAttribute.CREATED_DATE - The create data on the GeoObject?
    try
    {
      mdBusinessDAOIF.definesAttribute(MdBusinessInfo.CREATE_DATE);
    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObjectType.CREATED_DATE does not exist.It should be defined on the business class");
    }

    // DefaultAttribute.UPDATED_DATE - The update data on the GeoObject?
    try
    {
      mdBusinessDAOIF.definesAttribute(MdBusinessInfo.LAST_UPDATE_DATE);
    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObjectType.LAST_UPDATE_DATE does not exist.It should be defined on the business class");
    }

    // DefaultAttribute.STATUS
    try
    {
      mdBusinessDAOIF.definesAttribute(MdBusinessInfo.LAST_UPDATE_DATE);
    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObjectType.LAST_UPDATE_DATE does not exist.It should be defined on the business class");
    }
  }
}
