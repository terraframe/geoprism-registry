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

import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.request.ServiceFactory;

public class GeoObjectCache extends LRUCache<String, ServerGeoObjectIF>
{
  public static final String         SEPARATOR = "$@~";

  private GeoObjectBusinessServiceIF objectService;

  public GeoObjectCache()
  {
    this(10000);
  }

  public GeoObjectCache(int cacheSize)
  {
    super(cacheSize);

    init();
  }

  private void init()
  {
    this.objectService = ServiceFactory.getBean(GeoObjectBusinessServiceIF.class);
  }

  public Optional<ServerGeoObjectIF> get(String code, String typeCode)
  {
    return this.get(typeCode + SEPARATOR + code);
  }

  public ServerGeoObjectIF getByCode(String code, String typeCode)
  {
    return get(code, typeCode).orElse(null);
  }

  public ServerGeoObjectIF getOrFetchByCode(String code, String typeCode)
  {
    return this.get(code, typeCode).orElseGet(() -> {
      ServerGeoObjectIF object = this.objectService.getGeoObjectByCode(code, typeCode, true);

      this.put(typeCode + SEPARATOR + code, object);

      return object;
    });
  }
}
