package net.geoprism.registry.etl.export;

public class UnsupportedGeometryException extends UnsupportedGeometryExceptionBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 501859670;
  
  public UnsupportedGeometryException()
  {
    super();
  }
  
  public UnsupportedGeometryException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public UnsupportedGeometryException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public UnsupportedGeometryException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
