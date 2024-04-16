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
package net.geoprism.registry.dhis2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.resource.CloseableFile;

import net.geoprism.configuration.GeoprismProperties;
import net.geoprism.registry.GeoRegistryUtil;

/**
 * This class is responsible for extracting a DHIS2 plugin zip (created from the
 * source in georegistry-dhis2-plugin), injecting the external url to the
 * current CGR server, and then rezipping the file and providing it as a
 * resource which can be downloaded and reused for other purposes.
 * 
 * @author rrowlands
 */
public class DHIS2PluginZipManager
{
  private static final String          REPLACE_FILENAME = "index.html";

  private static final String          REPLACE_TOKEN    = "REPLACE_WITH_URL";

  private static final String          PLUGIN_ZIP_NAME  = "CGR-DHIS2-Plugin.zip";

  private static final Logger          logger           = LoggerFactory.getLogger(DHIS2PluginZipManager.class);

  private static DHIS2PluginZipManager instance;

  private File                         pluginZip        = null;

  public static void main(String[] args)
  {
    DHIS2PluginZipManager.getPluginZip();
  }

  synchronized public static DHIS2PluginZipManager getInstance()
  {
    if (instance == null)
    {
      instance = new DHIS2PluginZipManager();
    }

    return instance;
  }

  public static File getPluginZip()
  {
    return DHIS2PluginZipManager.getInstance().iGetPluginZip();
  }

  public File iGetPluginZip()
  {
    if (pluginZip != null && pluginZip.exists())
    {
      return pluginZip;
    }
    else
    {
      pluginZip = new File(GeoprismProperties.getGeoprismFileStorage() + "/" + PLUGIN_ZIP_NAME);

      if (pluginZip.exists())
      {
        FileUtils.deleteQuietly(pluginZip);
      }

      try (CloseableFile directory = extractAndReplace())
      {
        GeoRegistryUtil.zipDirectory(directory, pluginZip);
      }

      return pluginZip;
    }
  }

  private CloseableFile extractAndReplace()
  {
    // create a buffer to improve copy performance later.
    byte[] buffer = new byte[2048];

    CloseableFile directory = GeoprismProperties.newTempDirectory();
    try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("cgr-dhis2-app.zip"))
    {

      try (ZipInputStream zstream = new ZipInputStream(is))
      {
        ZipEntry entry;

        while ( ( entry = zstream.getNextEntry() ) != null)
        {
          File file = new File(directory, entry.getName());

          logger.info("Writing to [" + file.getAbsolutePath() + "].");

          if (!entry.isDirectory())
          {
            FileOutputStream output = null;

            try
            {
              output = new FileOutputStream(file);

              int len = 0;

              while ( ( len = zstream.read(buffer) ) > 0)
              {
                output.write(buffer, 0, len);
              }
            }
            finally
            {
              if (output != null)
              {
                output.close();
              }
            }

            if (file.getName().equals(REPLACE_FILENAME))
            {
              String indexHtml = FileUtils.readFileToString(file, "UTF-8");

              indexHtml = indexHtml.replace(REPLACE_TOKEN, GeoprismProperties.getRemoteServerUrl());

              FileUtils.writeStringToFile(file, indexHtml, Charset.forName("UTF-8"));
            }
          }
          else
          {
            if (!file.mkdir())
            {
              logger.debug("Unable to create directory: " + file.getAbsolutePath());
            }
          }
        }
      }
    }
    catch (IOException e1)
    {
      throw new RuntimeException(e1);
    }

    return directory;
  }

}
