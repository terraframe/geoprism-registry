package net.geoprism.georegistry.service;

import net.geoprism.georegistry.AdapterUtilities;

import org.commongeoregistry.adapter.RegistryAdapter;

public class ServiceFactory
{
  public static void constructServices(RegistryAdapter adapter)
  {
    ConversionService cs = ConversionService.getInstance();
    AdapterUtilities util = AdapterUtilities.getInstance();
    RegistryIdService idService = RegistryIdService.getInstance();
    
    cs.setAdapter(adapter);
    cs.setUtil(util);
    
    util.setConversionService(cs);
    util.setAdapter(adapter);
    
    idService.setConversionService(cs);
  }
}
