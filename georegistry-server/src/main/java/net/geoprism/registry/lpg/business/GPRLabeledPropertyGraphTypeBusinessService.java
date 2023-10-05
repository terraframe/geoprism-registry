package net.geoprism.registry.lpg.business;

import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.lpg.business.LabeledPropertyGraphTypeBusinessService;
import net.geoprism.graph.lpg.business.LabeledPropertyGraphTypeBusinessServiceIF;
import net.geoprism.registry.Organization;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.permission.RolePermissionService;

public class GPRLabeledPropertyGraphTypeBusinessService extends LabeledPropertyGraphTypeBusinessService implements LabeledPropertyGraphTypeBusinessServiceIF
{
  @Override
  public void apply(LabeledPropertyGraphType type)
  {
    ServerHierarchyType hierarchy = ServerHierarchyType.get(type.getHierarchy());

    // Ensure the user has permissions to create
    Organization organization = hierarchy.getOrganization();

    new RolePermissionService().enforceRA(organization.getCode());

    super.apply(type);
  }
}
