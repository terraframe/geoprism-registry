package net.geoprism.georegistry.service;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.RegistryAdapterServer;
import org.commongeoregistry.adapter.constants.DefaultTerms;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.runwaysdk.gis.geometry.GeometryHelper;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.UniversalQuery;
import com.runwaysdk.system.gis.geo.WKTParsingProblem;
import com.vividsolutions.jts.geom.Geometry;

public class RegistryService
{
  private static ConversionService conversionService;
  
  private static RegistryAdapter registry;
  
  public RegistryService()
  {
    initialize();
  }
  
  private synchronized void initialize()
  {
    if (RegistryService.registry == null)
    {
      RegistryService.registry = new RegistryAdapterServer();
      
      conversionService = new ConversionService(registry);
      
      refreshMetadataCache();
    }
  }
  
  public static RegistryAdapter getRegistryAdapter()
  {
    return registry;
  }
  
  public void refreshMetadataCache()
  {
    registry.getMetadataCache().clear();
    
    DefaultTerms.buildGeoObjectStatusTree(registry);
    
    QueryFactory qf = new QueryFactory();
    UniversalQuery uq = new UniversalQuery(qf);
    OIterator<? extends Universal> it = uq.getIterator();
    
    try
    {
      while (it.hasNext())
      {
        Universal uni = it.next();
        
        GeoObjectType got = conversionService.universalToGeoObjectType(uni);
        
        registry.getMetadataCache().addGeoObjectType(got);
      }
    }
    finally
    {
      it.close();
    }
    
    // TODO : HierarchyType and Terms
  }
  
  @Request(RequestType.SESSION)
  public GeoObject getGeoObject(String sessionId, String uid)
  {
    GeoEntity geo = GeoEntity.get(uid);
    
    GeoObject geoObject = conversionService.geoEntityToGeoObject(geo);
    
    return geoObject;
  }
  
  @Request(RequestType.SESSION)
  public GeoObject updateGeoObject(String sessionId, String jGeoObj)
  {
    GeoObject geoObject = GeoObject.fromJSON(registry, jGeoObj);
    
    GeoEntity ge;
    if (geoObject.getUid() != null && geoObject.getUid().length() > 0)
    {
      GeoEntityQuery geq = new GeoEntityQuery(new QueryFactory());
      geq.WHERE(geq.getOid().EQ(geoObject.getUid()));
  
      OIterator<? extends GeoEntity> it = geq.getIterator();
      try
      {
        if (it.hasNext())
        {
          ge = it.next();
          ge.appLock();
        }
        else
        {
          ge = new GeoEntity();
        }
      }
      finally
      {
        it.close();
      }
    }
    else
    {
      ge = new GeoEntity();
    }
    
    if (geoObject.getCode() != null)
    {
      ge.setGeoId(geoObject.getCode());
    }
    
    if (geoObject.getLocalizedDisplayLabel() != null)
    {
      ge.getDisplayLabel().setValue(geoObject.getLocalizedDisplayLabel());
    }
    
    if (geoObject.getType() != null)
    {
      GeoObjectType got = geoObject.getType();
      
      Universal inputUni = conversionService.geoObjectTypeToUniversal(got);
      
      if (inputUni != ge.getUniversal())
      {
        ge.setUniversal(inputUni);
      }
    }
    
    org.locationtech.jts.geom.Geometry geom = geoObject.getGeometry();
    if (geom != null)
    {
      try
      {
        String wkt = geom.toText();
        
        GeometryHelper geometryHelper = new GeometryHelper();
        
        Geometry geo = geometryHelper.parseGeometry(wkt);
        ge.setGeoPoint(geometryHelper.getGeoPoint(geo));
        ge.setGeoMultiPolygon(geometryHelper.getGeoMultiPolygon(geo));
        ge.setWkt(wkt);
      }
      catch (Exception e)
      {
        String msg = "Error parsing WKT";
        
        WKTParsingProblem p = new WKTParsingProblem(msg);
        p.setNotification(ge, GeoEntity.WKT);
        p.setReason(e.getLocalizedMessage());
        p.apply();
        p.throwIt();
      }
    }
    
    // TODO : STATUS
    
    ge.apply();
    
    return geoObject;
  }

  @Request(RequestType.SESSION)
  public String[] getUIDS(String sessionId, Integer amount)
  {
    return IdService.getInstance(sessionId).getUIDS(amount);
  }

  @Request(RequestType.SESSION)
  public GeoObjectType[] getGeoObjectTypes(String sessionId, String[] codes)
  {
    GeoObjectType[] gots = new GeoObjectType[codes.length];
    
    for (int i = 0; i < codes.length; ++i)
    {
      gots[i] = registry.getMetadataCache().getGeoObjectType(codes[i]).get();
    }
    
    return gots;
  }
}
