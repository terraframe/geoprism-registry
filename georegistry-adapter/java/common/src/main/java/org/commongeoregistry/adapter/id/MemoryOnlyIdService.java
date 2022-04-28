/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Common Geo Registry Adapter(tm).
 *
 * Common Geo Registry Adapter(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Common Geo Registry Adapter(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Common Geo Registry Adapter(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package org.commongeoregistry.adapter.id;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.commongeoregistry.adapter.HttpRegistryClient;
import org.commongeoregistry.adapter.http.AuthenticationException;
import org.commongeoregistry.adapter.http.ServerResponseException;

public class MemoryOnlyIdService implements AdapterIdServiceIF
{
  protected Set<String>        cache;

  protected HttpRegistryClient client;

  protected Object             lock;

  public MemoryOnlyIdService()
  {
    this.cache = new HashSet<String>(100);
    this.lock = new Object();
  }

  public void setClient(HttpRegistryClient client)
  {
    this.client = client;
  }

  @Override
  public void populate(int size) throws AuthenticationException, ServerResponseException, IOException
  {
    synchronized (lock)
    {
      int amount = size - this.cache.size();

      if (amount > 0)
      {
        Set<String> fetchedSet = this.client.getGeoObjectUids(amount);

        this.cache.addAll(fetchedSet);
      }
    }
  }

  @Override
  public String next() throws EmptyIdCacheException
  {
    synchronized (lock)
    {
      if (this.cache.size() > 0)
      {
        Iterator<String> it = this.cache.iterator();

        String id = it.next();
        it.remove();

        return id;
      }
      else
      {
        throw new EmptyIdCacheException();
      }
    }
  }

}
