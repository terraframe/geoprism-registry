package net.geoprism.registry.geoobject;

import com.runwaysdk.business.rbac.SingleActorDAOIF;

import net.geoprism.registry.model.ServerGeoObjectType;

public class AllowAllGeoObjectPermissionService implements GeoObjectPermissionServiceIF
{

  @Override
  public boolean canRead(SingleActorDAOIF actor, ServerGeoObjectType got)
  {
    return true;
  }

  @Override
  public void enforceCanRead(SingleActorDAOIF actor, ServerGeoObjectType got)
  {
    
  }

  @Override
  public boolean canWrite(SingleActorDAOIF actor, ServerGeoObjectType got)
  {
    return true;
  }

  @Override
  public void enforceCanWrite(SingleActorDAOIF actor, ServerGeoObjectType got)
  {
    
  }

  @Override
  public boolean canCreate(SingleActorDAOIF actor, ServerGeoObjectType got)
  {
    return true;
  }

  @Override
  public void enforceCanCreate(SingleActorDAOIF actor, ServerGeoObjectType got)
  {
    
  }

  @Override
  public boolean canWriteCR(SingleActorDAOIF actor, ServerGeoObjectType got)
  {
    return true;
  }

  @Override
  public void enforceCanWriteCR(SingleActorDAOIF actor, ServerGeoObjectType got)
  {
    
  }

  @Override
  public boolean canCreateCR(SingleActorDAOIF actor, ServerGeoObjectType got)
  {
    return true;
  }

  @Override
  public void enforceCanCreateCR(SingleActorDAOIF actor, ServerGeoObjectType got)
  {
    
  }
  
}
