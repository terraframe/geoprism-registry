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
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
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

  private CellStyle                                boldStyle;

  private CellStyle                                dateStyle;

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

    CreationHelper createHelper = workbook.getCreationHelper();
    Font font = workbook.createFont();
    font.setBold(true);

    this.boldStyle = workbook.createCellStyle();
    this.boldStyle.setFont(font);

    this.dateStyle = workbook.createCellStyle();
    this.dateStyle.setDataFormat(createHelper.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat(14)));

    this.createDataSheet(workbook);
    this.createMetadataSheet(workbook);
    this.createDataDictionarySheet(workbook);

    return workbook;
  }

  private void createDataDictionarySheet(Workbook workbook)
  {
    Sheet sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(LocalizationFacade.getFromBundles("masterlist.data.dictionary")));

    Locale locale = Session.getCurrentLocale();

    int rowNumber = 0;

    for (MdAttributeConcreteDAOIF mdAttribute : mdAttributes)
    {
      this.createRow(sheet, locale, rowNumber++, mdAttribute, mdAttribute.getDescription(locale));
    }
  }

  private void createMetadataSheet(Workbook workbook)
  {
    Sheet sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(LocalizationFacade.getFromBundles("masterlist.metadata")));

    Locale locale = Session.getCurrentLocale();
    MdBusinessDAOIF metadata = MdBusinessDAO.getMdBusinessDAO(MasterList.CLASS);

    int rowNumber = 0;

    this.createRow(sheet, locale, metadata, rowNumber++, MasterList.DISPLAYLABEL, this.list.getDisplayLabel().getValue());
    this.createRow(sheet, locale, metadata, rowNumber++, MasterList.CODE, this.list.getCode());
    this.createRow(sheet, locale, metadata, rowNumber++, MasterList.REPRESENTATIVITYDATE, this.list.getRepresentativityDate());
    this.createRow(sheet, locale, metadata, rowNumber++, MasterList.PUBLISHDATE, this.list.getPublishDate());
    this.createRow(sheet, locale, metadata, rowNumber++, MasterList.LISTABSTRACT, this.list.getListAbstract());
    this.createRow(sheet, locale, metadata, rowNumber++, MasterList.PROCESS, this.list.getProcess());
    this.createRow(sheet, locale, metadata, rowNumber++, MasterList.PROGRESS, this.list.getProgress());
    this.createRow(sheet, locale, metadata, rowNumber++, MasterList.ACCESSCONSTRAINTS, this.list.getAccessConstraints());
    this.createRow(sheet, locale, metadata, rowNumber++, MasterList.USECONSTRAINTS, this.list.getUseConstraints());
    this.createRow(sheet, locale, metadata, rowNumber++, MasterList.ACKNOWLEDGEMENTS, this.list.getAcknowledgements());
    this.createRow(sheet, locale, metadata, rowNumber++, MasterList.DISCLAIMER, this.list.getDisclaimer());

    rowNumber++;

    this.createRow(sheet, locale, metadata, rowNumber++, MasterList.CONTACTNAME, this.list.getContactName());
    this.createRow(sheet, locale, metadata, rowNumber++, MasterList.ORGANIZATION, this.list.getOrganization());
    this.createRow(sheet, locale, metadata, rowNumber++, MasterList.TELEPHONENUMBER, this.list.getTelephoneNumber());
    this.createRow(sheet, locale, metadata, rowNumber++, MasterList.EMAIL, this.list.getEmail());
  }

  private void createRow(Sheet sheet, Locale locale, MdBusinessDAOIF metadata, int rowNum, String attributeName, Object value)
  {
    this.createRow(sheet, locale, rowNum, metadata.definesAttribute(attributeName), value);
  }

  private void createRow(Sheet sheet, Locale locale, int rowNum, MdAttributeConcreteDAOIF mdAttribute, Object value)
  {
    Row row = sheet.createRow(rowNum);
    Cell labelCell = row.createCell(0);
    labelCell.setCellStyle(this.boldStyle);
    labelCell.setCellValue(mdAttribute.getDisplayLabel(locale));

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

    // MdAttributeGeometryDAOIF geometryAttribute = (MdAttributeGeometryDAOIF)
    // this.mdBusiness.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);
    //
    // boolean includeCoordinates = ( geometryAttribute instanceof
    // MdAttributePointDAOIF );

    this.writeHeader(this.boldStyle, header);

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
