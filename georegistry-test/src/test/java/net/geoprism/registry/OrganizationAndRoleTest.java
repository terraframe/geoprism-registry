/**
 *
 */
package net.geoprism.registry;

import java.util.Map;

import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.session.Request;
import com.runwaysdk.system.Roles;

import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.conversion.RegistryRoleConverter;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.business.GPROrganizationBusinessService;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
public class OrganizationAndRoleTest extends FastDatasetTest implements InstanceTestClassListener
{
  @Autowired
  private GPROrganizationBusinessService service;

  @Test
  public void testRoleNames()
  {
    Assertions.assertEquals(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE + "." + FastTestDataset.ORG_CGOV.getCode(), RegistryRole.Type.getRootOrgRoleName(FastTestDataset.ORG_CGOV.getCode()));
    Assertions.assertEquals(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE + "." + FastTestDataset.ORG_CGOV.getCode() + ".RA", RegistryRole.Type.getRA_RoleName(FastTestDataset.ORG_CGOV.getCode()));
  }

  @Test
  @Request
  public void testRoles()
  {
    ServerOrganization organization = FastTestDataset.ORG_CGOV.getServerObject();

    Roles orgRole = organization.getRole();
    Assertions.assertEquals(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE + "." + FastTestDataset.ORG_CGOV.getCode(), orgRole.getRoleName());

    // Make sure the organization code can be obtained from the corresponding
    // role id
    Organization serverRootOrganization = Organization.getRootOrganization(orgRole.getOid());
    Assertions.assertTrue(serverRootOrganization != null);
    Assertions.assertEquals(FastTestDataset.ORG_CGOV.getCode(), serverRootOrganization.getCode());

    Roles raOrgRole = organization.getRegistryAdminiRole();
    Assertions.assertEquals(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE + "." + FastTestDataset.ORG_CGOV.getCode() + ".RA", raOrgRole.getRoleName());

    // Make sure NULL is returned, as the role is not the root role for the
    // organization.
    Organization serverRaOrg = Organization.getRootOrganization(raOrgRole.getOid());
    Assertions.assertTrue(serverRaOrg == null);
  }

  @Test
  public void testOrgRoleNameParsing()
  {
    Assertions.assertTrue(RegistryRole.Type.isRootOrgRole(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE), "Valid root org role name was not parsed correctly");
    Assertions.assertTrue(!RegistryRole.Type.isRootOrgRole(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE + "." + "SomethingElse"), "Invalid root org role name was parsed as valid");
  }

  @Test
  public void testSRARoleNameParsing()
  {
    Assertions.assertTrue(RegistryRole.Type.isSRA_Role(RegistryRole.Type.getSRA_RoleName()), "Valid SRA role name was not parsed correctly");
    Assertions.assertTrue(!RegistryRole.Type.isSRA_Role(RegistryRole.Type.getSRA_RoleName() + "." + "SomethingElse"), "Invalid SRA role name was parsed as valid");
  }

  @Test
  @Request
  public void testSRA_RoleToRegistryRole()
  {
    String sraRoleName = RegistryRole.Type.getSRA_RoleName();

    Roles sraRole = Roles.findRoleByName(sraRoleName);

    RegistryRole registryRole = new RegistryRoleConverter().build(sraRole);

    Assertions.assertEquals(sraRoleName, registryRole.getName());
  }

  @Test
  public void testRARoleNameParsing()
  {
    String organizationCode = FastTestDataset.ORG_CGOV.getCode();
    String raRole = RegistryRole.Type.getRA_RoleName(organizationCode);

    Assertions.assertTrue(RegistryRole.Type.isRA_Role(raRole), "Valid RA role name was not parsed correctly");

    String invalidRole = RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE + "." + organizationCode + ".FastTestDataset.PROVINCE." + RegistryRole.Type.RA.name();
    Assertions.assertFalse(RegistryRole.Type.isRA_Role(invalidRole), "Invalid RA role name was parsed as valid");

    Assertions.assertTrue(RegistryRole.Type.isRA_Role(raRole), "Valid RA role name was not parsed correctly");
  }

