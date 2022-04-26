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

import org.commongeoregistry.adapter.http.AuthenticationException;
import org.commongeoregistry.adapter.http.ServerResponseException;

public interface AdapterIdServiceIF
{
  /**
   * Populates the id cache with ids. After invoking this method, the id cache will be guaranteed to contain @size ids.
   * If the id cache already contains ids, the actual amount fetched from the server will be the difference between @size
   * and the current amount in the cache. As such, @size should be set to the max size of the id cache.
   * @throws IOException 
   * @throws ServerResponseException 
   * @throws AuthenticationException 
   */
  public void populate(int size) throws AuthenticationException, ServerResponseException, IOException;
  
  /**
   * Fetches the next id from the id cache. If the id cache is empty, a EmptyIdCacheException is thrown.
   * 
   * @return
   */
  public String next() throws EmptyIdCacheException;
}
