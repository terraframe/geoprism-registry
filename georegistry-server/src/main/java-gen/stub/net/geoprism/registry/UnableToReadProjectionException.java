package net.geoprism.registry;

public class UnableToReadProjectionException extends UnableToReadProjectionExceptionBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1592081471;
  
  public UnableToReadProjectionException()
  {
    super();
  }
  
  public UnableToReadProjectionException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public UnableToReadProjectionException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public UnableToReadProjectionException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
