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
package net.geoprism.registry;

import net.geoprism.registry.conversion.RegistryLocalizedValueConverter;
import net.geoprism.registry.model.ServerHierarchyType;

public class ListTypeHierarchyGroup extends ListTypeHierarchyGroupBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -422963119;

  public ListTypeHierarchyGroup()
  {
    super();
  }

  public ServerHierarchyType getServerHierarchyType()
  {
    return ServerHierarchyType.get(this.getHierarchy());
  }

  public static ListTypeHierarchyGroup create(ListTypeVersion version, ListTypeGroup parent, ServerHierarchyType hierarchy)
  {
    ListTypeHierarchyGroup group = new ListTypeHierarchyGroup();
    group.setVersion(version);
    group.setHierarchy(hierarchy.getHierarchicalRelationshipType());
    group.setParent(parent);

    RegistryLocalizedValueConverter.populate(group.getLabel(), hierarchy.getLabel());

    group.apply();

    return group;
  }
}
