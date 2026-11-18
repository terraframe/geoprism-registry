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


import java.util.ArrayList;
import java.util.List;

import org.geotools.api.data.Query;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.feature.type.GeometryDescriptor;
import org.geotools.api.filter.sort.SortBy;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.ApplicationResource;

import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.SimpleFeatureRow;
import net.geoprism.registry.InvalidProjectionException;
import net.geoprism.registry.UnableToReadProjectionException;
import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.io.GeoObjectImportConfiguration;

/**
 * Imports the features of a GeoTools {@link SimpleFeatureSource}, for the
 * formats GeoTools reads: shapefiles and GeoPackages. Subclasses open the
 * format's data store and pass the feature source to
 * {@link #importFeatures(ImportStage, SimpleFeatureSource)}.
 */
public abstract class FeatureSourceImporter implements FormatSpecificImporterIF
{
  protected ApplicationResource      resource;

  protected ObjectImporterIF         objectImporter;

  protected ImportProgressListenerIF progressListener;

  protected Long                     startIndex = 0L;

  protected ImportConfiguration      config;

  public FeatureSourceImporter(ApplicationResource resource, ImportConfiguration config, ImportProgressListenerIF progressListener)
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

  /**
   * Ensures the features are in WGS 84 (EPSG:4326)
   *
   * @throws UnableToReadProjectionException
   *           if the projection is missing or can't be identified
   * @throws InvalidProjectionException
   *           if the projection is not EPSG:4326
   */
  public static void validateProjection(SimpleFeatureType schema)
  {
    CoordinateReferenceSystem sourceCRS = schema.getCoordinateReferenceSystem();

    if (sourceCRS != null)
    {
      try
      {
        String code = CRS.lookupIdentifier(sourceCRS, true);

        if (code == null)
        {
          // The CRS doesn't match any known EPSG code
          throw new UnableToReadProjectionException();
        }

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

  /**
   * @return true if a column can be matched to an attribute, i.e. it has a
   *         type GPR can import
   */
  public static boolean isImportable(AttributeDescriptor descriptor)
  {
    try
    {
      GeoObjectImportConfiguration.getBaseType(descriptor.getType());

      return true;
    }
    catch (UnsupportedOperationException e)
    {
      return false;
    }
  }

  /**
   * @param includeGeometry
   *          Whether to include the default geometry column
   * @return The names of the columns which can be imported, in order
   */
  public static List<String> getImportableColumns(SimpleFeatureType schema, boolean includeGeometry)
  {
    List<String> names = new ArrayList<>();

    for (AttributeDescriptor descriptor : schema.getAttributeDescriptors())
    {
      if (descriptor instanceof GeometryDescriptor)
      {
        if (includeGeometry && descriptor.equals(schema.getGeometryDescriptor()))
        {
          names.add(descriptor.getLocalName());
        }
      }
      else if (isImportable(descriptor))
      {
        names.add(descriptor.getLocalName());
      }
    }

    return names;
  }

  /**
   * Validates or imports every feature of the source, in a stable order so
   * that an import can be resumed.
   */
  protected void importFeatures(ImportStage stage, SimpleFeatureSource source) throws Exception
  {
    if (!config.getIgnoreProjection())
    {
      validateProjection(source.getSchema());
    }

    this.progressListener.setWorkTotal((long) source.getFeatures().size());

    // Features are read in their natural order (the order they are stored
    // in), so that an import can be resumed from a row number. The features
    // are streamed: they used to be sorted with GeoTools' SortedFeatureReader,
    // which writes every feature to temporary files once there are more than a
    // few thousand, and made reading two million features ten times slower.
    // (The sort also listed the parent column, but after the unique natural
    // order, so it never changed the order.)
    Query query = new Query(source.getSchema().getTypeName());

    // Only read the columns which can be imported (and the geometry). Others,
    // such as binary columns, are never matched to an attribute, and reading
    // some can fail: GeoTools reads a GeoPackage JSON column as an array, and
    // throws when it holds a JSON object.
    query.setPropertyNames(getImportableColumns(source.getSchema(), true));

    if (this.sortInDataStore(source))
    {
      query.setSortBy(SortBy.NATURAL_ORDER);
    }

    if (this.getStartIndex() > 0)
    {
      query.setStartIndex(Math.toIntExact(this.getStartIndex()));
    }

    try (SimpleFeatureIterator sr = source.getFeatures(query).features())
    {
      long rowNum = 1;

      while (sr.hasNext())
      {
        SimpleFeature feature = sr.next();

        if (stage.equals(ImportStage.VALIDATE))
        {
          this.objectImporter.validateRow(this.createRow(feature, rowNum));
        }
        else
        {
          this.objectImporter.importRow(this.createRow(feature, rowNum));
        }

        rowNum++;
      }
    }
    catch (Throwable t)
    {
      throw new ProgrammingErrorException(t);
    }
  }

  /**
   * @return true if the data store must be asked to return features in their
   *         natural order. This is free for databases (e.g. a GeoPackage is
   *         read with ORDER BY on its feature id, which is its primary key).
   *         Files such as shapefiles are already read in their natural order,
   *         and asking for it can make the data store sort them.
   */
  protected boolean sortInDataStore(SimpleFeatureSource source)
  {
    return false;
  }

  /**
   * Wraps a feature as a row for the object importer. Subclasses can override
   * this to convert values read by their format.
   */
  protected FeatureRow createRow(SimpleFeature feature, long rowNumber)
  {
    return new SimpleFeatureRow(feature, rowNumber);
  }

  @Override
  public Geometry getGeometry(FeatureRow row)
  {
    return (Geometry) ( (SimpleFeatureRow) row ).getFeature().getDefaultGeometry();
  }
}
