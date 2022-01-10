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

import java.awt.Point;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public enum ValueType {
  
  TEXT( String.class, true ),
  LONG_TEXT( String.class, true ),
  LETTER( String.class, true ),
  PHONE_NUMBER( String.class, false ),
  EMAIL( String.class, false ),
  BOOLEAN( Boolean.class, true ),
  TRUE_ONLY( Boolean.class, true ),
  DATE( LocalDate.class, false ),
  DATETIME( LocalDateTime.class, false ),
  TIME( String.class, false ),
  NUMBER( Double.class, true ),
  UNIT_INTERVAL( Double.class, true ),
  PERCENTAGE( Double.class, true ),
  INTEGER( Integer.class, true ),
  INTEGER_POSITIVE( Integer.class, true ),
  INTEGER_NEGATIVE( Integer.class, true ),
  INTEGER_ZERO_OR_POSITIVE( Integer.class, true ),
  TRACKER_ASSOCIATE( TrackedEntityInstance.class, false ),
  USERNAME( String.class, false ),
  COORDINATE( Point.class, true ),
  ORGANISATION_UNIT( OrganisationUnit.class, false ),
  AGE( Date.class, false ),
  URL( String.class, false ),
  FILE_RESOURCE( String.class, false ),
  IMAGE( String.class, false);
  
  private Class<?> javaClass;
  
  private boolean aggregateable;
  
  ValueType( Class<?> javaClass, boolean aggregateable )
  {
      this.javaClass = javaClass;
      this.aggregateable = aggregateable;
  }

  public Class<?> getJavaClass()
  {
    return javaClass;
  }

  public void setJavaClass(Class<?> javaClass)
  {
    this.javaClass = javaClass;
  }

  public boolean isAggregateable()
  {
    return aggregateable;
  }

  public void setAggregateable(boolean aggregateable)
  {
    this.aggregateable = aggregateable;
  }
  
}
