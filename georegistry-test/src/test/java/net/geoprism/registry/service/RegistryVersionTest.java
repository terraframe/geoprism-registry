/**
 *
 */
package net.geoprism.registry.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.AttributeGeometry;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.ValueOverTimeCollectionDTO;
import org.commongeoregistry.adapter.dataaccess.ValueOverTimeDTO;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.dataaccess.graph.attributes.ValueOverTimeCollection;
import com.runwaysdk.session.Request;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestRegistryClient;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class RegistryVersionTest extends FastDatasetTest implements InstanceTestClassListener
{
  @Autowired
  private TestRegistryClient            client;
  
  @Autowired private GeoObjectBusinessServiceIF goService;
  
  @Before
  public void setUp()
  {
    testData.setUpInstanceData();
    
    testData.logIn(FastTestDataset.USER_CGOV_RA);
  }

  @After
  public void tearDown()
  {
    testData.logOut();
    
    testData.tearDownInstanceData();
  }
  
  @Test
  public void testGetGeoObjectOverTimeByCode() throws ParseException
  {
    this.addVersionData(FastTestDataset.PROV_CENTRAL);
    
    GeoObjectOverTime goTime = client.getGeoObjectOverTimeByCode(FastTestDataset.PROV_CENTRAL.getCode(), FastTestDataset.PROV_CENTRAL.getGeoObjectType().getCode());
    
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    dateFormat.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);
    
    Assert.assertEquals(FastTestDataset.PROV_CENTRAL.getCode(), goTime.getCode());
    Assert.assertEquals(Boolean.FALSE, goTime.getExists(dateFormat.parse("1990-01-01")));
    Assert.assertEquals(Boolean.TRUE, goTime.getExists(dateFormat.parse("1990-02-01")));
    Assert.assertEquals(Boolean.FALSE, goTime.getExists(dateFormat.parse("1990-03-01")));
    
    ValueOverTimeCollectionDTO allStatus = goTime.getAllValues(DefaultAttribute.EXISTS.getName());
    Iterator<ValueOverTimeDTO> it = allStatus.iterator();
    ValueOverTimeDTO first = it.next();
    ValueOverTimeDTO second = it.next();
    ValueOverTimeDTO third = it.next();
    Assert.assertEquals("1990-01-01", dateFormat.format(first.getStartDate()));
    Assert.assertEquals("1990-01-31", dateFormat.format(first.getEndDate()));
    Assert.assertEquals("1990-02-01", dateFormat.format(second.getStartDate()));
    Assert.assertEquals("1990-02-28", dateFormat.format(second.getEndDate()));
    Assert.assertEquals("1990-03-01", dateFormat.format(third.getStartDate()));
    Assert.assertEquals("1990-03-31", dateFormat.format(third.getEndDate()));
    
    Geometry expectedGeom = FastTestDataset.PROV_CENTRAL.fetchGeoObject().getGeometry();
    Geometry actualGeom = ( (AttributeGeometry) goTime.getAttributeOnDate(DefaultAttribute.GEOMETRY.getName(), FastTestDataset.DEFAULT_OVER_TIME_DATE) ).getValue();
    Assert.assertTrue(expectedGeom.equalsTopo(actualGeom));
  }
  
  /**
   * Tests to make sure if we set a value which is encompassed by a larger surrounding date range
   * that the operation is ignored.
   */
  @Test
  @Request
  public void testInsertDuplicateBetween() throws ParseException
  {
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    dateFormat.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);
    
    this.addVersionData(FastTestDataset.PROV_CENTRAL);
    
    Assert.assertEquals(3, FastTestDataset.PROV_CENTRAL.getServerObject().getValuesOverTime(DefaultAttribute.EXISTS.getName()).size());
    
    FastTestDataset.PROV_CENTRAL.getServerObject().setExists(Boolean.TRUE, dateFormat.parse("02-10-1990"), dateFormat.parse("02-15-1990"));
    
    Assert.assertEquals(3, FastTestDataset.PROV_CENTRAL.getServerObject().getValuesOverTime(DefaultAttribute.EXISTS.getName()).size());
    
    GeoObjectOverTime goTime = client.getGeoObjectOverTimeByCode(FastTestDataset.PROV_CENTRAL.getCode(), FastTestDataset.PROV_CENTRAL.getGeoObjectType().getCode());
    
    Assert.assertEquals(3, goTime.getAllValues(DefaultAttribute.EXISTS.getName()).size());
    
    
    Assert.assertEquals(FastTestDataset.PROV_CENTRAL.getCode(), goTime.getCode());
    Assert.assertEquals(Boolean.FALSE, goTime.getExists(dateFormat.parse("1990-01-01")));
    Assert.assertEquals(Boolean.TRUE, goTime.getExists(dateFormat.parse("1990-02-01")));
    Assert.assertEquals(Boolean.FALSE, goTime.getExists(dateFormat.parse("1990-03-01")));
    
    ValueOverTimeCollectionDTO allStatus = goTime.getAllValues(DefaultAttribute.EXISTS.getName());
    Iterator<ValueOverTimeDTO> it = allStatus.iterator();
    ValueOverTimeDTO first = it.next();
    ValueOverTimeDTO second = it.next();
    ValueOverTimeDTO third = it.next();
    Assert.assertEquals("1990-01-01", dateFormat.format(first.getStartDate()));
    Assert.assertEquals("1990-01-31", dateFormat.format(first.getEndDate()));
    Assert.assertEquals("1990-02-01", dateFormat.format(second.getStartDate()));
    Assert.assertEquals("1990-02-28", dateFormat.format(second.getEndDate()));
    Assert.assertEquals("1990-03-01", dateFormat.format(third.getStartDate()));
    Assert.assertEquals("1990-03-31", dateFormat.format(third.getEndDate()));
    
    Geometry expectedGeom = FastTestDataset.PROV_CENTRAL.fetchGeoObject().getGeometry();
    Geometry actualGeom = ( (AttributeGeometry) goTime.getAttributeOnDate(DefaultAttribute.GEOMETRY.getName(), FastTestDataset.DEFAULT_OVER_TIME_DATE) ).getValue();
    Assert.assertTrue(expectedGeom.equalsTopo(actualGeom));
  }
  
  @Test
  @Request
  public void testInsertDuplicateLabelBetween() throws ParseException
  {
    LocalizedValue lv = new LocalizedValue("My date range is huge");
    lv.setValue(LocalizedValue.DEFAULT_LOCALE, "My date range is huge");
    
    genericDuplicateDatatypeTest(DefaultAttribute.DISPLAY_LABEL.getName(), lv);
  }

  private void genericDuplicateDatatypeTest(String attributeName, Object value) throws ParseException
  {
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    dateFormat.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);
    
    // Initial Setup : Create a VOT with a large date range
    VertexServerGeoObject serverObj = (VertexServerGeoObject) FastTestDataset.PROV_CENTRAL.getServerObject();
    serverObj.setValuesOverTime(attributeName, new ValueOverTimeCollection());
    serverObj.setValue(attributeName, value, dateFormat.parse("1990-01-01"), dateFormat.parse("1990-02-01"));
    goService.apply(serverObj, false);
    Assert.assertEquals(1, FastTestDataset.PROV_CENTRAL.getServerObject().getValuesOverTime(attributeName).size());
    
    // Set a value inside that date range with the same value
    VertexServerGeoObject serverObj2 = (VertexServerGeoObject) FastTestDataset.PROV_CENTRAL.getServerObject();
    serverObj2.setValue(attributeName, value, dateFormat.parse("1990-01-05"), dateFormat.parse("1990-01-10"));
    goService.apply(serverObj2, false);
    
    // Fetch the object and assert values on it
    Assert.assertEquals(1, FastTestDataset.PROV_CENTRAL.getServerObject().getValuesOverTime(attributeName).size());
    
    GeoObjectOverTime goTime = client.getGeoObjectOverTimeByCode(FastTestDataset.PROV_CENTRAL.getCode(), FastTestDataset.PROV_CENTRAL.getGeoObjectType().getCode());
    
    Assert.assertEquals(1, goTime.getAllValues(attributeName).size());
    
    ValueOverTimeCollectionDTO all = goTime.getAllValues(attributeName);
    Iterator<ValueOverTimeDTO> it = all.iterator();
    ValueOverTimeDTO first = it.next();
    Assert.assertEquals("1990-01-01", dateFormat.format(first.getStartDate()));
    Assert.assertEquals("1990-02-01", dateFormat.format(first.getEndDate()));
    Assert.assertTrue(value.equals(first.getValue()));
  }
  
