/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.dhis2.dhis2adapter.exception;

public class InvalidLoginException extends Exception
{

  private static final long serialVersionUID = 5138509402364454307L;
  
  public InvalidLoginException() {
      super();
  }

  public InvalidLoginException(String message) {
      super(message);
  }

  public InvalidLoginException(String message, Throwable cause) {
      super(message, cause);
  }

  public InvalidLoginException(Throwable cause) {
      super(cause);
  }

  protected InvalidLoginException(String message, Throwable cause,
                      boolean enableSuppression,
                      boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
  }
  
}
