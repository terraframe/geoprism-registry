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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.geotools.api.data.FileDataStore;
import org.geotools.api.data.FileDataStoreFinder;
import org.geotools.api.data.Query;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.DelegateSimpleFeatureReader;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.api.data.SimpleFeatureReader;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.data.sort.SortedFeatureReader;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.filter.sort.SortBy;
import org.geotools.api.filter.sort.SortOrder;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.RunwayException;
import com.runwaysdk.business.SmartException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.session.Request;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.data.importer.SimpleFeatureRow;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.InvalidProjectionException;
import net.geoprism.registry.UnableToReadProjectionException;
import net.geoprism.registry.etl.ImportFileFormatException;
import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.io.GeoObjectImportConfiguration;

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

  /**
   * Configures a shapefile data store the way GPR reads shapefiles, for both
   * the import and the preview shown while matching columns:
   * <ul>
   * <li>DBF dates are read as UTC, the time zone GPR stores dates in.
   * Otherwise they are read in the JVM's time zone and can shift by a
   * day.</li>
   * <li>Text is decoded with the encoding named in the .cpg file, when there
   * is one. GeoTools ignores the .cpg file and assumes ISO-8859-1, which
   * garbles non-ASCII text in UTF-8 shapefiles (the ArcGIS default).</li>
   * </ul>
   *
   * @param file
   *          Any file of the shapefile (.shp, .dbf, ...), used to find the .cpg
   */
  public static void configure(ShapefileDataStore store, File file)
  {
    store.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    Charset charset = getCharset(file);

    if (charset != null)
    {
      store.setCharset(charset);
    }
  }

  /**
   * @return The encoding named in the shapefile's .cpg file, or null if there
   *         is no .cpg file or its encoding is not recognized
   */
  public static Charset getCharset(File file)
  {
    String name = file.getName();
    int index = name.lastIndexOf('.');
    String base = index == -1 ? name : name.substring(0, index);

    for (String extension : new String[] { ".cpg", ".CPG", ".Cpg" })
    {
      File cpg = new File(file.getParentFile(), base + extension);

      if (cpg.isFile())
      {
        try
        {
          return parseCharset(Files.readString(cpg.toPath(), StandardCharsets.US_ASCII));
        }
        catch (IOException e)
        {
          logger.warn("Unable to read the shapefile encoding from [" + cpg + "]", e);

          return null;
        }
      }
    }

    return null;
  }

  /**
   * Parses the contents of a .cpg file, e.g. "UTF-8", "1252" or "ANSI 1252"
   *
   * @return The charset, or null if it is blank or not recognized
   */
  public static Charset parseCharset(String contents)
  {
    String value = contents == null ? "" : contents.trim();

    if (value.toUpperCase().startsWith("ANSI "))
    {
      value = value.substring(5).trim();
    }

    if (value.isEmpty())
    {
      return null;
    }

    // Bare code page numbers, as written by ArcGIS
    if (value.matches("\\d+"))
    {
      if (value.equals("65001"))
      {
        value = "UTF-8";
      }
      else
      {
        value = value.matches("8859\\d+") ? "ISO-8859-" + value.substring(4) : "windows-" + value;
      }
    }

    try
    {
      return Charset.forName(value);
    }
    catch (IllegalArgumentException e)
    {
      logger.warn("Unrecognized shapefile encoding [" + contents + "], falling back to the default");

      return null;
    }
  }

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
   * Locates a file of the shapefile (e.g. the .shp or .dbf) in an uploaded
   * zip. Shapefiles are always uploaded as a zip, since they span several
   * files.
   */
  public static CloseableFile getShapefileFromResource(ApplicationResource res, String extension)
  {
    if (ImportFileResources.hasExtension(res, ImportFileResources.ZIP))
    {
      return ImportFileResources.getFileFromZip(res, extension);
    }

    throw ImportFileFormatException.withDetails("Shapefiles must be uploaded as a ." + ImportFileResources.ZIP + " file, but got [" + res.getName() + "].");
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
    FileDataStore myData;
    SimpleFeatureSource source;
    SimpleFeatureType schema;

    try
    {
      myData = FileDataStoreFinder.getDataStore(shp);

      if (myData == null)
      {
        throw ImportFileFormatException.withDetails("[" + shp.getName() + "] could not be read as a shapefile.");
      }

      if (myData instanceof ShapefileDataStore)
      {
        configure((ShapefileDataStore) myData, shp);
      }

      source = myData.getFeatureSource();
      schema = source.getSchema();
    }
    catch (IOException e)
    {
      // Show the underlying error, without the server's temporary directory
      String details = ImportFileFormatException.describe(e).replace(shp.getParentFile().getAbsolutePath() + File.separator, "");

      throw ImportFileFormatException.withDetails(details, e);
    }

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

    if (this.config instanceof GeoObjectImportConfiguration)
    {
      GeoObjectImportConfiguration gConfig = (GeoObjectImportConfiguration) this.config;

      if (gConfig.getLocations().size() > 0)
      {
        ShapefileFunction loc = gConfig.getLocations().get(0).getFunction();

        if (loc instanceof BasicColumnFunction)
        {
          sortBy.add(ff.sort( ( (BasicColumnFunction) loc ).getAttributeName(), SortOrder.ASCENDING));
        }
      }
    }

    try (SimpleFeatureReader sr = new SortedFeatureReader(fr, sortBy.toArray(new SortBy[sortBy.size()]), 5000))
    {
      long rowNum = 1;

      while (sr.hasNext())
      {
        SimpleFeature feature = sr.next();

        if (stage.equals(ImportStage.VALIDATE))
        {
          this.objectImporter.validateRow(new SimpleFeatureRow(feature, rowNum));
        }
        else
        {
          this.objectImporter.importRow(new SimpleFeatureRow(feature, rowNum));
        }

        rowNum++;
      }
    }
    catch (Throwable t)
    {
      throw new ProgrammingErrorException(t);
    }
    finally
    {
      myData.dispose();
    }
  }

  @Override
  public Geometry getGeometry(FeatureRow row)
  {
    return (Geometry) ( (SimpleFeatureRow) row ).getFeature().getDefaultGeometry();
  }

}
