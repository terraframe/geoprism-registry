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
package net.geoprism.registry.graph.transition;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Session;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.conversion.RegistryLocalizedValueConverter;
import net.geoprism.registry.graph.transition.Transition.TransitionImpact;
import net.geoprism.registry.graph.transition.Transition.TransitionType;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.permission.GeoObjectPermissionService;
import net.geoprism.registry.permission.RolePermissionService;
import net.geoprism.registry.query.graph.GeoObjectTypeRestrictionUtil;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.transition.TransitionPermissionService;
import net.geoprism.registry.view.JsonSerializable;
import net.geoprism.registry.view.Page;

public class TransitionEvent extends TransitionEventBase implements JsonSerializable
{
  public static final String EVENT_SEQUENCE   = "event_seq";

  private static final long  serialVersionUID = 112753140;

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
//    MdAttributeDAOIF sourceAttribute = mdVertex.definesAttribute(Transition.SOURCE);
    MdAttributeDAOIF targetAttribute = mdVertex.definesAttribute(Transition.TARGET);
    MdAttributeDAOIF orderAttribute = mdVertex.definesAttribute(Transition.ORDER);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" WHERE " + mdAttribute.getColumnName() + " = :event");
    statement.append(" ORDER BY " + orderAttribute.getColumnName() + " ASC");
    statement.append(", " + targetAttribute.getColumnName() + ".code");

    GraphQuery<Transition> query = new GraphQuery<Transition>(statement.toString());
    query.setParameter("event", this.getRID());

    List<Transition> results = query.getResults();
    return results;
  }

  @Transaction
  public Transition addTransition(ServerGeoObjectIF source, ServerGeoObjectIF target, TransitionType transitionType, TransitionImpact impact)
  {
    Transition transition = new Transition();
    transition.setTransitionType(transitionType);
    transition.setImpact(impact);
    transition.setEvent(this);
    transition.apply(this, this.getTransitions().size(), (VertexServerGeoObject) source, (VertexServerGeoObject) target);

    return transition;
  }

  @Override
  public JsonObject toJSON()
  {
    return this.toJSON(false);
  }

  public boolean readOnly()
  {
    RolePermissionService rps = ServiceFactory.getRolePermissionService();
    ServerGeoObjectType type = ServiceFactory.getMetadataCache().getGeoObjectType(this.getBeforeTypeCode()).get();

    final String orgCode = this.getBeforeTypeOrgCode();

    return ! ( rps.isSRA() || rps.isRA(orgCode) || rps.isRM(orgCode, type) || rps.isRC(orgCode, type) );
  }

  public JsonObject toJSON(boolean includeTransitions)
  {
    DateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
    format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    LocalizedValue localizedValue = RegistryLocalizedValueConverter.convert(this.getEmbeddedComponent(TransitionEvent.DESCRIPTION));
    ServerGeoObjectType beforeType = ServerGeoObjectType.get(this.getBeforeTypeCode());
    ServerGeoObjectType afterType = ServerGeoObjectType.get(this.getAfterTypeCode());

    JsonObject object = new JsonObject();
    object.addProperty(TransitionEvent.OID, this.getOid());
    object.addProperty(TransitionEvent.EVENTID, this.getEventId());
    object.addProperty(TransitionEvent.BEFORETYPECODE, beforeType.getCode());
    object.addProperty(TransitionEvent.AFTERTYPECODE, afterType.getCode());
    object.addProperty(TransitionEvent.EVENTDATE, format.format(this.getEventDate()));
    object.addProperty("beforeTypeLabel", beforeType.getLabel().getValue());
    object.addProperty("afterTypeLabel", afterType.getLabel().getValue());
    object.add(TransitionEvent.DESCRIPTION, localizedValue.toJSON());

    GsonBuilder builder = new GsonBuilder();
    JsonArray ja = builder.create().toJsonTree(new TransitionPermissionService().getPermissions(this)).getAsJsonArray();
    object.add("permissions", ja);

    if (includeTransitions)
    {
      JsonArray transitions = this.getTransitions().stream().map(e -> e.toJSON()).collect(() -> new JsonArray(), (array, element) -> array.add(element), (listA, listB) -> listA.addAll(listB));

      object.add("transitions", transitions);
    }

    return object;
  }

  @Override
  public void apply()
  {
    if (this.isNew() && !this.isAppliedToDb())
    {
      this.setEventId(TransitionEvent.getNextSequenceNumber());
    }

    super.apply();
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
      // ServiceFactory.getGeoObjectPermissionService().enforceCanWrite(afterType.getOrganization().getCode(),
      // afterType);

      DateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
      format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

      LocalizedValue description = LocalizedValue.fromJSON(json.get(TransitionEvent.DESCRIPTION).getAsJsonObject());
      TransitionEvent event = json.has(OID) ? TransitionEvent.get(json.get(OID).getAsString()) : new TransitionEvent();
      RegistryLocalizedValueConverter.populate(event, TransitionEvent.DESCRIPTION, description);
      event.setEventDate(format.parse(json.get(TransitionEvent.EVENTDATE).getAsString()));
      event.setBeforeTypeCode(beforeTypeCode);
      event.setAfterTypeCode(afterTypeCode);
      event.setBeforeTypeOrgCode(beforeType.getOrganization().getCode());
      event.setAfterTypeOrgCode(afterType.getOrganization().getCode());
      event.apply();

      JsonArray transitions = json.get("transitions").getAsJsonArray();

      List<String> appliedTrans = new ArrayList<String>();
      for (int i = 0; i < transitions.size(); i++)
      {
        JsonObject object = transitions.get(i).getAsJsonObject();

        Transition trans = Transition.apply(event, i, object);
        appliedTrans.add(trans.getOid());
      }

      for (Transition trans : event.getTransitions())
      {
        if (!appliedTrans.contains(trans.getOid()))
        {
          trans.delete();
        }
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

  public static Page<TransitionEvent> page(Integer pageSize, Integer pageNumber, String attrConditions)
  {
    Long count = getCount();

    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(TransitionEvent.CLASS);
    MdAttributeDAOIF eventDate = mdVertex.definesAttribute(TransitionEvent.EVENTDATE);

    Map<String, Object> parameters = new HashMap<String, Object>();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());

    addPageWhereCriteria(statement, parameters, attrConditions);

    statement.append(" ORDER BY " + eventDate.getColumnName() + " DESC");
    statement.append(" SKIP " + ( ( pageNumber - 1 ) * pageSize ) + " LIMIT " + pageSize);

    GraphQuery<TransitionEvent> query = new GraphQuery<TransitionEvent>(statement.toString(), parameters);

    return new Page<TransitionEvent>(count, pageNumber, pageSize, query.getResults());
  }

  public static void addPageWhereCriteria(StringBuilder statement, Map<String, Object> parameters, String attrConditions)
  {
    List<String> whereConditions = new ArrayList<String>();

    // Add permissions criteria
    if (Session.getCurrentSession() != null)
    {
        String beforeCondition = GeoObjectTypeRestrictionUtil.buildTypeWritePermissionsFilter(TransitionEvent.BEFORETYPEORGCODE, TransitionEvent.BEFORETYPECODE);
        if (beforeCondition.length() > 0)
        {
          whereConditions.add(beforeCondition);
        }
        
        String afterCondition = GeoObjectTypeRestrictionUtil.buildTypeReadPermissionsFilter(TransitionEvent.AFTERTYPEORGCODE, TransitionEvent.AFTERTYPECODE);
        if (afterCondition.length() > 0)
        {
          whereConditions.add(afterCondition);
        }
    }

    // Filter based on attributes
    if (attrConditions != null && attrConditions.length() > 0)
    {
      List<String> lAttrConditions = new ArrayList<String>();
      JsonArray jaAttrConditions = JsonParser.parseString(attrConditions).getAsJsonArray();

      for (int i = 0; i < jaAttrConditions.size(); ++i)
      {
        JsonObject attrCondition = jaAttrConditions.get(i).getAsJsonObject();

        String attr = attrCondition.get("attribute").getAsString();

        MdVertexDAO eventMd = (MdVertexDAO) MdVertexDAO.getMdVertexDAO(TransitionEvent.CLASS);
        MdAttributeDAOIF mdAttr = eventMd.definesAttribute(attr);

        if (attr.equals(TransitionEvent.EVENTDATE))
        {
          DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
          format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

          List<String> dateConditions = new ArrayList<String>();

          try
          {
            if (attrCondition.has("startDate") && !attrCondition.get("startDate").isJsonNull() && attrCondition.get("startDate").getAsString().length() > 0)
            {
              Date startDate = format.parse(attrCondition.get("startDate").getAsString());
              dateConditions.add(mdAttr.getColumnName() + ">=:startDate" + i);
              parameters.put("startDate" + i, startDate);
            }
            if (attrCondition.has("endDate") && !attrCondition.get("endDate").isJsonNull() && attrCondition.get("endDate").getAsString().length() > 0)
            {
              Date endDate = format.parse(attrCondition.get("endDate").getAsString());
              dateConditions.add(mdAttr.getColumnName() + "<=:endDate" + i);
              parameters.put("endDate" + i, endDate);
            }
          }
          catch (ParseException e)
          {
            throw new ProgrammingErrorException(e);
          }

          if (dateConditions.size() > 0)
          {
            lAttrConditions.add("(" + StringUtils.join(dateConditions, " AND ") + ")");
          }
        }
        else if (attrCondition.has("value") && !attrCondition.get("value").isJsonNull() && attrCondition.get("value").getAsString().length() > 0)
        {
          String value = attrCondition.get("value").getAsString();

          lAttrConditions.add(mdAttr.getColumnName() + "=:val" + i);
          parameters.put("val" + i, value);
        }
      }

      if (lAttrConditions.size() > 0)
      {
        whereConditions.add(StringUtils.join(lAttrConditions, " AND "));
      }
    }

    if (whereConditions.size() > 0)
    {
      statement.append(" WHERE " + StringUtils.join(whereConditions, " AND "));
    }
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

  public static Long getNextSequenceNumber()
  {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT sequence('" + EVENT_SEQUENCE + "').next()");

    GraphQuery<Long> query = new GraphQuery<Long>(statement.toString());

    return query.getSingleResult();

  }
}
