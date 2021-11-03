package net.geoprism.registry.graph.transition;

import java.util.List;

import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;

import net.geoprism.registry.conversion.VertexGeoObjectStrategy;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

public class Transition extends TransitionBase
{
  private static final long serialVersionUID = 1506268214;

  public static enum TransitionImpact {
    PARTIAL, FULL;
  }

  public static enum TransitionType {
    MERGE, SPLIT, REASSIGN,
    UPGRADE_MERGE, UPGRADE_SPLIT, UPGRADE_REASSIGN,
    DOWNGRADE_MERGE, DOWNGRADE_SPLIT, DOWNGRADE_REASSIGN;
  }

  public Transition()
  {
    super();
  }

  public JsonObject toJSON()
  {
    VertexServerGeoObject source = this.getSourceVertex();
    VertexServerGeoObject target = this.getTargetVertex();

    JsonObject object = new JsonObject();
    object.addProperty(OID, this.getOid());
    object.addProperty(Transition.ORDER, this.getOrder());
    object.addProperty("sourceCode", source.getCode());
    object.addProperty("sourceType", source.getType().getCode());
    object.addProperty("sourceText", source.getLabel() + " (" + source.getCode() + ")");
    object.addProperty("targetCode", target.getCode());
    object.addProperty("targetType", target.getType().getCode());
    object.addProperty("targetText", target.getLabel() + " (" + target.getCode() + ")");
    object.addProperty(Transition.TRANSITIONTYPE, this.getTransitionType());
    object.addProperty(Transition.IMPACT, this.getImpact());

    return object;
  }

  @Transaction
  public void apply(TransitionEvent event, int order, VertexServerGeoObject source, VertexServerGeoObject target)
  {
    this.validate(event, source, target);

    this.setOrder(order);
    this.setValue(Transition.SOURCE, source.getVertex());
    this.setValue(Transition.TARGET, target.getVertex());

    super.apply();
  }

  public void setTransitionType(TransitionType value)
  {
    this.setTransitionType(value.name());
  }

  public void setImpact(TransitionImpact value)
  {
    this.setImpact(value.name());
  }

  public VertexServerGeoObject getSourceVertex()
  {
    return getVertex(SOURCE);
  }

  public VertexServerGeoObject getTargetVertex()
  {
    return getVertex(TARGET);
  }

  private VertexServerGeoObject getVertex(String attributeName)
  {
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Transition.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(attributeName);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT expand(" + mdAttribute.getColumnName() + ")");
    statement.append(" FROM :parent");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());
    query.setParameter("parent", this.getRID());

    VertexObject vertex = query.getSingleResult();
    MdVertexDAOIF geoVertex = (MdVertexDAOIF) vertex.getMdClass();

    ServerGeoObjectType type = ServerGeoObjectType.get(geoVertex);

    return new VertexServerGeoObject(type, vertex);
  }

  public void validate(TransitionEvent event, VertexServerGeoObject source, VertexServerGeoObject target)
  {
    ServerGeoObjectType beforeType = ServerGeoObjectType.get(event.getBeforeTypeCode());
    ServerGeoObjectType afterType = ServerGeoObjectType.get(event.getAfterTypeCode());

    List<ServerGeoObjectType> beforeSubtypes = beforeType.getSubtypes();
    List<ServerGeoObjectType> afterSubtypes = afterType.getSubtypes();

    if (! ( beforeSubtypes.contains(source.getType()) || beforeType.getCode().equals(source.getType().getCode()) ))
    {
      // This should be prevented by the front-end
      throw new ProgrammingErrorException("Source type must be a subtype of (" + beforeType.getCode() + ")");
    }

    if (! ( afterSubtypes.contains(target.getType()) || afterType.getCode().equals(target.getType().getCode()) ))
    {
      // This should be prevented by the front-end
      throw new ProgrammingErrorException("Target type must be a subtype of (" + afterType.getCode() + ")");
    }
  }

  public static Transition apply(TransitionEvent event, int order, JsonObject object)
  {
    Transition transition = object.has(OID) ? Transition.get(object.get(OID).getAsString()) : new Transition();
    transition.setTransitionType(object.get(Transition.TRANSITIONTYPE).getAsString());
    transition.setImpact(object.get(Transition.IMPACT).getAsString());

    if (transition.isNew())
    {
      transition.setValue(Transition.EVENT, event);
    }

    String sourceCode = object.get("sourceCode").getAsString();
    String sourceType = object.get("sourceType").getAsString();

    String targetCode = object.get("targetCode").getAsString();
    String targetType = object.get("targetType").getAsString();

    VertexServerGeoObject source = new VertexGeoObjectStrategy(ServerGeoObjectType.get(sourceType)).getGeoObjectByCode(sourceCode);
    VertexServerGeoObject target = new VertexGeoObjectStrategy(ServerGeoObjectType.get(targetType)).getGeoObjectByCode(targetCode);

    transition.apply(event, order, source, target);

    return transition;
  }

  @Transaction
  public static void removeAll(ServerGeoObjectType type)
  {
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Transition.CLASS);
    MdAttributeDAOIF sourceAttribute = mdVertex.definesAttribute(Transition.SOURCE);
    MdAttributeDAOIF targetAttribute = mdVertex.definesAttribute(Transition.TARGET);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" WHERE " + sourceAttribute.getColumnName() + ".@class = :vertexClass");
    statement.append(" OR " + targetAttribute.getColumnName() + ".@class = :vertexClass");

    GraphQuery<Transition> query = new GraphQuery<Transition>(statement.toString());
    query.setParameter("vertexClass", type.getMdVertex().getDBClassName());

    List<Transition> results = query.getResults();
    results.forEach(event -> event.delete());
  }
}
