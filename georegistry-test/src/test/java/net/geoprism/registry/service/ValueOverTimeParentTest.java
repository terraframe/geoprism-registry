/**
 *
 */
package net.geoprism.registry.service;

import java.util.Calendar;
import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTimeCollection;
import com.runwaysdk.session.Request;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;

public class ValueOverTimeParentTest
{
  protected static FastTestDataset      testData;
  
  public static final TestGeoObjectInfo BELIZE = new TestGeoObjectInfo("Belize", FastTestDataset.COUNTRY);
  
  public static final TestGeoObjectInfo TEST_GO = new TestGeoObjectInfo("VOT_TEST_GO", FastTestDataset.PROVINCE);

  @BeforeClass
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();
    testData.setUpMetadata();
  }

  @AfterClass
  public static void cleanUpClass()
  {
    testData.tearDownMetadata();
  }

  @Before
  public void setUp()
  {
    testData.setUpInstanceData();

    testData.logIn(FastTestDataset.USER_CGOV_RA);

//    TestDataSet.populateAdapterIds(null, testData.adapter);
    
    TEST_GO.apply();
    BELIZE.apply();
    
    FastTestDataset.CAMBODIA.addChild(TEST_GO, FastTestDataset.HIER_ADMIN);
  }

  @After
  public void tearDown()
  {
    testData.logOut();
    
    TEST_GO.delete();
    BELIZE.delete();

    testData.tearDownInstanceData();
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
    go.addParent(FastTestDataset.CAMBODIA.getServerObject(), FastTestDataset.HIER_ADMIN.getServerObject(), start, end);
    go.apply(false);
    
    go = TEST_GO.getServerObject();
    ValueOverTimeCollection parents = go.getParentCollection(FastTestDataset.HIER_ADMIN.getServerObject());
    Assert.assertEquals(2, parents.size());
    
    ValueOverTime vot = parents.get(0);
    Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), ( (ServerGeoObjectIF) vot.getValue() ).getCode());
    Assert.assertEquals(start, vot.getStartDate());
    Assert.assertEquals(end, vot.getEndDate());
    
    ValueOverTime vot2 = parents.get(1);
    Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), ( (ServerGeoObjectIF) vot2.getValue() ).getCode());
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
    go.addParent(FastTestDataset.CAMBODIA.getServerObject(), FastTestDataset.HIER_ADMIN.getServerObject(), addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, 1), addDay(TestDataSet.DEFAULT_END_TIME_DATE, -1));
    go.apply(false);
    
    go = TEST_GO.getServerObject();
    ValueOverTimeCollection votc = go.getParentCollection(FastTestDataset.HIER_ADMIN.getServerObject());
    Assert.assertEquals(1, votc.size());
    
    ValueOverTime vot = votc.get(0);
    Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), ( (ServerGeoObjectIF) vot.getValue() ).getCode());
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
    go.addParent(BELIZE.getServerObject(), FastTestDataset.HIER_ADMIN.getServerObject(), addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, 5), addDay(TestDataSet.DEFAULT_END_TIME_DATE, -5));
    go.apply(false);
    
    go = TEST_GO.getServerObject();
    ValueOverTimeCollection votc = go.getParentCollection(FastTestDataset.HIER_ADMIN.getServerObject());
    Assert.assertEquals(3, votc.size());
    
    ValueOverTime vot = votc.get(0);
    Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), ( (ServerGeoObjectIF) vot.getValue() ).getCode());
    Assert.assertEquals(TestDataSet.DEFAULT_OVER_TIME_DATE, vot.getStartDate());
    Assert.assertEquals(addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, 4), vot.getEndDate());
    
    ValueOverTime vot2 = votc.get(1);
    Assert.assertEquals(BELIZE.getCode(), ( (ServerGeoObjectIF) vot2.getValue() ).getCode());
    Assert.assertEquals(addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, 5), vot2.getStartDate());
    Assert.assertEquals(addDay(TestDataSet.DEFAULT_END_TIME_DATE, -5), vot2.getEndDate());
    
    ValueOverTime vot3 = votc.get(2);
    Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), ( (ServerGeoObjectIF) vot3.getValue() ).getCode());
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
    go.addParent(FastTestDataset.CAMBODIA.getServerObject(), FastTestDataset.HIER_ADMIN.getServerObject(), addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, -5), addDay(TestDataSet.DEFAULT_END_TIME_DATE, 5));
    go.apply(false);
    
    go = TEST_GO.getServerObject();
    ValueOverTimeCollection votc = go.getParentCollection(FastTestDataset.HIER_ADMIN.getServerObject());
    Assert.assertEquals(1, votc.size());
    
    ValueOverTime vot = votc.get(0);
    Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), ( (ServerGeoObjectIF) vot.getValue() ).getCode());
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
    go.addParent(BELIZE.getServerObject(), FastTestDataset.HIER_ADMIN.getServerObject(), addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, -5), addDay(TestDataSet.DEFAULT_END_TIME_DATE, 5));
    go.apply(false);
    
    go = TEST_GO.getServerObject();
    ValueOverTimeCollection votc = go.getParentCollection(FastTestDataset.HIER_ADMIN.getServerObject());
    Assert.assertEquals(1, votc.size());
    
    ValueOverTime vot = votc.get(0);
    Assert.assertEquals(BELIZE.getCode(), ( (ServerGeoObjectIF) vot.getValue() ).getCode());
    Assert.assertEquals(addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, -5), vot.getStartDate());
    Assert.assertEquals(addDay(TestDataSet.DEFAULT_END_TIME_DATE, 5), vot.getEndDate());
  }
  
  /**
   * Imported data completely consumes existing data, and the values are different.
   */
  @Test
  @Request
  public void testGetParentAtTime()
  {
    ServerGeoObjectIF go = TEST_GO.getServerObject();
    go.addParent(BELIZE.getServerObject(), FastTestDataset.HIER_ADMIN.getServerObject(), addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, -5), addDay(TestDataSet.DEFAULT_END_TIME_DATE, 5));
    go.apply(false);
    
    go = TEST_GO.getServerObject();
    ServerParentTreeNode node = go.getParentsForHierarchy(FastTestDataset.HIER_ADMIN.getServerObject(), false, TestDataSet.DEFAULT_OVER_TIME_DATE);
    Assert.assertEquals(1, node.getParents().size());
    
    ServerGeoObjectIF value = node.getParents().get(0).getGeoObject();
    Assert.assertEquals(BELIZE.getCode(), value.getCode());
  }  
  
  /**
   * Imported data partially overlaps an existing range, and the values are the same.
   */
  @Test
  @Request
  public void testPartialOverlapSameValue()
  {
    ServerGeoObjectIF go = TEST_GO.getServerObject();
    go.addParent(FastTestDataset.CAMBODIA.getServerObject(), FastTestDataset.HIER_ADMIN.getServerObject(), addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, -30), addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, 5));
    go.apply(false);
    
    go = TEST_GO.getServerObject();
    ValueOverTimeCollection votc = go.getParentCollection(FastTestDataset.HIER_ADMIN.getServerObject());
    Assert.assertEquals(1, votc.size());
    
    ValueOverTime vot = votc.get(0);
    Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), ( (ServerGeoObjectIF) vot.getValue() ).getCode());
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
    go.addParent(BELIZE.getServerObject(), FastTestDataset.HIER_ADMIN.getServerObject(), addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, -30), addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, 5));
    go.apply(false);
    
    go = TEST_GO.getServerObject();
    ValueOverTimeCollection votc = go.getParentCollection(FastTestDataset.HIER_ADMIN.getServerObject());
    Assert.assertEquals(2, votc.size());
    
    ValueOverTime vot = votc.get(0);
    Assert.assertEquals(BELIZE.getCode(), ( (ServerGeoObjectIF) vot.getValue() ).getCode());
    Assert.assertEquals(addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, -30), vot.getStartDate());
    Assert.assertEquals(addDay(TestDataSet.DEFAULT_OVER_TIME_DATE, 5), vot.getEndDate());
    
    ValueOverTime vot2 = votc.get(1);
    Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), ( (ServerGeoObjectIF) vot2.getValue() ).getCode());
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
