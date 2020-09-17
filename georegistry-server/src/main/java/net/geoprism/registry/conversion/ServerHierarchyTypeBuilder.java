/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.conversion;

import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.Business;
import com.runwaysdk.business.ontology.InitializationStrategyIF;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdAttributeDateTimeInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.MdBusinessInfo;
import com.runwaysdk.constants.graph.MdEdgeInfo;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.attributes.AttributeValueException;
import com.runwaysdk.dataaccess.metadata.MdAttributeDateTimeDAO;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.constants.GISConstants;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.AssociationType;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdTermRelationship;
import com.runwaysdk.system.metadata.RelationshipCache;

import net.geoprism.DefaultConfiguration;
import net.geoprism.registry.InvalidMasterListCodeException;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.Organization;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;

public class ServerHierarchyTypeBuilder extends LocalizedValueConverter
{
  @Transaction
  public ServerHierarchyType createHierarchyType(HierarchyType hierarchyType)
  {
    if (hierarchyType.getOrganizationCode() == null || hierarchyType.getOrganizationCode().equals(""))
    {
      // TODO : A better exception
      throw new AttributeValueException("Organization code cannot be null.", hierarchyType.getOrganizationCode());
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
        RoleDAO adminRole = RoleDAO.findRole(DefaultConfiguration.ADMIN).getBusinessDAO();

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

    MdTermRelationship mdTermRelUniversal = this.newHierarchyToMdTermRelForUniversals(hierarchyType);
    mdTermRelUniversal.apply();

    this.grantWritePermissionsOnMdTermRel(mdTermRelUniversal);
    this.grantWritePermissionsOnMdTermRel(maintainer, mdTermRelUniversal);
    this.grantReadPermissionsOnMdTermRel(consumer, mdTermRelUniversal);
    this.grantReadPermissionsOnMdTermRel(contributor, mdTermRelUniversal);

    Universal.getStrategy().initialize(mdTermRelUniversal.definesType(), strategy);

    MdTermRelationship mdTermRelGeoEntity = this.newHierarchyToMdTermRelForGeoEntities(hierarchyType);
    mdTermRelGeoEntity.apply();

    this.grantWritePermissionsOnMdTermRel(mdTermRelGeoEntity);
    this.grantWritePermissionsOnMdTermRel(maintainer, mdTermRelGeoEntity);
    this.grantReadPermissionsOnMdTermRel(consumer, mdTermRelGeoEntity);
    this.grantReadPermissionsOnMdTermRel(contributor, mdTermRelGeoEntity);

    MdEdgeDAO mdEdge = this.createMdEdge(hierarchyType);

    this.grantWritePermissionsOnMdTermRel(mdEdge);
    this.grantWritePermissionsOnMdTermRel(maintainer, mdEdge);
    this.grantReadPermissionsOnMdTermRel(consumer, mdEdge);
    this.grantReadPermissionsOnMdTermRel(contributor, mdEdge);

    return this.get(mdTermRelUniversal);
  }

  /**
   * It creates an {@link MdTermRelationship} to model the relationship between
   * {@link Universal}s.
   * 
   * Needs to occur in a transaction.
   * 
   * @param hierarchyType
   * @return
   */
  public MdTermRelationship newHierarchyToMdTermRelForUniversals(HierarchyType hierarchyType)
  {
    if (!MasterList.isValidName(hierarchyType.getCode()))
    {
      throw new InvalidMasterListCodeException("The hierarchy type code has an invalid character");
    }

    MdBusiness mdBusUniversal = MdBusiness.getMdBusiness(Universal.CLASS);

    MdTermRelationship mdTermRelationship = new MdTermRelationship();

    mdTermRelationship.setTypeName(hierarchyType.getCode() + RegistryConstants.UNIVERSAL_RELATIONSHIP_POST);
    mdTermRelationship.setPackageName(GISConstants.GEO_PACKAGE);
    populate(mdTermRelationship.getDisplayLabel(), hierarchyType.getLabel());
    populate(mdTermRelationship.getDescription(), hierarchyType.getDescription());
    mdTermRelationship.setIsAbstract(false);
    mdTermRelationship.setGenerateSource(false);
    mdTermRelationship.addCacheAlgorithm(RelationshipCache.CACHE_EVERYTHING);
    mdTermRelationship.addAssociationType(AssociationType.Graph);
    mdTermRelationship.setRemove(true);
    // Create the relationship between different universals.
    mdTermRelationship.setParentMdBusiness(mdBusUniversal);
    mdTermRelationship.setParentCardinality("1");
    mdTermRelationship.setChildMdBusiness(mdBusUniversal);
    mdTermRelationship.setChildCardinality("*");
    mdTermRelationship.setParentMethod("Parent");
    mdTermRelationship.setChildMethod("Children");

    // Set the owner of the universal to the id of the corresponding role of the
    // responsible organization.
    String organizationCode = hierarchyType.getOrganizationCode();
    setOwner(mdTermRelationship, organizationCode);

    return mdTermRelationship;
  }

  /**
   * It creates an {@link MdTermRelationship} to model the relationship between
   * {@link GeoEntity}s.
   * 
   * Needs to occur in a transaction.
   * 
   * @param hierarchyType
   * @return
   */
  public MdTermRelationship newHierarchyToMdTermRelForGeoEntities(HierarchyType hierarchyType)
  {
    MdBusiness mdBusGeoEntity = MdBusiness.getMdBusiness(GeoEntity.CLASS);

    MdTermRelationship mdTermRelationship = new MdTermRelationship();

    mdTermRelationship.setTypeName(hierarchyType.getCode());
    mdTermRelationship.setPackageName(GISConstants.GEO_PACKAGE);
    populate(mdTermRelationship.getDisplayLabel(), hierarchyType.getLabel());
    populate(mdTermRelationship.getDescription(), hierarchyType.getDescription());
    mdTermRelationship.setIsAbstract(false);
    mdTermRelationship.setGenerateSource(false);
    mdTermRelationship.addCacheAlgorithm(RelationshipCache.CACHE_NOTHING);
    mdTermRelationship.addAssociationType(AssociationType.Graph);
    mdTermRelationship.setRemove(true);
    // Create the relationship between different universals.
    mdTermRelationship.setParentMdBusiness(mdBusGeoEntity);
    mdTermRelationship.setParentCardinality("1");
    mdTermRelationship.setChildMdBusiness(mdBusGeoEntity);
    mdTermRelationship.setChildCardinality("*");
    mdTermRelationship.setParentMethod("Parent");
    mdTermRelationship.setChildMethod("Children");

    // Set the owner of the universal to the id of the corresponding role of the
    // responsible organization.
    String organizationCode = hierarchyType.getOrganizationCode();
    setOwner(mdTermRelationship, organizationCode);

    return mdTermRelationship;
  }

  /**
   * It creates an {@link MdTermRelationship} to model the relationship between
   * {@link GeoEntity}s.
   * 
   * Needs to occur in a transaction.
   * 
   * @param hierarchyType
   * @return
   */
  public MdEdgeDAO createMdEdge(HierarchyType hierarchyType)
  {
    MdVertexDAOIF mdBusGeoEntity = MdVertexDAO.getMdVertexDAO(GeoVertex.CLASS);

    MdEdgeDAO mdEdgeDAO = MdEdgeDAO.newInstance();
    mdEdgeDAO.setValue(MdEdgeInfo.PACKAGE, RegistryConstants.UNIVERSAL_GRAPH_PACKAGE);
    mdEdgeDAO.setValue(MdEdgeInfo.NAME, hierarchyType.getCode());
    mdEdgeDAO.setValue(MdEdgeInfo.PARENT_MD_VERTEX, mdBusGeoEntity.getOid());
    mdEdgeDAO.setValue(MdEdgeInfo.CHILD_MD_VERTEX, mdBusGeoEntity.getOid());
    populate(mdEdgeDAO, MdEdgeInfo.DISPLAY_LABEL, hierarchyType.getLabel());
    populate(mdEdgeDAO, MdEdgeInfo.DESCRIPTION, hierarchyType.getDescription());
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

    return mdEdgeDAO;
  }

  private void grantWritePermissionsOnMdTermRel(ComponentIF mdTermRelationship)
  {
    RoleDAO adminRole = RoleDAO.findRole(DefaultConfiguration.ADMIN).getBusinessDAO();

    grantWritePermissionsOnMdTermRel(adminRole, mdTermRelationship);
  }

  public void grantWritePermissionsOnMdTermRel(RoleDAO role, ComponentIF mdTermRelationship)
  {
    role.grantPermission(Operation.ADD_PARENT, mdTermRelationship.getOid());
    role.grantPermission(Operation.ADD_CHILD, mdTermRelationship.getOid());
    role.grantPermission(Operation.DELETE_PARENT, mdTermRelationship.getOid());
    role.grantPermission(Operation.DELETE_CHILD, mdTermRelationship.getOid());
    role.grantPermission(Operation.READ_PARENT, mdTermRelationship.getOid());
    role.grantPermission(Operation.READ_CHILD, mdTermRelationship.getOid());
    role.grantPermission(Operation.READ_ALL, mdTermRelationship.getOid());
    role.grantPermission(Operation.READ, mdTermRelationship.getOid());
    role.grantPermission(Operation.WRITE_ALL, mdTermRelationship.getOid());
    role.grantPermission(Operation.WRITE, mdTermRelationship.getOid());
    role.grantPermission(Operation.CREATE, mdTermRelationship.getOid());
    role.grantPermission(Operation.DELETE, mdTermRelationship.getOid());
  }

  public void grantReadPermissionsOnMdTermRel(RoleDAO role, ComponentIF mdTermRelationship)
  {
    role.grantPermission(Operation.READ, mdTermRelationship.getOid());
    role.grantPermission(Operation.READ_ALL, mdTermRelationship.getOid());
  }

  /**
   * 
   * @param universalRelationship
   * @return
   */
  public ServerHierarchyType get(MdTermRelationship universalRelationship)
  {
    String hierarchyKey = ServerHierarchyType.buildHierarchyKeyFromMdTermRelUniversal(universalRelationship.getKey());
    String geoEntityKey = ServerHierarchyType.buildMdTermRelGeoEntityKey(hierarchyKey);
    String mdEdgeKey = ServerHierarchyType.buildMdEdgeKey(hierarchyKey);

    MdTermRelationship entityRelationship = MdTermRelationship.getByKey(geoEntityKey);
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(mdEdgeKey);

    LocalizedValue displayLabel = AttributeTypeConverter.convert(entityRelationship.getDisplayLabel());
    LocalizedValue description = AttributeTypeConverter.convert(entityRelationship.getDescription());

    String ownerActerOid = universalRelationship.getOwnerId();
    String organizationCode = Organization.getRootOrganizationCode(ownerActerOid);

    HierarchyType ht = new HierarchyType(hierarchyKey, displayLabel, description, organizationCode);

    Universal rootUniversal = Universal.getByKey(Universal.ROOT);

    // Copy all of the children to a list so as not to have recursion with open
    // database cursors.
    List<Universal> childUniversals = new LinkedList<Universal>();

    OIterator<? extends Business> i = rootUniversal.getChildren(universalRelationship.definesType());
    try
    {
      i.forEach(u -> childUniversals.add((Universal) u));
    }
    finally
    {
      i.close();
    }

    for (Universal childUniversal : childUniversals)
    {
      ServerGeoObjectType geoObjectType = ServerGeoObjectType.get(childUniversal);
      ServerHierarchyType inheritedHierarchy = geoObjectType.getInheritedHierarchy(universalRelationship);

      if (inheritedHierarchy != null)
      {
        HierarchyType.HierarchyNode child = new HierarchyType.HierarchyNode(geoObjectType.getType(), false);
        HierarchyType.HierarchyNode root = child;

        List<GeoObjectType> ancestors = geoObjectType.getTypeAncestors(inheritedHierarchy, true);

        for (GeoObjectType ancestor : ancestors)
        {
          HierarchyType.HierarchyNode cNode = new HierarchyType.HierarchyNode(ancestor, true);
          cNode.addChild(root);

          root = cNode;
        }

        buildHierarchy(child, childUniversal, universalRelationship);
        ht.addRootGeoObjects(root);
      }
      else
      {
        HierarchyType.HierarchyNode node = new HierarchyType.HierarchyNode(geoObjectType.getType());
        node = buildHierarchy(node, childUniversal, universalRelationship);
        ht.addRootGeoObjects(node);
      }

    }

    return new ServerHierarchyType(ht, universalRelationship, entityRelationship, mdEdge);
  }

  private HierarchyType.HierarchyNode buildHierarchy(HierarchyType.HierarchyNode parentNode, Universal parentUniversal, MdTermRelationship mdTermRel)
  {
    List<Universal> childUniversals = new LinkedList<Universal>();

    OIterator<? extends Business> i = parentUniversal.getChildren(mdTermRel.definesType());
    try
    {
      i.forEach(u -> childUniversals.add((Universal) u));
    }
    finally
    {
      i.close();
    }

    for (Universal childUniversal : childUniversals)
    {
      ServerGeoObjectType geoObjectType = ServerGeoObjectType.get(childUniversal);

      HierarchyType.HierarchyNode node = new HierarchyType.HierarchyNode(geoObjectType.getType());

      node = buildHierarchy(node, childUniversal, mdTermRel);

      parentNode.addChild(node);
    }

    return parentNode;

  }

}
