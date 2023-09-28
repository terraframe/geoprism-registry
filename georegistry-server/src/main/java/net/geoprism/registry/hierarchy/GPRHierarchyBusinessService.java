package net.geoprism.registry.hierarchy;

import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.springframework.stereotype.Component;

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
import net.geoprism.registry.Organization;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.business.HierarchyBusinessService;
import net.geoprism.registry.business.HierarchyBusinessServiceIF;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.model.ServerHierarchyType;

@Component
public class GPRHierarchyBusinessService extends HierarchyBusinessService implements HierarchyBusinessServiceIF
{
  @Transaction
  public ServerHierarchyType createHierarchyType(HierarchyType hierarchyType)
  {
    if (hierarchyType.getOrganizationCode() == null || hierarchyType.getOrganizationCode().equals(""))
    {
      // TODO : A better exception
      throw new AttributeValueException("Organization code cannot be null.", hierarchyType.getOrganizationCode());
    }

    Organization organization = Organization.getByCode(hierarchyType.getOrganizationCode());

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
      hierarchicalRelationship.setOrganization(organization);
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
