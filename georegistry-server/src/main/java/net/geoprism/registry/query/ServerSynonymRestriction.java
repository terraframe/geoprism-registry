/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.query;

import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.query.graph.VertexGeoObjectQuery;
import net.geoprism.registry.query.graph.VertexGeoObjectRestriction;
import net.geoprism.registry.query.graph.VertexSynonymRestriction;
import net.geoprism.registry.query.postgres.LeafGeoObjectQuery;
import net.geoprism.registry.query.postgres.LeafGeoObjectRestriction;
import net.geoprism.registry.query.postgres.SynonymRestriction;
import net.geoprism.registry.query.postgres.TreeGeoObjectQuery;
import net.geoprism.registry.query.postgres.TreeGeoObjectRestriction;

public class ServerSynonymRestriction implements ServerGeoObjectRestriction
{
  private String              label;

  private ServerGeoObjectIF   parent;

  private ServerHierarchyType hierarchyType;

  public ServerSynonymRestriction(String label)
  {
    this.label = label;
    this.parent = null;
    this.hierarchyType = null;
  }

  public ServerSynonymRestriction(String label, ServerGeoObjectIF parent, ServerHierarchyType hierarchyType)
  {
    this.label = label;
    this.parent = parent;
    this.hierarchyType = hierarchyType;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public ServerGeoObjectIF getParent()
  {
    return parent;
  }

  public void setParent(ServerGeoObjectIF parent)
  {
    this.parent = parent;
  }

  public ServerHierarchyType getHierarchyType()
  {
    return hierarchyType;
  }

  public void setHierarchyType(ServerHierarchyType hierarchyType)
  {
    this.hierarchyType = hierarchyType;
  }

  @Override
  public TreeGeoObjectRestriction create(TreeGeoObjectQuery query)
  {
    return new SynonymRestriction(this.label, this.parent, this.hierarchyType);
  }

  @Override
  public LeafGeoObjectRestriction create(LeafGeoObjectQuery query)
  {
    return new SynonymRestriction(this.label, this.parent, this.hierarchyType);
  }

  @Override
  public VertexGeoObjectRestriction create(VertexGeoObjectQuery query)
  {
    return new VertexSynonymRestriction(this.label, this.parent, this.hierarchyType);
  }
}
