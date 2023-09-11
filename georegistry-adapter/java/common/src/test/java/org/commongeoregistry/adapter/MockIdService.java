/**
 *
 */
package org.commongeoregistry.adapter;

import java.util.UUID;

import org.commongeoregistry.adapter.http.HttpResponse;
import org.commongeoregistry.adapter.id.AdapterIdServiceIF;
import org.commongeoregistry.adapter.id.MemoryOnlyIdService;

import com.google.gson.JsonArray;

public class MockIdService extends MemoryOnlyIdService implements AdapterIdServiceIF
{
  
  public MockIdService()
  {
    super();
    this.populate(500);
  }
  
  public static MockHttpRequest getIdRequest(int amount)
  {
    JsonArray ja = new JsonArray();
    
    for (int i = 0; i < amount; ++i)
    {
      ja.add(MockIdService.genId());
    }
    
    HttpResponse resp = new HttpResponse(ja.toString(), 200);
    
    return new MockHttpRequest(resp);
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
