package net.geoprism.registry;

public class DuplicateGeoObjectException extends DuplicateGeoObjectExceptionBase
{
  private static final long serialVersionUID = -2136898;
  
  public DuplicateGeoObjectException()
  {
    super();
  }
  
  public DuplicateGeoObjectException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public DuplicateGeoObjectException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public DuplicateGeoObjectException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
