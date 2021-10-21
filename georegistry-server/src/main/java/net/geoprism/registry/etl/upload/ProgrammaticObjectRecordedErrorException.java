/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.etl.upload;

import com.runwaysdk.RunwayException;

import net.geoprism.registry.etl.upload.ProgrammaticObjectImporter.GeoObjectErrorBuilder;

public class ProgrammaticObjectRecordedErrorException extends RunwayException
{

  private static final long     serialVersionUID = 711088516551711518L;

  private Throwable             error;

  private String                objectJson;

  private String                objectType;

  private GeoObjectErrorBuilder builder;

  public ProgrammaticObjectRecordedErrorException()
  {
    super("");
  }

  public void setError(Throwable t)
  {
    this.error = t;
  }

  public void setObjectJson(String objectJson)
  {
    this.objectJson = objectJson;
  }

  public void setObjectType(String objectType)
  {
    this.objectType = objectType;
  }

  public Throwable getError()
  {
    return error;
  }

  public String getObjectJson()
  {
    return objectJson;
  }

  public String getObjectType()
  {
    return objectType;
  }

  public GeoObjectErrorBuilder getBuilder()
  {
    return builder;
  }

  public void setBuilder(GeoObjectErrorBuilder builder)
  {
    this.builder = builder;
  }
}
