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
package net.geoprism.registry.cache;

import java.util.Optional;

import net.geoprism.registry.graph.BusinessType;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.service.business.BusinessObjectBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.view.TypeInfo;

public class BusinessObjectCache
{
  public static final String                           SEPARATOR = "$@~";

  private BusinessObjectBusinessServiceIF              objectService;

  private BusinessTypeBusinessServiceIF                typeService;

  private LRUCache<String, CacheEntry<BusinessObject>> cache;

  public BusinessObjectCache()
  {
    this(10000);
  }

  public BusinessObjectCache(int cacheSize)
  {
    this.cache = new LRUCache<>(cacheSize);
  }

  // Lazy load the service
  protected BusinessObjectBusinessServiceIF getObjectService()
  {
    if (this.objectService == null)
    {
      this.objectService = ServiceFactory.getBean(BusinessObjectBusinessServiceIF.class);
    }

    return this.objectService;
  }

  // Lazy load the service
  protected BusinessTypeBusinessServiceIF getTypeService()
  {
    if (this.typeService == null)
    {
      this.typeService = ServiceFactory.getBean(BusinessTypeBusinessServiceIF.class);
    }

    return this.typeService;
  }

  public Optional<CacheEntry<BusinessObject>> get(String key)
  {
    return this.cache.get(key);
  }

  public Optional<CacheEntry<BusinessObject>> get(String code, String typeCode)
  {
    return get(typeCode + SEPARATOR + code);
  }

  public BusinessObject getByCode(String code, String typeCode)
  {
    return get(code, typeCode).map(CacheEntry::orNull).orElse(null);
  }

  public BusinessObject getOrFetchByCode(String code, String typeCode)
  {
    return this.get(code, typeCode).orElseGet(() -> {
      BusinessType businessType = getTypeService().getByCodeOrThrow(typeCode);

      BusinessObject object = getObjectService().getByCode(businessType, code).orElse(null);
      CacheEntry<BusinessObject> entry = new CacheEntry<>(Optional.ofNullable(object));

      this.cache.put(typeCode + SEPARATOR + code, entry);

      return entry;
    }).orNull();
  }

  public BusinessObject getOrFetchByCode(String code, TypeInfo type)
  {
    return this.getOrFetchByCode(code, type.getTypeCode());
  }
}
