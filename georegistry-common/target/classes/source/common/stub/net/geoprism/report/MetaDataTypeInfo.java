/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.report;

public interface MetaDataTypeInfo
{
  public static final int STRING_PARAMETER    = 1;

  public static final int INTEGER_PARAMETER   = 4;

  public static final int DOUBLE_PARAMETER    = 8;

  public static final int DECIMAL_PARAMETER   = 3;

  public static final int DATE_PARAMETER      = 91;

  public static final int TIME_PARAMETER      = 92;

  public static final int TIMESTAMP_PARAMETER = 93;

  public static final int BOOLEAN_PARAMETER   = 16;
}
