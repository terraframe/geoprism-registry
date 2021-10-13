package net.geoprism.registry.graph;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;

import net.geoprism.registry.Organization;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.view.JsonSerializable;
import net.geoprism.registry.view.Page;

public class TransitionEvent extends TransitionEventBase implements JsonSerializable
{
  public static String      TRANSITION_ASSIGNMENT = "net.geoprism.registry.graph.TransitionAssignment";

  private static final long serialVersionUID      = 112753140;

  public TransitionEvent()
  {
    super();
  }

  @Override
  @Transaction
  public void delete()
  {
    this.getTransitions().forEach(t -> t.delete());

    super.delete();
  }

  public List<Transition> getTransitions()
  {
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(TRANSITION_ASSIGNMENT);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT expand(out('" + mdEdge.getDBClassName() + "'))");
    statement.append(" FROM :parent");

    GraphQuery<Transition> query = new GraphQuery<Transition>(statement.toString());
    query.setParameter("parent", this.getRID());

    List<Transition> results = query.getResults();
    return results;
  }

  @Transaction
  public void addTransition(Transition transition)
  {
    EdgeObject targetEdge = this.addChild(transition, MdEdgeDAO.getMdEdgeDAO(TRANSITION_ASSIGNMENT));
    targetEdge.apply();
  }

  @Transaction
  public void addTransition(ServerGeoObjectIF source, ServerGeoObjectIF target, String transitionType)
  {
    String typeCode = this.getTypeCode();

    ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);
    List<ServerGeoObjectType> subtypes = type.getSubtypes();

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

    Transition transition = new Transition();
    transition.setTransitionType(transitionType);
    transition.apply((VertexServerGeoObject) source, (VertexServerGeoObject) target);

    this.addTransition(transition);
  }

  @Override
  public JsonObject toJSON()
  {
    return this.toJSON(false);
  }

  public JsonObject toJSON(boolean includeTransitions)
  {
    DateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
    LocalizedValue localizedValue = LocalizedValueConverter.convert(this.getEmbeddedComponent(TransitionEvent.DESCRIPTION));
    ServerGeoObjectType type = ServerGeoObjectType.get(this.getTypeCode());

    JsonObject object = new JsonObject();
    object.addProperty(TransitionEvent.OID, this.getOid());
    object.addProperty(TransitionEvent.TYPECODE, type.getCode());
    object.addProperty(TransitionEvent.EVENTDATE, format.format(this.getEventDate()));
    object.addProperty("typeLabel", type.getLabel().getValue());
    object.add(TransitionEvent.DESCRIPTION, localizedValue.toJSON());

    if (includeTransitions)
    {
      JsonArray transitions = this.getTransitions().stream().map(e -> e.toJSON()).collect(() -> new JsonArray(), (array, element) -> array.add(element), (listA, listB) -> listA.addAll(listB));

      object.add("transitions", transitions);
    }

    return object;
  }

  @Transaction
  public static JsonObject apply(JsonObject json)
  {
    try
    {
      String typeCode = json.get(TransitionEvent.TYPECODE).getAsString();
      ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);

      ServiceFactory.getGeoObjectPermissionService().enforceCanWrite(type.getOrganization().getCode(), type);

      DateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
      LocalizedValue description = LocalizedValue.fromJSON(json.get(TransitionEvent.DESCRIPTION).getAsJsonObject());
      TransitionEvent event = json.has(OID) ? TransitionEvent.get(json.get(OID).getAsString()) : new TransitionEvent();
      LocalizedValueConverter.populate(event, TransitionEvent.DESCRIPTION, description);
      event.setEventDate(format.parse(json.get(TransitionEvent.EVENTDATE).getAsString()));
      event.setTypeCode(typeCode);
      event.apply();

      JsonArray transitions = json.get("transitions").getAsJsonArray();

      for (int i = 0; i < transitions.size(); i++)
      {
        JsonObject object = transitions.get(i).getAsJsonObject();

        event.addTransition(Transition.apply(event, object));
      }

      return event.toJSON(false);
    }
    catch (ParseException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public static Long getCount()
  {
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(TransitionEvent.CLASS);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*) FROM " + mdVertex.getDBClassName());

    GraphQuery<Long> query = new GraphQuery<Long>(statement.toString());

    return query.getSingleResult();
  }

  public static Page<TransitionEvent> page(Integer pageSize, Integer pageNumber)
  {
    Long count = getCount();

    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(TransitionEvent.CLASS);
    MdAttributeDAOIF eventDate = mdVertex.definesAttribute(TransitionEvent.EVENTDATE);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" ORDER BY " + eventDate.getColumnName() + " DESC");
    statement.append(" SKIP " + ( ( pageNumber - 1 ) * pageSize ) + " LIMIT " + pageSize);

    GraphQuery<TransitionEvent> query = new GraphQuery<TransitionEvent>(statement.toString());

    return new Page<TransitionEvent>(count, pageNumber, pageSize, query.getResults());
  }

  @Transaction
  public static void removeAll(ServerGeoObjectType type)
  {
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(TransitionEvent.CLASS);
    MdAttributeDAOIF typeCode = mdVertex.definesAttribute(TransitionEvent.TYPECODE);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" WHERE " + typeCode.getColumnName() + " = :typeCode");

    GraphQuery<TransitionEvent> query = new GraphQuery<TransitionEvent>(statement.toString());
    query.setParameter("typeCode", type.getCode());

    List<TransitionEvent> results = query.getResults();
    results.forEach(event -> event.delete());
  }
}
