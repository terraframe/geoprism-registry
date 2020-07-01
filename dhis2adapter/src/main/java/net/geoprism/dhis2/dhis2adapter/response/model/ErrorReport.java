package net.geoprism.dhis2.dhis2adapter.response.model;

public class ErrorReport
{
  protected String message;
  
  protected String mainKlass;
  
  protected String errorCode;
  
  protected String mainId;
  
  protected String errorProperty;
  
  protected String[] errorProperties;

  public String getMessage()
  {
    return message;
  }

  public void setMessage(String message)
  {
    this.message = message;
  }

  public String getMainKlass()
  {
    return mainKlass;
  }

  public void setMainKlass(String mainKlass)
  {
    this.mainKlass = mainKlass;
  }

  public String getErrorCode()
  {
    return errorCode;
  }

  public void setErrorCode(String errorCode)
  {
    this.errorCode = errorCode;
  }

  public String getMainId()
  {
    return mainId;
  }

  public void setMainId(String mainId)
  {
    this.mainId = mainId;
  }

  public String getErrorProperty()
  {
    return errorProperty;
  }

  public void setErrorProperty(String errorProperty)
  {
    this.errorProperty = errorProperty;
  }

  public String[] getErrorProperties()
  {
    return errorProperties;
  }

  public void setErrorProperties(String[] errorProperties)
  {
    this.errorProperties = errorProperties;
  }
}