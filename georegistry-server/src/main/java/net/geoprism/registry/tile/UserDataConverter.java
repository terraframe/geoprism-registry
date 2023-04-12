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
package net.geoprism.registry.tile;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.wdtinc.mapbox_vector_tile.VectorTile.Tile.Feature.Builder;
import com.wdtinc.mapbox_vector_tile.adapt.jts.IUserDataConverter;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerProps;

public class UserDataConverter implements IUserDataConverter
{
  @Override
  @SuppressWarnings("unchecked")
  public void addTags(Object userData, MvtLayerProps layerProps, Builder featureBuilder)
  {
    if (userData != null)
    {
      Map<String, Object> data = (Map<String, Object>) userData;
      Set<Entry<String, Object>> entries = data.entrySet();

      for (Entry<String, Object> entry : entries)
      {
        Object value = entry.getValue();
        String key = entry.getKey();

        if (value != null)
        {
          if (value instanceof BigDecimal)
          {
            value = ( (BigDecimal) value ).doubleValue();
          }

          int kIndex = layerProps.addKey(key);
          int vIndex = layerProps.addValue(value);

          featureBuilder.addTags(kIndex);
          featureBuilder.addTags(vIndex);
        }
      }
    }
  }
}
