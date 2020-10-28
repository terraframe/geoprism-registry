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
package net.geoprism.registry;

import java.util.Map;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.session.Request;
import com.runwaysdk.system.Roles;

import net.geoprism.registry.conversion.RegistryRoleConverter;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.test.FastTestDataset;

public class OrganizationAndRoleTest
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
  public void testRoleNames()
  {
    Assert.assertEquals(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE + "." + testData.ORG_CGOV.getCode(), RegistryRole.Type.getRootOrgRoleName(testData.ORG_CGOV.getCode()));
    Assert.assertEquals(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE + "." + testData.ORG_CGOV.getCode() + ".RA", RegistryRole.Type.getRA_RoleName(testData.ORG_CGOV.getCode()));
  }

  @Test
  @Request
  public void testRoles()
  {
    Organization organization = testData.ORG_CGOV.getServerObject();

    Roles orgRole = organization.getRole();
    Assert.assertEquals(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE + "." + testData.ORG_CGOV.getCode(), orgRole.getRoleName());

    // Make sure the organization code can be obtained from the corresponding
    // role id
    Organization serverRootOrganization = Organization.getRootOrganization(orgRole.getOid());
    Assert.assertTrue(serverRootOrganization != null);
    Assert.assertEquals(testData.ORG_CGOV.getCode(), serverRootOrganization.getCode());

    Roles raOrgRole = organization.getRegistryAdminiRole();
    Assert.assertEquals(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE + "." + testData.ORG_CGOV.getCode() + ".RA", raOrgRole.getRoleName());

    // Make sure NULL is returned, as the role is not the root role for the
    // organization.
    Organization serverRaOrg = Organization.getRootOrganization(raOrgRole.getOid());
    Assert.assertTrue(serverRaOrg == null);
  }

  @Test
  public void testOrgRoleNameParsing()
  {
    Assert.assertTrue("Valid root org role name was not parsed correctly", RegistryRole.Type.isRootOrgRole(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE));
    Assert.assertTrue("Invalid root org role name was parsed as valid", !RegistryRole.Type.isRootOrgRole(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE + "." + "SomethingElse"));
  }

  @Test
  public void testSRARoleNameParsing()
  {
    Assert.assertTrue("Valid SRA role name was not parsed correctly", RegistryRole.Type.isSRA_Role(RegistryRole.Type.getSRA_RoleName()));
    Assert.assertTrue("Invalid SRA role name was parsed as valid", !RegistryRole.Type.isSRA_Role(RegistryRole.Type.getSRA_RoleName() + "." + "SomethingElse"));
  }

  @Test
  @Request
  public void testSRA_RoleToRegistryRole()
  {
    String sraRoleName = RegistryRole.Type.getSRA_RoleName();

    Roles sraRole = Roles.findRoleByName(sraRoleName);

    RegistryRole registryRole = new RegistryRoleConverter().build(sraRole);

    Assert.assertEquals(sraRoleName, registryRole.getName());
  }

  @Test
  public void testRARoleNameParsing()
  {
    String organizationCode = testData.ORG_CGOV.getCode();
    String raRole = RegistryRole.Type.getRA_RoleName(organizationCode);

    Assert.assertTrue("Valid RA role name was not parsed correctly", RegistryRole.Type.isRA_Role(raRole));

    String invalidRole = RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE + "." + organizationCode + ".testData.PROVINCE." + RegistryRole.Type.RA.name();
    Assert.assertFalse("Invalid RA role name was parsed as valid", RegistryRole.Type.isRA_Role(invalidRole));

    Assert.assertTrue("Valid RA role name was not parsed correctly", RegistryRole.Type.isRA_Role(raRole));
  }

  @Test
  @Request
  public void testRA_RoleToRegistryRole()
  {
    String organizationCode = testData.ORG_CGOV.getCode();

    String raRoleName = RegistryRole.Type.getRA_RoleName(organizationCode);

    Roles raRole = Roles.findRoleByName(raRoleName);

    RegistryRole registryRole = new RegistryRoleConverter().build(raRole);

    Assert.assertEquals(raRoleName, registryRole.getName());
    Assert.assertEquals(organizationCode, registryRole.getOrganizationCode());
  }

  @Test
  public void testRMRoleNameParsing()
  {
    String organizationCode = testData.ORG_CGOV.getCode();
    String geoObjectType = testData.PROVINCE.getCode();

    String rmRole = RegistryRole.Type.getRM_RoleName(organizationCode, geoObjectType);

    Assert.assertTrue("Valid RM role name was not parsed correctly", RegistryRole.Type.isRM_Role(rmRole));

    String invalidRole = RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE + "." + RegistryRole.Type.RM.name();
    Assert.assertFalse("Invalid RM role name was parsed as valid", RegistryRole.Type.isRM_Role(invalidRole));
  }

  @Test
  public void testRM_RoleToRegistryRole()
  {
    this.rm_RoleToRegistryRoleRequest();
  }

  @Request
  public void rm_RoleToRegistryRoleRequest()
  {
    String rmRoleName = RegistryRole.Type.getRM_RoleName(testData.ORG_CGOV.getCode(), testData.PROVINCE.getCode());
    Roles rmRole = Roles.findRoleByName(rmRoleName);

    RegistryRole registryRole = new RegistryRoleConverter().build(rmRole);

    Assert.assertEquals(rmRoleName, registryRole.getName());
    Assert.assertEquals(testData.ORG_CGOV.getCode(), registryRole.getOrganizationCode());
    Assert.assertEquals(testData.PROVINCE.getCode(), registryRole.getGeoObjectTypeCode());
  }

  @Test
  public void testRCRoleNameParsing()
  {
    String organizationCode = testData.ORG_CGOV.getCode();
    String geoObjectType = testData.PROVINCE.getCode();

    String rmRole = RegistryRole.Type.getRC_RoleName(organizationCode, geoObjectType);

    Assert.assertTrue("Valid RC role name was not parsed correctly", RegistryRole.Type.isRC_Role(rmRole));

    String invalidRole = RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE + "." + RegistryRole.Type.RC.name();
    Assert.assertFalse("Invalid RC role name was parsed as valid", RegistryRole.Type.isRC_Role(invalidRole));
  }

  @Test
  public void testACRoleNameParsing()
  {
    String organizationCode = testData.ORG_CGOV.getCode();
    String geoObjectType = testData.PROVINCE.getCode();

    String rmRole = RegistryRole.Type.getAC_RoleName(organizationCode, geoObjectType);

    Assert.assertTrue("Valid AC role name was not parsed correctly", RegistryRole.Type.isAC_Role(rmRole));

    String invalidRole = RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE + "." + RegistryRole.Type.AC.name();
    Assert.assertFalse("Invalid RC role name was parsed as valid", RegistryRole.Type.isAC_Role(invalidRole));
  }

  @Test
  @Request
  public void testGetGeoObjectTypesMethod()
  {
    Map<String, ServerGeoObjectType> geoObjectTypeInfo = testData.ORG_CGOV.getServerObject().getGeoObjectTypes();

    Assert.assertEquals("Method did not return the correct number of GeoObjectTypes managed by the organization", 2, geoObjectTypeInfo.size());

    Assert.assertEquals(true, geoObjectTypeInfo.containsKey(testData.COUNTRY.getCode()));
    Assert.assertEquals(true, geoObjectTypeInfo.containsKey(testData.PROVINCE.getCode()));
  }
}
