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

import org.commongeoregistry.adapter.constants.DefaultAttribute;

import net.geoprism.registry.DataNotFoundException;
import net.geoprism.registry.model.GeoObjectMetadata;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.business.GPRGeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.view.TypeInfo;

public class GeoObjectCache
{
  public static final String                              SEPARATOR = "$@~";

  private GPRGeoObjectBusinessServiceIF                   objectService;

  private LRUCache<String, CacheEntry<ServerGeoObjectIF>> cache;

  public GeoObjectCache()
  {
    this(10000);
  }

  public GeoObjectCache(int cacheSize)
  {
    this.cache = new LRUCache<>(cacheSize);
  }

  // Lazy load the service
  protected GPRGeoObjectBusinessServiceIF getObjectService()
  {
    if (this.objectService == null)
    {
      this.objectService = ServiceFactory.getBean(GPRGeoObjectBusinessServiceIF.class);
    }

    return this.objectService;
  }

  public Optional<CacheEntry<ServerGeoObjectIF>> get(String key)
  {
    return this.cache.get(key);
  }

  public Optional<CacheEntry<ServerGeoObjectIF>> get(String code, String typeCode)
  {
    return get(typeCode + SEPARATOR + code);
  }

  public Optional<CacheEntry<ServerGeoObjectIF>> getByExternalId(String externalId, String typeCode, String authority)
  {
    return this.cache.get(typeCode + SEPARATOR + authority + SEPARATOR + externalId);
  }

  public ServerGeoObjectIF getByCode(String code, String typeCode)
  {
    return get(code, typeCode).map(CacheEntry::orNull).orElse(null);
  }

  public ServerGeoObjectIF getOrFetchByCode(String code, String typeCode)
  {
    return this.get(code, typeCode).orElseGet(() -> {
      ServerGeoObjectIF object = getObjectService().getGeoObjectByCode(code, typeCode, false);
      CacheEntry<ServerGeoObjectIF> entry = new CacheEntry<>(Optional.ofNullable(object));

      this.cache.put(typeCode + SEPARATOR + code, entry);

      return entry;
    }).orElseThrow(() -> {
      DataNotFoundException ex = new DataNotFoundException("Could not find a GeoObject with code [" + code + "].");
      ex.setTypeLabel(GeoObjectMetadata.get().getClassDisplayLabel());
      ex.setDataIdentifier(code);
      ex.setAttributeLabel(GeoObjectMetadata.get().getAttributeDisplayLabel(DefaultAttribute.CODE.getName()));

      return ex;
    });
  }

  public ServerGeoObjectIF getOrFetchByCode(String code, TypeInfo type)
  {
    return this.getOrFetchByCode(code, type.getTypeCode());
  }

  public ServerGeoObjectIF getOrFetchByExternalId(String externalId, TypeInfo type, String authority)
  {
    return this.getOrFetchByExternalId(externalId, type.getTypeCode(), authority);
  }

  public ServerGeoObjectIF getOrFetchByExternalId(String externalId, String typeCode, String authority)
  {
    return this.getByExternalId(externalId, typeCode, authority).orElseGet(() -> {
      Optional<ServerGeoObjectIF> optional = getObjectService().getByExternalId(externalId, authority, ServerGeoObjectType.get(typeCode));
      CacheEntry<ServerGeoObjectIF> entry = new CacheEntry<>(optional);

      this.cache.put(typeCode + SEPARATOR + authority + SEPARATOR + externalId, entry);

      optional.ifPresent(object -> {
        this.cache.put(typeCode + SEPARATOR + object.getCode(), entry);
      });

      return entry;
    }).orElseThrow(() -> {
      DataNotFoundException ex = new DataNotFoundException("Could not find a GeoObject with alternate id [" + externalId + "].");
      ex.setTypeLabel(GeoObjectMetadata.get().getClassDisplayLabel());
      ex.setDataIdentifier(externalId);
      ex.setAttributeLabel(authority);

      return ex;
    });
  }
}