//  @Test
  //  public void testSetAttributeVersions()
//  {
//    String attributeName = "status";
//    AttributeType type = getAttributeType(attributeName);
//    
//    ServerGeoObjectIF serverObj = testData.PROV_CENTRAL.getServerObject();
//    
//    Assert.assertEquals(0, serverObj.getValuesOverTime(attributeName).size());
//    
//    ValueOverTimeCollectionDTO dto = buildVersionDTO(type);
//    
//    testData.adapter.setAttributeVersions(testData.PROV_CENTRAL.getCode(), testData.PROV_CENTRAL.getGeoObjectType().getCode(), "status", dto);
//    
//    assertVersionData(ValueOverTimeConverter.colToDTO(serverObj.getValuesOverTime(attributeName), type));
//  }
//  
//  private void assertVersionData(ValueOverTimeCollectionDTO dto)
//  {
//    SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
//    dateFormat.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);
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
  private void addVersionData(TestGeoObjectInfo geoObj) throws ParseException
  {
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
    dateFormat.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);
    
    ServerGeoObjectIF serverObj = geoObj.getServerObject();
    
    // Clear the existing values
    serverObj.setValuesOverTime(DefaultAttribute.EXISTS.getName(), new ValueOverTimeCollection());
    
    serverObj.setExists(Boolean.FALSE, dateFormat.parse("01-01-1990"), dateFormat.parse("01-31-1990"));
    Assert.assertEquals(Boolean.FALSE, serverObj.getExists(dateFormat.parse("01-01-1990")));
    
    serverObj.setExists(Boolean.TRUE, dateFormat.parse("02-01-1990"), dateFormat.parse("02-28-1990"));
    Assert.assertEquals(Boolean.TRUE, serverObj.getExists(dateFormat.parse("02-01-1990")));
    
    serverObj.setExists(Boolean.FALSE, dateFormat.parse("03-01-1990"), dateFormat.parse("03-31-1990"));
    Assert.assertEquals(Boolean.FALSE, serverObj.getExists(dateFormat.parse("03-01-1990")));
    
    goService.apply(serverObj, false);
    
    Assert.assertEquals(Boolean.FALSE, serverObj.getExists(dateFormat.parse("01-01-1990")));
  }
//  
//  private ValueOverTimeCollectionDTO buildVersionDTO(AttributeType type)
//  {
//    SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
//    dateFormat.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);
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
//    return testData.PROV_CENTRAL.getGeoObjectType().getGeoObjectType().getAttribute(attributeName).get();
//  }
}
