package net.geoprism.registry.etl.export;

public class HttpError extends HttpErrorBase
{
  private static final long serialVersionUID = 496832806;
  
  public HttpError()
  {
    super();
  }
  
  public HttpError(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public HttpError(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public HttpError(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
