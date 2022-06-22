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
package org.commongeoregistry.adapter.android;

import org.commongeoregistry.adapter.http.AuthenticationException;
import org.commongeoregistry.adapter.http.ServerResponseException;
import org.commongeoregistry.adapter.id.AdapterIdServiceIF;
import org.commongeoregistry.adapter.id.EmptyIdCacheException;
import org.commongeoregistry.adapter.id.MemoryOnlyIdService;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public class AndroidSQLiteIdService extends MemoryOnlyIdService implements AdapterIdServiceIF
{
    @Override
    public void populate(int size) throws AuthenticationException, ServerResponseException, IOException
    {
        synchronized(lock)
        {
            AndroidRegistryClient androidClient = (AndroidRegistryClient) this.client;

            int amount = size - androidClient.getLocalCache().countNumberRegistryIds();

            if (amount > 0) {
                    Set<String> fetchedSet = this.client.getGeoObjectUids(amount);

                    androidClient.getLocalCache().addRegistryIds(fetchedSet);
            }
        }
    }

    @Override
    public String next() throws EmptyIdCacheException {
        synchronized(lock) {
            AndroidRegistryClient androidClient = (AndroidRegistryClient) this.client;

            return androidClient.getLocalCache().nextRegistryId();
        }
    }
}
