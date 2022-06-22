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
package net.geoprism.registry.visualization;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGraphNode;

public class EdgeView
{
  private String id;
  
  private String source;
  
  private String target;
  
  private String label;

  public EdgeView(String id, String source, String target, String label)
  {
    super();
    this.id = id;
    this.source = source;
    this.target = target;
    this.label = label;
  }
  
  public static EdgeView create(BusinessObject source, BusinessObject target)
  {
    return new EdgeView("g-" + source.getCode() + "-" + target.getCode(), "g-" + source.getCode(), "g-" + target.getCode(), "");
  }
  
  public static EdgeView create(ServerGeoObjectIF source, BusinessObject target)
  {
    return new EdgeView("g-" + source.getUid() + "-" + target.getCode(), "g-" + source.getUid(), "g-" + target.getCode(), "");
  }
  
  public static EdgeView create(BusinessObject source, ServerGeoObjectIF target)
  {
    return new EdgeView("g-" + source.getCode() + "-" + target.getUid(), "g-" + source.getCode(), "g-" + target.getUid(), "");
  }
  
  public static EdgeView create(ServerGeoObjectIF source, ServerGeoObjectIF target, GraphType graphType, ServerGraphNode node)
  {
    String label = graphType.getLabel().getValue();
    return new EdgeView("g-" + node.getOid(), "g-" + source.getUid(), "g-" + target.getUid(), label == null ? "" : label);
  }
  
  public JsonObject toJson()
  {
    GsonBuilder builder = new GsonBuilder();

    return (JsonObject) builder.create().toJsonTree(this);
  }
  
  public static EdgeView fromJSON(String sJson)
  {
    GsonBuilder builder = new GsonBuilder();

    return builder.create().fromJson(sJson, EdgeView.class);
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getSource()
  {
    return source;
  }

  public void setSource(String source)
  {
    this.source = source;
  }

  public String getTarget()
  {
    return target;
  }

  public void setTarget(String target)
  {
    this.target = target;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }
}
