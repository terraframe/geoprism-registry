package net.geoprism.registry;

public class UnauthorizedAccessException extends UnauthorizedAccessExceptionBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 925734298;
  
  public UnauthorizedAccessException()
  {
    super();
  }
  
  public UnauthorizedAccessException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public UnauthorizedAccessException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public UnauthorizedAccessException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
