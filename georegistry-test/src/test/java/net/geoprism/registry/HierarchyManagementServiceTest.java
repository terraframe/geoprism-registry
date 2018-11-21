package net.geoprism.registry;

import java.util.Locale;

import net.geoprism.GeoprismUser;
import net.geoprism.georegistry.RegistryConstants;
import net.geoprism.georegistry.service.ConversionService;
import net.geoprism.georegistry.service.RegistryService;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.RegistryAdapterServer;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.MetadataFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.business.rbac.UserDAO;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.constants.LocalProperties;
import com.runwaysdk.constants.MdBusinessInfo;
import com.runwaysdk.dataaccess.DuplicateDataException;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.constants.GISConstants;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.SessionFacade;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdTermRelationship;

public class HierarchyManagementServiceTest
{
  
  public static RegistryAdapter adapter = null;
  
  public static RegistryService service = null;
  
  /**
   * The test user object
   */
//  private static UserDAO                  newUser;
  private static GeoprismUser             newUser;
  
  /**
   * The username for the user
   */
  private final static String             USERNAME                          = "btables";
  
  private static String                   USER_KEY                          = "";

  /**
   * The password for the user
   */
  private final static String             PASSWORD                          = "1234";

  private final static int                sessionLimit                      = 2;
  
  private final static String             COUNTRY_CODE                      = "CountryTest";
  
  private final static String             PROVINCE_CODE                     = "ProvinceTest";
  
  private final static String             DISTRICT_CODE                     = "DistrictTest";
  
  private final static String             VILLAGE_CODE                      = "VillageTest";
  
  private final static String             RIVER_CODE                        = "RiverTest";
  
  private final static String             REPORTING_DIVISION_CODE           = "ReportingDivision";
  
  private final static String             ADMINISTRATIVE_DIVISION_CODE      = "AdministrativeDivision";
  
  @BeforeClass
  @Request
  public static void setUp()
  {
    LocalProperties.setSkipCodeGenAndCompile(true);
    
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
      newUser = new GeoprismUser();
      newUser.setUsername(USERNAME);
      newUser.setPassword(PASSWORD);
      newUser.setFirstName("Bobby");
      newUser.setLastName("Tables");
      newUser.setEmail("bobby@tables.com");
      newUser.setSessionLimit(sessionLimit);
      newUser.apply();
      
      // Make the user an admin //      
      RoleDAO adminRole = RoleDAO.findRole(RegistryConstants.REGISTRY_ADMIN_ROLE).getBusinessDAO();
      adminRole.assignMember((UserDAO)BusinessFacade.getEntityDAO(newUser));
    }
    catch (DuplicateDataException e) {}
    
