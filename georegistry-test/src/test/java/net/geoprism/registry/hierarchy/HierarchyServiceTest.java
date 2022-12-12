/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.MetadataFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.RunwayExceptionDTO;
import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.gis.constants.GISConstants;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.LocatedIn;

import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.permission.PermissionContext;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestHierarchyTypeInfo;
import net.geoprism.registry.test.TestRegistryAdapter;
import net.geoprism.registry.test.TestUserInfo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
public class HierarchyServiceTest
{

  public static final TestGeoObjectTypeInfo TEST_GOT = new TestGeoObjectTypeInfo("HMST_Country", FastTestDataset.ORG_CGOV);

  public static final TestHierarchyTypeInfo TEST_HT  = new TestHierarchyTypeInfo("HMST_ReportDiv", FastTestDataset.ORG_CGOV);

  protected static FastTestDataset          testData;

  protected HierarchyService                service  = new HierarchyService();

  @Autowired
  private TestRegistryAdapter               adapter;

  @BeforeClass
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();
    testData.setUpMetadata();
  }

  @AfterClass
  public static void cleanUpClass()
  {
    deleteExtraMetadata();

    testData.tearDownMetadata();
  }

  @Before
  public void setUp()
  {
    testData.setUpInstanceData();

    deleteExtraMetadata();

    testData.logIn(FastTestDataset.USER_CGOV_RA);
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    deleteExtraMetadata();

    testData.tearDownInstanceData();
  }

  private static void deleteExtraMetadata()
  {
    TEST_HT.delete();
    TEST_GOT.delete();
  }

  @Test
  public void testGetHierarchyGroupedTypes()
  {
    for (TestUserInfo user : new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM })
    {
      FastTestDataset.runAsUser(user, (request) -> {
        JsonArray ja = service.getHierarchyGroupedTypes(request.getSessionId());

        ArrayList<String> hierarchyLabels = new ArrayList<String>();
        ArrayList<String> hierarchyCodes = new ArrayList<String>();

        for (int i = 0; i < ja.size(); ++i)
        {
          JsonObject hierarchy = ja.get(i).getAsJsonObject();

          Assert.assertNotNull(hierarchy.get("label").getAsString());
          Assert.assertNotNull(hierarchy.get("code").getAsString());

          hierarchyLabels.add(hierarchy.get("label").getAsString());
          hierarchyCodes.add(hierarchy.get("code").getAsString());
        }

        Assert.assertTrue(hierarchyCodes.contains(FastTestDataset.HIER_ADMIN.getCode()));
        Assert.assertTrue(hierarchyLabels.contains(FastTestDataset.HIER_ADMIN.getDisplayLabel()));

        Assert.assertFalse(hierarchyCodes.contains(FastTestDataset.HIER_HEALTH_ADMIN.getCode()));
        Assert.assertFalse(hierarchyLabels.contains(FastTestDataset.HIER_HEALTH_ADMIN.getDisplayLabel()));
      });
    }

    for (TestUserInfo user : new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM })
    {
      FastTestDataset.runAsUser(user, (request) -> {
        JsonArray ja = service.getHierarchyGroupedTypes(request.getSessionId());

        ArrayList<String> hierarchyLabels = new ArrayList<String>();
        ArrayList<String> hierarchyCodes = new ArrayList<String>();

        for (int i = 0; i < ja.size(); ++i)
        {
          JsonObject hierarchy = ja.get(i).getAsJsonObject();

          Assert.assertNotNull(hierarchy.get("label").getAsString());
          Assert.assertNotNull(hierarchy.get("code").getAsString());

          hierarchyLabels.add(hierarchy.get("label").getAsString());
          hierarchyCodes.add(hierarchy.get("code").getAsString());
        }

        Assert.assertFalse(hierarchyCodes.contains(FastTestDataset.HIER_ADMIN.getCode()));
        Assert.assertFalse(hierarchyLabels.contains(FastTestDataset.HIER_ADMIN.getDisplayLabel()));

        Assert.assertTrue(hierarchyCodes.contains(FastTestDataset.HIER_HEALTH_ADMIN.getCode()));
        Assert.assertTrue(hierarchyLabels.contains(FastTestDataset.HIER_HEALTH_ADMIN.getDisplayLabel()));
      });
    }

    for (TestUserInfo user : new TestUserInfo[] { FastTestDataset.USER_ADMIN })
    {
      FastTestDataset.runAsUser(user, (request) -> {
        JsonArray ja = service.getHierarchyGroupedTypes(request.getSessionId());

        ArrayList<String> hierarchyLabels = new ArrayList<String>();
        ArrayList<String> hierarchyCodes = new ArrayList<String>();

        for (int i = 0; i < ja.size(); ++i)
        {
          JsonObject hierarchy = ja.get(i).getAsJsonObject();

          Assert.assertNotNull(hierarchy.get("label").getAsString());
          Assert.assertNotNull(hierarchy.get("code").getAsString());

          hierarchyLabels.add(hierarchy.get("label").getAsString());
          hierarchyCodes.add(hierarchy.get("code").getAsString());
        }

        Assert.assertTrue(hierarchyCodes.contains(FastTestDataset.HIER_ADMIN.getCode()));
        Assert.assertTrue(hierarchyLabels.contains(FastTestDataset.HIER_ADMIN.getDisplayLabel()));

        Assert.assertTrue(hierarchyCodes.contains(FastTestDataset.HIER_HEALTH_ADMIN.getCode()));
        Assert.assertTrue(hierarchyLabels.contains(FastTestDataset.HIER_HEALTH_ADMIN.getDisplayLabel()));
      });
    }
  }

  @Test
  public void testCreateHierarchyType()
  {
    String organizationCode = FastTestDataset.ORG_CGOV.getCode();

    HierarchyType reportingDivision = MetadataFactory.newHierarchyType(TEST_HT.getCode(), new LocalizedValue("Reporting Division"), new LocalizedValue("The rporting division hieracy..."), organizationCode, adapter);
    reportingDivision.setAbstractDescription("Test Abstract");
    reportingDivision.setAcknowledgement("Test Acknowledgement");
    reportingDivision.setDisclaimer("Test disclaimer");
    reportingDivision.setContact("Test Contact");
    reportingDivision.setPhoneNumber("Test phone number");
    reportingDivision.setEmail("Test email");
    reportingDivision.setProgress("Test Progress");
    reportingDivision.setAccessConstraints("Access Constraints");
    reportingDivision.setUseConstraints("Test Use Constraints");

    String gtJSON = reportingDivision.toJSON().toString();

    ServiceFactory.getHierarchyService().createHierarchyType(testData.clientSession.getSessionId(), gtJSON);

    HierarchyType[] hierarchies = ServiceFactory.getHierarchyService().getHierarchyTypes(testData.clientSession.getSessionId(), new String[] { TEST_HT.getCode() }, PermissionContext.READ);

    Assert.assertNotNull("The created hierarchy was not returned", hierarchies);

    Assert.assertEquals("The wrong number of hierarchies were returned.", 1, hierarchies.length);

    HierarchyType hierarchy = hierarchies[0];

    Assert.assertEquals("Reporting Division", hierarchy.getLabel().getValue());
    Assert.assertEquals("Test Abstract", hierarchy.getAbstractDescription());
    Assert.assertEquals("Test Acknowledgement", hierarchy.getAcknowledgement());
    Assert.assertEquals("Test disclaimer", hierarchy.getDisclaimer());
    Assert.assertEquals("Test Contact", hierarchy.getContact());
    Assert.assertEquals("Test phone number", hierarchy.getPhoneNumber());
    Assert.assertEquals("Test email", hierarchy.getEmail());
    Assert.assertEquals("Test Progress", hierarchy.getProgress());
    Assert.assertEquals("Access Constraints", hierarchy.getAccessConstraints());
    Assert.assertEquals("Test Use Constraints", hierarchy.getUseConstraints());

    // test the types that were created
    String mdTermRelUniversal = ServerHierarchyType.buildMdTermRelUniversalKey(reportingDivision.getCode());
    String expectedMdTermRelUniversal = GISConstants.GEO_PACKAGE + "." + reportingDivision.getCode() + RegistryConstants.UNIVERSAL_RELATIONSHIP_POST;
    Assert.assertEquals("The type name of the MdTermRelationshp defining the universals was not correctly defined for the given code.", expectedMdTermRelUniversal, mdTermRelUniversal);

    String mdTermRelGeoEntity = ServerHierarchyType.buildMdTermRelGeoEntityKey(reportingDivision.getCode());
    String expectedMdTermRelGeoEntity = GISConstants.GEO_PACKAGE + "." + reportingDivision.getCode();
    Assert.assertEquals("The type name of the MdTermRelationshp defining the geoentities was not correctly defined for the given code.", expectedMdTermRelGeoEntity, mdTermRelGeoEntity);
  }

  @Test
  public void testCreateHierarchyTypeWithOrganization()
  {
    HierarchyType reportingDivision = null;

    String organizationCode = FastTestDataset.ORG_CGOV.getCode();

    reportingDivision = MetadataFactory.newHierarchyType(TEST_HT.getCode(), new LocalizedValue("Reporting Division"), new LocalizedValue("The rporting division hieracy..."), organizationCode, adapter);
    String gtJSON = reportingDivision.toJSON().toString();

    ServiceFactory.getHierarchyService().createHierarchyType(testData.clientSession.getSessionId(), gtJSON);

    HierarchyType[] hierarchies = ServiceFactory.getHierarchyService().getHierarchyTypes(testData.clientSession.getSessionId(), new String[] { TEST_HT.getCode() }, PermissionContext.READ);

    Assert.assertNotNull("The created hierarchy was not returned", hierarchies);

    Assert.assertEquals("The wrong number of hierarchies were returned.", 1, hierarchies.length);

    HierarchyType hierarchy = hierarchies[0];

    Assert.assertEquals("Reporting Division", hierarchy.getLabel().getValue());

    Assert.assertEquals(organizationCode, hierarchy.getOrganizationCode());

    // test the types that were created
    String mdTermRelUniversal = ServerHierarchyType.buildMdTermRelUniversalKey(reportingDivision.getCode());
    String expectedMdTermRelUniversal = GISConstants.GEO_PACKAGE + "." + reportingDivision.getCode() + RegistryConstants.UNIVERSAL_RELATIONSHIP_POST;
    Assert.assertEquals("The type name of the MdTermRelationshp defining the universals was not correctly defined for the given code.", expectedMdTermRelUniversal, mdTermRelUniversal);

    String mdTermRelGeoEntity = ServerHierarchyType.buildMdTermRelGeoEntityKey(reportingDivision.getCode());
    String expectedMdTermRelGeoEntity = GISConstants.GEO_PACKAGE + "." + reportingDivision.getCode();
    Assert.assertEquals("The type name of the MdTermRelationshp defining the geoentities was not correctly defined for the given code.", expectedMdTermRelGeoEntity, mdTermRelGeoEntity);
  }

  @Test(expected = SmartExceptionDTO.class)
  public void testCreateHierarchyTypeAsBadRole()
  {
    String organizationCode = FastTestDataset.ORG_CGOV.getCode();

    HierarchyType reportingDivision = MetadataFactory.newHierarchyType(TEST_HT.getCode(), new LocalizedValue("Reporting Division"), new LocalizedValue("The rporting division hieracy..."), organizationCode, adapter);
    String gtJSON = reportingDivision.toJSON().toString();

    TestUserInfo[] users = new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_CGOV_RM };

    for (TestUserInfo user : users)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {

          ServiceFactory.getHierarchyService().createHierarchyType(request.getSessionId(), gtJSON);
        });

        Assert.fail("Able to update a geo object type as a user with bad roles");
      }
      catch (RunwayExceptionDTO e)
      {
        // This is expected
      }
    }

  }

  @Test
  public void testUpdateHierarchyType()
  {
    HierarchyType reportingDivision = FastTestDataset.HIER_ADMIN.toDTO();
    String gtJSON = reportingDivision.toJSON().toString();

    reportingDivision.setLabel(new LocalizedValue("Reporting Division 2"));

    reportingDivision.setDescription(new LocalizedValue("The rporting division hieracy 2"));

    gtJSON = reportingDivision.toJSON().toString();

    reportingDivision = ServiceFactory.getHierarchyService().updateHierarchyType(testData.clientSession.getSessionId(), gtJSON);

    try
    {
      Assert.assertNotNull("The created hierarchy was not returned", reportingDivision);
      Assert.assertEquals("Reporting Division 2", reportingDivision.getLabel().getValue());
      Assert.assertEquals("The rporting division hieracy 2", reportingDivision.getDescription().getValue());
    }
    finally
    {
      reportingDivision.setLabel(new LocalizedValue(FastTestDataset.HIER_ADMIN.getDisplayLabel()));
      reportingDivision.setDescription(new LocalizedValue(FastTestDataset.HIER_ADMIN.getDisplayLabel()));
      gtJSON = reportingDivision.toJSON().toString();
      reportingDivision = ServiceFactory.getHierarchyService().updateHierarchyType(testData.clientSession.getSessionId(), gtJSON);
    }
  }

  @Test
  public void testUpdateHierarchyTypeAsBadRole()
  {
    HierarchyType reportingDivision = FastTestDataset.HIER_ADMIN.toDTO();
    reportingDivision.setLabel(new LocalizedValue("Reporting Division 2"));
    reportingDivision.setDescription(new LocalizedValue("The rporting division hieracy 2"));

    final String updateJSON = reportingDivision.toJSON().toString();

    TestUserInfo[] users = new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_CGOV_RM };

    for (TestUserInfo user : users)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {

          ServiceFactory.getHierarchyService().updateHierarchyType(request.getSessionId(), updateJSON);
        });

        Assert.fail("Able to update a geo object type as a user with bad roles");
      }
      catch (SmartExceptionDTO e)
      {
        // This is expected
      }
    }
  }

  @Test
  public void testDeleteHierarchyTypeAsBadRole()
  {
    String organizationCode = FastTestDataset.ORG_CGOV.getCode();

    HierarchyType reportingDivision = MetadataFactory.newHierarchyType(TEST_HT.getCode(), new LocalizedValue("Reporting Division"), new LocalizedValue("The rporting division hieracy..."), organizationCode, adapter);
    String gtJSON = reportingDivision.toJSON().toString();

    ServiceFactory.getHierarchyService().createHierarchyType(testData.clientSession.getSessionId(), gtJSON);

    TestUserInfo[] users = new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_CGOV_RM };

    for (TestUserInfo user : users)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {

          ServiceFactory.getHierarchyService().deleteHierarchyType(request.getSessionId(), TEST_HT.getCode());
        });

        Assert.fail("Able to update a geo object type as a user with bad roles");
      }
      catch (SmartExceptionDTO e)
      {
        // This is expected
      }
    }
  }

  @Test
  public void testGetHierarchyTypeAsDifferentOrgWithWriteContext()
  {
    FastTestDataset.runAsUser(FastTestDataset.USER_MOHA_RA, (request) -> {
      HierarchyType[] hierarchyTypes = ServiceFactory.getHierarchyService().getHierarchyTypes(request.getSessionId(), null, PermissionContext.WRITE);

      Assert.assertEquals(1, hierarchyTypes.length);
    });
  }

  @Test
  public void testGetHierarchyTypeAsDifferentOrgWithReadContext()
  {
    FastTestDataset.runAsUser(FastTestDataset.USER_MOHA_RA, (request) -> {
      HierarchyType[] hierarchyTypes = ServiceFactory.getHierarchyService().getHierarchyTypes(request.getSessionId(), null, PermissionContext.READ);

      Assert.assertEquals(testData.getManagedHierarchyTypes().size(), hierarchyTypes.length);
    });
  }

  @Test
  public void testHierarchyType()
  {
    HierarchyType[] hierarchyTypes = ServiceFactory.getHierarchyService().getHierarchyTypes(testData.clientSession.getSessionId(), null, PermissionContext.READ);

    for (HierarchyType hierarchyType : hierarchyTypes)
    {
      System.out.println(hierarchyType.toJSON());
    }
  }

  /**
   * The hardcoded {@link AllowedIn} and {@link LocatedIn} relationship do not
   * follow the common geo registry convention.
   */
  @Test
  public void testLocatedInCode_To_MdTermRelUniversal()
  {
    String locatedInClassName = LocatedIn.class.getSimpleName();

    String mdTermRelUniversalType = ServerHierarchyType.buildMdTermRelUniversalKey(locatedInClassName);

    Assert.assertEquals("HierarchyCode LocatedIn did not get converted to the AllowedIn Universal relationshipType.", AllowedIn.CLASS, mdTermRelUniversalType);
  }

  /**
   * The hardcoded {@link AllowedIn} and {@link LocatedIn} relationship do not
   * follow the common geo registry convention.
   */
  @Test
  public void testToMdTermRelUniversal_To_HierarchyCode()
  {
    String allowedInClass = AllowedIn.CLASS;

    String hierarchyCode = ServerHierarchyType.buildHierarchyKeyFromMdTermRelUniversal(allowedInClass);

    Assert.assertEquals("AllowedIn relationship type did not get converted into the LocatedIn  hierarchy code", LocatedIn.class.getSimpleName(), hierarchyCode);
  }

  /**
   * The hardcoded {@link AllowedIn} and {@link LocatedIn} relationship do not
   * follow the common geo registry convention.
   */
  @Test
  public void testLocatedInCode_To_MdTermRelGeoEntity()
  {
    String locatedInClassName = LocatedIn.class.getSimpleName();

    String mdTermRelGeoEntity = ServerHierarchyType.buildMdTermRelGeoEntityKey(locatedInClassName);

    Assert.assertEquals("HierarchyCode LocatedIn did not get converted to the AllowedIn Universal relationshipType.", LocatedIn.CLASS, mdTermRelGeoEntity);
  }

  /**
   * The hardcoded {@link AllowedIn} and {@link LocatedIn} relationship do not
   * follow the common geo registry convention.
   */
  @Test
  public void testToMdTermRelGeoEntity_To_HierarchyCode()
  {
    String locatedInClass = LocatedIn.CLASS;

    String hierarchyCode = ServerHierarchyType.buildHierarchyKeyFromMdTermRelGeoEntity(locatedInClass);

    Assert.assertEquals("AllowedIn relationship type did not get converted into the LocatedIn  hierarchy code", LocatedIn.class.getSimpleName(), hierarchyCode);
  }

}
