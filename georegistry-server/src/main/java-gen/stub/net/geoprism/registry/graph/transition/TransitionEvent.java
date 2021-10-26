package net.geoprism.registry.graph.transition;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;

import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.graph.transition.Transition.TransitionImpact;
import net.geoprism.registry.graph.transition.Transition.TransitionType;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.view.HistoricalRow;
import net.geoprism.registry.view.JsonSerializable;
import net.geoprism.registry.view.Page;

public class TransitionEvent extends TransitionEventBase implements JsonSerializable
{
  private static final long serialVersionUID = 112753140;

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
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Transition.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(Transition.EVENT);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" WHERE " + mdAttribute.getColumnName() + " = :event");

    GraphQuery<Transition> query = new GraphQuery<Transition>(statement.toString());
    query.setParameter("event", this.getRID());

    List<Transition> results = query.getResults();
    return results;
  }

  @Transaction
  public void addTransition(ServerGeoObjectIF source, ServerGeoObjectIF target, TransitionType transitionType, TransitionImpact impact)
  {
    Transition transition = new Transition();
    transition.setTransitionType(transitionType);
    transition.setImpact(impact);
    transition.setEvent(this);
    transition.apply(this, (VertexServerGeoObject) source, (VertexServerGeoObject) target);
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
    ServerGeoObjectType beforeType = ServerGeoObjectType.get(this.getBeforeTypeCode());
    ServerGeoObjectType afterType = ServerGeoObjectType.get(this.getAfterTypeCode());

    JsonObject object = new JsonObject();
    object.addProperty(TransitionEvent.OID, this.getOid());
    object.addProperty(TransitionEvent.BEFORETYPECODE, beforeType.getCode());
    object.addProperty(TransitionEvent.AFTERTYPECODE, afterType.getCode());
    object.addProperty(TransitionEvent.EVENTDATE, format.format(this.getEventDate()));
    object.addProperty("beforeTypeLabel", beforeType.getLabel().getValue());
    object.addProperty("afterTypeLabel", afterType.getLabel().getValue());
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
      String beforeTypeCode = json.get(TransitionEvent.BEFORETYPECODE).getAsString();
      String afterTypeCode = json.get(TransitionEvent.AFTERTYPECODE).getAsString();
      ServerGeoObjectType beforeType = ServerGeoObjectType.get(beforeTypeCode);
      ServerGeoObjectType afterType = ServerGeoObjectType.get(afterTypeCode);

      ServiceFactory.getGeoObjectPermissionService().enforceCanWrite(beforeType.getOrganization().getCode(), beforeType);
      ServiceFactory.getGeoObjectPermissionService().enforceCanWrite(afterType.getOrganization().getCode(), afterType);

      DateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
      LocalizedValue description = LocalizedValue.fromJSON(json.get(TransitionEvent.DESCRIPTION).getAsJsonObject());
      TransitionEvent event = json.has(OID) ? TransitionEvent.get(json.get(OID).getAsString()) : new TransitionEvent();
      LocalizedValueConverter.populate(event, TransitionEvent.DESCRIPTION, description);
      event.setEventDate(format.parse(json.get(TransitionEvent.EVENTDATE).getAsString()));
      event.setBeforeTypeCode(beforeTypeCode);
      event.setAfterTypeCode(afterTypeCode);
      event.apply();

      JsonArray transitions = json.get("transitions").getAsJsonArray();

      for (int i = 0; i < transitions.size(); i++)
      {
        JsonObject object = transitions.get(i).getAsJsonObject();

        Transition.apply(event, object);
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
    MdAttributeDAOIF beforeTypeCode = mdVertex.definesAttribute(TransitionEvent.BEFORETYPECODE);
    MdAttributeDAOIF afterTypeCode = mdVertex.definesAttribute(TransitionEvent.AFTERTYPECODE);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" WHERE " + beforeTypeCode.getColumnName() + " = :typeCode OR " + afterTypeCode.getColumnName() + " = :typeCode");

    GraphQuery<TransitionEvent> query = new GraphQuery<TransitionEvent>(statement.toString());
    query.setParameter("typeCode", type.getCode());

    List<TransitionEvent> results = query.getResults();
    results.forEach(event -> event.delete());
  }

  public static List<TransitionEvent> getAll(ServerGeoObjectType type)
  {
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(TransitionEvent.CLASS);
    MdAttributeDAOIF beforeTypeCode = mdVertex.definesAttribute(TransitionEvent.BEFORETYPECODE);
    MdAttributeDAOIF afterTypeCode = mdVertex.definesAttribute(TransitionEvent.AFTERTYPECODE);

    List<ServerGeoObjectType> types = new LinkedList<ServerGeoObjectType>();
    types.add(type);
    types.addAll(type.getSubtypes());

    List<String> codes = types.stream().map(t -> type.getCode()).collect(Collectors.toList());

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" WHERE (" + beforeTypeCode.getColumnName() + " IN :typeCode");
    statement.append(" OR " + afterTypeCode.getColumnName() + " IN :typeCode )");

    GraphQuery<TransitionEvent> query = new GraphQuery<TransitionEvent>(statement.toString());
    query.setParameter("typeCode", codes);

    return query.getResults();
  }

  public static List<HistoricalRow> getHistoricalReport(ServerGeoObjectType type, Date startDate, Date endDate)
  {
    MdVertexDAOIF transitionVertex = MdVertexDAO.getMdVertexDAO(Transition.CLASS);
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(TransitionEvent.CLASS);
    MdAttributeDAOIF beforeTypeCode = mdVertex.definesAttribute(TransitionEvent.BEFORETYPECODE);
    MdAttributeDAOIF afterTypeCode = mdVertex.definesAttribute(TransitionEvent.AFTERTYPECODE);
    MdAttributeDAOIF eventDate = mdVertex.definesAttribute(TransitionEvent.EVENTDATE);

    List<ServerGeoObjectType> types = new LinkedList<ServerGeoObjectType>();
    types.add(type);
    types.addAll(type.getSubtypes());

    List<String> codes = types.stream().map(t -> type.getCode()).collect(Collectors.toList());

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT event.oid AS eventId");
    statement.append(", event.eventDate AS eventDate");
    statement.append(", transitionType AS eventType");
    statement.append(", event.description AS description");
    statement.append(", event.beforeTypeCode AS beforeType");
    statement.append(", source.code AS beforeCode");
    statement.append(", source.displayLabel_cot.value[0] AS beforeLabel");
    statement.append(", event.afterTypeCode AS afterType");
    statement.append(", target.code AS afterCode");
    statement.append(", target.displayLabel_cot.value[0] AS afterLabel");
    statement.append(" FROM " + transitionVertex.getDBClassName());
    statement.append(" WHERE ( event." + beforeTypeCode.getColumnName() + " IN :typeCode");
    statement.append(" OR event." + afterTypeCode.getColumnName() + " IN :typeCode )");
    statement.append(" AND event." + eventDate.getColumnName() + " BETWEEN :startDate AND :endDate");
    statement.append(" ORDER BY event." + eventDate.getColumnName());

    GraphQuery<List<?>> query = new GraphQuery<List<?>>(statement.toString());
    query.setParameter("typeCode", codes);
    query.setParameter("startDate", startDate);
    query.setParameter("endDate", endDate);

    return query.getRawResults().stream().map(list -> HistoricalRow.parse(list)).collect(Collectors.toList());
  }
}