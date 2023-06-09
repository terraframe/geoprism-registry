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
package net.geoprism.registry;

import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonObject;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdAttributeDateTimeInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.graph.MdEdgeInfo;
import com.runwaysdk.dataaccess.DuplicateDataException;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.MdAttributeDateTimeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdEdge;

import net.geoprism.registry.conversion.RegistryLocalizedValueConverter;
import net.geoprism.registry.conversion.ServerHierarchyTypeBuilder;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerElement;
import net.geoprism.registry.model.graph.GraphStrategy;
import net.geoprism.registry.model.graph.UndirectedGraphStrategy;
import net.geoprism.registry.view.JsonSerializable;

public class UndirectedGraphType extends UndirectedGraphTypeBase implements JsonSerializable, GraphType, ServerElement
{
  private static final long  serialVersionUID = -1097845938;

  public static final String JSON_LABEL       = "label";

  public UndirectedGraphType()
  {
    super();
  }

  public MdEdgeDAOIF getMdEdgeDAO()
  {
    return MdEdgeDAO.get(this.getMdEdgeOid());
  }

  @Override
  protected String buildKey()
  {
    return this.getCode();
  }

  @Override
  public LocalizedValue getLabel()
  {
    return RegistryLocalizedValueConverter.convertNoAutoCoalesce(this.getDisplayLabel());
  }

  @Transaction
  public void update(JsonObject object)
  {
    try
    {
      this.appLock();

      if (object.has(UndirectedGraphType.JSON_LABEL))
      {
        LocalizedValue label = LocalizedValue.fromJSON(object.getAsJsonObject(UndirectedGraphType.JSON_LABEL));

        RegistryLocalizedValueConverter.populate(this.getDisplayLabel(), label);
      }

      if (object.has(UndirectedGraphType.DESCRIPTION))
      {
        LocalizedValue description = LocalizedValue.fromJSON(object.getAsJsonObject(UndirectedGraphType.DESCRIPTION));

        RegistryLocalizedValueConverter.populate(this.getDescription(), description);
      }

      this.apply();
    }
    finally
    {
      this.unlock();
    }
  }

  @Override
  @Transaction
  public void delete()
  {
    MdEdge mdEdge = this.getMdEdge();

    super.delete();

    mdEdge.delete();
  }

  @Override
  public JsonObject toJSON()
  {
    JsonObject object = new JsonObject();
    object.addProperty(UndirectedGraphType.OID, this.getOid());
    object.addProperty(UndirectedGraphType.TYPE, "UndirectedGraphType");
    object.addProperty(UndirectedGraphType.CODE, this.getCode());
    object.add(UndirectedGraphType.JSON_LABEL, RegistryLocalizedValueConverter.convertNoAutoCoalesce(this.getDisplayLabel()).toJSON());
    object.add(UndirectedGraphType.DESCRIPTION, RegistryLocalizedValueConverter.convertNoAutoCoalesce(this.getDescription()).toJSON());

    return object;
  }

  public static List<UndirectedGraphType> getAll()
  {
    UndirectedGraphTypeQuery query = new UndirectedGraphTypeQuery(new QueryFactory());

    try (OIterator<? extends UndirectedGraphType> it = query.getIterator())
    {
      return new LinkedList<UndirectedGraphType>(it.getAll());
    }
  }

  @Override
  public GraphStrategy getStrategy()
  {
    return new UndirectedGraphStrategy(this);
  }

  public static UndirectedGraphType getByCode(String code)
  {
    return UndirectedGraphType.getByKey(code);
  }

