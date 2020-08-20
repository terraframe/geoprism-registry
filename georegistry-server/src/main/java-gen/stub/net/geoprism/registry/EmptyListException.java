package net.geoprism.registry;

public class EmptyListException extends EmptyListExceptionBase
{
  private static final long serialVersionUID = -655630253;
  
  public EmptyListException()
  {
    super();
  }
  
  public EmptyListException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public EmptyListException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public EmptyListException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
