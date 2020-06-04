package net.geoprism.registry.organization;

import com.runwaysdk.business.rbac.SingleActorDAOIF;

import net.geoprism.registry.service.ServiceFactory;

public class OrganizationPermissionService
{
  
  public void enforceActorCanCreate(SingleActorDAOIF user)
  {
    ServiceFactory.getRolePermissionService().enforceSRA(user);
  }

  public void enforceActorCanUpdate(SingleActorDAOIF user)
  {
    ServiceFactory.getRolePermissionService().enforceSRA(user);
  }
  
  public boolean canActorCreate(SingleActorDAOIF user)
  {
    return ServiceFactory.getRolePermissionService().isSRA(user);
  }

  public boolean canActorUpdate(SingleActorDAOIF user)
  {
    return ServiceFactory.getRolePermissionService().isSRA(user);
  }

  public void enforceActorCanDelete(SingleActorDAOIF user)
  {
    ServiceFactory.getRolePermissionService().enforceSRA(user);
  }
  
  public boolean canActorDelete(SingleActorDAOIF user)
  {
    return ServiceFactory.getRolePermissionService().isSRA(user);
  }
  
  public boolean canActorRead(SingleActorDAOIF actor, String orgCode)
  {
    return ServiceFactory.getRolePermissionService().isRA(actor, orgCode);
  }
  
  public void enforceActorCanRead(SingleActorDAOIF actor, String orgCode)
  {
    ServiceFactory.getRolePermissionService().enforceRA(actor, orgCode);
  }
  
}
