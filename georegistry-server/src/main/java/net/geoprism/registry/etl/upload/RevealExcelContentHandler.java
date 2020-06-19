/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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

import org.apache.poi.ss.util.CellReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wololo.jts2geojson.GeoJSONReader;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.ExceptionUtil;
import net.geoprism.data.etl.ColumnType;
import net.geoprism.data.etl.excel.ExcelValueException;
import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.io.InvalidGeometryException;

public class RevealExcelContentHandler extends ExcelContentHandler
{
  private static Logger        logger = LoggerFactory.getLogger(RevealExcelContentHandler.class);
  
  private StringBuilder geometry;
  
  private String geometryType;
  
  private Integer geometryColumnStart;
  
  private Integer colNum;
  
  private Boolean hasGeometry = true;
  
  private String revealGeometryColumn;

  public RevealExcelContentHandler(ObjectImporterIF objectImporter, ImportStage stage, Long startIndex, String revealGeometryColumn)
  {
    super(objectImporter, stage, startIndex);
    
    this.revealGeometryColumn = revealGeometryColumn;
  }

  @Override
  public void startRow(int rowNum)
  {
    super.startRow(rowNum);
    
    if (this.isFirstSheet)
    {
      this.geometry = new StringBuilder();
    }
  }

  @Override
  public void endRow()
  {
    super.endRow();
    
    if (this.rowNum == 0)
    {
      if (this.revealGeometryColumn == null || this.revealGeometryColumn.length() == 0 || !this.map.containsValue(this.revealGeometryColumn))
      {
        this.hasGeometry = false;
      }
      
      this.geometryColumnStart = colNum;
      
      for (Integer itCol : this.map.keySet())
      {
        String attrName = this.map.get(itCol);
        
        if (attrName.equals(this.revealGeometryColumn))
        {
          if (!this.geometryColumnStart.equals(itCol))
          {
            throw new InvalidGeometryException("Geometry column must be at the end of the spreadsheet.");
          }
          
          break;
        }
      }
      
      if (!this.map.get(this.colNum - 1).equals("type"))
      {
        throw new InvalidGeometryException("Expected column header 'type' at column index [" + String.valueOf(this.colNum - 1) + "].");
      }
    }
  }

  @Override
  public void cell(String cellReference, String contentValue, String formattedValue, ColumnType cellType)
  {
    super.cell(cellReference, contentValue, formattedValue, cellType);
    
    final CellReference reference = new CellReference(cellReference);
    this.colNum = new Integer(reference.getCol());
    
    if (this.isFirstSheet)
    {
      try
      {
        if (this.hasGeometry && this.geometry != null && this.geometryColumnStart != null)
        {
          if (this.colNum >= this.geometryColumnStart)
          {
            if (this.colNum == geometryColumnStart && !contentValue.startsWith("["))
            {
              this.hasGeometry = false;
            }
            
            if (cellType.equals(ColumnType.TEXT) || cellType.equals(ColumnType.INLINE_STRING))
            {
              geometry.append(contentValue);
              geometry.append(",");
            }
          }
          else if (this.colNum == this.geometryColumnStart - 1) // Geometry Type
          {
            this.geometryType = contentValue;
          }
        }
      }
      catch (Exception e)
      {
        logger.error("An error occurred while importing cell [" + cellReference + "].", e);

        // Wrap all exceptions with information about the cell and row
        ExcelValueException exception = new ExcelValueException();
        exception.setCell(cellReference);
        exception.setMsg(ExceptionUtil.getLocalizedException(e));

        throw exception;
      }
    }
  }
  
  public Geometry getGeometry()
  {
    if (!this.hasGeometry)
    {
      return null;
    }
    
    try
    {
      String sCoordinates = this.geometry.toString();
      
      if (sCoordinates.endsWith(","))
      {
        sCoordinates = sCoordinates.substring(0, sCoordinates.length()-1);
      }
      
      JsonArray joCoordinates = new JsonParser().parse(sCoordinates).getAsJsonArray();
      
      // TODO : Not sure if we want to keep this polygon -> multipolygon conversion code
      if (this.geometryType.toUpperCase().equals("POLYGON"))
      {
        this.geometryType = "MultiPolygon";
        
        JsonArray joCoordinates2 = new JsonArray();
        joCoordinates2.add(joCoordinates);
        joCoordinates = joCoordinates2;
      }
      
      JsonObject joGeometry = new JsonObject();
      {
        joGeometry.add("coordinates", joCoordinates);
        
        joGeometry.addProperty("type", this.geometryType);
      }
      
      GeoJSONReader reader = new GeoJSONReader();
      Geometry jtsGeom = reader.read(joGeometry.toString());
      
      return jtsGeom;
    }
    catch (Throwable t)
    {
      throw new InvalidGeometryException(t);
    }
  }
}
