package net.geoprism.registry.test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.commongeoregistry.adapter.HttpRegistryClient;
import org.commongeoregistry.adapter.id.AdapterIdServiceIF;
import org.commongeoregistry.adapter.id.EmptyIdCacheException;

import net.geoprism.registry.RegistryController;

public class TestRegistryClientIdService implements AdapterIdServiceIF
{
  protected Set<String> cache;

  protected Object lock;
  
  protected TestRegistryAdapterClient client;
  
  public TestRegistryClientIdService()
  {
    this.cache = new HashSet<String>(100);
    this.lock = new Object();
  }
  
  public void setClient(TestRegistryAdapterClient client)
  {
    this.client = client;
  }
  
  @Override
  public void populate(int size)
  {
    synchronized(lock)
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
    synchronized(lock)
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
