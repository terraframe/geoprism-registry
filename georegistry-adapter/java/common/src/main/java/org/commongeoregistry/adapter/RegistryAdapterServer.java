/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Common Geo Registry Adapter(tm).
 *
 * Common Geo Registry Adapter(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Common Geo Registry Adapter(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Common Geo Registry Adapter(tm).  If not, see <http://www.gnu.org/licenses/>.
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
