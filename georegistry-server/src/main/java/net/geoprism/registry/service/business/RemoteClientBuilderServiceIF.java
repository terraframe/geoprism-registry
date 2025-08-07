package net.geoprism.registry.service.business;

import org.springframework.stereotype.Component;

@Component
public interface RemoteClientBuilderServiceIF
{

  RemoteClientIF open(String source);

}
