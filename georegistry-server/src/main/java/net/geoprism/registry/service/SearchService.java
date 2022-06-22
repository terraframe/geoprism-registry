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
package net.geoprism.registry.service;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.constants.DefaultAttribute;

import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.constants.IndexTypes;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdAttributeTextInfo;
import com.runwaysdk.constants.graph.MdEdgeInfo;
import com.runwaysdk.constants.graph.MdVertexInfo;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.graph.GraphDDLCommandAction;
import com.runwaysdk.dataaccess.graph.GraphRequest;
import com.runwaysdk.dataaccess.graph.VertexObjectDAOIF;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTimeCollection;
import com.runwaysdk.dataaccess.metadata.MdAttributeDateDAO;
import com.runwaysdk.dataaccess.metadata.MdAttributeTextDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.Roles;

import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.localization.LocalizationService;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.permission.GeoObjectPermissionService;
import net.geoprism.registry.permission.RolePermissionService;

public class SearchService
{
  public static final String PACKAGE       = "net.geoprism.registry.search";

  public static final String VERTEX_PREFIX = "Search";

  public static final String EDGE_PREFIX   = "SearchLink";

  public static final String LABEL         = "label";

  public static final String VERTEX_TYPE   = "vertexType";

  public static final String CODE          = "code";

  public static final String START_DATE    = "startDate";

  public static final String END_DATE      = "endDate";

  @Transaction
  public void createSearchTable()
  {
    String suffix = this.getSuffix();
    String typeName = VERTEX_PREFIX + suffix;

    MdVertexDAO mdVertex = MdVertexDAO.newInstance();
    mdVertex.setValue(MdVertexInfo.PACKAGE, PACKAGE);
    mdVertex.setValue(MdVertexInfo.NAME, typeName);
    mdVertex.setValue(MdVertexInfo.GENERATE_SOURCE, MdAttributeBooleanInfo.FALSE);
    mdVertex.apply();

    MdAttributeTextDAO label = MdAttributeTextDAO.newInstance();
    label.setValue(MdAttributeTextInfo.NAME, LABEL);
    label.setValue(MdAttributeTextInfo.DEFINING_MD_CLASS, mdVertex.getOid());
    label.apply();

    MdAttributeTextDAO code = MdAttributeTextDAO.newInstance();
    code.setValue(MdAttributeTextInfo.NAME, CODE);
    code.setValue(MdAttributeTextInfo.DEFINING_MD_CLASS, mdVertex.getOid());
    code.setValue(MdAttributeTextInfo.INDEX_TYPE, IndexTypes.NON_UNIQUE_INDEX.getOid());
    code.apply();

    MdAttributeTextDAO vertexType = MdAttributeTextDAO.newInstance();
    vertexType.setValue(MdAttributeTextInfo.NAME, VERTEX_TYPE);
    vertexType.setValue(MdAttributeTextInfo.DEFINING_MD_CLASS, mdVertex.getOid());
    vertexType.setValue(MdAttributeTextInfo.INDEX_TYPE, IndexTypes.NON_UNIQUE_INDEX.getOid());
    vertexType.apply();

    MdAttributeDateDAO startDate = MdAttributeDateDAO.newInstance();
    startDate.setValue(MdAttributeTextInfo.NAME, START_DATE);
    startDate.setValue(MdAttributeTextInfo.DEFINING_MD_CLASS, mdVertex.getOid());
    startDate.setValue(MdAttributeTextInfo.INDEX_TYPE, IndexTypes.NON_UNIQUE_INDEX.getOid());
    startDate.apply();

    MdAttributeDateDAO endDate = MdAttributeDateDAO.newInstance();
    endDate.setValue(MdAttributeTextInfo.NAME, END_DATE);
    endDate.setValue(MdAttributeTextInfo.DEFINING_MD_CLASS, mdVertex.getOid());
    endDate.setValue(MdAttributeTextInfo.INDEX_TYPE, IndexTypes.NON_UNIQUE_INDEX.getOid());
    endDate.apply();

    MdEdgeDAO mdEdge = MdEdgeDAO.newInstance();
    mdEdge.setValue(MdVertexInfo.PACKAGE, PACKAGE);
    mdEdge.setValue(MdVertexInfo.NAME, EDGE_PREFIX + suffix);
    mdEdge.setValue(MdEdgeInfo.PARENT_MD_VERTEX, mdVertex.getOid());
    mdEdge.setValue(MdEdgeInfo.CHILD_MD_VERTEX, MdVertexDAO.getMdVertexDAO(GeoVertex.CLASS).getOid());
    mdEdge.apply();

    GraphDBService service = GraphDBService.getInstance();
    GraphRequest dml = service.getGraphDBRequest();
    GraphRequest ddl = service.getDDLGraphDBRequest();

    String attributeName = label.getValue(MdAttributeTextInfo.NAME);
    String className = mdVertex.getDBClassName();

    String indexName = className + "." + attributeName;
    String statement = "CREATE INDEX " + indexName + " ON " + className + "(" + attributeName + ") FULLTEXT ENGINE LUCENE";

    GraphDDLCommandAction action = service.ddlCommand(dml, ddl, statement, new HashMap<String, Object>());
    action.execute();

    this.assignAllPermissions(Roles.findRoleByName(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE), mdVertex, mdEdge);
    this.assignAllPermissions(Roles.findRoleByName(RegistryConstants.REGISTRY_ADMIN_ROLE), mdVertex, mdEdge);
    this.assignAllPermissions(Roles.findRoleByName(RegistryConstants.REGISTRY_MAINTAINER_ROLE), mdVertex, mdEdge);
  }

