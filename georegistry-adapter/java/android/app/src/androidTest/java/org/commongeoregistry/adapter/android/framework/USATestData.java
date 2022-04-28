package org.commongeoregistry.adapter.android.framework;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.commongeoregistry.adapter.HttpRegistryClient;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.http.AuthenticationException;
import org.commongeoregistry.adapter.http.ServerResponseException;
import org.commongeoregistry.adapter.metadata.HierarchyType;

/**
 * This class contains logic for creating and retrieving test objects in reusable, predictable ways.
 * It also contains logic for assertion of test data.
 */
public class USATestData
{
  public static final String TEST_DATA_KEY = "USATestData";

  public final TestHierarchyTypeInfo LOCATED_IN = new TestHierarchyTypeInfo("LocatedIn");

  public final TestHierarchyTypeInfo ALLOWED_IN = new TestHierarchyTypeInfo("AllowedIn");
  
  public final TestGeoObjectTypeInfo COUNTRY = new TestGeoObjectTypeInfo(this, "Country", GeometryType.MULTIPOLYGON);

  public final TestGeoObjectTypeInfo STATE = new TestGeoObjectTypeInfo(this, "State", GeometryType.MULTIPOLYGON);
  
  public final TestGeoObjectTypeInfo DISTRICT = new TestGeoObjectTypeInfo(this, "District", GeometryType.POINT);

  public final TestGeoObjectInfo USA = new TestGeoObjectInfo(this, "USA", COUNTRY);
  
  public final TestGeoObjectInfo COLORADO = new TestGeoObjectInfo(this, "Colorado", STATE);
  
  public final TestGeoObjectInfo CO_D_ONE = new TestGeoObjectInfo(this, "ColoradoDistrictOne", DISTRICT);
  
  public final TestGeoObjectInfo CO_D_TWO = new TestGeoObjectInfo(this, "ColoradoDistrictTwo", DISTRICT);
  
  public final TestGeoObjectInfo CO_D_THREE = new TestGeoObjectInfo(this, "ColoradoDistrictThree", DISTRICT);
  
  public final TestGeoObjectInfo WASHINGTON = new TestGeoObjectInfo(this, "Washington", STATE, "POLYGON((1 1,5 1,5 5,1 5,1 1),(2 2, 3 2, 3 3, 2 3,2 2))");
  
  public final TestGeoObjectInfo WA_D_ONE = new TestGeoObjectInfo(this, "WashingtonDistrictOne", DISTRICT);
  
  public final TestGeoObjectInfo WA_D_TWO = new TestGeoObjectInfo(this, "WashingtonDistrictTwo", DISTRICT);

  public ArrayList<TestGeoObjectTypeInfo> GEOOBJECTTYPES = new ArrayList<TestGeoObjectTypeInfo>(Arrays.asList(new TestGeoObjectTypeInfo[]{COUNTRY, STATE, DISTRICT}));
  
  public ArrayList<TestGeoObjectInfo> GEOOBJECTS = new ArrayList<TestGeoObjectInfo>(Arrays.asList(new TestGeoObjectInfo[]{USA, COLORADO, WASHINGTON, CO_D_ONE, CO_D_TWO, CO_D_THREE, WA_D_ONE, WA_D_TWO}));
  
  public HttpRegistryClient client;
  
  public USATestData(HttpRegistryClient client)
  {
    this.client = client;
  }

  public void setUp() throws AuthenticationException, ServerResponseException, IOException {
    for (TestGeoObjectInfo geo : GEOOBJECTS)
    {
      geo.fetchUid();
    }

    COUNTRY.addChild(STATE);
    STATE.addChild(DISTRICT);

    USA.addChild(COLORADO);
    COLORADO.addChild(CO_D_ONE);
    COLORADO.addChild(CO_D_TWO);
    COLORADO.addChild(CO_D_THREE);

    USA.addChild(WASHINGTON);
    WASHINGTON.addChild(WA_D_ONE);
    WASHINGTON.addChild(WA_D_TWO);
  }

  public class TestHierarchyTypeInfo
  {
    private String code;

    private TestHierarchyTypeInfo(String code)
    {
      this.code = code;
    }

    public String getCode()
    {
      return code;
    }

    public void setCode(String code)
    {
      this.code = code;
    }
  }

  public TestGeoObjectInfo newTestGeoObjectInfo(String genKey, TestGeoObjectTypeInfo testUni)
  {
    TestGeoObjectInfo info = new TestGeoObjectInfo(this, genKey, testUni);
    
    info.delete();
    
    this.GEOOBJECTS.add(info);
    
    return info;
  }
  
  public TestGeoObjectInfo newTestGeoObjectInfo(String genKey, TestGeoObjectTypeInfo testUni, String wkt)
  {
    TestGeoObjectInfo info = new TestGeoObjectInfo(this, genKey, testUni, wkt);
    
    info.delete();
    
    this.GEOOBJECTS.add(info);
    
    return info;
  }
  
  public TestGeoObjectTypeInfo newTestGeoObjectTypeInfo(String genKey)
  {
    TestGeoObjectTypeInfo info = new TestGeoObjectTypeInfo(this, genKey);

    info.delete();

    this.GEOOBJECTTYPES.add(info);

    return info;
  }

    public static void assertEqualsHierarchyType(String relationshipType, HierarchyType compare)
    {
        // TODO

//        MdRelationship allowedIn = MdRelationship.getMdRelationship(relationshipType);
//
//        Assert.assertEquals(allowedIn.getKey(), compare.getCode());
//        Assert.assertEquals(allowedIn.getDescription().getValue(), compare.getLocalizedDescription());
//        Assert.assertEquals(allowedIn.getDisplayLabel().getValue(), compare.getLocalizedLabel());

//    compare.getRootGeoObjectTypes() // TODO
    }

}
