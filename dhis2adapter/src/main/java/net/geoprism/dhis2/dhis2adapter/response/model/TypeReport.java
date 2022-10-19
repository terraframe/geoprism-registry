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

import java.util.ArrayList;
import java.util.List;

public class TypeReport
{
  
  private String klass;
  
  private List<ObjectReport> objectReports;
  
  private Stats stats;

  public String getKlass()
  {
    return klass;
  }

  public void setKlass(String klass)
  {
    this.klass = klass;
  }

  public List<ObjectReport> getObjectReports()
  {
    if (this.objectReports != null)
    {
      return objectReports;
    }
    else
    {
      return new ArrayList<ObjectReport>();
    }
  }

  public void setObjectReports(List<ObjectReport> objectReports)
  {
    this.objectReports = objectReports;
  }

  public Stats getStats()
  {
    return stats;
  }

  public void setStats(Stats stats)
  {
    this.stats = stats;
  }
  
}
