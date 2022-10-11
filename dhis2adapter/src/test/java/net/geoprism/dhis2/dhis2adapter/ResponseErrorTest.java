package net.geoprism.dhis2.dhis2adapter;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import net.geoprism.dhis2.dhis2adapter.response.ImportReportResponse;
import net.geoprism.dhis2.dhis2adapter.response.MetadataImportResponse;
import net.geoprism.dhis2.dhis2adapter.response.model.ErrorReport;

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
}
