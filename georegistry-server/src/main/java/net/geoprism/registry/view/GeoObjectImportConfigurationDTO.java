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
package net.geoprism.registry.view;

import java.util.LinkedList;
import java.util.List;

import net.geoprism.registry.etl.ObjectImporterFactory.JobHistoryType;

public class GeoObjectImportConfigurationDTO extends TypedObjectImportConfigurationDTO
{
  private List<LocationDTO> locations;

  private Boolean           postalCode;

  private String            hierarchy;

  private String            revealGeometryColumn;

  public GeoObjectImportConfigurationDTO()
  {
    super();

    this.locations = new LinkedList<LocationDTO>();

    this.setObjectType(JobHistoryType.GEO_OBJECT);
    this.postalCode = false;
  }

  public List<LocationDTO> getLocations()
  {
    return locations;
  }

  public void setLocations(List<LocationDTO> locations)
  {
    this.locations = locations;
  }

  public void addLocation(LocationDTO dto)
  {
    this.locations.add(dto);
  }

  public Boolean getPostalCode()
  {
    return postalCode;
  }

  public void setPostalCode(Boolean postalCode)
  {
    this.postalCode = postalCode;
  }

  public String getHierarchy()
  {
    return hierarchy;
  }

  public void setHierarchy(String hierarchy)
  {
    this.hierarchy = hierarchy;
  }

  public String getRevealGeometryColumn()
  {
    return revealGeometryColumn;
  }

  public void setRevealGeometryColumn(String revealGeometryColumn)
  {
    this.revealGeometryColumn = revealGeometryColumn;
  }
}
