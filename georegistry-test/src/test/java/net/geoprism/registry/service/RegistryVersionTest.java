package net.geoprism.registry.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.commongeoregistry.adapter.constants.DefaultTerms;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.session.Request;

import junit.framework.Assert;
import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.USATestData;

public class RegistryVersionTest
{
  protected static USATestData               testData;

  @BeforeClass
  public static void setUpClass()
  {
    testData = USATestData.newTestDataForClass();
    testData.setUpClass();
  }
  
  @AfterClass
  public static void cleanUpClass()
  {
    if (testData != null)
    {
      testData.cleanUpClass();
    }
  }
  
  @Before
  public void setUp()
  {
    if (testData != null)
    {
      testData.setUpTest();
    }
  }

  @After
  public void tearDown()
  {
    if (testData != null)
    {
      testData.cleanUpTest();
    }
  }
  
  @Test
  public void testGetGeoObjectOverTimeByCode()
  {
    this.addVersionData(testData.COLORADO);
    
    GeoObjectOverTime goTime = testData.adapter.getGeoObjectOverTimeByCode(testData.COLORADO.getCode(), testData.COLORADO.getGeoObjectType().getCode());
    
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    
    try
    {
      Assert.assertEquals(testData.COLORADO.getCode(), goTime.getCode());
      Assert.assertEquals(DefaultTerms.GeoObjectStatusTerm.INACTIVE.code, goTime.getStatus(dateFormat.parse("01-01-1990")).getCode());
      Assert.assertEquals(DefaultTerms.GeoObjectStatusTerm.NEW.code, goTime.getStatus(dateFormat.parse("02-01-1990")).getCode());
      Assert.assertEquals(DefaultTerms.GeoObjectStatusTerm.PENDING.code, goTime.getStatus(dateFormat.parse("03-01-1990")).getCode());
    }
    catch (ParseException e)
    {
      throw new RuntimeException(e);
    }
  }
  
//  @Test
//  public void testSetAttributeVersions()
//  {
//    String attributeName = "status";
//    AttributeType type = getAttributeType(attributeName);
//    
//    ServerGeoObjectIF serverObj = testData.COLORADO.getServerObject();
//    
//    Assert.assertEquals(0, serverObj.getValuesOverTime(attributeName).size());
//    
//    ValueOverTimeCollectionDTO dto = buildVersionDTO(type);
//    
//    testData.adapter.setAttributeVersions(testData.COLORADO.getCode(), testData.COLORADO.getGeoObjectType().getCode(), "status", dto);
//    
//    assertVersionData(ValueOverTimeConverter.colToDTO(serverObj.getValuesOverTime(attributeName), type));
//  }
//  
//  private void assertVersionData(ValueOverTimeCollectionDTO dto)
//  {
//    SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
//    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
//    
//    Assert.assertEquals(3, dto.size());
//   
//    try
//    {
//      Assert.assertEquals(dto.get(0).getValue(), GeoObjectStatus.INACTIVE);
//      Assert.assertEquals(dto.get(0).getStartDate(), dateFormat.parse("01-01-1990"));
//      Assert.assertEquals(dto.get(0).getEndDate(), dateFormat.parse("01-31-1990"));
//      
//      Assert.assertEquals(dto.get(1).getValue(), GeoObjectStatus.NEW);
//      Assert.assertEquals(dto.get(1).getStartDate(), dateFormat.parse("02-01-1990"));
//      Assert.assertEquals(dto.get(1).getEndDate(), dateFormat.parse("02-28-1990"));
//      
//      Assert.assertEquals(dto.get(2).getValue(), GeoObjectStatus.PENDING);
//      Assert.assertEquals(dto.get(2).getStartDate(), dateFormat.parse("03-01-1990"));
//      Assert.assertEquals(dto.get(2).getEndDate(), ValueOverTime.INFINITY_END_DATE);
//    }
//    catch (ParseException e)
//    {
//      throw new RuntimeException(e);
//    }
//  }
//  
  @Request
  private void addVersionData(TestGeoObjectInfo geoObj)
  {
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    
    try
    {
      ServerGeoObjectIF serverObj = geoObj.getServerObject();
      serverObj.getValuesOverTime("status").clear();
      serverObj.setStatus(GeoObjectStatus.INACTIVE, dateFormat.parse("01-01-1990"), null);
      serverObj.setStatus(GeoObjectStatus.NEW, dateFormat.parse("02-01-1990"), null);
      serverObj.setStatus(GeoObjectStatus.PENDING, dateFormat.parse("03-01-1990"), null);
      serverObj.apply(false);
    }
    catch (ParseException e)
    {
      throw new RuntimeException(e);
    }
  }
//  
//  private ValueOverTimeCollectionDTO buildVersionDTO(AttributeType type)
//  {
//    SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
//    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
//    
//    ValueOverTimeCollectionDTO dto = new ValueOverTimeCollectionDTO();
//    
//    try
//    {
//      dto.add(new ValueOverTimeDTO(dateFormat.parse("01-01-1990"), null, GeoObjectStatus.INACTIVE, type));
//      dto.add(new ValueOverTimeDTO(dateFormat.parse("02-01-1990"), null, GeoObjectStatus.NEW, type));
//      dto.add(new ValueOverTimeDTO(dateFormat.parse("03-01-1990"), null, GeoObjectStatus.PENDING, type));
//    }
//    catch (ParseException e)
//    {
//      throw new RuntimeException(e);
//    }
//    
//    return dto;
//  }
//  
//  @Request
//  private AttributeType getAttributeType(String attributeName)
//  {
//    return testData.COLORADO.getGeoObjectType().getGeoObjectType().getAttribute(attributeName).get();
//  }
}
