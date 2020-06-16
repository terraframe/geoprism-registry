package net.geoprism.registry.model;

import com.runwaysdk.LocalizationFacade;

public class OrganizationMetadata
{
  public static final String TYPE_LABEL = "organization.label";
  
  public static OrganizationMetadata get()
  {
    return new OrganizationMetadata();
  }
  
  public String getClassDisplayLabel()
  {
    return LocalizationFacade.localize(TYPE_LABEL);
  }
  
  public String getAttributeDisplayLabel(String attributeName)
  {
    return LocalizationFacade.localize("organization.attr."  + attributeName);
  }
}
