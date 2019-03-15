package net.geoprism.registry.shapefile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.kms.model.UnsupportedOperationException;
import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.dataaccess.MdAttributeBooleanDAOIF;
import com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDateDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDoubleDAOIF;
import com.runwaysdk.dataaccess.MdAttributeLongDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTextDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.gis.dataaccess.MdAttributeGeometryDAOIF;
import com.runwaysdk.gis.dataaccess.MdAttributeLineStringDAOIF;
import com.runwaysdk.gis.dataaccess.MdAttributeMultiLineStringDAOIF;
import com.runwaysdk.gis.dataaccess.MdAttributeMultiPointDAOIF;
import com.runwaysdk.gis.dataaccess.MdAttributeMultiPolygonDAOIF;
import com.runwaysdk.gis.dataaccess.MdAttributePointDAOIF;
import com.runwaysdk.gis.dataaccess.MdAttributePolygonDAOIF;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import net.geoprism.gis.geoserver.SessionPredicate;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.RegistryConstants;

public class MasterListShapefileExporter
{
  private static Logger                            logger = LoggerFactory.getLogger(GeoObjectShapefileExporter.class);

  public static final String                       GEOM   = "the_geom";

  private Map<String, String>                      columnNames;

  private MasterList                               list;

  private MdBusinessDAOIF                          mdBusiness;

  private List<? extends MdAttributeConcreteDAOIF> mdAttributes;

  public MasterListShapefileExporter(MasterList list, MdBusinessDAOIF mdBusiness, List<? extends MdAttributeConcreteDAOIF> mdAttributes)
  {
    this.list = list;
    this.mdBusiness = mdBusiness;
    this.mdAttributes = mdAttributes;
    this.columnNames = new HashMap<String, String>();
  }

  public Map<String, String> getColumnNames()
  {
    return columnNames;
  }

  public MasterList getList()
  {
    return list;
  }

  public void setList(MasterList list)
  {
    this.list = list;
  }

  public File writeToFile() throws IOException
  {
    SimpleFeatureType featureType = createFeatureType();

    FeatureCollection<SimpleFeatureType, SimpleFeature> collection = features(featureType);

    String name = SessionPredicate.generateId();

    File root = new File(new File(VaultProperties.getPath("vault.default"), "files"), name);
    root.mkdirs();

    File directory = new File(root, this.getList().getCode());
    directory.mkdirs();

    File file = new File(directory, this.getList().getCode() + ".shp");

    /*
     * Get an output file name and create the new shapefile
     */
    ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

    Map<String, Serializable> params = new HashMap<String, Serializable>();
    params.put("url", file.toURI().toURL());
    params.put("create spatial index", Boolean.TRUE);

    ShapefileDataStore dataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
    dataStore.createSchema(featureType);

    /*
     * Write the features to the shapefile
     */
    try (Transaction transaction = new DefaultTransaction())
    {
      String typeName = dataStore.getTypeNames()[0];
      SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);

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

    dataStore.dispose();

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

  public FeatureCollection<SimpleFeatureType, SimpleFeature> features(SimpleFeatureType featureType)
  {
    List<SimpleFeature> features = new ArrayList<SimpleFeature>();
    SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);

    BusinessQuery query = new QueryFactory().businessQuery(mdBusiness.definesType());
    query.ORDER_BY_DESC(query.aCharacter(DefaultAttribute.CODE.getName()));

    OIterator<Business> objects = query.getIterator();

    try
    {

      while (objects.hasNext())
      {
        Business row = objects.next();

        builder.set(GEOM, row.getObjectValue(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));

        for (MdAttributeConcreteDAOIF mdAttribute : mdAttributes)
        {
          String attributeName = mdAttribute.definesAttribute();
          Object value = row.getObjectValue(attributeName);

          if (value != null)
          {
            builder.set(this.getColumnName(attributeName), value);
          }
        }

        SimpleFeature feature = builder.buildFeature(row.getValue(DefaultAttribute.CODE.getName()));
        features.add(feature);
      }
    }
    finally
    {
      objects.close();
    }

    return new ListFeatureCollection(featureType, features);
  }

  public SimpleFeatureType createFeatureType()
  {
    MdAttributeGeometryDAOIF geometryAttribute = (MdAttributeGeometryDAOIF) this.mdBusiness.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);

    SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
    builder.setName(this.list.getDisplayLabel().getValue());
    builder.setCRS(DefaultGeographicCRS.WGS84);
    builder.add(GEOM, this.getShapefileType(geometryAttribute), 4326);

    this.mdAttributes.forEach(attribute -> {
      builder.add(generateColumnName(attribute.definesAttribute()), this.getShapefileType(attribute));
    });

    return builder.buildFeatureType();
  }

  public String getColumnName(String name)
  {
    if (this.columnNames.containsKey(name))
    {
      return this.columnNames.get(name);
    }

    throw new ProgrammingErrorException("Unable to find column name with key [" + name + "]");
  }

  public String generateColumnName(String name)
  {
    if (!this.columnNames.containsKey(name))
    {
      String format = this.format(name);

      int count = 1;

      String value = new String(format);

      while (this.columnNames.containsValue(value))
      {
        if (count == 1)
        {
          format = format.substring(0, format.length() - 1);
        }

        if (count == 10)
        {
          format = format.substring(0, format.length() - 1);
        }

        value = format + ( count++ );
      }

      this.columnNames.put(name, value);
    }

    return this.columnNames.get(name);
  }

  private String format(String name)
  {
    if (name.equals(GeoObject.DISPLAY_LABEL))
    {
      return "label";
    }

    return name.substring(0, Math.min(10, name.length()));
  }

  private Class<?> getShapefileType(MdAttributeConcreteDAOIF mdAttribute)
  {
    if (mdAttribute instanceof MdAttributeBooleanDAOIF)
    {
      return Boolean.class;
    }
    else if (mdAttribute instanceof MdAttributeCharacterDAOIF)
    {
      return String.class;
    }
    else if (mdAttribute instanceof MdAttributeDateDAOIF)
    {
      return Date.class;
    }
    else if (mdAttribute instanceof MdAttributeDoubleDAOIF)
    {
      return Double.class;
    }
    else if (mdAttribute instanceof MdAttributeLongDAOIF)
    {
      return Long.class;
    }
    else if (mdAttribute instanceof MdAttributeTextDAOIF)
    {
      return String.class;
    }

    throw new UnsupportedOperationException("Unsupported attribute type [" + mdAttribute.getClass().getSimpleName() + "]");
  }

  private Class<?> getShapefileType(MdAttributeGeometryDAOIF mdAttribute)
  {
    if (mdAttribute instanceof MdAttributePointDAOIF)
    {
      return Point.class;
    }
    else if (mdAttribute instanceof MdAttributeMultiPointDAOIF)
    {
      return MultiPoint.class;
    }
    else if (mdAttribute instanceof MdAttributeLineStringDAOIF)
    {
      return LineString.class;
    }
    else if (mdAttribute instanceof MdAttributeMultiLineStringDAOIF)
    {
      return MultiLineString.class;
    }
    else if (mdAttribute instanceof MdAttributePolygonDAOIF)
    {
      return Polygon.class;
    }
    else if (mdAttribute instanceof MdAttributeMultiPolygonDAOIF)
    {
      return MultiPolygon.class;
    }

    throw new UnsupportedOperationException("Unsupported attribute type [" + mdAttribute.getClass().getSimpleName() + "]");
  }

}
