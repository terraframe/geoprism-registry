package net.geoprism.georegistry;

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.GeoEntity;

import net.geoprism.georegistry.service.IdService;
import net.geoprism.georegistry.service.RegistryService;

public class RegistryServiceTest
{
  protected static USATestData data;
  
  @Before
  public void setUp()
  {
    data = new USATestData();
    
    data.setUp();
  }
  
  @After
  public void tearDown()
  {
    data.cleanUp();
  }
  
  @Test
  public void testGetGeoObject()
  {
    GeoObject geoObj = data.registryService.getGeoObject(data.systemSession.getSessionId(), USATestData.COLORADO_UID);
    
    Assert.assertEquals(USATestData.COLORADO_UID, geoObj.getUid());
    Assert.assertEquals(USATestData.COLORADO_GEOID, geoObj.getCode());
    Assert.assertEquals(USATestData.COLORADO_WKT, geoObj.getGeometry().toText());
    Assert.assertEquals(USATestData.COLORADO_DISPLAY_LABEL, geoObj.getLocalizedDisplayLabel());
    Assert.assertEquals(USATestData.STATE_CODE, geoObj.getType().getCode());
  }
  
  @Test
  @Request
  public void testUpdateGeoObject()
  {
    // 1. Test creating a new one
    GeoObject geoObj = RegistryService.getRegistryAdapter().newGeoObjectInstance(USATestData.STATE_CODE);
    geoObj.setWKTGeometry(USATestData.WASHINGTON_WKT);
    geoObj.setCode(USATestData.WASHINGTON_GEOID);
//    geoObj.setUid(USATestData.WASHINGTON_GEOID); // TODO : This should be set by the API
    geoObj.setLocalizedDisplayLabel(USATestData.WASHINGTON_DISPLAY_LABEL);
    data.registryService.updateGeoObject(data.systemSession.getSessionId(), geoObj.toJSON().toString());
    
    GeoEntity waGeo = GeoEntity.getByKey(USATestData.WASHINGTON_GEOID);
    Assert.assertEquals(StringUtils.deleteWhitespace(USATestData.WASHINGTON_WKT), StringUtils.deleteWhitespace(waGeo.getWkt()));
    Assert.assertEquals(USATestData.WASHINGTON_GEOID, waGeo.getGeoId());
//    Assert.assertEquals(USATestData.WASHINGTON_GEOID, waGeo.getOid()); // TODO : check the uid?
    Assert.assertEquals(USATestData.WASHINGTON_DISPLAY_LABEL, waGeo.getDisplayLabel().getValue());
    
    // 2. Test updating the one we created earlier
    GeoObject waGeoObj = data.registryService.getGeoObject(data.systemSession.getSessionId(), waGeo.getOid());
    waGeoObj.setWKTGeometry(USATestData.COLORADO_WKT);
    waGeoObj.setLocalizedDisplayLabel(USATestData.COLORADO_DISPLAY_LABEL);
    data.registryService.updateGeoObject(data.systemSession.getSessionId(), waGeoObj.toJSON().toString());
    
    GeoEntity waGeo2 = GeoEntity.getByKey(USATestData.WASHINGTON_GEOID);
    Assert.assertEquals(StringUtils.deleteWhitespace(USATestData.COLORADO_WKT), StringUtils.deleteWhitespace(waGeo2.getWkt()));
    Assert.assertEquals(USATestData.COLORADO_DISPLAY_LABEL, waGeo2.getDisplayLabel().getValue());
  }
  
  @Test
  public void testGetUIDS()
  {
    String[] ids = data.registryService.getUIDS(data.systemSession.getSessionId(), 100);
    
    Assert.assertEquals(100, ids.length);
    
    for (String id : ids)
    {
      Assert.assertTrue(IdService.getInstance(data.systemSession.getSessionId()).isIssuedId(id));
    }
  }
  
  @Test
  public void testGetGeoObjectTypes()
  {
    String[] codes = new String[]{ USATestData.STATE_CODE, USATestData.DISTRICT_CODE };
    
    GeoObjectType[] gots = data.registryService.getGeoObjectTypes(data.systemSession.getSessionId(), codes);
    
    Assert.assertEquals(2, gots.length);
    
    GeoObjectType state = gots[0];
//    Assert.assertEquals(USATestData.STATE_UID, state.get); // TODO : GeoOBjectTypes don't have a uid?
    Assert.assertEquals(USATestData.STATE_CODE, state.getCode());
    Assert.assertEquals(USATestData.STATE_DISPLAY_LABEL, state.getLocalizedLabel());
    Assert.assertEquals(USATestData.STATE_DESCRIPTION, state.getLocalizedDescription());
    
    GeoObjectType district = gots[1];
//  Assert.assertEquals(USATestData.DISTRICT_UID, district.get); // TODO : GeoOBjectTypes don't have a uid?
    Assert.assertEquals(USATestData.DISTRICT_CODE, district.getCode());
    Assert.assertEquals(USATestData.DISTRICT_DISPLAY_LABEL, district.getLocalizedLabel());
    Assert.assertEquals(USATestData.DISTRICT_DESCRIPTION, district.getLocalizedDescription());
  }
}
