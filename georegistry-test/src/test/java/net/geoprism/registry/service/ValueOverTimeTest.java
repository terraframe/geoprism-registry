/**
 *
 */
package net.geoprism.registry.service;

import java.util.Calendar;
import java.util.Date;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTimeCollection;
import com.runwaysdk.session.Request;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc

@RunWith(SpringInstanceTestClassRunner.class)
public class ValueOverTimeTest extends FastDatasetTest implements InstanceTestClassListener
{
  public static final TestGeoObjectInfo TEST_GO = new TestGeoObjectInfo("VOT_TEST_GO", FastTestDataset.COUNTRY, FastTestDataset.SOURCE);

  @Autowired private GeoObjectBusinessServiceIF goBiz;
  
  @Before
  public void setUp()
  {
    testData.setUpInstanceData();

    testData.logIn(FastTestDataset.USER_CGOV_RA);

//    TestDataSet.populateAdapterIds(null, testData.adapter);
    
    TEST_GO.apply();
//    ServerGeoObjectIF go = TEST_GO.getServerObject();
//    ValueOverTimeCollection votc = go.getValuesOverTime(DefaultAttribute.EXISTS.getName());
//    votc.clear();
//    votc.add(new ValueOverTime());
  }

  @After
  public void tearDown()
  {
    testData.logOut();
    
    TEST_GO.delete();

    testData.tearDownInstanceData();
  }
  
  @Test
  @Request
  public void testStoreSameLocalizedValue()
  {
    final String defaultValue = "default store";
    
    LocalizedValue original = new LocalizedValue(defaultValue);
    original.setValue(LocalizedValue.DEFAULT_LOCALE, defaultValue);
    
    ServerGeoObjectIF go = TEST_GO.getServerObject();
    go.setDisplayLabel(original, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);
    goBiz.apply(go, false, false);
    
    final String newValue = defaultValue;
    
    LocalizedValue lvNew = new LocalizedValue(newValue);
    lvNew.setValue(LocalizedValue.DEFAULT_LOCALE, newValue);
    
    ServerGeoObjectIF go2 = TEST_GO.getServerObject();
    go2.setDisplayLabel(lvNew, addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, 5), addDay(TestDataSet.DEFAULT_END_TIME_DATE, -5));
    goBiz.apply(go2, false, false);
    
    ServerGeoObjectIF go3 = TEST_GO.getServerObject();
    ValueOverTimeCollection votc = go3.getValuesOverTime(DefaultAttribute.DISPLAY_LABEL.getName());
    Assert.assertEquals(1, votc.size());
    
