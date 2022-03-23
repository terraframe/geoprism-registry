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
package net.geoprism.registry.shapefile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
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
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.session.Session;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import net.geoprism.gis.geoserver.SessionPredicate;
import net.geoprism.registry.io.GeoObjectUtil;
import net.geoprism.registry.io.ImportAttributeSerializer;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.query.graph.VertexSelectGeoObjectQuery;

public class GeoObjectAtTimeShapefileExporter
{
  private static Logger             logger = LoggerFactory.getLogger(GeoObjectAtTimeShapefileExporter.class);

  public static final String        GEOM   = "the_geom";

  private ServerGeoObjectType       type;

  private Date                      date;

  private Collection<AttributeType> attributes;

  private Map<String, String>       columnNames;

  private Collection<Locale>        locales;

  public GeoObjectAtTimeShapefileExporter(ServerGeoObjectType type, Date date)
  {
    this(type, date, LocalizationFacade.getInstalledLocales());
  }

  public GeoObjectAtTimeShapefileExporter(ServerGeoObjectType type, Date date, Collection<Locale> locales)
  {
    this.type = type;
    this.date = date;
    this.attributes = new ImportAttributeSerializer(Session.getCurrentLocale(), false, false, locales).attributes(this.type.getType());
    this.columnNames = new HashMap<String, String>();
    this.locales = locales;
  }

  public Map<String, String> getColumnNames()
  {
    return columnNames;
  }

  public ServerGeoObjectType getType()
  {
    return type;
  }

  public void setType(ServerGeoObjectType type)
  {
    this.type = type;
  }

  public File writeToFile() throws IOException
  {
    SimpleFeatureType featureType = createFeatureType();

    FeatureCollection<SimpleFeatureType, SimpleFeature> collection = features(featureType);

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
    final int BLOCK_SIZE = 2000;

    List<SimpleFeature> features = new ArrayList<SimpleFeature>();
    SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
    VertexServerGeoObject prev = null;

    do
    {
      final VertexSelectGeoObjectQuery query = new VertexSelectGeoObjectQuery(type, this.date, prev);
      query.setLimit(BLOCK_SIZE);

      final List<VertexServerGeoObject> objects = query.getResults();

      prev = null;

      for (VertexServerGeoObject object : objects)
      {
        if (!object.getInvalid())
        {
          builder.set(GEOM, object.getGeometry());

          this.attributes.forEach(attribute -> {
            String name = attribute.getName();
            String columnName = this.getColumnName(name);
            Object value = attribute.isChangeOverTime() ? object.getValue(name, this.date) : object.getValue(name);

            if (attribute instanceof AttributeTermType)
            {
              builder.set(columnName, GeoObjectUtil.convertToTermString((AttributeTermType) attribute, value));
            }
            else if (attribute instanceof AttributeClassificationType)
            {
              builder.set(columnName, GeoObjectUtil.convertToTermString((AttributeClassificationType) attribute, value));
            }
            else if (attribute instanceof AttributeLocalType)
            {
              builder.set(columnName, ( (LocalizedValue) value ).getValue());
            }
            else
            {
              builder.set(columnName, value);
            }
          });

          AttributeType attribute = this.getType().getAttribute(DefaultAttribute.DISPLAY_LABEL.getName()).get();

          LocalizedValue label = object.getDisplayLabel(this.date);

          builder.set(this.getColumnName(attribute.getName() + " " + MdAttributeLocalInfo.DEFAULT_LOCALE), label.getValue(LocalizedValue.DEFAULT_LOCALE));

          for (Locale locale : locales)
          {
            builder.set(this.getColumnName(attribute.getName() + " " + locale.toString()), label.getValue(locale));
          }

          SimpleFeature feature = builder.buildFeature(object.getCode());
          features.add(feature);
        }

        prev = object;

        Thread.yield();
      }
    } while (prev != null);

    return new ListFeatureCollection(featureType, features);

  }

  public SimpleFeatureType createFeatureType()
  {
    SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
    builder.setName(this.type.getLabel().getValue());
    builder.setCRS(DefaultGeographicCRS.WGS84);
    builder.add(GEOM, this.getShapefileType(this.type.getGeometryType()), 4326);

    this.attributes.forEach(attribute -> {
      builder.add(generateColumnName(attribute.getName()), this.getShapefileType(attribute));
    });

    AttributeType attribute = this.getType().getAttribute(DefaultAttribute.DISPLAY_LABEL.getName()).get();

    builder.add(generateColumnName(attribute.getName() + " " + MdAttributeLocalInfo.DEFAULT_LOCALE), String.class);

    for (Locale locale : locales)
    {
      builder.add(generateColumnName(attribute.getName() + " " + locale.toString()), String.class);
    }

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
    else if (attribute instanceof AttributeClassificationType)
    {
      return String.class;
    }
    else if (attribute instanceof AttributeLocalType)
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
    else if (geometryType.equals(GeometryType.MIXED))
    {
      return Geometry.class;
    }

    throw new UnsupportedOperationException("Unsupported geometry type [" + geometryType.name() + "]");
  }
}
