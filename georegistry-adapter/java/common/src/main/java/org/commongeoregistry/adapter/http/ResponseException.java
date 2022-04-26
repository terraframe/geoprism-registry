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
