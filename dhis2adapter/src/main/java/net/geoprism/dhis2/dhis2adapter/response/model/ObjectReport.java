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

import java.util.List;

public class ObjectReport
{
  private String klass;
  
  private Integer index;
  
  private List<ErrorReport> errorReports;

  public Boolean hasErrorReports()
  {
    return this.errorReports != null && this.errorReports.size() > 0;
  }

  public String getKlass()
  {
    return klass;
  }

  public void setKlass(String klass)
  {
    this.klass = klass;
  }

  public Integer getIndex()
  {
    return index;
  }

  public void setIndex(Integer index)
  {
    this.index = index;
  }

  public List<ErrorReport> getErrorReports()
  {
    return errorReports;
  }

  public void setErrorReports(List<ErrorReport> errorReports)
  {
    this.errorReports = errorReports;
  }
  
}
