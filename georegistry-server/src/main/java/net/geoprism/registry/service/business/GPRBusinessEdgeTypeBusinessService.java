package net.geoprism.registry.service.business;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.transaction.Transaction;

import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.RegistryConstants;

@Service
@Primary
public class GPRBusinessEdgeTypeBusinessService extends BusinessEdgeTypeBusinessService implements BusinessEdgeTypeBusinessServiceIF
{
  @Autowired
  private HierarchyTypeBusinessServiceIF hierarchyService;

  @Override
  @Transaction
  public BusinessEdgeType create(String organizationCode, String code, LocalizedValue label, LocalizedValue description, String parentTypeCode, String childTypeCode)
  {
    BusinessEdgeType edgeType = super.create(organizationCode, code, label, description, parentTypeCode, childTypeCode);
    

    RoleDAO maintainer = RoleDAO.findRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE).getBusinessDAO();
    RoleDAO consumer = RoleDAO.findRole(RegistryConstants.API_CONSUMER_ROLE).getBusinessDAO();
    RoleDAO contributor = RoleDAO.findRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE).getBusinessDAO();
    
    MdEdgeDAOIF mdEdgeDAO = edgeType.getMdEdgeDAO();

    hierarchyService.grantWritePermissionsOnMdTermRel(mdEdgeDAO);
    hierarchyService.grantWritePermissionsOnMdTermRel(maintainer, mdEdgeDAO);
    hierarchyService.grantReadPermissionsOnMdTermRel(consumer, mdEdgeDAO);
    hierarchyService.grantReadPermissionsOnMdTermRel(contributor, mdEdgeDAO);
    
    return edgeType;
  }
}
