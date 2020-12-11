package net.geoprism.registry.model;

import com.runwaysdk.LocalizationFacade;

public class GeoObjectTypeMetadata extends GeoObjectTypeMetadataBase
{
  private static final long serialVersionUID = -427820585;
  
  public static final String TYPE_LABEL = "geoObjectType.label";
  
  public GeoObjectTypeMetadata()
  {
    super();
  }
  
  @Override
  protected String buildKey()
  {
    return this.getUniversal().getKey();
  }
  
  public String getClassDisplayLabel()
  {
    return sGetClassDisplayLabel();
  }
  
  public static String sGetClassDisplayLabel()
  {
    return LocalizationFacade.localize(TYPE_LABEL);
  }
  
  public static String getAttributeDisplayLabel(String attributeName)
  {
    return LocalizationFacade.localize("geoObjectType.attr."  + attributeName);
  }
  
}
