package net.geoprism.georegistry.shapefile;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeatureType;

import com.amazonaws.services.kms.model.UnsupportedOperationException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeoObjectShapefileExporter
{

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

  public InputStream export()
  {
    SimpleFeatureType featureType = createFeatureType();

    DefaultFeatureCollection collection = createFeatures(featureType);

//    /*
//     * Get an output file name and create the new shapefile
//     */
//    File newFile = getNewShapeFile(file);
//
//    ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
//
//    Map<String, Serializable> params = new HashMap<String, Serializable>();
//    params.put("url", newFile.toURI().toURL());
//    params.put("create spatial index", Boolean.TRUE);
//
//    ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
//    newDataStore.createSchema(featureType);
//
//    /*
//     * You can comment out this line if you are using the createFeatureType
//     * method (at end of class file) rather than DataUtilities.createType
//     */
//    newDataStore.forceSchemaCRS(DefaultGeographicCRS.WGS84);
//
//    /*
//     * Write the features to the shapefile
//     */
//    Transaction transaction = new DefaultTransaction("create");
//
//    String typeName = newDataStore.getTypeNames()[0];
//    SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
//
//    if (featureSource instanceof SimpleFeatureStore)
//    {
//      SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
//
//      featureStore.setTransaction(transaction);
//      try
//      {
//        featureStore.addFeatures(collection);
//        transaction.commit();
//
//      }
//      catch (Exception problem)
//      {
//        problem.printStackTrace();
//        transaction.rollback();
//
//      }
//      finally
//      {
//        transaction.close();
//      }
//      System.exit(0); // success!
//    }
//    else
//    {
//      System.out.println(typeName + " does not support read/write access");
//      System.exit(1);
//    }

    return null;
  }

  public DefaultFeatureCollection createFeatures(SimpleFeatureType featureType)
  {
    Map<String, AttributeType> attributes = this.type.getAttributeMap();
    Set<Entry<String, AttributeType>> entries = attributes.entrySet();

    DefaultFeatureCollection collection = new DefaultFeatureCollection("internal", featureType);

    SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);

    for (GeoObject object : this.objects)
    {
      featureBuilder.add(object.getGeometry());

      for (Entry<String, AttributeType> entry : entries)
      {
        featureBuilder.add(object.getValue(entry.getValue().getName()));
      }

      collection.add(featureBuilder.buildFeature(null));
    }

    return collection;
  }

  public SimpleFeatureType createFeatureType()
  {
    try
    {
      StringBuilder builder = new StringBuilder();
      builder.append("geom:" + this.getShapefileType(this.type.getGeometryType()) + ":srid=4326");

      Map<String, AttributeType> attributes = this.type.getAttributeMap();
      Set<Entry<String, AttributeType>> entries = attributes.entrySet();

      for (Entry<String, AttributeType> entry : entries)
      {
        AttributeType attribute = entry.getValue();

        builder.append("," + attribute.getName() + ":" + this.getShapefileType(attribute));
      }

      return DataUtilities.createType(this.type.getLocalizedLabel(), builder.toString());
    }
    catch (SchemaException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  private String getShapefileType(AttributeType attribute)
  {
    if (attribute instanceof AttributeBooleanType)
    {
      return Boolean.class.getName();
    }
    else if (attribute instanceof AttributeCharacterType)
    {
      return String.class.getName();
    }
    else if (attribute instanceof AttributeDateType)
    {
      return Date.class.getName();
    }
    else if (attribute instanceof AttributeFloatType)
    {
      return Double.class.getName();
    }
    else if (attribute instanceof AttributeIntegerType)
    {
      return Long.class.getName();
    }
    else if (attribute instanceof AttributeTermType)
    {
      return String.class.getName();
    }

    throw new UnsupportedOperationException("Unsupported attribute type [" + attribute.getClass().getSimpleName() + "]");
  }

  private String getShapefileType(GeometryType geometryType)
  {
    if (geometryType.equals(GeometryType.POINT))
    {
      return Point.class.getSimpleName();
    }
    else if (geometryType.equals(GeometryType.MULTIPOINT))
    {
      return MultiPoint.class.getSimpleName();
    }
    else if (geometryType.equals(GeometryType.LINE))
    {
      return LineString.class.getSimpleName();
    }
    else if (geometryType.equals(GeometryType.MULTILINE))
    {
      return MultiLineString.class.getSimpleName();
    }
    else if (geometryType.equals(GeometryType.POLYGON))
    {
      return Polygon.class.getSimpleName();
    }
    else if (geometryType.equals(GeometryType.MULTIPOLYGON))
    {
      return MultiPolygon.class.getSimpleName();
    }

    throw new UnsupportedOperationException("Unsupported geometry type [" + geometryType.name() + "]");
  }

}
