package net.geoprism.registry.etl.export.dhis2;

public class InvalidGeometryException extends InvalidGeometryExceptionBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 821114494;
  
  public InvalidGeometryException()
  {
    super();
  }
  
  public InvalidGeometryException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public InvalidGeometryException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public InvalidGeometryException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
