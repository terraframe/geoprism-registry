/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.hierarchy;

import java.util.ArrayList;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
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
import com.runwaysdk.constants.ClientRequestIF;
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

import net.geoprism.registry.GeoObjectTypeAssignmentException;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.graph.GeoVertexType;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.permission.PermissionContext;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestRegistryAdapterClient;
import net.geoprism.registry.test.TestUserInfo;

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
  }

  private void setUpExtras()
  {
    cleanUpExtras();
  }

  private void createGot(ClientRequestIF request, TestRegistryAdapterClient adapter)
  {
    GeoObjectType testGot = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), TEST_GOT.getGeometryType(), TEST_GOT.getDisplayLabel(), TEST_GOT.getDescription(), true, TEST_GOT.getOrganization().getCode(), adapter);

    String gtJSON = testGot.toJSON().toString();

    GeoObjectType returned = adapter.createGeoObjectType(gtJSON);

    checkMdBusinessAttributes(TEST_GOT.getCode());
    checkMdGraphAttributes(TEST_GOT.getCode());

    TEST_GOT.assertEquals(returned);
    TEST_GOT.assertApplied();
  }

  @Test
  public void testCreateGeoObjectTypeAsGoodUser()
  {
    TestUserInfo[] users = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA };

    for (TestUserInfo user : users)
    {
      TestDataSet.runAsUser(user, (request, adapter) -> {
        createGot(request, adapter);
      });

      TEST_GOT.delete();
    }
  }

  @Test
  public void testCreateGeoObjectTypeAsBadUser()
  {
    TestUserInfo[] users = new TestUserInfo[] { FastTestDataset.ADMIN_USER, FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };

    for (TestUserInfo user : users)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request, adapter) -> {

          createGot(request, adapter);

          Assert.fail("Able to create a geo object type as a user with bad roles");
        });
      }
      catch (SmartExceptionDTO e)
      {
        // This is expected
      }
    }
  }

  private void updateGot(ClientRequestIF request, TestRegistryAdapterClient adapter)
  {
    GeoObjectType got = TEST_GOT.fetchDTO();

    final String newLabel = "Some Label 2";
    got.setLabel(MdAttributeLocalInfo.DEFAULT_LOCALE, newLabel);

    final String newDesc = "Some Description 2";
    got.setDescription(MdAttributeLocalInfo.DEFAULT_LOCALE, newDesc);

    String gtJSON = got.toJSON().toString();
    GeoObjectType returned = adapter.updateGeoObjectType(gtJSON);

    Assert.assertEquals(newLabel, returned.getLabel().getValue());
    Assert.assertEquals(newDesc, returned.getDescription().getValue());
  }

  @Test
  public void testUpdateGeoObjectTypeAsGoodUser()
  {
    TEST_GOT.apply();

    TestUserInfo[] users = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA };

    for (TestUserInfo user : users)
    {
      TestDataSet.runAsUser(user, (request, adapter) -> {
        updateGot(request, adapter);

        TEST_GOT.delete();
        TEST_GOT.apply();
      });
    }
  }

  @Test
  public void testUpdateGeoObjectTypeAsBadUser()
  {
    TEST_GOT.apply();

    TestUserInfo[] users = new TestUserInfo[] { FastTestDataset.ADMIN_USER, FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };

    for (TestUserInfo user : users)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request, adapter) -> {
          updateGot(request, adapter);

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
  public void testDeleteGeoObjectTypeAsBadUser()
  {
    TEST_GOT.apply();

    TestUserInfo[] users = new TestUserInfo[] { FastTestDataset.ADMIN_USER, FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };

    for (TestUserInfo user : users)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request, adapter) -> {

          ServiceFactory.getRegistryService().deleteGeoObjectType(request.getSessionId(), TEST_GOT.getCode());

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

    FastTestDataset.runAsUser(FastTestDataset.USER_MOHA_RA, (request, adapter) -> {
      GeoObjectType[] response = adapter.getGeoObjectTypes(new String[] { TEST_GOT.getCode() }, null, PermissionContext.WRITE);

      Assert.assertEquals(0, response.length);
    });
  }

  @Test
  public void testGetGeoObjectTypeAsDifferentOrgWithReadContext()
  {
    TEST_GOT.apply();

    FastTestDataset.runAsUser(FastTestDataset.USER_MOHA_RA, (request, adapter) -> {
      GeoObjectType[] response = adapter.getGeoObjectTypes(new String[] { TEST_GOT.getCode() }, null, PermissionContext.READ);

      Assert.assertEquals(1, response.length);
    });
  }

  @Test
  public void testGetGeoObjectTypes()
  {
    FastTestDataset.runAsUser(FastTestDataset.USER_CGOV_RA, (request, adapter) -> {
      String[] codes = new String[] { FastTestDataset.COUNTRY.getCode(), FastTestDataset.PROVINCE.getCode() };

      GeoObjectType[] gots = adapter.getGeoObjectTypes(codes, null, PermissionContext.READ);

      Assert.assertEquals(codes.length, gots.length);

      GeoObjectType state = gots[0];
      Assert.assertEquals(state.toJSON().toString(), GeoObjectType.fromJSON(state.toJSON().toString(), adapter).toJSON().toString());
      FastTestDataset.COUNTRY.assertEquals(state);

      GeoObjectType district = gots[1];
      Assert.assertEquals(district.toJSON().toString(), GeoObjectType.fromJSON(district.toJSON().toString(), adapter).toJSON().toString());
      FastTestDataset.PROVINCE.assertEquals(district);

      // Test to make sure we can provide none
      GeoObjectType[] gots2 = adapter.getGeoObjectTypes(new String[] {}, null, PermissionContext.READ);
      Assert.assertTrue(gots2.length > 0);

      GeoObjectType[] gots3 = adapter.getGeoObjectTypes(null, null, PermissionContext.READ);
      Assert.assertTrue(gots3.length > 0);
    });
  }

  @Test
  public void testListGeoObjectTypes()
  {
    FastTestDataset.runAsUser(FastTestDataset.USER_CGOV_RA, (request, adapter) -> {
      JsonArray types = adapter.listGeoObjectTypes();

      ArrayList<TestGeoObjectTypeInfo> expectedGots = testData.getManagedGeoObjectTypes();
      for (TestGeoObjectTypeInfo got : expectedGots)
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
