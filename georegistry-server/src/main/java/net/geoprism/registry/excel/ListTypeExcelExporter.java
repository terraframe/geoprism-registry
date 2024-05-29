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
package net.geoprism.registry.excel;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Session;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.masterlist.AbstractListColumnVisitor;
import net.geoprism.registry.masterlist.ListAttribute;
import net.geoprism.registry.masterlist.ListAttributeGroup;
import net.geoprism.registry.masterlist.ListColumn;
import net.geoprism.registry.masterlist.ListColumnVisitor;
import net.geoprism.registry.shapefile.ShapefileColumnNameGenerator;

public class ListTypeExcelExporter
{
  private static Logger                            logger = LoggerFactory.getLogger(GeoObjectExcelExporter.class);

  private ListType                                 list;

  private ListTypeVersion                          version;

  private List<ListColumn>                         columns;

  private List<? extends MdAttributeConcreteDAOIF> mdAttributes;

  private JsonObject                               criteria;

  private CellStyle                                boldStyle;

  private CellStyle                                dateStyle;

  private ListTypeExcelExporterSheet[]             includedSheets;

  private ListMetadataSource                       metadataSource;

  private ShapefileColumnNameGenerator             generator;

  public static enum ListTypeExcelExporterSheet {
    DATA, METADATA, DICTIONARY
  }

  public static enum ListMetadataSource {
    LIST, GEOSPATIAL
  }

  public ListTypeExcelExporter(ListTypeVersion version, List<? extends MdAttributeConcreteDAOIF> mdAttributes, ListTypeExcelExporterSheet[] includedSheets, JsonObject criteria, ListMetadataSource metadataSource)
  {
    this(version, mdAttributes, includedSheets, criteria, metadataSource, new ShapefileColumnNameGenerator());
  }

  public ListTypeExcelExporter(ListTypeVersion version, List<? extends MdAttributeConcreteDAOIF> mdAttributes, ListTypeExcelExporterSheet[] includedSheets, JsonObject criteria, ListMetadataSource metadataSource, ShapefileColumnNameGenerator generator)
  {
    this.version = version;
    this.mdAttributes = mdAttributes;
    this.criteria = criteria;
    this.metadataSource = metadataSource;
    this.generator = generator;

    if (includedSheets != null)
    {
      this.includedSheets = includedSheets;
    }
    else
    {
      this.includedSheets = ListTypeExcelExporterSheet.values();
    }

    this.list = version.getListType();
    this.columns = version.getAttributeColumns();
  }

  public ListType getList()
  {
    return list;
  }

  public void setList(ListType list)
  {
    this.list = list;
  }

  public Workbook createWorkbook() throws IOException
  {
    LocaleUtil.setUserTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    Workbook workbook = new XSSFWorkbook();

    CreationHelper createHelper = workbook.getCreationHelper();
    Font font = workbook.createFont();
    font.setBold(true);

    this.boldStyle = workbook.createCellStyle();
    this.boldStyle.setFont(font);

    this.dateStyle = workbook.createCellStyle();

    DataFormat df = createHelper.createDataFormat();
    this.dateStyle.setDataFormat(df.getFormat("yyyy-mm-dd"));

    if (ArrayUtils.contains(this.includedSheets, ListTypeExcelExporterSheet.DATA))
    {
      this.createDataSheet(workbook);
    }
    if (ArrayUtils.contains(this.includedSheets, ListTypeExcelExporterSheet.METADATA))
    {
      this.createMetadataSheet(workbook);
    }
    if (ArrayUtils.contains(this.includedSheets, ListTypeExcelExporterSheet.DICTIONARY))
    {
      this.createDataDictionarySheet(workbook);
    }

    return workbook;
  }

