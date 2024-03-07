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
package net.geoprism.registry.service.request;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

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
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessObjectBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.service.permission.GeoObjectPermissionServiceIF;
import net.geoprism.registry.service.permission.GeoObjectTypePermissionServiceIF;
import net.geoprism.registry.service.permission.HierarchyTypePermissionServiceIF;
import net.geoprism.registry.visualization.EdgeView;
import net.geoprism.registry.visualization.VertexView;
import net.geoprism.registry.visualization.VertexView.ObjectType;

@Service
public class RelationshipVisualizationService
{
  // Usability really degrades past 500 or so. Past 1000 the browser falls over,
  // even on good computers. @rrowlands
  public static final long                  maxResults                              = 500;

  public static final String                SHOW_BUSINESS_OBJECTS_RELATIONSHIP_TYPE = "BUSINESS";

  public static final String                SHOW_GEOOBJECTS_RELATIONSHIP_TYPE       = "GEOOBJECT";

  @Autowired
  private BusinessObjectBusinessServiceIF   bObjectService;
  
  @Autowired
  private GeoObjectBusinessServiceIF        geoObjectService;

  @Autowired
  private BusinessTypeBusinessServiceIF     bTypeService;

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF bEdgeService;

  @Autowired
  private RegistryComponentService          service;

  @Autowired
  private GeoObjectBusinessServiceIF        objectService;

  @Autowired
  private GeoObjectTypeBusinessServiceIF    typeService;

  @Autowired
  private GeoObjectPermissionServiceIF      objectPermissions;

  @Autowired
  private GeoObjectTypePermissionServiceIF  typePermissions;

  @Autowired
  private HierarchyTypePermissionServiceIF  hierarchyPermissions;

