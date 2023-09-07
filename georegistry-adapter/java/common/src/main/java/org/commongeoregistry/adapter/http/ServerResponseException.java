/**
 *
 */
package org.commongeoregistry.adapter.http;

/**
 * Represents a generic exception thrown from the server during a server request
 * 
 * @author terraframe
 */
public class ServerResponseException extends Exception
{

  /**
   * 
   */
  private static final long serialVersionUID = 3771528795519887032L;

  /**
   * HTTP response status
   */
  private int               status;

  /**
   * Server exception type
   */
  private String            type;

  /**
   * Localized exception message
   */
  private String            localizedMessage;

  public ServerResponseException()
  {
    super();
  }

  public ServerResponseException(String message, int status, String type, String localizedMessage)
  {
    super(message);

    this.status = status;
    this.type = type;
    this.localizedMessage = localizedMessage;
  }

  public int getStatus()
  {
    return status;
  }

  public void setStatus(int status)
  {
    this.status = status;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getLocalizedMessage()
  {
    return localizedMessage;
  }

  public void setLocalizedMessage(String localizedMessage)
  {
    this.localizedMessage = localizedMessage;
  }

}
