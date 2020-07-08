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
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestOrganizationInfo;

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
  public static RegistryAccountController controller;

  public static ClientSession             systemSession      = null;

  public static ClientRequestIF           clientRequest      = null;

  public static String                    TEST_USER_NAME     = "testRegistryUser";

  public static String                    TEST_USER_PASSWORD = "abc123";
  
  public static final String TEST_KEY = "AccountService";
  
  public static TestOrganizationInfo moiOrg = new TestOrganizationInfo(TEST_KEY + "MOI", TEST_KEY + "MOI");
  
  public static TestGeoObjectTypeInfo district = new TestGeoObjectTypeInfo(TEST_KEY+ "District", moiOrg);
  
  public static TestGeoObjectTypeInfo village = new TestGeoObjectTypeInfo(TEST_KEY + "Village", moiOrg);
  
  public static TestOrganizationInfo mohOrg = new TestOrganizationInfo(TEST_KEY + "MOH", TEST_KEY + "MOH");
  
  public static TestGeoObjectTypeInfo healthFacility = new TestGeoObjectTypeInfo(TEST_KEY + "HealthFacility", mohOrg);
  
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
    moiOrg.apply();
    district.apply();
    village.apply();
    
    mohOrg.apply();
    healthFacility.apply();
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
    district.delete();
    village.delete();
    moiOrg.delete();
    
    healthFacility.delete();
    mohOrg.delete();
  }

  /**
   * Test returning possible roles that can be assigned to a person for a given
   * organization.
   */
  @Test
  public void queryUsers()
  {
    String rmDistrictRole = RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), district.getCode());
    String rmVillageRole = RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), village.getCode());

    String rcDistrictRole = RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), district.getCode());
    String rcVillageRole = RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), village.getCode());

    JSONObject johny = createUser("johny@gmail.com", rmDistrictRole + "," + rmVillageRole);
    JSONObject sally = createUser("sallyy@gmail.com", rcDistrictRole + "," + rcVillageRole);

    String rmHealthFacilityRole = RegistryRole.Type.getRM_RoleName(mohOrg.getCode(), healthFacility.getCode());
    String rcHealthFacilityRole = RegistryRole.Type.getRC_RoleName(mohOrg.getCode(), healthFacility.getCode());

    JSONObject franky = createUser("franky@gmail.com", rmHealthFacilityRole);
    JSONObject becky = createUser("beckyy@gmail.com", rcHealthFacilityRole);

    try
    {
      JSONArray orgCodeArray = new JSONArray();
      orgCodeArray.put(moiOrg.getCode());
      // orgCodeArray.put(mohOrg.getCode());
      // orgCodeArray.put("TEST1");
      // orgCodeArray.put("TEST2");

      RestBodyResponse response = (RestBodyResponse) controller.page(clientRequest, 1);
    }
    finally
    {
      controller.remove(clientRequest, johny.getString(GeoprismUserDTO.OID));
      controller.remove(clientRequest, sally.getString(GeoprismUserDTO.OID));

      controller.remove(clientRequest, franky.getString(GeoprismUserDTO.OID));
      controller.remove(clientRequest, becky.getString(GeoprismUserDTO.OID));
    }
  }

  private JSONObject createUser(String userName, String roleNames)
  {
    RestResponse response = (RestResponse) controller.newInstance(clientRequest, "[" + moiOrg.getCode() + "]");
    Pair userPair = (Pair) response.getAttribute("user");
    JSONObject user = (JSONObject) userPair.getFirst();

    Pair rolesPair = (Pair) response.getAttribute("roles");
    JSONArray roleJSONArray = (JSONArray) rolesPair.getFirst();

    user.put(GeoprismUserDTO.FIRSTNAME, "Some Firstname");
    user.put(GeoprismUserDTO.LASTNAME, "Some Lastame");
    user.put(GeoprismUserDTO.USERNAME, userName);
    user.put(GeoprismUserDTO.EMAIL, userName);
    user.put(GeoprismUserDTO.PASSWORD, "123456");

    response = (RestResponse) controller.apply(clientRequest, user.toString(), roleNames);
    userPair = (Pair) response.getAttribute("user");
    user = (JSONObject) userPair.getFirst();

    return user;
  }

  /**
   * Test returning possible roles that can be assigned to a person for a given
   * organization.
   */
  @Test
  @SuppressWarnings("rawtypes")
  public void createAndApplyUserWithOrgRoles()
  {
    // New Instance
    RestResponse response = (RestResponse) controller.newInstance(clientRequest, "[" + moiOrg.getCode() + "]");
    Pair userPair = (Pair) response.getAttribute("user");
    GeoprismUserDTO user = (GeoprismUserDTO) userPair.getFirst();

    Pair rolesPair = (Pair) response.getAttribute("roles");
    JSONArray roleJSONArray = (JSONArray) rolesPair.getFirst();

    JSONObject jsonUser = new JSONObject();
    jsonUser.put(GeoprismUserDTO.FIRSTNAME, "John");
    jsonUser.put(GeoprismUserDTO.LASTNAME, "Doe");
    jsonUser.put(GeoprismUserDTO.USERNAME, "jdoe6");
    jsonUser.put(GeoprismUserDTO.EMAIL, "john6@doe.com");
    jsonUser.put(GeoprismUserDTO.PASSWORD, "123456");
    
//    user.setFirstName("John");
//    user.setLastName("Doe");
//    user.setUsername("jdoe6");
//    user.setEmail("john6doe.com");
//    user.setPassword("123456");

    String rmDistrictRole = RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), district.getCode());
    String rmVillageRole = RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), village.getCode());

    // Apply
    response = (RestResponse) controller.apply(clientRequest, jsonUser.toString(), "[" + rmDistrictRole + "," + rmVillageRole + "]");
    userPair = (Pair) response.getAttribute("user");
    jsonUser = (JSONObject) userPair.getFirst();

    try
    {
      Pair rolePair = (Pair) response.getAttribute("roles");
      roleJSONArray = (JSONArray) rolePair.getFirst();
      Assert.assertEquals(7, roleJSONArray.length());

      Set<String> rolesFoundSet = new HashSet<String>();
      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), district.getCode()));
      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), village.getCode()));
      this.iterateOverReturnedRoles(roleJSONArray, rolesFoundSet, true);
      Assert.assertEquals("Not all related roles were returned from the server", 0, rolesFoundSet.size());

      rolesFoundSet.add(RegistryRole.Type.getRA_RoleName(moiOrg.getCode()));
      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), district.getCode()));
      rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), district.getCode()));
      rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(moiOrg.getCode(), district.getCode()));
      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), village.getCode()));
      rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), village.getCode()));
      rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(moiOrg.getCode(), village.getCode()));
      this.iterateOverReturnedRoles(roleJSONArray, rolesFoundSet, false);
      Assert.assertEquals("Not all related roles were returned from the server", 0, rolesFoundSet.size());

      // Edit
      response = (RestResponse) controller.edit(clientRequest, user.getOid());
      userPair = (Pair) response.getAttribute("user");
      user = (GeoprismUserDTO) userPair.getFirst();

      rolesPair = (Pair) response.getAttribute("roles");
      roleJSONArray = (JSONArray) rolesPair.getFirst();
      Assert.assertEquals(7, roleJSONArray.length());

      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), district.getCode()));
      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), village.getCode()));
      this.iterateOverReturnedRoles(roleJSONArray, rolesFoundSet, true);
      Assert.assertEquals("Not all related roles were returned from the server", 0, rolesFoundSet.size());

      rolesFoundSet.add(RegistryRole.Type.getRA_RoleName(moiOrg.getCode()));
      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), district.getCode()));
      rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), district.getCode()));
      rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(moiOrg.getCode(), district.getCode()));
      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), village.getCode()));
      rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), village.getCode()));
      rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(moiOrg.getCode(), village.getCode()));

      this.iterateOverReturnedRoles(roleJSONArray, rolesFoundSet, false);
      Assert.assertEquals("Not all related roles were returned from the server", 0, rolesFoundSet.size());

      // Apply to change and roles