  private void createDataDictionarySheet(Workbook workbook)
  {
    Sheet sheet = workbook.createSheet(getSheetName(workbook, "masterlist.data.dictionary"));

    if (metadataSource.equals(ListMetadataSource.GEOSPATIAL))
    {
      String attributeLabel = LocalizationFacade.localize("masterlist.dictionary.attribute");
      String descriptionLabel = LocalizationFacade.localize("masterlist.dictionary.description");
      String columnLabel = LocalizationFacade.localize("masterlist.dictionary.columnName");
      
      ListTypeExcelExporter.this.createRow(sheet, 0, boldStyle, attributeLabel, columnLabel, descriptionLabel);
    }
    else
    {
      String attributeLabel = LocalizationFacade.localize("masterlist.dictionary.attribute");
      String descriptionLabel = LocalizationFacade.localize("masterlist.dictionary.description");

      ListTypeExcelExporter.this.createRow(sheet, 0, boldStyle, attributeLabel, descriptionLabel);
    }

    Locale locale = Session.getCurrentLocale();

    ListColumnVisitor visitor = new AbstractListColumnVisitor()
    {
      private int rowNumber = 1;
      
      Stack<String> labels = new Stack<>();

      @Override
      public void accept(ListAttributeGroup group)
      {
        labels.push(group.getLabel());
      }

      @Override
      public void close(ListAttributeGroup group)
      {
        labels.pop();
      }

      @Override
      public void accept(ListAttribute attribute)
      {
        mdAttributes.stream().filter(p -> p.definesAttribute().equals(attribute.getName())).findAny().ifPresent((mdAttribute) -> {
          labels.push(attribute.getLabel());

          String label = StringUtils.join(labels, " - ");
          String description = mdAttribute.getDescription(locale);

          if (metadataSource.equals(ListMetadataSource.GEOSPATIAL))
          {
            String shapefileColumnName = ListTypeExcelExporter.this.generator.getColumnName(mdAttribute.definesAttribute());

            ListTypeExcelExporter.this.createRow(sheet, rowNumber++, null, label, shapefileColumnName, description);
          }
          else
          {
            ListTypeExcelExporter.this.createRow(sheet, rowNumber++, null, label, description);
          }

          labels.pop();
        });
      }
    };

    this.columns.forEach(column -> column.visit(visitor));
  }

  private String getSheetName(Workbook workbook, String key)
  {
    String label = LocalizationFacade.localize(key);
    String name = WorkbookUtil.createSafeSheetName(label);

    int i = 0;

    while (workbook.getSheet(name) != null)
    {
      name = WorkbookUtil.createSafeSheetName(label + "_" + i);

      i++;
    }

    return name;
  }

