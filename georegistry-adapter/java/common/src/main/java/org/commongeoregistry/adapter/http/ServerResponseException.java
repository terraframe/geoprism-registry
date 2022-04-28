/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Common Geo Registry Adapter(tm).
 *
 * Common Geo Registry Adapter(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Common Geo Registry Adapter(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Common Geo Registry Adapter(tm).  If not, see <http://www.gnu.org/licenses/>.
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
