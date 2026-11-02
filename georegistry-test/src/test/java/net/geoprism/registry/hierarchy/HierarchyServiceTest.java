/**
 *
 */
package net.geoprism.registry.hierarchy;

import java.util.ArrayList;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.MetadataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.RunwayExceptionDTO;
import com.runwaysdk.business.SmartExceptionDTO;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.permission.PermissionContext;
import net.geoprism.registry.service.request.GPRHierarchyTypeService;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestHierarchyTypeInfo;
import net.geoprism.registry.test.TestRegistryAdapter;
import net.geoprism.registry.test.TestUserInfo;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
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

  @BeforeEach
  public void setUp() throws Exception
  {
    super.setUp();

    deleteExtraMetadata();
  }

  @AfterEach
  public void tearDown() throws Exception
  {
    deleteExtraMetadata();

    super.tearDown();
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
        JsonObject view = service.getHierarchyGroupedTypes(request.getSessionId());
        JsonArray ja = view.get("hierarchies").getAsJsonArray();

        ArrayList<String> hierarchyLabels = new ArrayList<String>();
        ArrayList<String> hierarchyCodes = new ArrayList<String>();

        for (int i = 0; i < ja.size(); ++i)
        {
          JsonObject hierarchy = ja.get(i).getAsJsonObject();

          Assertions.assertNotNull(hierarchy.get("label").getAsString());
          Assertions.assertNotNull(hierarchy.get("code").getAsString());

          hierarchyLabels.add(hierarchy.get("label").getAsString());
          hierarchyCodes.add(hierarchy.get("code").getAsString());
        }

        Assertions.assertTrue(hierarchyCodes.contains(FastTestDataset.HIER_ADMIN.getCode()));
        Assertions.assertTrue(hierarchyLabels.contains(FastTestDataset.HIER_ADMIN.getDisplayLabel()));

        Assertions.assertFalse(hierarchyCodes.contains(FastTestDataset.HIER_HEALTH_ADMIN.getCode()));
        Assertions.assertFalse(hierarchyLabels.contains(FastTestDataset.HIER_HEALTH_ADMIN.getDisplayLabel()));
      });
    }

    for (TestUserInfo user : new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM })
    {
      FastTestDataset.runAsUser(user, (request) -> {
        JsonObject view = service.getHierarchyGroupedTypes(request.getSessionId());
        JsonArray ja = view.get("hierarchies").getAsJsonArray();

        ArrayList<String> hierarchyLabels = new ArrayList<String>();
        ArrayList<String> hierarchyCodes = new ArrayList<String>();

        for (int i = 0; i < ja.size(); ++i)
        {
          JsonObject hierarchy = ja.get(i).getAsJsonObject();

          Assertions.assertNotNull(hierarchy.get("label").getAsString());
          Assertions.assertNotNull(hierarchy.get("code").getAsString());

          hierarchyLabels.add(hierarchy.get("label").getAsString());
          hierarchyCodes.add(hierarchy.get("code").getAsString());
        }

        Assertions.assertFalse(hierarchyCodes.contains(FastTestDataset.HIER_ADMIN.getCode()));
        Assertions.assertFalse(hierarchyLabels.contains(FastTestDataset.HIER_ADMIN.getDisplayLabel()));

        Assertions.assertTrue(hierarchyCodes.contains(FastTestDataset.HIER_HEALTH_ADMIN.getCode()));
        Assertions.assertTrue(hierarchyLabels.contains(FastTestDataset.HIER_HEALTH_ADMIN.getDisplayLabel()));
      });
    }

    for (TestUserInfo user : new TestUserInfo[] { FastTestDataset.USER_ADMIN })
    {
      FastTestDataset.runAsUser(user, (request) -> {
        JsonObject view = service.getHierarchyGroupedTypes(request.getSessionId());
        JsonArray ja = view.get("hierarchies").getAsJsonArray();

        ArrayList<String> hierarchyLabels = new ArrayList<String>();
        ArrayList<String> hierarchyCodes = new ArrayList<String>();

        for (int i = 0; i < ja.size(); ++i)
        {
          JsonObject hierarchy = ja.get(i).getAsJsonObject();

          Assertions.assertNotNull(hierarchy.get("label").getAsString());
          Assertions.assertNotNull(hierarchy.get("code").getAsString());

          hierarchyLabels.add(hierarchy.get("label").getAsString());
          hierarchyCodes.add(hierarchy.get("code").getAsString());
        }

        Assertions.assertTrue(hierarchyCodes.contains(FastTestDataset.HIER_ADMIN.getCode()));
        Assertions.assertTrue(hierarchyLabels.contains(FastTestDataset.HIER_ADMIN.getDisplayLabel()));

        Assertions.assertTrue(hierarchyCodes.contains(FastTestDataset.HIER_HEALTH_ADMIN.getCode()));
        Assertions.assertTrue(hierarchyLabels.contains(FastTestDataset.HIER_HEALTH_ADMIN.getDisplayLabel()));
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

    Assertions.assertNotNull(hierarchies, "The created hierarchy was not returned");

    Assertions.assertEquals(1, hierarchies.length, "The wrong number of hierarchies were returned.");

    HierarchyType hierarchy = hierarchies[0];

    Assertions.assertEquals("Reporting Division", hierarchy.getLabel().getValue());
    Assertions.assertEquals("Test Abstract", hierarchy.getAbstractDescription());
    Assertions.assertEquals("Test Acknowledgement", hierarchy.getAcknowledgement());
    Assertions.assertEquals("Test disclaimer", hierarchy.getDisclaimer());
    Assertions.assertEquals("Test Contact", hierarchy.getContact());
    Assertions.assertEquals("Test phone number", hierarchy.getPhoneNumber());
    Assertions.assertEquals("Test email", hierarchy.getEmail());
    Assertions.assertEquals("Test Progress", hierarchy.getProgress());
    Assertions.assertEquals("Access Constraints", hierarchy.getAccessConstraints());
    Assertions.assertEquals("Test Use Constraints", hierarchy.getUseConstraints());
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

    Assertions.assertNotNull(hierarchies, "The created hierarchy was not returned");

    Assertions.assertEquals(1, hierarchies.length, "The wrong number of hierarchies were returned.");

    HierarchyType hierarchy = hierarchies[0];

    Assertions.assertEquals("Reporting Division", hierarchy.getLabel().getValue());

    Assertions.assertEquals(organizationCode, hierarchy.getOrganizationCode());
  }

  @Test
  public void testCreateHierarchyTypeAsBadRole()
  {
    assertThrows(SmartExceptionDTO.class, () -> {
      String organizationCode = FastTestDataset.ORG_CGOV.getCode();

      HierarchyType reportingDivision = MetadataFactory.newHierarchyType(TEST_HT.getCode(), new LocalizedValue("Reporting Division"), new LocalizedValue("The rporting division hieracy..."), organizationCode, adapter);
      String gtJSON = reportingDivision.toJSON().toString();

      TestUserInfo[] users = new TestUserInfo[]{FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_CGOV_RM};

      for (TestUserInfo user : users)
      {
        try
        {
          FastTestDataset.runAsUser(user, (request) ->

            this.service.createHierarchyType(request.getSessionId(), gtJSON));

          Assertions.fail("Able to update a geo object type as a user with bad roles");
        }
        catch (RunwayExceptionDTO e)
        {
          // This is expected
        }
      }

    });

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
      Assertions.assertNotNull(dto, "The created hierarchy was not returned");
      Assertions.assertEquals("Reporting Division 2", dto.getLabel().getValue());
      Assertions.assertEquals("The rporting division hieracy 2", dto.getDescription().getValue());
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
        FastTestDataset.runAsUser(user, (request) ->

          this.service.updateHierarchyType(request.getSessionId(), updateJSON));

        Assertions.fail("Able to update a geo object type as a user with bad roles");
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
        FastTestDataset.runAsUser(user, (request) ->

          this.service.deleteHierarchyType(request.getSessionId(), TEST_HT.getCode()));

        Assertions.fail("Able to update a geo object type as a user with bad roles");
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

      Assertions.assertEquals(1, hierarchyTypes.length);
    });
  }

  @Test
  public void testGetHierarchyTypeAsDifferentOrgWithReadContext()
  {
    FastTestDataset.runAsUser(FastTestDataset.USER_MOHA_RA, (request) -> {
      HierarchyType[] hierarchyTypes = this.service.getHierarchyTypes(request.getSessionId(), null, PermissionContext.READ);

      Assertions.assertTrue(testData.getManagedHierarchyTypes().size() <= hierarchyTypes.length);
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
