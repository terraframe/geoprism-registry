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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.CustomSerializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.DirectedAcyclicGraphType;
import net.geoprism.registry.UndirectedGraphType;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerChildGraphNode;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerParentGraphNode;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.permission.GeoObjectTypePermissionServiceIF;
import net.geoprism.registry.permission.HierarchyTypePermissionServiceIF;
import net.geoprism.registry.service.ServiceFactory;

public class RelationshipVisualizationService
{
  public static final int maxResults = 100;
  
  public static final String SHOW_BUSINESS_OBJECTS_RELATIONSHIP_TYPE = "BUSINESS";
  
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
        List<GeoObject> parents = getParents(rootGo.getGraphParents(graphType, false, date, boundsWKT), date, false, maxResults);
        geoObjects.addAll(parents);
        
        geoObjects.addAll(getChildren(rootGo.getGraphChildren(graphType, false, date, boundsWKT), date, maxResults - parents.size()));
      }
      else
      {
        List<GeoObject> parents = getParents(rootGo.getGraphParents(graphType, true, date, boundsWKT), date, true, maxResults);
        geoObjects.addAll(parents);
        
        geoObjects.addAll(getChildren(rootGo.getGraphChildren(graphType, false, date, boundsWKT), date, maxResults - parents.size()));
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
  public JsonElement tree(String sessionId, Date date, String relationshipType, String graphTypeCode, String sourceVertex, String boundsWKT)
  {
    final GeoObjectTypePermissionServiceIF typePermissions = ServiceFactory.getGeoObjectTypePermissionService();

    if (!this.validateBounds(boundsWKT)) {
      boundsWKT = null;
    }
    
    final Map<String, VertexView> verticies = new HashMap<String, VertexView>();
    final Map<String, EdgeView> edges = new HashMap<String, EdgeView>();
    final Map<String, JsonObject> relatedTypes = new HashMap<String, JsonObject>();
    
    VertexView sourceView = VertexView.fromJSON(sourceVertex);
    
    if (VertexView.ObjectType.GEOOBJECT.equals(sourceView.getObjectType()))
    {
      final ServerGeoObjectType type = ServiceFactory.getMetadataCache().getGeoObjectType(sourceView.getTypeCode()).get();
  
      if (typePermissions.canRead(type.getOrganization().getCode(), type, type.getIsPrivate()))
      {
        VertexServerGeoObject sourceGO = (VertexServerGeoObject) ServiceFactory.getGeoObjectService().getGeoObjectByCode(sourceView.getCode(), type);
        
        verticies.put(sourceGO.getUid(), VertexView.fromGeoObject(sourceGO, "PARENT"));
        addRelatedType(relatedTypes, type);
        
        if (SHOW_BUSINESS_OBJECTS_RELATIONSHIP_TYPE.equals(relationshipType))
        {
          List<BusinessObject> objects = sourceGO.getBusinessObjects();
          
          int endIndex = (objects.size() > maxResults) ? maxResults : objects.size();
          
          for (int i = 0; i < endIndex; ++i)
          {
            BusinessObject bo = objects.get(i);
            
            if (!verticies.containsKey(bo.getCode()))
            {
              verticies.put(bo.getCode(), VertexView.fromBusinessObject(bo, "CHILD"));
              edges.put(sourceGO.getUid() + "-" + bo.getCode(), EdgeView.create(sourceGO, bo));
              addRelatedType(relatedTypes, bo.getType());
            }
          }
        }
        else
        {
          final GraphType graphType = GraphType.getByCode(relationshipType, graphTypeCode);
    
          if (graphType instanceof UndirectedGraphType)
          {
            // get parent and get children return the same thing for an undirected
            // graph
            fetchChildrenData(false, sourceGO, graphType, date, edges, verticies, relatedTypes, boundsWKT);
          }
          else if(graphType instanceof DirectedAcyclicGraphType)
          {
            // Out is children
            fetchParentsData(false, sourceGO, graphType, date, edges, verticies, relatedTypes, boundsWKT);
            
            // In is parents
            fetchChildrenData(false, sourceGO, graphType, date, edges, verticies, relatedTypes, boundsWKT);
          }
          else
          {
            // Out is children
            fetchParentsData(true, sourceGO, graphType, date, edges, verticies, relatedTypes, boundsWKT);
    
            // In is parents
            fetchChildrenData(false, sourceGO, graphType, date, edges, verticies, relatedTypes, boundsWKT);
          }
        }
      }
    }
    else if (VertexView.ObjectType.BUSINESS.equals(sourceView.getObjectType()))
    {
      final BusinessType type = BusinessType.getByCode(sourceView.getTypeCode());
      
      if (true) // TODO check permissions
      {
        final BusinessObject source = BusinessObject.getByCode(type, sourceView.getCode());
        final BusinessEdgeType edgeType = BusinessEdgeType.getByCode(graphTypeCode);
        
        verticies.put(source.getCode(), VertexView.fromBusinessObject(source, "PARENT"));
        addRelatedType(relatedTypes, type);
        
        // Parents
        List<BusinessObject> objects = source.getParents(edgeType);
        int capacity = maxResults;
        int endIndex = (objects.size() > capacity) ? capacity : objects.size();
        
        for (int i = 0; i < endIndex; ++i)
        {
          BusinessObject bo = objects.get(i);
          
          if (!verticies.containsKey(bo.getCode()))
          {
            verticies.put(bo.getCode(), VertexView.fromBusinessObject(bo, "PARENT"));
            edges.put(source.getCode() + "-" + bo.getCode(), EdgeView.create(source, bo));
            addRelatedType(relatedTypes, bo.getType());
          }
        }
        
        // Children
        objects = source.getChildren(edgeType);
        capacity = maxResults - verticies.size();
        endIndex = (objects.size() > capacity) ? capacity : objects.size();
        
        for (int i = 0; i < endIndex; ++i)
        {
          BusinessObject bo = objects.get(i);
          
          if (!verticies.containsKey(bo.getCode()))
          {
            verticies.put(bo.getCode(), VertexView.fromBusinessObject(bo, "CHILD"));
            edges.put(source.getCode() + "-" + bo.getCode(), EdgeView.create(source, bo));
            addRelatedType(relatedTypes, bo.getType());
          }
        }
      }
    }
    
    JsonObject view = new JsonObject();
    
    JsonArray jaVerticies = new JsonArray();
    verticies.values().stream().sorted((a,b) -> a.getLabel().compareTo(b.getLabel())).forEach(vertex -> jaVerticies.add(vertex.toJson()));
    view.add("verticies", jaVerticies);
    
    JsonArray jaEdges = new JsonArray();
    edges.values().stream().sorted((a,b) -> a.getLabel().compareTo(b.getLabel())).forEach(edge -> jaEdges.add(edge.toJson()));
    view.add("edges", jaEdges);
    
    JsonArray jaRelatedTypes = new JsonArray();
    relatedTypes.values().stream().forEach(relatedType -> jaRelatedTypes.add(relatedType));
    view.add("relatedTypes", jaRelatedTypes);

    return view;
  }
  
  private void addRelatedType(Map<String, JsonObject> relatedTypes, BusinessType type)
  {
    JsonObject view = new JsonObject();
    view.addProperty("code", type.getCode());
    view.addProperty("label", type.getLabel().getValue());
    relatedTypes.put(type.getCode(), view);
  }
  
  private void addRelatedType(Map<String, JsonObject> relatedTypes, ServerGeoObjectType type)
  {
    JsonObject view = new JsonObject();
    view.addProperty("code", type.getCode());
    view.addProperty("label", type.getLabel().getValue());
    relatedTypes.put(type.getCode(), view);
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
  public JsonElement getRelationshipTypes(String sessionId, VertexView.ObjectType objectType, String typeCode)
  {
    JsonArray views = new JsonArray();

    // Hierarchy relationships
    final HierarchyTypePermissionServiceIF htpService = ServiceFactory.getHierarchyPermissionService();

    if (objectType.equals(VertexView.ObjectType.GEOOBJECT))
    {
      ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);
      type.getHierarchies().stream().filter(htp -> {
        return htpService.canRead(htp.getOrganizationCode());
      }).forEach(graphType -> {
  
        JsonObject jo = new JsonObject();
        jo.addProperty("oid", graphType.getCode());
        jo.addProperty("code", graphType.getCode());
        jo.add("label", graphType.getLabel().toJSON());
        jo.addProperty("isHierarchy", true);
  
        views.add(jo);
  
      });
  
      // Non-hierarchy relationships
      UndirectedGraphType.getAll().forEach(graphType -> {
        JsonObject jo = graphType.toJSON();
        jo.addProperty("isHierarchy", false);
  
        views.add(jo);
      });
  
      DirectedAcyclicGraphType.getAll().forEach(graphType -> {
        JsonObject jo = graphType.toJSON();
        jo.addProperty("isHierarchy", false);
  
        views.add(jo);
      });
      
      // Show all business objects which are related to a Geo-Object
      JsonObject jo = new JsonObject();
      jo.addProperty("oid", SHOW_BUSINESS_OBJECTS_RELATIONSHIP_TYPE);
      jo.addProperty("code", SHOW_BUSINESS_OBJECTS_RELATIONSHIP_TYPE);
      jo.addProperty(UndirectedGraphType.TYPE, SHOW_BUSINESS_OBJECTS_RELATIONSHIP_TYPE);
      jo.add("label", new LocalizedValue(LocalizationFacade.localize("graph.visualizer.showBusinessObjects")).toJSON());
      jo.addProperty("isHierarchy", false);
      views.add(jo);
    }
    else if (objectType.equals(VertexView.ObjectType.BUSINESS))
    {
      BusinessEdgeType.getAll().forEach(edgeType -> {
        JsonObject jo = new JsonObject();
        jo.addProperty("oid", edgeType.getOid());
        jo.addProperty("code", edgeType.getCode());
        jo.add("label", edgeType.getLabel().toJSON());
        jo.addProperty("isHierarchy", false);
        views.add(jo);
      });
    }

    return views;
  }

  private void fetchParentsData(boolean recursive, VertexServerGeoObject vertexGo, GraphType graphType, Date date, Map<String, EdgeView> edges, Map<String, VertexView> verticies, Map<String, JsonObject> relatedTypes, String boundsWKT)
  {
    ServerParentGraphNode node = vertexGo.getGraphParents(graphType, recursive, date, boundsWKT);

    processParentNode(node, graphType, edges, verticies, relatedTypes);
  }

  private void processParentNode(ServerParentGraphNode root, GraphType graphType, Map<String, EdgeView> edges, Map<String, VertexView> verticies, Map<String, JsonObject> relatedTypes)
  {
    final ServerGeoObjectIF sourceGO = root.getGeoObject();

    root.getParents().forEach(node -> {

      if (node.getOid() != null)
      {
        ServerGeoObjectIF targetGO = node.getGeoObject();

        if (!verticies.containsKey(targetGO.getUid()))
        {
          verticies.put(targetGO.getUid(), VertexView.fromGeoObject(targetGO, "PARENT"));

          addRelatedType(relatedTypes, targetGO.getType());
        }

        if (!edges.containsKey(sourceGO.getUid() + "-" + targetGO.getUid()))
        {
          edges.put(sourceGO.getUid() + "-" + targetGO.getUid(), EdgeView.create(sourceGO, targetGO, graphType, node));
        }
      }

      this.processParentNode(node, graphType, edges, verticies, relatedTypes);
    });
  }

  private void fetchChildrenData(boolean recursive, VertexServerGeoObject vertexGo, GraphType graphType, Date date, Map<String, EdgeView> edges, Map<String, VertexView> verticies, Map<String, JsonObject> relatedTypes, String boundsWKT)
  {
    ServerChildGraphNode node = vertexGo.getGraphChildren(graphType, recursive, date, boundsWKT);

    this.processChildNode(node, graphType, edges, verticies, relatedTypes);
  }

  private void processChildNode(ServerChildGraphNode root, GraphType graphType, Map<String, EdgeView> edges, Map<String, VertexView> verticies, Map<String, JsonObject> relatedTypes)
  {
    final ServerGeoObjectIF sourceGO = root.getGeoObject();

    root.getChildren().forEach(node -> {

      if (node.getOid() != null)
      {
        ServerGeoObjectIF targetGO = node.getGeoObject();

        if (!verticies.containsKey(targetGO.getUid()))
        {
          verticies.put(targetGO.getUid(), VertexView.fromGeoObject(targetGO, "CHILD"));

          addRelatedType(relatedTypes, targetGO.getType());
        }

        if (!edges.containsKey(sourceGO.getUid() + "-" + targetGO.getUid()))
        {
          edges.put(sourceGO.getUid() + "-" + targetGO.getUid(), EdgeView.create(sourceGO, targetGO, graphType, node));
        }
      }

      this.processChildNode(node, graphType, edges, verticies, relatedTypes);
    });
  }
}
