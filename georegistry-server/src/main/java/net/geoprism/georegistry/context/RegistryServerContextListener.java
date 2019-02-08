package net.geoprism.georegistry.context;


import net.geoprism.context.ServerContextListener;

public class RegistryServerContextListener implements ServerContextListener
{
  @Override
  public void initialize()
  {
    System.out.println("initialized");
  }

  @Override
  public void startup()
  {
  }

  @Override
  public void shutdown()
  {
  }
}
