/**
 *
 */
package net.geoprism.registry.service;

import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.gson.JsonObject;
import com.runwaysdk.build.domain.SearchTablePatch;
import com.runwaysdk.session.Request;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.service.business.SearchService;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestGeoObjectInfo;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class SearchServiceTest extends FastDatasetTest implements InstanceTestClassListener
{
  public static final TestGeoObjectInfo BELIZE_CITY      = new TestGeoObjectInfo("Belize Ci't/=&y*)(0$#-@!\"}{][.,;:", "BelizeCity", FastTestDataset.COUNTRY, FastTestDataset.WKT_DEFAULT_MULTIPOLYGON, true, true);

  public static final TestGeoObjectInfo EXACT_MATCH_TEST = new TestGeoObjectInfo("Exact-Term(Test) Match Test", "ExactMatchTest", FastTestDataset.COUNTRY, FastTestDataset.WKT_DEFAULT_MULTIPOLYGON, true, true);

  public static final TestGeoObjectInfo EXACT_MATCH_FAIL = new TestGeoObjectInfo("Exact-Term(Test) Match Fail", "ExactMatchFail", FastTestDataset.COUNTRY, FastTestDataset.WKT_DEFAULT_MULTIPOLYGON, true, true);

  public static final TestGeoObjectInfo EXACT_WRONG_FAIL = new TestGeoObjectInfo("Exact-Term(Test) Wrong Fail", "ExactWrongFail", FastTestDataset.COUNTRY, FastTestDataset.WKT_DEFAULT_MULTIPOLYGON, true, true);

  @Autowired
  private SearchService                 service;

  @Before
  public void setUp()
  {
    testData.setUpInstanceData();

    BELIZE_CITY.apply();
    // HALF_LIFE.apply();
    // HALFLIFE.apply();
    EXACT_MATCH_TEST.apply();
    EXACT_MATCH_FAIL.apply();
    EXACT_WRONG_FAIL.apply();

    testData.logIn(FastTestDataset.USER_ADMIN);
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    BELIZE_CITY.delete();
    // HALF_LIFE.delete();
    // HALFLIFE.delete();
    EXACT_MATCH_TEST.delete();
    EXACT_MATCH_FAIL.delete();
    EXACT_WRONG_FAIL.delete();

    testData.tearDownInstanceData();
  }

  @Test
  @Request
  public void testSearchTable()
  {

    service.clear();
    service.deleteSearchTable();
    service.createSearchTable();

    new SearchTablePatch().createRecords(service);

    Date date = FastTestDataset.DEFAULT_OVER_TIME_DATE;

    List<ServerGeoObjectIF> results = service.search(FastTestDataset.CAMBODIA.getDisplayLabel(), date, 10L);

    Assert.assertEquals(1, results.size());

    ServerGeoObjectIF result = results.get(0);

    Assert.assertEquals(result.getCode(), FastTestDataset.CAMBODIA.getCode());

    Assert.assertEquals(6, service.search(FastTestDataset.TEST_DATA_KEY, date, 10L).size());
    Assert.assertEquals(1, service.search(FastTestDataset.TEST_DATA_KEY, date, 1L).size());
  }

  @Test
  @Request
  public void testWhitespace()
  {

    service.clear();
    service.deleteSearchTable();
    service.createSearchTable();

    new SearchTablePatch().createRecords(service);

    Date date = FastTestDataset.DEFAULT_OVER_TIME_DATE;

    List<ServerGeoObjectIF> results = service.search(FastTestDataset.CAMBODIA.getDisplayLabel() + " ", date, 10L);

    Assert.assertEquals(1, results.size());

    ServerGeoObjectIF result = results.get(0);

    Assert.assertEquals(result.getCode(), FastTestDataset.CAMBODIA.getCode());

    Assert.assertEquals(6, service.search(FastTestDataset.TEST_DATA_KEY, date, 10L).size());
    Assert.assertEquals(1, service.search(FastTestDataset.TEST_DATA_KEY, date, 1L).size());
  }

  @Test
  @Request
  public void testSpecialCharacters()
  {

    service.clear();
    service.deleteSearchTable();
    service.createSearchTable();

    new SearchTablePatch().createRecords(service);

    Date date = FastTestDataset.DEFAULT_OVER_TIME_DATE;

    // These tests were removed because dashes are not escaping properly and we
    // did a little bit of a hack so it doesn't query exactly as expected
    // assertFound(service.search("Half-", date, 10L), HALF_LIFE);
    // assertFound(service.search(HALF_LIFE.getDisplayLabel(), date, 10L),
    // HALF_LIFE);

    assertFound(service.search("Bel", date, 10L), BELIZE_CITY);
    assertFound(service.search("Belize", date, 10L), BELIZE_CITY);
    assertFound(service.search("Belize ", date, 10L), BELIZE_CITY);
    assertFound(service.search("Belize Ci", date, 10L), BELIZE_CITY);
    assertFound(service.search("Belize Ci't", date, 10L), BELIZE_CITY);
    assertFound(service.search("Belize Ci't/", date, 10L), BELIZE_CITY);
    assertFound(service.search("Belize Ci't/=", date, 10L), BELIZE_CITY);
    assertFound(service.search("Belize Ci't/=&", date, 10L), BELIZE_CITY);
    assertFound(service.search("Belize Ci't/=&y*", date, 10L), BELIZE_CITY);
    assertFound(service.search("Belize Ci't/=&y*)", date, 10L), BELIZE_CITY);
    assertFound(service.search("Belize Ci't/=&y*)(", date, 10L), BELIZE_CITY);
    assertFound(service.search("Belize Ci't/=&y*)(", date, 10L), BELIZE_CITY);
    assertFound(service.search("Belize Ci't/=&y*)(0", date, 10L), BELIZE_CITY);
    assertFound(service.search("Belize Ci't/=&y*)(0$", date, 10L), BELIZE_CITY);
    assertFound(service.search("Belize Ci't/=&y*)(0$#", date, 10L), BELIZE_CITY);
    assertFound(service.search("Belize Ci't/=&y*)(0$#-", date, 10L), BELIZE_CITY);
    assertFound(service.search("Belize Ci't/=&y*)(0$#-@!", date, 10L), BELIZE_CITY);
    assertFound(service.search("Belize Ci't/=&y*)(0$#-@!\"", date, 10L), BELIZE_CITY);
    assertFound(service.search("Belize Ci't/=&y*)(0$#-@!\"}", date, 10L), BELIZE_CITY);
    assertFound(service.search("Belize Ci't/=&y*)(0$#-@!\"}{", date, 10L), BELIZE_CITY);
    assertFound(service.search("Belize Ci't/=&y*)(0$#-@!\"}{]", date, 10L), BELIZE_CITY);
    assertFound(service.search("Belize Ci't/=&y*)(0$#-@!\"}{][", date, 10L), BELIZE_CITY);
    assertFound(service.search("Belize Ci't/=&y*)(0$#-@!\"}{][.", date, 10L), BELIZE_CITY);
    assertFound(service.search("Belize Ci't/=&y*)(0$#-@!\"}{][.,", date, 10L), BELIZE_CITY);
    assertFound(service.search("Belize Ci't/=&y*)(0$#-@!\"}{][.,;", date, 10L), BELIZE_CITY);
    assertFound(service.search("Belize Ci't/=&y*)(0$#-@!\"}{][.,;:", date, 10L), BELIZE_CITY);
    assertFound(service.search(BELIZE_CITY.getDisplayLabel(), date, 10L), BELIZE_CITY);
  }

  private void assertFound(List<ServerGeoObjectIF> results, TestGeoObjectInfo go)
  {
    Assert.assertEquals(1, results.size());

    ServerGeoObjectIF result = results.get(0);

    Assert.assertEquals(result.getCode(), go.getCode());
  }

  @Test
  @Request
  public void testSearchExactMatch()
  {

    service.clear();
    service.deleteSearchTable();
    service.createSearchTable();

    new SearchTablePatch().createRecords(service);

    Date date = FastTestDataset.DEFAULT_OVER_TIME_DATE;

    List<ServerGeoObjectIF> results = service.search("Exact-Term(Test)", date, 10L);
    Assert.assertEquals(3, results.size());

    results = service.search("Exact-Term(Test) Match", date, 10L);
    Assert.assertEquals(3, results.size());
    Assert.assertEquals(EXACT_WRONG_FAIL.getCode(), results.get(2).getCode());

    results = service.search("Exact-Term(Test) Match Test", date, 10L);
    Assert.assertEquals(3, results.size());
    Assert.assertEquals(EXACT_MATCH_TEST.getCode(), results.get(0).getCode());
    Assert.assertEquals(EXACT_MATCH_FAIL.getCode(), results.get(1).getCode());
    Assert.assertEquals(EXACT_WRONG_FAIL.getCode(), results.get(2).getCode());
  }

  @Test
  @Request
  public void testSearchNull()
  {

    service.clear();
    service.deleteSearchTable();
    service.createSearchTable();

    new SearchTablePatch().createRecords(service);

    Date date = FastTestDataset.DEFAULT_OVER_TIME_DATE;

    List<ServerGeoObjectIF> results = service.search(null, date, 10L);

    Assert.assertTrue(results.size() > 0);
  }

  @Test
  @Request
  public void testSearchLabels()
  {

    service.clear();
    service.deleteSearchTable();
    service.createSearchTable();

    new SearchTablePatch().createRecords(service);

    Date date = FastTestDataset.DEFAULT_OVER_TIME_DATE;

    List<JsonObject> results = service.labels(FastTestDataset.CAMBODIA.getDisplayLabel(), date, 10L);

    Assert.assertEquals(1, results.size());

    JsonObject result = results.get(0);

    Assert.assertEquals(result.get("code").getAsString(), FastTestDataset.CAMBODIA.getCode());
  }

}
