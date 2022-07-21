package net.geoprism.registry;

public class InvalidProjectionException extends InvalidProjectionExceptionBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1237584856;
  
  public InvalidProjectionException()
  {
    super();
  }
  
  public InvalidProjectionException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public InvalidProjectionException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public InvalidProjectionException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