  @Test
  @Request
  public void testRA_RoleToRegistryRole()
  {
    String organizationCode = FastTestDataset.ORG_CGOV.getCode();

    String raRoleName = RegistryRole.Type.getRA_RoleName(organizationCode);

    Roles raRole = Roles.findRoleByName(raRoleName);

    RegistryRole registryRole = new RegistryRoleConverter().build(raRole);

    Assertions.assertEquals(raRoleName, registryRole.getName());
    Assertions.assertEquals(organizationCode, registryRole.getOrganizationCode());
  }

  @Test
  public void testRMRoleNameParsing()
  {
    String organizationCode = FastTestDataset.ORG_CGOV.getCode();
    String geoObjectType = FastTestDataset.PROVINCE.getCode();

    String rmRole = RegistryRole.Type.getRM_RoleName(organizationCode, geoObjectType);

    Assertions.assertTrue(RegistryRole.Type.isRM_Role(rmRole), "Valid RM role name was not parsed correctly");

    String invalidRole = RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE + "." + RegistryRole.Type.RM.name();
    Assertions.assertFalse(RegistryRole.Type.isRM_Role(invalidRole), "Invalid RM role name was parsed as valid");
  }

  @Test
  public void testRM_RoleToRegistryRole()
  {
    this.rm_RoleToRegistryRoleRequest();
  }

  @Request
  public void rm_RoleToRegistryRoleRequest()
  {
    String rmRoleName = RegistryRole.Type.getRM_RoleName(FastTestDataset.ORG_CGOV.getCode(), FastTestDataset.PROVINCE.getCode());
    Roles rmRole = Roles.findRoleByName(rmRoleName);

    RegistryRole registryRole = new RegistryRoleConverter().build(rmRole);

    Assertions.assertEquals(rmRoleName, registryRole.getName());
    Assertions.assertEquals(FastTestDataset.ORG_CGOV.getCode(), registryRole.getOrganizationCode());
    Assertions.assertEquals(FastTestDataset.PROVINCE.getCode(), registryRole.getGeoObjectTypeCode());
  }

  @Test
  public void testRCRoleNameParsing()
  {
    String organizationCode = FastTestDataset.ORG_CGOV.getCode();
    String geoObjectType = FastTestDataset.PROVINCE.getCode();

    String rmRole = RegistryRole.Type.getRC_RoleName(organizationCode, geoObjectType);

    Assertions.assertTrue(RegistryRole.Type.isRC_Role(rmRole), "Valid RC role name was not parsed correctly");

    String invalidRole = RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE + "." + RegistryRole.Type.RC.name();
    Assertions.assertFalse(RegistryRole.Type.isRC_Role(invalidRole), "Invalid RC role name was parsed as valid");
  }

  @Test
  public void testACRoleNameParsing()
  {
    String organizationCode = FastTestDataset.ORG_CGOV.getCode();
    String geoObjectType = FastTestDataset.PROVINCE.getCode();

    String rmRole = RegistryRole.Type.getAC_RoleName(organizationCode, geoObjectType);

    Assertions.assertTrue(RegistryRole.Type.isAC_Role(rmRole), "Valid AC role name was not parsed correctly");

    String invalidRole = RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE + "." + RegistryRole.Type.AC.name();
    Assertions.assertFalse(RegistryRole.Type.isAC_Role(invalidRole), "Invalid RC role name was parsed as valid");
  }

  @Test
  @Request
  public void testGetGeoObjectTypesMethod()
  {
    int numGots = 0;

    for (TestGeoObjectTypeInfo testGot : testData.getManagedGeoObjectTypes())
    {
      if (testGot.getOrganization().getCode().equals(FastTestDataset.ORG_CGOV.getCode()))
      {
        numGots++;
      }
    }

    Map<String, ServerGeoObjectType> geoObjectTypeInfo = service.getGeoObjectTypes(FastTestDataset.ORG_CGOV.getServerObject());

    Assertions.assertEquals(numGots, geoObjectTypeInfo.size(), "Method did not return the correct number of GeoObjectTypes managed by the organization");

    Assertions.assertEquals(true, geoObjectTypeInfo.containsKey(FastTestDataset.COUNTRY.getCode()));
    Assertions.assertEquals(true, geoObjectTypeInfo.containsKey(FastTestDataset.PROVINCE.getCode()));
  }
}
