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
package net.geoprism.registry.service;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.runwaysdk.RunwayException;
import com.runwaysdk.business.SmartException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.VaultFile;

import net.geoprism.data.etl.excel.ExcelDataFormatter;
import net.geoprism.data.etl.excel.ExcelSheetReader;
import net.geoprism.data.etl.excel.InvalidExcelFileException;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ObjectImporterFactory;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.etl.upload.BusinessObjectImportConfiguration;
import net.geoprism.registry.excel.ExcelFieldContentsHandler;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.io.ImportAttributeSerializer;
import net.geoprism.registry.io.PostalCodeFactory;
import net.geoprism.registry.model.ServerGeoObjectType;

@Component
public class ExcelService
{

  @Request(RequestType.SESSION)
  public JSONObject getExcelConfiguration(String sessionId, String type, Date startDate, Date endDate, String fileName, InputStream fileStream, ImportStrategy strategy, Boolean copyBlank)
  {
    return getExcelConfiguration(type, startDate, endDate, fileName, fileStream, strategy, copyBlank);
  }

  public JSONObject getExcelConfiguration(String type, Date startDate, Date endDate, String fileName, InputStream fileStream, ImportStrategy strategy, Boolean copyBlank)
  {
    // Save the file to the file system
    try
    {
      ServerGeoObjectType geoObjectType = ServerGeoObjectType.get(type);

      VaultFile vf = VaultFile.createAndApply(fileName, fileStream);

      try (InputStream is = vf.openNewStream())
      {
        SimpleDateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
        format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

        ExcelFieldContentsHandler handler = new ExcelFieldContentsHandler();
        ExcelDataFormatter formatter = new ExcelDataFormatter();

        ExcelSheetReader reader = new ExcelSheetReader(handler, formatter);
        reader.process(is);

        JSONObject object = new JSONObject();
        object.put(GeoObjectImportConfiguration.TYPE, this.getType(geoObjectType));
        object.put(GeoObjectImportConfiguration.SHEET, handler.getSheets().getJSONObject(0));
        object.put(ImportConfiguration.VAULT_FILE_ID, vf.getOid());
        object.put(ImportConfiguration.FILE_NAME, fileName);
        object.put(GeoObjectImportConfiguration.HAS_POSTAL_CODE, PostalCodeFactory.isAvailable(geoObjectType));
        object.put(ImportConfiguration.IMPORT_STRATEGY, strategy.name());
        object.put(ImportConfiguration.FORMAT_TYPE, FormatImporterType.EXCEL.name());
        object.put(ImportConfiguration.OBJECT_TYPE, ObjectImporterFactory.ObjectImportType.GEO_OBJECT.name());
        object.put(ImportConfiguration.COPY_BLANK, copyBlank);

        if (startDate != null)
        {
          object.put(GeoObjectImportConfiguration.START_DATE, format.format(startDate));
        }

        if (endDate != null)
        {
          object.put(GeoObjectImportConfiguration.END_DATE, format.format(endDate));
        }

        return object;
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

  @Request(RequestType.SESSION)
  public JSONObject getBusinessTypeConfiguration(String sessionId, String type, Date date, String fileName, InputStream fileStream, ImportStrategy strategy, Boolean copyBlank)
  {
    return this.getBusinessTypeConfiguration(type, date, fileName, fileStream, strategy, copyBlank);
  }

  public JSONObject getBusinessTypeConfiguration(String type, Date date, String fileName, InputStream fileStream, ImportStrategy strategy, Boolean copyBlank)
  {
    // Save the file to the file system
    try
    {
      BusinessType businessType = BusinessType.getByCode(type);

      VaultFile vf = VaultFile.createAndApply(fileName, fileStream);

      try (InputStream is = vf.openNewStream())
      {
        SimpleDateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
        format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

        ExcelFieldContentsHandler handler = new ExcelFieldContentsHandler();
        ExcelDataFormatter formatter = new ExcelDataFormatter();

        ExcelSheetReader reader = new ExcelSheetReader(handler, formatter);
        reader.process(is);

        JSONObject object = new JSONObject();
        object.put(BusinessObjectImportConfiguration.TYPE, this.getType(businessType));
        object.put(BusinessObjectImportConfiguration.SHEET, handler.getSheets().getJSONObject(0));
        object.put(BusinessObjectImportConfiguration.VAULT_FILE_ID, vf.getOid());
        object.put(BusinessObjectImportConfiguration.FILE_NAME, fileName);
        object.put(BusinessObjectImportConfiguration.IMPORT_STRATEGY, strategy.name());
        object.put(BusinessObjectImportConfiguration.FORMAT_TYPE, FormatImporterType.EXCEL.name());
        object.put(BusinessObjectImportConfiguration.OBJECT_TYPE, ObjectImporterFactory.ObjectImportType.BUSINESS_OBJECT.name());
        object.put(BusinessObjectImportConfiguration.COPY_BLANK, copyBlank);

        if (date != null)
        {
          object.put(BusinessObjectImportConfiguration.DATE, format.format(date));
        }

        return object;
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

  private JSONObject getType(ServerGeoObjectType geoObjectType)
  {
    final boolean includeCoordinates = geoObjectType.getGeometryType().equals(GeometryType.POINT) || geoObjectType.getGeometryType().equals(GeometryType.MULTIPOINT) || geoObjectType.getGeometryType().equals(GeometryType.MIXED);
    final ImportAttributeSerializer serializer = new ImportAttributeSerializer(Session.getCurrentLocale(), includeCoordinates, false, geoObjectType.getType());

    JSONObject type = new JSONObject(geoObjectType.toJSON(serializer).toString());
    JSONArray attributes = type.getJSONArray(GeoObjectType.JSON_ATTRIBUTES);

    for (int i = 0; i < attributes.length(); i++)
    {
      JSONObject attribute = attributes.getJSONObject(i);
      String attributeType = attribute.getString(AttributeType.JSON_TYPE);

      attribute.put(GeoObjectImportConfiguration.BASE_TYPE, GeoObjectImportConfiguration.getBaseType(attributeType));
    }

    return type;
  }

  private JSONObject getType(BusinessType pType)
  {
    JSONObject type = new JSONObject(pType.toJSON(true, true).toString());
    JSONArray attributes = type.getJSONArray(GeoObjectType.JSON_ATTRIBUTES);

    for (int i = 0; i < attributes.length(); i++)
    {
      JSONObject attribute = attributes.getJSONObject(i);
      String attributeType = attribute.getString(AttributeType.JSON_TYPE);

      attribute.put(BusinessObjectImportConfiguration.BASE_TYPE, BusinessObjectImportConfiguration.getBaseType(attributeType));
    }

    return type;
  }

  @Request(RequestType.SESSION)
  public InputStream exportSpreadsheet(String sessionId, String code, String hierarchyCode)
  {
    return GeoRegistryUtil.exportSpreadsheet(code, hierarchyCode);
  }
}