//      user.put(GeoprismUserDTO.LASTNAME, "Dwayne");
      user.setLastName("Dwayne");
      
      String rcVillageRole = RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), village.getCode());
      response = (RestResponse) controller.apply(clientRequest, user.toString(), rmDistrictRole + "," + rcVillageRole);

      userPair = (Pair) response.getAttribute("user");
      user = (GeoprismUserDTO) userPair.getFirst();
      Assert.assertEquals("Dwayne", user.getLastName());

      rolePair = (Pair) response.getAttribute("roles");
      roleJSONArray = (JSONArray) rolePair.getFirst();
      Assert.assertEquals(7, roleJSONArray.length());

      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), district.getCode()));
      rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), village.getCode()));
      this.iterateOverReturnedRoles(roleJSONArray, rolesFoundSet, true);
      Assert.assertEquals("Not all related roles were returned from the server", 0, rolesFoundSet.size());

      rolesFoundSet = new HashSet<String>();
      rolesFoundSet.add(RegistryRole.Type.getRA_RoleName(moiOrg.getCode()));
      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), district.getCode()));
      rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), district.getCode()));
      rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(moiOrg.getCode(), district.getCode()));
      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), village.getCode()));
      rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), village.getCode()));
      rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(moiOrg.getCode(), village.getCode()));

      this.iterateOverReturnedRoles(roleJSONArray, rolesFoundSet, false);
      Assert.assertEquals("Not all related roles were returned from the server", 0, rolesFoundSet.size());

    }
    finally
    {
      controller.remove(clientRequest, user.getOid());
    }
  }

  /**
   * Test returning possible roles that can be assigned to a person for a given
   * organization.
   */
  @Test
  @SuppressWarnings("rawtypes")
  public void newInstaneWithOrgRoles()
  {
    RestResponse response = (RestResponse) controller.newInstance(clientRequest, "[" + moiOrg.getCode() + "]");

    Pair pair = (Pair) response.getAttribute("roles");

    JSONArray roleJSONArray = (JSONArray) pair.getFirst();

    Assert.assertEquals(7, roleJSONArray.length());

    Set<String> rolesFoundSet = new HashSet<String>();

    rolesFoundSet.add(RegistryRole.Type.getRA_RoleName(moiOrg.getCode()));
    rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), district.getCode()));
    rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), district.getCode()));
    rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(moiOrg.getCode(), district.getCode()));
    rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), village.getCode()));
    rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), village.getCode()));
    rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(moiOrg.getCode(), village.getCode()));

    this.iterateOverReturnedRoles(roleJSONArray, rolesFoundSet, false);

    Assert.assertEquals("Not all related roles were returned from the server", 0, rolesFoundSet.size());
  }

  /**
   * Test returning all possible roles that can be assigned to a person by
   * passing in an empty string for the organizations.
   */
  @Test
  public void newInstanceWithRolesEmptyOrgString()
  {
    RestResponse response = (RestResponse) controller.newInstance(clientRequest, "[]");

    createUserWithRoles(response);
  }

  /**
   * Test returning all possible roles that can be assigned to a person by
   * passing in an empty string for the organizations.
   */
  @Test
  public void newInstanceRolesEmptyOrgNull()
  {
    RestResponse response = (RestResponse) controller.newInstance(clientRequest, null);

    createUserWithRoles(response);
  }

  @SuppressWarnings("rawtypes")
  private void createUserWithRoles(RestResponse response)
  {
    Pair pair = (Pair) response.getAttribute("roles");

    JSONArray roleJSONArray = (JSONArray) pair.getFirst();

    Assert.assertEquals(12, roleJSONArray.length());

    Set<String> rolesFoundSet = new HashSet<String>();
    rolesFoundSet.add(RegistryRole.Type.getSRA_RoleName());
    rolesFoundSet.add(RegistryRole.Type.getRA_RoleName(mohOrg.getCode()));
    rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(mohOrg.getCode(), healthFacility.getCode()));
    rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(mohOrg.getCode(), healthFacility.getCode()));
    rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(mohOrg.getCode(), healthFacility.getCode()));
    rolesFoundSet.add(RegistryRole.Type.getRA_RoleName(moiOrg.getCode()));
    rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), district.getCode()));
    rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), district.getCode()));
    rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(moiOrg.getCode(), district.getCode()));
    rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), village.getCode()));
    rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), village.getCode()));
    rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(moiOrg.getCode(), village.getCode()));

    this.iterateOverReturnedRoles(roleJSONArray, rolesFoundSet, false);

    Assert.assertEquals("Not all related roles were returned from the server", 0, rolesFoundSet.size());
  }

  private void iterateOverReturnedRoles(JSONArray roleJSONArray, Set<String> rolesFoundSet, boolean assignedCheck)
  {
    for (int i = 0; i < roleJSONArray.length(); i++)
    {
      JSONObject json = (JSONObject) roleJSONArray.get(i);
      RegistryRole registryRole = RegistryRole.fromJSON(json.toString());
      if (!assignedCheck || registryRole.isAssigned())
      {
        rolesFoundSet.remove(registryRole.getName());
      }
      // System.out.println(registryRole.getName()+"
      // "+registryRole.isAssigned());
    }
    // System.out.println("");
  }
}
