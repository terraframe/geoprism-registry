package net.geoprism.georegistry.service;

import java.util.Map;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.RegistryAdapterServer;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.Attribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;

public class ConversionService
{
  private RegistryAdapter registry;
  
  private static ConversionService instance;
  
  public ConversionService()
  {
    registry = new RegistryAdapterServer();
  }
  
  public static synchronized ConversionService getInstance()
  {
    if(instance == null)
    {
      instance = new ConversionService();
    }
    
    return instance;
  }
  
  public RegistryAdapter getRegistry()
  {
    return this.registry;
  }
  
  public Universal geoObjectTypeToUniversal(GeoObjectType got)
  {
    Universal uni = Universal.getByKey(got.getCode());
    
    return uni;
  }
  
  public GeoObjectType universalToGeoObjectType(Universal uni)
  {
    // TODO : GeometryType is hardcoded
    GeoObjectType geoObjType = new GeoObjectType(uni.getUniversalId(), GeometryType.POLYGON, uni.getDisplayLabel().getValue(), uni.getDescription().getValue(), registry);
    
    return geoObjType;
  }
  
  public GeoObject geoEntityToGeoObject(GeoEntity geoEntity)
  {
    GeoObjectType got = universalToGeoObjectType(geoEntity.getUniversal());
    
    Map<String, Attribute> attributeMap = GeoObject.buildAttributeMap(got);
    
    // TODO : GeometryType is hardcoded
    GeoObject geoObj = new GeoObject(got, GeometryType.POLYGON, attributeMap);
    
    geoObj.setUid(geoEntity.getOid());
    geoObj.setCode(geoEntity.getGeoId());
    geoObj.setWKTGeometry(geoEntity.getWkt());
    geoObj.setLocalizedDisplayLabel(geoEntity.getDisplayLabel().getValue());
    // TODO : Status term?
    // TODO : Type attribute?
    
    return geoObj;
  }
}
