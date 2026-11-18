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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.RunwayException;
import com.runwaysdk.business.SmartException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.session.Request;

import net.geoprism.data.importer.FeatureRow;
import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.etl.ImportFileFormatException;
import net.geoprism.registry.excel.MapFeatureRow;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * JSON importer for a top-level JSON array of objects. Reads using Jackson
 * streaming (JsonParser) to keep memory low. Each array element is converted to
 * Map<String,Object> and exposed via MapFeatureRow.
 *
 * @author rrowlands
 */
public class JSONImporter implements FormatSpecificImporterIF
{
  protected static final Logger            logger     = LoggerFactory.getLogger(JSONImporter.class);

  protected final ApplicationResource      resource;

  protected final ImportProgressListenerIF progressListener;

  protected final ImportConfiguration      config;

  protected ObjectImporterIF               objectImporter;

  protected Long                           startIndex = 0L;

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
    catch (RunwayException | SmartException e)
    {
      // Let user facing exceptions through, so the import job reports them
      throw e;
    }
    catch (Exception e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  /**
   * Locates the JSON file in an uploaded resource: either a .json file, or a
   * zip containing one (the first by path is used).
   */
  public static CloseableFile getJsonFromResource(ApplicationResource res)
  {
    return ImportFileResources.getFile(res, "json");
  }

  @Request
  private void process(ImportStage stage, File jsonFile) throws InvocationTargetException, IOException, InterruptedException
  {
    final JsonMapper mapper = JsonMapper.builder()
        .disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
        .build();
    
    try
    {
      // Pass 1: count elements to set progress total (without materializing)
      long total = countTopLevelArrayElements(mapper, jsonFile);
      long effective = Math.max(total - this.getStartIndex(), 0);
      this.progressListener.setWorkTotal(effective);

      this.readRows(stage, mapper, jsonFile);
    }
    catch (JacksonException e)
    {
      // Malformed JSON. Use the parser's message and location, rather than
      // getMessage(), which can include a description of the source file.
      String details = "The file is not valid JSON: " + e.getOriginalMessage();

      if (e.getLocation() != null && e.getLocation().getLineNr() > 0)
      {
        details += " (line " + e.getLocation().getLineNr() + ", column " + e.getLocation().getColumnNr() + ")";
      }

      throw ImportFileFormatException.withDetails(details, e);
    }
    catch (RunwayException | SmartException e)
    {
      throw e;
    }
    catch (Throwable t)
    {
      throw new ProgrammingErrorException(t);
    }
  }

  private void readRows(ImportStage stage, JsonMapper mapper, File jsonFile) throws InterruptedException
  {
    // Pass 2: stream rows, skip startIndex, hand off to objectImporter
    long seen = 0;
    long rowNum = 1;

    try (JsonParser p = mapper.createParser(jsonFile))
    {
      if (p.nextToken() != JsonToken.START_ARRAY)
      {
        throw newNotAnArrayException();
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

        // Deserialize just this object into a Map, consuming the object from
        // the stream
        Map<String, Object> rowMap = mapper.readValue(p, new TypeReference<Map<String, Object>>()
        {
        });

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
  }

  private static ImportFileFormatException newNotAnArrayException()
  {
    return ImportFileFormatException.withDetails("The JSON file must contain an array of objects at the top level.");
  }

  /** Count elements in the top-level array via streaming. */
  private long countTopLevelArrayElements(JsonMapper factory, File jsonFile) throws IOException
  {
    long count = 0;
    try (JsonParser p = factory.createParser(jsonFile))
    {
      if (p.nextToken() != JsonToken.START_ARRAY)
      {
        throw newNotAnArrayException();
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
