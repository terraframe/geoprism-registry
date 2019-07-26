/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
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
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.dataaccess.metadata.SupportedLocaleDAO;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.vividsolutions.jts.geom.Point;

import net.geoprism.registry.io.GeoObjectConfiguration;
import net.geoprism.registry.io.GeoObjectUtil;
import net.geoprism.registry.io.ImportAttributeSerializer;
import net.geoprism.registry.service.ServiceFactory;

public class GeoObjectExcelExporter
{
  private static Logger        logger = LoggerFactory.getLogger(GeoObjectExcelExporter.class);

  private GeoObjectType        type;

  private HierarchyType        hierarchy;

  private OIterator<GeoObject> objects;

  public GeoObjectExcelExporter(GeoObjectType type, HierarchyType hierarchy, OIterator<GeoObject> objects)
  {
    this.type = type;
    this.hierarchy = hierarchy;
    this.objects = objects;
  }

  public GeoObjectType getType()
  {
    return type;
  }

  public void setType(GeoObjectType type)
  {
    this.type = type;
  }

  public Iterable<GeoObject> getObjects()
  {
    return objects;
  }

  public void setObjects(OIterator<GeoObject> objects)
  {
    this.objects = objects;
  }

  public Workbook createWorkbook() throws IOException
  {
    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

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

    boolean includeCoordinates = this.type.getGeometryType().equals(GeometryType.POINT);
    Collection<AttributeType> attributes = new ImportAttributeSerializer(Session.getCurrentLocale(), includeCoordinates, true, locales).attributes(this.type);

    // Get the ancestors of the type
    List<GeoObjectType> ancestors = ServiceFactory.getUtilities().getAncestors(this.type, this.hierarchy.getCode());

    this.writeHeader(boldStyle, header, attributes, ancestors, locales);

    int rownum = 1;

    while (this.objects.hasNext())
    {
      GeoObject object = this.objects.next();

      Row row = sheet.createRow(rownum++);

      this.writeRow(row, object, attributes, ancestors, locales, dateStyle);
    }

    return workbook;
  }

  public void writeRow(Row row, GeoObject object, Collection<AttributeType> attributes, List<GeoObjectType> ancestors, List<Locale> locales, CellStyle dateStyle)
  {
    int col = 0;
    // Write the row
    for (AttributeType attribute : attributes)
    {
      String name = attribute.getName();
      Cell cell = row.createCell(col++);

      if (name.equals(GeoObjectConfiguration.LATITUDE))
      {
        Point point = (Point) object.getGeometry();

        if (point != null)
        {
          cell.setCellValue(point.getY());
        }
      }
      else if (name.equals(GeoObjectConfiguration.LONGITUDE))
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
    Map<String, ValueObject> map = GeoObjectUtil.getAncestorMap(object, this.hierarchy);

    for (GeoObjectType ancestor : ancestors)
    {
      ValueObject vObject = map.get(ancestor.getCode());

      Cell codeCell = row.createCell(col++);
      Cell labelCell = row.createCell(col++);

      for (int i = 0; i < locales.size(); i++)
      {
        row.createCell(col++);
      }

      if (vObject != null)
      {
        codeCell.setCellValue(vObject.getValue(GeoEntity.GEOID));
        labelCell.setCellValue(vObject.getValue(DefaultAttribute.DISPLAY_LABEL.getName()));

        for (int i = 0; i < locales.size(); i++)
        {
          Locale locale = locales.get(i);

          Cell cell = row.getCell(labelCell.getColumnIndex() + i + 1);
          cell.setCellValue(vObject.getValue(DefaultAttribute.DISPLAY_LABEL.getName() + "_" + locale.toString()));
        }
      }
    }
  }

  public void writeHeader(CellStyle boldStyle, Row header, Collection<AttributeType> attributes, List<GeoObjectType> ancestors, List<Locale> locales)
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

    for (GeoObjectType ancestor : ancestors)
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
