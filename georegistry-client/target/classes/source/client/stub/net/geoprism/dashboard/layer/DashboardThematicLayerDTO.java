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
package net.geoprism.dashboard.layer;

import java.util.List;

import net.geoprism.dashboard.AggregationTypeDTO;
import net.geoprism.dashboard.AllAggregationTypeDTO;

public class DashboardThematicLayerDTO extends DashboardThematicLayerDTOBase
 {
  private static final long serialVersionUID = -1877883022;
  
  public DashboardThematicLayerDTO(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected DashboardThematicLayerDTO(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  public AggregationTypeDTO getActiveAggregation(List<AggregationTypeDTO> _aggregations)
  {
    List<AllAggregationTypeDTO> activeAggregations = this.getAggregationType();

    if (activeAggregations.size() > 0)
    {
      AllAggregationTypeDTO activeAggregation = activeAggregations.get(0);
      AggregationTypeDTO item = activeAggregation.item(this.getRequest());

      return item;
    }
    else if (_aggregations.size() > 0)
    {
      return _aggregations.get(0);
    }

    return null;
  }

  public String getActiveAggregationLabel(List<AggregationTypeDTO> _aggregations)
  {
    AggregationTypeDTO item = this.getActiveAggregation(_aggregations);

    if (item != null)
    {
      return item.getDisplayLabel().getValue();
    }

    return "";
  }
  
}
