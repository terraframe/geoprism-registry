package net.geoprism.georegistry.shapefile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.kms.model.UnsupportedOperationException;
import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import net.geoprism.georegistry.io.GeoObjectUtil;
import net.geoprism.gis.geoserver.SessionPredicate;

public class GeoObjectShapefileExporter
{
  private static Logger       logger = LoggerFactory.getLogger(GeoObjectShapefileExporter.class);

  public static final String  GEOM   = "geom";

  private GeoObjectType       type;

  private Iterable<GeoObject> objects;

  public GeoObjectShapefileExporter(GeoObjectType type, Iterable<GeoObject> objects)
  {
    this.type = type;
    this.objects = objects;
  }

  public GeoObjectType getType()
  {
    return type;
  }

  public void setType(GeoObjectType type)
  {
    this.type = type;
  }

  public Iterable<GeoObject> getObjects()
  {
    return objects;
  }

  public void setObjects(Iterable<GeoObject> objects)
  {
    this.objects = objects;
  }

  public File writeToFile() throws IOException
  {
    SimpleFeatureType featureType = createFeatureType();

    DefaultFeatureCollection collection = createFeatures(featureType);

    String name = SessionPredicate.generateId();

    File root = new File(new File(VaultProperties.getPath("vault.default"), "files"), name);
    root.mkdirs();

    File directory = new File(root, this.getType().getCode());
    directory.mkdirs();

    File file = new File(directory, this.getType().getCode() + ".shp");

    /*
     * Get an output file name and create the new shapefile
     */
    ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

    Map<String, Serializable> params = new HashMap<String, Serializable>();
    params.put("url", file.toURI().toURL());
    params.put("create spatial index", Boolean.TRUE);

    ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
    newDataStore.createSchema(featureType);

    /*
     * Write the features to the shapefile
     */
    try (Transaction transaction = new DefaultTransaction("create"))
    {
      String typeName = newDataStore.getTypeNames()[0];
      SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);

      if (featureSource instanceof SimpleFeatureStore)
      {
        SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;

        featureStore.setTransaction(transaction);
        try
        {
          featureStore.addFeatures(collection);
          transaction.commit();

        }
        catch (Exception problem)
        {
          transaction.rollback();

          throw new ProgrammingErrorException(problem);
        }
      }
      else
      {
        throw new ProgrammingErrorException(typeName + " does not support read/write access");
      }
    }

    return directory;
  }

  public InputStream export() throws IOException
  {
    // Zip up the entire contents of the file
    final File directory = this.writeToFile();
    final PipedOutputStream pos = new PipedOutputStream();
    final PipedInputStream pis = new PipedInputStream(pos);

    Thread t = new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          try (ZipOutputStream zipFile = new ZipOutputStream(pos))
          {
            File[] files = directory.listFiles();

            for (File file : files)
            {
              ZipEntry entry = new ZipEntry(file.getName());
              zipFile.putNextEntry(entry);

              try (FileInputStream in = new FileInputStream(file))
              {
                IOUtils.copy(in, zipFile);
              }
            }
          }
          finally
          {
            pos.close();
          }

          FileUtils.deleteQuietly(directory);
        }
        catch (IOException e)
        {
          logger.error("Error while writing the workbook", e);
        }
      }
    });
    t.setDaemon(true);
    t.start();

    return pis;

  }

  public DefaultFeatureCollection createFeatures(SimpleFeatureType featureType)
  {
    Map<String, AttributeType> attributes = this.type.getAttributeMap();
    Set<Entry<String, AttributeType>> entries = attributes.entrySet();

    DefaultFeatureCollection collection = new DefaultFeatureCollection("internal", featureType);
    SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);

    for (GeoObject object : this.objects)
    {
      featureBuilder.set(GEOM, object.getGeometry());

      for (Entry<String, AttributeType> entry : entries)
      {
        AttributeType attribute = entry.getValue();
        String name = attribute.getName();
        Object value = object.getValue(name);

        if (attribute instanceof AttributeTermType)
        {
          featureBuilder.set(GeoObjectShapefileExporter.format(name), GeoObjectUtil.convertToTermString(value));
        }
        else
        {
          featureBuilder.set(GeoObjectShapefileExporter.format(name), value);
        }
      }

      SimpleFeature feature = featureBuilder.buildFeature(object.getCode());
      collection.add(feature);
    }

    return collection;
  }

  public SimpleFeatureType createFeatureType()
  {
    SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
    builder.setName(this.type.getLocalizedLabel());
    builder.setCRS(DefaultGeographicCRS.WGS84);
    builder.add(GEOM, this.getShapefileType(this.type.getGeometryType()));

    Map<String, AttributeType> attributes = this.type.getAttributeMap();
    Set<Entry<String, AttributeType>> entries = attributes.entrySet();

    for (Entry<String, AttributeType> entry : entries)
    {
      AttributeType attribute = entry.getValue();

      builder.length(15).add(GeoObjectShapefileExporter.format(attribute.getName()), this.getShapefileType(attribute));
    }

    return builder.buildFeatureType();
  }

  private Class<?> getShapefileType(AttributeType attribute)
  {
    if (attribute instanceof AttributeBooleanType)
    {
      return Boolean.class;
    }
    else if (attribute instanceof AttributeCharacterType)
    {
      return String.class;
    }
    else if (attribute instanceof AttributeDateType)
    {
      return Date.class;
    }
    else if (attribute instanceof AttributeFloatType)
    {
      return Double.class;
    }
    else if (attribute instanceof AttributeIntegerType)
    {
      return Long.class;
    }
    else if (attribute instanceof AttributeTermType)
    {
      return String.class;
    }

    throw new UnsupportedOperationException("Unsupported attribute type [" + attribute.getClass().getSimpleName() + "]");
  }

  private Class<?> getShapefileType(GeometryType geometryType)
  {
    if (geometryType.equals(GeometryType.POINT))
    {
      return Point.class;
    }
    else if (geometryType.equals(GeometryType.MULTIPOINT))
    {
      return MultiPoint.class;
    }
    else if (geometryType.equals(GeometryType.LINE))
    {
      return LineString.class;
    }
    else if (geometryType.equals(GeometryType.MULTILINE))
    {
      return MultiLineString.class;
    }
    else if (geometryType.equals(GeometryType.POLYGON))
    {
      return Polygon.class;
    }
    else if (geometryType.equals(GeometryType.MULTIPOLYGON))
    {
      return MultiPolygon.class;
    }

    throw new UnsupportedOperationException("Unsupported geometry type [" + geometryType.name() + "]");
  }

  public static String format(String name)
  {
    if (name.equals(GeoObject.LOCALIZED_DISPLAY_LABEL))
    {
      return "LABEL";
    }

    return name.substring(0, Math.min(10, name.length()));
  }
}
