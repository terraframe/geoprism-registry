/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.etl;

import java.util.LinkedHashMap;
import java.util.Map;

import net.geoprism.registry.BusinessType;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.service.business.BusinessObjectBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.service.request.ServiceFactory;

public class BusinessObjectCache
{
  public static final String                SEPARATOR = "$@~";

  protected BusinessTypeBusinessServiceIF   typeService;

  protected BusinessObjectBusinessServiceIF objectService;

  protected Map<String, BusinessObject>     cache;

  public BusinessObjectCache(int cacheSize)
  {
    this.init(cacheSize);
  }

  public BusinessObjectCache()
  {
    this.init(10000);
  }

  @SuppressWarnings("serial")
  private void init(int cacheSize)
  {
    this.cache = new LinkedHashMap<String, BusinessObject>(cacheSize + 1, .75F, true)
    {
      public boolean removeEldestEntry(@SuppressWarnings("rawtypes") Map.Entry eldest)
      {
        return size() > cacheSize;
      }
    };

    this.typeService = ServiceFactory.getBean(BusinessTypeBusinessServiceIF.class);
    this.objectService = ServiceFactory.getBean(BusinessObjectBusinessServiceIF.class);
  }

  public long getSize()
  {
    return this.cache.size();
  }

  public BusinessObject getByCode(String code, String typeCode)
  {
    return this.cache.get(typeCode + SEPARATOR + code);
  }

  public BusinessObject getOrFetchByCode(String code, String typeCode)
  {
    BusinessObject go = this.cache.get(typeCode + SEPARATOR + code);

    if (go == null)
    {
      BusinessType businessType = this.typeService.getByCode(typeCode);
      go = this.objectService.getByCode(businessType, code);

      this.cache.put(typeCode + SEPARATOR + code, go);
    }

    return go;
  }
}
