package net.geoprism.registry.service.request;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.service.business.BackupAndRestoreBusinessServiceIF;

@Service
public class BackupAndRestoreService implements BackupAndRestoreServiceIF
{
  @Autowired
  private BackupAndRestoreBusinessServiceIF service;

  @Override
  @Request(RequestType.SESSION)
  public InputStream backup(String sessionId)
  {
    try
    {
      Path path = Files.createTempFile("gpr-dump", ".zip");

      this.service.createBackup(path.toFile());

      return Files.newInputStream(path, StandardOpenOption.DELETE_ON_CLOSE);
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Override
  @Request(RequestType.SESSION)
  public void deleteData(String sessionId)
  {
    this.service.deleteData();
  }

  @Override
  @Request(RequestType.SESSION)
  public void restore(String sessionId, InputStream istream)
  {
    this.service.restoreFromBackup(istream);
  }

}
