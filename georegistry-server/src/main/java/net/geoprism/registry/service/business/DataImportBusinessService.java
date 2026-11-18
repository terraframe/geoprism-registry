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
package net.geoprism.registry.service.business;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.geotools.api.data.FeatureSource;
import org.geotools.api.data.Query;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.feature.type.GeometryDescriptor;
import org.geotools.api.filter.Filter;
import org.geotools.feature.FeatureIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.localization.LocalizationFacade;

import net.geoprism.registry.etl.upload.FeatureSourceImporter;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.excel.SheetDTO;
import net.geoprism.registry.io.ColumnUniquenessTracker;
import net.geoprism.registry.graph.ObjectClass;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.io.view.ImportColumnDTO;
import net.geoprism.registry.io.view.ImportTypeDTO;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.ObjectClassIF;

public abstract class DataImportBusinessService
{
  private static final Logger logger              = LoggerFactory.getLogger(DataImportBusinessService.class);

  /**
   * Maximum number of features read when checking for non-unique columns.
   * Duplicates beyond this point will not be detected.
   */
  private static final int    MAX_UNIQUENESS_ROWS = 100_000;

  /**
   * Describes the columns of a GeoTools feature source (a shapefile or a
   * GeoPackage layer) for matching to attributes: the columns available for
   * each attribute type, the columns with duplicate values, and a sample of
   * the first rows. The geometry is never read.
   *
   * @param name
   *          Name of the sheet, e.g. the shapefile or layer name
   */
  protected SheetDTO getSheetInformation(String name, FeatureSource<SimpleFeatureType, SimpleFeature> source, boolean ignoreProjection) throws IOException
  {
    SimpleFeatureType schema = source.getSchema();

    if (!ignoreProjection)
    {
      FeatureSourceImporter.validateProjection(schema);
    }

    SheetDTO sheet = new SheetDTO();
    sheet.setName(name);

    List<String> names = new ArrayList<>();

    for (AttributeDescriptor descriptor : schema.getAttributeDescriptors())
    {
      if (! ( descriptor instanceof GeometryDescriptor ))
      {
        String column = descriptor.getName().getLocalPart();

        if (!FeatureSourceImporter.isImportable(descriptor))
        {
          // e.g. binary columns, which can't be matched to any attribute
          logger.debug("Skipping column [" + column + "] with unsupported type [" + descriptor.getType().getBinding().getName() + "]");

          continue;
        }

        String baseType = GeoObjectImportConfiguration.getBaseType(descriptor.getType());

        names.add(column);

        sheet.put(baseType, column);

        if (baseType.equals(GeoObjectImportConfiguration.NUMERIC))
        {
          sheet.put(GeoObjectImportConfiguration.TEXT, column);
        }
      }
    }

    sheet.getNonUniqueAttributes().addAll(this.getNonUniqueColumns(source, names));

    this.addSample(sheet, source, names);

    return sheet;
  }

  /**
   * @return The value of a feature's column, as it will be imported. Formats
   *         whose values need converting override this.
   */
  protected Object getValue(SimpleFeature feature, String name)
  {
    return feature.getAttribute(name);
  }

  /**
   * Scans the features, up to {@link #MAX_UNIQUENESS_ROWS}, to determine which
   * of the columns contain duplicate values. Only the given columns are read,
   * so geometries are never parsed.
   */
  private Set<String> getNonUniqueColumns(FeatureSource<SimpleFeatureType, SimpleFeature> source, List<String> names) throws IOException
  {
    ColumnUniquenessTracker tracker = new ColumnUniquenessTracker();

    if (names.isEmpty())
    {
      return tracker.getNonUniqueColumns();
    }

    Query query = new Query(source.getSchema().getTypeName(), Filter.INCLUDE, names.toArray(new String[names.size()]));
    query.setMaxFeatures(MAX_UNIQUENESS_ROWS);

    try (FeatureIterator<SimpleFeature> iterator = source.getFeatures(query).features())
    {
      while (iterator.hasNext() && !tracker.isExhausted())
      {
        SimpleFeature feature = iterator.next();

        for (String name : names)
        {
          tracker.add(name, this.getValue(feature, name));
        }
      }
    }

    return tracker.getNonUniqueColumns();
  }

  /**
   * Reads the first {@link SheetDTO#MAX_SAMPLE_ROWS} features into the sheet's
   * sample. Only the given columns are read, so geometries are never parsed.
   */
  private void addSample(SheetDTO sheet, FeatureSource<SimpleFeatureType, SimpleFeature> source, List<String> names) throws IOException
  {
    if (names.isEmpty())
    {
      return;
    }

    Query query = new Query(source.getSchema().getTypeName(), Filter.INCLUDE, names.toArray(new String[names.size()]));
    query.setMaxFeatures(SheetDTO.MAX_SAMPLE_ROWS);

    try (FeatureIterator<SimpleFeature> iterator = source.getFeatures(query).features())
    {
      while (iterator.hasNext() && !sheet.sampleFull())
      {
        SimpleFeature feature = iterator.next();

        Map<String, Object> row = new LinkedHashMap<>();

        for (String name : names)
        {
          row.put(name, this.getValue(feature, name));
        }

        sheet.addSampleRow(row);
      }
    }
  }

  protected ImportTypeDTO getType(ServerGeoObjectType geoObjectType)
  {
    final boolean includeCoordinates = geoObjectType.getGeometryType().equals(GeometryType.POINT) //
        || geoObjectType.getGeometryType().equals(GeometryType.MULTIPOINT) //
        || geoObjectType.getGeometryType().equals(GeometryType.MIXED);

    return getType(geoObjectType, includeCoordinates);

  }

  protected ImportTypeDTO getType(ServerGeoObjectType geoObjectType, boolean includeCoordinates)
  {
    ImportTypeDTO type = ImportConfiguration.toTypeDTO(geoObjectType, new HashMap<>());

    if (includeCoordinates)
    {
      ImportColumnDTO latitude = new ImportColumnDTO();
      latitude.setCode(GeoObjectImportConfiguration.LATITUDE);
      latitude.setBaseType(GeoObjectImportConfiguration.NUMERIC);
      latitude.setLabel(new LocalizedValue(LocalizationFacade.localize(GeoObjectImportConfiguration.LATITUDE_KEY)));

      ImportColumnDTO longitude = new ImportColumnDTO();
      longitude.setCode(GeoObjectImportConfiguration.LONGITUDE);
      longitude.setBaseType(GeoObjectImportConfiguration.NUMERIC);
      longitude.setLabel(new LocalizedValue(LocalizationFacade.localize(GeoObjectImportConfiguration.LONGITUDE_KEY)));

      type.getAttributes().add(0, latitude);
      type.getAttributes().add(0, longitude);
    }

    return type;
  }

  protected ImportTypeDTO getType(ObjectClass type)
  {
    return ImportConfiguration.toTypeDTO(type, new HashMap<>());
  }

  protected ImportTypeDTO getType(ObjectClassIF type)
  {
    if (type instanceof ServerGeoObjectType)
    {
      return this.getType((ServerGeoObjectType) type);
    }

    return this.getType((ObjectClass) type);
  }
}
