package net.geoprism.georegistry;

import net.geoprism.georegistry.service.ConversionService;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.runwaysdk.system.gis.geo.GeoEntity;
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
  public GeoObject getGeoObject(String ref)
  {
    // TODO : Add support for virtual leaf nodes.
    GeoEntity ge = null;
    
    if (ref.startsWith("uid:"))
    {
      String uid = ref.replaceFirst("uid:", "");
      
      ge = GeoEntity.get(uid);
    }
    else
    {
      ge = GeoEntity.getByKey(ref);
    }
    
    GeoObject gobj = this.conversionService.geoEntityToGeoObject(ge);
    
    return gobj;
  }

  public HierarchyType getHierarchyType(String ref)
  {
    MdRelationship mdRel = null;
    
    if (ref.startsWith("uid:"))
    {
      String uid = ref.replaceFirst("uid:", "");
      
      mdRel = MdRelationship.get(uid);
    }
    else
    {
      mdRel = MdRelationship.getByKey(ref);
    }
    
    HierarchyType ht = this.conversionService.mdRelationshipToHierarchyType(mdRel);
    
    return ht;
  }
  
  public boolean isVirtual(GeoObject gobj)
  {
    return false; // TODO
  }
}
