package net.geoprism.registry;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import net.geoprism.registry.conversion.RegistryRoleConverter;
import net.geoprism.registry.conversion.ServerGeoObjectTypeConverter;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.RegistryIdService;
import net.geoprism.registry.service.RegistryService;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.test.TestDataSet;

import org.commongeoregistry.adapter.RegistryAdapterServer;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.MetadataFactory;
import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.ClientSession;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.Roles;

public class OrganizationAndRoleTest
{
//  public static RegistryAdapter          adapter                      = null;

  public static RegistryService          service                      = null;
  
  public static ClientSession            adminSession                 = null;
  
  public static final String             DISTRICT                     = "District";
  
  public static final String             VILLAGE                      = "Village";
  
  public static final String             MOI_ORG_CODE                 = "MOI";
 

  @BeforeClass
  public static void setUpClass()
  {
    adminSession = ClientSession.createUserSession(TestDataSet.ADMIN_USER_NAME, TestDataSet.ADMIN_PASSWORD, new Locale[] { CommonProperties.getDefaultLocale() });
    
    service = RegistryService.getInstance();
  }

  @AfterClass
  public static void cleanUpClass()
  {
    if (adminSession != null)
    {
      adminSession.logout();
    }
  }
  
  @Before
  public void setUp()
  {
    deleteGeoObjectType(VILLAGE);
    deleteOrganization(MOI_ORG_CODE);
  }
  
