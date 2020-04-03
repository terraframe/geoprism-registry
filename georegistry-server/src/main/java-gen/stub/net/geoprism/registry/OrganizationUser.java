package net.geoprism.registry;

public class OrganizationUser extends OrganizationUserBase
{
  private static final long serialVersionUID = -1551724541;
  
  public OrganizationUser(String parentOid, String childOid)
  {
    super(parentOid, childOid);
  }
  
  public OrganizationUser(net.geoprism.registry.Organization parent, net.geoprism.GeoprismUser child)
  {
    this(parent.getOid(), child.getOid());
  }
  
}
