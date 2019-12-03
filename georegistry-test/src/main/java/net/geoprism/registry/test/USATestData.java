/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.test;

import java.util.Locale;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;

import com.runwaysdk.ClientSession;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.constants.LocalProperties;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.gis.geo.Universal;

import net.geoprism.gis.geoserver.GeoserverFacade;
import net.geoprism.gis.geoserver.NullGeoserverService;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.RegistryService;

public class USATestData extends TestDataSet
{
  public final String                TEST_DATA_KEY    = "USATestData";

  public final TestGeoObjectTypeInfo COUNTRY          = new TestGeoObjectTypeInfo(this, "Country");

  public final TestGeoObjectTypeInfo STATE            = new TestGeoObjectTypeInfo(this, "State");

  public final TestGeoObjectTypeInfo COUNTY           = new TestGeoObjectTypeInfo(this, "County");

  public final TestGeoObjectTypeInfo AREA             = new TestGeoObjectTypeInfo(this, "Area");

  public final TestGeoObjectTypeInfo DISTRICT         = new TestGeoObjectTypeInfo(this, "District", GeometryType.POINT);

  public final TestGeoObjectInfo     USA              = new TestGeoObjectInfo(this, "USA", COUNTRY);

  public final TestGeoObjectInfo     CANADA           = new TestGeoObjectInfo(this, "CANADA", COUNTRY);

  public final TestGeoObjectInfo     COLORADO         = new TestGeoObjectInfo(this, "Colorado", STATE);

  public final TestGeoObjectInfo     CO_D_ONE         = new TestGeoObjectInfo(this, "ColoradoDistrictOne", DISTRICT);

  public final TestGeoObjectInfo     CO_D_TWO         = new TestGeoObjectInfo(this, "ColoradoDistrictTwo", DISTRICT);

  public final TestGeoObjectInfo     CO_D_THREE       = new TestGeoObjectInfo(this, "ColoradoDistrictThree", DISTRICT);

  public final TestGeoObjectInfo     CO_C_ONE         = new TestGeoObjectInfo(this, "ColoradoCountyOne", COUNTY);

  public final TestGeoObjectInfo     CO_A_ONE         = new TestGeoObjectInfo(this, "ColoradoAreaOne", AREA);

  public final TestGeoObjectInfo     WASHINGTON       = new TestGeoObjectInfo(this, "Washington", STATE, "POLYGON((1 1,5 1,5 5,1 5,1 1),(2 2, 3 2, 3 3, 2 3,2 2))");

  public final TestGeoObjectInfo     WA_D_ONE         = new TestGeoObjectInfo(this, "WashingtonDistrictOne", DISTRICT);

  public final TestGeoObjectInfo     WA_D_TWO         = new TestGeoObjectInfo(this, "WashingtonDistrictTwo", DISTRICT);

  /**
   * The Mexico Hierarchy cannot have any leaf nodes in it.
   */

  public final TestGeoObjectTypeInfo MEXICO_CITY_GOT  = new TestGeoObjectTypeInfo(this, "Mexico_City_GOT");

  public final TestGeoObjectTypeInfo MEXICO_STATE     = new TestGeoObjectTypeInfo(this, "Mexico_State_GOT");

  public final TestGeoObjectInfo     MEXICO           = new TestGeoObjectInfo(this, "Mexico", COUNTRY);

  public final TestGeoObjectInfo     MEXICO_CITY_ONE  = new TestGeoObjectInfo(this, "Mexico City One", MEXICO_CITY_GOT);

  public final TestGeoObjectInfo     MEXICO_CITY_TWO  = new TestGeoObjectInfo(this, "Mexico City Two", MEXICO_CITY_GOT);

  public final TestGeoObjectInfo     MEXICO_STATE_ONE = new TestGeoObjectInfo(this, "Mexico State One", MEXICO_STATE);

  public final TestGeoObjectInfo     MEXICO_STATE_TWO = new TestGeoObjectInfo(this, "Mexico State Two", MEXICO_STATE);

  {
    managedGeoObjectTypeInfos.add(COUNTRY);
    managedGeoObjectTypeInfos.add(STATE);
    managedGeoObjectTypeInfos.add(DISTRICT);
    managedGeoObjectTypeInfos.add(COUNTY);
    managedGeoObjectTypeInfos.add(AREA);

    managedGeoObjectInfos.add(USA);
    managedGeoObjectInfos.add(CANADA);
    managedGeoObjectInfos.add(COLORADO);
    managedGeoObjectInfos.add(WASHINGTON);
    managedGeoObjectInfos.add(CO_D_ONE);
    managedGeoObjectInfos.add(CO_D_TWO);
    managedGeoObjectInfos.add(CO_D_THREE);
    managedGeoObjectInfos.add(CO_C_ONE);
    managedGeoObjectInfos.add(CO_A_ONE);
    managedGeoObjectInfos.add(WA_D_ONE);
    managedGeoObjectInfos.add(WA_D_TWO);

    managedGeoObjectTypeInfos.add(MEXICO_STATE);
    managedGeoObjectTypeInfos.add(MEXICO_CITY_GOT);
    managedGeoObjectInfos.add(MEXICO);
    managedGeoObjectInfos.add(MEXICO_CITY_ONE);
    managedGeoObjectInfos.add(MEXICO_CITY_TWO);
    managedGeoObjectInfos.add(MEXICO_STATE_ONE);
    managedGeoObjectInfos.add(MEXICO_STATE_TWO);
  }

