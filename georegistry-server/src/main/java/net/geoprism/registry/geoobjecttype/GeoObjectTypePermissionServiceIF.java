package net.geoprism.registry.geoobjecttype;

import com.runwaysdk.business.rbac.SingleActorDAOIF;

import net.geoprism.registry.Organization;
import net.geoprism.registry.model.ServerGeoObjectType;

public interface GeoObjectTypePermissionServiceIF
{
  
  public boolean canRead(SingleActorDAOIF actor, String orgCode);
  
  public void enforceCanRead(SingleActorDAOIF actor, String orgCode, String gotLabel);

  public boolean canWrite(SingleActorDAOIF actor, String orgCode);
  
  public void enforceCanWrite(SingleActorDAOIF actor, String orgCode, String gotLabel);
  
  public boolean canCreate(SingleActorDAOIF actor, String orgCode);
  
  public void enforceCanCreate(SingleActorDAOIF actor, String orgCode);
  
  public boolean canDelete(SingleActorDAOIF actor, String orgCode);
  
  public void enforceCanDelete(SingleActorDAOIF actor, String orgCode, String gotLabel);
  
}
