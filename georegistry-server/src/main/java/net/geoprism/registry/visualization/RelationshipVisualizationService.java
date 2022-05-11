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

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.CustomSerializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import net.geoprism.registry.DirectedAcyclicGraphType;
import net.geoprism.registry.UndirectedGraphType;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerChildGraphNode;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerGraphNode;
import net.geoprism.registry.model.ServerParentGraphNode;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.permission.GeoObjectTypePermissionServiceIF;
import net.geoprism.registry.permission.HierarchyTypePermissionServiceIF;
import net.geoprism.registry.service.ServiceFactory;

public class RelationshipVisualizationService
{
  public static final int maxResults = 100;
  
  @Request(RequestType.SESSION)
  public JsonElement treeAsGeoJson(String sessionId, Date date, String relationshipType, String graphTypeCode, String geoObjectCode, String geoObjectTypeCode, String boundsWKT)
  {
    final GeoObjectTypePermissionServiceIF typePermissions = ServiceFactory.getGeoObjectTypePermissionService();
    final ServerGeoObjectType type = ServiceFactory.getMetadataCache().getGeoObjectType(geoObjectTypeCode).get();
    final CustomSerializer serializer = ServiceFactory.getRegistryService().serializer(sessionId);
   
    if (!this.validateBounds(boundsWKT)) {
      boundsWKT = null;
    }
    
    // 1. Build a list of all related objects
    List<GeoObject> geoObjects = new LinkedList<GeoObject>();
    
    if (typePermissions.canRead(type.getOrganization().getCode(), type, type.getIsPrivate()))
    {
      VertexServerGeoObject rootGo = (VertexServerGeoObject) ServiceFactory.getGeoObjectService().getGeoObjectByCode(geoObjectCode, type);

      final GraphType graphType = GraphType.getByCode(relationshipType, graphTypeCode);
      
      geoObjects.add(rootGo.toGeoObject(date));

      if (graphType instanceof UndirectedGraphType)
      {
        // get parent and get children return the same thing for an undirected graph
        geoObjects.addAll(getChildren(rootGo.getGraphChildren(graphType, false, date, boundsWKT), date, maxResults));
      }
      else if(graphType instanceof DirectedAcyclicGraphType)
      {
        geoObjects.addAll(getChildren(rootGo.getGraphChildren(graphType, false, date, boundsWKT), date, maxResults/2));
        geoObjects.addAll(getParents(rootGo.getGraphParents(graphType, false, date, boundsWKT), date, false, maxResults/2));
      }
      else
      {
        geoObjects.addAll(getChildren(rootGo.getGraphChildren(graphType, false, date, boundsWKT), date, maxResults/2));
        geoObjects.addAll(getParents(rootGo.getGraphParents(graphType, true, date, boundsWKT), date, true, maxResults/2));
      }
    }
    
    // 2. Serialize and wrap into a feature collection
    JsonObject featureCollection = new JsonObject();
    featureCollection.addProperty("type", "FeatureCollection");
    
    JsonArray features = new JsonArray();
    
    for (GeoObject object : geoObjects)
    {
      features.add(object.toJSON(serializer));
    }
    
    featureCollection.add("features", features);
    
    return featureCollection;
  }
  
