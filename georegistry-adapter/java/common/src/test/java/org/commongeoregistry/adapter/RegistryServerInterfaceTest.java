/**
 *
 */
package org.commongeoregistry.adapter;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class RegistryServerInterfaceTest
{
  private static RegistryAdapterServer registryServerInterface;
  
  public RegistryServerInterfaceTest()
  {
    super();
  }

  @BeforeClass
  public static void classSetUp()
  {
    registryServerInterface = new RegistryAdapterServer(new MockIdService());
    
    TestFixture.defineExampleHierarchies(registryServerInterface);
  }
  
  @AfterClass
  public static void classTeardown()
  {
    registryServerInterface.getMetadataCache().rebuild();
  }
  
  @Test
  public void testDefinedGeoTypes()
  {
    Assert.assertNotNull(TestFixture.PROVINCE+" was not found in the cache", registryServerInterface.getMetadataCache().getGeoObjectType(TestFixture.PROVINCE).get());

    Assert.assertNotNull(TestFixture.DISTRICT+" was not found in the cache", registryServerInterface.getMetadataCache().getGeoObjectType(TestFixture.DISTRICT).get());
    
    Assert.assertNotNull(TestFixture.COMMUNE+" was not found in the cache", registryServerInterface.getMetadataCache().getGeoObjectType(TestFixture.COMMUNE).get());
    
    Assert.assertNotNull(TestFixture.VILLAGE+" was not found in the cache", registryServerInterface.getMetadataCache().getGeoObjectType(TestFixture.VILLAGE).get());
    
    Assert.assertNotNull(TestFixture.HOUSEHOLD+" was not found in the cache", registryServerInterface.getMetadataCache().getGeoObjectType(TestFixture.HOUSEHOLD).get());
    
//    Assert.assertNotNull(TestFixture.FOCUS_AREA+" was not found in the cache", registryServerInterface.getMetadataCache().getGeoObjectType(TestFixture.FOCUS_AREA).get());
    
    Assert.assertNotNull(TestFixture.HEALTH_FACILITY+" was not found in the cache", registryServerInterface.getMetadataCache().getGeoObjectType(TestFixture.HEALTH_FACILITY).get());
  }
  
  @Test
  public void testDefaultAttributes()
  { 
    Optional<GeoObjectType> geoObjectType = registryServerInterface.getMetadataCache().getGeoObjectType(TestFixture.PROVINCE);
    
    Assert.assertNotNull(TestFixture.PROVINCE+" was not found in the cache", geoObjectType.get());
    
    GeoObjectType province = geoObjectType.get();
    
    for (DefaultAttribute defaultAttribute : DefaultAttribute.values())
    {
      if (defaultAttribute.equals(DefaultAttribute.GEOMETRY)) { continue; }
      
      AttributeType attributeType = province.getAttribute(defaultAttribute.getName()).get();
      
      Assert.assertNotNull(defaultAttribute.getName()+"  was not defined as a default attribute", attributeType);
    }
  }
  
  
  @Test
  public void testCreateGeoObject()
  {
	  
	String geom = "POLYGON ((10000 10000, 12300 40000, 16800 50000, 12354 60000, 13354 60000, 17800 50000, 13300 40000, 11000 10000, 10000 10000))";
    
	GeoObject geoObject = registryServerInterface.newGeoObjectInstance(TestFixture.HEALTH_FACILITY);
    geoObject.setWKTGeometry(geom);
    
    geoObject.printAttributes();
    
  }
  
//  @Test
//  public void testHealthFacilityTypes()
//  { 
//    GeoObjectType healthFacility = registryServerInterface.getMetadataCache().getGeoObjectType(HEALTH_FACILITY).get();
//    
//    AttributeTermType attributeTermType = (AttributeTermType)healthFacility.getAttribute(HEALTH_FACILITY_ATTRIBUTE).get();
//    
//    List<Term> terms = attributeTermType.getTerms();
//  }
  
}
