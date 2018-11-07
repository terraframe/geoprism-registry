package net.geoprism.georegistry;

import net.geoprism.georegistry.service.ConversionService;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdRelationship;

public class AdapterUtilities
{
  private RegistryAdapter adapter;
  
  private ConversionService conversionService;
  
  public AdapterUtilities(RegistryAdapter adapter, ConversionService conversionService)
  {
    this.adapter = adapter;
    this.conversionService = conversionService;
  }
  
  /**
   * Resolves the given GeoObject reference string into a GeoObject.
   * 
   * @return
   */
  public GeoObject getGeoObjectById(String oid)
  {
    // TODO : virtual leaf nodes
    
    GeoEntity ge = GeoEntity.get(oid);
    
    GeoObject gobj = this.conversionService.geoEntityToGeoObject(ge);
    
    return gobj;
  }

  public HierarchyType getHierarchyTypeById(String oid)
  {
    MdRelationship mdRel = MdRelationship.get(oid);
    
    HierarchyType ht = this.conversionService.mdRelationshipToHierarchyType(mdRel);
    
    return ht;
  }

  public GeoObjectType getGeoObjectTypeById(String id)
  {
    Universal uni = Universal.get(id);
    
    return this.adapter.getMetadataCache().getGeoObjectType(uni.getKey()).get();
  }
}
