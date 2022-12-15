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
package net.geoprism.registry.model.graph;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdVertexDAOIF;

import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.ServerGeoObjectType;

public class ExternalId
{
  public static final String KEY_SEPARATOR = "!~_-";
  
  public static final String CLASS = GeoVertex.EXTERNAL_ID;
  
  public static final String ID = "id";
  
  public static final String KEY = "key";
  
  private EdgeObject edge;
  
  public ExternalId(EdgeObject edge)
  {
    this.edge = edge;
  }
  
  public void apply()
  {
    this.edge.setValue(KEY, this.buildKey());
    
    this.edge.apply();
  }
  
  public String buildKey()
  {
    return this.getExternalId() + KEY_SEPARATOR + this.getParent().getOid() + KEY_SEPARATOR + this.getChild().getType().getMdVertex().getDBClassName();
  }
  
  public VertexServerGeoObject getChild()
  {
    VertexObject child = this.edge.getChild();
    
    ServerGeoObjectType type = ServerGeoObjectType.get((MdVertexDAOIF) child.getMdClass());
    
    return new VertexServerGeoObject(type, child);
  }
  
  public ExternalSystem getParent()
  {
    return (ExternalSystem) this.edge.getParent();
  }
  
  public String getOid()
  {
    return this.edge.getOid();
  }
  
  public String getExternalId()
  {
    return this.edge.getObjectValue("id");
  }
  
  public void setExternalId(String id)
  {
    this.edge.setValue(ID, id);
  }
}