  @Request(RequestType.SESSION)
  public JsonElement tree(String sessionId, Date date, String relationshipType, String graphTypeCode, String geoObjectCode, String geoObjectTypeCode, String boundsWKT)
  {
    final GeoObjectTypePermissionServiceIF typePermissions = ServiceFactory.getGeoObjectTypePermissionService();

    final ServerGeoObjectType type = ServiceFactory.getMetadataCache().getGeoObjectType(geoObjectTypeCode).get();
    
    if (!this.validateBounds(boundsWKT)) {
      boundsWKT = null;
    }
    
    // Get a list of all possible types
    Set<String> relatedTypes = new HashSet<String>();

    JsonObject view = new JsonObject();

    JsonArray jaEdges = new JsonArray();
    view.add("edges", jaEdges);

    JsonArray jaVerticies = new JsonArray();
    view.add("verticies", jaVerticies);

    if (typePermissions.canRead(type.getOrganization().getCode(), type, type.getIsPrivate()))
    {
      VertexServerGeoObject rootGo = (VertexServerGeoObject) ServiceFactory.getGeoObjectService().getGeoObjectByCode(geoObjectCode, type);

      final GraphType graphType = GraphType.getByCode(relationshipType, graphTypeCode);
      
      // jaVerticies.add(serializeVertex(rootGo, (graphType instanceof UndirectedGraphType) ? "PARENT" : "SELECTED"));
      jaVerticies.add(serializeVertex(rootGo, "PARENT"));
      relatedTypes.add(rootGo.getType().getCode());

      Set<String> setEdges = new HashSet<String>();
      Set<String> setVerticies = new HashSet<String>();

      if (graphType instanceof UndirectedGraphType)
      {
        // get parent and get children return the same thing for an undirected
        // graph
        fetchChildrenData(false, rootGo, graphType, date, jaEdges, jaVerticies, setEdges, setVerticies, relatedTypes, boundsWKT);
      }
      else if(graphType instanceof DirectedAcyclicGraphType)
      {
        // Out is children
        fetchParentsData(false, rootGo, graphType, date, jaEdges, jaVerticies, setEdges, setVerticies, relatedTypes, boundsWKT);
        
        // In is parents
        fetchChildrenData(false, rootGo, graphType, date, jaEdges, jaVerticies, setEdges, setVerticies, relatedTypes, boundsWKT);
      }
      else
      {
        // Out is children
        fetchParentsData(true, rootGo, graphType, date, jaEdges, jaVerticies, setEdges, setVerticies, relatedTypes, boundsWKT);

        // In is parents
        fetchChildrenData(false, rootGo, graphType, date, jaEdges, jaVerticies, setEdges, setVerticies, relatedTypes, boundsWKT);
      }
    }
    
    JsonArray jaRelatedTypes = new JsonArray();
    for (String relatedType : relatedTypes) {
      jaRelatedTypes.add(relatedType);
    }
    view.add("relatedTypes", jaRelatedTypes);

    return view;
  }
  
  private boolean validateBounds(String boundsWKT)
  {
    if (boundsWKT == null) {
      return false;
    }
    
    Geometry boundsGeom;
    
    // Validate their bounds otherwise we will throw lots of big scary errors
    WKTReader reader = new WKTReader();
    try
    {
      boundsGeom = reader.read(boundsWKT);
      
      if (!boundsGeom.isValid())
      {
        return false;
      }
      
      // A lat of 520 is perfectly valid according to jts... So we need to roll our own validation
      Geometry geom = reader.read(boundsWKT);
      Coordinate[] coordinates = geom.getCoordinates();

      for (int i = 0; i < coordinates.length; ++i) {
        Coordinate coord = coordinates[i];
        
        if (coord.x > 180 || coord.x < -180 || coord.y > 90 || coord.y < -90) {
          return false;
        }
      }
      
      return true;
    }
    catch (ParseException e)
    {
      return false;
    }
  }
  
  public List<GeoObject> getChildren(ServerChildGraphNode node, Date date, int maxResults)
  {
    List<GeoObject> geoObjects = new LinkedList<GeoObject>();
    
    List<ServerChildGraphNode> children = node.getChildren();
    
    int endIndex = (children.size() > maxResults) ? maxResults : children.size();
    
    for (int i = 0; i < endIndex; ++i)
    {
      ServerChildGraphNode child = children.get(i);
      
      geoObjects.add(child.getGeoObject().toGeoObject(date));
    }
    
    return geoObjects;
  }
  
  public List<GeoObject> getParents(ServerParentGraphNode node, Date date, boolean recursive, int maxResults)
  {
    List<GeoObject> geoObjects = new LinkedList<GeoObject>();
    
    List<ServerParentGraphNode> parents = node.getParents();
    
    int endIndex = (parents.size() > maxResults) ? maxResults : parents.size();
    
    for (int i = 0; i < endIndex; ++i)
    {
      ServerParentGraphNode parent = parents.get(i);
      
      geoObjects.add(parent.getGeoObject().toGeoObject(date));
      
      if (recursive)
      {
        geoObjects.addAll(getParents(parent, date, true, maxResults / 2));
      }
    }
    
    return geoObjects;
  }

  @Request(RequestType.SESSION)
  public JsonElement getRelationshipTypes(String sessionId, String geoObjectTypeCode)
  {
    JsonArray view = new JsonArray();

    // Hierarchy relationships
    final HierarchyTypePermissionServiceIF htpService = ServiceFactory.getHierarchyPermissionService();

    ServerGeoObjectType type = ServerGeoObjectType.get(geoObjectTypeCode);
    type.getHierarchies().stream().filter(htp -> {
      return htpService.canRead(htp.getOrganizationCode());
    }).forEach(graphType -> {

      JsonObject jo = new JsonObject();
      jo.addProperty("oid", graphType.getCode());
      jo.addProperty("code", graphType.getCode());
      jo.add("label", graphType.getLabel().toJSON());
      jo.addProperty("isHierarchy", true);

      view.add(jo);

    });

    // Non-hierarchy relationships
    UndirectedGraphType.getAll().forEach(graphType -> {
      JsonObject jo = graphType.toJSON();
      jo.addProperty("isHierarchy", false);

      view.add(jo);
    });

    DirectedAcyclicGraphType.getAll().forEach(graphType -> {
      JsonObject jo = graphType.toJSON();
      jo.addProperty("isHierarchy", false);

      view.add(jo);
    });

    return view;
  }

