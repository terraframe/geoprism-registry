package net.geoprism.registry.action;

public class InvalidChangeRequestException extends InvalidChangeRequestExceptionBase
{
  private static final long serialVersionUID = 606351806;
  
  public InvalidChangeRequestException()
  {
    super();
  }
  
  public InvalidChangeRequestException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public InvalidChangeRequestException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public InvalidChangeRequestException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