    ValueOverTime vot = votc.get(0);
    Assert.assertEquals(defaultValue, ((LocalizedValue) vot.getValue()).getValue(LocalizedValue.DEFAULT_LOCALE));
    Assert.assertEquals(TestDataSet.DEFAULT_OVER_TIME_DATE, vot.getStartDate());
    Assert.assertEquals(TestDataSet.DEFAULT_END_TIME_DATE, vot.getEndDate());
  }
  
  @Test
  @Request
  public void testStoreDifferentLocalizedValue()
  {
    final String defaultValue = "default store";
    
    LocalizedValue original = new LocalizedValue(defaultValue);
    original.setValue(LocalizedValue.DEFAULT_LOCALE, defaultValue);
    
    ServerGeoObjectIF go = TEST_GO.getServerObject();
    go.setDisplayLabel(original, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);
    goBiz.apply(go, false, false);
    
    final String newValue = "new value";
    
    LocalizedValue lvNew = new LocalizedValue(newValue);
    lvNew.setValue(LocalizedValue.DEFAULT_LOCALE, newValue);
    
    ServerGeoObjectIF go2 = TEST_GO.getServerObject();
    go2.setDisplayLabel(lvNew, addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, 5), addDay(TestDataSet.DEFAULT_END_TIME_DATE, -5));
    goBiz.apply(go2, false, false);
    
    ServerGeoObjectIF go3 = TEST_GO.getServerObject();
    ValueOverTimeCollection votc = go3.getValuesOverTime(DefaultAttribute.DISPLAY_LABEL.getName());
    Assert.assertEquals(3, votc.size());
    
    ValueOverTime vot = votc.get(0);
    Assert.assertEquals(defaultValue, ((LocalizedValue) vot.getValue()).getValue(LocalizedValue.DEFAULT_LOCALE));
    Assert.assertEquals(TestDataSet.DEFAULT_OVER_TIME_DATE, vot.getStartDate());
    Assert.assertEquals(addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, 4), vot.getEndDate());
    
    ValueOverTime vot2 = votc.get(1);
    Assert.assertEquals(newValue, ((LocalizedValue) vot2.getValue()).getValue(LocalizedValue.DEFAULT_LOCALE));
    Assert.assertEquals(addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, 5), vot2.getStartDate());
    Assert.assertEquals(addDay(TestDataSet.DEFAULT_END_TIME_DATE, -5), vot2.getEndDate());
    
    ValueOverTime vot3 = votc.get(2);
    Assert.assertEquals(defaultValue, ((LocalizedValue) vot3.getValue()).getValue(LocalizedValue.DEFAULT_LOCALE));
    Assert.assertEquals(addDay(TestDataSet.DEFAULT_END_TIME_DATE, -4), vot3.getStartDate());
    Assert.assertEquals(TestDataSet.DEFAULT_END_TIME_DATE, vot3.getEndDate());
  }

  /**
   * Imported data has no conflict with any existing data in the system.
   */
  @Test
  @Request
  public void testNoOverlap()
  {
    Calendar cStart = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
    cStart.clear();
    cStart.set(2005, Calendar.JANUARY, 1);
    Date start = cStart.getTime();
    
    Calendar cEnd = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
    cEnd.clear();
    cEnd.set(2006, Calendar.JANUARY, 1);
    Date end = cEnd.getTime();
    
    ServerGeoObjectIF go = TEST_GO.getServerObject();
    go.setValue(DefaultAttribute.EXISTS.getName(), false, start, end);
    goBiz.apply(go, false, false);
    
    go = TEST_GO.getServerObject();
    ValueOverTimeCollection votc = go.getValuesOverTime(DefaultAttribute.EXISTS.getName());
    Assert.assertEquals(2, votc.size());
    
    ValueOverTime vot = votc.get(0);
    Assert.assertEquals(false, vot.getValue());
    Assert.assertEquals(start, vot.getStartDate());
    Assert.assertEquals(end, vot.getEndDate());
    
    ValueOverTime vot2 = votc.get(1);
    Assert.assertEquals(true, vot2.getValue());
    Assert.assertEquals(TestDataSet.DEFAULT_OVER_TIME_DATE, vot2.getStartDate());
    Assert.assertEquals(TestDataSet.DEFAULT_END_TIME_DATE, vot2.getEndDate());
  }
  
  /**
   * Imported data is completely eclipsed by existing data, and the values are the same.
   */
  @Test
  @Request
  public void testFullOverlapSameValue()
  {
    ServerGeoObjectIF go = TEST_GO.getServerObject();
    go.setValue(DefaultAttribute.EXISTS.getName(), true, addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, 1), addDay(TestDataSet.DEFAULT_END_TIME_DATE, -1));
    goBiz.apply(go, false, false);
    
    go = TEST_GO.getServerObject();
    ValueOverTimeCollection votc = go.getValuesOverTime(DefaultAttribute.EXISTS.getName());
    Assert.assertEquals(1, votc.size());
    
    ValueOverTime vot = votc.get(0);
    Assert.assertEquals(true, vot.getValue());
    Assert.assertEquals(TestDataSet.DEFAULT_OVER_TIME_DATE, vot.getStartDate());
    Assert.assertEquals(TestDataSet.DEFAULT_END_TIME_DATE, vot.getEndDate());
  }
  
  /**
   * Imported data is completely eclipsed by existing data, and the values are different.
   */
  @Test
  @Request
  public void testFullOverlapDifferentValue()
  {
    ServerGeoObjectIF go = TEST_GO.getServerObject();
    go.setValue(DefaultAttribute.EXISTS.getName(), false, addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, 5), addDay(TestDataSet.DEFAULT_END_TIME_DATE, -5));
    goBiz.apply(go, false, false);
    
    go = TEST_GO.getServerObject();
    ValueOverTimeCollection votc = go.getValuesOverTime(DefaultAttribute.EXISTS.getName());
    Assert.assertEquals(3, votc.size());
    
    ValueOverTime vot = votc.get(0);
    Assert.assertEquals(true, vot.getValue());
    Assert.assertEquals(TestDataSet.DEFAULT_OVER_TIME_DATE, vot.getStartDate());
    Assert.assertEquals(addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, 4), vot.getEndDate());
    
    ValueOverTime vot2 = votc.get(1);
    Assert.assertEquals(false, vot2.getValue());
    Assert.assertEquals(addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, 5), vot2.getStartDate());
    Assert.assertEquals(addDay(TestDataSet.DEFAULT_END_TIME_DATE, -5), vot2.getEndDate());
    
    ValueOverTime vot3 = votc.get(2);
    Assert.assertEquals(true, vot3.getValue());
    Assert.assertEquals(addDay(TestDataSet.DEFAULT_END_TIME_DATE, -4), vot3.getStartDate());
    Assert.assertEquals(TestDataSet.DEFAULT_END_TIME_DATE, vot3.getEndDate());
  }
  
  /**
   * Imported data completely consumes existing data, and the values are the same.
   */
  @Test
  @Request
  public void testFullConsumeSameValue()
  {
    ServerGeoObjectIF go = TEST_GO.getServerObject();
    go.setValue(DefaultAttribute.EXISTS.getName(), true, addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, -5), addDay(TestDataSet.DEFAULT_END_TIME_DATE, 5));
    goBiz.apply(go, false, false);
    
    go = TEST_GO.getServerObject();
    ValueOverTimeCollection votc = go.getValuesOverTime(DefaultAttribute.EXISTS.getName());
    Assert.assertEquals(1, votc.size());
    
    ValueOverTime vot = votc.get(0);
    Assert.assertEquals(true, vot.getValue());
    Assert.assertEquals(addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, -5), vot.getStartDate());
    Assert.assertEquals(addDay(TestDataSet.DEFAULT_END_TIME_DATE, 5), vot.getEndDate());
  }
  
  /**
   * Imported data completely consumes existing data, and the values are different.
   */
  @Test
  @Request
  public void testFullConsumeDifferentValue()
  {
    ServerGeoObjectIF go = TEST_GO.getServerObject();
    go.setValue(DefaultAttribute.EXISTS.getName(), false, addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, -5), addDay(TestDataSet.DEFAULT_END_TIME_DATE, 5));
    goBiz.apply(go, false, false);
    
    go = TEST_GO.getServerObject();
    ValueOverTimeCollection votc = go.getValuesOverTime(DefaultAttribute.EXISTS.getName());
    Assert.assertEquals(1, votc.size());
    
    ValueOverTime vot = votc.get(0);
    Assert.assertEquals(false, vot.getValue());
    Assert.assertEquals(addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, -5), vot.getStartDate());
    Assert.assertEquals(addDay(TestDataSet.DEFAULT_END_TIME_DATE, 5), vot.getEndDate());
  }
  
  /**
   * Imported data partially overlaps an existing range, and the values are the same.
   */
  @Test
  @Request
  public void testPartialOverlapSameValue()
  {
    ServerGeoObjectIF go = TEST_GO.getServerObject();
    go.setValue(DefaultAttribute.EXISTS.getName(), true, addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, -30), addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, 5));
    goBiz.apply(go, false, false);
    
    go = TEST_GO.getServerObject();
    ValueOverTimeCollection votc = go.getValuesOverTime(DefaultAttribute.EXISTS.getName());
    Assert.assertEquals(1, votc.size());
    
    ValueOverTime vot = votc.get(0);
    Assert.assertEquals(true, vot.getValue());
    Assert.assertEquals(addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, -30), vot.getStartDate());
    Assert.assertEquals(TestDataSet.DEFAULT_END_TIME_DATE, vot.getEndDate());
  }
  
  /**
   * Imported data partially overlaps an existing range, and the values are different.
   */
  @Test
  @Request
  public void testPartialOverlapDifferentValue()
  {
    ServerGeoObjectIF go = TEST_GO.getServerObject();
    go.setValue(DefaultAttribute.EXISTS.getName(), false, addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, -30), addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, 5));
    goBiz.apply(go, false, false);
    
    go = TEST_GO.getServerObject();
    ValueOverTimeCollection votc = go.getValuesOverTime(DefaultAttribute.EXISTS.getName());
    Assert.assertEquals(2, votc.size());
    
    ValueOverTime vot = votc.get(0);
    Assert.assertEquals(false, vot.getValue());
    Assert.assertEquals(addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, -30), vot.getStartDate());
    Assert.assertEquals(addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, 5), vot.getEndDate());
    
    ValueOverTime vot2 = votc.get(1);
    Assert.assertEquals(true, vot2.getValue());
    Assert.assertEquals(addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, 6), vot2.getStartDate());
    Assert.assertEquals(TestDataSet.DEFAULT_END_TIME_DATE, vot2.getEndDate());
  }
  
  private Date addDay(Date date, int amount)
  {
    Calendar calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
    calendar.clear();
    calendar.setTime(date);
    calendar.add(Calendar.DAY_OF_MONTH, amount);
    Date newDate = calendar.getTime();
    
    return newDate;
  }
}
