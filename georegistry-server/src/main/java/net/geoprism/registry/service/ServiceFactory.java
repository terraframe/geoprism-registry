package net.geoprism.registry.service;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.RegistryAdapterServer;

import net.geoprism.registry.AdapterUtilities;

public class ServiceFactory
{
  private static ServiceFactory instance;
  
  private ConversionService cs;
  
  private AdapterUtilities util;
  
  private RegistryIdService idService;
  
  private RegistryService registryService;
  
  private RegistryAdapter adapter;
  
  private void initialize()
  {
    this.registryService = new RegistryService();
    this.cs = new ConversionService();
    this.util = new AdapterUtilities();
    this.idService = new RegistryIdService();
    
    this.adapter = new RegistryAdapterServer(this.idService);
    
    this.registryService.initialize(this.adapter);
  }
  
  public static synchronized ServiceFactory getInstance()
  {
    if (instance == null)
    {
      instance = new ServiceFactory();
      instance.initialize();
    }
    
    return instance;
  }

  public static RegistryAdapter getAdapter()
  {
    return ServiceFactory.getInstance().adapter;
  }

  public static RegistryService getRegistryService()
  {
    return ServiceFactory.getInstance().registryService;
  }
  
  public static ConversionService getConversionService()
  {
    return ServiceFactory.getInstance().cs;
  }
  
  public static AdapterUtilities getUtilities()
  {
    return ServiceFactory.getInstance().util;
  }
  
  public static RegistryIdService getIdService()
  {
    return ServiceFactory.getInstance().idService;
  }
}
