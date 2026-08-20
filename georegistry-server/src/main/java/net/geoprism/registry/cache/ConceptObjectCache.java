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
import net.geoprism.registry.service.business.ConceptClassBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptObjectBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;

public class ConceptObjectCache extends LRUCache<String, ConceptObject>
{
  public static final String               SEPARATOR = "$@~";

  protected ConceptClassBusinessServiceIF  typeService;

  protected ConceptObjectBusinessServiceIF objectService;

  public ConceptObjectCache()
  {
    this(10000);
  }

  public ConceptObjectCache(int cacheSize)
  {
    super(cacheSize);
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

  public Optional<ConceptObject> get(String code, String typeCode)
  {
    return this.get(typeCode + SEPARATOR + code);
  }

  public ConceptObject getByCode(String code, String typeCode)
  {
    return this.get(typeCode + SEPARATOR + code).orElse(null);
  }

  public ConceptObject getOrFetchByCode(String code, String typeCode)
  {
    return this.get(typeCode, code).orElseGet(() -> {
      ConceptClass type = getTypeService().getByCodeOrThrow(typeCode);

      ConceptObject object = getObjectService().getByCode(type, code).orElse(null);

      this.put(typeCode + SEPARATOR + code, object);

      return object;
    });
  }

  public ConceptObject getOrFetchByCode(String code, ConceptClass type)
  {
    return this.get(type.getCode(), code).orElseGet(() -> {

      ConceptObject object = getObjectService().getByCode(type, code).orElse(null);

      this.put(type.getCode() + SEPARATOR + code, object);

      return object;
    });
  }
}