  private void createMetadataSheet(Workbook workbook)
  {
    Sheet sheet = workbook.createSheet(this.getSheetName(workbook, "masterlist.metadata"));

    Locale locale = Session.getCurrentLocale();
    MdBusinessDAOIF metadata = MdBusinessDAO.getMdBusinessDAO(ListType.CLASS);

    int rowNumber = 0;

    //this.createRow(sheet, locale, metadata, rowNumber++, ListType.DISPLAYLABEL, this.list.getDisplayLabel().getValue());
    //this.createRow(sheet, locale, metadata, rowNumber++, ListType.CODE, this.list.getCode());
    this.createRow(sheet, rowNumber++, LocalizationFacade.localize("masterlist.publishDate"), stripTime(this.version.getPublishDate()));
    this.createRow(sheet, rowNumber++, LocalizationFacade.localize("masterlist.forDate"), stripTime(this.version.getForDate()));

    if (this.metadataSource == null || this.metadataSource.equals(ListMetadataSource.LIST))
    {
      this.createRowForMetadata(sheet, locale, rowNumber++, ListTypeVersion.LISTORIGINATOR, this.version.getListOriginator());
      this.createRowForMetadata(sheet, locale, rowNumber++, ListTypeVersion.LISTLABEL, this.version.getListLabel().getValue());
      this.createRowForMetadata(sheet, locale, rowNumber++, ListTypeVersion.LISTDESCRIPTION, this.version.getListDescription().getValue());
      this.createRowForMetadata(sheet, locale, rowNumber++, ListTypeVersion.LISTPROCESS, this.version.getListProcess().getValue());
      this.createRowForMetadata(sheet, locale, rowNumber++, ListTypeVersion.LISTPROGRESS, this.version.getListProgress().getValue());
      this.createRowForMetadata(sheet, locale, rowNumber++, ListTypeVersion.LISTACCESSCONSTRAINTS, this.version.getListAccessConstraints().getValue());
      this.createRowForMetadata(sheet, locale, rowNumber++, ListTypeVersion.LISTUSECONSTRAINTS, this.version.getListUseConstraints().getValue());
      this.createRowForMetadata(sheet, locale, rowNumber++, ListTypeVersion.LISTDISCLAIMER, this.version.getListDisclaimer().getValue());

      this.createRow(sheet, locale, metadata, rowNumber++, ListType.LISTCONTACTNAME, this.version.getListContactName());
      this.createRow(sheet, locale, metadata, rowNumber++, ListType.LISTORGANIZATION, this.list.getOrganization().getDisplayLabel().getValue());
      this.createRow(sheet, locale, metadata, rowNumber++, ListType.LISTTELEPHONENUMBER, this.version.getListTelephoneNumber());
      this.createRow(sheet, locale, metadata, rowNumber++, ListType.LISTEMAIL, this.version.getListEmail());
    }
    else
    {
      this.createRowForMetadata(sheet, locale, rowNumber++, ListTypeVersion.GEOSPATIALORIGINATOR, this.version.getGeospatialOriginator());
      this.createRowForMetadata(sheet, locale, rowNumber++, ListTypeVersion.GEOSPATIALLABEL, this.version.getGeospatialLabel().getValue());
      this.createRowForMetadata(sheet, locale, rowNumber++, ListTypeVersion.GEOSPATIALDESCRIPTION, this.version.getGeospatialDescription().getValue());
      this.createRowForMetadata(sheet, locale, rowNumber++, ListTypeVersion.GEOSPATIALPROCESS, this.version.getGeospatialProcess().getValue());
      this.createRowForMetadata(sheet, locale, rowNumber++, ListTypeVersion.GEOSPATIALPROGRESS, this.version.getGeospatialProgress());
      this.createRowForMetadata(sheet, locale, rowNumber++, ListTypeVersion.GEOSPATIALACCESSCONSTRAINTS, this.version.getGeospatialAccessConstraints().getValue());
      this.createRowForMetadata(sheet, locale, rowNumber++, ListTypeVersion.GEOSPATIALUSECONSTRAINTS, this.version.getGeospatialUseConstraints().getValue());
      this.createRowForMetadata(sheet, locale, rowNumber++, ListTypeVersion.GEOSPATIALDISCLAIMER, this.version.getGeospatialDisclaimer().getValue());

      this.createRow(sheet, locale, metadata, rowNumber++, ListType.GEOSPATIALCONTACTNAME, this.version.getGeospatialContactName());
      this.createRow(sheet, locale, metadata, rowNumber++, ListType.GEOSPATIALORGANIZATION, this.list.getGeospatialOrganization());
      this.createRow(sheet, locale, metadata, rowNumber++, ListType.GEOSPATIALTELEPHONENUMBER, this.version.getGeospatialTelephoneNumber());
      this.createRow(sheet, locale, metadata, rowNumber++, ListType.GEOSPATIALEMAIL, this.version.getGeospatialEmail());
    }

    rowNumber++;
  }

  private Date stripTime(Date date)
  {
    Calendar cal = Calendar.getInstance();
    cal.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);
    cal.setTime(date);
    cal.set(Calendar.HOUR, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal.getTime();
  }

  private void createRow(Sheet sheet, Locale locale, MdBusinessDAOIF metadata, int rowNum, String attributeName, Object value)
  {
    this.createRow(sheet, locale, rowNum, metadata.definesAttribute(attributeName), value);
  }

