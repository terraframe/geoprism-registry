package net.geoprism.registry.graph;

import java.util.List;

import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;

import net.geoprism.registry.conversion.VertexGeoObjectStrategy;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

public class Transition extends TransitionBase
{
  public static String      TRANSITION_SOURCE = "net.geoprism.registry.graph.TransitionSource";

  public static String      TRANSITION_TARGET = "net.geoprism.registry.graph.TransitionTarget";

  private static final long serialVersionUID  = -1384043682;

  public Transition()
  {
    super();
  }

  public JsonObject toJSON()
  {
    VertexServerGeoObject source = this.getSource();
    VertexServerGeoObject target = this.getTarget();

    JsonObject object = new JsonObject();
    object.addProperty(OID, this.getOid());
    object.addProperty("sourceCode", source.getCode());
    object.addProperty("sourceType", source.getType().getCode());
    object.addProperty("sourceText", source.getLabel());
    object.addProperty("targetCode", target.getCode());
    object.addProperty("targetType", target.getType().getCode());
    object.addProperty("targetText", target.getLabel());
    object.addProperty(Transition.TRANSITIONTYPE, this.getTransitionType());

    return object;
  }

  @Transaction
  public void apply(VertexServerGeoObject source, VertexServerGeoObject target)
  {
    boolean isNew = this.isNew();

    super.apply();

    if (isNew)
    {
      EdgeObject sourceEdge = this.addChild(source.getVertex(), MdEdgeDAO.getMdEdgeDAO(TRANSITION_SOURCE));
      sourceEdge.apply();

      EdgeObject targetEdge = this.addChild(target.getVertex(), MdEdgeDAO.getMdEdgeDAO(TRANSITION_TARGET));
      targetEdge.apply();
    }
  }

  public VertexServerGeoObject getSource()
  {
    return getVertex(MdEdgeDAO.getMdEdgeDAO(TRANSITION_SOURCE));
  }

  public VertexServerGeoObject getTarget()
  {
    return getVertex(MdEdgeDAO.getMdEdgeDAO(TRANSITION_TARGET));
  }

  private VertexServerGeoObject getVertex(MdEdgeDAOIF mdEdge)
  {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT expand(out('" + mdEdge.getDBClassName() + "'))");
    statement.append(" FROM :parent");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());
    query.setParameter("parent", this.getRID());

    VertexObject vertex = query.getSingleResult();
    MdVertexDAOIF mdVertex = (MdVertexDAOIF) vertex.getMdClass();

    ServerGeoObjectType type = ServerGeoObjectType.get(mdVertex);

    return new VertexServerGeoObject(type, vertex);
  }

  public static Transition apply(TransitionEvent event, JsonObject object)
  {
    Transition transition = object.has(OID) ? Transition.get(object.get(OID).getAsString()) : new Transition();
    transition.setTransitionType(object.get(Transition.TRANSITIONTYPE).getAsString());

    if (transition.isNew())
    {
      String typeCode = event.getTypeCode();

      ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);
      List<ServerGeoObjectType> subtypes = type.getSubtypes();

      String sourceCode = object.get("sourceCode").getAsString();
      String sourceType = object.get("sourceType").getAsString();

      String targetCode = object.get("targetCode").getAsString();
      String targetType = object.get("targetType").getAsString();

      VertexServerGeoObject source = new VertexGeoObjectStrategy(ServerGeoObjectType.get(sourceType)).getGeoObjectByCode(sourceCode);
      VertexServerGeoObject target = new VertexGeoObjectStrategy(ServerGeoObjectType.get(targetType)).getGeoObjectByCode(targetCode);

      if (! ( subtypes.contains(source.getType()) || type.getCode().equals(source.getType().getCode()) ))
      {
        // This should be prevented by the front-end
        throw new ProgrammingErrorException("Source type must be a subtype of (" + type.getCode() + ")");
      }

      if (! ( subtypes.contains(target.getType()) || type.getCode().equals(target.getType().getCode()) ))
      {
        // This should be prevented by the front-end
        throw new ProgrammingErrorException("Target type must be a subtype of (" + type.getCode() + ")");
      }

      transition.apply(source, target);
    }
    else
    {
      transition.apply();
    }

    return transition;
  }

  @Transaction
  public static void removeAll(ServerGeoObjectType type)
  {
    removeAll(type, TRANSITION_SOURCE);
    removeAll(type, TRANSITION_TARGET);
  }

  @Transaction
  public static void removeAll(ServerGeoObjectType type, String edgeType)
  {
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Transition.CLASS);
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(edgeType);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT expand(out) FROM " + mdEdge.getDBClassName());
    statement.append(" WHERE in.@class = :vertexClass");

    GraphQuery<TransitionEvent> query = new GraphQuery<TransitionEvent>(statement.toString());
    query.setParameter("vertexClass", mdVertex.getDBClassName());

    List<TransitionEvent> results = query.getResults();
    results.forEach(event -> event.delete());
  }

}
