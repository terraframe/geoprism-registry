/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.service;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import net.geoprism.GeoprismUserDTO;
import net.geoprism.registry.OrganizationAndRoleTest;
import net.geoprism.registry.controller.RegistryAccountController;
import net.geoprism.registry.test.TestDataSet;

import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.ClientSession;
import com.runwaysdk.Pair;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;
import com.runwaysdk.session.Request;

public class AccountServiceControllerTest
{
  public static RegistryAccountController           controller;
  
  public static ClientSession                       systemSession                   = null;

  public static ClientRequestIF                     clientRequest                   = null;
  
  public static String                              TEST_USER_NAME                  = "testRegistryUser";
  
  public static String                              TEST_USER_PASSWORD              = "abc123";
  
  public static String                              ORG_MOH                         = "MOH";
  
  public static String                              ORG_MOI                         = "MOI";
  
  public static String                              HEALTH_FACILITY                 = "HealthFacility";
  
  public static String                              DISTRICT                        = "District";
  
  public static String                              VILLAGE                         = "Village";

  
  @BeforeClass
  public static void classSetUp()
  {
    controller = new RegistryAccountController();
    
    systemSession = ClientSession.createUserSession("default", TestDataSet.ADMIN_USER_NAME, TestDataSet.ADMIN_PASSWORD, new Locale[] { CommonProperties.getDefaultLocale() });
    clientRequest = systemSession.getRequest();
    
    classTearDownRequest();
    classSetUpRequest();
  }

  @Request
  public static void classSetUpRequest()
  {
    classSetUpTransaction();
  }
  
  @Transaction
  public static void classSetUpTransaction()
  {
    OrganizationAndRoleTest.createOrganization(ORG_MOI);
    OrganizationAndRoleTest.createGeoObjectType(ORG_MOI, DISTRICT);
    OrganizationAndRoleTest.createGeoObjectType(ORG_MOI, VILLAGE);
    
    OrganizationAndRoleTest.createOrganization(ORG_MOH);
    OrganizationAndRoleTest.createGeoObjectType(ORG_MOH, HEALTH_FACILITY);
  }
  
  @AfterClass
  public static void classTearDown()
  {
    systemSession.logout();
    
    classTearDownRequest();
  }
  
  @Request
  public static void classTearDownRequest()
  {
    classTearDownTransaction();
  }

  @Transaction
  public static void classTearDownTransaction()
  {
    OrganizationAndRoleTest.deleteGeoObjectType(DISTRICT);
    OrganizationAndRoleTest.deleteGeoObjectType(VILLAGE);
    OrganizationAndRoleTest.deleteOrganization(ORG_MOI);
    
    OrganizationAndRoleTest.deleteGeoObjectType(HEALTH_FACILITY);
    OrganizationAndRoleTest.deleteOrganization(ORG_MOH);
  }
  
  /** 
   * Test returning possible roles that can be assigned to a person for a given organization.
   */
  @Test
  public void queryUsers()
  {
    String rmDistrictRole = RegistryRole.Type.getRM_RoleName(ORG_MOI, DISTRICT);
    String rmVillageRole = RegistryRole.Type.getRM_RoleName(ORG_MOI, VILLAGE);
    
    String rcDistrictRole = RegistryRole.Type.getRC_RoleName(ORG_MOI, DISTRICT);
    String rcVillageRole = RegistryRole.Type.getRC_RoleName(ORG_MOI, VILLAGE);
    
    GeoprismUserDTO johny = createUser("johny@gmail.com", rmDistrictRole+","+rmVillageRole);
    GeoprismUserDTO sally = createUser("sallyy@gmail.com", rcDistrictRole+","+rcVillageRole);
    
    String rmHealthFacilityRole = RegistryRole.Type.getRM_RoleName(ORG_MOH, HEALTH_FACILITY);
    String rcHealthFacilityRole = RegistryRole.Type.getRC_RoleName(ORG_MOH, HEALTH_FACILITY);
    
    GeoprismUserDTO franky = createUser("franky@gmail.com", rmHealthFacilityRole);
    GeoprismUserDTO becky = createUser("beckyy@gmail.com", rcHealthFacilityRole);
    
    try
    {
      JSONArray orgCodeArray = new JSONArray();
      orgCodeArray.put(ORG_MOI);
//      orgCodeArray.put(ORG_MOH);
//      orgCodeArray.put("TEST1");
//      orgCodeArray.put("TEST2");
    
      RestBodyResponse response = (RestBodyResponse)controller.page(clientRequest, orgCodeArray.toString(), 1);
    }
    finally
    {
      controller.remove(clientRequest, johny.getOid());
      controller.remove(clientRequest, sally.getOid());
      
      controller.remove(clientRequest, franky.getOid());
      controller.remove(clientRequest, becky.getOid());
    }
  }
  
