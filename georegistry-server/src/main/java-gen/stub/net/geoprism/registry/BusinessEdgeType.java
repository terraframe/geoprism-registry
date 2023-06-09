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
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.graph.MdEdgeInfo;
import com.runwaysdk.dataaccess.DuplicateDataException;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdEdge;

import net.geoprism.registry.conversion.RegistryLocalizedValueConverter;
import net.geoprism.registry.conversion.ServerHierarchyTypeBuilder;
import net.geoprism.registry.model.ServerElement;
import net.geoprism.registry.view.JsonSerializable;

public class BusinessEdgeType extends BusinessEdgeTypeBase implements JsonSerializable, ServerElement
{
  @SuppressWarnings("unused")
  private static final long  serialVersionUID = 1946865589;

  public static final String JSON_LABEL       = "label";

  public BusinessEdgeType()
  {
    super();
  }

  @Override
  protected String buildKey()
  {
    return this.getCode();
  }

  public LocalizedValue getLabel()
  {
    return RegistryLocalizedValueConverter.convertNoAutoCoalesce(this.getDisplayLabel());
  }

  public MdEdgeDAOIF getMdEdgeDAO()
  {
    return MdEdgeDAO.get(this.getMdEdgeOid());
  }

  public BusinessType getParent()
  {
    return BusinessType.getByMdVertex((MdVertexDAOIF) BusinessFacade.getEntityDAO(this.getParentType()));
  }

  public BusinessType getChild()
  {
    return BusinessType.getByMdVertex((MdVertexDAOIF) BusinessFacade.getEntityDAO(this.getChildType()));
  }

