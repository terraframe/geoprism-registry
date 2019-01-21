package net.geoprism.georegistry.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.session.Request;

import net.geoprism.georegistry.GeoObjectQuery;
import net.geoprism.georegistry.excel.GeoObjectExcelExporter;
import net.geoprism.georegistry.io.GeoObjectConfiguration;
import net.geoprism.georegistry.testframework.USATestData;

public class ExcelServiceTest
{
  protected USATestData     tutil;

  protected ClientRequestIF adminCR;

  @Before
  public void setUp()
  {
    this.tutil = USATestData.newTestData(GeometryType.POINT, false);

    this.adminCR = tutil.adminClientRequest;
  }

  @After
  public void tearDown()
  {
    tutil.cleanUp();
  }

  @Test
  public void testGetAttributeInformation()
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();
    JsonObject result = service.getExcelConfiguration(this.adminCR.getSessionId(), tutil.STATE.getCode(), "test-spreadsheet.xlsx", istream);

    System.out.println(result.toString());

    Assert.assertNotNull(result.getAsJsonObject("type"));

    JsonObject sheet = result.getAsJsonObject("sheet");

    Assert.assertNotNull(sheet);
    Assert.assertEquals("Objects", sheet.get("name").getAsString());

    JsonObject attributes = sheet.get("attributes").getAsJsonObject();

    Assert.assertNotNull(attributes);

    JsonArray fields = attributes.get(GeoObjectConfiguration.TEXT).getAsJsonArray();

    Assert.assertEquals(2, fields.size());
    Assert.assertEquals("Status", fields.get(0).getAsString());

    Assert.assertEquals(2, attributes.get(GeoObjectConfiguration.NUMERIC).getAsJsonArray().size());
    Assert.assertEquals(0, attributes.get(AttributeBooleanType.TYPE).getAsJsonArray().size());
    Assert.assertEquals(0, attributes.get(AttributeDateType.TYPE).getAsJsonArray().size());
  }

  @Test
  @Request
  public void testImportSpreadsheet()
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JsonObject json = this.getTestConfiguration(istream, service);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), true);

    service.importExcelFile(this.adminCR.getSessionId(), configuration.toJson().toString());

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(this.tutil.adminClientRequest.getSessionId(), "Test", tutil.STATE.getCode());

    Assert.assertNotNull(object);
    Assert.assertNotNull(object.getGeometry());
    Assert.assertEquals("Test", object.getValue(GeoObject.LOCALIZED_DISPLAY_LABEL));
  }

  @Test
  @Request
  public void testCreateWorkbook() throws IOException
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JsonObject json = this.getTestConfiguration(istream, service);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), true);

    service.importExcelFile(this.adminCR.getSessionId(), configuration.toJson().toString());

    GeoObjectType type = tutil.STATE.getGeoObjectType(GeometryType.POINT);

    List<GeoObject> objects = new GeoObjectQuery(type, tutil.STATE.getUniversal()).getIterator().getAll();

    Assert.assertEquals(1, objects.size());

    GeoObjectExcelExporter exporter = new GeoObjectExcelExporter(type, objects);
    Workbook workbook = exporter.createWorkbook();

    Assert.assertEquals(1, workbook.getNumberOfSheets());

    Sheet sheet = workbook.getSheetAt(0);

    Assert.assertEquals(WorkbookUtil.createSafeSheetName(type.getLocalizedLabel()), sheet.getSheetName());
  }

  @Test
  @Request
  public void testExport() throws IOException
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JsonObject json = this.getTestConfiguration(istream, service);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), true);

    service.importExcelFile(this.adminCR.getSessionId(), configuration.toJson().toString());

    GeoObjectType type = tutil.STATE.getGeoObjectType(GeometryType.POINT);

    List<GeoObject> objects = new GeoObjectQuery(type, tutil.STATE.getUniversal()).getIterator().getAll();

    Assert.assertEquals(1, objects.size());

    GeoObjectExcelExporter exporter = new GeoObjectExcelExporter(type, objects);
    InputStream export = exporter.export();

    Assert.assertNotNull(export);

    IOUtils.copy(export, new NullOutputStream());
  }

  private JsonObject getTestConfiguration(InputStream istream, ExcelService service)
  {
    JsonObject result = service.getExcelConfiguration(this.adminCR.getSessionId(), tutil.STATE.getCode(), "test-spreadsheet.xlsx", istream);
    JsonObject type = result.getAsJsonObject("type");
    JsonArray attributes = type.get("attributes").getAsJsonArray();

    for (int i = 0; i < attributes.size(); i++)
    {
      JsonObject attribute = attributes.get(i).getAsJsonObject();

      String attributeName = attribute.get("name").getAsString();

      if (attributeName.equals(GeoObject.LOCALIZED_DISPLAY_LABEL))
      {
        attribute.addProperty("target", "Status");
      }
      else if (attributeName.equals(GeoObject.CODE))
      {
        attribute.addProperty("target", "Code");
      }
      else if (attributeName.equals(GeoObjectConfiguration.LATITUDE))
      {
        attribute.addProperty("target", "Latitude");
      }
      else if (attributeName.equals(GeoObjectConfiguration.LONGITUDE))
      {
        attribute.addProperty("target", "Longitude");
      }
    }

    return result;
  }
}
