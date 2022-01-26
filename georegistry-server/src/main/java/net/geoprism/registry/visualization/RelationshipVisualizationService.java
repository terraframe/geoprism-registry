package net.geoprism.registry.visualization;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.HashedMap;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.runwaysdk.build.domain.RelationshipVisualizationDataImporter;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.metadata.MdEdge;
import com.runwaysdk.system.metadata.MdEdgeQuery;
import com.runwaysdk.system.metadata.MetadataDisplayLabel;

import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.hierarchy.HierarchyService;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.permission.GeoObjectTypePermissionServiceIF;
import net.geoprism.registry.permission.HierarchyTypePermissionServiceIF;
import net.geoprism.registry.permission.RolePermissionService;
import net.geoprism.registry.service.ServiceFactory;

public class RelationshipVisualizationService
{
  private static final String OUT = "outE";
  
  private static final String IN = "inE";
  
  @Request(RequestType.SESSION)
  public JsonElement tree(String sessionId, Date date, String mdEdgeOid, String geoObjectCode, String geoObjectTypeCode)
  {
    final GeoObjectTypePermissionServiceIF typePermissions = ServiceFactory.getGeoObjectTypePermissionService();
    
    final MdEdgeDAOIF mdEdgeDAO = MdEdgeDAO.get(mdEdgeOid);
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
      internalFetchRelationshipData(OUT, false, rootGo, mdEdgeDAO, date, jaEdges, jaVerticies, setEdges, setVerticies);
      
      // In is parents
      internalFetchRelationshipData(IN, true, rootGo, mdEdgeDAO, date, jaEdges, jaVerticies, setEdges, setVerticies);
    }
    
    return view;
  }
  
  @Request(RequestType.SESSION)
  public JsonElement getRelationships(String sessionId, String geoObjectTypeCode)
  {
    JsonArray view = new JsonArray();
    
    // Hierarchy relationships
    final JsonArray ja = new HierarchyService().getHierarchiesForType(sessionId, geoObjectTypeCode, false);
    
    for (int i = 0; i < ja.size(); ++i)
    {
      final JsonObject jo = ja.get(i).getAsJsonObject();
      
      final String code = jo.get("code").getAsString();
      
      ServerHierarchyType sht = ServiceFactory.getMetadataCache().getHierachyType(code).get();
      
      view.add(this.serializeMdEdge(sht.getMdEdge(), true, sht.getDisplayLabel()));
    }
    
    // Non-hierarchy relationships
    MdEdgeQuery query = new MdEdgeQuery(new QueryFactory());
    
    query.WHERE(query.getPackageName().EQ(RelationshipVisualizationDataImporter.MDEDGE_PACKAGE));
    
    OIterator<? extends MdEdge> it = query.getIterator();
    
    while (it.hasNext())
    {
      final MdEdge mdEdge = it.next();
      final MdEdgeDAO mdEdgeDAO = (MdEdgeDAO) BusinessFacade.getEntityDAO(mdEdge);
      
      view.add(this.serializeMdEdge(mdEdgeDAO, false, mdEdge.getDisplayLabel()));
    }
    
    return view;
  }
  
  private JsonElement serializeMdEdge(MdEdgeDAOIF mdEdge, boolean isHierarchy, MetadataDisplayLabel label)
  {
    JsonObject jo = new JsonObject();
    
    jo.addProperty("oid", mdEdge.getOid());
    jo.add("label", LocalizedValueConverter.convert(label).toJSON());
    jo.addProperty("isHierarchy", isHierarchy);
    
    return jo;
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
          jaVerticies.add(serializeVertex(relatedGO));
          
          setVerticies.add(relatedGO.getCode());
        }
        
        if (!setVerticies.contains(edge.getOid()))
        {
          if (inOrOut == OUT)
          {
            jaEdges.add(serializeEdge(vertexGo, relatedGO, mdEdge, edge));
          }
          else
          {
            jaEdges.add(serializeEdge(relatedGO, vertexGo, mdEdge, edge));
          }
        }
      }
    }
  }
  
  private JsonObject serializeEdge(VertexServerGeoObject source, VertexServerGeoObject target, MdEdgeDAOIF mdEdge, EdgeObject edge)
  {
    JsonObject joEdge = new JsonObject();
    joEdge.addProperty("id", "g-" + edge.getOid());
    joEdge.addProperty("source", "g-" + source.getUid());
    joEdge.addProperty("target", "g-" + target.getUid());
    
    String label = mdEdge.getDisplayLabel(Session.getCurrentLocale());
    joEdge.addProperty("label", label == null ? "" : label); // If we write an object with a null label ngx graph freaks out. So we're just going to write "" instead.
    
    return joEdge;
  }
  
  private JsonObject serializeVertex(VertexServerGeoObject vertex)
  {
    JsonObject joVertex = new JsonObject();
    joVertex.addProperty("id", "g-" + vertex.getUid());
    joVertex.addProperty("code", vertex.getCode());
    joVertex.addProperty("typeCode", vertex.getType().getCode());
    
    String label = vertex.getDisplayLabel().getValue();
    joVertex.addProperty("label", label == null ? "" : label); // If we write an object with a null label ngx graph freaks out. So we're just going to write "" instead.
    
    return joVertex;
  }
}
