/**
 *
 */
package net.geoprism.registry.hierarchy;

import java.util.ArrayList;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.MetadataFactory;
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
import com.runwaysdk.RunwayExceptionDTO;
import com.runwaysdk.business.SmartExceptionDTO;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.permission.PermissionContext;
import net.geoprism.registry.service.request.GPRHierarchyTypeService;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestHierarchyTypeInfo;
import net.geoprism.registry.test.TestRegistryAdapter;
import net.geoprism.registry.test.TestUserInfo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class HierarchyServiceTest extends FastDatasetTest implements InstanceTestClassListener
{

  public static final TestGeoObjectTypeInfo TEST_GOT = new TestGeoObjectTypeInfo("HMST_Country", FastTestDataset.ORG_CGOV);

  public static final TestHierarchyTypeInfo TEST_HT  = new TestHierarchyTypeInfo("HMST_ReportDiv", FastTestDataset.ORG_CGOV);

  @Autowired
  protected GPRHierarchyTypeService         service;

  @Autowired
  private TestRegistryAdapter               adapter;

  @Override
  public void afterClassSetup() throws Exception
  {
    deleteExtraMetadata();

    super.afterClassSetup();
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

  private void deleteExtraMetadata()
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

    this.service.createHierarchyType(testData.clientSession.getSessionId(), gtJSON);

    HierarchyType[] hierarchies = this.service.getHierarchyTypes(testData.clientSession.getSessionId(), new String[] { TEST_HT.getCode() }, PermissionContext.READ);

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
  }

  @Test
  public void testCreateHierarchyTypeWithOrganization()
  {
    HierarchyType reportingDivision = null;

    String organizationCode = FastTestDataset.ORG_CGOV.getCode();

    reportingDivision = MetadataFactory.newHierarchyType(TEST_HT.getCode(), new LocalizedValue("Reporting Division"), new LocalizedValue("The rporting division hieracy..."), organizationCode, adapter);
    String gtJSON = reportingDivision.toJSON().toString();

    this.service.createHierarchyType(testData.clientSession.getSessionId(), gtJSON);

    HierarchyType[] hierarchies = this.service.getHierarchyTypes(testData.clientSession.getSessionId(), new String[] { TEST_HT.getCode() }, PermissionContext.READ);

    Assert.assertNotNull("The created hierarchy was not returned", hierarchies);

    Assert.assertEquals("The wrong number of hierarchies were returned.", 1, hierarchies.length);

    HierarchyType hierarchy = hierarchies[0];

    Assert.assertEquals("Reporting Division", hierarchy.getLabel().getValue());

    Assert.assertEquals(organizationCode, hierarchy.getOrganizationCode());
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

          this.service.createHierarchyType(request.getSessionId(), gtJSON);
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
    HierarchyType dto = FastTestDataset.HIER_ADMIN.toDTO();
    String gtJSON = dto.toJSON().toString();

    dto.setLabel(new LocalizedValue("Reporting Division 2"));

    dto.setDescription(new LocalizedValue("The rporting division hieracy 2"));

    gtJSON = dto.toJSON().toString();

    dto = this.service.updateHierarchyType(testData.clientSession.getSessionId(), gtJSON);

    try
    {
      Assert.assertNotNull("The created hierarchy was not returned", dto);
      Assert.assertEquals("Reporting Division 2", dto.getLabel().getValue());
      Assert.assertEquals("The rporting division hieracy 2", dto.getDescription().getValue());
    }
    finally
    {
      dto.setLabel(new LocalizedValue(FastTestDataset.HIER_ADMIN.getDisplayLabel()));
      dto.setDescription(new LocalizedValue(FastTestDataset.HIER_ADMIN.getDisplayLabel()));
      gtJSON = dto.toJSON().toString();
      dto = this.service.updateHierarchyType(testData.clientSession.getSessionId(), gtJSON);
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

          this.service.updateHierarchyType(request.getSessionId(), updateJSON);
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

    this.service.createHierarchyType(testData.clientSession.getSessionId(), gtJSON);

    TestUserInfo[] users = new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_CGOV_RM };

    for (TestUserInfo user : users)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {

          this.service.deleteHierarchyType(request.getSessionId(), TEST_HT.getCode());
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
      HierarchyType[] hierarchyTypes = this.service.getHierarchyTypes(request.getSessionId(), null, PermissionContext.WRITE);

      Assert.assertEquals(1, hierarchyTypes.length);
    });
  }

  @Test
  public void testGetHierarchyTypeAsDifferentOrgWithReadContext()
  {
    FastTestDataset.runAsUser(FastTestDataset.USER_MOHA_RA, (request) -> {
      HierarchyType[] hierarchyTypes = this.service.getHierarchyTypes(request.getSessionId(), null, PermissionContext.READ);

      Assert.assertEquals(testData.getManagedHierarchyTypes().size(), hierarchyTypes.length);
    });
  }

  @Test
  public void testHierarchyType()
  {
    HierarchyType[] hierarchyTypes = this.service.getHierarchyTypes(testData.clientSession.getSessionId(), null, PermissionContext.READ);

    for (HierarchyType hierarchyType : hierarchyTypes)
    {
      System.out.println(hierarchyType.toJSON());
    }
  }
}
