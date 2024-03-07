/**
 *
 */
package net.geoprism.registry.test;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.springframework.stereotype.Service;

import net.geoprism.registry.cache.ServerAdapterCache;

@Service
public class TestRegistryAdapter extends RegistryAdapter
{
  private static final long serialVersionUID = -433764579483802366L;

  public TestRegistryAdapter()
  {
    super(new TestRegistryIdService(), new ServerAdapterCache());
  }
}
