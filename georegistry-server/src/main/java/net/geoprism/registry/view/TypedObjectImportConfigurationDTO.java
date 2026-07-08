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

public class TypedObjectImportConfigurationDTO extends ImportConfigurationDTO
{
  private ImportTypeDTO      type;

  private List<ExclusionDTO> exclusions;

  public TypedObjectImportConfigurationDTO()
  {
    super();

    this.exclusions = new LinkedList<>();
  }

  public ImportTypeDTO getType()
  {
    return type;
  }

  public void setType(ImportTypeDTO type)
  {
    this.type = type;
  }

  public List<ExclusionDTO> getExclusions()
  {
    return exclusions;
  }

  public void setExclusions(List<ExclusionDTO> exclusions)
  {
    this.exclusions = exclusions;
  }

  public void addExclusion(ExclusionDTO exclusion)
  {
    this.exclusions.add(exclusion);
  }
}
