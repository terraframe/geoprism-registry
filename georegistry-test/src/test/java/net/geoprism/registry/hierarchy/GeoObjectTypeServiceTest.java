/**
 *
 */
package net.geoprism.registry.hierarchy;

import java.util.ArrayList;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.MetadataFactory;
import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.MdBusinessInfo;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdBusiness;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.GeoObjectTypeAssignmentException;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.permission.PermissionContext;
import net.geoprism.registry.roles.CreateGeoObjectTypePermissionException;
import net.geoprism.registry.roles.WriteGeoObjectTypePermissionException;
import net.geoprism.registry.service.request.GeoObjectTypeServiceIF;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestRegistryClient;
import net.geoprism.registry.test.TestUserInfo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class GeoObjectTypeServiceTest extends FastDatasetTest implements InstanceTestClassListener
{
  public static TestGeoObjectTypeInfo TEST_GOT            = new TestGeoObjectTypeInfo("GOTTest_TEST1", FastTestDataset.ORG_CGOV);

  public static TestGeoObjectTypeInfo TEST_PRIVATE_GOT    = new TestGeoObjectTypeInfo("GOTTest_TEST1", GeometryType.MULTIPOLYGON, true, FastTestDataset.ORG_CGOV, null);

  public static final TestUserInfo    USER_PRIVATE_GOT_RM = new TestUserInfo(FastTestDataset.TEST_DATA_KEY + "_" + "gottestrmprivate", "gottestrmprivate", FastTestDataset.TEST_DATA_KEY + "gottestrmprivate@noreply.com", new String[] { RegistryRole.Type.getRM_RoleName(FastTestDataset.ORG_CGOV.getCode(), TEST_PRIVATE_GOT.getCode()) });

  public static final TestUserInfo    USER_PRIVATE_GOT_RC = new TestUserInfo(FastTestDataset.TEST_DATA_KEY + "_" + "gottestrcprivate", "gottestrcprivate", FastTestDataset.TEST_DATA_KEY + "gottestrcprivate@noreply.com", new String[] { RegistryRole.Type.getRC_RoleName(FastTestDataset.ORG_CGOV.getCode(), TEST_PRIVATE_GOT.getCode()) });

  public static final TestUserInfo    USER_PRIVATE_GOT_AC = new TestUserInfo(FastTestDataset.TEST_DATA_KEY + "_" + "gottestacprivate", "gottestacprivate", FastTestDataset.TEST_DATA_KEY + "gottestacprivate@noreply.com", new String[] { RegistryRole.Type.getAC_RoleName(FastTestDataset.ORG_CGOV.getCode(), TEST_PRIVATE_GOT.getCode()) });

  @Autowired
  private TestRegistryClient          client;

  @Autowired
  private GeoObjectTypeServiceIF      typeService;

  private static void createPrivateTestGot()
  {
    TEST_PRIVATE_GOT.apply();

    USER_PRIVATE_GOT_RM.apply();
    USER_PRIVATE_GOT_RC.apply();
    USER_PRIVATE_GOT_AC.apply();
  }

  private static void cleanupPrivateTestGot()
  {
    TEST_PRIVATE_GOT.delete();

    USER_PRIVATE_GOT_RM.delete();
    USER_PRIVATE_GOT_RC.delete();
    USER_PRIVATE_GOT_AC.delete();
  }

  @Before
  public void setUp()
  {
    testData.setUpInstanceData();

    setUpExtras();

    // testData.logIn(FastTestDataset.USER_CGOV_RA);
  }

  @After
  public void tearDown()
  {
    // testData.logOut();

    cleanUpExtras();

    testData.tearDownInstanceData();
  }

  private void cleanUpExtras()
  {
    TestDataSet.deleteClassifier("termValue1");
    TestDataSet.deleteClassifier("termValue2");

    TEST_GOT.delete();

    cleanupPrivateTestGot();
  }

  private void setUpExtras()
  {
    cleanUpExtras();
  }

  private void createGot()
  {
    GeoObjectType testGot = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), TEST_GOT.getGeometryType(), TEST_GOT.getDisplayLabel(), TEST_GOT.getDescription(), true, TEST_GOT.getOrganization().getCode(), client.getAdapter());

    String gtJSON = testGot.toJSON().toString();

    GeoObjectType returned = client.createGeoObjectType(gtJSON);

    checkMdGraphAttributes(TEST_GOT.getCode());

    TEST_GOT.assertEquals(returned);
    TEST_GOT.assertApplied();
  }

  @Test
  public void testCreateGeoObjectType()
  {
    // Allowed users
    for (TestUserInfo user : new TestUserInfo[] { FastTestDataset.USER_ADMIN, FastTestDataset.USER_CGOV_RA })
    {
      TestDataSet.runAsUser(user, (request) -> {
        createGot();
      });

      TEST_GOT.delete();
    }

    // Disallowed users
    for (TestUserInfo user : new TestUserInfo[] { FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC })
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {

          createGot();

          Assert.fail("Able to create a geo object type as a user with bad roles");
        });
      }
      catch (SmartExceptionDTO e)
      {
        Assert.assertEquals(CreateGeoObjectTypePermissionException.CLASS, e.getType());
      }
    }
  }

  private void updateGot(TestGeoObjectTypeInfo testGot)
  {
    GeoObjectType got = testGot.fetchDTO();

    final String newLabel = "Some Label 2";
    got.setLabel(MdAttributeLocalInfo.DEFAULT_LOCALE, newLabel);

    final String newDesc = "Some Description 2";
    got.setDescription(MdAttributeLocalInfo.DEFAULT_LOCALE, newDesc);

    String gtJSON = got.toJSON().toString();
    GeoObjectType returned = client.updateGeoObjectType(gtJSON);

    Assert.assertEquals(newLabel, returned.getLabel().getValue());
    Assert.assertEquals(newDesc, returned.getDescription().getValue());
  }

  @Test
  public void testUpdateGeoObjectTypeAsGoodUser()
  {
    TEST_GOT.apply();

    TestUserInfo[] users = new TestUserInfo[] { FastTestDataset.USER_ADMIN, FastTestDataset.USER_CGOV_RA };

    for (TestUserInfo user : users)
    {
      TestDataSet.runAsUser(user, (request) -> {
        updateGot(TEST_GOT);

        TEST_GOT.delete();
        TEST_GOT.apply();
      });
    }
  }

  @Test
  public void testUpdateGeoObjectTypeAsBadUser()
  {
    TEST_GOT.apply();

    TestUserInfo[] users = new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };

    for (TestUserInfo user : users)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {
          updateGot(TEST_GOT);

          Assert.fail("Able to update a geo object type as a user with bad roles");
        });
      }
      catch (SmartExceptionDTO e)
      {
        // This is expected
      }
    }
  }

  @Test
  public void testUpdatePrivateGeoObjectType()
  {
    createPrivateTestGot();

    // Allowed users
    for (TestUserInfo user : new TestUserInfo[] { FastTestDataset.USER_CGOV_RA })
    {
      FastTestDataset.runAsUser(user, (request) -> {
        updateGot(TEST_PRIVATE_GOT);

        cleanupPrivateTestGot();
        createPrivateTestGot();
      });
    }

    // Disallowed users
    for (TestUserInfo user : new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, USER_PRIVATE_GOT_RM, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC, FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC, USER_PRIVATE_GOT_RC, USER_PRIVATE_GOT_AC })
    {
      FastTestDataset.runAsUser(user, (request) -> {
        try
        {
          updateGot(TEST_PRIVATE_GOT);
        }
        catch (SmartExceptionDTO e)
        {
          Assert.assertEquals(WriteGeoObjectTypePermissionException.CLASS, e.getType());
        }
      });
    }
  }

  @Test
  public void testDeleteGeoObjectTypeAsBadUser()
  {
    TEST_GOT.apply();

    TestUserInfo[] users = new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };

    for (TestUserInfo user : users)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {

          this.typeService.deleteGeoObjectType(request.getSessionId(), TEST_GOT.getCode());

          Assert.fail("Able to delete a geo object type as a user with bad roles");
        });
      }
      catch (SmartExceptionDTO e)
      {
        // This is expected
      }
    }
  }

  @Test
  public void testGetGeoObjectTypeAsDifferentOrgWithWriteContext()
  {
    TEST_GOT.apply();

    FastTestDataset.runAsUser(FastTestDataset.USER_MOHA_RA, (request) -> {
      GeoObjectType[] response = client.getGeoObjectTypes(new String[] { TEST_GOT.getCode() }, PermissionContext.WRITE);

      Assert.assertEquals(0, response.length);
    });
  }

  @Test
  public void testGetGeoObjectTypeAsDifferentOrgWithReadContext()
  {
    TEST_GOT.apply();

    FastTestDataset.runAsUser(FastTestDataset.USER_MOHA_RA, (request) -> {
      GeoObjectType[] response = client.getGeoObjectTypes(new String[] { TEST_GOT.getCode() }, PermissionContext.READ);

      Assert.assertEquals(1, response.length);
    });
  }

  @Test
  public void testGetPrivateGeoObjectType()
  {
    createPrivateTestGot();

    // Allowed users with read context
    for (TestUserInfo user : new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, USER_PRIVATE_GOT_RM, FastTestDataset.USER_CGOV_RM, USER_PRIVATE_GOT_RC, USER_PRIVATE_GOT_AC, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC })
    {
      FastTestDataset.runAsUser(user, (request) -> {
        GeoObjectType[] response = client.getGeoObjectTypes(new String[] { TEST_PRIVATE_GOT.getCode() }, PermissionContext.READ);

        Assert.assertEquals(1, response.length);
      });
    }

    // Disallowed users with read context
    for (TestUserInfo user : new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC })
    {
      FastTestDataset.runAsUser(user, (request) -> {
        GeoObjectType[] response = client.getGeoObjectTypes(new String[] { TEST_PRIVATE_GOT.getCode() }, PermissionContext.READ);

        Assert.assertEquals("User: " + user.getUsername(), 0, response.length);
      });
    }

    // Allowed users with write context
    for (TestUserInfo user : new TestUserInfo[] { FastTestDataset.USER_CGOV_RA })
    {
      FastTestDataset.runAsUser(user, (request) -> {
        GeoObjectType[] response = client.getGeoObjectTypes(new String[] { TEST_PRIVATE_GOT.getCode() }, PermissionContext.WRITE);

        Assert.assertEquals(1, response.length);
      });
    }

    // Disallowed users with write context
    for (TestUserInfo user : new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, USER_PRIVATE_GOT_RM, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC, FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC, USER_PRIVATE_GOT_RC, USER_PRIVATE_GOT_AC })
    {
      FastTestDataset.runAsUser(user, (request) -> {
        GeoObjectType[] response = client.getGeoObjectTypes(new String[] { TEST_PRIVATE_GOT.getCode() }, PermissionContext.WRITE);

        Assert.assertEquals(0, response.length);
      });
    }
  }

  @Test
  public void testGetGeoObjectTypes()
  {
    FastTestDataset.runAsUser(FastTestDataset.USER_CGOV_RA, (request) -> {
      String[] codes = new String[] { FastTestDataset.COUNTRY.getCode(), FastTestDataset.PROVINCE.getCode() };

      GeoObjectType[] gots = client.getGeoObjectTypes(codes, PermissionContext.READ);

      Assert.assertEquals(codes.length, gots.length);

      GeoObjectType state = gots[0];
      Assert.assertEquals(state.toJSON().toString(), GeoObjectType.fromJSON(state.toJSON().toString(), client.getAdapter()).toJSON().toString());
      FastTestDataset.COUNTRY.assertEquals(state);

      GeoObjectType district = gots[1];
      Assert.assertEquals(district.toJSON().toString(), GeoObjectType.fromJSON(district.toJSON().toString(), client.getAdapter()).toJSON().toString());
      FastTestDataset.PROVINCE.assertEquals(district);

      // Test to make sure we can provide none
      GeoObjectType[] gots2 = client.getGeoObjectTypes(new String[] {}, PermissionContext.READ);
      Assert.assertTrue(gots2.length > 0);

      GeoObjectType[] gots3 = client.getGeoObjectTypes(null, PermissionContext.READ);
      Assert.assertTrue(gots3.length > 0);
    });
  }

  @Test
  public void testListGeoObjectTypes()
  {
    FastTestDataset.runAsUser(FastTestDataset.USER_CGOV_RA, (request) -> {
      JsonArray types = client.listGeoObjectTypes();

      ArrayList<TestGeoObjectTypeInfo> expectedGots = testData.getManagedGeoObjectTypes();
      for (TestGeoObjectTypeInfo got : expectedGots)
      {
        if (got.getOrganization().getCode().equals(FastTestDataset.ORG_CGOV.getCode()))
        {
          boolean found = false;

          for (int i = 0; i < types.size(); ++i)
          {
            JsonObject jo = types.get(i).getAsJsonObject();

            if (jo.get("label").getAsString().equals(got.getDisplayLabel().getValue()) && jo.get("code").getAsString().equals(got.getCode()))
            {
              found = true;
            }
          }

          Assert.assertTrue(found);
        }
      }
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

  @Test
  @Request
  public void testExtendAbstractType()
  {
    TestGeoObjectTypeInfo parentGot = new TestGeoObjectTypeInfo("HMST_Abstract", FastTestDataset.ORG_CGOV);
    parentGot.setAbstract(true);

    TestGeoObjectTypeInfo childGot = new TestGeoObjectTypeInfo("HMST_Child", FastTestDataset.ORG_CGOV);
    childGot.setSuperType(parentGot);

    try
    {
      parentGot.apply();
      childGot.apply();

      ServerGeoObjectType childType = childGot.getServerObject();

      ServerGeoObjectType superType = childType.getSuperType();

      Assert.assertNotNull(superType);
      Assert.assertEquals(parentGot.getCode(), superType.getCode());
    }
    finally
    {
      childGot.delete();
      parentGot.delete();
    }
  }

  @Test(expected = GeoObjectTypeAssignmentException.class)
  @Request
  public void testExtendNonAbstractType()
  {
    TestGeoObjectTypeInfo parentGot = new TestGeoObjectTypeInfo("HMST_Abstract", FastTestDataset.ORG_CGOV);

    TestGeoObjectTypeInfo childGot = new TestGeoObjectTypeInfo("HMST_Child", FastTestDataset.ORG_CGOV);
    childGot.setSuperType(parentGot);

    try
    {
      parentGot.apply();
      childGot.apply();
    }
    finally
    {
      childGot.delete();
      parentGot.delete();
    }
  }

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
    ServerGeoObjectType type = ServerGeoObjectType.get(code);
    
    Assert.assertTrue(type.getAttribute(DefaultAttribute.UID.getName()).isPresent());

    // DefaultAttribute.CODE - defined by GeoEntity geoId
    Assert.assertTrue(type.getAttribute(DefaultAttribute.CODE.getName()).isPresent());
    
    MdVertexDAOIF mdGraphClassDAOIF = type.getMdVertex();

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
  }
}
