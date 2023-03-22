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
package net.geoprism.registry.etl.upload;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.util.CellReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.RunwayException;
import com.runwaysdk.session.Session;

import net.geoprism.data.etl.excel.ColumnType;
import net.geoprism.data.etl.excel.ExcelDataFormatter;
import net.geoprism.data.etl.excel.ExcelFormulaException;
import net.geoprism.data.etl.excel.ExcelObjectException;
import net.geoprism.data.etl.excel.ExcelValueException;
import net.geoprism.data.etl.excel.InvalidHeaderRowException;
import net.geoprism.data.etl.excel.SheetHandler;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.excel.MapFeatureRow;

public class ExcelContentHandler implements SheetHandler
{
  private static Logger          logger = LoggerFactory.getLogger(ExcelContentHandler.class);

  /**
   * Handler which handles the view object once they have been created.
   */
  private ObjectImporterIF       objectImporter;

  /**
   * Column index-Column Name Map for the current sheet
   */
  protected Map<Integer, String> map;

  /**
   * Current row number
   */
  protected long                 rowNum;

  /**
   * Current error row number
   */
  private long                   errorNum;

  /**
   * Current view
   */
  private Map<String, Object>    row;

  /**
   * Format used for parsing and formatting dateTime fields
   */
  private DateFormat             dateTimeFormat;

  boolean                        isFirstSheet;

  ImportStage                    stage;

  protected Long                 startIndex;

  public ExcelContentHandler(ObjectImporterIF objectImporter, ImportStage stage, Long startIndex)
  {
    this.objectImporter = objectImporter;

    this.stage = stage;

    this.startIndex = startIndex;

    this.isFirstSheet = true;

    this.map = new HashMap<Integer, String>();

    this.dateTimeFormat = new SimpleDateFormat(ExcelDataFormatter.DATE_TIME_FORMAT);
    this.dateTimeFormat.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);
  }

  @Override
  public void startSheet(String sheetName)
  {
    this.errorNum = 1;
  }

  @Override
  public void endSheet()
  {
    this.isFirstSheet = false;
  }

  @Override
  public void startRow(int rowNum)
  {
    if (this.isFirstSheet)
    {
      this.rowNum = rowNum;
      this.row = new HashMap<String, Object>();
    }
  }

  @Override
  public void endRow()
  {
    if (this.isFirstSheet)
    {
      try
      {
        if (this.rowNum != 0 && rowNum >= this.startIndex)
        {
          if (stage.equals(ImportStage.VALIDATE))
          {
            this.objectImporter.validateRow(new MapFeatureRow(this.row, (this.rowNum + 1)));
          }
          else
          {
            this.objectImporter.importRow(new MapFeatureRow(this.row, (this.rowNum + 1)));
          }

          this.row = new HashMap<String, Object>();
        }
      }
      catch (Exception e)
      {
        // Wrap all exceptions with information about the cell and row
        ExcelObjectException exception = new ExcelObjectException(e);
        exception.setRow(Long.valueOf(this.rowNum));
        exception.setMsg(RunwayException.localizeThrowable(e, Session.getCurrentLocale()));

        throw exception;
      }
    }
  }

  protected String setColumnName(String cellReference, String columnName)
  {
    CellReference reference = new CellReference(cellReference);
    Integer column = Integer.valueOf(reference.getCol());

    return this.map.put(column, columnName);
  }

  protected String getColumnName(String cellReference)
  {
    CellReference reference = new CellReference(cellReference);
    Integer column = Integer.valueOf(reference.getCol());

    return this.map.get(column);
  }

  @Override
  public void cell(String cellReference, String contentValue, String formattedValue, ColumnType cellType)
  {
    if (this.isFirstSheet)
    {
      try
      {
        if (cellType.equals(ColumnType.FORMULA))
        {
          throw new ExcelFormulaException();
        }

        if (this.rowNum == 0)
        {
          if (! ( cellType.equals(ColumnType.TEXT) || cellType.equals(ColumnType.INLINE_STRING) ))
          {
            throw new InvalidHeaderRowException();
          }

          this.setColumnName(cellReference, formattedValue);
        }
        else if (this.row != null)
        {
          String columnName = this.getColumnName(cellReference);

          if (cellType.equals(ColumnType.DATE))
          {
            this.row.put(columnName, this.dateTimeFormat.parse(contentValue));
          }
          else if (cellType.equals(ColumnType.BOOLEAN))
          {
            this.row.put(columnName, Boolean.valueOf(contentValue.equals("TRUE")));
          }
          else
          {
            this.row.put(columnName, formattedValue);
          }
        }
      }
      catch (Exception e)
      {
        logger.error("An error occurred while importing cell [" + cellReference + "].", e);

        // Wrap all exceptions with information about the cell and row
        ExcelValueException exception = new ExcelValueException();
        exception.setCell(cellReference);
        exception.setMsg(RunwayException.localizeThrowable(e, Session.getCurrentLocale()));

        throw exception;
      }
    }
  }

  @Override
  public void headerFooter(String text, boolean isHeader, String tagName)
  {
  }

  public long getTotalRows()
  {
    return rowNum;
  }

  public long getNumberOfErrors()
  {
    return ( this.errorNum - 1 );
  }

  @Override
  public void setDatasetProperty(String dataset)
  {
    // Do nothing
  }
}
