package net.geoprism.registry.etl.export;

public class UnexpectedRemoteResponse extends UnexpectedRemoteResponseBase
{
  private static final long serialVersionUID = -275875676;
  
  public UnexpectedRemoteResponse()
  {
    super();
  }
  
  public UnexpectedRemoteResponse(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public UnexpectedRemoteResponse(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public UnexpectedRemoteResponse(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
