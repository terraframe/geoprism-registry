package net.geoprism.registry.session;

public class UserNotFoundException extends UserNotFoundExceptionBase
{
  private static final long serialVersionUID = -1734022104;
  
  public UserNotFoundException()
  {
    super();
  }
  
  public UserNotFoundException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public UserNotFoundException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public UserNotFoundException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
