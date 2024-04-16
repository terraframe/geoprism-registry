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

import net.geoprism.registry.BusinessType;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.service.business.BusinessObjectBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;

public class BusinessObjectCache extends LRUCache<String, BusinessObject>
{
  public static final String                SEPARATOR = "$@~";

  protected BusinessTypeBusinessServiceIF   typeService;

  protected BusinessObjectBusinessServiceIF objectService;

  public BusinessObjectCache()
  {
    this(10000);
  }

  public BusinessObjectCache(int cacheSize)
  {
    super(cacheSize);

    this.init();
  }

  private void init()
  {
    this.typeService = ServiceFactory.getBean(BusinessTypeBusinessServiceIF.class);
    this.objectService = ServiceFactory.getBean(BusinessObjectBusinessServiceIF.class);
  }

  public Optional<BusinessObject> get(String code, String typeCode)
  {
    return this.get(typeCode + SEPARATOR + code);
  }

  public BusinessObject getByCode(String code, String typeCode)
  {
    return this.get(typeCode + SEPARATOR + code).orElse(null);
  }

  public BusinessObject getOrFetchByCode(String code, String typeCode)
  {
    return this.get(typeCode, code).orElseGet(() -> {
      BusinessType businessType = this.typeService.getByCode(typeCode);

      BusinessObject object = this.objectService.getByCode(businessType, code);

      this.put(typeCode + SEPARATOR + code, object);

      return object;
    });
  }

  public BusinessObject getOrFetchByCode(String code, BusinessType type)
  {
    return this.get(type.getCode(), code).orElseGet(() -> {

      BusinessObject object = this.objectService.getByCode(type, code);

      this.put(type.getCode() + SEPARATOR + code, object);

      return object;
    });
  }
}
