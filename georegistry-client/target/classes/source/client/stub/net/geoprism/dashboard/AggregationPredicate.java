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
package net.geoprism.dashboard;

import java.util.ArrayList;
import java.util.List;

import net.geoprism.util.Predicate;


import com.runwaysdk.system.metadata.MdAttributeConcreteDTO;
import com.runwaysdk.system.metadata.MdAttributeDTO;
import com.runwaysdk.system.metadata.MdAttributeMomentDTO;
import com.runwaysdk.system.metadata.MdAttributeVirtualDTO;

public class AggregationPredicate implements Predicate<AggregationTypeDTO>
{
  private List<AllAggregationTypeDTO> filters;

  public AggregationPredicate(MdAttributeDTO _mdAttribute)
  {
    this.filters = new ArrayList<AllAggregationTypeDTO>();

    MdAttributeConcreteDTO mdAttributeConcrete = getMdAttributeConcrete(_mdAttribute);

    if (mdAttributeConcrete instanceof MdAttributeMomentDTO)
    {
      this.filters.add(AllAggregationTypeDTO.AVG);
      this.filters.add(AllAggregationTypeDTO.SUM);
    }
  }

  private MdAttributeConcreteDTO getMdAttributeConcrete(MdAttributeDTO _mdAttribute)
  {
    if (_mdAttribute instanceof MdAttributeVirtualDTO)
    {
      return ( (MdAttributeVirtualDTO) _mdAttribute ).getMdAttributeConcrete();
    }

    return (MdAttributeConcreteDTO) _mdAttribute;
  }

  @Override
  public boolean evaulate(AggregationTypeDTO _dto)
  {
    for (AllAggregationTypeDTO filter : filters)
    {
      if (_dto.getEnumName().equals(filter.getName()))
      {
        return true;
      }
    }

    return false;
  }
}
