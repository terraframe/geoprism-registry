/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.etl.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.jaitools.jts.CoordinateSequence2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.session.Request;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import net.geoprism.data.etl.excel.CountSheetHandler;
import net.geoprism.data.etl.excel.ExcelDataFormatter;
import net.geoprism.data.etl.excel.ExcelSheetReader;
import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.registry.etl.CloseableDelegateFile;
import net.geoprism.registry.etl.ImportFileFormatException;
import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.graph.RevealExternalSystem;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.io.LatLonException;
import net.geoprism.registry.model.ServerGeoObjectType;

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
    catch (Exception e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public static CloseableFile getExcelFileFromResource(ApplicationResource res)
  {
    final String extension = "xlsx";

    try
    {
      if (res.getNameExtension().equals("zip"))
      {
        try (InputStream is = res.openNewStream())
        {
          File dir = Files.createTempDirectory(res.getBaseName()).toFile();

          extract(is, dir);

          File[] files = dir.listFiles(new FilenameFilter()
          {
            @Override
            public boolean accept(File dir, String name)
            {
              return name.endsWith("." + extension);
            }
          });

          if (files != null && files.length > 0)
          {
            return new CloseableDelegateFile(files[0], dir);
          }
          else
          {
            throw new ImportFileFormatException();
          }
        }
      }
      else if (res.getNameExtension().equals(extension))
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

  private static void extract(InputStream iStream, File directory)
  {
    // create a buffer to improve copy performance later.
    byte[] buffer = new byte[2048];

    try
    {
      ZipInputStream zstream = new ZipInputStream(iStream);

      ZipEntry entry;

      while ( ( entry = zstream.getNextEntry() ) != null)
      {
        File file = new File(directory, entry.getName());

        try (FileOutputStream output = new FileOutputStream(file))
        {
          int len = 0;

          while ( ( len = zstream.read(buffer) ) > 0)
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
  private void process(ImportStage stage, File file) throws InvocationTargetException, IOException
  {
    try
    {
      /*
       * Check permissions
       */
      ImportConfiguration config = this.getObjectImporter().getConfiguration();
      config.enforceCreatePermissions();

      // TODO Determine permissions for

      this.progressListener.setWorkTotal(this.getWorkTotal(file));

      if (this.config.isExternalImport() && this.config.getExternalSystem() instanceof RevealExternalSystem && this.config instanceof GeoObjectImportConfiguration)
      {
        this.excelHandler = new RevealExcelContentHandler(this.objectImporter, stage, this.getStartIndex(), ( (GeoObjectImportConfiguration) this.config ).getRevealGeometryColumn());
      }
      else
      {
        this.excelHandler = new ExcelContentHandler(this.objectImporter, stage, this.getStartIndex());
      }

      ExcelDataFormatter formatter = new ExcelDataFormatter();

      ExcelSheetReader reader = new ExcelSheetReader(excelHandler, formatter);

      try (FileInputStream istream = new FileInputStream(file))
      {
        reader.process(istream);
      }
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
    if (this.config.isExternalImport() && this.config.getExternalSystem() instanceof RevealExternalSystem)
    {
      return ( (RevealExcelContentHandler) this.excelHandler ).getGeometry();
    }
    else
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
          
          return new Point(new CoordinateSequence2D(lon, lat), factory);
        }
      }

      return null;
    }
  }
}
