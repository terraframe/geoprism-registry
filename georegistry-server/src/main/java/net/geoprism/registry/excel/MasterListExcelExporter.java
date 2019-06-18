package net.geoprism.registry.excel;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.gis.dataaccess.MdAttributePointDAOIF;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Session;
import com.vividsolutions.jts.geom.Point;

import net.geoprism.localization.LocalizationFacade;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.io.GeoObjectConfiguration;

public class MasterListExcelExporter
{
  private static Logger                            logger = LoggerFactory.getLogger(GeoObjectExcelExporter.class);

  private MasterList                               list;

  private MdBusinessDAOIF                          mdBusiness;

  private List<? extends MdAttributeConcreteDAOIF> mdAttributes;

  private String                                   filterJson;

  public MasterListExcelExporter(MasterList list, MdBusinessDAOIF mdBusiness, List<? extends MdAttributeConcreteDAOIF> mdAttributes, String filterJson)
  {
    this.list = list;
    this.mdBusiness = mdBusiness;
    this.mdAttributes = mdAttributes;
    this.filterJson = filterJson;
  }

  public MasterList getList()
  {
    return list;
  }

  public void setList(MasterList list)
  {
    this.list = list;
  }

  public Workbook createWorkbook() throws IOException
  {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(this.getList().getDisplayLabel().getValue()));

    CreationHelper createHelper = workbook.getCreationHelper();
    Font font = workbook.createFont();
    font.setBold(true);

    CellStyle boldStyle = workbook.createCellStyle();
    boldStyle.setFont(font);

    CellStyle dateStyle = workbook.createCellStyle();
    dateStyle.setDataFormat(createHelper.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat(14)));

    Row header = sheet.createRow(0);

    // MdAttributeGeometryDAOIF geometryAttribute = (MdAttributeGeometryDAOIF)
    // this.mdBusiness.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);
    //
    // boolean includeCoordinates = ( geometryAttribute instanceof
    // MdAttributePointDAOIF );

    this.writeHeader(boldStyle, header);

    int rownum = 1;

    BusinessQuery query = this.list.buildQuery(this.filterJson);
    query.ORDER_BY_DESC(query.aCharacter(DefaultAttribute.CODE.getName()));

    OIterator<Business> objects = query.getIterator();

    try
    {

      while (objects.hasNext())
      {

        Business object = objects.next();

        Row row = sheet.createRow(rownum++);

        this.writeRow(row, object, dateStyle);
      }
    }
    finally
    {
      objects.close();
    }

    return workbook;
  }

  public void writeRow(Row row, Business object, CellStyle dateStyle)
  {
    int col = 0;
    // Write the row

    MdAttributeConcreteDAOIF mdGeometry = mdBusiness.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);

    if (mdGeometry instanceof MdAttributePointDAOIF)
    {
      Point point = (Point) object.getObjectValue(mdGeometry.definesAttribute());

      if (point != null)
      {
        row.createCell(col++).setCellValue(point.getX());
        row.createCell(col++).setCellValue(point.getY());
      }
    }

    for (MdAttributeConcreteDAOIF mdAttribute : mdAttributes)
    {
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
    }
  }

  public void writeHeader(CellStyle boldStyle, Row header)
  {
    int col = 0;
    Locale locale = Session.getCurrentLocale();

    MdAttributeConcreteDAOIF mdGeometry = mdBusiness.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);

    if (mdGeometry instanceof MdAttributePointDAOIF)
    {
      Cell longitude = header.createCell(col++);
      longitude.setCellStyle(boldStyle);
      longitude.setCellValue(LocalizationFacade.getFromBundles(GeoObjectConfiguration.LONGITUDE_KEY));

      Cell latitude = header.createCell(col++);
      latitude.setCellStyle(boldStyle);
      latitude.setCellValue(LocalizationFacade.getFromBundles(GeoObjectConfiguration.LATITUDE_KEY));
    }

    for (MdAttributeConcreteDAOIF mdAttribute : this.mdAttributes)
    {
      Cell cell = header.createCell(col++);
      cell.setCellStyle(boldStyle);
      cell.setCellValue(mdAttribute.getDisplayLabel(locale));
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