  public static USATestData newTestDataForClass()
  {
    LocalProperties.setSkipCodeGenAndCompile(true);
    GeoserverFacade.setService(new NullGeoserverService());

    TestRegistryAdapterClient adapter = new TestRegistryAdapterClient();

    USATestData data = new USATestData(adapter, true);

    return data;
  }

  public static USATestData newTestData()
  {
    return USATestData.newTestData(true);
  }

  @Request
  public static USATestData newTestData(boolean includeData)
  {
    LocalProperties.setSkipCodeGenAndCompile(true);
    GeoserverFacade.setService(new NullGeoserverService());

    TestRegistryAdapterClient adapter = new TestRegistryAdapterClient();

    USATestData data = new USATestData(adapter, includeData);
    // data.setDebugMode(2);
    data.setUp();

    RegistryService.getInstance().refreshMetadataCache();

    adapter.setClientRequest(data.adminClientRequest);
    adapter.refreshMetadataCache();

    try
    {
      adapter.getIdService().populate(1000);
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }

    return data;
  }

  public USATestData(TestRegistryAdapterClient adapter, boolean includeData)
  {
    this.adapter = adapter;
    this.includeData = includeData;
  }

  @Transaction
  @Override
  protected void setUpClassInTrans()
  {
    for (TestGeoObjectTypeInfo uni : managedGeoObjectTypeInfos)
    {
      uni.apply();
    }

    COUNTRY.getUniversal().addLink(Universal.getRoot(), AllowedIn.CLASS);
    COUNTRY.addChild(STATE, AllowedIn.CLASS);
    STATE.addChild(DISTRICT, AllowedIn.CLASS);
    STATE.addChild(COUNTY, AllowedIn.CLASS);
    COUNTY.addChild(AREA, AllowedIn.CLASS);

    COUNTRY.addChild(MEXICO_STATE, AllowedIn.CLASS);
    MEXICO_STATE.addChild(MEXICO_CITY_GOT, AllowedIn.CLASS);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());
    hierarchyType.addParentReferenceToLeafType(STATE.getUniversal(), DISTRICT.getUniversal());

    adminSession = ClientSession.createUserSession(ADMIN_USER_NAME, ADMIN_PASSWORD, new Locale[] { CommonProperties.getDefaultLocale() });
    adminClientRequest = adminSession.getRequest();
  }

  @Transaction
  @Override
  protected void setUpTestInTrans()
  {
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
      COLORADO.addChild(CO_C_ONE, LocatedIn.CLASS);
      CO_C_ONE.addChild(CO_A_ONE, LocatedIn.CLASS);

      USA.addChild(WASHINGTON, LocatedIn.CLASS);
      WASHINGTON.addChild(WA_D_ONE, LocatedIn.CLASS);
      WASHINGTON.addChild(WA_D_TWO, LocatedIn.CLASS);

      MEXICO.addChild(MEXICO_STATE_ONE, LocatedIn.CLASS);
      MEXICO.addChild(MEXICO_STATE_TWO, LocatedIn.CLASS);
      MEXICO_STATE_TWO.addChild(MEXICO_CITY_ONE, LocatedIn.CLASS);
      MEXICO_STATE_TWO.addChild(MEXICO_CITY_TWO, LocatedIn.CLASS);
    }

    adminSession = ClientSession.createUserSession(TestDataSet.ADMIN_USER_NAME, TestDataSet.ADMIN_PASSWORD, new Locale[] { CommonProperties.getDefaultLocale() });
    adminClientRequest = adminSession.getRequest();
  }

  @Transaction
  @Override
  public void cleanUpClassInTrans()
  {
    if (STATE.getUniversal() != null && DISTRICT.getUniversal() != null)
    {
      ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());
      hierarchyType.removeParentReferenceToLeafType(STATE.getUniversal(), DISTRICT.getUniversal());
    }

    super.cleanUpClassInTrans();
  }

  @Override
  public String getTestDataKey()
  {
    return TEST_DATA_KEY;
  }

  @Request
  public void refreshTerms(AttributeTermType attribute)
  {
    attribute.setRootTerm(new TermConverter(TermConverter.buildClassifierKeyFromTermCode(attribute.getRootTerm().getCode())).build());
  }
}
