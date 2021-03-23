package net.geoprism.registry.action;

public interface GovernancePermissionEntity
{
  public String getOrganization();
  
  public String getGeoObjectType();
  
  public AllGovernanceStatus getGovernanceStatus();
}
