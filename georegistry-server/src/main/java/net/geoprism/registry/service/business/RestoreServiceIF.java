package net.geoprism.registry.service.business;

import java.io.File;

import org.springframework.stereotype.Component;

@Component
public interface RestoreServiceIF
{
  void restoreFromBackup(File zipfile);
}
