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
package net.geoprism.registry.masterlist;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;

public class ListTypeAttributeComparator implements Comparator<MdAttributeConcreteDAOIF>, Serializable
{
  private static final long serialVersionUID = 1L;

  private List<String>      order;

  public ListTypeAttributeComparator(List<String> order, List<? extends MdAttributeConcreteDAOIF> attributes)
  {
    this.order = order;

    for (MdAttributeConcreteDAOIF attribute : attributes)
    {
      if (!this.order.contains(attribute.definesAttribute()))
      {
        this.order.add(attribute.definesAttribute());
      }
    }
  }

  @Override
  public int compare(MdAttributeConcreteDAOIF o1, MdAttributeConcreteDAOIF o2)
  {
    Integer i1 = this.order.indexOf(o1.definesAttribute());
    Integer i2 = this.order.indexOf(o2.definesAttribute());

    return i1.compareTo(i2);
  }

}
