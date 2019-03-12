package net.geoprism.registry.io;

import java.util.HashMap;

import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.junit.Assert;
import org.junit.Test;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.registry.excel.MapFeatureRow;
import net.geoprism.registry.service.ServiceFactory;

public class PostalCodeFactoryTest
{

  @Test
  public void testCambodiaProvince()
  {
    GeoObjectType type = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType("Cambodia_Province").get();

    Assert.assertTrue(PostalCodeFactory.isAvailable(type));

    HashMap<String, Object> row = new HashMap<String, Object>();
    row.put("TEST", "20");

    MapFeatureRow feature = new MapFeatureRow(row);

    LocationBuilder builder = PostalCodeFactory.get(type);
    Location location = builder.build(new BasicColumnFunction("TEST"));

    String result = (String) location.getFunction().getValue(feature);

    Assert.assertEquals("855", result);
  }

  @Test
  public void testCambodiaDistrct()
  {
    GeoObjectType type = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType("Cambodia_District").get();

    Assert.assertTrue(PostalCodeFactory.isAvailable(type));

    HashMap<String, Object> row = new HashMap<String, Object>();
    row.put("TEST", "2004");

    MapFeatureRow feature = new MapFeatureRow(row);

    LocationBuilder builder = PostalCodeFactory.get(type);
    Location location = builder.build(new BasicColumnFunction("TEST"));

    String result = (String) location.getFunction().getValue(feature);

    Assert.assertEquals("855 20", result);
  }
  
  @Test
  public void testCambodiaCommune()
  {
    GeoObjectType type = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType("Cambodia_Commune").get();
    
    Assert.assertTrue(PostalCodeFactory.isAvailable(type));
    
    HashMap<String, Object> row = new HashMap<String, Object>();
    row.put("TEST", "120308");
    
    MapFeatureRow feature = new MapFeatureRow(row);
    
    LocationBuilder builder = PostalCodeFactory.get(type);
    Location location = builder.build(new BasicColumnFunction("TEST"));
    
    String result = (String) location.getFunction().getValue(feature);
    
    Assert.assertEquals("855 1203", result);
  }
  
  @Test
  public void testCambodiaVillage()
  {
    GeoObjectType type = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType("Cambodia_Village").get();
    
    Assert.assertTrue(PostalCodeFactory.isAvailable(type));
    
    HashMap<String, Object> row = new HashMap<String, Object>();
    row.put("TEST", "20071103");
    
    MapFeatureRow feature = new MapFeatureRow(row);
    
    LocationBuilder builder = PostalCodeFactory.get(type);
    Location location = builder.build(new BasicColumnFunction("TEST"));
    
    String result = (String) location.getFunction().getValue(feature);
    
    Assert.assertEquals("855 200711", result);
  }
}
