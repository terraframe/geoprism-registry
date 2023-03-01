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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.localization.LocalizationFacade;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.view.HistoricalRow;
import net.geoprism.registry.view.Page;

public class HistoricalReportExcelExporter
{
  private static Logger       logger = LoggerFactory.getLogger(GeoObjectExcelExporter.class);

  private ServerGeoObjectType type;

  private Date                startDate;

  private Date                endDate;

  private CellStyle           boldStyle;

  private SimpleDateFormat    format;

  public HistoricalReportExcelExporter(ServerGeoObjectType type, Date startDate, Date endDate)
  {
    this.type = type;
    this.startDate = startDate;
    this.endDate = endDate;

    this.format = new SimpleDateFormat("yyyy-MM-dd");
    this.format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);
  }

  public ServerGeoObjectType getType()
  {
    return type;
  }

  public void setType(ServerGeoObjectType type)
  {
    this.type = type;
  }

  public Date getStartDate()
  {
    return startDate;
  }

  public void setStartDate(Date startDate)
  {
    this.startDate = startDate;
  }

  public Date getEndDate()
  {
    return endDate;
  }

  public void setEndDate(Date endDate)
  {
    this.endDate = endDate;
  }

  public Workbook createWorkbook() throws IOException
  {
    LocaleUtil.setUserTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    Workbook workbook = new XSSFWorkbook();

    Font font = workbook.createFont();
    font.setBold(true);

    this.boldStyle = workbook.createCellStyle();
    this.boldStyle.setFont(font);

    this.createDataSheet(workbook);

    return workbook;
  }

  private void createDataSheet(Workbook workbook)
  {
    Sheet sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName("Historical Report"));

    int rowNum = 0;

    this.writeHeader(sheet.createRow(rowNum++));

    Page<HistoricalRow> page = HistoricalRow.getHistoricalReport(type, startDate, endDate, 1000, 1);

    while (page.getResults().size() > 0)
    {
      List<HistoricalRow> results = page.getResults();

      for (HistoricalRow result : results)
      {
        this.writeRow(sheet.createRow(rowNum++), result);
      }

      page = HistoricalRow.getHistoricalReport(type, startDate, endDate, page.getPageSize(), page.getPageNumber() + 1);
    }
  }

  public void writeRow(Row row, HistoricalRow result)
  {
    int col = 0;

    row.createCell(col++).setCellValue(result.getEventId());
    row.createCell(col++).setCellValue(this.format.format(result.getEventDate()));
    row.createCell(col++).setCellValue(result.getLocalizedEventType());
    row.createCell(col++).setCellValue(result.getDescription().getValue());
    row.createCell(col++).setCellValue(result.getBeforeType());
    row.createCell(col++).setCellValue(result.getBeforeCode());
    row.createCell(col++).setCellValue(result.getBeforeLabel().getValue());
    row.createCell(col++).setCellValue(result.getAfterType());
    row.createCell(col++).setCellValue(result.getAfterCode());
    row.createCell(col++).setCellValue(result.getAfterLabel().getValue());

  }

  public void writeHeader(Row header)
  {
    int col = 0;

    addHeaderCell(header, col++, HistoricalRow.EVENT_ID);
    addHeaderCell(header, col++, HistoricalRow.EVENT_DATE);
    addHeaderCell(header, col++, HistoricalRow.EVENT_TYPE);
    addHeaderCell(header, col++, HistoricalRow.DESCRIPTION);
    addHeaderCell(header, col++, HistoricalRow.BEFORE_TYPE);
    addHeaderCell(header, col++, HistoricalRow.BEFORE_CODE);
    addHeaderCell(header, col++, HistoricalRow.BEFORE_LABEL);
    addHeaderCell(header, col++, HistoricalRow.AFTER_TYPE);
    addHeaderCell(header, col++, HistoricalRow.AFTER_CODE);
    addHeaderCell(header, col++, HistoricalRow.AFTER_LABEL);
  }

  public void addHeaderCell(Row header, int col, String code)
  {
    Cell cell = header.createCell(col);
    cell.setCellStyle(boldStyle);
    cell.setCellValue(LocalizationFacade.localize("historical.row." + code));
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
