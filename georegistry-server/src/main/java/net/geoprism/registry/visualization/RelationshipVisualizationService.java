package net.geoprism.registry.visualization;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyNode;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.build.domain.RelationshipVisualizationDataImporter;
import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.permission.GeoObjectTypePermissionServiceIF;
import net.geoprism.registry.permission.HierarchyTypePermissionServiceIF;
import net.geoprism.registry.permission.RolePermissionService;
import net.geoprism.registry.query.graph.VertexGeoObjectQuery;
import net.geoprism.registry.service.ServiceFactory;

public class RelationshipVisualizationService
{
  @Request(RequestType.SESSION)
  public JsonElement fetchGraphVisualizerData(String sessionId, Date date, String geoObjectCode, String geoObjectTypeCode)
  {
    // ServerGeoObjectType gotDock = ServiceFactory.getMetadataCache().getGeoObjectType(RelationshipVisualizationDataImporter.GOT_DOCK).get();
    
    return JsonParser.parseString("{\"edges\":[{\"id\":\"Canal-Dock9940\",\"label\":\"Adjacent to\",\"source\":\"Canal\",\"target\":\"Dock9940\"},{\"id\":\"Canal-Dock11068\",\"label\":\"Adjacent to\",\"source\":\"Canal\",\"target\":\"Dock11068\"},{\"id\":\"East-Boat-Basin-Dock9942\",\"label\":\"Connected to\",\"source\":\"Dock9942\",\"target\":\"East-Boat-Basin\"},{\"id\":\"East-Boat-Basin-Dock23358\",\"label\":\"Connected to\",\"source\":\"Dock23358\",\"target\":\"East-Boat-Basin\"},{\"id\":\"Dock23358-Project\",\"label\":\"Connected to\",\"source\":\"Dock23358\",\"target\":\"Project\"},{\"id\":\"Dock9942-Project\",\"label\":\"Connected to\",\"source\":\"Dock9942\",\"target\":\"Project\"},{\"id\":\"Dock11068-Project\",\"label\":\"Connected to\",\"source\":\"Dock11068\",\"target\":\"Project\"},{\"id\":\"Dock9940-Project\",\"label\":\"Connected to\",\"source\":\"Dock9940\",\"target\":\"Project\"},{\"id\":\"Dock23358-Site\",\"label\":\"Connected to\",\"source\":\"Dock23358\",\"target\":\"Site\"},{\"id\":\"Dock9942-Site\",\"label\":\"Adjacent to\",\"source\":\"Dock9942\",\"target\":\"Site\"},{\"id\":\"Dock9940-Site\",\"label\":\"Adjacent to\",\"source\":\"Dock9940\",\"target\":\"Site\"},{\"id\":\"Dock11068-Site\",\"label\":\"Adjacent to\",\"source\":\"Dock11068\",\"target\":\"Site\"},{\"id\":\"Canal-Project\",\"label\":\"Adjacent to\",\"source\":\"Canal\",\"target\":\"Project\"},{\"id\":\"Canal-Site\",\"label\":\"Adjacent to\",\"source\":\"Canal\",\"target\":\"Site\"},{\"id\":\"Canal-EastBasin\",\"label\":\"Connected to\",\"source\":\"Canal\",\"target\":\"East-Boat-Basin\"}],\"verticies\":[{\"id\":\"Canal\",\"label\":\"Cape Cod Canal (channel)\"},{\"id\":\"East-Boat-Basin\",\"label\":\"East Boat Basin\"},{\"id\":\"Dock9940\",\"label\":\"Dock 9940\"},{\"id\":\"Dock11068\",\"label\":\"Dock 11068\"},{\"id\":\"Dock9942\",\"label\":\"Dock 9942\"},{\"id\":\"Dock23358\",\"label\":\"Dock 23358\"},{\"id\":\"Project\",\"label\":\"Cape Code Canal Project 2357\"},{\"id\":\"Site\",\"label\":\"Cape Cod Canal Site CAPECO-SI-168116\"}],\"clusters\":[{\"id\":\"c1\",\"label\":\"Canals and Docks\",\"childNodeIds\":[\"Canal\",\"Dock\"]}]}");
  }
  
