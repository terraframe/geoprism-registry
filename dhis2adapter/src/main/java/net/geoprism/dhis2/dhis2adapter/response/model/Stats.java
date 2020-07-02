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
package net.geoprism.dhis2.dhis2adapter.response.model;

public class Stats
{
  private long created;
  
  private long updated;
  
  private long deleted;
  
  private long ignored;
  
  private long total;

  public long getCreated()
  {
    return created;
  }

  public void setCreated(long created)
  {
    this.created = created;
  }

  public long getUpdated()
  {
    return updated;
  }

  public void setUpdated(long updated)
  {
    this.updated = updated;
  }

  public long getDeleted()
  {
    return deleted;
  }

  public void setDeleted(long deleted)
  {
    this.deleted = deleted;
  }

  public long getIgnored()
  {
    return ignored;
  }

  public void setIgnored(long ignored)
  {
    this.ignored = ignored;
  }

  public long getTotal()
  {
    return total;
  }

  public void setTotal(long total)
  {
    this.total = total;
  }
  
}