  private void assignAllPermissions(Roles role, ComponentIF... components)
  {
    RoleDAO roleDAO = (RoleDAO) BusinessFacade.getEntityDAO(role);

    for (ComponentIF component : components)
    {
      roleDAO.grantPermission(Operation.CREATE, component.getOid());
      roleDAO.grantPermission(Operation.DELETE, component.getOid());
      roleDAO.grantPermission(Operation.WRITE, component.getOid());
      roleDAO.grantPermission(Operation.WRITE_ALL, component.getOid());
      roleDAO.grantPermission(Operation.READ, component.getOid());
      roleDAO.grantPermission(Operation.READ_ALL, component.getOid());
    }
  }

  @Transaction
  public void deleteSearchTable()
  {
    String suffix = this.getSuffix();

    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(PACKAGE + "." + EDGE_PREFIX + suffix);
    mdEdge.getBusinessDAO().delete();

    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(PACKAGE + "." + VERTEX_PREFIX + suffix);
    mdVertex.getBusinessDAO().delete();
  }

  @Transaction
  public void clear()
  {
    String suffix = this.getSuffix();

    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(PACKAGE + "." + VERTEX_PREFIX + suffix);

    StringBuilder statement = new StringBuilder();
    statement.append("DELETE VERTEX " + mdVertex.getDBClassName());

    GraphDBService service = GraphDBService.getInstance();
    GraphRequest request = service.getGraphDBRequest();

    service.command(request, statement.toString(), new HashMap<>());
  }

  // @Transaction
  public void clear(String vertexType)
  {
    String suffix = this.getSuffix();

    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(PACKAGE + "." + VERTEX_PREFIX + suffix);
    MdAttributeDAOIF mdVertexType = mdVertex.definesAttribute(VERTEX_TYPE);

    StringBuilder statement = new StringBuilder();
    statement.append("DELETE VERTEX " + mdVertex.getDBClassName());
    statement.append(" WHERE " + mdVertexType.getColumnName() + " = :vertexType");

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("vertexType", vertexType);

    GraphDBService service = GraphDBService.getInstance();
    GraphRequest request = service.getGraphDBRequest();
    GraphRequest ddlRequest = service.getDDLGraphDBRequest();

    service.ddlCommand(request, ddlRequest, statement.toString(), parameters);
  }

