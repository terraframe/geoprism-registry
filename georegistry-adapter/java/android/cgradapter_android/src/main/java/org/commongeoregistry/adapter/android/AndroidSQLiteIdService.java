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
