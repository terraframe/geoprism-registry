/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.etl;

import java.util.LinkedHashMap;
import java.util.Map;

import net.geoprism.registry.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.service.ServiceFactory;

public class GeoObjectCache
{
  public static final String SEPARATOR = "$@~";
  
  protected Map<String, ServerGeoObjectIF> cache;

  private GeoObjectBusinessServiceIF objectService;
  
  public GeoObjectCache(int cacheSize)
  {
    this.init(cacheSize);
  }
  
  public GeoObjectCache()
  {
    this.init(10000);
  }
  
  @SuppressWarnings("serial")
  private void init(int cacheSize)
  {
    this.cache = new LinkedHashMap<String, ServerGeoObjectIF>(cacheSize + 1, .75F, true)
    {
      public boolean removeEldestEntry(@SuppressWarnings("rawtypes") Map.Entry eldest)
      {
        return size() > cacheSize;
      }
    };
    
    this.objectService = ServiceFactory.getBean(GeoObjectBusinessServiceIF.class);

  }
  
  public long getSize()
  {
    return this.cache.size();
  }
  
  public ServerGeoObjectIF getByCode(String code, String typeCode)
  {
    return this.cache.get(typeCode + SEPARATOR + code);
  }
  
  public ServerGeoObjectIF getOrFetchByCode(String code, String typeCode)
  {
    ServerGeoObjectIF go = this.cache.get(typeCode + SEPARATOR + code);
    
    if (go == null)
    {
      go = this.objectService.getGeoObjectByCode(code, typeCode, true);
      
      this.cache.put(typeCode + SEPARATOR + code, go);
    }
    
    return go;
  }
}
