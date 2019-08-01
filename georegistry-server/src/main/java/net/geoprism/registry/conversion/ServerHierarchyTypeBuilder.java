package net.geoprism.registry.conversion;

import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.ontology.InitializationStrategyIF;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdBusinessInfo;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
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
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.ServiceFactory;

public class ServerHierarchyTypeBuilder extends AbstractBuilder
{
  @Transaction
  public ServerHierarchyType createHierarchyType(HierarchyType hierarchyType)
  {
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

        adminRole.grantPermission(Operation.READ_ALL, mdBusiness.getOid());
        adminRole.grantPermission(Operation.WRITE_ALL, mdBusiness.getOid());
        adminRole.grantPermission(Operation.CREATE, mdBusiness.getOid());
        adminRole.grantPermission(Operation.DELETE, mdBusiness.getOid());

        maintainer.grantPermission(Operation.READ_ALL, mdBusiness.getOid());
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

    MdTermRelationship mdTermRelGeoEntity = ServiceFactory.getConversionService().newHierarchyToMdTermRelForGeoEntities(hierarchyType);
    mdTermRelGeoEntity.apply();

    this.grantWritePermissionsOnMdTermRel(mdTermRelGeoEntity);
    this.grantWritePermissionsOnMdTermRel(maintainer, mdTermRelGeoEntity);
    this.grantReadPermissionsOnMdTermRel(consumer, mdTermRelGeoEntity);
    this.grantReadPermissionsOnMdTermRel(contributor, mdTermRelGeoEntity);

    GeoEntity.getStrategy().initialize(mdTermRelGeoEntity.definesType(), strategy);

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
    this.populate(mdTermRelationship.getDisplayLabel(), hierarchyType.getLabel());
    this.populate(mdTermRelationship.getDescription(), hierarchyType.getDescription());
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

    return mdTermRelationship;
  }

  private void grantWritePermissionsOnMdTermRel(MdTermRelationship mdTermRelationship)
  {
    RoleDAO adminRole = RoleDAO.findRole(DefaultConfiguration.ADMIN).getBusinessDAO();

    grantWritePermissionsOnMdTermRel(adminRole, mdTermRelationship);
  }

  public void grantWritePermissionsOnMdTermRel(RoleDAO role, MdTermRelationship mdTermRelationship)
  {
    role.grantPermission(Operation.ADD_PARENT, mdTermRelationship.getOid());
    role.grantPermission(Operation.ADD_CHILD, mdTermRelationship.getOid());
    role.grantPermission(Operation.DELETE_PARENT, mdTermRelationship.getOid());
    role.grantPermission(Operation.DELETE_CHILD, mdTermRelationship.getOid());
    role.grantPermission(Operation.READ_PARENT, mdTermRelationship.getOid());
    role.grantPermission(Operation.READ_CHILD, mdTermRelationship.getOid());
    role.grantPermission(Operation.READ_ALL, mdTermRelationship.getOid());
    role.grantPermission(Operation.WRITE_ALL, mdTermRelationship.getOid());
    role.grantPermission(Operation.CREATE, mdTermRelationship.getOid());
    role.grantPermission(Operation.DELETE, mdTermRelationship.getOid());
  }

  public void grantReadPermissionsOnMdTermRel(RoleDAO role, MdTermRelationship mdTermRelationship)
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
    AttributeTypeBuilder builder = new AttributeTypeBuilder();

    String hierarchyKey = ServerHierarchyType.buildHierarchyKeyFromMdTermRelUniversal(universalRelationship.getKey());
    String geoEntityKey = ServerHierarchyType.buildMdTermRelGeoEntityKey(hierarchyKey);

    MdTermRelationship entityRelationship = MdTermRelationship.getByKey(geoEntityKey);

    LocalizedValue displayLabel = builder.convert(entityRelationship.getDisplayLabel());
    LocalizedValue description = builder.convert(entityRelationship.getDescription());

    HierarchyType ht = new HierarchyType(hierarchyKey, displayLabel, description);

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

      HierarchyType.HierarchyNode node = new HierarchyType.HierarchyNode(geoObjectType.getType());

      node = buildHierarchy(node, childUniversal, universalRelationship);

      ht.addRootGeoObjects(node);
    }

    return new ServerHierarchyType(ht, universalRelationship, entityRelationship);
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