  // @Transaction
  public void remove(String code)
  {
    // String suffix = this.getSuffix();
    //
    // MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(PACKAGE + "." +
    // VERTEX_PREFIX + suffix);
    // MdAttributeDAOIF mdCode = mdVertex.definesAttribute(CODE);
    //
    // StringBuilder statement = new StringBuilder();
    // statement.append("DELETE VERTEX " + mdVertex.getDBClassName());
    // statement.append(" WHERE " + mdCode.getColumnName() + " = :code");
    //
    // Map<String, Object> parameters = new HashMap<String, Object>();
    // parameters.put("code", code);
    //
    // GraphDBService service = GraphDBService.getInstance();
    // GraphRequest request = service.getGraphDBRequest();
    //
    // service.command(request, statement.toString(), parameters);

    String suffix = this.getSuffix();

    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(PACKAGE + "." + VERTEX_PREFIX + suffix);
    MdAttributeDAOIF mdCode = mdVertex.definesAttribute(CODE);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" WHERE :code = " + mdCode.getColumnName());

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());
    query.setParameter("code", code);

    List<VertexObject> results = query.getResults();

    for (VertexObject result : results)
    {
      result.delete();
    }
  }

  // @Transaction
  public void insert(VertexServerGeoObject object, boolean isNew)
  {
    if (!isNew)
    {
      this.remove(object.getCode());
    }

    String suffix = this.getSuffix();

    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(PACKAGE + "." + VERTEX_PREFIX + suffix);
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(PACKAGE + "." + EDGE_PREFIX + suffix);

    ValueOverTimeCollection vots = object.getValuesOverTime(DefaultAttribute.DISPLAY_LABEL.getName());

    for (ValueOverTime vot : vots)
    {
      VertexObjectDAOIF value = (VertexObjectDAOIF) vot.getValue();

      Set<String> attributeNames = LocalizationService.getLocaleNames();

      for (String attributeName : attributeNames)
      {
        String label = value.getObjectValue(attributeName);

        if (label != null && label.length() > 0)
        {
          VertexObject vertex = new VertexObject(mdVertex.definesType());
          vertex.setValue(START_DATE, vot.getStartDate());
          vertex.setValue(END_DATE, vot.getEndDate());
          vertex.setValue(CODE, object.getCode());
          vertex.setValue(LABEL, label);
          vertex.setValue(VERTEX_TYPE, object.getType().getCode());
          vertex.apply();

          vertex.addChild(object.getVertex(), mdEdge).apply();
        }
      }
    }

  }

  public List<ServerGeoObjectIF> search(String text, Date date, Long limit)
  {
    String suffix = this.getSuffix();

    RolePermissionService service = new RolePermissionService();

    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(PACKAGE + "." + VERTEX_PREFIX + suffix);
    MdAttributeDAOIF code = mdVertex.definesAttribute(CODE);
    MdAttributeDAOIF startDate = mdVertex.definesAttribute(START_DATE);
    MdAttributeDAOIF endDate = mdVertex.definesAttribute(END_DATE);
    MdAttributeDAOIF label = mdVertex.definesAttribute(LABEL);
    MdAttributeDAOIF vertexType = mdVertex.definesAttribute(VERTEX_TYPE);
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(PACKAGE + "." + EDGE_PREFIX + suffix);
    String attributeName = label.getValue(MdAttributeTextInfo.NAME);
    String className = mdVertex.getDBClassName();
    String indexName = className + "." + attributeName;

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND(out('" + mdEdge.getDBClassName() + "'))");
    statement.append(" FROM " + mdVertex.getDBClassName());

    if (text != null)
    {
      String regex = "([+\\-!\\(\\){}\\[\\]^\"~*?:\\\\]|[&\\|]{2})";
      String escapedText = text.replaceAll(regex, "\\\\\\\\$1").trim();
      
      String[] escapedTokens = StringUtils.split(escapedText, " ");
      String term;
      if (escapedTokens.length == 1)
      {
          term = escapedText + "*";
      }
      else 
      {
          term = "(\"" + escapedText + "\"^" + escapedTokens.length + " " + StringUtils.join(escapedTokens, "* ") + "*)";
      }
      
      statement.append(" WHERE (SEARCH_INDEX(\"" + indexName + "\", '+" + label.getColumnName() + ":" + term + "') = true");
      statement.append(" OR :code = " + code.getColumnName() + ")");
    }
    else
    {
      statement.append(" WHERE " + code.getColumnName() + " IS NOT NULL");
    }

    if (date != null)
    {
      statement.append(" AND :date BETWEEN " + startDate.getColumnName() + " AND " + endDate.getColumnName());
    }

    if (!service.isSRA() && service.hasSessionUser())
    {
      statement.append(" AND " + vertexType.getColumnName() + " IN ( :vertexTypes )");
    }

    statement.append(" ORDER BY " + label.getColumnName() + " DESC");

    if (limit != null)
    {
      statement.append(" LIMIT " + limit);
    }

    List<ServerGeoObjectIF> list = new LinkedList<ServerGeoObjectIF>();
    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());

    if (text != null)
    {
      query.setParameter("code", text);
    }

    if (date != null)
    {
      query.setParameter("date", date);
    }

    if (!service.isSRA() && service.hasSessionUser())
    {
      List<String> vertexTypes = new GeoObjectPermissionService().getMandateTypes(service.getOrganization());
      query.setParameter("vertexTypes", vertexTypes);
    }

    List<VertexObject> results = query.getResults();

    for (VertexObject result : results)
    {
      MdVertexDAOIF mdVertexType = (MdVertexDAOIF) result.getMdClass();
      ServerGeoObjectType type = ServerGeoObjectType.get(mdVertexType);

      list.add(new VertexServerGeoObject(type, result, date));
    }

    return list;
  }

  private String getSuffix()
  {
    // return locale != null ? locale.toString() : "Default";
    return "Default";
  }
}
