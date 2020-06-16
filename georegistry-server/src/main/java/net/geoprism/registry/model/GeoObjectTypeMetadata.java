package net.geoprism.registry.model;

import com.runwaysdk.LocalizationFacade;

public class GeoObjectTypeMetadata
{
  public static final String TYPE_LABEL = "geoObjectType.label";
  
  public static GeoObjectTypeMetadata get()
  {
    return new GeoObjectTypeMetadata();
  }
  
  public String getClassDisplayLabel()
  {
    return LocalizationFacade.localize(TYPE_LABEL);
  }
  
  public String getAttributeDisplayLabel(String attributeName)
  {
    return LocalizationFacade.localize("geoObjectType.attr."  + attributeName);
  }
}
