package net.geoprism.dhis2.dhis2adapter;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import net.geoprism.dhis2.dhis2adapter.response.ImportReportResponse;
import net.geoprism.dhis2.dhis2adapter.response.model.ErrorReport;
import net.geoprism.dhis2.dhis2adapter.response.model.ImportReport;
import net.geoprism.dhis2.dhis2adapter.response.model.ObjectReport;
import net.geoprism.dhis2.dhis2adapter.response.model.TypeReport;

public class ResponseErrorTest
{
  @Test
  public void testImportReport() throws Throwable
  {
    String json = IOUtils.toString(ResponseErrorTest.class.getResourceAsStream("/default/error/import-report.json"), "UTF-8");
    
    ImportReportResponse resp = new ImportReportResponse(json, 409);
    
    // DHIS2Response super type
    Assert.assertEquals(409, resp.getStatusCode());
    Assert.assertFalse(resp.isSuccess());
    Assert.assertTrue(resp.hasMessage());
    Assert.assertEquals("There are duplicate translation record for property `SHORT_NAME` and locale `lo_LA`", resp.getMessage());
    
    Assert.assertTrue(resp.getResponse() != null && resp.getResponse().length() > 0);
    
    Assert.assertTrue(resp.hasErrorReports());
    
    List<ErrorReport> reports = resp.getErrorReports();
    
    Assert.assertEquals("There are duplicate translation record for property `SHORT_NAME` and locale `lo_LA`", reports.get(0).getMessage());
    Assert.assertEquals("There are duplicate translation record for property `NAME` and locale `lo_LA`", reports.get(1).getMessage());
    Assert.assertEquals("There are duplicate translation record for property `NAME` and locale `lo`", reports.get(2).getMessage());
  }
  
  @Test
  public void testMissingOpeningDate238() throws Throwable
  {
    missingOpeningDate("/2.38/error/metadata-missing-openingdate.json");
  }
  
  @Test
  public void testMissingOpeningDate237() throws Throwable
  {
    missingOpeningDate("/2.37/error/metadata-missing-openingdate.json");
  }
  
  @Test
  public void testMissingOpeningDate236() throws Throwable
  {
    missingOpeningDate("/2.36/error/metadata-missing-openingdate.json");
  }

  private void missingOpeningDate(final String resourcePath) throws IOException
  {
    String json = IOUtils.toString(ResponseErrorTest.class.getResourceAsStream(resourcePath), "UTF-8");
    
    ImportReportResponse resp = new ImportReportResponse(json, 409);
    
    // DHIS2Response super type
    Assert.assertEquals(409, resp.getStatusCode());
    Assert.assertFalse(resp.isSuccess());
    Assert.assertTrue(resp.hasMessage());
    Assert.assertEquals("Missing required property `openingDate`.", resp.getMessage());
    
    Assert.assertTrue(resp.getResponse() != null && resp.getResponse().length() > 0);
    
    Assert.assertTrue(resp.hasErrorReports());
    
    List<ErrorReport> reports = resp.getErrorReports();
    
    Assert.assertEquals("Missing required property `openingDate`.", reports.get(0).getMessage());
    
    Assert.assertTrue(resp.hasTypeReports());
    
    ImportReport ir = resp.getImportReport();
    
    Assert.assertTrue(ir.hasTypeReports());
    
    Assert.assertEquals(1, resp.getTypeReports().size());
    Assert.assertEquals(1, ir.getTypeReports().size());
    
    TypeReport tr = ir.getTypeReports().get(0);
    
    List<ObjectReport> objectReports = tr.getObjectReports();
    
    Assert.assertEquals(1, objectReports.size());
    
    Assert.assertTrue(objectReports.get(0).hasErrorReports());
    
    List<ErrorReport> errorReports = objectReports.get(0).getErrorReports();
    
    Assert.assertEquals(1, errorReports.size());
    
    Assert.assertEquals("Missing required property `openingDate`.", errorReports.get(0).getMessage());
  }
}
