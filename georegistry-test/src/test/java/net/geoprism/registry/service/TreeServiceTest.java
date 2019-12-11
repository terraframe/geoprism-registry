package net.geoprism.registry.service;

import org.json.JSONArray;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.geoprism.registry.test.USATestData;

public class TreeServiceTest
{
  protected static USATestData               testData;

  @BeforeClass
  public static void setUpClass()
  {
    testData = USATestData.newTestDataForClass();
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
    if (testData != null)
    {
      testData.setUpInstanceData();
    }
  }

  @After
  public void tearDown()
  {
    if (testData != null)
    {
      testData.tearDownInstanceData();
    }
  }
  
  @Test
  public void testGetHierarchies()
  {
    JSONArray ptn = testData.adapter.getHierarchiesForGeoObjectOverTime(testData.CO_D_ONE.getCode(), testData.CO_D_ONE.getGeoObjectType().getCode());
    
    Assert.assertTrue(ptn.length() > 0);
  }
}
