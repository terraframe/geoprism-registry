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

import java.time.LocalDate;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGraphNode;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.query.graph.VertexAndEdgeQuery.EdgeQueryObject;
import net.geoprism.registry.view.ObjectAtTimeDTO;

public class EdgeView
{
  private String id;
  
  private String source;
  
  private String target;
  
  private String label;
  
  private Date startDate;
  
  private Date endDate;

  public EdgeView(String id, String source, String target, String label, Date startDate, Date endDate)
  {
    super();
    this.id = id;
    this.source = source;
    this.target = target;
    this.label = label;
    this.startDate = startDate;
    this.endDate = endDate;
  }
  
//  public static EdgeView create(BusinessObject source, BusinessObject target)
//  {
//    return new EdgeView("g-" + source.getCode() + "-" + target.getCode(), "g-" + source.getCode(), "g-" + target.getCode(), "");
//  }
//  
//  public static EdgeView create(ServerGeoObjectIF source, BusinessObject target)
//  {
//    return new EdgeView("g-" + source.getUid() + "-" + target.getCode(), "g-" + source.getUid(), "g-" + target.getCode(), "");
//  }
//  
//  public static EdgeView create(BusinessObject source, ServerGeoObjectIF target)
//  {
//    return new EdgeView("g-" + source.getCode() + "-" + target.getUid(), "g-" + source.getCode(), "g-" + target.getUid(), "");
//  }
//  
//  public static EdgeView create(VertexServerGeoObject source, ObjectAtTimeDTO target)
//  {
//    return new EdgeView("g-" + source.getCode() + "-" + target.getCode(), "g-" + source.getCode(), "g-" + target.getCode(), "");
//  }
  
  public static EdgeView create(BusinessObject source, EdgeQueryObject edge)
  {
    return new EdgeView("g-" + edge.getOid(), "g-" + source.getOid(), "g-" + edge.getObject().getOid(), "", edge.getStartDate(), edge.getEndDate());
  }
  
  public static EdgeView create(VertexServerGeoObject source, EdgeQueryObject edge)
  {
    return new EdgeView("g-" + edge.getOid(), "g-" + source.getOid(), "g-" + edge.getObject().getOid(), "", edge.getStartDate(), edge.getEndDate());
  }
  
  public static EdgeView create(ServerGeoObjectIF source, ServerGeoObjectIF target, GraphType graphType, ServerGraphNode node)
  {
    String label = graphType.getLabel().getValue();
    return new EdgeView("g-" + node.getOid(), "g-" + source.getRunwayId(), "g-" + target.getRunwayId(), label == null ? "" : label, node.getStartDate(), node.getEndDate());
  }
  
  public JsonObject toJson()
  {
    Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString())).create();

    return (JsonObject) gson.toJsonTree(this);
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
