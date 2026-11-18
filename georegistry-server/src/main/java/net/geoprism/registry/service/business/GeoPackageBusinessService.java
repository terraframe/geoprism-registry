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

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.geotools.api.data.DataStore;
import org.geotools.api.feature.simple.SimpleFeature;
import org.springframework.stereotype.Service;

import com.runwaysdk.RunwayException;
import com.runwaysdk.business.SmartException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.system.VaultFile;

import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ObjectImporterFactory;
import net.geoprism.registry.etl.upload.GeoPackageImporter;
import net.geoprism.registry.etl.upload.ImportFileResources;
import net.geoprism.registry.excel.SheetDTO;
import net.geoprism.registry.io.PostalCodeFactory;
import net.geoprism.registry.io.view.GeoObjectImportConfigurationDTO;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.view.ImportConfigurationView;

@Service
public class GeoPackageBusinessService extends DataImportBusinessService
{
  public GeoObjectImportConfigurationDTO getConfiguration(String fileName, InputStream fileStream, ImportConfigurationView view, boolean ignoreProjection)
  {
    try
    {
      ServerGeoObjectType geoObjectType = ServerGeoObjectType.get(view.getType());

      VaultFile vf = VaultFile.createAndApply(fileName, fileStream);

      try (CloseableFile file = GeoPackageImporter.getGeoPackageFromResource(vf))
      {
        GeoObjectImportConfigurationDTO dto = new GeoObjectImportConfigurationDTO();
        dto.setType(this.getType(geoObjectType, false));
        dto.setVaultFileId(vf.getOid());
        dto.setFileName(fileName);
        dto.setImportStrategy(view.getStrategy());
        dto.setFormatType(FormatImporterType.GEOPACKAGE);
        dto.setObjectType(ObjectImporterFactory.JobHistoryType.GEO_OBJECT);
        dto.setCopyBlank(view.getCopyBlank());
        dto.setSheet(this.getSheetInformation(file, ignoreProjection));
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

  /**
   * Describes the columns of the GeoPackage's layer of features. Package
   * visible so it can be tested without a database.
   */
  SheetDTO getSheetInformation(File file, boolean ignoreProjection) throws Exception
  {
    DataStore store = GeoPackageImporter.open(file);

    try
    {
      String layer = GeoPackageImporter.getLayer(store);

      SheetDTO sheet = this.getSheetInformation(layer, store.getFeatureSource(layer), ignoreProjection);

      // Use the file's name rather than the layer's, as for shapefiles
      sheet.setName(FilenameUtils.getBaseName(ImportFileResources.getDisplayName(file)));

      return sheet;
    }
    finally
    {
      store.dispose();
    }
  }

  /**
   * Shows dates in the preview the way they will be imported
   */
  @Override
  protected Object getValue(SimpleFeature feature, String name)
  {
    return GeoPackageImporter.convert(super.getValue(feature, name));
  }
}
