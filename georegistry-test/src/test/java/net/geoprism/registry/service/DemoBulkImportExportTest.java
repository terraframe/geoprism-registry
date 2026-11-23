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
import net.geoprism.registry.service.business.BulkImportExportBusinessServiceIF;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
@Disabled
public class DemoBulkImportExportTest
{
  @Autowired
  private BulkImportExportBusinessServiceIF service;

  @Request
  @Test
  public void testImportAndExport() throws Exception
  {
    File file = new File("demo-export.zip");

    // this.service.createExport(file);
    //
    Assertions.assertTrue(file.exists());

    this.service.deleteData();
    //
    this.service.importData(new FileInputStream(file));
  }

}
