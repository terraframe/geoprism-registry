package net.geoprism.registry.event;

public class EmptyPublishException extends EmptyPublishExceptionBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 469122428;
  
  public EmptyPublishException()
  {
    super();
  }
  
  public EmptyPublishException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public EmptyPublishException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public EmptyPublishException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
