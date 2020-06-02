package net.geoprism.registry.geoobject;

import com.runwaysdk.business.rbac.SingleActorDAOIF;

import net.geoprism.registry.model.ServerGeoObjectType;

public interface GeoObjectPermissionServiceIF
{
  
  public boolean canRead(SingleActorDAOIF actor, ServerGeoObjectType got);
  
  public void enforceCanRead(SingleActorDAOIF actor, ServerGeoObjectType got);

  public boolean canWrite(SingleActorDAOIF actor, ServerGeoObjectType got);
  
  public void enforceCanWrite(SingleActorDAOIF actor, ServerGeoObjectType got);
  
  public boolean canCreate(SingleActorDAOIF actor, ServerGeoObjectType got);
  
  public void enforceCanCreate(SingleActorDAOIF actor, ServerGeoObjectType got);
  
  public boolean canWriteCR(SingleActorDAOIF actor, ServerGeoObjectType got);
  
  public void enforceCanWriteCR(SingleActorDAOIF actor, ServerGeoObjectType got);
  
  public boolean canCreateCR(SingleActorDAOIF actor, ServerGeoObjectType got);
  
  public void enforceCanCreateCR(SingleActorDAOIF actor, ServerGeoObjectType got);
  
}
