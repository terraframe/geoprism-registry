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
package net.geoprism.registry.excel;

import java.util.Map;

import net.geoprism.data.importer.FeatureRow;

public class MapFeatureRow implements FeatureRow
{
  private Map<String, Object> row;

  private Long                rowNumber;

  public MapFeatureRow(Map<String, Object> row, Long rowNumber)
  {
    this.row = row;
    this.rowNumber = rowNumber;
  }

  @Override
  public Object getValue(String attributeName)
  {
    return this.row.get(attributeName);
  }

  @Override
  public Long getRowNumber()
  {
    return this.rowNumber;
  }
}