  @Transaction
  public void update(JsonObject object)
  {
    try
    {
      this.appLock();

      if (object.has(BusinessEdgeType.DISPLAYLABEL))
      {
        LocalizedValue label = LocalizedValue.fromJSON(object.getAsJsonObject(BusinessEdgeType.DISPLAYLABEL));

        RegistryLocalizedValueConverter.populate(this.getDisplayLabel(), label);
      }

      if (object.has(BusinessEdgeType.DESCRIPTION))
      {
        LocalizedValue description = LocalizedValue.fromJSON(object.getAsJsonObject(BusinessEdgeType.DESCRIPTION));

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
    object.addProperty(BusinessEdgeType.OID, this.getOid());
    object.addProperty(BusinessEdgeType.TYPE, "BusinessEdgeType");
    object.addProperty(BusinessEdgeType.CODE, this.getCode());
    object.addProperty(BusinessEdgeType.ORGANIZATION, this.getOrganization().getCode());
    object.addProperty(BusinessEdgeType.PARENTTYPE, this.getParentType().getTypeName());
    object.addProperty(BusinessEdgeType.CHILDTYPE, this.getChildType().getTypeName());
    object.add(BusinessEdgeType.JSON_LABEL, RegistryLocalizedValueConverter.convertNoAutoCoalesce(this.getDisplayLabel()).toJSON());
    object.add(BusinessEdgeType.DESCRIPTION, RegistryLocalizedValueConverter.convertNoAutoCoalesce(this.getDescription()).toJSON());

    return object;
  }

  public static List<BusinessEdgeType> getAll()
  {
    BusinessEdgeTypeQuery query = new BusinessEdgeTypeQuery(new QueryFactory());

    try (OIterator<? extends BusinessEdgeType> it = query.getIterator())
    {
      return new LinkedList<BusinessEdgeType>(it.getAll());
    }
  }

  public static BusinessEdgeType getByCode(String code)
  {
    return BusinessEdgeType.getByKey(code);
  }

  public static BusinessEdgeType getByMdEdge(MdEdge mdEdge)
  {
    BusinessEdgeTypeQuery query = new BusinessEdgeTypeQuery(new QueryFactory());
    query.WHERE(query.getMdEdge().EQ(mdEdge));

    try (OIterator<? extends BusinessEdgeType> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    return null;
  }

  public static BusinessEdgeType create(JsonObject object)
  {
    String code = object.get(BusinessEdgeType.CODE).getAsString();
    String parentTypeCode = object.get(BusinessEdgeType.PARENTTYPE).getAsString();
    String childTypeCode = object.get(BusinessEdgeType.CHILDTYPE).getAsString();
    LocalizedValue label = LocalizedValue.fromJSON(object.getAsJsonObject(BusinessEdgeType.JSON_LABEL));
    LocalizedValue description = LocalizedValue.fromJSON(object.getAsJsonObject(BusinessEdgeType.DESCRIPTION));
    String organizationCode = object.get(BusinessType.ORGANIZATION).getAsString();

    return create(organizationCode, code, label, description, parentTypeCode, childTypeCode);
  }

  @Transaction
  public static BusinessEdgeType create(String organizationCode, String code, LocalizedValue label, LocalizedValue description, String parentTypeCode, String childTypeCode)
  {
    RoleDAO maintainer = RoleDAO.findRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE).getBusinessDAO();
    RoleDAO consumer = RoleDAO.findRole(RegistryConstants.API_CONSUMER_ROLE).getBusinessDAO();
    RoleDAO contributor = RoleDAO.findRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE).getBusinessDAO();

    BusinessType parentType = BusinessType.getByCode(parentTypeCode);
    BusinessType childType = BusinessType.getByCode(childTypeCode);
    Organization organization = Organization.getByCode(organizationCode);

    try
    {
      MdEdgeDAO mdEdgeDAO = MdEdgeDAO.newInstance();
      mdEdgeDAO.setValue(MdEdgeInfo.PACKAGE, RegistryConstants.DAG_PACKAGE);
      mdEdgeDAO.setValue(MdEdgeInfo.NAME, code);
      mdEdgeDAO.setValue(MdEdgeInfo.PARENT_MD_VERTEX, parentType.getMdVertexOid());
      mdEdgeDAO.setValue(MdEdgeInfo.CHILD_MD_VERTEX, childType.getMdVertexOid());
      RegistryLocalizedValueConverter.populate(mdEdgeDAO, MdEdgeInfo.DISPLAY_LABEL, label);
      RegistryLocalizedValueConverter.populate(mdEdgeDAO, MdEdgeInfo.DESCRIPTION, description);
      mdEdgeDAO.setValue(MdEdgeInfo.ENABLE_CHANGE_OVER_TIME, MdAttributeBooleanInfo.FALSE);
      mdEdgeDAO.apply();

      ServerHierarchyTypeBuilder permissionBuilder = new ServerHierarchyTypeBuilder();
      permissionBuilder.grantWritePermissionsOnMdTermRel(mdEdgeDAO);
      permissionBuilder.grantWritePermissionsOnMdTermRel(maintainer, mdEdgeDAO);
      permissionBuilder.grantReadPermissionsOnMdTermRel(consumer, mdEdgeDAO);
      permissionBuilder.grantReadPermissionsOnMdTermRel(contributor, mdEdgeDAO);

      BusinessEdgeType businessEdgeType = new BusinessEdgeType();
      businessEdgeType.setOrganization(organization);
      businessEdgeType.setCode(code);
      businessEdgeType.setMdEdgeId(mdEdgeDAO.getOid());
      businessEdgeType.setParentTypeId(parentType.getMdVertexOid());
      businessEdgeType.setChildTypeId(childType.getMdVertexOid());
      RegistryLocalizedValueConverter.populate(businessEdgeType.getDisplayLabel(), label);
      RegistryLocalizedValueConverter.populate(businessEdgeType.getDescription(), description);
      businessEdgeType.apply();

      return businessEdgeType;
    }
    catch (DuplicateDataException ex)
    {
      DuplicateHierarchyTypeException ex2 = new DuplicateHierarchyTypeException();
      ex2.setDuplicateValue(code);
      throw ex2;
    }
  }

}
