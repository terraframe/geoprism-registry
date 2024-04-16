package net.geoprism.registry.service.request;

import java.io.InputStream;

import org.springframework.stereotype.Component;

@Component
public interface BackupAndRestoreServiceIF
{

  /**
   * Creates a backup of the data in the system
   * 
   * @param zipfile
   */
  public InputStream backup(String sessionId);

  /**
   * Deletes the data in the system
   */
  public void deleteData(String sessionId);

  public void restore(String sessionId, InputStream istream);

}
