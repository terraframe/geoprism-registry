package net.geoprism.registry.service.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class StartupService implements ApplicationListener<ApplicationReadyEvent>
{
  @Autowired
  private GraphRepoServiceIF service;

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event)
  {
    try
    {
      service.initialize();
    }
    catch (Exception e)
    {

    }
  }
}
