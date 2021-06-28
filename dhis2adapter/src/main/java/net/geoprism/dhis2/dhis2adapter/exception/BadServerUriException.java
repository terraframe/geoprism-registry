package net.geoprism.dhis2.dhis2adapter.exception;

import java.net.URI;

public class BadServerUriException extends Exception
{
  private static final long serialVersionUID = -8355461305287296933L;
  
  private String uri;
  
  public BadServerUriException() {
    super();
  }
  
  public BadServerUriException(String message) {
    super(message);
  }
  
  public BadServerUriException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public BadServerUriException(Throwable cause) {
    super(cause);
  }
  
  public BadServerUriException(Throwable cause, String uri) {
    super(cause);
    this.uri = uri;
  }
  
  protected BadServerUriException(String message, Throwable cause,
                      boolean enableSuppression,
                      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
  
  public BadServerUriException(String message, String uri)
  {
    super(message);
    this.uri = uri;
  }

  public String getUri()
  {
    return uri;
  }

  public void setUri(String uri)
  {
    this.uri = uri;
  }
}
