package net.geoprism.registry.service.business;

import java.io.File;

import org.springframework.stereotype.Component;

@Component
public interface BackupAndRestoreServiceIF
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

  void restoreFromBackup(File zipfile);

}
