package net.geoprism.registry.test;

import org.commongeoregistry.adapter.constants.GeometryType;

import com.runwaysdk.constants.LocalProperties;

import net.geoprism.gis.geoserver.GeoserverFacade;
import net.geoprism.gis.geoserver.NullGeoserverService;

public class CambodiaTestData extends TestDataSet
{
  public final String                TEST_DATA_KEY    = "CAM";
  
  public final TestGeoObjectTypeInfo COUNTRY          = new TestGeoObjectTypeInfo(this, "Cambodia");

  public final TestGeoObjectTypeInfo STATE            = new TestGeoObjectTypeInfo(this, "Province");

  public final TestGeoObjectTypeInfo COUNTY           = new TestGeoObjectTypeInfo(this, "District");

  public final TestGeoObjectTypeInfo AREA             = new TestGeoObjectTypeInfo(this, "Commune");

  public final TestGeoObjectTypeInfo DISTRICT         = new TestGeoObjectTypeInfo(this, "Village", GeometryType.POINT);
  
//  public static CambodiaTestData buildForDev()
//  {
//    LocalProperties.setSkipCodeGenAndCompile(true);
//
//    TestRegistryAdapterClient adapter = new TestRegistryAdapterClient();
//
//    CambodiaTestData data = new CambodiaTestData(adapter, true);
//
//    return data;
//  }
  
  @Override
  public String getTestDataKey()
  {
    return TEST_DATA_KEY;
  }

}
