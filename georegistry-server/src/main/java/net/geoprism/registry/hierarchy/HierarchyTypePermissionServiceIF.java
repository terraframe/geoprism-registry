package net.geoprism.registry.hierarchy;

import com.runwaysdk.business.rbac.SingleActorDAOIF;

import net.geoprism.registry.Organization;
import net.geoprism.registry.model.ServerHierarchyType;

public interface HierarchyTypePermissionServiceIF
{
  
  public boolean canDelete(SingleActorDAOIF actor, String orgCode);
  
  public void enforceCanDelete(SingleActorDAOIF actor, String orgCode);
  
  public boolean canRead(SingleActorDAOIF actor, String orgCode);
  
  public void enforceCanRead(SingleActorDAOIF actor, String orgCode);

  public boolean canWrite(SingleActorDAOIF actor, String orgCode);
  
  public void enforceCanWrite(SingleActorDAOIF actor, String orgCode);
  
  public boolean canCreate(SingleActorDAOIF actor, String orgCode);
  
  public void enforceCanCreate(SingleActorDAOIF actor, String orgCode);
  
}
