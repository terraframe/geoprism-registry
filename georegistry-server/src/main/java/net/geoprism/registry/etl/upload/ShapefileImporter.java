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
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.simple.DelegateSimpleFeatureReader;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureReader;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.sort.SortedFeatureReader;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.session.Request;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.data.importer.SimpleFeatureRow;
import net.geoprism.registry.InvalidProjectionException;
import net.geoprism.registry.UnableToReadProjectionException;
import net.geoprism.registry.etl.CloseableDelegateFile;
import net.geoprism.registry.etl.ImportFileFormatException;
import net.geoprism.registry.etl.ImportStage;

/**
 * Class responsible for reading data from a shapefile row by row and making
 * available that data as a common interface for consumption by the object
 * importer.
 * 
 * @author Richard Rowlands
 */
public class ShapefileImporter implements FormatSpecificImporterIF
{
  protected static final Logger      logger     = LoggerFactory.getLogger(ShapefileImporter.class);

  protected ApplicationResource      resource;

  protected ObjectImporterIF         objectImporter;

  protected ImportProgressListenerIF progressListener;

  protected Long                     startIndex = 0L;

  protected ImportConfiguration      config;

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
    this.startIndex = startIndex - 1; // Subtract 1 because we're zero indexed
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
   * @throws InterruptedException
   */
  @Request
  private void process(ImportStage stage, File shp) throws InvocationTargetException, IOException, InterruptedException
  {
    /*
     * Check permissions
     */
    ImportConfiguration config = this.getObjectImporter().getConfiguration();
    config.enforceCreatePermissions();

    FileDataStore myData = FileDataStoreFinder.getDataStore(shp);

    SimpleFeatureSource source = myData.getFeatureSource();
    SimpleFeatureType schema = source.getSchema();

    if (!config.getIgnoreProjection())
    {

      CoordinateReferenceSystem sourceCRS = schema.getCoordinateReferenceSystem();

      if (sourceCRS != null)
      {
        try
        {

          String code = CRS.lookupIdentifier(sourceCRS, true);

          if (!code.equalsIgnoreCase("EPSG:4326"))
          {
            throw new InvalidProjectionException();
          }
        }
        catch (FactoryException e)
        {
          throw new UnableToReadProjectionException();
        }
      }
      else
      {
        throw new UnableToReadProjectionException();
      }
    }
    
    SimpleFeatureCollection featCol = source.getFeatures();
    this.progressListener.setWorkTotal((long) featCol.size());

    if (this.getStartIndex() > 0)
    {

      Query query = new Query();
      query.setStartIndex(Math.toIntExact(this.getStartIndex()));

      featCol = source.getFeatures(query);
    }

    SimpleFeatureIterator featIt = featCol.features();

    SimpleFeatureReader fr = new DelegateSimpleFeatureReader(source.getSchema(), featIt);

    FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

    // We want to sort the features by the parentId column for better lookup
    // performance (more cache hits) and
    // also so that we have predictable ordering if we want to resume the import
    // later.
    List<SortBy> sortBy = new ArrayList<SortBy>();
    sortBy.add(SortBy.NATURAL_ORDER); // We also sort by featureId because it's
                                      // guaranteed to be unique.
    if (this.config.getLocations().size() > 0)
    {
      ShapefileFunction loc = this.config.getLocations().get(0).getFunction();

      if (loc instanceof BasicColumnFunction)
      {
        sortBy.add(ff.sort(loc.toJson().toString(), SortOrder.ASCENDING)); // TODO
                                                                           // :
                                                                           // This
                                                                           // assumes
                                                                           // loc.tojson()
                                                                           // returns
                                                                           // only
                                                                           // the
                                                                           // attribute
                                                                           // name.
      }
    }

    try (SimpleFeatureReader sr = new SortedFeatureReader(fr, sortBy.toArray(new SortBy[sortBy.size()]), 5000))
    {
      while (sr.hasNext())
      {
        SimpleFeature feature = sr.next();

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
    catch (Throwable t)
    {
      t.printStackTrace();
    }
    finally
    {
      myData.dispose();
    }
  }

  public Geometry getGeometry(FeatureRow row)
  {
    Geometry geometry = (Geometry) ( (SimpleFeatureRow) row ).getFeature().getDefaultGeometry();

    if (geometry instanceof Point)
    {
      return new GeometryFactory().createMultiPoint(new Point[] { (Point) geometry });
    }
    else if (geometry instanceof Polygon)
    {
      return new GeometryFactory().createMultiPolygon(new Polygon[] { (Polygon) geometry });
    }
    else if (geometry instanceof LineString)
    {
      return new GeometryFactory().createMultiLineString(new LineString[] { (LineString) geometry });
    }

    return (Geometry) geometry;
  }

}
