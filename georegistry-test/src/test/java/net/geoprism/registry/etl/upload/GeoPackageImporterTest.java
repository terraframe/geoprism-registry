/**
 *
 */
package net.geoprism.registry.etl.upload;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import org.geotools.api.data.DataStore;
import org.geotools.api.data.Query;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.feature.FeatureIterator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests reading GeoPackages the way the import does, without a database.
 * Found with the generated stress files: both used to fail the whole import.
 */
public class GeoPackageImporterTest
{
  @TempDir
  File directory;

  private File copy(String resource, String name) throws Exception
  {
    File file = new File(this.directory, name);

    try (InputStream istream = this.getClass().getResourceAsStream(resource))
    {
      Files.copy(istream, file.toPath());
    }

    return file;
  }

  /**
   * GeoTools reads a JSON column (as GDAL writes them) as an array, and throws
   * when it holds a JSON object. Columns which can't be imported, such as JSON
   * and binary columns, are not read.
   */
  @Test
  public void testUnsupportedColumnsAreNotRead() throws Exception
  {
    File file = this.copy("/geopackage/json-column.gpkg.test", "json.gpkg");

    DataStore store = GeoPackageImporter.open(file);

    try
    {
      SimpleFeatureSource source = store.getFeatureSource(GeoPackageImporter.getLayer(store));

      Assertions.assertEquals(List.of("geom", "code"), FeatureSourceImporter.getImportableColumns(source.getSchema(), true));
      Assertions.assertEquals(List.of("code"), FeatureSourceImporter.getImportableColumns(source.getSchema(), false));

      // Read as the import reads
      Query query = new Query(source.getSchema().getTypeName());
      query.setPropertyNames(FeatureSourceImporter.getImportableColumns(source.getSchema(), true));

      int rows = 0;

      try (FeatureIterator<SimpleFeature> iterator = source.getFeatures(query).features())
      {
        while (iterator.hasNext())
        {
          SimpleFeature feature = iterator.next();

          Assertions.assertNotNull(feature.getAttribute("code"));
          Assertions.assertNotNull(feature.getDefaultGeometry());

          rows++;
        }
      }

      Assertions.assertEquals(3, rows);
    }
    finally
    {
      store.dispose();
    }
  }

  /**
   * A layer defined by a SQL view has no primary key, so it can't be read in
   * feature id order (GeoTools fails with "Cannot do natural order without a
   * primary key"). It is read as stored instead. Tables are still ordered.
   */
  @Test
  public void testViewLayerIsNotSorted() throws Exception
  {
    File file = this.copy("/geopackage/states.gpkg.test", "view.gpkg");

    try (Connection connection = new org.sqlite.JDBC().connect("jdbc:sqlite:" + file.getAbsolutePath(), new Properties()); Statement statement = connection.createStatement())
    {
      statement.execute("CREATE VIEW south AS SELECT fid, geom, GEOID, NAME FROM states WHERE STATEFP > '40'");
      statement.execute("INSERT INTO gpkg_contents (table_name, data_type, identifier, srs_id) VALUES ('south', 'features', 'south', 4326)");
      statement.execute("INSERT INTO gpkg_geometry_columns (table_name, column_name, geometry_type_name, srs_id, z, m) VALUES ('south', 'geom', 'MULTIPOLYGON', 4326, 0, 0)");
    }

    GeoPackageImporter importer = new GeoPackageImporter(null, null, null);

    DataStore store = GeoPackageImporter.open(file);

    try
    {
      Assertions.assertTrue(importer.sortInDataStore(store.getFeatureSource("states")));
      Assertions.assertFalse(importer.sortInDataStore(store.getFeatureSource("south")));

      // The view can be read in full
      int rows = 0;

      try (FeatureIterator<SimpleFeature> iterator = store.getFeatureSource("south").getFeatures().features())
      {
        while (iterator.hasNext())
        {
          iterator.next();
          rows++;
        }
      }

      Assertions.assertTrue(rows > 0);
    }
    finally
    {
      store.dispose();
    }
  }
}
