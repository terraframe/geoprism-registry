package net.geoprism.registry.model;

import java.util.Locale;
import java.util.Map;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.runwaysdk.localization.LocalizedValueIF;

public class LocalizedValueContainer implements LocalizedValueIF
{
  private LocalizedValue value;

  public LocalizedValueContainer(LocalizedValue value)
  {
    this.value = value;
  }

  @Override
  public String getValue()
  {
    return this.value.getValue();
  }

  @Override
  public String getValue(Locale locale)
  {
    return this.value.getValue(locale);
  }

  @Override
  public void setValue(Locale locale, String value)
  {
    this.value.setValue(locale, value);
  }

  @Override
  public void setValue(String value)
  {
    this.value.setValue(value);
  }

  @Override
  public Map<String, String> getLocaleMap()
  {
    return this.value.getLocaleMap();
  }

  @Override
  public void setLocaleMap(Map<String, String> map)
  {
    this.value.setLocaleMap(map);
  }

  @Override
  public String getDefaultValue()
  {
    return this.value.getValue(LocalizedValue.DEFAULT_LOCALE);
  }

  @Override
  public void setDefaultValue(String value)
  {
    this.value.setValue(LocalizedValue.DEFAULT_LOCALE, value);
  }
}
