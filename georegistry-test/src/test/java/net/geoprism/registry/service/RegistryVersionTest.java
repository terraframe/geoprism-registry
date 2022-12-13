/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.AttributeGeometry;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.ValueOverTimeCollectionDTO;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.runwaysdk.business.graph.GraphObject;
import com.runwaysdk.session.Request;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestRegistryClient;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
public class RegistryVersionTest
{
  protected static FastTestDataset               testData;

  @Autowired
  private TestRegistryClient            client;

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
    Assert.assertEquals("1990-01-01", dateFormat.format(allStatus.get(0).getStartDate()));
    Assert.assertEquals("1990-01-31", dateFormat.format(allStatus.get(0).getEndDate()));
    Assert.assertEquals("1990-02-01", dateFormat.format(allStatus.get(1).getStartDate()));
    Assert.assertEquals("1990-02-28", dateFormat.format(allStatus.get(1).getEndDate()));
    Assert.assertEquals("1990-03-01", dateFormat.format(allStatus.get(2).getStartDate()));
    Assert.assertEquals("1990-03-31", dateFormat.format(allStatus.get(2).getEndDate()));
    
    Geometry expectedGeom = FastTestDataset.PROV_CENTRAL.fetchGeoObject().getGeometry();
    Geometry actualGeom = ( (AttributeGeometry) goTime.getAttributeOnDate(DefaultAttribute.GEOMETRY.getName(), FastTestDataset.DEFAULT_OVER_TIME_DATE) ).getValue();
    Assert.assertTrue(expectedGeom.equalsTopo(actualGeom));
  }
  
  /**
   * Tests to make sure if we set a value which is encompassed by a larger surrounding date range
   * that the operation is ignored.
   */
  @Test
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
    Assert.assertEquals("1990-01-01", dateFormat.format(allStatus.get(0).getStartDate()));
    Assert.assertEquals("1990-01-31", dateFormat.format(allStatus.get(0).getEndDate()));
    Assert.assertEquals("1990-02-01", dateFormat.format(allStatus.get(1).getStartDate()));
    Assert.assertEquals("1990-02-28", dateFormat.format(allStatus.get(1).getEndDate()));
    Assert.assertEquals("1990-03-01", dateFormat.format(allStatus.get(2).getStartDate()));
    Assert.assertEquals("1990-03-31", dateFormat.format(allStatus.get(2).getEndDate()));
    
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
    serverObj.getValuesOverTime(attributeName).clear();
    serverObj.setValue(attributeName, value, dateFormat.parse("1990-01-01"), dateFormat.parse("1990-02-01"));
    serverObj.apply(false);
    Assert.assertEquals(1, FastTestDataset.PROV_CENTRAL.getServerObject().getValuesOverTime(attributeName).size());
    
    // Set a value inside that date range with the same value
    VertexServerGeoObject serverObj2 = (VertexServerGeoObject) FastTestDataset.PROV_CENTRAL.getServerObject();
    serverObj2.setValue(attributeName, value, dateFormat.parse("1990-01-05"), dateFormat.parse("1990-01-10"));
    serverObj2.apply(false);
    
    // Fetch the object and assert values on it
    Assert.assertEquals(1, FastTestDataset.PROV_CENTRAL.getServerObject().getValuesOverTime(attributeName).size());
    
    GeoObjectOverTime goTime = client.getGeoObjectOverTimeByCode(FastTestDataset.PROV_CENTRAL.getCode(), FastTestDataset.PROV_CENTRAL.getGeoObjectType().getCode());
    
    Assert.assertEquals(1, goTime.getAllValues(attributeName).size());
    
    ValueOverTimeCollectionDTO all = goTime.getAllValues(attributeName);
    Assert.assertEquals("1990-01-01", dateFormat.format(all.get(0).getStartDate()));
    Assert.assertEquals("1990-02-01", dateFormat.format(all.get(0).getEndDate()));
    Assert.assertTrue(value.equals(all.get(0).getValue()));
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
  @SuppressWarnings("unchecked")
  private void addVersionData(TestGeoObjectInfo geoObj) throws ParseException
  {
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
    dateFormat.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);
    
    ServerGeoObjectIF serverObj = geoObj.getServerObject();
    serverObj.getValuesOverTime(DefaultAttribute.EXISTS.getName()).clear();
    
    serverObj.setExists(Boolean.FALSE, dateFormat.parse("01-01-1990"), dateFormat.parse("01-31-1990"));
    Assert.assertEquals(Boolean.FALSE, serverObj.getExists(dateFormat.parse("01-01-1990")));
    
    serverObj.setExists(Boolean.TRUE, dateFormat.parse("02-01-1990"), dateFormat.parse("02-28-1990"));
    Assert.assertEquals(Boolean.TRUE, serverObj.getExists(dateFormat.parse("02-01-1990")));
    
    serverObj.setExists(Boolean.FALSE, dateFormat.parse("03-01-1990"), dateFormat.parse("03-31-1990"));
    Assert.assertEquals(Boolean.FALSE, serverObj.getExists(dateFormat.parse("03-01-1990")));
    
    serverObj.apply(false);
    
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
