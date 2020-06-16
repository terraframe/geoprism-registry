package net.geoprism.registry.model;

import com.runwaysdk.LocalizationFacade;

public class GeoObjectMetadata
{
  public static final String TYPE_LABEL = "geoObject.label";
  
  public static GeoObjectMetadata get()
  {
    return new GeoObjectMetadata();
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
