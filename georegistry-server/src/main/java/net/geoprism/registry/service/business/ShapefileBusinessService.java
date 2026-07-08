/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.service.business;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;

import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.stereotype.Service;

import com.runwaysdk.RunwayException;
import com.runwaysdk.business.SmartException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.system.VaultFile;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.InvalidProjectionException;
import net.geoprism.registry.UnableToReadProjectionException;
import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ObjectImporterFactory;
import net.geoprism.registry.etl.ShapefileFormatException;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.etl.upload.ShapefileImporter;
import net.geoprism.registry.excel.SheetDTO;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.io.PostalCodeFactory;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.view.GeoObjectImportConfigurationDTO;
import net.geoprism.registry.view.ImportConfigurationDTO;
import net.geoprism.registry.view.ImportConfigurationView;

@Service
public class ShapefileBusinessService extends DataImportBusinessService
{

  public GeoObjectImportConfigurationDTO getShapefileConfiguration(String fileName, InputStream fileStream, ImportConfigurationView view, boolean ignoreProjection)
  {
    // Save the file to the file system
    try
    {
      ServerGeoObjectType geoObjectType = ServerGeoObjectType.get(view.getType());

      VaultFile vf = VaultFile.createAndApply(fileName, fileStream);

      try (CloseableFile dbf = ShapefileImporter.getShapefileFromResource(vf, "dbf"))
      {
        SimpleDateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
        format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

        GeoObjectImportConfigurationDTO dto = new GeoObjectImportConfigurationDTO();
        dto.setType(this.getType(geoObjectType, false));
        dto.setVaultFileId(vf.getOid());
        dto.setFileName(fileName);
        dto.setImportStrategy(view.getStrategy());
        dto.setFormatType(FormatImporterType.SHAPEFILE);
        dto.setObjectType(ObjectImporterFactory.JobHistoryType.GEO_OBJECT);
        dto.setCopyBlank(view.getCopyBlank());
        dto.setSheet(this.getSheetInformation(dbf, ignoreProjection));
        dto.setPostalCode(PostalCodeFactory.isAvailable(geoObjectType));
        dto.setDataSource(view.getDataSource());
        dto.setDescription(view.getDescription());
        dto.setStartDate(view.getStartDate());
        dto.setEndDate(view.getEndDate());
        dto.setIgnoreProjection(ignoreProjection);

        return dto;
      }
    }
    catch (RunwayException | SmartException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  private SheetDTO getSheetInformation(File dbf, boolean ignoreProjection)
  {
    try
    {
      ShapefileDataStore store = new ShapefileDataStore(dbf.toURI().toURL());

      try
      {
        String[] typeNames = store.getTypeNames();

        if (typeNames.length > 0)
        {
          String typeName = typeNames[0];

          FeatureSource<SimpleFeatureType, SimpleFeature> source = store.getFeatureSource(typeName);

          SimpleFeatureType schema = source.getSchema();

          if (!ignoreProjection)
          {
            CoordinateReferenceSystem sourceCRS = schema.getCoordinateReferenceSystem();

            if (sourceCRS != null)
            {
              try
              {

                String code = CRS.lookupIdentifier(sourceCRS, true);

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

          List<AttributeDescriptor> descriptors = schema.getAttributeDescriptors();

          SheetDTO sheet = new SheetDTO();
          sheet.setName(typeName);

          for (AttributeDescriptor descriptor : descriptors)
          {
            if (! ( descriptor instanceof GeometryDescriptor ))
            {
              String name = descriptor.getName().getLocalPart();
              String baseType = GeoObjectImportConfiguration.getBaseType(descriptor.getType());

              sheet.put(baseType, name);

              if (baseType.equals(GeoObjectImportConfiguration.NUMERIC))
              {
                sheet.put(GeoObjectImportConfiguration.TEXT, name);
              }
            }
          }

          return sheet;
        }
        else
        {
          // TODO Change exception type
          throw new ProgrammingErrorException("Shapefile does not contain any types");
        }
      }
      finally
      {
        store.dispose();
      }
    }
    catch (RuntimeException e)
    {
      Throwable cause = e.getCause();

      if (cause instanceof IOException)
      {
        throw new ShapefileFormatException(e);
      }

      throw e;
    }
    catch (Exception e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public void cancelImport(ImportConfigurationDTO dto)
  {
    ImportConfiguration config = ImportConfiguration.build(dto);

    String id = config.getVaultFileId();

    VaultFile.get(id).delete();
  }

  public InputStream exportShapefile(String code, String hierarchyCode)
  {
    return GeoRegistryUtil.exportShapefile(code, hierarchyCode);
  }
}
