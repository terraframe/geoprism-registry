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
package net.geoprism.registry.shapefile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.GISImportLoggerIF;
import net.geoprism.data.importer.SimpleFeatureRow;
import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.excel.FeatureRowImporter;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.model.ServerGeoObjectIF;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.sort.SortBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.session.Request;
import com.vividsolutions.jts.geom.Geometry;

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
  protected URL url;
  
  protected static final Logger logger = LoggerFactory.getLogger(GeoObjectShapefileImporter.class);

  /**
   * @param url
   *          URL of the shapefile
   */
  public GeoObjectShapefileImporter(URL url, GeoObjectImportConfiguration config)
  {
    super(config);

    this.url = url;
  }

  public GeoObjectShapefileImporter(File file, GeoObjectImportConfiguration config) throws MalformedURLException
  {
    this(file.toURI().toURL(), config);
  }
  
  @Request
  public void run(ImportStage stage, GISImportLoggerIF logger) throws InvocationTargetException
  {
    try
    {
      try
      {
        this.process(stage, logger);
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
   * @throws IOException 
   */
  private void process(ImportStage stage, GISImportLoggerIF logger) throws InvocationTargetException, IOException
  {
    FileDataStore myData = FileDataStoreFinder.getDataStore(this.url);
    
    try
    {
      SimpleFeatureSource source = myData.getFeatureSource();
  
      Query query = new Query();
      
      if (this.history.getWorkProgress() > 0)
      {
        query.setStartIndex(this.history.getWorkProgress());
      }
      
      query.setSortBy(new SortBy[] {SortBy.NATURAL_ORDER}); // Enforce predictable ordering based on alphabetical Feature Ids
      
      this.history.appLock();
      this.history.setWorkTotal(source.getFeatures(query).size());
      this.history.apply();
      GeoObjectShapefileImporter.logger.info("Shapefile import total work [" + this.history.getWorkTotal() + "]");
      
      SimpleFeatureIterator iterator = source.getFeatures(query).features();
      
      try
      {
        int i = 1;
        
        while (iterator.hasNext())
        {
          SimpleFeature feature = iterator.next();
          
          GeoObjectShapefileImporter.logger.info("Feature num " + i + "[" + feature.getIdentifier() + "]");

          if (stage.equals(ImportStage.SYNONYM_CHECK))
          {
            this.validateRow(new SimpleFeatureRow(feature));
          }
          else
          {
            this.create(new SimpleFeatureRow(feature));
          }
          
          i++;
        }
      }
      finally
      {
        iterator.close();
      }
      
      this.history.appLock();
      this.history.setConfigJson(this.configuration.toJson().toString());
      this.history.apply();
    }
    finally
    {
      myData.dispose();
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
      entity.setDisplayLabel((LocalizedValue) value, this.configuration.getStartDate(), this.configuration.getEndDate());
    }
    else if (attributeType instanceof AttributeTermType)
    {
      this.setTermValue(entity, attributeType, attributeName, value, this.configuration.getStartDate(), this.configuration.getEndDate());
    }
    else if (attributeType instanceof AttributeFloatType)
    {
      entity.setValue(attributeName, ( (Number) value ).doubleValue(), this.configuration.getStartDate(), this.configuration.getEndDate());
    }
    else if (attributeType instanceof AttributeIntegerType)
    {
      entity.setValue(attributeName, ( (Number) value ).longValue(), this.configuration.getStartDate(), this.configuration.getEndDate());
    }
    else if (attributeType instanceof AttributeCharacterType)
    {
      entity.setValue(attributeName, value.toString(), this.configuration.getStartDate(), this.configuration.getEndDate());
    }
    else
    {
      entity.setValue(attributeName, value, this.configuration.getStartDate(), this.configuration.getEndDate());
    }
  }

}
