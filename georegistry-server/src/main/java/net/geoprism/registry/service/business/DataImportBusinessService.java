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
package net.geoprism.registry.service.business;

import java.util.HashMap;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.runwaysdk.localization.LocalizationFacade;

import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.graph.ObjectClass;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.io.view.ImportColumnDTO;
import net.geoprism.registry.io.view.ImportTypeDTO;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.ObjectClassIF;

public abstract class DataImportBusinessService
{
  protected ImportTypeDTO getType(ServerGeoObjectType geoObjectType)
  {
    final boolean includeCoordinates = geoObjectType.getGeometryType().equals(GeometryType.POINT) //
        || geoObjectType.getGeometryType().equals(GeometryType.MULTIPOINT) //
        || geoObjectType.getGeometryType().equals(GeometryType.MIXED);

    return getType(geoObjectType, includeCoordinates);

  }

  protected ImportTypeDTO getType(ServerGeoObjectType geoObjectType, boolean includeCoordinates)
  {
    ImportTypeDTO type = ImportConfiguration.toTypeDTO(geoObjectType, new HashMap<>());

    if (includeCoordinates)
    {
      ImportColumnDTO latitude = new ImportColumnDTO();
      latitude.setCode(GeoObjectImportConfiguration.LATITUDE);
      latitude.setBaseType(GeoObjectImportConfiguration.NUMERIC);
      latitude.setLabel(new LocalizedValue(LocalizationFacade.localize(GeoObjectImportConfiguration.LATITUDE_KEY)));

      ImportColumnDTO longitude = new ImportColumnDTO();
      longitude.setCode(GeoObjectImportConfiguration.LONGITUDE);
      longitude.setBaseType(GeoObjectImportConfiguration.NUMERIC);
      longitude.setLabel(new LocalizedValue(LocalizationFacade.localize(GeoObjectImportConfiguration.LONGITUDE_KEY)));

      type.getAttributes().add(0, latitude);
      type.getAttributes().add(0, longitude);
    }

    return type;
  }

  protected ImportTypeDTO getType(ObjectClass type)
  {
    return ImportConfiguration.toTypeDTO(type, new HashMap<>());
  }

  protected ImportTypeDTO getType(ObjectClassIF type)
  {
    if (type instanceof ServerGeoObjectType)
    {
      return this.getType((ServerGeoObjectType) type);
    }

    return this.getType((ObjectClass) type);
  }
}
