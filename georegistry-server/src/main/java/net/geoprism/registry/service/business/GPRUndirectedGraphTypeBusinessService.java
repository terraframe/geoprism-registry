package net.geoprism.registry.service.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;

import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.business.UndirectedGraphTypeBusinessService;
import net.geoprism.registry.business.UndirectedGraphTypeBusinessServiceIF;

@Service
@Primary
public class GPRUndirectedGraphTypeBusinessService extends UndirectedGraphTypeBusinessService implements UndirectedGraphTypeBusinessServiceIF
{
  @Autowired
  protected GPRHierarchyTypeBusinessService htService;
  
  @Override
  protected void createPermissions(MdEdgeDAO mdEdgeDAO)
  {
    RoleDAO maintainer = RoleDAO.findRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE).getBusinessDAO();
    RoleDAO consumer = RoleDAO.findRole(RegistryConstants.API_CONSUMER_ROLE).getBusinessDAO();
    RoleDAO contributor = RoleDAO.findRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE).getBusinessDAO();
    
    htService.grantWritePermissionsOnMdTermRel(mdEdgeDAO);
    htService.grantWritePermissionsOnMdTermRel(maintainer, mdEdgeDAO);
    htService.grantReadPermissionsOnMdTermRel(consumer, mdEdgeDAO);
    htService.grantReadPermissionsOnMdTermRel(contributor, mdEdgeDAO);
  }
}
