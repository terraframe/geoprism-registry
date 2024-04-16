package net.geoprism.registry.service.business;

import java.io.File;
import java.io.InputStream;

import org.springframework.stereotype.Component;

@Component
public interface BackupAndRestoreBusinessServiceIF
{

  /**
   * Creates a backup of the data in the system
   * 
   * @param zipfile
   */
  void createBackup(File zipfile);

  /**
   * Deletes the data in the system
   */
  void deleteData();

  void restoreFromBackup(InputStream stream);

}
