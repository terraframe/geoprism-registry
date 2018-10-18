package net.geoprism.georegistry.service;

import java.util.Map;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.Attribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdRelationship;

public class ConversionService
{
  private RegistryAdapter registry;
  
  public ConversionService(RegistryAdapter registry)
  {
    this.registry = registry;
  }
  
  public RegistryAdapter getRegistry()
  {
    return this.registry;
  }
  
  public void setRegistry(RegistryAdapter registry)
  {
    this.registry = registry;
  }
  
  public HierarchyType mdRelationshipToHierarchyType(MdRelationship mdRel)
  {
    HierarchyType ht = new HierarchyType(mdRel.getKey(), mdRel.getDisplayLabel().getValue(), mdRel.getDescription().getValue());
    
    return ht;
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
    
    // TODO : Status term
//    geoObj.setStatus(this.registry.getMetadataCache().getTerm());
    
    // TODO : Type attribute?
    
    return geoObj;
  }
}
