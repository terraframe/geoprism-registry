/**
 *
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

import net.geoprism.registry.service.request.RegistryIdService;

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
