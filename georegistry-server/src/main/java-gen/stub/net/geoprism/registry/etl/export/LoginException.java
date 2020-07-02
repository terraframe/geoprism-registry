package net.geoprism.registry.etl.export;

public class LoginException extends LoginExceptionBase
{
  private static final long serialVersionUID = 105974130;
  
  public LoginException()
  {
    super();
  }
  
  public LoginException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public LoginException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public LoginException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
