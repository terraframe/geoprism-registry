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
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.RunwayException;
import com.runwaysdk.business.SmartException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.session.Request;

import net.geoprism.data.etl.excel.CountSheetHandler;
import net.geoprism.data.etl.excel.ExcelDataFormatter;
import net.geoprism.data.etl.excel.ExcelSheetReader;
import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.registry.etl.ImportFileFormatException;
import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.io.LatLonException;

/**
 * Class responsible for reading data from a spreadsheet row by row and making
 * available that data as a common interface for consumption by the object
 * importer.
 * 
 * @author Richard Rowlands
 */
public class ExcelImporter implements FormatSpecificImporterIF
{
  protected ApplicationResource      resource;

  protected File                     excelImportFile = null;

  protected ObjectImporterIF         objectImporter;

  protected ImportConfiguration      config;

  protected ImportProgressListenerIF progressListener;

  protected Long                     startIndex      = 0L;

  protected GeometryFactory          factory;

  protected ExcelContentHandler      excelHandler;

  protected static final Logger      logger          = LoggerFactory.getLogger(ExcelImporter.class);

  public ExcelImporter(ApplicationResource resource, ImportConfiguration config, ImportProgressListenerIF progressListener)
  {
    this.resource = resource;
    this.config = config;
    this.progressListener = progressListener;
    this.factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
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
      try (CloseableFile xlsx = ExcelImporter.getExcelFileFromResource(this.resource))
      {
        this.process(stage, xlsx);
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
   * Locates the spreadsheet in an uploaded resource: either an .xlsx file, or
   * a zip containing one (the first by path is used).
   */
  public static CloseableFile getExcelFileFromResource(ApplicationResource res)
  {
    return ImportFileResources.getFile(res, "xlsx");
  }

  @Request
  private void process(ImportStage stage, File file) throws InvocationTargetException, IOException
  {
    try
    {
      long total;

      try
      {
        total = this.getWorkTotal(file);
      }
      catch (Exception e)
      {
        // Counting only parses the spreadsheet, so a failure here means the
        // file itself can't be read
        throw ImportFileFormatException.fromCause(e);
      }

      this.progressListener.setWorkTotal(total);

      this.excelHandler = new ExcelContentHandler(this.objectImporter, stage, this.getStartIndex());

      ExcelDataFormatter formatter = new ExcelDataFormatter();

      ExcelSheetReader reader = new ExcelSheetReader(excelHandler, formatter);

      try (FileInputStream istream = new FileInputStream(file))
      {
        reader.process(istream);
      }
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

  private Long getWorkTotal(File file) throws Exception
  {
    try (FileInputStream istream = new FileInputStream(file))
    {
      CountSheetHandler handler = new CountSheetHandler();
      ExcelDataFormatter formatter = new ExcelDataFormatter();

      ExcelSheetReader reader = new ExcelSheetReader(handler, formatter);
      reader.process(istream);

      return (long) ( handler.getRowNum() - 1 ); // Header row doesn't count as
                                                 // work
    }
  }

  @Override
  public Geometry getGeometry(FeatureRow row)
  {
    // TODO : Do we need to support this still
    // if (this.config.isExternalImport() && this.config.getExternalSystem()
    // instanceof RevealExternalSystem)
    // {
    // return ( (RevealExcelContentHandler) this.excelHandler ).getGeometry();
    // }
    // else
    {
      ShapefileFunction latitudeFunction = this.config.getFunction(GeoObjectImportConfiguration.LATITUDE);
      ShapefileFunction longitudeFunction = this.config.getFunction(GeoObjectImportConfiguration.LONGITUDE);

      if (latitudeFunction != null && longitudeFunction != null)
      {
        Object latitude = latitudeFunction.getValue(row);
        Object longitude = longitudeFunction.getValue(row);

        if (latitude != null && longitude != null)
        {
          Double lat = Double.valueOf(latitude.toString());
          Double lon = Double.valueOf(longitude.toString());

          if (Math.abs(lat) > 90 || Math.abs(lon) > 180)
          {
            LatLonException ex = new LatLonException();
            ex.setLat(lat.toString());
            ex.setLon(lon.toString());
            throw ex;
          }

          return factory.createPoint(new Coordinate(lon, lat));
        }
      }

      return null;
    }
  }
}
