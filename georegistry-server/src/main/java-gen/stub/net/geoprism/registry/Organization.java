package net.geoprism.registry;

public class Organization extends OrganizationBase
{
  private static final long serialVersionUID = -640706555;
  
  public Organization()
  {
    super();
  }
  
  /**
   * Builds the this object's key name.
   */
  @Override
  public String buildKey()
  {
    return this.getCode();
  }
}
