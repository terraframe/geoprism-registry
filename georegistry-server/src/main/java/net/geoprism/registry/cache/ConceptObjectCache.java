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

import net.geoprism.registry.graph.ConceptClass;
import net.geoprism.registry.model.ConceptObject;
import net.geoprism.registry.service.business.ConceptObjectBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptClassBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.view.TypeInfo;

public class ConceptObjectCache
{
  public static final String                          SEPARATOR = "$@~";

  private ConceptObjectBusinessServiceIF              objectService;

  private ConceptClassBusinessServiceIF               typeService;

  private LRUCache<String, CacheEntry<ConceptObject>> cache;

  public ConceptObjectCache()
  {
    this(10000);
  }

  public ConceptObjectCache(int cacheSize)
  {
    this.cache = new LRUCache<>(cacheSize);
  }

  // Lazy load the service
  protected ConceptObjectBusinessServiceIF getObjectService()
  {
    if (this.objectService == null)
    {
      this.objectService = ServiceFactory.getBean(ConceptObjectBusinessServiceIF.class);
    }

    return this.objectService;
  }

  // Lazy load the service
  protected ConceptClassBusinessServiceIF getTypeService()
  {
    if (this.typeService == null)
    {
      this.typeService = ServiceFactory.getBean(ConceptClassBusinessServiceIF.class);
    }

    return this.typeService;
  }

  public Optional<CacheEntry<ConceptObject>> get(String key)
  {
    return this.cache.get(key);
  }

  public Optional<CacheEntry<ConceptObject>> get(String code, String typeCode)
  {
    return get(typeCode + SEPARATOR + code);
  }

  public ConceptObject getByCode(String code, String typeCode)
  {
    return get(code, typeCode).map(CacheEntry::orNull).orElse(null);
  }

  public ConceptObject getOrFetchByCode(String code, String typeCode)
  {
    return this.get(code, typeCode).orElseGet(() -> {
      ConceptClass conceptClass = getTypeService().getByCodeOrThrow(typeCode);

      ConceptObject object = getObjectService().getByCode(conceptClass, code).orElse(null);
      CacheEntry<ConceptObject> entry = new CacheEntry<>(Optional.ofNullable(object));

      this.cache.put(typeCode + SEPARATOR + code, entry);

      return entry;
    }).orNull();
  }

  public ConceptObject getOrFetchByCode(String code, TypeInfo type)
  {
    return this.getOrFetchByCode(code, type.getTypeCode());
  }
}