    USER_KEY = "Users."+USERNAME;
  }
  
  @AfterClass
  @Request
  public static void tearDown()
  {
    try
    {
      tearDownTransaction();
    }
    finally
    {
      LocalProperties.setSkipCodeGenAndCompile(false);
    }
  }
  @Transaction
  private static void tearDownTransaction()
  {    
    // Just in case a previous test did not clean up properly.   
    try
    {
      Universal universal = Universal.getByKey(VILLAGE_CODE);
      universal.delete();
    } catch (DataNotFoundException e) {} 
    
    try
    {
      Universal universal = Universal.getByKey(DISTRICT_CODE);
      universal.delete();
    } catch (DataNotFoundException e) {} 
    
    try
    {
      Universal provinceTestUniversal = Universal.getByKey(PROVINCE_CODE);
      provinceTestUniversal.delete();
    } catch (DataNotFoundException e) {} 
    
    try
    {
      Universal countryTestUniversal = Universal.getByKey(COUNTRY_CODE);
      countryTestUniversal.delete();
    } catch (DataNotFoundException e) {} 
    
    try
    {
      Universal riverTestUniversal = Universal.getByKey(RIVER_CODE);
      riverTestUniversal.delete();
    } catch (DataNotFoundException e) {} 
    
    try
    {
      MdTermRelationship mdTermRelationship = MdTermRelationship.getByKey(ConversionService.buildMdTermRelUniversalKey(REPORTING_DIVISION_CODE));
      mdTermRelationship.delete();
    } catch (DataNotFoundException e) {} 
    
    try
    {
      MdTermRelationship mdTermRelationship = MdTermRelationship.getByKey(ConversionService.buildMdTermRelUniversalKey(ADMINISTRATIVE_DIVISION_CODE));
      mdTermRelationship.delete();
    } catch (DataNotFoundException e) {} 
    
    try
    {
      MdTermRelationship mdTermRelationship = MdTermRelationship.getByKey(ConversionService.buildMdTermRelGeoEntityKey(REPORTING_DIVISION_CODE));
      mdTermRelationship.delete();
    } catch (DataNotFoundException e) {} 
    
    try
    {
      MdTermRelationship mdTermRelationship = MdTermRelationship.getByKey(ConversionService.buildMdTermRelGeoEntityKey(ADMINISTRATIVE_DIVISION_CODE));
      mdTermRelationship.delete();
    } catch (DataNotFoundException e) {} 
    
    GeoprismUser.getByKey(USER_KEY).delete();
  }
  
  @Test
  public void testCreateGeoObjectType()
  {  
    String sessionId = this.logInAdmin();
   
    
    RegistryAdapterServer registry = new RegistryAdapterServer();
    
    GeoObjectType province = MetadataFactory.newGeoObjectType(PROVINCE_CODE, GeometryType.POLYGON, "Province", "", false, registry);
    String gtJSON = province.toJSON().toString();

    try
    {
      service.createGeoObjectType(sessionId, gtJSON);
    }
    finally
    {
      logOutAdmin(sessionId);
    }
    
    checkAttributes(PROVINCE_CODE);
    
    sessionId = this.logInAdmin();
    try
    {
      service.deleteGeoObjectType(sessionId, PROVINCE_CODE);
    }
    finally
    {
      logOutAdmin(sessionId);
    }
  }  
  @Request
  private void checkAttributes(String code)
  {
    Universal universal = Universal.getByKey(code);
    MdBusiness mdBusiness = universal.getMdBusiness();
    
    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF)BusinessFacade.getEntityDAO(mdBusiness);
