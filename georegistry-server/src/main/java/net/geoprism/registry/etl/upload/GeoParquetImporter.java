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

import org.locationtech.jts.geom.Geometry;

import com.runwaysdk.RunwayException;
import com.runwaysdk.business.SmartException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.session.Request;

import net.geoprism.data.importer.FeatureRow;
import net.geoprism.registry.etl.ImportStage;

public class GeoParquetImporter implements FormatSpecificImporterIF
{
  protected final ApplicationResource      resource;

  protected final ImportProgressListenerIF progressListener;

  protected final ImportConfiguration      config;

  protected ObjectImporterIF               objectImporter;

  protected Long                           startIndex = 0L;

  public GeoParquetImporter(ApplicationResource resource, ImportConfiguration config, ImportProgressListenerIF progressListener)
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
    try (CloseableFile file = GeoParquetReader.getParquetFromResource(this.resource))
    {
      this.process(stage, file);
    }
    catch (RunwayException | SmartException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Request
  private void process(ImportStage stage, File file) throws Exception
  {
    try (GeoParquetReader reader = new GeoParquetReader(file))
    {
      if (!this.config.getIgnoreProjection())
      {
        reader.validateProjection();
      }

      this.progressListener.setWorkTotal(reader.count());

      reader.read(this.getStartIndex(), true, null, row -> {
        if (stage.equals(ImportStage.VALIDATE))
        {
          this.objectImporter.validateRow(row);
        }
        else
        {
          this.objectImporter.importRow(row);
        }

        return true;
      });
    }
  }

  @Override
  public Geometry getGeometry(FeatureRow row)
  {
    return ( (GeoParquetReader.Row) row ).getGeometry();
  }
}
