package net.geoprism.registry.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.session.Request;

import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.service.business.BackupAndRestoreBusinessServiceIF;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
@Ignore
public class DemoBackupAndRestoreTest
{
  @Autowired
  private BackupAndRestoreBusinessServiceIF backupService;

  @Request
  @Test
  public void testImportAndExport() throws IOException
  {
    File file = new File("demo-backup.zip");

    // this.backupService.createBackup(file);
    //
    Assert.assertTrue(file.exists());

    this.backupService.deleteData();
    //
    this.backupService.restoreFromBackup(new FileInputStream(file));
  }

}
