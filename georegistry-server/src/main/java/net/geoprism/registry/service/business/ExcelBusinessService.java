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

import java.io.InputStream;
import java.text.SimpleDateFormat;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.RunwayException;
import com.runwaysdk.business.SmartException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.system.VaultFile;

import net.geoprism.data.etl.excel.ExcelDataFormatter;
import net.geoprism.data.etl.excel.ExcelSheetReader;
import net.geoprism.data.etl.excel.InvalidExcelFileException;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ObjectImporterFactory;
import net.geoprism.registry.excel.ExcelFieldContentsHandler;
import net.geoprism.registry.graph.BusinessType;
import net.geoprism.registry.graph.ConceptClass;
import net.geoprism.registry.graph.ObjectClass;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.io.PostalCodeFactory;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.view.BusinessObjectImportConfigurationDTO;
import net.geoprism.registry.view.ConceptObjectImportConfigurationDTO;
import net.geoprism.registry.view.GeoObjectImportConfigurationDTO;
import net.geoprism.registry.view.ImportConfigurationView;
import net.geoprism.registry.view.TypedObjectImportConfigurationDTO;

@Service
public class ExcelBusinessService extends DataImportBusinessService
{
  @Autowired
  private BusinessTypeBusinessServiceIF bTypeService;

  @Autowired
  private ConceptClassBusinessServiceIF cClassService;

  public GeoObjectImportConfigurationDTO getExcelConfiguration(String fileName, InputStream fileStream, ImportConfigurationView view)
  {
    // Save the file to the file system
    try
    {
      ServerGeoObjectType geoObjectType = ServerGeoObjectType.get(view.getType());

      VaultFile vf = VaultFile.createAndApply(fileName, fileStream);

      try (InputStream is = vf.openNewStream())
      {
        SimpleDateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
        format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

        ExcelFieldContentsHandler handler = new ExcelFieldContentsHandler();
        ExcelDataFormatter formatter = new ExcelDataFormatter();

        ExcelSheetReader reader = new ExcelSheetReader(handler, formatter);
        reader.process(is);

        GeoObjectImportConfigurationDTO dto = new GeoObjectImportConfigurationDTO();
        dto.setType(this.getType(geoObjectType));
        dto.setVaultFileId(vf.getOid());
        dto.setFileName(fileName);
        dto.setImportStrategy(view.getStrategy());
        dto.setFormatType(FormatImporterType.EXCEL);
        dto.setObjectType(ObjectImporterFactory.JobHistoryType.BUSINESS_OBJECT);
        dto.setCopyBlank(view.getCopyBlank());
        dto.setSheet(handler.getSheets().get(0));
        dto.setPostalCode(PostalCodeFactory.isAvailable(geoObjectType));
        dto.setDataSource(view.getDataSource());
        dto.setDescription(view.getDescription());
        dto.setStartDate(view.getStartDate());
        dto.setEndDate(view.getEndDate());

        return dto;
      }
    }
    catch (InvalidFormatException e)
    {
      InvalidExcelFileException ex = new InvalidExcelFileException(e);
      ex.setFileName(fileName);

      throw ex;
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

  public BusinessObjectImportConfigurationDTO getBusinessTypeConfiguration(String fileName, InputStream fileStream, ImportConfigurationView view)
  {
    BusinessType type = this.bTypeService.getByCodeOrThrow(view.getType());
    BusinessObjectImportConfigurationDTO dto = new BusinessObjectImportConfigurationDTO();

    return (BusinessObjectImportConfigurationDTO) getTypedConfiguration(fileName, fileStream, view, type, dto);
  }

  public ConceptObjectImportConfigurationDTO getConceptClassConfiguration(String fileName, InputStream fileStream, ImportConfigurationView view)
  {
    ConceptClass type = this.cClassService.getByCodeOrThrow(view.getType());
    ConceptObjectImportConfigurationDTO dto = new ConceptObjectImportConfigurationDTO();

    return (ConceptObjectImportConfigurationDTO) getTypedConfiguration(fileName, fileStream, view, type, dto);
  }

  public TypedObjectImportConfigurationDTO getTypedConfiguration(String fileName, InputStream fileStream, ImportConfigurationView view, ObjectClass businessType, TypedObjectImportConfigurationDTO dto)
  {
    // Save the file to the file system
    try
    {
      VaultFile vf = VaultFile.createAndApply(fileName, fileStream);

      try (InputStream is = vf.openNewStream())
      {
        ExcelFieldContentsHandler handler = new ExcelFieldContentsHandler();
        ExcelDataFormatter formatter = new ExcelDataFormatter();

        ExcelSheetReader reader = new ExcelSheetReader(handler, formatter);
        reader.process(is);

        dto.setType(this.getType(businessType));
        dto.setVaultFileId(vf.getOid());
        dto.setFileName(fileName);
        dto.setImportStrategy(view.getStrategy());
        dto.setFormatType(FormatImporterType.EXCEL);
        dto.setCopyBlank(view.getCopyBlank());
        dto.setSheet(handler.getSheets().get(0));
        dto.setDataSource(view.getDataSource());
        dto.setDescription(view.getDescription());
        dto.setStartDate(view.getStartDate());
        dto.setEndDate(view.getEndDate());

        return dto;
      }
    }
    catch (InvalidFormatException e)
    {
      InvalidExcelFileException ex = new InvalidExcelFileException(e);
      ex.setFileName(fileName);

      throw ex;
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

  public InputStream exportSpreadsheet(String code, String hierarchyCode)
  {
    return GeoRegistryUtil.exportSpreadsheet(code, hierarchyCode);
  }
}
