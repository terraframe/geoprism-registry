package com.runwaysdk.patcher.domain;

import net.geoprism.DefaultConfiguration;
import net.geoprism.ontology.Classifier;
import net.geoprism.ontology.ClassifierIsARelationship;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.service.ServiceFactory;

import org.apache.log4j.Logger;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.ontology.InitializationStrategyIF;
import com.runwaysdk.business.ontology.OntologyStrategyBuilderIF;
import com.runwaysdk.business.ontology.OntologyStrategyFactory;
import com.runwaysdk.business.ontology.OntologyStrategyIF;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdBusinessInfo;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.constants.GISConstants;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdTermRelationship;
import com.runwaysdk.system.metadata.ontology.DatabaseAllPathsStrategy;

/**
 * Fixes https://github.com/terraframe/geoprism-registry/issues/134
 * 
 * @author rrowlands
 */
public class AddWritePermissions
{
  private static Logger logger = Logger.getLogger(AddWritePermissions.class);
  
  public static void main(String[] args)
  {
    new AddWritePermissions().doIt();
  }
  
  @Transaction
  private void doIt()
  {
    initializeStrategies();
    
    HierarchyType[] htypes = ServiceFactory.getAdapter().getMetadataCache().getAllHierarchyTypes();
    
    for (HierarchyType ht : htypes)
    {
      if (ht.getCode().equals("LocatedIn"))
        continue;
      
      reassignPermissions(ht);
    }
  }
  
  private void initializeStrategies()
  {
    OntologyStrategyFactory.set(GeoEntity.CLASS, new OntologyStrategyBuilderIF()
    {
      @Override
      public OntologyStrategyIF build()
      {
        return DatabaseAllPathsStrategy.factory(GeoEntity.CLASS);
      }
    });
    
    Classifier.getStrategy().initialize(ClassifierIsARelationship.CLASS);
    Universal.getStrategy().initialize(AllowedIn.CLASS);
    GeoEntity.getStrategy().initialize(LocatedIn.CLASS);
  }
  
  private void reassignPermissions(HierarchyType hierarchyType)
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
    
    String key = GISConstants.GEO_PACKAGE + "." + hierarchyType.getCode() + RegistryConstants.UNIVERSAL_RELATIONSHIP_POST;
    if (hierarchyType.getCode().equals("LocatedIn"))
    {
      key = GISConstants.GEO_PACKAGE + "." + hierarchyType.getCode();
    }

    MdTermRelationship mdTermRelUniversal = MdTermRelationship.getByKey(key);

    this.grantWritePermissionsOnMdTermRel(mdTermRelUniversal);
    this.grantWritePermissionsOnMdTermRel(maintainer, mdTermRelUniversal);
    this.grantReadPermissionsOnMdTermRel(consumer, mdTermRelUniversal);
    this.grantReadPermissionsOnMdTermRel(contributor, mdTermRelUniversal);

    Universal.getStrategy().initialize(mdTermRelUniversal.definesType(), strategy);

    MdTermRelationship mdTermRelGeoEntity = MdTermRelationship.getByKey(GISConstants.GEO_PACKAGE + "." + hierarchyType.getCode());

    this.grantWritePermissionsOnMdTermRel(mdTermRelGeoEntity);
    this.grantWritePermissionsOnMdTermRel(maintainer, mdTermRelGeoEntity);
    this.grantReadPermissionsOnMdTermRel(consumer, mdTermRelGeoEntity);
    this.grantReadPermissionsOnMdTermRel(contributor, mdTermRelGeoEntity);

    GeoEntity.getStrategy().initialize(mdTermRelGeoEntity.definesType(), strategy);

    MdEdgeDAO mdEdge = (MdEdgeDAO) MdEdgeDAO.getMdEdgeDAO(RegistryConstants.UNIVERSAL_GRAPH_PACKAGE + "." + hierarchyType.getCode());

    this.grantWritePermissionsOnMdTermRel(mdEdge);
    this.grantWritePermissionsOnMdTermRel(maintainer, mdEdge);
    this.grantReadPermissionsOnMdTermRel(consumer, mdEdge);
    this.grantReadPermissionsOnMdTermRel(contributor, mdEdge);
    
    logger.info("Updated permissions for HierarchyType [" + hierarchyType.getCode() + "]. ");
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
}
