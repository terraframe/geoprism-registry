package net.geoprism.registry;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.Roles;
import com.vividsolutions.jts.util.Assert;

public class OrganizationTest
{
  public static final String MOI_ORG_CODE  =    "MOI";
 

  @Before
  @Request
  public void setUp()
  {
    tearDownTransaction();
  }
  
  @After
  @Request
  public void tearDown() throws IOException
  {
    tearDownTransaction();  
  }
  
  @Transaction
  public void tearDownTransaction()
  {
    this.deleteOrganization(MOI_ORG_CODE);
  }
  
  @Test
  public void testRoleNames()
  {    
    Assert.equals(RegistryConstants.REGISTRY_ROOT_ORG_ROLE+"."+MOI_ORG_CODE, Organization.getRoleName(MOI_ORG_CODE));
    Assert.equals(RegistryConstants.REGISTRY_ROOT_ORG_ROLE+"."+MOI_ORG_CODE+".RA", Organization.getRegistryAdminRoleName(MOI_ORG_CODE));
  }
  
  @Test
  @Request
  public void testRoles()
  {    
    Organization organization = null;

    try
    {
      organization = createOrganization(MOI_ORG_CODE);
      
      Roles orgRole = organization.getRole();
      Assert.equals(RegistryConstants.REGISTRY_ROOT_ORG_ROLE+"."+MOI_ORG_CODE, orgRole.getRoleName());
      
      // Make sure the organization code can be obtained from the corresponding role id
      Organization serverRootOrganization = Organization.getRootOrganization(orgRole.getOid());
      Assert.isTrue(serverRootOrganization != null);
      Assert.equals(MOI_ORG_CODE, serverRootOrganization.getCode());
      
      
      Roles raOrgRole = organization.getRegistryAdminiRole();
      Assert.equals(RegistryConstants.REGISTRY_ROOT_ORG_ROLE+"."+MOI_ORG_CODE+".RA", raOrgRole.getRoleName());
      
      // Make sure NULL is returned, as the role is not the root role for the organization.
      Organization serverRaOrg = Organization.getRootOrganization(raOrgRole.getOid());
      Assert.isTrue(serverRaOrg == null);
    }
    finally
    {
      if (organization != null)
      {
        organization.delete();
      }
    }
  }
 

  private Organization createOrganization(String organizationCode)
  {
    Organization organization = new Organization();
    organization.setCode(organizationCode);
    organization.getDisplayLabel().setDefaultValue(organizationCode);
    organization.getContactInfo().setDefaultValue("Contact Fred at 555...");
    organization.apply();
    
    return organization;
  }
  
  private void deleteOrganization(String organizationCode)
  {
    Organization organization = null;
    
    try
    {
      organization = Organization.getByKey(organizationCode);
      organization.delete();
    }
    catch (com.runwaysdk.dataaccess.cache.DataNotFoundException e)
    {
      
    }
  }
}
