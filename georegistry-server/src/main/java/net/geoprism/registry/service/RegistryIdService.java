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
package net.geoprism.registry.service;

import java.util.UUID;

import org.commongeoregistry.adapter.id.AdapterIdServiceIF;
import org.commongeoregistry.adapter.id.EmptyIdCacheException;

import com.runwaysdk.query.QueryFactory;

import net.geoprism.registry.IdRecord;
import net.geoprism.registry.IdRecordQuery;

public class RegistryIdService implements AdapterIdServiceIF
{
  public RegistryIdService()
  {
  }

  @Override
  public void populate(int size)
  {
    // Intentionally empty. The RegistryIdService doesn't need to be populated
    // and will always return ids.
  }

  @Override
  public String next() throws EmptyIdCacheException
  {
    String id = UUID.randomUUID().toString();

    IdRecord record = new IdRecord();
    record.setRegistryId(id);
    record.apply();

    return id;
  }

  /**
   * Convenience method for fetching a large number of ids. Equivalent to
   * invoking next @amount times.
   * 
   * @param amount
   * @return
   */
  public String[] getUids(int amount)
  {
    String[] ids = new String[amount];

    for (int i = 0; i < amount; ++i)
    {
      String id = this.next();

      ids[i] = id;
    }

    return ids;
  }

  public boolean isIssuedId(String id)
  {
    IdRecordQuery query = new IdRecordQuery(new QueryFactory());
    query.WHERE(query.getRegistryId().EQ(id));
    return query.getCount() > 0;
  }

  public static synchronized RegistryIdService getInstance()
  {
    return ServiceFactory.getIdService();
  }
}
