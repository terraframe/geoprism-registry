/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.commongeoregistry.adapter.id.AdapterIdServiceIF;
import org.commongeoregistry.adapter.id.EmptyIdCacheException;

import com.runwaysdk.session.Request;

import net.geoprism.registry.service.RegistryIdService;

public class TestRegistryIdService implements AdapterIdServiceIF
{
  protected Set<String> cache;

  protected Object      lock;

  public TestRegistryIdService()
  {
    this.cache = new HashSet<String>(100);
    this.lock = new Object();
  }

  @Override
  public void populate(int size)
  {
    synchronized (lock)
    {
      int amount = size - this.cache.size();

      if (amount > 0)
      {
        Set<String> fetchedSet = this.getUIDs(amount);

        this.cache.addAll(fetchedSet);
      }
    }
  }

  @Request
  private Set<String> getUIDs(int amount)
  {
    String[] uids = RegistryIdService.getInstance().getUids(amount);
    
    return new TreeSet<String>(Arrays.asList(uids));
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