  private GeoprismUserDTO createUser(String userName, String roleNames)
  {
    RestResponse response = (RestResponse)controller.newInstance(clientRequest, ORG_MOI);
    Pair userPair = (Pair)response.getAttribute("user");
    GeoprismUserDTO user = (GeoprismUserDTO)userPair.getFirst();

    Pair rolesPair = (Pair)response.getAttribute("roles");
    JSONArray roleJSONArray = (JSONArray)rolesPair.getFirst();

    user.setFirstName("Some Firstname");
    user.setLastName("Some Lastame");
    user.setUsername(userName);
    user.setEmail(userName);
    user.setPassword("123456");
    
    response = (RestResponse)controller.apply(clientRequest, user, roleNames);
    userPair = (Pair)response.getAttribute("user");
    user = (GeoprismUserDTO)userPair.getFirst();
    
    return user;
  }
  
  /** 
   * Test returning possible roles that can be assigned to a person for a given organization.
   */
  @Test
  @SuppressWarnings("rawtypes")
  public void createAndApplyUserWithOrgRoles()
  {
    // New Instance
    RestResponse response = (RestResponse)controller.newInstance(clientRequest, ORG_MOI);
    Pair userPair = (Pair)response.getAttribute("user");
    GeoprismUserDTO user = (GeoprismUserDTO)userPair.getFirst();

    Pair rolesPair = (Pair)response.getAttribute("roles");
    JSONArray roleJSONArray = (JSONArray)rolesPair.getFirst();

    user.setFirstName("John");
    user.setLastName("Doe");
    user.setUsername("jdoe6");
    user.setEmail("john6@doe.com");
    user.setPassword("123456");
    
    String rmDistrictRole = RegistryRole.Type.getRM_RoleName(ORG_MOI, DISTRICT);
    String rmVillageRole = RegistryRole.Type.getRM_RoleName(ORG_MOI, VILLAGE);
    
    // Apply
    response = (RestResponse)controller.apply(clientRequest, user, rmDistrictRole+","+rmVillageRole);
    userPair = (Pair)response.getAttribute("user");
    user = (GeoprismUserDTO)userPair.getFirst();

    try
    {
      Pair rolePair = (Pair)response.getAttribute("roles");
      roleJSONArray = (JSONArray)rolePair.getFirst();
      Assert.assertEquals(7, roleJSONArray.length());
  
      Set<String> rolesFoundSet = new HashSet<String>();
      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(ORG_MOI, DISTRICT));
      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(ORG_MOI, VILLAGE));
      this.iterateOverReturnedRoles(roleJSONArray, rolesFoundSet, true);
      Assert.assertEquals("Not all related roles were returned from the server", 0, rolesFoundSet.size());
      
