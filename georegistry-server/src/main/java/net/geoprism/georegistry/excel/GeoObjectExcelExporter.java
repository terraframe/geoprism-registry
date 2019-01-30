package net.geoprism.georegistry.excel;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.geoprism.georegistry.io.GeoObjectUtil;

public class GeoObjectExcelExporter
{
  private static Logger       logger = LoggerFactory.getLogger(GeoObjectExcelExporter.class);

  private GeoObjectType       type;

  private Iterable<GeoObject> objects;

  public GeoObjectExcelExporter(GeoObjectType type, Iterable<GeoObject> objects)
  {
    this.type = type;
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

  public void setObjects(Iterable<GeoObject> objects)
  {
    this.objects = objects;
  }

  public Workbook createWorkbook() throws IOException
  {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(this.type.getLocalizedLabel()));

    Font font = workbook.createFont();
    font.setBold(true);

    CellStyle boldStyle = workbook.createCellStyle();
    boldStyle.setFont(font);

    Row header = sheet.createRow(0);

    Map<String, AttributeType> attributes = this.type.getAttributeMap();
    Set<Entry<String, AttributeType>> entries = attributes.entrySet();

    int col = 0;

    for (Entry<String, AttributeType> entry : entries)
    {
      AttributeType attribute = entry.getValue();

      Cell cell = header.createCell(col++);
      cell.setCellStyle(boldStyle);
      cell.setCellValue(attribute.getLocalizedLabel());
    }

    int rownum = 1;

    for (GeoObject object : this.objects)
    {
      col = 0;

      Row row = sheet.createRow(rownum++);

      for (Entry<String, AttributeType> entry : entries)
      {
        AttributeType attribute = entry.getValue();
        String name = attribute.getName();
        Object value = object.getValue(name);

        Cell cell = row.createCell(col++);

        if (value != null)
        {
          if (attribute instanceof AttributeTermType)
          {
            cell.setCellValue(GeoObjectUtil.convertToTermString((AttributeTermType) attribute, value));
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

    return workbook;
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
