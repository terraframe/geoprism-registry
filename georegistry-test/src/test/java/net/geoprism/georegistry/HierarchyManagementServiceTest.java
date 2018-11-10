package net.geoprism.georegistry;

import java.util.Locale;

import net.geoprism.georegistry.service.RegistryService;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.RegistryAdapterServer;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.MetadataFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.business.rbac.UserDAO;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.constants.UserInfo;
import com.runwaysdk.dataaccess.DuplicateDataException;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.SessionFacade;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdBusiness;

public class HierarchyManagementServiceTest
{
  
  public static RegistryAdapter adapter = null;
  
  public static RegistryService service = null;
  
  /**
   * The test user object
   */
  private static UserDAO                  newUser;
  
  /**
   * The username for the user
   */
  private final static String             USERNAME       = "btables";

  /**
   * The password for the user
   */
  private final static String             PASSWORD       = "1234";

  private final static int                sessionLimit   = 2;
  
  @BeforeClass
  @Request
  public static void setUp()
  {
    adapter = RegistryService.getRegistryAdapter();
    
    service = new RegistryService();
    
    setUpTransaction();
  }
  
  @Transaction
  private static void setUpTransaction()
  {    
    try
    {
      // Create a new user
      newUser = UserDAO.newInstance();
      newUser.setValue(UserInfo.USERNAME, USERNAME);
      newUser.setValue(UserInfo.PASSWORD, PASSWORD);
      newUser.setValue(UserInfo.SESSION_LIMIT, Integer.toString(sessionLimit));
      newUser.apply();
      
      // Make the user an admin
      RoleDAO adminRole = RoleDAO.findRole(RoleDAO.ADMIN_ROLE).getBusinessDAO();
      adminRole.assignMember(newUser);
    }
    catch (DuplicateDataException e) {}
  }
  
  @AfterClass
  @Request
  public static void tearDown()
  {
    tearDownTransaction();
  }
  @Transaction
  private static void tearDownTransaction()
  {
    if (newUser.isAppliedToDB())
    {
      newUser.getBusinessDAO().delete();
    }  
  }
  
//  @Test
//  public void testCreateGeoObjectType()
//  {  
//    String sessionId = this.logInAdmin();
//    
//    String code = "PROVINCE_TEST";
//    
//    RegistryAdapterServer registry = new RegistryAdapterServer();
//    
//    GeoObjectType province = MetadataFactory.newGeoObjectType(code, GeometryType.POLYGON, "Province", "", false, registry);
//    String gtJSON = province.toJSON().toString();
//    
//    try
//    {
//      service.createGeoObjectType(sessionId, gtJSON);
//    }
//    finally
//    {
//      logOutAdmin(sessionId);
//    }
//    
//    checkAttributes(code);
//    
//    sessionId = this.logInAdmin();
//    
//    try
//    {
//      service.deleteGeoObjectType(sessionId, code);
//    }
//    finally
//    {
//      logOutAdmin(sessionId);
//    }
//  }  
//  @Request
//  private void checkAttributes(String code)
//  {
//    Universal universal = Universal.getByKey(code);
//    MdBusiness mdBusiness = universal.getMdBusiness();
//    
//    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF)BusinessFacade.getEntityDAO(mdBusiness);
//    
//    mdBusinessDAOIF.getAllDefinedMdAttributes().forEach(a -> System.out.println(a.definesAttribute() +" "+a.getType()));
//  }

  
  @Test
  public void testHierarchyType()
  {  
    String sessionId = this.logInAdmin();

    try
    {
      HierarchyType[] hierarchyTypes = service.getHierarchyTypes(sessionId, null);

      for (HierarchyType hierarchyType : hierarchyTypes)
      {
System.out.println(hierarchyType.toJSON());
      }
    }
    finally
    {
      logOutAdmin(sessionId);
    }

  }  
  
  /**
   * Logs in admin user and returns session id of the user.
   * 
   * @return
   */
  @Request
  private String logInAdmin()
  {
    return SessionFacade.logIn(USERNAME, PASSWORD, new Locale[] { CommonProperties.getDefaultLocale() });
  }
  
  /**
   * Log out the admin user with the given session id.
   * 
   * @param sessionId
   */
  @Request
  private void logOutAdmin(String sessionId)
  {
    SessionFacade.closeSession(sessionId);
  }

  
}
