package net.geoprism.dhis2.dhis2adapter.exception;

public class HTTPException extends Exception
{

  private static final long serialVersionUID = -6010794787560014358L;
  
  public HTTPException() {
    super();
  }
  
  public HTTPException(String message) {
    super(message);
  }
  
  public HTTPException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public HTTPException(Throwable cause) {
    super(cause);
  }
  
  protected HTTPException(String message, Throwable cause,
                      boolean enableSuppression,
                      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
  
}
