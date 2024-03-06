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
package net.geoprism.registry;

import java.util.Collections;
import java.util.Comparator;

import org.commongeoregistry.adapter.constants.DefaultAttribute;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.registry.conversion.RegistryLocalizedValueConverter;
import net.geoprism.registry.masterlist.ColumnFilter;
import net.geoprism.registry.masterlist.ListAttributeGroup;
import net.geoprism.registry.masterlist.ListColumn;
import net.geoprism.registry.model.ServerGeoObjectType;

public class ListTypeGeoObjectTypeGroup extends ListTypeGeoObjectTypeGroupBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 622327581;

  public ListTypeGeoObjectTypeGroup()
  {
    super();
  }

  public ListColumn toColumn(ColumnFilter filter)
  {
    String label = this.getLabel().getValue();

    // Integer level = this.getLevel();
    //
    // if (level != null)
    // {
    // label += " - " + level;
    // }
    //
    ListAttributeGroup column = new ListAttributeGroup(label);

    this.getChildren().forEach(child -> column.add(child.toColumn(filter)));
    this.getAttributes().forEach(child -> column.add(child.toColumn(filter)));

    Collections.sort(column.getColumns(), new Comparator<ListColumn>()
    {

      @Override
      public int compare(ListColumn o1, ListColumn o2)
      {
        if (o1.getName() != null && o1.getName().equals(DefaultAttribute.CODE.getName()))
        {
          return -1;
        }

        if (o2.getName() != null && o2.getName().equals(DefaultAttribute.CODE.getName()))
        {
          return 1;
        }

        return o1.getLabel().compareTo(o2.getLabel());
      }
    });

    if (filter.isValid(column))
    {
      return column;
    }

    return null;
  }

  public static ListTypeGeoObjectTypeGroup create(ListTypeVersion version, ListTypeGroup parent, ServerGeoObjectType type, Integer level)
  {
    ListTypeGeoObjectTypeGroup group = new ListTypeGeoObjectTypeGroup();
    group.setVersion(version);
    group.setGeoObjectType(type.getType());
    group.setLevel(level);
    group.setParent(parent);

    RegistryLocalizedValueConverter.populate(group.getLabel(), type.getLabel());

    group.apply();

    return group;
  }

  public static ListTypeGeoObjectTypeGroup getRoot(ListTypeVersion version, ServerGeoObjectType type)
  {
    ListTypeGeoObjectTypeGroupQuery query = new ListTypeGeoObjectTypeGroupQuery(new QueryFactory());
    query.WHERE(query.getVersion().EQ(version));
    query.AND(query.getParent().EQ((String) null));
    query.AND(query.getGeoObjectType().EQ(type.getOid()));

    try (OIterator<? extends ListTypeGeoObjectTypeGroup> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    return null;
  }

}
