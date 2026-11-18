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
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.api.data.DataStore;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.geopkg.GeoPkgDataStoreFactory;
import org.geotools.jdbc.JDBCFeatureSource;
import org.geotools.jdbc.JDBCFeatureStore;
import org.geotools.jdbc.NullPrimaryKey;
import org.geotools.jdbc.PrimaryKey;

import com.runwaysdk.RunwayException;
import com.runwaysdk.business.SmartException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.session.Request;

import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.SimpleFeatureRow;
import net.geoprism.registry.etl.GeoPackageLayerException;
import net.geoprism.registry.etl.ImportFileFormatException;
import net.geoprism.registry.etl.ImportStage;

/**
 * Imports the features of a GeoPackage (https://www.geopackage.org), read with
 * GeoTools. A GeoPackage can hold several layers; it must contain exactly one
 * layer of features to be imported.
 */
public class GeoPackageImporter extends FeatureSourceImporter
{
  public static final String EXTENSION = "gpkg";

  public GeoPackageImporter(ApplicationResource resource, ImportConfiguration config, ImportProgressListenerIF progressListener)
  {
    super(resource, config, progressListener);
  }

  @Override
  public void run(ImportStage stage)
  {
    try (CloseableFile file = getGeoPackageFromResource(this.resource))
    {
      this.process(stage, file);
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

  @Request
  private void process(ImportStage stage, File file) throws Exception
  {
    DataStore store = open(file);

    try
    {
      String layer = getLayer(store);

      this.importFeatures(stage, store.getFeatureSource(layer));
    }
    finally
    {
      store.dispose();
    }
  }

  /**
   * Ask SQLite to return features ordered by their feature id, which is the
   * table's primary key, so this costs nothing. A layer without a primary key
   * (e.g. one defined by a SQL view) can't be ordered by feature id, and is
   * read as stored. (GeoTools reports that it can sort these, but fails.)
   */
  @Override
  protected boolean sortInDataStore(SimpleFeatureSource source)
  {
    PrimaryKey key = null;

    if (source instanceof JDBCFeatureSource)
    {
      key = ( (JDBCFeatureSource) source ).getPrimaryKey();
    }
    else if (source instanceof JDBCFeatureStore)
    {
      key = ( (JDBCFeatureStore) source ).getPrimaryKey();
    }

    return key != null && ! ( key instanceof NullPrimaryKey ) && !key.getColumns().isEmpty();
  }

  @Override
  protected FeatureRow createRow(SimpleFeature feature, long rowNumber)
  {
    return new SimpleFeatureRow(feature, rowNumber)
    {
      @Override
      public Object getValue(String attributeName)
      {
        return convert(super.getValue(attributeName));
      }
    };
  }

  /**
   * Converts a value read by GeoTools to the value GPR imports.
   * <p>
   * GeoTools reads a GeoPackage DATE as a {@link java.sql.Date} at midnight in
   * the JVM's time zone, while GPR stores dates at midnight UTC. Unless the
   * server runs in UTC the date would shift, by a day when the server is ahead
   * of UTC. DATETIME values are read as {@link java.sql.Timestamp}s, which are
   * exact instants and are not changed.
   */
  public static Object convert(Object value)
  {
    if (value instanceof java.sql.Date)
    {
      return Date.from( ( (java.sql.Date) value ).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant());
    }

    return value;
  }

  /**
   * Opens a GeoPackage read only, so the file is never modified. It is also
   * opened as immutable, so SQLite doesn't lock it or create journal files
   * next to it: the file may be the upload, read in place in the vault.
   *
   * @throws ImportFileFormatException
   *           if the file is not a GeoPackage
   */
  public static DataStore open(File file)
  {
    Map<String, Object> params = new HashMap<>();
    params.put(GeoPkgDataStoreFactory.DBTYPE.key, "geopkg");
    params.put(GeoPkgDataStoreFactory.DATABASE.key, file);
    params.put(GeoPkgDataStoreFactory.READ_ONLY.key, true);
    params.put(GeoPkgDataStoreFactory.IMMUTABLE.key, true);

    try
    {
      DataStore store = new GeoPkgDataStoreFactory().createDataStore(params);

      if (store == null)
      {
        throw ImportFileFormatException.withDetails("[" + ImportFileResources.getDisplayName(file) + "] could not be opened as a GeoPackage.");
      }

      try
      {
        // Reading the layers fails if the file isn't a GeoPackage
        store.getTypeNames();
      }
      catch (IOException | RuntimeException e)
      {
        store.dispose();

        throw e;
      }

      return store;
    }
    catch (IOException | RuntimeException e)
    {
      if (e instanceof RunwayException || e instanceof SmartException)
      {
        throw (RuntimeException) e;
      }

      // e.g. "file is not a database". Show the underlying error, without the
      // server's temporary file path.
      String name = ImportFileResources.getDisplayName(file);
      String details = ImportFileFormatException.describe(e).replace(file.getAbsolutePath(), name);

      throw ImportFileFormatException.withDetails("[" + name + "] is not a valid GeoPackage. " + details, e);
    }
  }

  /**
   * @return The name of the GeoPackage's only layer of features (a layer with
   *         a geometry column). Tables without geometries are ignored.
   * @throws GeoPackageLayerException
   *           if the GeoPackage doesn't contain exactly one layer of features
   */
  public static String getLayer(DataStore store) throws IOException
  {
    List<String> layers = new ArrayList<>();

    for (String name : store.getTypeNames())
    {
      SimpleFeatureType schema = store.getSchema(name);

      if (schema.getGeometryDescriptor() != null)
      {
        layers.add(name);
      }
    }

    if (layers.size() != 1)
    {
      GeoPackageLayerException ex = new GeoPackageLayerException("Expected one layer of features, but found " + layers);
      ex.setLayers(String.join(", ", layers));

      throw ex;
    }

    return layers.get(0);
  }

  /**
   * Locates the GeoPackage in an uploaded resource: either a .gpkg file, or a
   * .zip containing one (anywhere in the zip).
   */
  public static CloseableFile getGeoPackageFromResource(ApplicationResource res)
  {
    return ImportFileResources.getFile(res, EXTENSION);
  }
}
