package net.geoprism.registry.etl;

public class RemoteConnectionException extends RemoteConnectionExceptionBase
{
  private static final long serialVersionUID = 1403425277;
  
  public RemoteConnectionException()
  {
    super();
  }
  
  public RemoteConnectionException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public RemoteConnectionException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public RemoteConnectionException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