  @Request(RequestType.SESSION)
  public JsonElement fetchHierarchyVisualizerData(String sessionId, Date date, String hierarchyCode, String geoObjectCode, String geoObjectTypeCode)
  {
    if (date == null)
    {
      date = new Date();
    }
    
    final HierarchyTypePermissionServiceIF hierarchyPermissions = ServiceFactory.getHierarchyPermissionService();
    final GeoObjectTypePermissionServiceIF typePermissions = ServiceFactory.getGeoObjectTypePermissionService();
    final RolePermissionService rps = ServiceFactory.getRolePermissionService();
    final boolean isSRA = rps.isSRA();

    JsonObject view = new JsonObject();
    
    JsonArray jaEdges = new JsonArray();
    view.add("edges", jaEdges);
    
    JsonArray jaVerticies = new JsonArray();
    view.add("verticies", jaVerticies);
    
    ServerHierarchyType sht = ServiceFactory.getMetadataCache().getHierachyType(hierarchyCode).get();
    
    final String htOrgCode = sht.getOrganizationCode();
    final HierarchyType ht = sht.getType();

    if (hierarchyPermissions.canRead(ht.getOrganizationCode()) && ( isSRA || rps.isRA(htOrgCode) || rps.isRM(htOrgCode) ))
    {
      Iterator<HierarchyNode> it = ht.getAllNodesIterator();
      
      HierarchyNode root = it.next();
      
      GeoObjectType rootGot = root.getGeoObjectType();
      ServerGeoObjectType serverType = ServerGeoObjectType.get(rootGot);
      
      VertexServerGeoObject rootGo = (VertexServerGeoObject) new VertexGeoObjectQuery(serverType, null).getSingleResult();
      
      JsonObject joVertex = new JsonObject();
      joVertex.addProperty("id", "g-" + rootGo.getUid());
      joVertex.addProperty("code", rootGo.getCode());
      joVertex.addProperty("label", rootGo.getDisplayLabel().getValue());
      jaVerticies.add(joVertex);
      
      internalFetchHierarchyVisualizerData(rootGo, sht, date, jaEdges, jaVerticies);
    }
    
    return view;
  }
  
  private void internalFetchHierarchyVisualizerData(VertexServerGeoObject goParent, ServerHierarchyType sht, Date date, JsonArray jaEdges, JsonArray jaVerticies)
  {
    Map<String, Object> parameters = new HashedMap<String, Object>();
    parameters.put("rid", goParent.getVertex().getRID());

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND(outE(");

    if (sht != null)
    {
      statement.append("'" + sht.getMdEdge().getDBClassName() + "'");
    }
    statement.append(")");

    statement.append(") FROM :rid");

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement.toString(), parameters);

    List<EdgeObject> edges = query.getResults();

    for (EdgeObject edge : edges)
    {
      MdEdgeDAOIF mdEdge = (MdEdgeDAOIF) edge.getMdClass();

      if (VertexServerGeoObject.isEdgeAHierarchyType(mdEdge.definesType()))
      {
        VertexObject childVertex = edge.getChild();

        MdVertexDAOIF mdVertex = (MdVertexDAOIF) childVertex.getMdClass();

        ServerHierarchyType ht2 = ServerHierarchyType.get(mdEdge);
        ServerGeoObjectType childType = ServerGeoObjectType.get(mdVertex);

        VertexServerGeoObject child = new VertexServerGeoObject(childType, childVertex, date);

        internalFetchHierarchyVisualizerData(child, ht2, date, jaEdges, jaVerticies);
        
        JsonObject joVertex = new JsonObject();
        joVertex.addProperty("id", "g-" + child.getUid());
        joVertex.addProperty("code", child.getCode());
        joVertex.addProperty("label", child.getDisplayLabel().getValue());
        jaVerticies.add(joVertex);
        
        JsonObject joEdge = new JsonObject();
        joEdge.addProperty("id", "g-" + child.getUid());
        joEdge.addProperty("source", "g-" + goParent.getUid());
        joEdge.addProperty("target", "g-" + child.getUid());
        joEdge.addProperty("label", child.getDisplayLabel().getValue());
        jaEdges.add(joEdge);
      }
    }
  }
}