      rolesFoundSet.add(RegistryRole.Type.getRA_RoleName(ORG_MOI));
      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(ORG_MOI, DISTRICT));
      rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(ORG_MOI, DISTRICT));
      rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(ORG_MOI, DISTRICT));
      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(ORG_MOI, VILLAGE));
      rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(ORG_MOI, VILLAGE));
      rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(ORG_MOI, VILLAGE));
      this.iterateOverReturnedRoles(roleJSONArray, rolesFoundSet, false);
      Assert.assertEquals("Not all related roles were returned from the server", 0, rolesFoundSet.size());
      
      // Edit
      response = (RestResponse)controller.edit(clientRequest, user.getOid());
      userPair = (Pair)response.getAttribute("user");
      user = (GeoprismUserDTO)userPair.getFirst();
      
      rolesPair = (Pair)response.getAttribute("roles");
      roleJSONArray = (JSONArray)rolesPair.getFirst();
      Assert.assertEquals(7, roleJSONArray.length());
      
      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(ORG_MOI, DISTRICT));
      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(ORG_MOI, VILLAGE));
      this.iterateOverReturnedRoles(roleJSONArray, rolesFoundSet, true);
      Assert.assertEquals("Not all related roles were returned from the server", 0, rolesFoundSet.size());
      
      rolesFoundSet.add(RegistryRole.Type.getRA_RoleName(ORG_MOI));
      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(ORG_MOI, DISTRICT));
      rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(ORG_MOI, DISTRICT));
      rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(ORG_MOI, DISTRICT));
      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(ORG_MOI, VILLAGE));
      rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(ORG_MOI, VILLAGE));
      rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(ORG_MOI, VILLAGE));
      
      this.iterateOverReturnedRoles(roleJSONArray, rolesFoundSet, false);
      Assert.assertEquals("Not all related roles were returned from the server", 0, rolesFoundSet.size());
      
      
      // Apply to change and roles
      user.setLastName("Dwayne");
      String rcVillageRole = RegistryRole.Type.getRC_RoleName(ORG_MOI, VILLAGE);
      response = (RestResponse)controller.apply(clientRequest, user, rmDistrictRole+","+rcVillageRole);
      
      userPair = (Pair)response.getAttribute("user");
      user = (GeoprismUserDTO)userPair.getFirst();
      Assert.assertEquals("Dwayne", user.getLastName());
      
      rolePair = (Pair)response.getAttribute("roles");
      roleJSONArray = (JSONArray)rolePair.getFirst();
      Assert.assertEquals(7, roleJSONArray.length());
      
      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(ORG_MOI, DISTRICT));
      rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(ORG_MOI, VILLAGE));
      this.iterateOverReturnedRoles(roleJSONArray, rolesFoundSet, true);
      Assert.assertEquals("Not all related roles were returned from the server", 0, rolesFoundSet.size());
      
      rolesFoundSet = new HashSet<String>();
      rolesFoundSet.add(RegistryRole.Type.getRA_RoleName(ORG_MOI));
      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(ORG_MOI, DISTRICT));
      rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(ORG_MOI, DISTRICT));
      rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(ORG_MOI, DISTRICT));
      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(ORG_MOI, VILLAGE));
      rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(ORG_MOI, VILLAGE));
      rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(ORG_MOI, VILLAGE));

      this.iterateOverReturnedRoles(roleJSONArray, rolesFoundSet, false);
      Assert.assertEquals("Not all related roles were returned from the server", 0, rolesFoundSet.size());
      
    }
    finally
    {
      controller.remove(clientRequest, user.getOid());
    }
  }
  
  /** 
   * Test returning possible roles that can be assigned to a person for a given organization.
   */
  @Test
  @SuppressWarnings("rawtypes")
  public void newInstaneWithOrgRoles()
  {
    RestResponse response = (RestResponse)controller.newInstance(clientRequest, ORG_MOI);
    
    Pair pair = (Pair)response.getAttribute("roles");
    
    JSONArray roleJSONArray = (JSONArray)pair.getFirst();
    
    Assert.assertEquals(7, roleJSONArray.length());
    
    Set<String> rolesFoundSet = new HashSet<String>();

    rolesFoundSet.add(RegistryRole.Type.getRA_RoleName(ORG_MOI));
    rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(ORG_MOI, DISTRICT));
    rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(ORG_MOI, DISTRICT));
    rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(ORG_MOI, DISTRICT));
    rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(ORG_MOI, VILLAGE));
    rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(ORG_MOI, VILLAGE));
    rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(ORG_MOI, VILLAGE));
    
    this.iterateOverReturnedRoles(roleJSONArray, rolesFoundSet, false);
    
    Assert.assertEquals("Not all related roles were returned from the server", 0, rolesFoundSet.size());
  }
  
  /** 
   * Test returning all possible roles that can be assigned to a person by passing in an empty string for the organizations.
   */
  @Test
  public void newInstanceWithRolesEmptyOrgString()
  {
    RestResponse response = (RestResponse)controller.newInstance(clientRequest, "");
    
    createUserWithRoles(response);
  }
  
  /** 
   * Test returning all possible roles that can be assigned to a person by passing in an empty string for the organizations.
   */
  @Test
  public void newInstanceRolesEmptyOrgNull()
  {
    RestResponse response = (RestResponse)controller.newInstance(clientRequest, null);
    
    createUserWithRoles(response);
  }
  
  @SuppressWarnings("rawtypes")
  private void createUserWithRoles(RestResponse response)
  {
    Pair pair = (Pair)response.getAttribute("roles");
    
    JSONArray roleJSONArray = (JSONArray)pair.getFirst();
    
    Assert.assertEquals(12, roleJSONArray.length());
    
    Set<String> rolesFoundSet = new HashSet<String>();
    rolesFoundSet.add(RegistryRole.Type.getSRA_RoleName());
    rolesFoundSet.add(RegistryRole.Type.getRA_RoleName(ORG_MOH));
    rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(ORG_MOH, HEALTH_FACILITY));
    rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(ORG_MOH, HEALTH_FACILITY));
    rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(ORG_MOH, HEALTH_FACILITY));
    rolesFoundSet.add(RegistryRole.Type.getRA_RoleName(ORG_MOI));
    rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(ORG_MOI, DISTRICT));
    rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(ORG_MOI, DISTRICT));
    rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(ORG_MOI, DISTRICT));
    rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(ORG_MOI, VILLAGE));
    rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(ORG_MOI, VILLAGE));
    rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(ORG_MOI, VILLAGE));
    
    this.iterateOverReturnedRoles(roleJSONArray, rolesFoundSet, false);
    
    Assert.assertEquals("Not all related roles were returned from the server", 0, rolesFoundSet.size());
  }
  

  private void iterateOverReturnedRoles(JSONArray roleJSONArray, Set<String> rolesFoundSet, boolean assignedCheck)
  {
    for (int i = 0; i < roleJSONArray.length(); i++)
    {
      JSONObject json = (JSONObject)roleJSONArray.get(i);
      RegistryRole registryRole = RegistryRole.fromJSON(json.toString()); 
      if (!assignedCheck || registryRole.isAssigned())
      {
        rolesFoundSet.remove(registryRole.getName());
      }
// System.out.println(registryRole.getName()+" "+registryRole.isAssigned());
    }
// System.out.println("");
  }
}
