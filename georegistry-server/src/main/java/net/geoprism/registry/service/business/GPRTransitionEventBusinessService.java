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
package net.geoprism.registry.service.business;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.session.Session;

import net.geoprism.registry.DateFormatter;
import net.geoprism.registry.excel.HistoricalReportExcelExporter;
import net.geoprism.registry.graph.transition.Transition;
import net.geoprism.registry.graph.transition.TransitionEvent;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.permission.RolePermissionService;
import net.geoprism.registry.service.request.GeoObjectTypeRestrictionUtil;
import net.geoprism.registry.service.request.ServiceFactory;
import net.geoprism.registry.view.HistoricalRow;
import net.geoprism.registry.view.Page;

@Service
@Primary
public class GPRTransitionEventBusinessService extends TransitionEventBusinessService implements TransitionEventBusinessServiceIF
{
  @Autowired
  private GeoObjectTypeRestrictionUtil   util;

  @Autowired
  private GeoObjectTypeBusinessServiceIF typeService;

  @Autowired
  private RolePermissionService          permissions;

  @Override
  public boolean readOnly(TransitionEvent tran)
  {
    ServerGeoObjectType type = ServiceFactory.getMetadataCache().getGeoObjectType(tran.getBeforeTypeCode()).get();

    final String orgCode = tran.getBeforeTypeOrgCode();

    return ! ( permissions.isSRA() || permissions.isRA(orgCode) || permissions.isRM(orgCode, type) || permissions.isRC(orgCode, type) );
  }

