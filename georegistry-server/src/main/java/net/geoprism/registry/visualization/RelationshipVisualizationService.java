package net.geoprism.registry.visualization;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.HashedMap;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyNode;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.runwaysdk.build.domain.RelationshipVisualizationDataImporter;
import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;

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
  private static final String OUT = "outE";
  
  private static final String IN = "inE";
  
  @Request(RequestType.SESSION)
  public JsonElement fetchGraphVisualizerData(String sessionId, Date date, String geoObjectCode, String geoObjectTypeCode)
  {
//     return JsonParser.parseString("{\"edges\":[{\"id\":\"Canal-Dock9940\",\"label\":\"Adjacent to\",\"source\":\"Canal\",\"target\":\"Dock9940\"},{\"id\":\"Canal-Dock11068\",\"label\":\"Adjacent to\",\"source\":\"Canal\",\"target\":\"Dock11068\"},{\"id\":\"East-Boat-Basin-Dock9942\",\"label\":\"Connected to\",\"source\":\"Dock9942\",\"target\":\"East-Boat-Basin\"},{\"id\":\"East-Boat-Basin-Dock23358\",\"label\":\"Connected to\",\"source\":\"Dock23358\",\"target\":\"East-Boat-Basin\"},{\"id\":\"Dock23358-Project\",\"label\":\"Connected to\",\"source\":\"Dock23358\",\"target\":\"Project\"},{\"id\":\"Dock9942-Project\",\"label\":\"Connected to\",\"source\":\"Dock9942\",\"target\":\"Project\"},{\"id\":\"Dock11068-Project\",\"label\":\"Connected to\",\"source\":\"Dock11068\",\"target\":\"Project\"},{\"id\":\"Dock9940-Project\",\"label\":\"Connected to\",\"source\":\"Dock9940\",\"target\":\"Project\"},{\"id\":\"Dock23358-Site\",\"label\":\"Connected to\",\"source\":\"Dock23358\",\"target\":\"Site\"},{\"id\":\"Dock9942-Site\",\"label\":\"Adjacent to\",\"source\":\"Dock9942\",\"target\":\"Site\"},{\"id\":\"Dock9940-Site\",\"label\":\"Adjacent to\",\"source\":\"Dock9940\",\"target\":\"Site\"},{\"id\":\"Dock11068-Site\",\"label\":\"Adjacent to\",\"source\":\"Dock11068\",\"target\":\"Site\"},{\"id\":\"Canal-Project\",\"label\":\"Adjacent to\",\"source\":\"Canal\",\"target\":\"Project\"},{\"id\":\"Canal-Site\",\"label\":\"Adjacent to\",\"source\":\"Canal\",\"target\":\"Site\"},{\"id\":\"Canal-EastBasin\",\"label\":\"Connected to\",\"source\":\"Canal\",\"target\":\"East-Boat-Basin\"}],\"verticies\":[{\"id\":\"Canal\",\"label\":\"Cape Cod Canal (channel)\"},{\"id\":\"East-Boat-Basin\",\"label\":\"East Boat Basin\"},{\"id\":\"Dock9940\",\"label\":\"Dock 9940\"},{\"id\":\"Dock11068\",\"label\":\"Dock 11068\"},{\"id\":\"Dock9942\",\"label\":\"Dock 9942\"},{\"id\":\"Dock23358\",\"label\":\"Dock 23358\"},{\"id\":\"Project\",\"label\":\"Cape Code Canal Project 2357\"},{\"id\":\"Site\",\"label\":\"Cape Cod Canal Site CAPECO-SI-168116\"}],\"clusters\":[{\"id\":\"c1\",\"label\":\"Canals and Docks\",\"childNodeIds\":[\"Canal\",\"Dock\"]}]}");
    
    JsonObject view = new JsonObject();
    
    JsonArray jaEdges = new JsonArray();
    view.add("edges", jaEdges);
    
    JsonArray jaVerticies = new JsonArray();
    view.add("verticies", jaVerticies);
    
    ServerGeoObjectType type = ServiceFactory.getMetadataCache().getGeoObjectType(geoObjectTypeCode).get();
    VertexServerGeoObject rootGo = (VertexServerGeoObject) ServiceFactory.getGeoObjectService().getGeoObjectByCode(geoObjectCode, type);
    
    JsonObject joVertex = new JsonObject();
    joVertex.addProperty("id", "g-" + rootGo.getUid());
    joVertex.addProperty("code", rootGo.getCode());
    joVertex.addProperty("label", rootGo.getDisplayLabel().getValue());
    jaVerticies.add(joVertex);
    
    Set<String> setEdges = new HashSet<String>();
    Set<String> setVerticies = new HashSet<String>();
    
    MdEdgeDAO mdeConnectedTo = (MdEdgeDAO) MdEdgeDAO.getMdEdgeDAO(RelationshipVisualizationDataImporter.MDEDGE_PACKAGE + "." + RelationshipVisualizationDataImporter.MDEDGE_CONNECTED_TO);
    internalFetchRelationshipData(IN, false, rootGo, mdeConnectedTo, date, jaEdges, jaVerticies, setEdges, setVerticies);
    internalFetchRelationshipData(OUT, false, rootGo, mdeConnectedTo, date, jaEdges, jaVerticies, setEdges, setVerticies);
    
    MdEdgeDAO mdeAdjacentTo = (MdEdgeDAO) MdEdgeDAO.getMdEdgeDAO(RelationshipVisualizationDataImporter.MDEDGE_PACKAGE + "." + RelationshipVisualizationDataImporter.MDEDGE_ADJACENT_TO);
    internalFetchRelationshipData(OUT, false, rootGo, mdeAdjacentTo, date, jaEdges, jaVerticies, setEdges, setVerticies);
    internalFetchRelationshipData(IN, false, rootGo, mdeAdjacentTo, date, jaEdges, jaVerticies, setEdges, setVerticies);
    
    return view;
  }
  
  @Request(RequestType.SESSION)
  public JsonElement fetchHierarchyVisualizerData(String sessionId, Date date, String hierarchyCode, String geoObjectCode, String geoObjectTypeCode)
  {
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
//      Iterator<HierarchyNode> it = ht.getAllNodesIterator();
//      
//      HierarchyNode root = it.next();
//      
//      GeoObjectType rootGot = root.getGeoObjectType();
//      ServerGeoObjectType serverType = ServerGeoObjectType.get(rootGot);
//      
//      VertexServerGeoObject rootGo = (VertexServerGeoObject) new VertexGeoObjectQuery(serverType, null).getSingleResult();
      
      ServerGeoObjectType type = ServiceFactory.getMetadataCache().getGeoObjectType(geoObjectTypeCode).get();
      VertexServerGeoObject rootGo = (VertexServerGeoObject) ServiceFactory.getGeoObjectService().getGeoObjectByCode(geoObjectCode, type);
      
      JsonObject joVertex = new JsonObject();
      joVertex.addProperty("id", "g-" + rootGo.getUid());
      joVertex.addProperty("code", rootGo.getCode());
      joVertex.addProperty("label", rootGo.getDisplayLabel().getValue());
      jaVerticies.add(joVertex);
      
      Set<String> setEdges = new HashSet<String>();
      Set<String> setVerticies = new HashSet<String>();
      
      // Out is children
      internalFetchRelationshipData(OUT, false, rootGo, sht.getMdEdge(), date, jaEdges, jaVerticies, setEdges, setVerticies);
      
      // In is parents
      internalFetchRelationshipData(IN, true, rootGo, sht.getMdEdge(), date, jaEdges, jaVerticies, setEdges, setVerticies);
    }
    
    return view;
  }
  
  private void internalFetchRelationshipData(String inOrOut, boolean recursive, VertexServerGeoObject vertexGo, MdEdgeDAOIF mdEdge, Date date, JsonArray jaEdges, JsonArray jaVerticies, Set<String> setEdges, Set<String> setVerticies)
  {
    Map<String, Object> parameters = new HashedMap<String, Object>();
    parameters.put("rid", vertexGo.getVertex().getRID());

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND(" + inOrOut + "(");

    if (mdEdge != null)
    {
      statement.append("'" + mdEdge.getDBClassName() + "'");
    }
    statement.append(")");

    statement.append(") FROM :rid");

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement.toString(), parameters);

    List<EdgeObject> edges = query.getResults();

    for (EdgeObject edge : edges)
    {
      MdEdgeDAOIF mdEdge2 = (MdEdgeDAOIF) edge.getMdClass();

      if (mdEdge2.definesType().equals(mdEdge.definesType()))
      {
        VertexObject relatedVertex;
        if (inOrOut == OUT)
        {
          relatedVertex = edge.getChild();
        }
        else
        {
          relatedVertex = edge.getParent();
        }

        MdVertexDAOIF mdVertex = (MdVertexDAOIF) relatedVertex.getMdClass();

        ServerGeoObjectType childType = ServerGeoObjectType.get(mdVertex);

        VertexServerGeoObject relatedGO = new VertexServerGeoObject(childType, relatedVertex, date);

        if (recursive)
        {
          internalFetchRelationshipData(inOrOut, true, relatedGO, mdEdge2, date, jaEdges, jaVerticies, setEdges, setVerticies);
        }
        
        if (!setVerticies.contains(relatedGO.getCode()))
        {
          JsonObject joVertex = new JsonObject();
          joVertex.addProperty("id", "g-" + relatedGO.getUid());
          joVertex.addProperty("code", relatedGO.getCode());
          joVertex.addProperty("label", relatedGO.getDisplayLabel().getValue());
          jaVerticies.add(joVertex);
          
          setVerticies.add(relatedGO.getCode());
        }
        
        if (!setVerticies.contains(edge.getOid()))
        {
          JsonObject joEdge = new JsonObject();
          joEdge.addProperty("id", "g-" + edge.getOid());
          
          if (inOrOut == OUT)
          {
            joEdge.addProperty("source", "g-" + vertexGo.getUid());
            joEdge.addProperty("target", "g-" + relatedGO.getUid());
          }
          else
          {
            joEdge.addProperty("target", "g-" + vertexGo.getUid());
            joEdge.addProperty("source", "g-" + relatedGO.getUid());
          }
          
          joEdge.addProperty("label", mdEdge.getDisplayLabel(Session.getCurrentLocale()));
          jaEdges.add(joEdge);
        }
      }
    }
  }
}