  public static UndirectedGraphType getByMdEdge(MdEdge mdEdge)
  {
    UndirectedGraphTypeQuery query = new UndirectedGraphTypeQuery(new QueryFactory());
    query.WHERE(query.getMdEdge().EQ(mdEdge));

    try (OIterator<? extends UndirectedGraphType> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    return null;
  }

  public static UndirectedGraphType create(JsonObject object)
  {
    String code = object.get(UndirectedGraphType.CODE).getAsString();
    LocalizedValue label = LocalizedValue.fromJSON(object.getAsJsonObject(UndirectedGraphType.JSON_LABEL));
    LocalizedValue description = LocalizedValue.fromJSON(object.getAsJsonObject(UndirectedGraphType.DESCRIPTION));

    return create(code, label, description);
  }

  @Transaction
  public static UndirectedGraphType create(String code, LocalizedValue label, LocalizedValue description)
  {
    RoleDAO maintainer = RoleDAO.findRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE).getBusinessDAO();
    RoleDAO consumer = RoleDAO.findRole(RegistryConstants.API_CONSUMER_ROLE).getBusinessDAO();
    RoleDAO contributor = RoleDAO.findRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE).getBusinessDAO();

    try
    {
      MdVertexDAOIF mdBusGeoEntity = MdVertexDAO.getMdVertexDAO(GeoVertex.CLASS);

      MdEdgeDAO mdEdgeDAO = MdEdgeDAO.newInstance();
      mdEdgeDAO.setValue(MdEdgeInfo.PACKAGE, RegistryConstants.UNDIRECTED_GRAPH_PACKAGE);
      mdEdgeDAO.setValue(MdEdgeInfo.NAME, code);
      mdEdgeDAO.setValue(MdEdgeInfo.PARENT_MD_VERTEX, mdBusGeoEntity.getOid());
      mdEdgeDAO.setValue(MdEdgeInfo.CHILD_MD_VERTEX, mdBusGeoEntity.getOid());
      RegistryLocalizedValueConverter.populate(mdEdgeDAO, MdEdgeInfo.DISPLAY_LABEL, label);
      RegistryLocalizedValueConverter.populate(mdEdgeDAO, MdEdgeInfo.DESCRIPTION, description);
      mdEdgeDAO.setValue(MdEdgeInfo.ENABLE_CHANGE_OVER_TIME, MdAttributeBooleanInfo.FALSE);
      mdEdgeDAO.apply();

      MdAttributeDateTimeDAO startDate = MdAttributeDateTimeDAO.newInstance();
      startDate.setValue(MdAttributeDateTimeInfo.NAME, GeoVertex.START_DATE);
      startDate.setStructValue(MdAttributeDateTimeInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, "Start Date");
      startDate.setStructValue(MdAttributeDateTimeInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, "Start Date");
      startDate.setValue(MdAttributeDateTimeInfo.DEFINING_MD_CLASS, mdEdgeDAO.getOid());
      startDate.apply();

      MdAttributeDateTimeDAO endDate = MdAttributeDateTimeDAO.newInstance();
      endDate.setValue(MdAttributeDateTimeInfo.NAME, GeoVertex.END_DATE);
      endDate.setStructValue(MdAttributeDateTimeInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, "End Date");
      endDate.setStructValue(MdAttributeDateTimeInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, "End Date");
      endDate.setValue(MdAttributeDateTimeInfo.DEFINING_MD_CLASS, mdEdgeDAO.getOid());
      endDate.apply();

      ServerHierarchyTypeBuilder permissionBuilder = new ServerHierarchyTypeBuilder();
      permissionBuilder.grantWritePermissionsOnMdTermRel(mdEdgeDAO);
      permissionBuilder.grantWritePermissionsOnMdTermRel(maintainer, mdEdgeDAO);
      permissionBuilder.grantReadPermissionsOnMdTermRel(consumer, mdEdgeDAO);
      permissionBuilder.grantReadPermissionsOnMdTermRel(contributor, mdEdgeDAO);

      UndirectedGraphType graphType = new UndirectedGraphType();
      graphType.setCode(code);
      graphType.setMdEdgeId(mdEdgeDAO.getOid());
      RegistryLocalizedValueConverter.populate(graphType.getDisplayLabel(), label);
      RegistryLocalizedValueConverter.populate(graphType.getDescription(), description);
      graphType.apply();

      return graphType;
    }
    catch (DuplicateDataException ex)
    {
      DuplicateHierarchyTypeException ex2 = new DuplicateHierarchyTypeException();
      ex2.setDuplicateValue(code);
      throw ex2;
    }
  }

}
