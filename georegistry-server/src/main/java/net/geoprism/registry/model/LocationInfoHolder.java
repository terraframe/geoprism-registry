package net.geoprism.registry.model;

import java.util.Locale;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

public class LocationInfoHolder implements LocationInfo
{

  private String code;
  
  private LocalizedValue label;
  
  public LocationInfoHolder(String code, LocalizedValue label)
  {
    this.code = code;
    this.label = label;
  }
  
  public void setCode(String code)
  {
    this.code = code;
  }

  public void setLabel(LocalizedValue label)
  {
    this.label = label;
  }

  @Override
  public String getCode()
  {
    return this.code;
  }

  @Override
  public String getLabel()
  {
    return this.label.getValue();
  }

  @Override
  public String getLabel(Locale locale)
  {
    return this.label.getValue(locale);
  }
  
}
