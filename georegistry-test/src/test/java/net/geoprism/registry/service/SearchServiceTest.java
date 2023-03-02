/**
 *
 */
package net.geoprism.registry.service;

import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.runwaysdk.build.domain.SearchTablePatch;
import com.runwaysdk.session.Request;

import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.test.USATestData;

public class SearchServiceTest
{
  private static USATestData testData;

  @BeforeClass
  public static void setUpClass()
  {
    testData = USATestData.newTestData();
    testData.setUpMetadata();
  }

  @AfterClass
  public static void cleanUpClass()
  {
    if (testData != null)
    {
      testData.tearDownMetadata();
    }
  }

  @Before
  public void setUp()
  {
    testData.setUpInstanceData();

    testData.logIn(USATestData.USER_NPS_RA);
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    testData.tearDownInstanceData();
  }

  @Test
  @Request
  public void testSearchTable()
  {
    SearchService service = new SearchService();
    service.clear();
    service.deleteSearchTable();
    service.createSearchTable();

    new SearchTablePatch().createRecords(service);

    Date date = USATestData.DEFAULT_OVER_TIME_DATE;

    List<ServerGeoObjectIF> results = service.search(USATestData.CO_D_ONE.getDisplayLabel(), date, 10L);

    Assert.assertEquals(1, results.size());

    ServerGeoObjectIF result = results.get(0);

    Assert.assertEquals(result.getCode(), USATestData.CO_D_ONE.getCode());

    Assert.assertEquals(3, service.search(USATestData.TEST_DATA_KEY + "ColoradoDistrict", date, 10L).size());
    Assert.assertEquals(1, service.search(USATestData.TEST_DATA_KEY, date, 1L).size());
  }

  @Test
  @Request
  public void testWhitespace()
  {
    SearchService service = new SearchService();
    service.clear();
    service.deleteSearchTable();
    service.createSearchTable();

    new SearchTablePatch().createRecords(service);

    Date date = USATestData.DEFAULT_OVER_TIME_DATE;

    List<ServerGeoObjectIF> results = service.search(USATestData.CO_D_ONE.getDisplayLabel() + " ", date, 10L);

    Assert.assertEquals(1, results.size());

    ServerGeoObjectIF result = results.get(0);

    Assert.assertEquals(result.getCode(), USATestData.CO_D_ONE.getCode());

    Assert.assertEquals(3, service.search(USATestData.TEST_DATA_KEY + "ColoradoDistrict", date, 10L).size());
    Assert.assertEquals(1, service.search(USATestData.TEST_DATA_KEY, date, 1L).size());
  }

  @Test
  @Request
  public void testSpecialCharacters()
  {
    SearchService service = new SearchService();
    service.clear();
    service.deleteSearchTable();
    service.createSearchTable();

    new SearchTablePatch().createRecords(service);

    Date date = USATestData.DEFAULT_OVER_TIME_DATE;

    List<ServerGeoObjectIF> results = service.search(":~", date, 10L);

    Assert.assertEquals(0, results.size());
  }

  @Test
  @Request
  public void testSearchNull()
  {
    SearchService service = new SearchService();
    service.clear();
    service.deleteSearchTable();
    service.createSearchTable();

    new SearchTablePatch().createRecords(service);

    Date date = USATestData.DEFAULT_OVER_TIME_DATE;

    List<ServerGeoObjectIF> results = service.search(null, date, 10L);

    Assert.assertTrue(results.size() > 0);
  }
  
  @Test
  @Request
  public void testSearchLabels()
  {
    SearchService service = new SearchService();
    service.clear();
    service.deleteSearchTable();
    service.createSearchTable();

    new SearchTablePatch().createRecords(service);

    Date date = USATestData.DEFAULT_OVER_TIME_DATE;

    List<JsonObject> results = service.labels(USATestData.CO_D_ONE.getDisplayLabel(), date, 10L);

    Assert.assertEquals(1, results.size());

    JsonObject result = results.get(0);

    Assert.assertEquals(result.get("code").getAsString(), USATestData.CO_D_ONE.getCode());
  }


}
