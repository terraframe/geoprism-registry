/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.shapefile;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.GISImportLoggerIF;
import net.geoprism.data.importer.SimpleFeatureRow;
import net.geoprism.registry.excel.FeatureRowImporter;
import net.geoprism.registry.io.GeoObjectConfiguration;
import net.geoprism.registry.io.ImportProblemException;
import net.geoprism.registry.model.ServerGeoObjectIF;

/**
 * Class responsible for importing GeoObject definitions from a shapefile.
 * 
 * @author Justin Smethie
 */
public class GeoObjectShapefileImporter extends FeatureRowImporter
{
  /**
   * URL of the file being imported
   */
  private URL url;

  /**
   * @param url
   *          URL of the shapefile
   */
  public GeoObjectShapefileImporter(URL url, GeoObjectConfiguration config)
  {
    super(config);

    this.url = url;
  }

  public GeoObjectShapefileImporter(File file, GeoObjectConfiguration config) throws MalformedURLException
  {
    this(file.toURI().toURL(), config);
  }

  @Request
  public void run(GISImportLoggerIF logger) throws InvocationTargetException
  {
    try
    {
      try
      {
        this.createEntities(logger);
      }
      finally
      {
        logger.close();
      }
    }
    catch (RuntimeException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throw new InvocationTargetException(e);
    }
  }

  /**
   * Imports the entities from the shapefile
   * 
   * @param writer
   *          Log file writer
   * @throws InvocationTargetException
   */
  @Transaction
  private void createEntities(GISImportLoggerIF logger) throws InvocationTargetException
  {
    try
    {
      ShapefileDataStore store = new ShapefileDataStore(url);

      try
      {
        String[] typeNames = store.getTypeNames();

        if (typeNames.length > 0)
        {
          String typeName = typeNames[0];

          FeatureSource<SimpleFeatureType, SimpleFeature> source = store.getFeatureSource(typeName);

          // Display the geo entity information about each row
          FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures();

          FeatureIterator<SimpleFeature> iterator = collection.features();

          try
          {
            while (iterator.hasNext())
            {
              SimpleFeature feature = iterator.next();

              create(new SimpleFeatureRow(feature));
            }
          }
          finally
          {
            iterator.close();
          }
        }
      }
      finally
      {
        store.dispose();
      }

      if (this.getConfiguration().hasProblems())
      {
        throw new ImportProblemException("Import contains problems");
      }
    }
    catch (RuntimeException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throw new InvocationTargetException(e);
    }
  }

  @Override
  protected Geometry getGeometry(FeatureRow row)
  {
    Object geometry = ( (SimpleFeatureRow) row ).getFeature().getDefaultGeometry();

    return (Geometry) geometry;
  }

  @Override
  protected void setValue(ServerGeoObjectIF entity, org.commongeoregistry.adapter.metadata.AttributeType attributeType, String attributeName, Object value)
  {
    if (attributeName.equals(DefaultAttribute.DISPLAY_LABEL.getName()))
    {
      entity.setDisplayLabel((LocalizedValue) value);
    }
    else if (attributeType instanceof AttributeTermType)
    {
      this.setTermValue(entity, attributeType, attributeName, value);
    }
    else if (attributeType instanceof AttributeFloatType)
    {
      entity.setValue(attributeName, ( (Number) value ).doubleValue());
    }
    else if (attributeType instanceof AttributeIntegerType)
    {
      entity.setValue(attributeName, ( (Number) value ).longValue());
    }
    else if (attributeType instanceof AttributeCharacterType)
    {
      entity.setValue(attributeName, value.toString());
    }
    else
    {
      entity.setValue(attributeName, value);
    }
  }

}
