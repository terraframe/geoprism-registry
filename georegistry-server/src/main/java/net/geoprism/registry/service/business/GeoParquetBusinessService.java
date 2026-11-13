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

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import com.runwaysdk.RunwayException;
import com.runwaysdk.business.SmartException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.system.VaultFile;

import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ObjectImporterFactory;
import net.geoprism.registry.etl.upload.GeoParquetReader;
import net.geoprism.registry.etl.upload.GeoParquetReader.Column;
import net.geoprism.registry.excel.SheetDTO;
import net.geoprism.registry.io.ColumnUniquenessTracker;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.io.PostalCodeFactory;
import net.geoprism.registry.io.view.GeoObjectImportConfigurationDTO;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.view.ImportConfigurationView;

@Service
public class GeoParquetBusinessService extends DataImportBusinessService
{
  public GeoObjectImportConfigurationDTO getConfiguration(String fileName, InputStream fileStream, ImportConfigurationView view, boolean ignoreProjection)
  {
    try
    {
      ServerGeoObjectType geoObjectType = ServerGeoObjectType.get(view.getType());

      VaultFile vf = VaultFile.createAndApply(fileName, fileStream);

      try (CloseableFile file = GeoParquetReader.getParquetFromResource(vf); GeoParquetReader reader = new GeoParquetReader(file))
      {
        if (!ignoreProjection)
        {
          reader.validateProjection();
        }

        GeoObjectImportConfigurationDTO dto = new GeoObjectImportConfigurationDTO();
        dto.setType(this.getType(geoObjectType, false));
        dto.setVaultFileId(vf.getOid());
        dto.setFileName(fileName);
        dto.setImportStrategy(view.getStrategy());
        dto.setFormatType(FormatImporterType.GEOPARQUET);
        dto.setObjectType(ObjectImporterFactory.JobHistoryType.GEO_OBJECT);
        dto.setCopyBlank(view.getCopyBlank());
        dto.setSheet(this.getSheetInformation(reader, FilenameUtils.getBaseName(fileName)));
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

  private SheetDTO getSheetInformation(GeoParquetReader reader, String name) throws Exception
  {
    SheetDTO sheet = new SheetDTO();
    sheet.setName(name);

    for (Column column : reader.getColumns())
    {
      String baseType = GeoObjectImportConfiguration.getBaseType(column.getBinding());

      sheet.put(baseType, column.getName());

      if (baseType.equals(GeoObjectImportConfiguration.NUMERIC))
      {
        sheet.put(GeoObjectImportConfiguration.TEXT, column.getName());
      }
    }

    sheet.getNonUniqueAttributes().addAll(this.getNonUniqueColumns(reader));

    return sheet;
  }

  /**
   * Scans the rows of the file, up to {@link ColumnUniquenessTracker#MAX_ROWS},
   * to determine which columns contain duplicate values. The geometry column is
   * not read.
   */
  private Set<String> getNonUniqueColumns(GeoParquetReader reader) throws Exception
  {
    List<Column> columns = reader.getColumns();

    ColumnUniquenessTracker tracker = new ColumnUniquenessTracker();

    if (!columns.isEmpty())
    {
      reader.read(0, false, (long) ColumnUniquenessTracker.MAX_ROWS, row -> {
        for (Column column : columns)
        {
          tracker.add(column.getName(), row.getValue(column.getName()));
        }

        return !tracker.isExhausted();
      });
    }

    return tracker.getNonUniqueColumns();
  }
}
