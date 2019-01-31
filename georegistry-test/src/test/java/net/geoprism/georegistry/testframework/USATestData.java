package net.geoprism.georegistry.testframework;

import java.util.Locale;

import org.commongeoregistry.adapter.constants.GeometryType;

import com.runwaysdk.ClientSession;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.constants.LocalProperties;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.gis.geo.Universal;

import net.geoprism.georegistry.service.ConversionService;
import net.geoprism.georegistry.service.RegistryService;

public class USATestData extends TestDataSet
{
  public final String                      TEST_DATA_KEY      = "USATestData";

  public final TestGeoObjectTypeInfo       COUNTRY            = new TestGeoObjectTypeInfo("Country");

  public final TestGeoObjectTypeInfo       STATE              = new TestGeoObjectTypeInfo("State");

  public final TestGeoObjectTypeInfo       DISTRICT           = new TestGeoObjectTypeInfo("District", true);

  public final TestGeoObjectInfo           USA                = new TestGeoObjectInfo("USA", COUNTRY);

  public final TestGeoObjectInfo           COLORADO           = new TestGeoObjectInfo("Colorado", STATE);

  public final TestGeoObjectInfo           CO_D_ONE           = new TestGeoObjectInfo("ColoradoDistrictOne", DISTRICT);

  public final TestGeoObjectInfo           CO_D_TWO           = new TestGeoObjectInfo("ColoradoDistrictTwo", DISTRICT);

  public final TestGeoObjectInfo           CO_D_THREE         = new TestGeoObjectInfo("ColoradoDistrictThree", DISTRICT);

  public final TestGeoObjectInfo           WASHINGTON         = new TestGeoObjectInfo("Washington", STATE, "POLYGON((1 1,5 1,5 5,1 5,1 1),(2 2, 3 2, 3 3, 2 3,2 2))");

  public final TestGeoObjectInfo           WA_D_ONE           = new TestGeoObjectInfo("WashingtonDistrictOne", DISTRICT);

  public final TestGeoObjectInfo           WA_D_TWO           = new TestGeoObjectInfo("WashingtonDistrictTwo", DISTRICT);

  {
    managedGeoObjectTypeInfos.add(COUNTRY);
    managedGeoObjectTypeInfos.add(STATE);
    managedGeoObjectTypeInfos.add(DISTRICT);
    
    managedGeoObjectInfos.add(USA);
    managedGeoObjectInfos.add(COLORADO);
    managedGeoObjectInfos.add(WASHINGTON);
    managedGeoObjectInfos.add(CO_D_ONE);
    managedGeoObjectInfos.add(CO_D_TWO);
    managedGeoObjectInfos.add(CO_D_THREE);
    managedGeoObjectInfos.add(WA_D_ONE);
    managedGeoObjectInfos.add(WA_D_TWO);
  }

  public static USATestData newTestData()
  {
    return USATestData.newTestData(GeometryType.POLYGON, true);
  }

  @Request
  public static USATestData newTestData(GeometryType geometryType, boolean includeData)
  {
    LocalProperties.setSkipCodeGenAndCompile(true);

    TestRegistryAdapterClient adapter = new TestRegistryAdapterClient();

    USATestData data = new USATestData(adapter, geometryType, includeData);
//    data.setDebugMode(2);
    data.setUp();

    RegistryService.getInstance().refreshMetadataCache();
    
    adapter.setClientRequest(data.adminClientRequest);
    adapter.refreshMetadataCache();
    adapter.getIdSerivce().populate(1000);

    return data;
  }

  public USATestData(TestRegistryAdapterClient adapter, GeometryType geometryType, boolean includeData)
  {
    this.adapter = adapter;
    this.geometryType = geometryType;
    this.includeData = includeData;
  }

  @Transaction
  protected void setUpInTrans()
  {
    for (TestGeoObjectTypeInfo uni : managedGeoObjectTypeInfos)
    {
      uni.apply(this.geometryType);
    }

    COUNTRY.getUniversal().addLink(Universal.getRoot(), AllowedIn.CLASS);
    COUNTRY.addChild(STATE, AllowedIn.CLASS);
    STATE.addChild(DISTRICT, AllowedIn.CLASS);

    ConversionService.addParentReferenceToLeafType(LocatedIn.class.getSimpleName(), STATE.getUniversal(), DISTRICT.getUniversal());

    if (this.includeData)
    {
      for (TestGeoObjectInfo geo : managedGeoObjectInfos)
      {
        geo.apply();
      }

      USA.getGeoEntity().addLink(GeoEntity.getRoot(), LocatedIn.CLASS);

      USA.addChild(COLORADO, LocatedIn.CLASS);
      COLORADO.addChild(CO_D_ONE, LocatedIn.CLASS);
      COLORADO.addChild(CO_D_TWO, LocatedIn.CLASS);
      COLORADO.addChild(CO_D_THREE, LocatedIn.CLASS);

      USA.addChild(WASHINGTON, LocatedIn.CLASS);
      WASHINGTON.addChild(WA_D_ONE, LocatedIn.CLASS);
      WASHINGTON.addChild(WA_D_TWO, LocatedIn.CLASS);
    }

    adminSession = ClientSession.createUserSession("admin", "_nm8P4gfdWxGqNRQ#8", new Locale[] { CommonProperties.getDefaultLocale() });
    adminClientRequest = adminSession.getRequest();
  }

  @Transaction
  public void cleanUpInTrans()
  {
    if (STATE.getUniversal() != null && DISTRICT.getUniversal() != null)
    {
      ConversionService.removeParentReferenceToLeafType(LocatedIn.class.getSimpleName(), STATE.getUniversal(), DISTRICT.getUniversal());
    }
    
    super.cleanUpInTrans();
  }

  @Override
  public String getTestDataKey()
  {
    return TEST_DATA_KEY;
  }
}
