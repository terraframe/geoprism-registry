package net.geoprism.registry.service.request;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.session.Request;

import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.service.business.LabeledPropertyGraphRDFExportBusinessService;

@Service
public class LabeledPropertyGraphRDFExportService
{
  private static final Logger logger = LoggerFactory.getLogger(LabeledPropertyGraphRDFExportService.class);
  
  @Autowired
  private LabeledPropertyGraphRDFExportBusinessService  rdfExportService;
  
  @Request
  public InputStream export(String sessionId, String versionId)
  {
    LabeledPropertyGraphTypeVersion version = LabeledPropertyGraphTypeVersion.get(versionId);
    
    try
    {
      File file = File.createTempFile(version.getOid(), ".ttl");
      
      try (FileOutputStream fos = new FileOutputStream(file))
      {
        rdfExportService.export(version, fos);
        fos.flush();
        
        // Zip up the entire contents of the file
        final PipedOutputStream pos = new PipedOutputStream();
        final PipedInputStream pis = new PipedInputStream(pos);
    
        Thread t = new Thread(new Runnable()
        {
          @Override
          public void run()
          {
            try
            {
              try (ZipOutputStream zipFile = new ZipOutputStream(pos))
              {
                ZipEntry entry = new ZipEntry(file.getName());
                zipFile.putNextEntry(entry);
  
                try (FileInputStream in = new FileInputStream(file))
                {
                  IOUtils.copy(in, zipFile);
                }
              }
              finally
              {
                pos.close();
              }
    
              FileUtils.deleteQuietly(file);
            }
            catch (IOException e)
            {
              logger.error("Error while writing the workbook", e);
            }
          }
        });
        t.setDaemon(true);
        t.start();
    
        return pis;
      }
    }
    catch(IOException ex)
    {
      throw new RuntimeException(ex);
    }
  }
}
