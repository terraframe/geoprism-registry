/**
 *
 */
package net.geoprism.registry.service;

public class AccountServiceControllerTest
{
//  public static RegistryAccountController controller;
//
//  public static ClientSession             systemSession      = null;
//
//  public static ClientRequestIF           clientRequest      = null;
//
//  public static String                    TEST_USER_NAME     = "testRegistryUser";
//
//  public static String                    TEST_USER_PASSWORD = "abc123";
//  
//  public static final String TEST_KEY = "AccountService";
//  
//  public static TestOrganizationInfo moiOrg = new TestOrganizationInfo(TEST_KEY + "MOI", TEST_KEY + "MOI");
//  
//  public static TestGeoObjectTypeInfo district = new TestGeoObjectTypeInfo(TEST_KEY+ "District", moiOrg);
//  
//  public static TestGeoObjectTypeInfo village = new TestGeoObjectTypeInfo(TEST_KEY + "Village", moiOrg);
//  
//  public static TestOrganizationInfo mohOrg = new TestOrganizationInfo(TEST_KEY + "MOH", TEST_KEY + "MOH");
//  
//  public static TestGeoObjectTypeInfo healthFacility = new TestGeoObjectTypeInfo(TEST_KEY + "HealthFacility", mohOrg);
//  
//  @BeforeClass
//  public static void classSetUp()
//  {
//    systemSession = ClientSession.createUserSession("default", TestDataSet.ADMIN_USER_NAME, TestDataSet.ADMIN_PASSWORD, new Locale[] { CommonProperties.getDefaultLocale() });
//    clientRequest = systemSession.getRequest();
//    
//    controller = new RegistryAccountController();
//    controller.setClientRequest(clientRequest);
//
//    classTearDownRequest();
//    classSetUpRequest();
//  }
//
//  @Request
//  public static void classSetUpRequest()
//  {
//    classSetUpTransaction();
//  }
//
//  @Transaction
//  public static void classSetUpTransaction()
//  {
//    moiOrg.apply();
//    district.apply();
//    village.apply();
//    
//    mohOrg.apply();
//    healthFacility.apply();
//  }
//
//  @AfterClass
//  public static void classTearDown()
//  {
//    systemSession.logout();
//
//    classTearDownRequest();
//  }
//
//  @Request
//  public static void classTearDownRequest()
//  {
//    classTearDownTransaction();
//  }
//
//  @Transaction
//  public static void classTearDownTransaction()
//  {
//    district.delete();
//    village.delete();
//    moiOrg.delete();
//    
//    healthFacility.delete();
//    mohOrg.delete();
//  }
//  
//  @Before
//  public void setUp()
//  {
//    deleteAllUsers();
//  }
//  
//  @Request
//  public void deleteAllUsers()
//  {
//    String[] usernames = new String[] {"johny@gmail.com", "sallyy@gmail.com", "franky@gmail.com", "beckyy@gmail.com", "jdoe6"};
//    
//    for (String username : usernames)
//    {
//      deleteUser(username);
//    }
//  }
//  
//  public void deleteUser(String username)
//  {
//    GeoprismUserQuery query = new GeoprismUserQuery(new QueryFactory());
//    
//    query.WHERE(query.getUsername().EQ(username));
//    
//    OIterator<? extends GeoprismUser> it = query.getIterator();
//    
//    try
//    {
//      if (it.hasNext())
//      {
//        GeoprismUser user = it.next();
//        
//        UserInfo info = UserInfo.getByUser(user);
//
//        if (info != null)
//        {
//          info.delete();
//        }
//
//        user.delete();
//      }
//    }
//    finally
//    {
//      it.close();
//    }
//  }
//
//  /**
//   * Test returning possible roles that can be assigned to a person for a given
//   * organization.
//   */
//  @Test
//  public void queryUsers()
//  {
//    String rmDistrictRole = RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), district.getCode());
//    String rmVillageRole = RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), village.getCode());
//
//    String rcDistrictRole = RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), district.getCode());
//    String rcVillageRole = RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), village.getCode());
//
//    JSONObject johny = createUser("johny@gmail.com", rmDistrictRole + "," + rmVillageRole);
//    JSONObject sally = createUser("sallyy@gmail.com", rcDistrictRole + "," + rcVillageRole);
//
//    String rmHealthFacilityRole = RegistryRole.Type.getRM_RoleName(mohOrg.getCode(), healthFacility.getCode());
//    String rcHealthFacilityRole = RegistryRole.Type.getRC_RoleName(mohOrg.getCode(), healthFacility.getCode());
//
//    JSONObject franky = createUser("franky@gmail.com", rmHealthFacilityRole);
//    JSONObject becky = createUser("beckyy@gmail.com", rcHealthFacilityRole);
//
////    try
////    {
//      JSONArray orgCodeArray = new JSONArray();
//      orgCodeArray.put(moiOrg.getCode());
//      // orgCodeArray.put(mohOrg.getCode());
//      // orgCodeArray.put("TEST1");
//      // orgCodeArray.put("TEST2");
//
//      ResponseEntity<String> response = (ResponseEntity<String>) controller.page(1, 10);
////    }
////    finally
////    {
////      controller.remove(johny.getString(GeoprismUserDTO.OID));
////      controller.remove(sally.getString(GeoprismUserDTO.OID));
////
////      controller.remove(franky.getString(GeoprismUserDTO.OID));
////      controller.remove(becky.getString(GeoprismUserDTO.OID));
////    }
//  }
//
//  private JSONObject createUser(String userName, String roleNames)
//  {
//    ResponseEntity<String> response = controller.newInstance("[" + moiOrg.getCode() + "]");
//    Pair userPair = (Pair) response.getAttribute("user");
//    GeoprismUserDTO user = (GeoprismUserDTO) userPair.getFirst();
//
//    Pair rolesPair = (Pair) response.getAttribute("roles");
//    JSONArray roleJSONArray = (JSONArray) rolesPair.getFirst();
//
//    JSONObject jsonUser = new JSONObject();
//    jsonUser.put(GeoprismUserDTO.FIRSTNAME, "Some Firstname");
//    jsonUser.put(GeoprismUserDTO.LASTNAME, "Some Lastame");
//    jsonUser.put(GeoprismUserDTO.USERNAME, userName);
//    jsonUser.put(GeoprismUserDTO.EMAIL, userName);
//    jsonUser.put(GeoprismUserDTO.PASSWORD, "123456");
//
//    response = (RestResponse) controller.apply(jsonUser.toString(), "[" + roleNames + "]");
//    userPair = (Pair) response.getAttribute("user");
//    jsonUser = (JSONObject) userPair.getFirst();
//
//    return jsonUser;
//  }
//
//  /**
//   * Test returning possible roles that can be assigned to a person for a given
//   * organization.
//   */
//  @Test
//  @SuppressWarnings("rawtypes")
//  public void createAndApplyUserWithOrgRoles()
//  {
//    // New Instance
//    RestResponse response = (RestResponse) controller.newInstance("[" + moiOrg.getCode() + "]");
//    Pair userPair = (Pair) response.getAttribute("user");
//    GeoprismUserDTO user = (GeoprismUserDTO) userPair.getFirst();
//
//    Pair rolesPair = (Pair) response.getAttribute("roles");
//    JSONArray roleJSONArray = (JSONArray) rolesPair.getFirst();
//
//    JSONObject jsonUser = new JSONObject();
//    jsonUser.put(GeoprismUserDTO.FIRSTNAME, "John");
//    jsonUser.put(GeoprismUserDTO.LASTNAME, "Doe");
//    jsonUser.put(GeoprismUserDTO.USERNAME, "jdoe6");
//    jsonUser.put(GeoprismUserDTO.EMAIL, "john6@doe.com");
//    jsonUser.put(GeoprismUserDTO.PASSWORD, "123456");
//    
////    user.setFirstName("John");
////    user.setLastName("Doe");
////    user.setUsername("jdoe6");
////    user.setEmail("john6doe.com");
////    user.setPassword("123456");
//
//    String rmDistrictRole = RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), district.getCode());
//    String rmVillageRole = RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), village.getCode());
//
//    // Apply
//    response = (RestResponse) controller.apply(jsonUser.toString(), "[" + rmDistrictRole + "," + rmVillageRole + "]");
//    userPair = (Pair) response.getAttribute("user");
//    jsonUser = (JSONObject) userPair.getFirst();
//
////    try
////    {
//      Pair rolePair = (Pair) response.getAttribute("roles");
//      roleJSONArray = (JSONArray) rolePair.getFirst();
//      Assert.assertEquals(8, roleJSONArray.length());
//
//      Set<String> rolesFoundSet = new HashSet<String>();
//      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), district.getCode()));
//      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), village.getCode()));
//      this.assertReturnedRoles(roleJSONArray, rolesFoundSet, true);
//
//      rolesFoundSet.add(RegistryRole.Type.getRA_RoleName(moiOrg.getCode()));
//      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), district.getCode()));
//      rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), district.getCode()));
//      rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(moiOrg.getCode(), district.getCode()));
//      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), village.getCode()));
//      rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), village.getCode()));
//      rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(moiOrg.getCode(), village.getCode()));
//      this.assertReturnedRoles(roleJSONArray, rolesFoundSet, false);
//
//      // Edit
//      response = (RestResponse) controller.edit(jsonUser.getString("oid"));
//      userPair = (Pair) response.getAttribute("user");
//      jsonUser = (JSONObject) userPair.getFirst();
//
//      rolesPair = (Pair) response.getAttribute("roles");
//      roleJSONArray = (JSONArray) rolesPair.getFirst();
//      Assert.assertEquals(8, roleJSONArray.length());
//
//      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), district.getCode()));
//      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), village.getCode()));
//      this.assertReturnedRoles(roleJSONArray, rolesFoundSet, true);
//
//      rolesFoundSet.add(RegistryRole.Type.getRA_RoleName(moiOrg.getCode()));
//      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), district.getCode()));
//      rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), district.getCode()));
//      rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(moiOrg.getCode(), district.getCode()));
//      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), village.getCode()));
//      rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), village.getCode()));
//      rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(moiOrg.getCode(), village.getCode()));
//
//      this.assertReturnedRoles(roleJSONArray, rolesFoundSet, false);
//
//      // Apply to change and roles
////      user.put(GeoprismUserDTO.LASTNAME, "Dwayne");
//      user.setLastName("Dwayne");
//      
//      String rcVillageRole = RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), village.getCode());
//      response = (RestResponse) controller.apply(jsonUser.toString(), "[" + rmDistrictRole + "," + rcVillageRole + "]");
//
//      userPair = (Pair) response.getAttribute("user");
//      jsonUser = (JSONObject) userPair.getFirst();
//      Assert.assertEquals("Dwayne", user.getLastName());
//
//      rolePair = (Pair) response.getAttribute("roles");
//      roleJSONArray = (JSONArray) rolePair.getFirst();
//      Assert.assertEquals(8, roleJSONArray.length());
//
//      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), district.getCode()));
//      rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), village.getCode()));
//      this.assertReturnedRoles(roleJSONArray, rolesFoundSet, true);
//
//      rolesFoundSet = new HashSet<String>();
//      rolesFoundSet.add(RegistryRole.Type.getRA_RoleName(moiOrg.getCode()));
//      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), district.getCode()));
//      rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), district.getCode()));
//      rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(moiOrg.getCode(), district.getCode()));
//      rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), village.getCode()));
//      rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), village.getCode()));
//      rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(moiOrg.getCode(), village.getCode()));
//
//      this.assertReturnedRoles(roleJSONArray, rolesFoundSet, false);
//
////    }
////    finally
////    {
////      controller.remove(user.getOid());
////    }
//  }
//
//  /**
//   * Test returning possible roles that can be assigned to a person for a given
//   * organization.
//   */
//  @Test
//  @SuppressWarnings("rawtypes")
//  public void newInstaneWithOrgRoles()
//  {
//    RestResponse response = (RestResponse) controller.newInstance("[" + moiOrg.getCode() + "]");
//
//    Pair pair = (Pair) response.getAttribute("roles");
//
//    JSONArray roleJSONArray = (JSONArray) pair.getFirst();
//
//    Assert.assertEquals(7, roleJSONArray.length());
//
//    Set<String> rolesFoundSet = new HashSet<String>();
//
//    rolesFoundSet.add(RegistryRole.Type.getRA_RoleName(moiOrg.getCode()));
//    rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), district.getCode()));
//    rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), district.getCode()));
//    rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(moiOrg.getCode(), district.getCode()));
//    rolesFoundSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), village.getCode()));
//    rolesFoundSet.add(RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), village.getCode()));
//    rolesFoundSet.add(RegistryRole.Type.getAC_RoleName(moiOrg.getCode(), village.getCode()));
//
//    this.assertReturnedRoles(roleJSONArray, rolesFoundSet, false);
//  }
//
//  /**
//   * Test returning all possible roles that can be assigned to a person by
//   * passing in an empty string for the organizations.
//   */
//  @Test
//  public void newInstanceWithRolesEmptyOrgString()
//  {
//    RestResponse response = (RestResponse) controller.newInstance("[]");
//
//    createUserWithRoles(response);
//  }
//
//  /**
//   * Test returning all possible roles that can be assigned to a person by
//   * passing in an empty string for the organizations.
//   */
//  @Test
//  public void newInstanceRolesEmptyOrgNull()
//  {
//    RestResponse response = (RestResponse) controller.newInstance(null);
//
//    createUserWithRoles(response);
//  }
//
//  @SuppressWarnings("rawtypes")
//  private void createUserWithRoles(RestResponse response)
//  {
//    Pair pair = (Pair) response.getAttribute("roles");
//
//    JSONArray roleJSONArray = (JSONArray) pair.getFirst();
//
//    Set<String> requiredRolesSet = new HashSet<String>();
//    requiredRolesSet.add(RegistryRole.Type.getSRA_RoleName());
//    requiredRolesSet.add(RegistryRole.Type.getRA_RoleName(mohOrg.getCode()));
//    requiredRolesSet.add(RegistryRole.Type.getRM_RoleName(mohOrg.getCode(), healthFacility.getCode()));
//    requiredRolesSet.add(RegistryRole.Type.getRC_RoleName(mohOrg.getCode(), healthFacility.getCode()));
//    requiredRolesSet.add(RegistryRole.Type.getAC_RoleName(mohOrg.getCode(), healthFacility.getCode()));
//    requiredRolesSet.add(RegistryRole.Type.getRA_RoleName(moiOrg.getCode()));
//    requiredRolesSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), district.getCode()));
//    requiredRolesSet.add(RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), district.getCode()));
//    requiredRolesSet.add(RegistryRole.Type.getAC_RoleName(moiOrg.getCode(), district.getCode()));
//    requiredRolesSet.add(RegistryRole.Type.getRM_RoleName(moiOrg.getCode(), village.getCode()));
//    requiredRolesSet.add(RegistryRole.Type.getRC_RoleName(moiOrg.getCode(), village.getCode()));
//    requiredRolesSet.add(RegistryRole.Type.getAC_RoleName(moiOrg.getCode(), village.getCode()));
//
//    this.assertReturnedRoles(roleJSONArray, requiredRolesSet, false);
//  }
//
//  private void assertReturnedRoles(JSONArray roleJSONArray, Set<String> rolesFoundSet, boolean assignedCheck)
//  {
//    for (int i = 0; i < roleJSONArray.length(); i++)
//    {
//      JSONObject json = (JSONObject) roleJSONArray.get(i);
//      RegistryRole registryRole = RegistryRole.fromJSON(json.toString());
//      if (!assignedCheck || registryRole.isAssigned())
//      {
//        rolesFoundSet.remove(registryRole.getName());
//      }
//      // System.out.println(registryRole.getName()+"
//      // "+registryRole.isAssigned());
//    }
//    // System.out.println("");
//    
//    Assert.assertEquals("Not all related roles were returned from the server", 0, rolesFoundSet.size());
//  }
}
