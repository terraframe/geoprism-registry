package net.geoprism.registry.service.permission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import net.geoprism.registry.BusinessType;
import net.geoprism.registry.model.ServerOrganization;

@Service
@Primary
public class GPRPermissionService extends PermissionService implements PermissionServiceIF
{
  @Autowired
  private RolePermissionService rolePermissions;

  @Override
  public boolean canWrite(BusinessType type)
  {
    ServerOrganization organization = ServerOrganization.get(type.getOrganization());

    return rolePermissions.isRA(organization.getCode()) || rolePermissions.isSRA();
  }

  @Override
  public boolean canRead(BusinessType type)
  {
    ServerOrganization organization = ServerOrganization.get(type.getOrganization());

    return ServerOrganization.isMember(organization) || rolePermissions.isSRA();
  }

  @Override
  public boolean isAdmin(ServerOrganization organization)
  {
    return rolePermissions.isRA(organization.getCode()) || rolePermissions.isSRA();
  }

  @Override
  public boolean isMember(ServerOrganization organization)
  {
    return ServerOrganization.isMember(organization) || rolePermissions.isSRA();
  }

}
