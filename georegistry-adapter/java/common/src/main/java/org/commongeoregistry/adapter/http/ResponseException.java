/**
 *
 */
package org.commongeoregistry.adapter.http;

public class ResponseException extends RuntimeException
{
  /**
   * 
   */
  private static final long serialVersionUID = 5997688438657079206L;

  private int               status;

  public ResponseException(int status)
  {
    super();

    this.status = status;
  }

  public ResponseException(String message, Throwable cause, int status)
  {
    super(message, cause);

    this.status = status;
  }

  public ResponseException(String message, int status)
  {
    super(message);

    this.status = status;
  }

  public ResponseException(Throwable cause, int status)
  {
    super(cause);

    this.status = status;
  }

  public int getStatus()
  {
    return status;
  }
}
