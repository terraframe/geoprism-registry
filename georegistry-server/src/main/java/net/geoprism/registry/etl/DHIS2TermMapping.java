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
package net.geoprism.registry.etl;

public class DHIS2TermMapping
{
  
  private String runwayClassifierId;
  
  private String dhis2Code;

  public String getDhis2Code()
  {
    return dhis2Code;
  }

  public void setDhis2Code(String externalId)
  {
    this.dhis2Code = externalId;
  }

  public String getRunwayClassifierId()
  {
    return runwayClassifierId;
  }

  public void setRunwayClassifierId(String runwayClassifierId)
  {
    this.runwayClassifierId = runwayClassifierId;
  }
  
  @Override
  public int hashCode() {
    return new String(runwayClassifierId + dhis2Code).hashCode();
  }
  
}
