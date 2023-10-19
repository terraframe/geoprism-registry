/**
 *
 */
package net.geoprism.registry.test;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.metadata.MetadataCache;
import org.springframework.stereotype.Service;

import net.geoprism.registry.service.request.ServiceFactory;

@Service
public class TestRegistryAdapter extends RegistryAdapter
{
  private static final long serialVersionUID = -433764579483802366L;

  public TestRegistryAdapter()
  {
    super(new TestRegistryIdService());
  }
  
  @Override
  public MetadataCache getMetadataCache()
  {
    return ServiceFactory.getAdapter().getMetadataCache();
  }
  
  
}
