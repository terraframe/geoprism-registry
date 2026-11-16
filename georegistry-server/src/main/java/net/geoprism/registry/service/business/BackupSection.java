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

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

/**
 * The kinds of data which can be included in a backup. Each section is written
 * to its own folder in the backup zip, and the restore process skips any
 * section which is missing from the zip.
 */
public enum BackupSection {
  ORGANIZATIONS, METADATA, GEO_OBJECTS, HIERARCHIES, DAGS, UNDIRECTED_GRAPHS, BUSINESS_OBJECTS, BUSINESS_EDGES, EVENTS, LIST_TYPES, USERS;

  public static Set<BackupSection> all()
  {
    return EnumSet.allOf(BackupSection.class);
  }

  /**
   * Parses the given section names. A null or empty collection means every
   * section.
   *
   * @throws IllegalArgumentException
   *           if a name does not match a section
   */
  public static Set<BackupSection> parse(Collection<String> names)
  {
    if (names == null || names.isEmpty())
    {
      return all();
    }

    Set<BackupSection> sections = EnumSet.noneOf(BackupSection.class);

    for (String name : names)
    {
      try
      {
        sections.add(BackupSection.valueOf(name.trim().toUpperCase()));
      }
      catch (IllegalArgumentException e)
      {
        throw new IllegalArgumentException("Unknown backup section [" + name + "]", e);
      }
    }

    return sections;
  }
}
