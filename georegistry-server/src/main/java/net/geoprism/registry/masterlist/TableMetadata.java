/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.masterlist;

import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

import com.runwaysdk.system.metadata.MdAttribute;
import com.runwaysdk.system.metadata.MdBusiness;

public class TableMetadata
{
  private MdBusiness                    mdBusiness;

  private Map<MdAttribute, MdAttribute> pairs;

  public TableMetadata()
  {
    this.pairs = new HashedMap<MdAttribute, MdAttribute>();
  }

  public MdBusiness getMdBusiness()
  {
    return mdBusiness;
  }

  public void setMdBusiness(MdBusiness mdBusiness)
  {
    this.mdBusiness = mdBusiness;
  }

  public Map<MdAttribute, MdAttribute> getPairs()
  {
    return pairs;
  }

  public void addPair(MdAttribute target, MdAttribute source)
  {
    this.pairs.put(target, source);
  }
}
