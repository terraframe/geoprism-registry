package net.geoprism.georegistry.context;

import com.runwaysdk.session.Request;

import net.geoprism.context.ServerContextListener;
import net.geoprism.georegistry.service.WMSService;

public class RegistryServerContextListener implements ServerContextListener
{
  @Override
  public void initialize()
  {
  }

  @Override
  @Request
  public void startup()
  {
    new WMSService().createAllWMSLayers(false);
  }

  @Override
  public void shutdown()
  {
  }
}
