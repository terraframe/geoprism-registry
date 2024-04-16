package net.geoprism.registry.service.business;

import java.io.InputStream;

import org.springframework.stereotype.Component;

@Component
public interface RestoreServiceIF
{
  void restoreFromBackup(InputStream istream);
}