  private void createRow(Sheet sheet, Locale locale, int rowNum, MdAttributeConcreteDAOIF mdAttribute, Object value)
  {
    String label = mdAttribute.getDisplayLabel(locale);

    this.createRow(sheet, rowNum, label, value);
  }

  private void createRowForMetadata(Sheet sheet, Locale locale, int rowNum, String attributeName, Object value)
  {
    String label = ListTypeVersion.getMetadataLabel(attributeName, locale);

    this.createRow(sheet, rowNum, label, value);
  }

  private void createRow(Sheet sheet, int rowNum, CellStyle style, String... values)
  {
    int cellNum = 0;

    Row row = sheet.createRow(rowNum);

    for (String value : values)
    {
      Cell cell = row.createCell(cellNum++);
      cell.setCellValue(value);

      if (style != null)
      {
        cell.setCellStyle(style);
      }
    }
  }

  private void createRow(Sheet sheet, int rowNum, String label, Object value)
  {
    Row row = sheet.createRow(rowNum);
    Cell labelCell = row.createCell(0);
    labelCell.setCellStyle(this.boldStyle);
    labelCell.setCellValue(label);

    if (value instanceof String)
    {
      row.createCell(1).setCellValue((String) value);
    }
    else if (value instanceof Date)
    {
      Cell valueCell = row.createCell(1);
      valueCell.setCellStyle(this.dateStyle);
      valueCell.setCellValue((Date) value);
    }
  }

  private void createDataSheet(Workbook workbook)
  {
    Sheet sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(this.getList().getDisplayLabel().getValue()));

    Row header = sheet.createRow(0);

    this.writeHeader(this.boldStyle, header);

    int rownum = 1;

    BusinessQuery query = this.version.buildQuery(this.criteria);
    query.ORDER_BY_DESC(query.aCharacter(DefaultAttribute.CODE.getName()));

    OIterator<Business> objects = query.getIterator();

    try
    {

      while (objects.hasNext())
      {
        Business object = objects.next();

        Row row = sheet.createRow(rownum++);

        this.writeRow(row, object, this.dateStyle);
      }
    }
    finally
    {
      objects.close();
    }
  }

  public void writeRow(Row row, Business object, CellStyle dateStyle)
  {
    ListColumnVisitor visitor = new AbstractListColumnVisitor()
    {
      int col = 0;

      @Override
      public void accept(ListAttribute attribute)
      {
        mdAttributes.stream().filter(p -> p.definesAttribute().equals(attribute.getName())).findAny().ifPresent((mdAttribute) -> {
          String attributeName = mdAttribute.definesAttribute();
          Object value = object.getObjectValue(attributeName);

          Cell cell = row.createCell(col++);

          if (value != null)
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
        });
      }
    };

    this.columns.forEach(column -> column.visit(visitor));

    // Write the row

  }

  public void writeHeader(CellStyle boldStyle, Row header)
  {
    // int col = 0;
    Locale locale = Session.getCurrentLocale();

    ListColumnVisitor visitor = new AbstractListColumnVisitor()
    {
      int           col    = 0;

      Stack<String> labels = new Stack<>();

      @Override
      public void accept(ListAttributeGroup group)
      {
        labels.push(group.getLabel());
      }

      @Override
      public void close(ListAttributeGroup group)
      {
        labels.pop();
      }

      @Override
      public void accept(ListAttribute attribute)
      {
        mdAttributes.stream().filter(p -> p.definesAttribute().equals(attribute.getName())).findAny().ifPresent((mdAttribute) -> {
          labels.push(attribute.getLabel());

          Cell cell = header.createCell(col++);
          cell.setCellStyle(boldStyle);
          cell.setCellValue(StringUtils.join(labels, " - "));
          
          labels.pop();
        });
      }
    };

    this.columns.forEach(column -> column.visit(visitor));

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
