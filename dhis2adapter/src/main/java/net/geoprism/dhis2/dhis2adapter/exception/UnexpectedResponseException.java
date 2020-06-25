package net.geoprism.dhis2.dhis2adapter.exception;

import net.geoprism.dhis2.dhis2adapter.response.HTTPResponse;

public class UnexpectedResponseException extends Exception
{

  private static final long serialVersionUID = -6010794787560014358L;
  
  private HTTPResponse response;
  
  private String errorMessage;
  
  public UnexpectedResponseException() {
    super();
  }
  
  public UnexpectedResponseException(String message) {
    super(message);
  }
  
  public UnexpectedResponseException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public UnexpectedResponseException(Throwable cause) {
    super(cause);
  }
  
  protected UnexpectedResponseException(String message, Throwable cause,
                      boolean enableSuppression,
                      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public HTTPResponse getResponse()
  {
    return response;
  }

  public void setResponse(HTTPResponse response)
  {
    this.response = response;
  }

  public String getErrorMessage()
  {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage)
  {
    this.errorMessage = errorMessage;
  }
  
}
