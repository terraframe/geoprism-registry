package net.geoprism.georegistry;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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
import com.vividsolutions.jts.util.Assert;

import net.geoprism.georegistry.service.ConversionService;
import net.geoprism.georegistry.service.RegistryService;

public class RegistryServiceTest
{
  private static final String COLORADO_GEOID = "RegistryTest-ColoradoCode";

  private static final String COLORADO_DISPLAY_LABEL = "RegistryTest Colorado Display Label";

  private static final String COLORADO_WKT = "POLYGON ((10000 10000, 12300 40000, 16800 50000, 12354 60000, 13354 60000, 17800 50000, 13300 40000, 11000 10000, 10000 10000))";

  private static final String STATE_CODE = "RegistryTest-StateCode";

  private static final String STATE_DISPLAY_LABEL = "RegistryTest State Display Label";
  
  private static String COLORADO_UID = null;
  
  private static String STATE_UID = null;

  private static Universal state;
  
  private static GeoEntity colorado;
  
  private static RegistryService registryService;
  
  private static ClientSession systemSession   = null;
  
  public static void checkDuplicateClasspathResources()
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
  
  @BeforeClass
  @Request
  public static void classSetUp()
  {
    checkDuplicateClasspathResources();
    
    systemSession = ClientSession.createUserSession("admin", "_nm8P4gfdWxGqNRQ#8", new Locale[] { CommonProperties.getDefaultLocale() });
    
    registryService = new RegistryService(ConversionService.getInstance());
    
    UniversalQuery uq = new UniversalQuery(new QueryFactory());
    uq.WHERE(uq.getKeyName().EQ("RegistryTest-StateCode"));
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
    
    GeoEntityQuery geq = new GeoEntityQuery(new QueryFactory());
    uq.WHERE(uq.getKeyName().EQ("RegistryTest-ColoradoCode"));
    OIterator<? extends GeoEntity> git = geq.getIterator();
    try
    {
      while(it.hasNext())
      {
        git.next().delete();
      }
    }
    finally
    {
      it.close();
    }
    
    state = new Universal();
    state.setUniversalId(STATE_CODE);
    state.getDisplayLabel().setValue(STATE_DISPLAY_LABEL);
    state.apply();
    STATE_UID = state.getOid();
    
    colorado = new GeoEntity();
    colorado.setGeoId(COLORADO_GEOID);
    colorado.getDisplayLabel().setValue(COLORADO_DISPLAY_LABEL);
    colorado.setWkt(COLORADO_WKT);
    colorado.setUniversal(state);
    colorado.apply();
    COLORADO_UID = colorado.getOid();
  }
  
  @AfterClass
  @Request
  public static void classTearDown()
  {
    colorado.delete();
    state.delete();
    systemSession.logout();
  }
  
  @Test
  public void testGetGeoObject()
  {
    GeoObject geoObj = registryService.getGeoObject(systemSession.getSessionId(), COLORADO_UID);
    
    Assert.equals(COLORADO_UID, geoObj.getUid());
    Assert.equals(COLORADO_GEOID, geoObj.getCode());
    Assert.equals(COLORADO_WKT, geoObj.getGeometry().toText());
    Assert.equals(COLORADO_DISPLAY_LABEL, geoObj.getLocalizedDisplayLabel());
    Assert.equals(STATE_CODE, geoObj.getType().getCode());
  }
}
