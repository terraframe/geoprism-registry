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
package net.geoprism.registry.etl.upload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.session.Request;

import net.geoprism.data.importer.FeatureRow;
import net.geoprism.registry.etl.CloseableDelegateFile;
import net.geoprism.registry.etl.ImportFileFormatException;
import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.excel.MapFeatureRow;

/**
 * JSON importer for a top-level JSON array of objects.
 * Reads using Jackson streaming (JsonParser) to keep memory low.
 * Each array element is converted to Map<String,Object> and exposed via MapFeatureRow.
 *
 * @author rrowlands
 */
public class JSONImporter implements FormatSpecificImporterIF
{
  protected static final Logger logger = LoggerFactory.getLogger(JSONImporter.class);

  protected final ApplicationResource      resource;
  protected final ImportProgressListenerIF progressListener;
  protected final ImportConfiguration      config;

  protected ObjectImporterIF objectImporter;
  protected Long             startIndex = 0L;

  public JSONImporter(ApplicationResource resource, ImportConfiguration config, ImportProgressListenerIF progressListener)
  {
    this.resource = resource;
    this.progressListener = progressListener;
    this.config = config;
  }

  public ObjectImporterIF getObjectImporter()
  {
    return objectImporter;
  }

  @Override
  public void setObjectImporter(ObjectImporterIF objectImporter)
  {
    this.objectImporter = objectImporter;
  }

  public Long getStartIndex()
  {
    return this.startIndex;
  }

  @Override
  public void run(ImportStage stage)
  {
    try
    {
      try (CloseableFile json = JSONImporter.getJsonFromResource(this.resource))
      {
        this.process(stage, json);
      }
    }
    catch (Exception e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  /**
   * Locate a JSON file from the resource. Supports:
   *  - a standalone .json (no copy needed)
   *  - a .zip containing one or more .json (first match is used)
   */
  public static CloseableFile getJsonFromResource(ApplicationResource res)
  {
    try
    {
      if ("zip".equalsIgnoreCase(res.getNameExtension()))
      {
        try (InputStream is = res.openNewStream())
        {
          File dir = Files.createTempDirectory(res.getBaseName()).toFile();
          extractZip(is, dir);

          File[] jsons = dir.listFiles(new FilenameFilter()
          {
            @Override
            public boolean accept(File dir, String name)
            {
              return name.toLowerCase().endsWith(".json");
            }
          });

          if (jsons != null && jsons.length > 0)
          {
            return new CloseableDelegateFile(jsons[0], dir);
          }
          throw new ImportFileFormatException();
        }
      }
      else if ("json".equalsIgnoreCase(res.getNameExtension()))
      {
        return res.openNewFile();
      }
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }

    throw new ImportFileFormatException();
  }

  private static void extractZip(InputStream iStream, File directory)
  {
    byte[] buffer = new byte[2048];
    try (ZipInputStream zstream = new ZipInputStream(iStream))
    {
      ZipEntry entry;
      while ((entry = zstream.getNextEntry()) != null)
      {
        File file = new File(directory, entry.getName());
        // Create parent dirs for nested entries
        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();

        try (FileOutputStream output = new FileOutputStream(file))
        {
          int len;
          while ((len = zstream.read(buffer)) > 0)
          {
            output.write(buffer, 0, len);
          }
        }
      }
    }
    catch (IOException e1)
    {
      throw new ProgrammingErrorException(e1);
    }
  }

  @Request
  private void process(ImportStage stage, File jsonFile) throws InvocationTargetException, IOException, InterruptedException
  {
    final ObjectMapper mapper = new ObjectMapper();
    final JsonFactory factory = mapper.getFactory();

    // Pass 1: count elements to set progress total (without materializing)
    long total = countTopLevelArrayElements(factory, jsonFile);
    long effective = Math.max(total - this.getStartIndex(), 0);
    this.progressListener.setWorkTotal(effective);

    // Pass 2: stream rows, skip startIndex, hand off to objectImporter
    long seen = 0;
    long rowNum = 1;

    try (JsonParser p = factory.createParser(jsonFile))
    {
      if (p.nextToken() != JsonToken.START_ARRAY)
      {
        throw new ImportFileFormatException();
      }

      while (p.nextToken() != JsonToken.END_ARRAY)
      {
        if (p.currentToken() != JsonToken.START_OBJECT)
        {
          // Non-object row; skip (or throw if you prefer strict)
          p.skipChildren();
          rowNum++;
          continue;
        }

        // Deserialize just this object into a Map, consuming the object from the stream
        Map<String, Object> rowMap = mapper.readValue(p, new TypeReference<Map<String, Object>>() {});

        if (seen++ < this.getStartIndex())
        {
          rowNum++;
          continue;
        }

        MapFeatureRow row = new MapFeatureRow(rowMap, rowNum);

        if (stage.equals(ImportStage.VALIDATE))
        {
          this.objectImporter.validateRow(row);
        }
        else
        {
          this.objectImporter.importRow(row);
        }

        rowNum++;
      }
    }
    catch (Throwable t)
    {
      throw new ProgrammingErrorException(t);
    }
  }

  /** Count elements in the top-level array via streaming. */
  private long countTopLevelArrayElements(JsonFactory factory, File jsonFile) throws IOException
  {
    long count = 0;
    try (JsonParser p = factory.createParser(jsonFile))
    {
      if (p.nextToken() != JsonToken.START_ARRAY)
      {
        throw new ImportFileFormatException();
      }
      while (p.nextToken() != JsonToken.END_ARRAY)
      {
        p.skipChildren(); // fast-forward each element
        count++;
      }
    }
    return count;
  }

  @Override
  public Geometry getGeometry(FeatureRow row)
  {
    // No implicit geometry from JSON.
    return null;
  }
}
