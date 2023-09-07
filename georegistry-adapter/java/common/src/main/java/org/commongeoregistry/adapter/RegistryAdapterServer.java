/**
 *
 */
package org.commongeoregistry.adapter;

import java.util.ArrayList;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.id.AdapterIdServiceIF;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.MetadataCache;

/**
 * This class is used to manage the metadata and the {@link GeoObject}s that are managed by an implementation of 
 * the Common Geo-Registry. The {@link MetadataCache} is populated with the {@link GeoObjectType}s and the 
 * {@link HierarchyType}s implemented on the local instance. When remote systems that want to interface with this 
 * implementation of the CommonGeoRegistry
 * 
 * @author nathan
 * @author rrowlands
 *
 */
public class RegistryAdapterServer extends RegistryAdapter
{
  /**
   * 
   */
  private static final long serialVersionUID = -3343727858910300438L;

  public RegistryAdapterServer(AdapterIdServiceIF idService)
  {
    super(idService);
  }
  
  /**
   * 
   * @param codes
   * @return An array of GeoObjectTypes in JSON format.
   */
  public String[] getGeoObjectTypes(String[] codes)
  {
    ArrayList<String> geoObjectTypesJSON = new ArrayList<String>();
    
    for (String code : codes)
    {
      Optional<GeoObjectType> geoObjectType = this.getMetadataCache().getGeoObjectType(code);
      
      if (geoObjectType.isPresent())
      {
        geoObjectTypesJSON.add(geoObjectType.get().toJSON().toString());
      }
    }
    
    return (String[]) geoObjectTypesJSON.toArray();
    
  }
}
