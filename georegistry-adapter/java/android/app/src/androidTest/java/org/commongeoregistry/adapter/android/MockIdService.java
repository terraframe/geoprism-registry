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
package org.commongeoregistry.adapter;

import java.util.UUID;

import org.commongeoregistry.adapter.http.HttpResponse;
import org.commongeoregistry.adapter.id.AdapterIdServiceIF;
import org.commongeoregistry.adapter.id.MemoryOnlyIdService;

import com.google.gson.JsonArray;

public class MockIdService extends MemoryOnlyIdService implements AdapterIdServiceIF
{
  
  public MockIdService() {
    super();
    this.populate(500);
  }
  
  public static String genId()
  {
    return UUID.randomUUID().toString();
  }

  @Override
  public void populate(int size)
  {
    synchronized(lock)
    {
      int amount = size - this.cache.size();
      
      for (int i = 0; i < amount; ++i)
      {
        String id = genId();
        
        this.cache.add(id);
      }
    }
  }

}