// For debugging    
//    mdBusinessDAOIF.getAllDefinedMdAttributes().forEach(a -> System.out.println(a.definesAttribute() +" "+a.getType()));

    // DefaultAttribute.UID - Defined on the MdBusiness and the values are from the {@code GeoObject#OID};
    try
    {
      mdBusinessDAOIF.definesAttribute(DefaultAttribute.UID.getName());
    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObject.UID does not exist. It should be defined on the business class");
    }
    
    // DefaultAttribute.CODE - defined by GeoEntity geoId
    try
    {
      mdBusinessDAOIF.definesAttribute(DefaultAttribute.CODE.getName());
    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObjectType.CODE does not exist.It should be defined on the business class");
    }
    
    // DefaultAttribute.CREATED_DATE - The create data on the GeoObject?
    try
    {
      mdBusinessDAOIF.definesAttribute(MdBusinessInfo.CREATE_DATE);
    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObjectType.CREATED_DATE does not exist.It should be defined on the business class");
    }
    
    // DefaultAttribute.UPDATED_DATE - The update data on the GeoObject?
    try
    {
      mdBusinessDAOIF.definesAttribute(MdBusinessInfo.LAST_UPDATE_DATE);
    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObjectType.LAST_UPDATE_DATE does not exist.It should be defined on the business class");
    }

    // DefaultAttribute.STATUS 
    try
    {
      mdBusinessDAOIF.definesAttribute(MdBusinessInfo.LAST_UPDATE_DATE);
    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObjectType.LAST_UPDATE_DATE does not exist.It should be defined on the business class");
    }
  }

  @Test
  public void testCreateGeoObjectTypePoint()
  {  
    String sessionId = this.logInAdmin();
   
    RegistryAdapterServer registry = new RegistryAdapterServer();
    
    GeoObjectType village = MetadataFactory.newGeoObjectType(VILLAGE_CODE, GeometryType.POINT, "Village", "", true, registry);
    String villageJSON = village.toJSON().toString();

    try
    {
      service.createGeoObjectType(sessionId, villageJSON);
    }
    finally
    {
      logOutAdmin(sessionId);
    }
    
    checkAttributePoint(VILLAGE_CODE);
    
    sessionId = this.logInAdmin();
    try
    {
      service.deleteGeoObjectType(sessionId, VILLAGE_CODE);
    }
    finally
    {
      logOutAdmin(sessionId);
    }
  } 
  
  @Test
  public void testCreateGeoObjectTypeLine()
  {  
    String sessionId = this.logInAdmin();
   
    RegistryAdapterServer registry = new RegistryAdapterServer();
   
    GeoObjectType river = MetadataFactory.newGeoObjectType(RIVER_CODE, GeometryType.LINE, "River", "", true, registry);
    String riverJSON = river.toJSON().toString();

    try
    {
      service.createGeoObjectType(sessionId, riverJSON);
    }
    finally
    {
      logOutAdmin(sessionId);
    }

    checkAttributeLine(RIVER_CODE);
    
    sessionId = this.logInAdmin();
    try
    {
      service.deleteGeoObjectType(sessionId, RIVER_CODE);
    }
    finally
    {
      logOutAdmin(sessionId);
    }
  } 
  
  @Test
  public void testCreateGeoObjectTypePolygon()
  {  
    String sessionId = this.logInAdmin();
   
    RegistryAdapterServer registry = new RegistryAdapterServer();
   
    GeoObjectType geoObjectType = MetadataFactory.newGeoObjectType(DISTRICT_CODE, GeometryType.POLYGON, "District", "", true, registry);
    String gtJSON = geoObjectType.toJSON().toString();

    try
    {
      service.createGeoObjectType(sessionId, gtJSON);
    }
    finally
    {
      logOutAdmin(sessionId);
    }

    checkAttributePolygon(DISTRICT_CODE);
    
    sessionId = this.logInAdmin();
    try
    {
      service.deleteGeoObjectType(sessionId, DISTRICT_CODE);
    }
    finally
    {
      logOutAdmin(sessionId);
    }
  } 
  
  @Test
  public void testCreateGeoObjectTypeMultiPoint()
  {  
    String sessionId = this.logInAdmin();
   
    RegistryAdapterServer registry = new RegistryAdapterServer();
    
    GeoObjectType village = MetadataFactory.newGeoObjectType(VILLAGE_CODE, GeometryType.MULTIPOINT, "Village", "", true, registry);
    String villageJSON = village.toJSON().toString();

    try
    {
      service.createGeoObjectType(sessionId, villageJSON);
    }
    finally
    {
      logOutAdmin(sessionId);
    }
    
    checkAttributeMultiPoint(VILLAGE_CODE);
    
    sessionId = this.logInAdmin();
    try
    {
      service.deleteGeoObjectType(sessionId, VILLAGE_CODE);
    }
    finally
    {
      logOutAdmin(sessionId);
    }
  } 
  
  @Test
  public void testCreateGeoObjectTypeMultiLine()
  {  
    String sessionId = this.logInAdmin();
   
    RegistryAdapterServer registry = new RegistryAdapterServer();
   
    GeoObjectType river = MetadataFactory.newGeoObjectType(RIVER_CODE, GeometryType.MULTILINE, "River", "", true, registry);
    String riverJSON = river.toJSON().toString();

    try
    {
      service.createGeoObjectType(sessionId, riverJSON);
    }
    finally
    {
      logOutAdmin(sessionId);
    }

    checkAttributeMultiLine(RIVER_CODE);
    
    sessionId = this.logInAdmin();
    try
    {
      service.deleteGeoObjectType(sessionId, RIVER_CODE);
    }
    finally
    {
      logOutAdmin(sessionId);
    }
  } 
  
  @Test
  public void testCreateGeoObjectTypeMultiPolygon()
  {  
    String sessionId = this.logInAdmin();
   
    RegistryAdapterServer registry = new RegistryAdapterServer();
   
    GeoObjectType geoObjectType = MetadataFactory.newGeoObjectType(DISTRICT_CODE, GeometryType.MULTIPOLYGON, "District", "", true, registry);
    String gtJSON = geoObjectType.toJSON().toString();

    try
    {
      service.createGeoObjectType(sessionId, gtJSON);
    }
    finally
    {
      logOutAdmin(sessionId);
    }

    checkAttributeMultiPolygon(DISTRICT_CODE);
    
    sessionId = this.logInAdmin();
    try
    {
      service.deleteGeoObjectType(sessionId, DISTRICT_CODE);
    }
    finally
    {
      logOutAdmin(sessionId);
    }
  } 
  
  @Request
  private void checkAttributePoint(String code)
  {
    Universal universal = Universal.getByKey(code);
    MdBusiness mdBusiness = universal.getMdBusiness();
    
    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF)BusinessFacade.getEntityDAO(mdBusiness);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = mdBusinessDAOIF.definesAttribute(RegistryConstants.GEO_POINT_ATTRIBUTE_NAME);

    Assert.assertNotNull("A GeoObjectType did not define the proper geometry type attribute: "+RegistryConstants.GEO_POINT_ATTRIBUTE_NAME, mdAttributeConcreteDAOIF);
  }
  @Request
  private void checkAttributeLine(String code)
  {
    Universal universal = Universal.getByKey(code);
    MdBusiness mdBusiness = universal.getMdBusiness();
    
    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF)BusinessFacade.getEntityDAO(mdBusiness);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = mdBusinessDAOIF.definesAttribute(RegistryConstants.GEO_LINE_ATTRIBUTE_NAME);

    Assert.assertNotNull("A GeoObjectType did not define the proper geometry type attribute: "+RegistryConstants.GEO_LINE_ATTRIBUTE_NAME, mdAttributeConcreteDAOIF);

  }
  @Request
  private void checkAttributePolygon(String code)
  {
    Universal universal = Universal.getByKey(code);
    MdBusiness mdBusiness = universal.getMdBusiness();
    
    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF)BusinessFacade.getEntityDAO(mdBusiness);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = mdBusinessDAOIF.definesAttribute(RegistryConstants.GEO_POLYGON_ATTRIBUTE_NAME);

    Assert.assertNotNull("A GeoObjectType did not define the proper geometry type attribute: "+RegistryConstants.GEO_POLYGON_ATTRIBUTE_NAME, mdAttributeConcreteDAOIF);
  }
  @Request
  private void checkAttributeMultiPoint(String code)
  {
    Universal universal = Universal.getByKey(code);
    MdBusiness mdBusiness = universal.getMdBusiness();
    
    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF)BusinessFacade.getEntityDAO(mdBusiness);
    
    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = mdBusinessDAOIF.definesAttribute(RegistryConstants.GEO_MULTIPOINT_ATTRIBUTE_NAME);

    Assert.assertNotNull("A GeoObjectType did not define the proper geometry type attribute: "+RegistryConstants.GEO_MULTIPOINT_ATTRIBUTE_NAME, mdAttributeConcreteDAOIF);
  }
  @Request
  private void checkAttributeMultiLine(String code)
  {
    Universal universal = Universal.getByKey(code);
    MdBusiness mdBusiness = universal.getMdBusiness();
    
    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF)BusinessFacade.getEntityDAO(mdBusiness);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = mdBusinessDAOIF.definesAttribute(RegistryConstants.GEO_MULTILINE_ATTRIBUTE_NAME);

    Assert.assertNotNull("A GeoObjectType did not define the proper geometry type attribute: "+RegistryConstants.GEO_MULTILINE_ATTRIBUTE_NAME, mdAttributeConcreteDAOIF);
  }
  @Request
  private void checkAttributeMultiPolygon(String code)
  {
    Universal universal = Universal.getByKey(code);
    MdBusiness mdBusiness = universal.getMdBusiness();
    
    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF)BusinessFacade.getEntityDAO(mdBusiness);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = mdBusinessDAOIF.definesAttribute(RegistryConstants.GEO_MULTIPOLYGON_ATTRIBUTE_NAME);

    Assert.assertNotNull("A GeoObjectType did not define the proper geometry type attribute: "+RegistryConstants.GEO_MULTIPOLYGON_ATTRIBUTE_NAME, mdAttributeConcreteDAOIF);
  }


  @Test
  public void testUpdateGeoObjectType()
  {      
    RegistryAdapterServer registry = new RegistryAdapterServer();
    
    GeoObjectType province = MetadataFactory.newGeoObjectType(PROVINCE_CODE, GeometryType.POLYGON, "Province Test", "Some Description", false, registry);
    String gtJSON = province.toJSON().toString();
    
    String sessionId = this.logInAdmin();
    try
    {
      service.createGeoObjectType(sessionId, gtJSON);
    }
    finally
    {
      logOutAdmin(sessionId);
    }

    sessionId = this.logInAdmin();
    try
    {
      province = service.getGeoObjectTypes(sessionId, new String[]{PROVINCE_CODE})[0]; 
    
      province.setLocalizedLabel("Province Test 2");
      province.setLocalizedDescription("Some Description 2");

      gtJSON = province.toJSON().toString();
      service.updateGeoObjectType(sessionId, gtJSON);
      
      province = service.getGeoObjectTypes(sessionId, new String[]{PROVINCE_CODE})[0]; 
      
      Assert.assertEquals("Display label was not updated on a GeoObjectType", "Province Test 2", province.getLocalizedLabel());
      Assert.assertEquals("Description  was not updated on a GeoObjectType", "Some Description 2", province.getLocalizedDescription());
    }
    finally
    {
      logOutAdmin(sessionId);
    }

    sessionId = this.logInAdmin();
    try
    {
      service.deleteGeoObjectType(sessionId, PROVINCE_CODE);
    }
    finally
    {
      logOutAdmin(sessionId);
    }
  }  
  
  @Test
  public void testCreateHierarchyType()
  {      
    RegistryAdapterServer registry = new RegistryAdapterServer();
    
    // newGeoObjectType(PROVINCE_CODE, GeometryType.POLYGON, "Province", "", false, registry);
    
    HierarchyType reportingDivision = MetadataFactory.newHierarchyType(REPORTING_DIVISION_CODE, "Reporting Division", "The rporting division hieracy...", registry);
    String gtJSON = reportingDivision.toJSON().toString();

    String sessionId = this.logInAdmin();
    try
    {
      service.createHierarchyType(sessionId, gtJSON);
    }
    finally
    {
      logOutAdmin(sessionId);
    }
   
    sessionId = this.logInAdmin();
    try
    {
      HierarchyType[] hierarchies = service.getHierarchyTypes(sessionId, new String[]{REPORTING_DIVISION_CODE});

      Assert.assertNotNull("The created hierarchy was not returned", hierarchies);
      
      Assert.assertEquals("The wrong number of hierarchies were returned.", 1, hierarchies.length);

      HierarchyType hierarchy = hierarchies[0];
      
      Assert.assertEquals("", "Reporting Division", hierarchy.getLocalizedLabel());
    }
    finally
    {
      logOutAdmin(sessionId);
    }
    
    
    // test the types that were created 
    String mdTermRelUniversal = ConversionService.buildMdTermRelUniversalKey(reportingDivision.getCode());
    String expectedMdTermRelUniversal = GISConstants.GEO_PACKAGE+"."+reportingDivision.getCode()+RegistryConstants.UNIVERSAL_RELATIONSHIP_POST;
    Assert.assertEquals("The type name of the MdTermRelationshp defining the universals was not correctly defined for the given code.", expectedMdTermRelUniversal, mdTermRelUniversal);
    
    String mdTermRelGeoEntity = ConversionService.buildMdTermRelGeoEntityKey(reportingDivision.getCode());
    String expectedMdTermRelGeoEntity = GISConstants.GEO_PACKAGE+"."+reportingDivision.getCode();
    Assert.assertEquals("The type name of the MdTermRelationshp defining the geoentities was not correctly defined for the given code.", expectedMdTermRelGeoEntity, mdTermRelGeoEntity);
        
    
    sessionId = this.logInAdmin();
    try
    {
      service.deleteHierarchyType(sessionId, REPORTING_DIVISION_CODE);
    }
    finally
    {
      logOutAdmin(sessionId);
    }
  }  
  
  @Test
  public void testUpdateHierarchyType()
  {      
    RegistryAdapterServer registry = new RegistryAdapterServer();
    
    // newGeoObjectType(PROVINCE_CODE, GeometryType.POLYGON, "Province", "", false, registry);
    
    HierarchyType reportingDivision = MetadataFactory.newHierarchyType(REPORTING_DIVISION_CODE, "Reporting Division", "The rporting division hieracy...", registry);
    String gtJSON = reportingDivision.toJSON().toString();
    
    String sessionId = this.logInAdmin();
    try
    {
      reportingDivision = service.createHierarchyType(sessionId, gtJSON);
      
      reportingDivision.setLocalizedLabel("Reporting Division 2");
      
      reportingDivision.setLocalizedDescription("The rporting division hieracy 2");
      
      gtJSON = reportingDivision.toJSON().toString();
     
      reportingDivision = service.updateHierarchyType(sessionId, gtJSON);
      
      Assert.assertNotNull("The created hierarchy was not returned", reportingDivision);
      Assert.assertEquals("", "Reporting Division 2", reportingDivision.getLocalizedLabel());
      Assert.assertEquals("", "The rporting division hieracy 2", reportingDivision.getLocalizedDescription());
    }
    finally
    {
      logOutAdmin(sessionId);
    }
 
    sessionId = this.logInAdmin();
    try
    {
      service.deleteHierarchyType(sessionId, REPORTING_DIVISION_CODE);
    }
    finally
    {
      logOutAdmin(sessionId);
    }
  }  
  
  
  @Test
  public void testAddToHierarchy()
  { 
    RegistryAdapterServer registry = new RegistryAdapterServer();
    
    GeoObjectType country = MetadataFactory.newGeoObjectType(COUNTRY_CODE, GeometryType.POLYGON, "Country Test", "Some Description", false, registry);
    
    GeoObjectType province = MetadataFactory.newGeoObjectType(PROVINCE_CODE, GeometryType.POLYGON, "Province Test", "Some Description", false, registry);
    
    GeoObjectType district = MetadataFactory.newGeoObjectType(DISTRICT_CODE, GeometryType.POLYGON, "District Test", "Some Description", false, registry);

    GeoObjectType village = MetadataFactory.newGeoObjectType(VILLAGE_CODE, GeometryType.POLYGON, "Village Test", "Some Description", false, registry);
    
    HierarchyType reportingDivision = MetadataFactory.newHierarchyType(REPORTING_DIVISION_CODE, "Reporting Division", "The reporting division hieracy...", registry);
    
    HierarchyType administrativeDivision = MetadataFactory.newHierarchyType(ADMINISTRATIVE_DIVISION_CODE, "Administrative Division", "The administrative division hieracy...", registry);
    
    
    
    // Create the GeoObjectTypes
    String sessionId = this.logInAdmin();
    try
    {
      String gtJSON = country.toJSON().toString();
      country = service.createGeoObjectType(sessionId, gtJSON);
      
      gtJSON = province.toJSON().toString();
      province = service.createGeoObjectType(sessionId, gtJSON);
      
      gtJSON = district.toJSON().toString();
      district = service.createGeoObjectType(sessionId, gtJSON);
      
      gtJSON = village.toJSON().toString();
      village = service.createGeoObjectType(sessionId, gtJSON);
      
      String htJSON = reportingDivision.toJSON().toString();
      reportingDivision = service.createHierarchyType(sessionId, htJSON);
      
      String htJSON2 = administrativeDivision.toJSON().toString();
      administrativeDivision = service.createHierarchyType(sessionId, htJSON2);
      
//    }
//    finally
//    {
//      logOutAdmin(sessionId);
//    }
//    // Log out and log back in so as to get the latest user permissions.  
//    sessionId = this.logInAdmin();
//    try
//    {
      Assert.assertEquals("HierarchyType \""+REPORTING_DIVISION_CODE+"\" should not have any GeoObjectTypes in the hierarchy", 0, reportingDivision.getRootGeoObjectTypes().size());
      
      reportingDivision = service.addToHierarchy(sessionId, reportingDivision.getCode(), Universal.ROOT, country.getCode());
      
      Assert.assertEquals("HierarchyType \""+REPORTING_DIVISION_CODE+"\" should have one root type", 1, reportingDivision.getRootGeoObjectTypes().size());
      
      HierarchyType.HierarchyNode countryNode = reportingDivision.getRootGeoObjectTypes().get(0);
      
      Assert.assertEquals("HierarchyType \""+REPORTING_DIVISION_CODE+"\" should have root of type", COUNTRY_CODE, countryNode.getGeoObjectType().getCode());
      
      Assert.assertEquals("GeoObjectType \""+COUNTRY_CODE+"\" should have no child", 0, countryNode.getChildren().size());
      
      reportingDivision = service.addToHierarchy(sessionId, reportingDivision.getCode(), country.getCode(), province.getCode());

      countryNode = reportingDivision.getRootGeoObjectTypes().get(0);
      
      Assert.assertEquals("GeoObjectType \""+COUNTRY_CODE+"\" should have one child", 1, countryNode.getChildren().size());
      
      HierarchyType.HierarchyNode provinceNode = countryNode.getChildren().get(0);
      
      Assert.assertEquals("GeoObjectType \""+COUNTRY_CODE+"\" should have a child of type", PROVINCE_CODE, provinceNode.getGeoObjectType().getCode());
      
      reportingDivision = service.addToHierarchy(sessionId, reportingDivision.getCode(), province.getCode(), district.getCode());
      
      countryNode = reportingDivision.getRootGeoObjectTypes().get(0);
      provinceNode = countryNode.getChildren().get(0);
      HierarchyType.HierarchyNode districtNode = provinceNode.getChildren().get(0);
      
      Assert.assertEquals("GeoObjectType \""+PROVINCE_CODE+"\" should have a child of type", DISTRICT_CODE, districtNode.getGeoObjectType().getCode()); 
      
      reportingDivision = service.addToHierarchy(sessionId, reportingDivision.getCode(), district.getCode(), village.getCode());
      
      countryNode = reportingDivision.getRootGeoObjectTypes().get(0);
      provinceNode = countryNode.getChildren().get(0);
      districtNode = provinceNode.getChildren().get(0);
      HierarchyType.HierarchyNode villageNode = districtNode.getChildren().get(0);
      
      Assert.assertEquals("GeoObjectType \""+DISTRICT_CODE+"\" should have a child of type", VILLAGE_CODE, villageNode.getGeoObjectType().getCode()); 
      
      
      reportingDivision = service.removeFromHierarchy(sessionId, reportingDivision.getCode(), district.getCode(), village.getCode());
      
      countryNode = reportingDivision.getRootGeoObjectTypes().get(0);
      provinceNode = countryNode.getChildren().get(0);
      districtNode = provinceNode.getChildren().get(0);
      
      Assert.assertEquals("GeoObjectType \""+DISTRICT_CODE+"\" should have no child", 0, districtNode.getChildren().size());
      
      reportingDivision = service.removeFromHierarchy(sessionId, reportingDivision.getCode(), province.getCode(), district.getCode());
      
      countryNode = reportingDivision.getRootGeoObjectTypes().get(0);
      provinceNode = countryNode.getChildren().get(0);

      Assert.assertEquals("GeoObjectType \""+PROVINCE_CODE+"\" should have no child", 0, provinceNode.getChildren().size());
      
      reportingDivision = service.removeFromHierarchy(sessionId, reportingDivision.getCode(), country.getCode(), province.getCode());
      
      countryNode = reportingDivision.getRootGeoObjectTypes().get(0);
      
      Assert.assertEquals("GeoObjectType \""+COUNTRY_CODE+"\" should have no child", 0, countryNode.getChildren().size());
      
      reportingDivision = service.removeFromHierarchy(sessionId, reportingDivision.getCode(), Universal.ROOT, country.getCode());
      
      Assert.assertEquals("HierarchyType \""+REPORTING_DIVISION_CODE+"\" should not have any GeoObjectTypes in the hierarchy", 0, reportingDivision.getRootGeoObjectTypes().size());
      
    }
    finally
    {
      logOutAdmin(sessionId);
    }

    

    sessionId = this.logInAdmin();
    try
    {
      service.deleteGeoObjectType(sessionId, VILLAGE_CODE);
      
      service.deleteGeoObjectType(sessionId, DISTRICT_CODE);
      
      service.deleteGeoObjectType(sessionId, PROVINCE_CODE);
      
      service.deleteGeoObjectType(sessionId, COUNTRY_CODE);
      
      service.deleteHierarchyType(sessionId, REPORTING_DIVISION_CODE);
      
      service.deleteHierarchyType(sessionId, ADMINISTRATIVE_DIVISION_CODE);
    }
    finally
    {
      logOutAdmin(sessionId);
    }
  }
  
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
   * The hardcoded {@link AllowedIn} and {@link LocatedIn} relationship do not follow the common geo registry convention.
   */
  @Test
  public void testLocatedInCode_To_MdTermRelUniversal()
  {
    String locatedInClassName = LocatedIn.class.getSimpleName();
    
    String mdTermRelUniversalType = ConversionService.buildMdTermRelUniversalKey(locatedInClassName);
    
    Assert.assertEquals("HierarchyCode LocatedIn did not get converted to the AllowedIn Universal relationshipType.", AllowedIn.CLASS, mdTermRelUniversalType);
  }
  
  /** 
   * The hardcoded {@link AllowedIn} and {@link LocatedIn} relationship do not follow the common geo registry convention.
   */
  @Test
  public void testToMdTermRelUniversal_To_HierarchyCode()
  {
    String allowedInClass = AllowedIn.CLASS;
    
    String hierarchyCode = ConversionService.buildHierarchyKeyFromMdTermRelUniversal(allowedInClass);
    
    Assert.assertEquals("AllowedIn relationship type did not get converted into the LocatedIn  hierarchy code", LocatedIn.class.getSimpleName(), hierarchyCode);
  }
  
  /** 
   * The hardcoded {@link AllowedIn} and {@link LocatedIn} relationship do not follow the common geo registry convention.
   */
  @Test
  public void testLocatedInCode_To_MdTermRelGeoEntity()
  {
    String locatedInClassName = LocatedIn.class.getSimpleName();
    
    String mdTermRelGeoEntity = ConversionService.buildMdTermRelGeoEntityKey(locatedInClassName);
    
    Assert.assertEquals("HierarchyCode LocatedIn did not get converted to the AllowedIn Universal relationshipType.", LocatedIn.CLASS, mdTermRelGeoEntity);
  }
  
  /** 
   * The hardcoded {@link AllowedIn} and {@link LocatedIn} relationship do not follow the common geo registry convention.
   */
  @Test
  public void testToMdTermRelGeoEntity_To_HierarchyCode()
  {
    String locatedInClass = LocatedIn.CLASS;
    
    String hierarchyCode = ConversionService.buildHierarchyKeyFromMdTermRelGeoEntity(locatedInClass);
    
    Assert.assertEquals("AllowedIn relationship type did not get converted into the LocatedIn  hierarchy code", LocatedIn.class.getSimpleName(), hierarchyCode);
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
    
//    return SessionFacade.logIn("admin", "_nm8P4gfdWxGqNRQ#8", new Locale[] { CommonProperties.getDefaultLocale() });
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
