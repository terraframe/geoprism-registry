/**
 *
 */
package net.geoprism.registry.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.session.Request;

import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.service.business.BackupAndRestoreBusinessServiceIF;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
@Disabled
public class DemoBackupAndRestoreTest
{
  @Autowired
  private BackupAndRestoreBusinessServiceIF backupService;

  @Request
  @Test
  public void testImportAndExport() throws Exception
  {
    File file = new File("demo-backup.zip");

    // this.backupService.createBackup(file);
    //
    Assertions.assertTrue(file.exists());

    this.backupService.deleteData();
    //
    this.backupService.restoreFromBackup(new FileInputStream(file));
  }

}
