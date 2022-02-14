package net.geoprism.registry.visualization;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

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
  @Request(RequestType.SESSION)
  public JsonElement tree(String sessionId, Date date, String relationshipType, String graphTypeCode, String geoObjectCode, String geoObjectTypeCode)
  {
    final GeoObjectTypePermissionServiceIF typePermissions = ServiceFactory.getGeoObjectTypePermissionService();

    final GraphType graphType = GraphType.getByCode(relationshipType, graphTypeCode);
    final ServerGeoObjectType type = ServiceFactory.getMetadataCache().getGeoObjectType(geoObjectTypeCode).get();

    JsonObject view = new JsonObject();

    JsonArray jaEdges = new JsonArray();
    view.add("edges", jaEdges);

    JsonArray jaVerticies = new JsonArray();
    view.add("verticies", jaVerticies);

    if (typePermissions.canRead(type.getOrganization().getCode(), type, type.getIsPrivate()))
    {
      VertexServerGeoObject rootGo = (VertexServerGeoObject) ServiceFactory.getGeoObjectService().getGeoObjectByCode(geoObjectCode, type);

      jaVerticies.add(serializeVertex(rootGo));

      Set<String> setEdges = new HashSet<String>();
      Set<String> setVerticies = new HashSet<String>();

      // Out is children
      fetchParentsData(true, rootGo, graphType, date, jaEdges, jaVerticies, setEdges, setVerticies);

      // In is parents
      fetchChildrenData(false, rootGo, graphType, date, jaEdges, jaVerticies, setEdges, setVerticies);
    }

    return view;
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
      jo.addProperty("isHierarchy", false);

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

  private void fetchParentsData(boolean recursive, VertexServerGeoObject vertexGo, GraphType graphType, Date date, JsonArray jaEdges, JsonArray jaVerticies, Set<String> setEdges, Set<String> setVerticies)
  {
    ServerParentGraphNode node = vertexGo.getGraphParents(graphType, recursive, date);

    processParentNode(node, graphType, jaEdges, jaVerticies, setVerticies);
  }

  private void processParentNode(ServerParentGraphNode root, GraphType graphType, JsonArray jaEdges, JsonArray jaVerticies, Set<String> setVerticies)
  {
    final ServerGeoObjectIF vertexGo = root.getGeoObject();

    root.getParents().forEach(node -> {

      if (node.getOid() != null)
      {
        ServerGeoObjectIF relatedGO = node.getGeoObject();

        if (!setVerticies.contains(relatedGO.getCode()))
        {
          jaVerticies.add(serializeVertex(relatedGO));

          setVerticies.add(relatedGO.getCode());
        }

        if (!setVerticies.contains(node.getOid()))
        {
          jaEdges.add(serializeEdge(relatedGO, vertexGo, graphType, node));
        }
      }

      this.processParentNode(node, graphType, jaEdges, jaVerticies, setVerticies);
    });
  }

  private void fetchChildrenData(boolean recursive, VertexServerGeoObject vertexGo, GraphType graphType, Date date, JsonArray jaEdges, JsonArray jaVerticies, Set<String> setEdges, Set<String> setVerticies)
  {
    ServerChildGraphNode node = vertexGo.getGraphChildren(graphType, recursive, date);

    this.processChildNode(node, graphType, jaEdges, jaVerticies, setVerticies);
  }

  private void processChildNode(ServerChildGraphNode root, GraphType graphType, JsonArray jaEdges, JsonArray jaVerticies, Set<String> setVerticies)
  {
    final ServerGeoObjectIF vertexGo = root.getGeoObject();

    root.getChildren().forEach(node -> {

      if (node.getOid() != null)
      {
        ServerGeoObjectIF relatedGO = node.getGeoObject();

        if (!setVerticies.contains(relatedGO.getCode()))
        {
          jaVerticies.add(serializeVertex(relatedGO));

          setVerticies.add(relatedGO.getCode());
        }

        if (!setVerticies.contains(node.getOid()))
        {
          jaEdges.add(serializeEdge(vertexGo, relatedGO, graphType, node));
        }
      }

      this.processChildNode(node, graphType, jaEdges, jaVerticies, setVerticies);
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

  private JsonObject serializeVertex(ServerGeoObjectIF vertex)
  {
    JsonObject joVertex = new JsonObject();
    joVertex.addProperty("id", "g-" + vertex.getUid());
    joVertex.addProperty("code", vertex.getCode());
    joVertex.addProperty("typeCode", vertex.getType().getCode());

    String label = vertex.getDisplayLabel().getValue();
    joVertex.addProperty("label", label == null ? "" : label); // If we write an
                                                               // object with a
                                                               // null label ngx
                                                               // graph freaks
                                                               // out. So we're
                                                               // just going to
                                                               // write ""
                                                               // instead.

    return joVertex;
  }
}
