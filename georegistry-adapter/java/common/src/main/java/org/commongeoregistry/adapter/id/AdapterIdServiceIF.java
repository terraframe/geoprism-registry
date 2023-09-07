/**
 *
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