  @Request(RequestType.SESSION)
  public JsonElement treeAsGeoJson(String sessionId, Date date, String relationshipType, String graphTypeCode, String sourceVertex, String boundsWKT)
  {
    final CustomSerializer serializer = this.service.serializer(sessionId);
    if (!this.validateBounds(boundsWKT))
    {
      boundsWKT = null;
    }

    final List<GeoObject> geoObjects = new LinkedList<GeoObject>();

    final VertexView sourceView = this.fromJSON(sourceVertex);

    // 1. Build a list of all related objects
    if (VertexView.ObjectType.GEOOBJECT.equals(sourceView.getObjectType()))
    {
      final ServerGeoObjectType type = ServerGeoObjectType.get(sourceView.getTypeCode());

      if (this.typePermissions.canRead(type.getOrganization().getCode(), type, type.getIsPrivate()))
      {
        VertexServerGeoObject rootGo = (VertexServerGeoObject) geoObjectService.getGeoObjectByCode(sourceView.getCode(), type);

        if (SHOW_BUSINESS_OBJECTS_RELATIONSHIP_TYPE.equals(relationshipType))
        {
          throw new UnsupportedOperationException("Cannot render business objects.");
        }
        else
        {
          final GraphType graphType = GraphType.getByCode(relationshipType, graphTypeCode);

          geoObjects.add(this.objectService.toGeoObject(rootGo, date));

          if (graphType instanceof UndirectedGraphType)
          {
            // get parent and get children return the same thing for an
            // undirected graph
            geoObjects.addAll(getChildren(rootGo.getGraphChildren(graphType, false, date, boundsWKT, null, maxResults), date));
          }
          else if (graphType instanceof DirectedAcyclicGraphType)
          {
            List<GeoObject> parents = getParents(rootGo.getGraphParents(graphType, false, date, boundsWKT, null, maxResults), date, false);
            geoObjects.addAll(parents);

            geoObjects.addAll(getChildren(rootGo.getGraphChildren(graphType, false, date, boundsWKT, null, maxResults - parents.size()), date));
          }
          else
          {
            List<GeoObject> parents = getParents(rootGo.getGraphParents(graphType, true, date, boundsWKT, null, maxResults), date, true);
            geoObjects.addAll(parents);

            geoObjects.addAll(getChildren(rootGo.getGraphChildren(graphType, false, date, boundsWKT, null, maxResults - parents.size()), date));
          }
        }
      }
    }
    else if (VertexView.ObjectType.BUSINESS.equals(sourceView.getObjectType()))
    {
      if (SHOW_GEOOBJECTS_RELATIONSHIP_TYPE.equals(relationshipType))
      {
        final BusinessType type = this.bTypeService.getByCode(sourceView.getTypeCode());

        if (canReadBusinessData(type))
        {
          final BusinessObject selected = this.bObjectService.getByCode(type, sourceView.getCode());

          List<VertexServerGeoObject> objects = this.bObjectService.getGeoObjects(selected);

          long endIndex = Math.min(maxResults, objects.size());

          for (int i = 0; i < endIndex; ++i)
          {
            VertexServerGeoObject object = objects.get(i);

            geoObjects.add(this.objectService.toGeoObject(object, date));
          }
        }
      }
      else
      {
        throw new UnsupportedOperationException("Cannot render business objects.");
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

    if (!this.validateBounds(boundsWKT))
    {
      boundsWKT = null;
    }

    final Map<String, VertexView> verticies = new HashMap<String, VertexView>();
    final Map<String, EdgeView> edges = new HashMap<String, EdgeView>();
    final Map<String, JsonObject> relatedTypes = new HashMap<String, JsonObject>();

    final VertexView sourceView = this.fromJSON(sourceVertex);

    if (VertexView.ObjectType.GEOOBJECT.equals(sourceView.getObjectType()))
    {
      final ServerGeoObjectType type = ServerGeoObjectType.get(sourceView.getTypeCode());

      if (objectPermissions.canRead(type.getOrganization().getCode(), type))
      {
        VertexServerGeoObject selected = (VertexServerGeoObject) geoObjectService.getGeoObjectByCode(sourceView.getCode(), type);

        verticies.put(selected.getUid(), this.fromGeoObject(selected, "SELECTED"));
        addRelatedType(relatedTypes, type);

        if (SHOW_BUSINESS_OBJECTS_RELATIONSHIP_TYPE.equals(relationshipType))
        {
          List<BusinessObject> objects = this.objectService.getBusinessObjects(selected);

          long endIndex = Math.min(maxResults, objects.size());

          for (int i = 0; i < endIndex; ++i)
          {
            BusinessObject child = objects.get(i);

            if (!verticies.containsKey(child.getCode()))
            {
              verticies.put(child.getCode(), this.fromBusinessObject(child, "CHILD"));
              EdgeView edge = EdgeView.create(selected, child);
              edges.put(edge.getId(), edge);
              addRelatedType(relatedTypes, child.getType());
            }
          }
        }
        else
        {
          final GraphType graphType = GraphType.getByCode(relationshipType, graphTypeCode);

          if (graphType instanceof UndirectedGraphType)
          {
            // get parent and get children return the same thing for an
            // undirected
            // graph
            fetchChildrenData(false, selected, graphType, date, edges, verticies, relatedTypes, boundsWKT);
          }
          else if (graphType instanceof DirectedAcyclicGraphType)
          {
            // Out is children
            fetchParentsData(false, selected, graphType, date, edges, verticies, relatedTypes, boundsWKT);

            // In is parents
            fetchChildrenData(false, selected, graphType, date, edges, verticies, relatedTypes, boundsWKT);
          }
          else
          {
            // Out is children
            fetchParentsData(true, selected, graphType, date, edges, verticies, relatedTypes, boundsWKT);

            // In is parents
            fetchChildrenData(false, selected, graphType, date, edges, verticies, relatedTypes, boundsWKT);
          }
        }
      }
    }
    else if (VertexView.ObjectType.BUSINESS.equals(sourceView.getObjectType()))
    {
      final BusinessType type = this.bTypeService.getByCode(sourceView.getTypeCode());

      if (canReadBusinessData(type))
      {
        final BusinessObject selected = this.bObjectService.getByCode(type, sourceView.getCode());

        verticies.put(selected.getCode(), this.fromBusinessObject(selected, "SELECTED"));
        addRelatedType(relatedTypes, type);

        if (SHOW_GEOOBJECTS_RELATIONSHIP_TYPE.equals(relationshipType))
        {
          List<VertexServerGeoObject> objects = this.bObjectService.getGeoObjects(selected);

          long endIndex = Math.min(maxResults, objects.size());

          for (int i = 0; i < endIndex; ++i)
          {
            VertexServerGeoObject child = objects.get(i);

            if (!verticies.containsKey(child.getCode()))
            {
              verticies.put(child.getCode(), this.fromGeoObject(child, "CHILD"));
              EdgeView edge = EdgeView.create(selected, child);
              edges.put(edge.getId(), edge);
              addRelatedType(relatedTypes, child.getType());
            }
          }
        }
        else
        {
          final BusinessEdgeType edgeType = this.bEdgeService.getByCode(graphTypeCode);

          // Parents
          List<BusinessObject> objects = this.bObjectService.getParents(selected, edgeType);
          long endIndex = Math.min(maxResults, objects.size());

          for (int i = 0; i < endIndex; ++i)
          {
            BusinessObject parent = objects.get(i);

            if (!verticies.containsKey(parent.getCode()))
            {
              verticies.put(parent.getCode(), this.fromBusinessObject(parent, "PARENT"));
              EdgeView edge = EdgeView.create(parent, selected);
              edges.put(edge.getId(), edge);
              addRelatedType(relatedTypes, parent.getType());
            }
          }

          // Children
          objects = this.bObjectService.getChildren(selected, edgeType);
          endIndex = Math.min(maxResults - verticies.size(), objects.size());

          for (int i = 0; i < endIndex; ++i)
          {
            BusinessObject child = objects.get(i);

            if (!verticies.containsKey(child.getCode()))
            {
              verticies.put(child.getCode(), this.fromBusinessObject(child, "CHILD"));
              EdgeView edge = EdgeView.create(selected, child);
              edges.put(edge.getId(), edge);
              addRelatedType(relatedTypes, child.getType());
            }
          }
        }
      }
    }

    JsonObject view = new JsonObject();

    JsonArray jaVerticies = new JsonArray();
    verticies.values().stream().sorted((a, b) -> a.getLabel().compareTo(b.getLabel())).forEachOrdered(vertex -> jaVerticies.add(vertex.toJson()));
    view.add("verticies", jaVerticies);

    JsonArray jaEdges = new JsonArray();
    edges.values().stream().sorted((a, b) -> a.getLabel().compareTo(b.getLabel())).forEachOrdered(edge -> jaEdges.add(edge.toJson()));
    view.add("edges", jaEdges);

    JsonArray jaRelatedTypes = new JsonArray();
    relatedTypes.values().stream().forEach(relatedType -> jaRelatedTypes.add(relatedType));
    view.add("relatedTypes", jaRelatedTypes);

    return view;
  }

  private boolean canReadBusinessData(BusinessType type)
  {
    return true; // TODO
  }

  private void addRelatedType(Map<String, JsonObject> relatedTypes, BusinessType type)
  {
    JsonObject view = new JsonObject();
    view.addProperty("code", type.getCode());
    view.addProperty("label", type.getLabel().getValue());
    view.addProperty("objectType", ObjectType.BUSINESS.name());
    relatedTypes.put(type.getCode(), view);
  }

  private void addRelatedType(Map<String, JsonObject> relatedTypes, ServerGeoObjectType type)
  {
    JsonObject view = new JsonObject();
    view.addProperty("code", type.getCode());
    view.addProperty("label", type.getLabel().getValue());
    view.addProperty("objectType", ObjectType.GEOOBJECT.name());
    relatedTypes.put(type.getCode(), view);
  }

  private boolean validateBounds(String boundsWKT)
  {
    if (boundsWKT == null)
    {
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

      // A lat of 520 is perfectly valid according to jts... So we need to roll
      // our own validation
      Geometry geom = reader.read(boundsWKT);
      Coordinate[] coordinates = geom.getCoordinates();

      for (int i = 0; i < coordinates.length; ++i)
      {
        Coordinate coord = coordinates[i];

        if (coord.x > 180 || coord.x < -180 || coord.y > 90 || coord.y < -90)
        {
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

  public List<GeoObject> getChildren(ServerChildGraphNode node, Date date)
  {
    List<GeoObject> geoObjects = new LinkedList<GeoObject>();

    List<ServerChildGraphNode> children = node.getChildren();

    for (int i = 0; i < children.size(); ++i)
    {
      ServerChildGraphNode child = children.get(i);

      geoObjects.add(this.objectService.toGeoObject(child.getGeoObject(), date));
    }

    return geoObjects;
  }

  public List<GeoObject> getParents(ServerParentGraphNode node, Date date, boolean recursive)
  {
    List<GeoObject> geoObjects = new LinkedList<GeoObject>();

    List<ServerParentGraphNode> parents = node.getParents();

    for (int i = 0; i < parents.size(); ++i)
    {
      ServerParentGraphNode parent = parents.get(i);

      geoObjects.add(this.objectService.toGeoObject(parent.getGeoObject(), date));

      if (recursive)
      {
        geoObjects.addAll(getParents(parent, date, true));
      }
    }

    return geoObjects;
  }

  @Request(RequestType.SESSION)
  public JsonElement getRelationshipTypes(String sessionId, VertexView.ObjectType objectType, String typeCode)
  {
    JsonArray views = new JsonArray();

    if (objectType.equals(VertexView.ObjectType.GEOOBJECT))
    {
      ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);

      this.typeService.getHierarchies(type).stream().filter(htp -> {
        return this.hierarchyPermissions.canRead(htp.getOrganizationCode());
      }).forEach(graphType -> {

        JsonObject jo = new JsonObject();
        jo.addProperty("oid", graphType.getCode());
        jo.addProperty("code", graphType.getCode());
        jo.add("label", graphType.getLabel().toJSON());
        jo.addProperty("layout", "VERTICAL");

        views.add(jo);

      });

      // Non-hierarchy relationships
      UndirectedGraphType.getAll().forEach(graphType -> {
        JsonObject jo = graphType.toJSON();
        jo.addProperty("layout", "HORIZONTAL");

        views.add(jo);
      });

      DirectedAcyclicGraphType.getAll().forEach(graphType -> {
        JsonObject jo = graphType.toJSON();
        jo.addProperty("layout", "HORIZONTAL");

        views.add(jo);
      });

      // Show all business objects which are related to a Geo-Object
      JsonObject jo = new JsonObject();
      jo.addProperty("oid", SHOW_BUSINESS_OBJECTS_RELATIONSHIP_TYPE);
      jo.addProperty("code", SHOW_BUSINESS_OBJECTS_RELATIONSHIP_TYPE);
      jo.addProperty(UndirectedGraphType.TYPE, SHOW_BUSINESS_OBJECTS_RELATIONSHIP_TYPE);
      jo.add("label", new LocalizedValue(LocalizationFacade.localize("graph.visualizer.showBusinessObjects")).toJSON());
      jo.addProperty("layout", "HORIZONTAL");
      views.add(jo);
    }
    else if (objectType.equals(VertexView.ObjectType.BUSINESS))
    {
      this.bTypeService.getAll().forEach(edgeType -> {
        JsonObject jo = new JsonObject();
        jo.addProperty("oid", edgeType.getOid());
        jo.addProperty("code", edgeType.getCode());
        jo.add("label", edgeType.getLabel().toJSON());
        jo.addProperty("layout", "HORIZONTAL");
        views.add(jo);
      });

      // Show all GeoObjects which are related to a Business Object
      JsonObject jo = new JsonObject();
      jo.addProperty("oid", SHOW_GEOOBJECTS_RELATIONSHIP_TYPE);
      jo.addProperty("code", SHOW_GEOOBJECTS_RELATIONSHIP_TYPE);
      jo.addProperty(UndirectedGraphType.TYPE, SHOW_GEOOBJECTS_RELATIONSHIP_TYPE);
      jo.add("label", new LocalizedValue(LocalizationFacade.localize("graph.visualizer.showGeoObjects")).toJSON());
      jo.addProperty("layout", "HORIZONTAL");
      views.add(jo);
    }

    return views;
  }

  private void fetchParentsData(boolean recursive, VertexServerGeoObject vertexGo, GraphType graphType, Date date, Map<String, EdgeView> edges, Map<String, VertexView> verticies, Map<String, JsonObject> relatedTypes, String boundsWKT)
  {
    ServerParentGraphNode node = vertexGo.getGraphParents(graphType, recursive, date, boundsWKT, null, maxResults);

    processParentNode(node, graphType, edges, verticies, relatedTypes);
  }

  private void processParentNode(ServerParentGraphNode root, GraphType graphType, Map<String, EdgeView> edges, Map<String, VertexView> verticies, Map<String, JsonObject> relatedTypes)
  {
    final ServerGeoObjectIF childGO = root.getGeoObject();

    root.getParents().forEach(node -> {

      if (node.getOid() != null)
      {
        ServerGeoObjectIF parentGO = node.getGeoObject();

        if (!verticies.containsKey(parentGO.getUid()))
        {
          verticies.put(parentGO.getUid(), this.fromGeoObject(parentGO, "PARENT"));

          addRelatedType(relatedTypes, parentGO.getType());
        }

        if (!edges.containsKey(childGO.getUid() + "-" + parentGO.getUid()))
        {
          edges.put(parentGO.getUid() + "-" + childGO.getUid(), EdgeView.create(parentGO, childGO, graphType, node));
        }
      }

      this.processParentNode(node, graphType, edges, verticies, relatedTypes);
    });
  }

  private void fetchChildrenData(boolean recursive, VertexServerGeoObject vertexGo, GraphType graphType, Date date, Map<String, EdgeView> edges, Map<String, VertexView> verticies, Map<String, JsonObject> relatedTypes, String boundsWKT)
  {
    ServerChildGraphNode node = vertexGo.getGraphChildren(graphType, recursive, date, boundsWKT, null, maxResults);

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
          verticies.put(targetGO.getUid(), this.fromGeoObject(targetGO, "CHILD"));

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

  public VertexView fromBusinessObject(BusinessObject bo, String relation)
  {
    String label = bo.getLabel();

    return new VertexView(ObjectType.BUSINESS, "g-" + bo.getCode(), bo.getCode(), bo.getType().getCode(), ( label == null || label.length() == 0 ) ? bo.getCode() : label, relation, true);
  }

  public VertexView fromGeoObject(ServerGeoObjectIF go, String relation)
  {
    final ServerGeoObjectType type = go.getType();

    boolean readable = objectPermissions.canRead(type.getOrganization().getCode(), type);

    String label = go.getDisplayLabel().getValue();

    return new VertexView(ObjectType.GEOOBJECT, "g-" + go.getUid(), go.getCode(), go.getType().getCode(), ( label == null || label.length() == 0 ) ? go.getCode() : label, relation, readable);
  }

  public VertexView fromJSON(String sJson)
  {
    GsonBuilder builder = new GsonBuilder();

    return builder.create().fromJson(sJson, VertexView.class);
  }

}