  private void fetchParentsData(boolean recursive, VertexServerGeoObject vertexGo, GraphType graphType, Date date, JsonArray jaEdges, JsonArray jaVerticies, Set<String> setEdges, Set<String> setVerticies, Set<String> relatedTypes, String boundsWKT)
  {
    ServerParentGraphNode node = vertexGo.getGraphParents(graphType, recursive, date, boundsWKT);

    processParentNode(node, graphType, jaEdges, jaVerticies, setVerticies, relatedTypes);
  }

  private void processParentNode(ServerParentGraphNode root, GraphType graphType, JsonArray jaEdges, JsonArray jaVerticies, Set<String> setVerticies, Set<String> relatedTypes)
  {
    final ServerGeoObjectIF vertexGo = root.getGeoObject();

    root.getParents().forEach(node -> {

      if (node.getOid() != null)
      {
        ServerGeoObjectIF relatedGO = node.getGeoObject();

        if (!setVerticies.contains(relatedGO.getCode()))
        {
          jaVerticies.add(serializeVertex(relatedGO, "PARENT"));

          setVerticies.add(relatedGO.getCode());
          relatedTypes.add(relatedGO.getType().getCode());
        }

        if (!setVerticies.contains(node.getOid()))
        {
          jaEdges.add(serializeEdge(relatedGO, vertexGo, graphType, node));
        }
      }

      this.processParentNode(node, graphType, jaEdges, jaVerticies, setVerticies, relatedTypes);
    });
  }

  private void fetchChildrenData(boolean recursive, VertexServerGeoObject vertexGo, GraphType graphType, Date date, JsonArray jaEdges, JsonArray jaVerticies, Set<String> setEdges, Set<String> setVerticies, Set<String> relatedTypes, String boundsWKT)
  {
    ServerChildGraphNode node = vertexGo.getGraphChildren(graphType, recursive, date, boundsWKT);

    this.processChildNode(node, graphType, jaEdges, jaVerticies, setVerticies, relatedTypes);
  }

  private void processChildNode(ServerChildGraphNode root, GraphType graphType, JsonArray jaEdges, JsonArray jaVerticies, Set<String> setVerticies, Set<String> relatedTypes)
  {
    final ServerGeoObjectIF vertexGo = root.getGeoObject();

    root.getChildren().forEach(node -> {

      if (node.getOid() != null)
      {
        ServerGeoObjectIF relatedGO = node.getGeoObject();

        if (!setVerticies.contains(relatedGO.getCode()))
        {
          jaVerticies.add(serializeVertex(relatedGO, "CHILD"));

          setVerticies.add(relatedGO.getCode());
          relatedTypes.add(relatedGO.getType().getCode());
        }

        if (!setVerticies.contains(node.getOid()))
        {
          jaEdges.add(serializeEdge(vertexGo, relatedGO, graphType, node));
        }
      }

      this.processChildNode(node, graphType, jaEdges, jaVerticies, setVerticies, relatedTypes);
    });
  }

  private JsonObject serializeEdge(ServerGeoObjectIF source, ServerGeoObjectIF target, GraphType graphType, ServerGraphNode node)
  {
    JsonObject joEdge = new JsonObject();
    joEdge.addProperty("id", "g-" + node.getOid());
    joEdge.addProperty("source", "g-" + source.getUid());
    joEdge.addProperty("target", "g-" + target.getUid());

    String label = graphType.getLabel().getValue();
    joEdge.addProperty("label", label == null ? "" : label); // If we write an
                                                             // object with a
                                                             // null label ngx
                                                             // graph freaks
                                                             // out. So we're
                                                             // just going to
                                                             // write ""
                                                             // instead.

    return joEdge;
  }

  private JsonObject serializeVertex(ServerGeoObjectIF vertex, String relation)
  {
    JsonObject joVertex = new JsonObject();
    joVertex.addProperty("id", "g-" + vertex.getUid());
    joVertex.addProperty("code", vertex.getCode());
    joVertex.addProperty("typeCode", vertex.getType().getCode());

    String label = vertex.getDisplayLabel().getValue();
    joVertex.addProperty("label", (label == null || label.length() == 0) ?  vertex.getCode() : label); // ngx graph freaks out if we put null here

    joVertex.addProperty("relation", relation);
    
    return joVertex;
  }
}
