package net.geoprism.registry.io;

public class InvalidGeometryException extends InvalidGeometryExceptionBase
{
  private static final long serialVersionUID = 526944369;
  
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
