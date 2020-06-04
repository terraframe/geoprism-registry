package net.geoprism.registry.geoobject;

import com.runwaysdk.business.rbac.SingleActorDAOIF;

import net.geoprism.registry.model.ServerGeoObjectType;

public interface GeoObjectPermissionServiceIF
{
  
  public boolean canRead(SingleActorDAOIF actor, String orgCode, String gotCode);
  
  public void enforceCanRead(SingleActorDAOIF actor, String orgCode, String gotCode);

  public boolean canWrite(SingleActorDAOIF actor, String orgCode, String gotCode);
  
  public void enforceCanWrite(SingleActorDAOIF actor, String orgCode, String gotCode);
  
  public boolean canCreate(SingleActorDAOIF actor, String orgCode, String gotCode);
  
  public void enforceCanCreate(SingleActorDAOIF actor, String orgCode, String gotCode);
  
  public boolean canWriteCR(SingleActorDAOIF actor, String orgCode, String gotCode);
  
  public void enforceCanWriteCR(SingleActorDAOIF actor, String orgCode, String gotCode);
  
  public boolean canCreateCR(SingleActorDAOIF actor, String orgCode, String gotCode);
  
  public void enforceCanCreateCR(SingleActorDAOIF actor, String orgCode, String gotCode);
  
}
