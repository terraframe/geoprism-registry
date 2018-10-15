package net.geoprism.georegistry;

import java.util.Map;

import org.commongeoregistry.adapter.RegistryInterface;
import org.commongeoregistry.adapter.RegistryServerInterface;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.Attribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;

public class AdapterConverter
{
  private static AdapterConverter instance;
  
  private static RegistryInterface registry;
  
  private AdapterConverter()
  {
    registry = new RegistryServerInterface();
  }
  
  public static synchronized AdapterConverter getInstance()
  {
    if(instance == null)
    {
      instance = new AdapterConverter();
    }
    
    return instance;
  }
  
  public GeoObjectType convertGeoObjectType(Universal uni)
  {
    // TODO : GeometryType is hardcoded
    GeoObjectType geoObjType = new GeoObjectType(uni.getUniversalId(), GeometryType.POLYGON, uni.getDisplayLabel().getValue(), uni.getDescription().getValue(), registry);
    
    return geoObjType;
  }
  
  public GeoObject convertGeoObject(GeoEntity geoEntity)
  {
    GeoObjectType got = convertGeoObjectType(geoEntity.getUniversal());
    
    Map<String, Attribute> attributeMap = GeoObject.buildAttributeMap(got);
    
    // TODO : GeometryType is hardcoded
    GeoObject geoObj = new GeoObject(got, GeometryType.POLYGON, attributeMap);
    
    geoObj.setUid(geoEntity.getOid());
    geoObj.setCode(geoEntity.getGeoId());
    geoObj.setWKTGeometry(geoEntity.getWkt());
    // TODO : Status term?
    // TODO : Type attribute?
    
    return geoObj;
  }
}
