package net.geoprism.georegistry;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.runwaysdk.ClasspathResource;
import com.runwaysdk.ClientSession;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.UniversalQuery;

import net.geoprism.georegistry.service.RegistryService;

public class USATestData
{
  public static final String COLORADO_GEOID = "RegistryTest-ColoradoCode";

  public static final String COLORADO_DISPLAY_LABEL = "RegistryTest Colorado Display Label";

  public static final String COLORADO_WKT = "POLYGON ((10000 10000, 12300 40000, 16800 50000, 12354 60000, 13354 60000, 17800 50000, 13300 40000, 11000 10000, 10000 10000))";
  
  public static final String WASHINGTON_GEOID = "RegistryTest-WashingtonCode";
  
  public static final String WASHINGTON_WKT = "POLYGON((1 1,5 1,5 5,1 5,1 1),(2 2, 3 2, 3 3, 2 3,2 2))";
  
  public static final String WASHINGTON_DISPLAY_LABEL = "RegistryTest Washington Display Label";
  
  public static final String STATE_CODE = "RegistryTest-StateCode";

  public static final String STATE_DISPLAY_LABEL = "RegistryTest State Display Label";
  
  public static final String STATE_DESCRIPTION = "RegistryTest State Description";
  
  public static final String DISTRICT_CODE = "RegistryTest-DistrictCode";

  public static final String DISTRICT_DISPLAY_LABEL = "RegistryTest District Display Label";
  
  public static final String DISTRICT_DESCRIPTION = "RegistryTest District Description";

  public static String COLORADO_UID = null;
  
  public static String STATE_UID = null;
  
  public static String DISTRICT_UID = null;

  public Universal state; 
  
  public Universal district;
  
  public GeoEntity colorado;
  
  public RegistryService registryService;
  
  public ClientSession systemSession   = null;
  
  public USATestData()
  {
    checkDuplicateClasspathResources();
  }
  
  @Request
  public void setUp()
  {
    cleanUp();
    
    state = new Universal();
    state.setUniversalId(STATE_CODE);
    state.getDisplayLabel().setValue(STATE_DISPLAY_LABEL);
    state.getDescription().setValue(STATE_DESCRIPTION);
    state.apply();
    STATE_UID = state.getOid();
    
    district = new Universal();
    district.setUniversalId(DISTRICT_CODE);
    district.getDisplayLabel().setValue(DISTRICT_DISPLAY_LABEL);
    district.getDescription().setValue(DISTRICT_DESCRIPTION);
    district.apply();
    DISTRICT_UID = state.getOid();
    
    colorado = new GeoEntity();
    colorado.setGeoId(COLORADO_GEOID);
    colorado.getDisplayLabel().setValue(COLORADO_DISPLAY_LABEL);
    colorado.setWkt(COLORADO_WKT);
    colorado.setUniversal(state);
    colorado.apply();
    COLORADO_UID = colorado.getOid();
    
    registryService = new RegistryService();
    systemSession = ClientSession.createUserSession("admin", "_nm8P4gfdWxGqNRQ#8", new Locale[] { CommonProperties.getDefaultLocale() });
  }
  
  @Request
  public void cleanUp()
  {
    deleteGeoEntity(COLORADO_GEOID);
    deleteGeoEntity(WASHINGTON_GEOID);
    deleteUniversal(STATE_CODE);
    deleteUniversal(DISTRICT_CODE);
    
    if (systemSession != null)
    {
      systemSession.logout();
    }
  }
  
  public void deleteUniversal(String key)
  {
    UniversalQuery uq = new UniversalQuery(new QueryFactory());
    uq.WHERE(uq.getKeyName().EQ(key));
    OIterator<? extends Universal> it = uq.getIterator();
    try
    {
      while(it.hasNext())
      {
        it.next().delete();
      }
    }
    finally
    {
      it.close();
    }
  }
  
  public void deleteGeoEntity(String key)
  {
    GeoEntityQuery geq = new GeoEntityQuery(new QueryFactory());
    geq.WHERE(geq.getKeyName().EQ(key));
    OIterator<? extends GeoEntity> git = geq.getIterator();
    try
    {
      while(git.hasNext())
      {
        git.next().delete();
      }
    }
    finally
    {
      git.close();
    }
  }
  
  /**
   * Duplicate resources on the classpath may cause issues. This method checks the runwaysdk directory because conflicts there are most common.
   */
  public void checkDuplicateClasspathResources()
  {
    Set<ClasspathResource> existingResources = new HashSet<ClasspathResource>();
    
    List<ClasspathResource> resources = ClasspathResource.getResourcesInPackage("runwaysdk");
    for (ClasspathResource resource : resources)
    {
      ClasspathResource existingRes = null;
      
      for (ClasspathResource existingResource : existingResources)
      {
        if (existingResource.getAbsolutePath().equals(resource.getAbsolutePath()))
        {
          existingRes = existingResource;
          break;
        }
      }
      
      if (existingRes != null)
      {
        System.out.println("WARNING : resource path [" + resource.getAbsolutePath() + "] is overloaded.  [" + resource.getPackageURL() + "] conflicts with existing resource [" + existingRes.getPackageURL() + "].");
      }
      
      existingResources.add(resource);
    }
  }
}
