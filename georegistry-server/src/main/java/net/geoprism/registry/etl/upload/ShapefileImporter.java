/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.SimpleFeatureRow;
import net.geoprism.registry.etl.CloseableDelegateFile;
import net.geoprism.registry.etl.ImportFileFormatException;
import net.geoprism.registry.etl.ImportStage;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.sort.SortBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.session.Request;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Class responsible for reading data from a shapefile row by row and making available that data as a common interface for
 * consumption by the object importer.
 * 
 * @author Richard Rowlands
 */
public class ShapefileImporter implements FormatSpecificImporterIF
{
  protected ApplicationResource resource;
  
  protected ObjectImporterIF objectImporter;
  
  protected ImportProgressListenerIF progressListener;
  
  protected Long startIndex = 0L;
  
  protected ImportConfiguration config;
  
  protected static final Logger logger = LoggerFactory.getLogger(ShapefileImporter.class);

  public ShapefileImporter(ApplicationResource resource, ImportConfiguration config, ImportProgressListenerIF progressListener)
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

  @Override
  public void setStartIndex(Long startIndex)
  {
    this.startIndex = startIndex;
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
      try (CloseableFile shp = ShapefileImporter.getShapefileFromResource(this.resource, "shp"))
      {
        this.process(stage, shp);
      }
    }
    catch (Exception e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
  
  public static CloseableFile getShapefileFromResource(ApplicationResource res, String extension)
  {
    try
    {
      if (res.getNameExtension().equals("zip"))
      {
        try (InputStream is = res.openNewStream())
        {
          File dir = Files.createTempDirectory(res.getBaseName()).toFile();
          
          extract(is, dir);
          
          File[] dbfs = dir.listFiles(new FilenameFilter()
          {
            @Override
            public boolean accept(File dir, String name)
            {
              return name.endsWith("." + extension);
            }
          });

          if (dbfs.length > 0)
          {
            return new CloseableDelegateFile(dbfs[0], dir);
          }
          else
          {
            throw new ImportFileFormatException();
          }
        }
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

  /**
   * Imports the entities from the shapefile
   * 
   * @param writer
   *          Log file writer
   * @throws InvocationTargetException
   * @throws IOException 
   */
  @Request
  private void process(ImportStage stage, File shp) throws InvocationTargetException, IOException
  {
    FileDataStore myData = FileDataStoreFinder.getDataStore(shp);
    
    try
    {
      SimpleFeatureSource source = myData.getFeatureSource();
  
      Query query = new Query();
      
      query.setSortBy(new SortBy[] {SortBy.NATURAL_ORDER}); // Enforce predictable ordering based on alphabetical Feature Ids
      
      if (this.getStartIndex() > 0)
      {
        query.setStartIndex(Math.toIntExact(this.getStartIndex()));
      }
      
      this.progressListener.setWorkTotal((long) source.getFeatures(query).size());
      
      SimpleFeatureIterator iterator = source.getFeatures(query).features();
      
      try
      {
        while (iterator.hasNext())
        {
          SimpleFeature feature = iterator.next();
          
          if (stage.equals(ImportStage.VALIDATE))
          {
            this.objectImporter.validateRow(new SimpleFeatureRow(feature));
          }
          else
          {
            this.objectImporter.importRow(new SimpleFeatureRow(feature));
          }
        }
      }
      finally
      {
        iterator.close();
      }
    }
    finally
    {
      myData.dispose();
    }
  }

  public Geometry getGeometry(FeatureRow row)
  {
    Object geometry = ( (SimpleFeatureRow) row ).getFeature().getDefaultGeometry();

    return (Geometry) geometry;
  }

}
