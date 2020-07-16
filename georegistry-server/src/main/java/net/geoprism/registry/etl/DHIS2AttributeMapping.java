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
package net.geoprism.registry.etl;

import net.geoprism.dhis2.dhis2adapter.response.model.ValueType;

public class DHIS2AttributeMapping
{
  
  private ValueType dhis2ValueType;
  
  private String runwayAttributeId;
  
  private String externalId;

  public ValueType getDhis2ValueType()
  {
    return dhis2ValueType;
  }

  public void setDhis2ValueType(ValueType dhis2ValueType)
  {
    this.dhis2ValueType = dhis2ValueType;
  }

  public String getRunwayAttributeId()
  {
    return runwayAttributeId;
  }

  public void setRunwayAttributeId(String runwayAttributeId)
  {
    this.runwayAttributeId = runwayAttributeId;
  }

  public String getExternalId()
  {
    return externalId;
  }

  public void setExternalId(String externalId)
  {
    this.externalId = externalId;
  }
  
}
