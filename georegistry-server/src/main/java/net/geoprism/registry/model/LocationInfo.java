package net.geoprism.registry.model;

import java.util.Locale;

public interface LocationInfo
{
  public String getCode();

  public String getLabel();

  public String getLabel(Locale locale);
}
