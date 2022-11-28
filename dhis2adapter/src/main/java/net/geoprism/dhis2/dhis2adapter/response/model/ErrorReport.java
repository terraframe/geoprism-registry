/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.dhis2.dhis2adapter.response.model;

public class ErrorReport
{
  protected String message;
  
  protected String mainKlass;
  
  protected String errorCode;
  
  protected String errorKlass;
  
  protected String errorProperty;
  
  protected String[] errorProperties;

  public String getMessage()
  {
    return message;
  }

  public void setMessage(String message)
  {
    this.message = message;
  }

  public String getMainKlass()
  {
    return mainKlass;
  }

  public void setMainKlass(String mainKlass)
  {
    this.mainKlass = mainKlass;
  }

  public String getErrorCode()
  {
    return errorCode;
  }

  public void setErrorCode(String errorCode)
  {
    this.errorCode = errorCode;
  }

  public String getErrorProperty()
  {
    return errorProperty;
  }

  public void setErrorProperty(String errorProperty)
  {
    this.errorProperty = errorProperty;
  }

  public String[] getErrorProperties()
  {
    return errorProperties;
  }

  public void setErrorProperties(String[] errorProperties)
  {
    this.errorProperties = errorProperties;
  }
}