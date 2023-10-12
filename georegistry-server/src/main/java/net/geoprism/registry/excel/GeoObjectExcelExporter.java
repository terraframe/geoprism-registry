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
package net.geoprism.registry.excel;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.session.Session;

import net.geoprism.registry.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.io.GeoObjectUtil;
import net.geoprism.registry.io.ImportAttributeSerializer;
import net.geoprism.registry.model.LocationInfo;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.ServiceFactory;

public class GeoObjectExcelExporter
{
  private static Logger                  logger = LoggerFactory.getLogger(GeoObjectExcelExporter.class);

  private ServerGeoObjectType            type;

  private ServerHierarchyType            hierarchy;

  private List<ServerGeoObjectIF>        objects;

  private List<Locale>                   locales;

  private GeoObjectTypeBusinessServiceIF typeService;

  private GeoObjectBusinessServiceIF     objectService;

  public GeoObjectExcelExporter(ServerGeoObjectType type, ServerHierarchyType hierarchy, List<ServerGeoObjectIF> objects)
  {
    this.typeService = ServiceFactory.getBean(GeoObjectTypeBusinessServiceIF.class);
    this.objectService = ServiceFactory.getBean(GeoObjectBusinessServiceIF.class);

    this.type = type;
    this.hierarchy = hierarchy;
    this.objects = objects;
    this.locales = LocalizationFacade.getInstalledLocales().stream().collect(Collectors.toList());
  }

  public ServerGeoObjectType getType()
  {
    return type;
  }

  public void setType(ServerGeoObjectType type)
  {
    this.type = type;
  }

  public List<ServerGeoObjectIF> getObjects()
  {
    return objects;
  }

  public void setObjects(List<ServerGeoObjectIF> objects)
  {
    this.objects = objects;
  }

  public Workbook createWorkbook() throws IOException
  {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(this.type.getLabel().getValue()));

    CreationHelper createHelper = workbook.getCreationHelper();
    Font font = workbook.createFont();
    font.setBold(true);

    CellStyle boldStyle = workbook.createCellStyle();
    boldStyle.setFont(font);