  @Override
  public void addPageWhereCriteria(StringBuilder statement, Map<String, Object> parameters, String attrConditions)
  {
    List<String> whereConditions = new ArrayList<String>();

    // Add permissions criteria
    if (Session.getCurrentSession() != null)
    {
      String beforeCondition = this.util.buildTypeWritePermissionsFilter(TransitionEvent.BEFORETYPEORGCODE, TransitionEvent.BEFORETYPECODE);
      if (beforeCondition.length() > 0)
      {
        whereConditions.add(beforeCondition);
      }

      String afterCondition = this.util.buildTypeReadPermissionsFilter(TransitionEvent.AFTERTYPEORGCODE, TransitionEvent.AFTERTYPECODE);
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
          format.setTimeZone(DateFormatter.SYSTEM_TIMEZONE);

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

  public Long getCount(ServerGeoObjectType type, Date startDate, Date endDate)
  {
    MdVertexDAOIF transitionVertex = MdVertexDAO.getMdVertexDAO(Transition.CLASS);
    MdAttributeDAOIF eventAttribute = transitionVertex.definesAttribute(Transition.EVENT);

    MdVertexDAOIF eventVertex = MdVertexDAO.getMdVertexDAO(TransitionEvent.CLASS);
    MdAttributeDAOIF beforeTypeCode = eventVertex.definesAttribute(TransitionEvent.BEFORETYPECODE);
    MdAttributeDAOIF afterTypeCode = eventVertex.definesAttribute(TransitionEvent.AFTERTYPECODE);
    MdAttributeDAOIF eventDate = eventVertex.definesAttribute(TransitionEvent.EVENTDATE);

    List<ServerGeoObjectType> types = new LinkedList<ServerGeoObjectType>();
    types.add(type);
    types.addAll(this.typeService.getSubtypes(type));

    List<String> codes = types.stream().map(t -> t.getCode()).distinct().collect(Collectors.toList());

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*)");
    statement.append(" FROM " + transitionVertex.getDBClassName());
    statement.append(" WHERE ( " + eventAttribute.getColumnName() + "." + beforeTypeCode.getColumnName() + " IN :typeCode");
    statement.append(" OR " + eventAttribute.getColumnName() + "." + afterTypeCode.getColumnName() + " IN :typeCode )");
    statement.append(" AND " + eventAttribute.getColumnName() + "." + eventDate.getColumnName() + " BETWEEN :startDate AND :endDate");

    GraphQuery<Long> query = new GraphQuery<Long>(statement.toString());
    query.setParameter("typeCode", codes);
    query.setParameter("startDate", startDate);
    query.setParameter("endDate", endDate);

    return query.getSingleResult();
  }

  public Page<HistoricalRow> getHistoricalReport(ServerGeoObjectType type, Date startDate, Date endDate, Integer pageSize, Integer pageNumber)
  {
    MdVertexDAOIF transitionVertex = MdVertexDAO.getMdVertexDAO(Transition.CLASS);
    MdAttributeDAOIF eventAttribute = transitionVertex.definesAttribute(Transition.EVENT);
    MdAttributeDAOIF sourceAttribute = transitionVertex.definesAttribute(Transition.SOURCE);
    MdAttributeDAOIF targetAttribute = transitionVertex.definesAttribute(Transition.TARGET);
    MdAttributeDAOIF transitionAttribute = transitionVertex.definesAttribute(Transition.TRANSITIONTYPE);

    MdVertexDAOIF eventVertex = MdVertexDAO.getMdVertexDAO(TransitionEvent.CLASS);
    MdAttributeDAOIF eventId = eventVertex.definesAttribute(TransitionEvent.EVENTID);
    MdAttributeDAOIF beforeTypeCode = eventVertex.definesAttribute(TransitionEvent.BEFORETYPECODE);
    MdAttributeDAOIF afterTypeCode = eventVertex.definesAttribute(TransitionEvent.AFTERTYPECODE);
    MdAttributeDAOIF eventDate = eventVertex.definesAttribute(TransitionEvent.EVENTDATE);
    MdAttributeDAOIF description = eventVertex.definesAttribute(TransitionEvent.DESCRIPTION);

    List<ServerGeoObjectType> types = new LinkedList<ServerGeoObjectType>();
    types.add(type);
    types.addAll(this.typeService.getSubtypes(type));

    List<String> codes = types.stream().map(t -> t.getCode()).distinct().collect(Collectors.toList());

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT " + eventAttribute.getColumnName() + "." + eventId.getColumnName() + " AS " + HistoricalRow.EVENT_ID);
    statement.append(", " + eventAttribute.getColumnName() + "." + eventDate.getColumnName() + " AS " + HistoricalRow.EVENT_DATE);
    statement.append(", " + transitionAttribute.getColumnName() + " AS " + HistoricalRow.EVENT_TYPE);
    statement.append(", " + eventAttribute.getColumnName() + "." + description.getColumnName() + " AS " + HistoricalRow.DESCRIPTION);
    statement.append(", " + eventAttribute.getColumnName() + "." + beforeTypeCode.getColumnName() + " AS " + HistoricalRow.BEFORE_TYPE);
    statement.append(", " + sourceAttribute.getColumnName() + ".code AS " + HistoricalRow.BEFORE_CODE);
    statement.append(", " + sourceAttribute.getColumnName() + ".displayLabel_cot AS " + HistoricalRow.BEFORE_LABEL);
    statement.append(", " + eventAttribute.getColumnName() + "." + afterTypeCode.getColumnName() + " AS " + HistoricalRow.AFTER_TYPE);
    statement.append(", " + targetAttribute.getColumnName() + ".code AS " + HistoricalRow.AFTER_CODE);
    statement.append(", " + targetAttribute.getColumnName() + ".displayLabel_cot AS " + HistoricalRow.AFTER_LABEL);
    statement.append(" FROM " + transitionVertex.getDBClassName());
    statement.append(" WHERE ( " + eventAttribute.getColumnName() + "." + beforeTypeCode.getColumnName() + " IN :typeCode");
    statement.append(" OR " + eventAttribute.getColumnName() + "." + afterTypeCode.getColumnName() + " IN :typeCode )");
    statement.append(" AND " + eventAttribute.getColumnName() + "." + eventDate.getColumnName() + " BETWEEN :startDate AND :endDate");
    statement.append(" ORDER BY " + eventAttribute.getColumnName() + "." + eventDate.getColumnName() + " DESC");
    statement.append(", " + eventAttribute.getColumnName() + "." + eventId.getColumnName());
    statement.append(", " + sourceAttribute.getColumnName() + ".code");
    statement.append(", " + targetAttribute.getColumnName() + ".code");

    if (pageNumber != null && pageSize != null)
    {
      statement.append(" SKIP " + ( ( pageNumber - 1 ) * pageSize ) + " LIMIT " + pageSize);
    }

    GraphQuery<Map<String, Object>> query = new GraphQuery<Map<String, Object>>(statement.toString());
    query.setParameter("typeCode", codes);
    query.setParameter("startDate", startDate);
    query.setParameter("endDate", endDate);

    Long count = getCount(type, startDate, endDate);

    List<HistoricalRow> results = query.getResults().stream().map(list -> HistoricalRow.parse(list)).collect(Collectors.toList());

    return new Page<HistoricalRow>(count, pageNumber, pageSize, results);
  }

  public InputStream exportToExcel(ServerGeoObjectType type, Date startDate, Date endDate) throws IOException
  {
    HistoricalReportExcelExporter exporter = new HistoricalReportExcelExporter(type, startDate, endDate);

    return exporter.export();
  }

  public Long getCount()
  {
    MdVertexDAOIF transitionVertex = MdVertexDAO.getMdVertexDAO(TransitionEvent.CLASS);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*)");
    statement.append(" FROM " + transitionVertex.getDBClassName());

    GraphQuery<Long> query = new GraphQuery<Long>(statement.toString());

    return query.getSingleResult();
  }

  public Page<TransitionEvent> getAll(Integer pageSize, Integer pageNumber)
  {
    MdVertexDAOIF transitionVertex = MdVertexDAO.getMdVertexDAO(TransitionEvent.CLASS);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + transitionVertex.getDBClassName());
    statement.append(" ORDER BY oid");

    if (pageNumber != null && pageSize != null)
    {
      statement.append(" SKIP " + ( ( pageNumber - 1 ) * pageSize ) + " LIMIT " + pageSize);
    }

    GraphQuery<TransitionEvent> query = new GraphQuery<TransitionEvent>(statement.toString());
    Long count = getCount();

    return new Page<TransitionEvent>(count, pageNumber, pageSize, query.getResults());
  }

  public void deleteAll()
  {
    Page<TransitionEvent> page = this.getAll(1000, 1);

    while (page.getResults().size() > 0)
    {
      List<TransitionEvent> results = page.getResults();

      for (TransitionEvent result : results)
      {
        this.delete(result);
      }

      page = this.getAll(1000, 1);
    }
  }

}