  @After
  public void tearDown() throws IOException
  { 
    deleteGeoObjectType(VILLAGE);
    deleteOrganization(MOI_ORG_CODE);
  }

  
  @Test
  public void testRoleNames()
  {    
    Assert.assertEquals(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE+"."+MOI_ORG_CODE, RegistryRole.Type.getRootOrgRoleName(MOI_ORG_CODE));
    Assert.assertEquals(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE+"."+MOI_ORG_CODE+".RA", RegistryRole.Type.getRA_RoleName(MOI_ORG_CODE));
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
      Assert.assertEquals(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE+"."+MOI_ORG_CODE, orgRole.getRoleName());
      
      // Make sure the organization code can be obtained from the corresponding role id
      Organization serverRootOrganization = Organization.getRootOrganization(orgRole.getOid());
      Assert.assertTrue(serverRootOrganization != null);
      Assert.assertEquals(MOI_ORG_CODE, serverRootOrganization.getCode());
      
      
      Roles raOrgRole = organization.getRegistryAdminiRole();
      Assert.assertEquals(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE+"."+MOI_ORG_CODE+".RA", raOrgRole.getRoleName());
      
      // Make sure NULL is returned, as the role is not the root role for the organization.
      Organization serverRaOrg = Organization.getRootOrganization(raOrgRole.getOid());
      Assert.assertTrue(serverRaOrg == null);
    }
    finally
    {
      if (organization != null)
      {
        organization.delete();
      }
    }
  }

  @Test
  public void testOrgRoleNameParsing()
  {
    Assert.assertTrue("Valid root org role name was not parsed correctly", RegistryRole.Type.isRootOrgRole(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE));
    Assert.assertTrue("Invalid root org role name was parsed as valid", !RegistryRole.Type.isRootOrgRole(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE+"."+"SomethingElse"));    
  }
  
  
  @Test
  public void testSRARoleNameParsing()
  {
    Assert.assertTrue("Valid SRA role name was not parsed correctly", RegistryRole.Type.isSRA_Role(RegistryRole.Type.getSRA_RoleName()));
    Assert.assertTrue("Invalid SRA role name was parsed as valid", !RegistryRole.Type.isSRA_Role(RegistryRole.Type.getSRA_RoleName()+"."+"SomethingElse"));    
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
    String organizationCode = MOI_ORG_CODE;
    String raRole = RegistryRole.Type.getRA_RoleName(organizationCode);
    
    Assert.assertTrue("Valid RA role name was not parsed correctly", RegistryRole.Type.isRA_Role(raRole));
    
    String invalidRole = RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE+"."+organizationCode+".Village."+RegistryRole.Type.RA.name();
    Assert.assertFalse("Invalid RA role name was parsed as valid", RegistryRole.Type.isRA_Role(invalidRole));
    
    Assert.assertTrue("Valid RA role name was not parsed correctly", RegistryRole.Type.isRA_Role(raRole));
  }
  
  @Test
  @Request
  public void testRA_RoleToRegistryRole()
  {
    String organizationCode = MOI_ORG_CODE; 
    
    createOrganization(organizationCode);
    
    try
    {
      String raRoleName = RegistryRole.Type.getRA_RoleName(organizationCode);
      
      Roles raRole = Roles.findRoleByName(raRoleName);

      RegistryRole registryRole = new RegistryRoleConverter().build(raRole);
    
      Assert.assertEquals(raRoleName, registryRole.getName());
      Assert.assertEquals(organizationCode, registryRole.getOrganizationCode());
    }
    finally
    {
      deleteOrganization(organizationCode);
    }
  }
  
  @Test
  public void testRMRoleNameParsing()
  {
    String organizationCode = MOI_ORG_CODE;
    String geoObjectTypeCode = "Village";

    String rmRole = RegistryRole.Type.getRM_RoleName(organizationCode, geoObjectTypeCode);
    
    Assert.assertTrue("Valid RM role name was not parsed correctly", RegistryRole.Type.isRM_Role(rmRole));
    
    String invalidRole = RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE+"."+RegistryRole.Type.RM.name();
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
    String organizationCode = MOI_ORG_CODE; 
    String geoObjectTypeCode = VILLAGE;
    
    createOrganization(organizationCode);
    createGeoObjectType(organizationCode, geoObjectTypeCode);
    
    try
    {
      String rmRoleName = RegistryRole.Type.getRM_RoleName(organizationCode, geoObjectTypeCode);
      Roles rmRole = Roles.findRoleByName(rmRoleName);
 
      RegistryRole registryRole = new RegistryRoleConverter().build(rmRole);
    
      Assert.assertEquals(rmRoleName, registryRole.getName());
      Assert.assertEquals(organizationCode, registryRole.getOrganizationCode());
      Assert.assertEquals(geoObjectTypeCode, registryRole.getGeoObjectTypeCode());
    }
    finally
    {      
      deleteGeoObjectType(geoObjectTypeCode);
      deleteOrganization(organizationCode);
    }
  }

  @Test
  public void testRCRoleNameParsing()
  {
    String organizationCode = MOI_ORG_CODE;
    String geoObjectType = "Village";

    String rmRole = RegistryRole.Type.getRC_RoleName(organizationCode, geoObjectType);
    
    Assert.assertTrue("Valid RC role name was not parsed correctly", RegistryRole.Type.isRC_Role(rmRole));
    
    String invalidRole = RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE+"."+RegistryRole.Type.RC.name();
    Assert.assertFalse("Invalid RC role name was parsed as valid", RegistryRole.Type.isRC_Role(invalidRole));
  }


  @Test
  public void testACRoleNameParsing()
  {
    String organizationCode = MOI_ORG_CODE;
    String geoObjectType = "Village";

    String rmRole = RegistryRole.Type.getAC_RoleName(organizationCode, geoObjectType);
    
    Assert.assertTrue("Valid AC role name was not parsed correctly", RegistryRole.Type.isAC_Role(rmRole));
    
    String invalidRole = RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE+"."+RegistryRole.Type.AC.name();
    Assert.assertFalse("Invalid RC role name was parsed as valid", RegistryRole.Type.isAC_Role(invalidRole));
  }
  
  @Test
  @Request
  public void testGetGeoObjectTypesMethod()
  {
    String organizationCode = MOI_ORG_CODE; 
    String villageCode = VILLAGE;
    String districtCode = DISTRICT;
    
    Organization organization = createOrganization(organizationCode);
    
    createGeoObjectType(organizationCode, villageCode);
    createGeoObjectType(organizationCode, districtCode);
    
    try
    {
      Map<String, LocalizedValue> geoObjectTypeInfo = organization.getGeoObjectTypes();
      
      Assert.assertEquals("Method did not return the correct number of GeoObjectTypes managed by the organization", 2, geoObjectTypeInfo.size());
      
      Assert.assertEquals(true, geoObjectTypeInfo.containsKey(districtCode));
      Assert.assertEquals(true, geoObjectTypeInfo.containsKey(villageCode));
    }
    finally
    {      
      deleteGeoObjectType(villageCode);
      deleteGeoObjectType(districtCode);
      deleteOrganization(organizationCode);
    }
  }
  
  
  /**
   * Precondition: Needs to be called within {@link Request} and {@link Transaction} annotations.
   * 
   * @param organizationCode
   * 
   * @return created and persisted {@link Organization} object.
   */
  @Request
  public static Organization createOrganization(String organizationCode)
  {
    Organization organization = new Organization();
    organization.setCode(organizationCode);
    organization.getDisplayLabel().setDefaultValue(organizationCode);
    organization.getContactInfo().setDefaultValue("Contact Fred at 555...");
    organization.apply();
    
    return organization;
  }
  
  /**
   * Precondition: Needs to be called within {@link Request} and {@link Transaction} annotations.
   * 
   * @param organizationCode
   * 
   */
  @Request
  public static void deleteOrganization(String organizationCode)
  {
    Organization organization = null;
    
    try
    {
      organization = Organization.getByKey(organizationCode);
      organization.delete();
    }
    catch (com.runwaysdk.dataaccess.cache.DataNotFoundException e) {}
  }
  
  @Request
  public static void createGeoObjectType(String organizationCode, String geoObjectTypeCode)
  {    
    RegistryAdapterServer registry = new RegistryAdapterServer(RegistryIdService.getInstance());
    GeoObjectType province = MetadataFactory.newGeoObjectType(geoObjectTypeCode, GeometryType.POLYGON, new LocalizedValue(geoObjectTypeCode+" DisplayLabel"), new LocalizedValue(""), true, organizationCode, registry);

    ServerGeoObjectType serverGeoObjectType = new ServerGeoObjectTypeConverter().create(province.toJSON().toString());
    
    ServiceFactory.getAdapter().getMetadataCache().addGeoObjectType(serverGeoObjectType.getType());
  }
  
  @Request
  public static void deleteGeoObjectType(String geoObjectTypeCode)
  {
    try
    {
      ServerGeoObjectType type = ServerGeoObjectType.get(geoObjectTypeCode);
    
      if (type != null)
      {
        type.delete();
      }
    }
    catch (com.runwaysdk.dataaccess.cache.DataNotFoundException e) {}
  }
}
