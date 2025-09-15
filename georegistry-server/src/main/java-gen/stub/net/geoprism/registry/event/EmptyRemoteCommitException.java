package net.geoprism.registry.event;

public class EmptyRemoteCommitException extends EmptyRemoteCommitExceptionBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -966205368;
  
  public EmptyRemoteCommitException()
  {
    super();
  }
  
  public EmptyRemoteCommitException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public EmptyRemoteCommitException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public EmptyRemoteCommitException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
