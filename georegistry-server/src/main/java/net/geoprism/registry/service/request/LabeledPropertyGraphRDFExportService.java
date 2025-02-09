/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
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
import net.geoprism.registry.service.business.LabeledPropertyGraphRDFExportBusinessService.GeometryExportType;
import net.geoprism.registry.service.business.LabeledPropertyGraphRDFExportBusinessServiceIF;

@Service
public class LabeledPropertyGraphRDFExportService
{
  private static final Logger logger = LoggerFactory.getLogger(LabeledPropertyGraphRDFExportService.class);
  
  @Autowired
  private LabeledPropertyGraphRDFExportBusinessServiceIF  rdfExportService;
  
  @Request
  public InputStream export(String sessionId, String versionId, GeometryExportType geomExportType)
  {
    LabeledPropertyGraphTypeVersion version = LabeledPropertyGraphTypeVersion.get(versionId);
    
    try
    {
      File file = File.createTempFile(version.getOid(), ".trig");
      
      try (FileOutputStream fos = new FileOutputStream(file))
      {
        rdfExportService.export(version, geomExportType, fos);
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
