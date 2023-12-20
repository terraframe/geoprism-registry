package net.geoprism.registry.service.business;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.registry.Organization;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.permission.RolePermissionService;

@Service
@Primary
public class GPRLabeledPropertyGraphTypeBusinessService extends LabeledPropertyGraphTypeBusinessService implements LabeledPropertyGraphTypeBusinessServiceIF
{
  @Override
  public void apply(LabeledPropertyGraphType type)
  {
    ServerHierarchyType hierarchy = ServerHierarchyType.get(type.getHierarchy());

    // Ensure the user has permissions to create
    Organization organization = hierarchy.getOrganization();

    new RolePermissionService().enforceRA(organization.getCode());
    
    type.setOrganization(organization);

    super.apply(type);
  }
}
