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

import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.ontology.InitializationStrategyIF;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdBusinessInfo;
import com.runwaysdk.dataaccess.DuplicateDataException;
import com.runwaysdk.dataaccess.attributes.AttributeValueException;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdEdge;
import com.runwaysdk.system.metadata.MdTermRelationship;

import net.geoprism.rbac.RoleConstants;
import net.geoprism.registry.CodeLengthException;
import net.geoprism.registry.DuplicateHierarchyTypeException;
import net.geoprism.registry.HierarchicalRelationshipType;
import net.geoprism.registry.ListType;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.request.SerializedListTypeCache;

@Service
@Primary
public class GPRHierarchyTypeBusinessService extends HierarchyTypeBusinessService implements HierarchyTypeBusinessServiceIF
{
  @Override
  public void refresh(ServerHierarchyType sht)
  {
    super.refresh(sht);
    SerializedListTypeCache.getInstance().clear();
  }

  @Override
  @Transaction
  protected void deleteInTrans(ServerHierarchyType sht)
  {
    super.deleteInTrans(sht);

    ListType.markAllAsInvalid(sht, null);
  }

  @Override
  @Transaction
  protected void removeFromHierarchy(ServerHierarchyType sht, ServerGeoObjectType parentType, ServerGeoObjectType childType, boolean migrateChildren)
  {
    super.removeFromHierarchy(sht, parentType, childType, migrateChildren);

    ListType.markAllAsInvalid(sht, childType);
    SerializedListTypeCache.getInstance().clear();
  }

  @Transaction
  public ServerHierarchyType createHierarchyType(HierarchyType hierarchyType)
  {
    if (hierarchyType.getOrganizationCode() == null || hierarchyType.getOrganizationCode().equals(""))
    {
      // TODO : A better exception
      throw new AttributeValueException("Organization code cannot be null.", hierarchyType.getOrganizationCode());
    }

    ServerOrganization organization = ServerOrganization.getByCode(hierarchyType.getOrganizationCode());

    if (organization != null && !organization.getEnabled())
    {
      throw new UnsupportedOperationException();
    }

    String addons = RegistryConstants.UNIVERSAL_RELATIONSHIP_POST + "AllPathsTable";

    if (hierarchyType.getCode().length() > ( 64 - addons.length() ))
    {
      // Initializing the Universal allpaths strategy creates this limitation.
      CodeLengthException ex = new CodeLengthException();
      ex.setLength(64 - addons.length());
      throw ex;
    }

    RoleDAO maintainer = RoleDAO.findRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE).getBusinessDAO();
    RoleDAO consumer = RoleDAO.findRole(RegistryConstants.API_CONSUMER_ROLE).getBusinessDAO();
    RoleDAO contributor = RoleDAO.findRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE).getBusinessDAO();

    InitializationStrategyIF strategy = new InitializationStrategyIF()
    {
      @Override
      public void preApply(MdBusinessDAO mdBusiness)
      {
        mdBusiness.setValue(MdBusinessInfo.GENERATE_SOURCE, MdAttributeBooleanInfo.FALSE);
      }

      @Override
      public void postApply(MdBusinessDAO mdBusiness)
      {
        RoleDAO adminRole = RoleDAO.findRole(RoleConstants.ADMIN).getBusinessDAO();

        adminRole.grantPermission(Operation.READ, mdBusiness.getOid());
        adminRole.grantPermission(Operation.READ_ALL, mdBusiness.getOid());
        adminRole.grantPermission(Operation.WRITE, mdBusiness.getOid());
        adminRole.grantPermission(Operation.WRITE_ALL, mdBusiness.getOid());
        adminRole.grantPermission(Operation.CREATE, mdBusiness.getOid());
        adminRole.grantPermission(Operation.DELETE, mdBusiness.getOid());

        maintainer.grantPermission(Operation.READ, mdBusiness.getOid());
        maintainer.grantPermission(Operation.READ_ALL, mdBusiness.getOid());
        maintainer.grantPermission(Operation.WRITE, mdBusiness.getOid());
        maintainer.grantPermission(Operation.WRITE_ALL, mdBusiness.getOid());
        maintainer.grantPermission(Operation.CREATE, mdBusiness.getOid());
        maintainer.grantPermission(Operation.DELETE, mdBusiness.getOid());

        consumer.grantPermission(Operation.READ, mdBusiness.getOid());
        consumer.grantPermission(Operation.READ_ALL, mdBusiness.getOid());

        contributor.grantPermission(Operation.READ, mdBusiness.getOid());
        contributor.grantPermission(Operation.READ_ALL, mdBusiness.getOid());
      }
    };

    try
    {
      MdTermRelationship mdTermRelUniversal = this.newHierarchyToMdTermRelForUniversals(hierarchyType);
      mdTermRelUniversal.apply();

      this.grantWritePermissionsOnMdTermRel(mdTermRelUniversal);
      this.grantWritePermissionsOnMdTermRel(maintainer, mdTermRelUniversal);
      this.grantReadPermissionsOnMdTermRel(consumer, mdTermRelUniversal);
      this.grantReadPermissionsOnMdTermRel(contributor, mdTermRelUniversal);

      Universal.getStrategy().initialize(mdTermRelUniversal.definesType(), strategy);

      MdEdge mdEdge = this.createMdEdge(hierarchyType);

      this.grantWritePermissionsOnMdTermRel(mdEdge);
      this.grantWritePermissionsOnMdTermRel(maintainer, mdEdge);
      this.grantReadPermissionsOnMdTermRel(consumer, mdEdge);
      this.grantReadPermissionsOnMdTermRel(contributor, mdEdge);

      HierarchicalRelationshipType hierarchicalRelationship = new HierarchicalRelationshipType();
      hierarchicalRelationship.setCode(hierarchyType.getCode());
      hierarchicalRelationship.setOrganization(organization.getOrganization());
      LocalizedValueConverter.populate(hierarchicalRelationship.getDisplayLabel(), hierarchyType.getLabel());
      LocalizedValueConverter.populate(hierarchicalRelationship.getDescription(), hierarchyType.getDescription());
      hierarchicalRelationship.setMdTermRelationship(mdTermRelUniversal);
      hierarchicalRelationship.setMdEdge(mdEdge);
      hierarchicalRelationship.setAbstractDescription(hierarchyType.getAbstractDescription());
      hierarchicalRelationship.setAcknowledgement(hierarchyType.getAcknowledgement());
      hierarchicalRelationship.setDisclaimer(hierarchyType.getDisclaimer());
      hierarchicalRelationship.setContact(hierarchyType.getContact());
      hierarchicalRelationship.setPhoneNumber(hierarchyType.getPhoneNumber());
      hierarchicalRelationship.setEmail(hierarchyType.getEmail());
      hierarchicalRelationship.setProgress(hierarchyType.getProgress());
      hierarchicalRelationship.setAccessConstraints(hierarchyType.getAccessConstraints());
      hierarchicalRelationship.setUseConstraints(hierarchyType.getUseConstraints());
      hierarchicalRelationship.apply();

      return this.get(hierarchicalRelationship);
    }
    catch (DuplicateDataException ex)
    {
      DuplicateHierarchyTypeException ex2 = new DuplicateHierarchyTypeException();
      ex2.setDuplicateValue(hierarchyType.getCode());
      throw ex2;
    }
  }
}
