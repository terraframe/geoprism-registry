package net.geoprism.dhis2.dhis2adapter.response.model;

public class Translation
{
  public static enum Property
  {
    NAME, DESCRIPTION, SHORT_NAME
  }
  
  private Property property;
  
  private String value;
  
  private String locale;

  public Property getProperty()
  {
    return property;
  }

  public void setProperty(Property property)
  {
    this.property = property;
  }

  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public String getLocale()
  {
    return locale;
  }

  public void setLocale(String locale)
  {
    this.locale = locale;
  }
}
