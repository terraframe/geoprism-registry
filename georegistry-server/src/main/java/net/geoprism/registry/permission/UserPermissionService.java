package net.geoprism.registry.permission;

import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.session.Session;

public class UserPermissionService
{
  public boolean hasSessionUser()
  {
    return ( Session.getCurrentSession() != null && Session.getCurrentSession().getUser() != null );
  }

  public SingleActorDAOIF getSessionUser()
  {
    return Session.getCurrentSession().getUser();
  }
}
