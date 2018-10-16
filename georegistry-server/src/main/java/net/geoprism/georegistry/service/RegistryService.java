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
  private ConversionService conversionService;
  
  private RegistryAdapter registry;
  
  public RegistryService(ConversionService conversionService, RegistryAdapter registry)
  {
    this.conversionService = conversionService;
    this.registry = registry;
  }
  
  public RegistryService()
  {
    this.registry = new RegistryAdapterServer();
    this.conversionService = new ConversionService(registry);
    
    refreshMetadataCache();
  }
  
  public RegistryAdapter getRegistryAdapter()
  {
    return this.registry;
  }
  
  public void refreshMetadataCache()
  {
    this.registry.getMetadataCache().clear();
    
    DefaultTerms.buildGeoObjectStatusTree(this.registry);
    
    QueryFactory qf = new QueryFactory();
    UniversalQuery uq = new UniversalQuery(qf);
    OIterator<? extends Universal> it = uq.getIterator();
    
    try
    {
      while (it.hasNext())
      {
        Universal uni = it.next();
        
        GeoObjectType got = this.conversionService.universalToGeoObjectType(uni);
        
        this.registry.getMetadataCache().addGeoObjectType(got);
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
    
    GeoObject geoObject = this.conversionService.geoEntityToGeoObject(geo);
    
    return geoObject;
  }
  
  @Request(RequestType.SESSION)
  public GeoObject updateGeoObject(String sessionId, String jGeoObj)
  {
    RegistryAdapter registry = this.conversionService.getRegistry();
    
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
      
      Universal inputUni = this.conversionService.geoObjectTypeToUniversal(got);
      
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
}
