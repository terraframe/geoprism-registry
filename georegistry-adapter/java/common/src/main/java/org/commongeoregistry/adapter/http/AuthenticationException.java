/**
 *
 */
package org.commongeoregistry.adapter.http;

public class AuthenticationException extends Exception
{

  /**
   * 
   */
  private static final long serialVersionUID = -3407507218050206209L;

  public AuthenticationException()
  {
    super();
  }

  public AuthenticationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
  {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public AuthenticationException(String message, Throwable cause)
  {
    super(message, cause);
  }

  public AuthenticationException(String message)
  {
    super(message);
  }

  public AuthenticationException(Throwable cause)
  {
    super(cause);
  }

}
