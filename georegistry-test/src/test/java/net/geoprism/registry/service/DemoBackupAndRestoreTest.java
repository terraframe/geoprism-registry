/**
 *
 */
package net.geoprism.registry.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.session.Request;

import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.service.business.BackupAndRestoreBusinessServiceIF;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
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