    CellStyle dateStyle = workbook.createCellStyle();
    dateStyle.setDataFormat(createHelper.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat(14)));

    Row header = sheet.createRow(0);

    boolean includeCoordinates = this.type.getGeometryType().equals(GeometryType.POINT) || this.type.getGeometryType().equals(GeometryType.MIXED);
    Collection<AttributeType> attributes = new ImportAttributeSerializer(Session.getCurrentLocale(), includeCoordinates, true, this.type.getType()).attributes(this.type.getType());

    // Get the ancestors of the type
    List<ServerGeoObjectType> ancestors = this.typeService.getTypeAncestors(type, this.hierarchy, true);

    this.writeHeader(boldStyle, header, attributes, ancestors);

    for (int i = 0; i < this.objects.size(); i++)
    {
      ServerGeoObjectIF object = this.objects.get(i);
      Row row = sheet.createRow(i + 1);

      this.writeRow(row, object, attributes, ancestors, dateStyle);
    }

    return workbook;
  }

  public void writeRow(Row row, ServerGeoObjectIF object, Collection<AttributeType> attributes, List<ServerGeoObjectType> ancestors, CellStyle dateStyle)
  {
    int col = 0;
    // Write the row
    for (AttributeType attribute : attributes)
    {
      String name = attribute.getName();
      Cell cell = row.createCell(col++);

      if (name.equals(GeoObjectImportConfiguration.LATITUDE))
      {
        Point point = (Point) object.getGeometry();

        if (point != null)
        {
          cell.setCellValue(point.getY());
        }
      }
      else if (name.equals(GeoObjectImportConfiguration.LONGITUDE))
      {
        Point point = (Point) object.getGeometry();
        if (point != null)
        {
          cell.setCellValue(point.getX());
        }
      }
      else
      {
        Object value = object.getValue(name);

        if (value != null)
        {
          if (attribute instanceof AttributeTermType)
          {
            cell.setCellValue(GeoObjectUtil.convertToTermString((AttributeTermType) attribute, value));
          }
          else if (attribute instanceof AttributeClassificationType)
          {
            cell.setCellValue(GeoObjectUtil.convertToTermString((AttributeClassificationType) attribute, value));
          }
          else if (attribute instanceof AttributeLocalType)
          {
            cell.setCellValue( ( (LocalizedValue) value ).getValue());
          }
          else
          {
            if (value instanceof String)
            {
              cell.setCellValue((String) value);
            }
            else if (value instanceof Date)
            {
              cell.setCellValue((Date) value);
              cell.setCellStyle(dateStyle);
            }
            else if (value instanceof Number)
            {
              cell.setCellValue( ( (Number) value ).doubleValue());
            }
            else if (value instanceof Boolean)
            {
              cell.setCellValue((Boolean) value);
            }
          }
        }
      }
    }

    {
      LocalizedValue value = (LocalizedValue) object.getValue(DefaultAttribute.DISPLAY_LABEL.getName());

      Cell cell = row.createCell(col++);
      cell.setCellValue(value.getValue(LocalizedValue.DEFAULT_LOCALE));

      for (Locale locale : locales)
      {
        cell = row.createCell(col++);
        cell.setCellValue(value.getValue(locale));
      }
    }

    // Write the parent values
    Map<String, LocationInfo> map = this.objectService.getAncestorMap(object, this.hierarchy, ancestors);

    for (ServerGeoObjectType ancestor : ancestors)
    {
      LocationInfo vObject = map.get(ancestor.getCode());

      Cell codeCell = row.createCell(col++);
      Cell labelCell = row.createCell(col++);

      for (int i = 0; i < locales.size(); i++)
      {
        row.createCell(col++);
      }

      if (vObject != null)
      {
        codeCell.setCellValue(vObject.getCode());
        labelCell.setCellValue(vObject.getLabel());

        for (int i = 0; i < locales.size(); ++i)
        {
          Cell cell = row.getCell(labelCell.getColumnIndex() + i + 1);
          cell.setCellValue(vObject.getLabel(locales.get(i)));
        }
      }
    }
  }

  public void writeHeader(CellStyle boldStyle, Row header, Collection<AttributeType> attributes, List<ServerGeoObjectType> ancestors)
  {
    int col = 0;

    for (AttributeType attribute : attributes)
    {
      Cell cell = header.createCell(col++);
      cell.setCellStyle(boldStyle);
      cell.setCellValue(attribute.getLabel().getValue());
    }

    {
      AttributeType attribute = this.getType().getAttribute(DefaultAttribute.DISPLAY_LABEL.getName()).get();

      Cell cell = header.createCell(col++);
      cell.setCellStyle(boldStyle);
      cell.setCellValue(attribute.getLabel().getValue() + " (" + MdAttributeLocalInfo.DEFAULT_LOCALE + ")");

      for (Locale locale : locales)
      {
        cell = header.createCell(col++);
        cell.setCellStyle(boldStyle);
        cell.setCellValue(attribute.getLabel().getValue() + " (" + locale.toString() + ")");
      }
    }

    for (ServerGeoObjectType ancestor : ancestors)
    {
      Cell cell = header.createCell(col++);
      cell.setCellStyle(boldStyle);
      cell.setCellValue(ancestor.getLabel().getValue() + " " + ancestor.getAttribute(GeoObject.CODE).get().getLabel().getValue());

      cell = header.createCell(col++);
      cell.setCellStyle(boldStyle);
      cell.setCellValue(ancestor.getLabel().getValue() + " (" + MdAttributeLocalInfo.DEFAULT_LOCALE + ")");

      for (Locale locale : locales)
      {
        cell = header.createCell(col++);
        cell.setCellStyle(boldStyle);
        cell.setCellValue(ancestor.getLabel().getValue() + " (" + locale.toString() + ")");
      }
    }
  }

  public InputStream export() throws IOException
  {
    final Workbook workbook = this.createWorkbook();

    PipedOutputStream pos = new PipedOutputStream();
    PipedInputStream pis = new PipedInputStream(pos);

    Thread t = new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          try
          {
            workbook.write(pos);
          }
          finally
          {
            pos.close();
          }
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

}
