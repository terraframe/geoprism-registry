package net.geoprism.georegistry;

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
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
    GeoObject geoObj = data.registryService.getGeoObject(data.systemSession.getSessionId(), USATestData.COLORADO.getUid());
    
    USATestData.COLORADO.assertEquals(geoObj);
  }
  
  @Test
  @Request
  public void testUpdateGeoObject()
  {
    USATestData.WASHINGTON.delete();
    
    // 1. Test creating a new one
    GeoObject geoObj = RegistryService.getRegistryAdapter().newGeoObjectInstance(USATestData.STATE.getCode());
    geoObj.setWKTGeometry(USATestData.WASHINGTON.getWkt());
    geoObj.setCode(USATestData.WASHINGTON.getGeoId());
//    geoObj.setUid(USATestData.WASHINGTON.getGeoId()); // TODO : This should be set by the API
    geoObj.setLocalizedDisplayLabel(USATestData.WASHINGTON.getDisplayLabel());
    data.registryService.updateGeoObject(data.systemSession.getSessionId(), geoObj.toJSON().toString());
    
    GeoEntity waGeo = GeoEntity.getByKey(USATestData.WASHINGTON.getGeoId());
    Assert.assertEquals(StringUtils.deleteWhitespace(USATestData.WASHINGTON.getWkt()), StringUtils.deleteWhitespace(waGeo.getWkt()));
    Assert.assertEquals(USATestData.WASHINGTON.getGeoId(), waGeo.getGeoId());
//    Assert.assertEquals(USATestData.WASHINGTON.getGeoId(), waGeo.getOid()); // TODO : check the uid?
    Assert.assertEquals(USATestData.WASHINGTON.getDisplayLabel(), waGeo.getDisplayLabel().getValue());
    
    // 2. Test updating the one we created earlier
    GeoObject waGeoObj = data.registryService.getGeoObject(data.systemSession.getSessionId(), waGeo.getOid());
    waGeoObj.setWKTGeometry(USATestData.COLORADO.getWkt());
    waGeoObj.setLocalizedDisplayLabel(USATestData.COLORADO.getDisplayLabel());
    data.registryService.updateGeoObject(data.systemSession.getSessionId(), waGeoObj.toJSON().toString());
    
    GeoEntity waGeo2 = GeoEntity.getByKey(USATestData.WASHINGTON.getGeoId());
    Assert.assertEquals(StringUtils.deleteWhitespace(USATestData.COLORADO.getWkt()), StringUtils.deleteWhitespace(waGeo2.getWkt()));
    Assert.assertEquals(USATestData.COLORADO.getDisplayLabel(), waGeo2.getDisplayLabel().getValue());
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
    String[] codes = new String[]{ USATestData.STATE.getCode(), USATestData.DISTRICT.getCode() };
    
    GeoObjectType[] gots = data.registryService.getGeoObjectTypes(data.systemSession.getSessionId(), codes);
    
    Assert.assertEquals(codes.length, gots.length);
    
    GeoObjectType state = gots[0];
//    Assert.assertEquals(USATestData.STATE_UID, state.get); // TODO : GeoOBjectTypes don't have a uid?
    Assert.assertEquals(USATestData.STATE.getCode(), state.getCode());
    Assert.assertEquals(USATestData.STATE.getDisplayLabel(), state.getLocalizedLabel());
    Assert.assertEquals(USATestData.STATE.getDescription(), state.getLocalizedDescription());
    
    GeoObjectType district = gots[1];
//  Assert.assertEquals(USATestData.DISTRICT_UID, district.get); // TODO : GeoOBjectTypes don't have a uid?
    Assert.assertEquals(USATestData.DISTRICT.getCode(), district.getCode());
    Assert.assertEquals(USATestData.DISTRICT.getDisplayLabel(), district.getLocalizedLabel());
    Assert.assertEquals(USATestData.DISTRICT.getDescription(), district.getLocalizedDescription());
  }
  
  @Test
  @Request
  public void testGetChildGeoObjects()
  {
    String parentId = USATestData.USA.getUid();
    String[] childrenTypes = new String[]{USATestData.STATE.getCode(), USATestData.DISTRICT.getCode()};
    
    // Recursive
    ChildTreeNode tn = data.registryService.getChildGeoObjects(data.systemSession.getSessionId(), parentId, childrenTypes, true);
    USATestData.USA.assertEquals(tn, childrenTypes, true);
    
    // Not recursive
    ChildTreeNode tn2 = data.registryService.getChildGeoObjects(data.systemSession.getSessionId(), parentId, childrenTypes, false);
    USATestData.USA.assertEquals(tn2, childrenTypes, false);
    
    // Test only getting districts
    String[] distArr = new String[]{USATestData.DISTRICT.getCode()};
    ChildTreeNode tn3 = data.registryService.getChildGeoObjects(data.systemSession.getSessionId(), parentId, distArr, true);
    USATestData.USA.assertEquals(tn3, distArr, true);
  }
  
  @Test
  @Request
  public void testGetParentGeoObjects()
  {
    String childId = USATestData.CO_D_TWO.getUid();
    String[] childrenTypes = new String[]{USATestData.COUNTRY.getCode(), USATestData.STATE.getCode()};
    
    // Recursive
    ParentTreeNode tn = data.registryService.getParentGeoObjects(data.systemSession.getSessionId(), childId, childrenTypes, true);
    USATestData.CO_D_TWO.assertEquals(tn, childrenTypes, true);
    
    // Not recursive
    ParentTreeNode tn2 = data.registryService.getParentGeoObjects(data.systemSession.getSessionId(), childId, childrenTypes, false);
    USATestData.CO_D_TWO.assertEquals(tn2, childrenTypes, false);
    
    // Test only getting countries
    String[] countryArr = new String[]{USATestData.COUNTRY.getCode()};
    ChildTreeNode tn3 = data.registryService.getChildGeoObjects(data.systemSession.getSessionId(), childId, countryArr, true);
    USATestData.CO_D_TWO.assertEquals(tn3, countryArr, true);
  }
}
