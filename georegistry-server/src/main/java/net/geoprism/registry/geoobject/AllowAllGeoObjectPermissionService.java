package net.geoprism.registry.geoobject;

import com.runwaysdk.business.rbac.SingleActorDAOIF;

import net.geoprism.registry.model.ServerGeoObjectType;

public class AllowAllGeoObjectPermissionService implements GeoObjectPermissionServiceIF
{

  @Override
  public boolean canRead(SingleActorDAOIF actor, String orgCode, String gotCode)
  {
    return true;
  }

  @Override
  public void enforceCanRead(SingleActorDAOIF actor, String orgCode, String gotCode)
  {
    
  }

  @Override
  public boolean canWrite(SingleActorDAOIF actor, String orgCode, String gotCode)
  {
    return true;
  }

  @Override
  public void enforceCanWrite(SingleActorDAOIF actor, String orgCode, String gotCode)
  {
    
  }

  @Override
  public boolean canCreate(SingleActorDAOIF actor, String orgCode, String gotCode)
  {
    return true;
  }

  @Override
  public void enforceCanCreate(SingleActorDAOIF actor, String orgCode, String gotCode)
  {
    
  }

  @Override
  public boolean canWriteCR(SingleActorDAOIF actor, String orgCode, String gotCode)
  {
    return true;
  }

  @Override
  public void enforceCanWriteCR(SingleActorDAOIF actor, String orgCode, String gotCode)
  {
    
  }

  @Override
  public boolean canCreateCR(SingleActorDAOIF actor, String orgCode, String gotCode)
  {
    return true;
  }

  @Override
  public void enforceCanCreateCR(SingleActorDAOIF actor, String orgCode, String gotCode)
  {
    
  }

}
