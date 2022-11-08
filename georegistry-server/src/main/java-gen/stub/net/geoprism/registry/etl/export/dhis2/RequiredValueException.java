package net.geoprism.registry.etl.export.dhis2;

public class RequiredValueException extends RequiredValueExceptionBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1924205026;
  
  public RequiredValueException()
  {
    super();
  }
  
  public RequiredValueException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public RequiredValueException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public RequiredValueException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
