package net.geoprism.registry.model;

import com.runwaysdk.LocalizationFacade;

public class HierarchyTypeMetadata
{
  public static final String TYPE_LABEL = "hierarchyType.label";
  
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
    return LocalizationFacade.localize("hierarchyType.attr."  + attributeName);
  }
}
