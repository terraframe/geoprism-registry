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

import java.util.ArrayList;

import org.commongeoregistry.adapter.constants.DefaultTerms;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;

public class USATestData extends TestDataSet
{
  public static final String                TEST_DATA_KEY    = "USATestData";

  public static final TestOrganizationInfo  ORG_NPS          = new TestOrganizationInfo(TEST_DATA_KEY + "NPS");

  public static final TestOrganizationInfo  ORG_PPP          = new TestOrganizationInfo(TEST_DATA_KEY + "PPP");

  public static final TestUserInfo          USER_NPS_RA      = new TestUserInfo(TEST_DATA_KEY + "_" + "npsra", "npsra", ORG_NPS.getCode() + "@noreply.com", new String[] { RegistryRole.Type.getRA_RoleName(ORG_NPS.getCode()) });

  public static final TestUserInfo          USER_PPP_RA      = new TestUserInfo(TEST_DATA_KEY + "_" + "pppra", "pppra", ORG_PPP.getCode()+ "@noreply.com", new String[] { RegistryRole.Type.getRA_RoleName(ORG_PPP.getCode()) });

  public static final TestHierarchyTypeInfo HIER_ADMIN       = new TestHierarchyTypeInfo(TEST_DATA_KEY + "Admin", ORG_NPS);

  public static final TestGeoObjectTypeInfo COUNTRY          = new TestGeoObjectTypeInfo(TEST_DATA_KEY + "Country", GeometryType.MULTIPOLYGON, ORG_NPS);

  public static final TestGeoObjectTypeInfo STATE            = new TestGeoObjectTypeInfo(TEST_DATA_KEY + "State", GeometryType.MULTIPOLYGON, ORG_NPS);

  public static final TestGeoObjectTypeInfo COUNTY           = new TestGeoObjectTypeInfo(TEST_DATA_KEY + "County", GeometryType.MULTIPOLYGON, ORG_NPS);

  public static final TestGeoObjectTypeInfo AREA             = new TestGeoObjectTypeInfo(TEST_DATA_KEY + "Area", GeometryType.POLYGON, ORG_NPS);

  public static final TestGeoObjectTypeInfo DISTRICT         = new TestGeoObjectTypeInfo(TEST_DATA_KEY + "District", GeometryType.POINT, ORG_NPS);

  public static final TestGeoObjectInfo     USA              = new TestGeoObjectInfo(TEST_DATA_KEY + "USA", COUNTRY);

  public static final TestGeoObjectInfo     COLORADO         = new TestGeoObjectInfo(TEST_DATA_KEY + "Colorado", STATE);

  public static final TestGeoObjectInfo     CO_D_ONE         = new TestGeoObjectInfo(TEST_DATA_KEY + "ColoradoDistrictOne", DISTRICT);

  public static final TestGeoObjectInfo     CO_D_TWO         = new TestGeoObjectInfo(TEST_DATA_KEY + "ColoradoDistrictTwo", DISTRICT);

  public static final TestGeoObjectInfo     CO_D_THREE       = new TestGeoObjectInfo(TEST_DATA_KEY + "ColoradoDistrictThree", DISTRICT, TestDataSet.WKT_DEFAULT_POINT, DefaultTerms.GeoObjectStatusTerm.INACTIVE.code, true);

  public static final TestGeoObjectInfo     CO_C_ONE         = new TestGeoObjectInfo(TEST_DATA_KEY + "ColoradoCountyOne", COUNTY);

  public static final TestGeoObjectInfo     CO_A_ONE         = new TestGeoObjectInfo(TEST_DATA_KEY + "ColoradoAreaOne", AREA);

  public static final TestGeoObjectInfo     WASHINGTON       = new TestGeoObjectInfo(TEST_DATA_KEY + "Washington", STATE, TestDataSet.WKT_POLYGON_2, DefaultTerms.GeoObjectStatusTerm.ACTIVE.code, true);

  public static final TestGeoObjectInfo     WA_D_ONE         = new TestGeoObjectInfo(TEST_DATA_KEY + "WashingtonDistrictOne", DISTRICT);

  public static final TestGeoObjectInfo     WA_D_TWO         = new TestGeoObjectInfo(TEST_DATA_KEY + "WashingtonDistrictTwo", DISTRICT);

  public static final TestGeoObjectInfo     CANADA           = new TestGeoObjectInfo(TEST_DATA_KEY + "CANADA", COUNTRY);

  /**
   * The Mexico Hierarchy cannot have any leaf nodes in it.
   */

  public static final TestGeoObjectTypeInfo MEXICO_CITY_GOT  = new TestGeoObjectTypeInfo(TEST_DATA_KEY + "Mexico_City_GOT", ORG_NPS);

  public static final TestGeoObjectTypeInfo MEXICO_STATE     = new TestGeoObjectTypeInfo(TEST_DATA_KEY + "Mexico_State_GOT", ORG_NPS);

  public static final TestGeoObjectInfo     MEXICO           = new TestGeoObjectInfo(TEST_DATA_KEY + "Mexico", COUNTRY);

  public static final TestGeoObjectInfo     MEXICO_CITY_ONE  = new TestGeoObjectInfo(TEST_DATA_KEY + "Mexico City One", MEXICO_CITY_GOT);

  public static final TestGeoObjectInfo     MEXICO_CITY_TWO  = new TestGeoObjectInfo(TEST_DATA_KEY + "Mexico City Two", MEXICO_CITY_GOT);

  public static final TestGeoObjectInfo     MEXICO_STATE_ONE = new TestGeoObjectInfo(TEST_DATA_KEY + "Mexico State One", MEXICO_STATE);

  public static final TestGeoObjectInfo     MEXICO_STATE_TWO = new TestGeoObjectInfo(TEST_DATA_KEY + "Mexico State Two", MEXICO_STATE);

  {
    managedOrganizationInfos.add(ORG_NPS);
    managedOrganizationInfos.add(ORG_PPP);

    managedHierarchyTypeInfos.add(HIER_ADMIN);

    managedGeoObjectTypeInfos.add(COUNTRY);
    managedGeoObjectTypeInfos.add(STATE);
    managedGeoObjectTypeInfos.add(DISTRICT);
    managedGeoObjectTypeInfos.add(COUNTY);
    managedGeoObjectTypeInfos.add(AREA);

    managedGeoObjectInfos.add(USA);
    managedGeoObjectInfos.add(CANADA);
    managedGeoObjectInfos.add(COLORADO);
    managedGeoObjectInfos.add(CO_D_ONE);
    managedGeoObjectInfos.add(CO_D_TWO);
    managedGeoObjectInfos.add(CO_D_THREE);
    managedGeoObjectInfos.add(CO_C_ONE);
    managedGeoObjectInfos.add(CO_A_ONE);
    managedGeoObjectInfos.add(WASHINGTON);
    managedGeoObjectInfos.add(WA_D_ONE);
    managedGeoObjectInfos.add(WA_D_TWO);

    managedGeoObjectTypeInfos.add(MEXICO_STATE);
    managedGeoObjectTypeInfos.add(MEXICO_CITY_GOT);
    managedGeoObjectInfos.add(MEXICO);
    managedGeoObjectInfos.add(MEXICO_CITY_ONE);
    managedGeoObjectInfos.add(MEXICO_CITY_TWO);
    managedGeoObjectInfos.add(MEXICO_STATE_ONE);
    managedGeoObjectInfos.add(MEXICO_STATE_TWO);

    managedUsers.add(USER_NPS_RA);
    managedUsers.add(USER_PPP_RA);
  }

  public static USATestData newTestData()
  {
    return new USATestData();
  }

  @Transaction
  @Override
  public void setUpClassRelationships()
  {
    COUNTRY.getUniversal().addLink(Universal.getRoot(), HIER_ADMIN.getServerObject().getUniversalType());
    COUNTRY.addChild(STATE, HIER_ADMIN);
    STATE.addChild(DISTRICT, HIER_ADMIN);
    STATE.addChild(COUNTY, HIER_ADMIN);
    COUNTY.addChild(AREA, HIER_ADMIN);

    COUNTRY.addChild(MEXICO_STATE, HIER_ADMIN);
    MEXICO_STATE.addChild(MEXICO_CITY_GOT, HIER_ADMIN);
  }

  @Transaction
  @Override
  public void setUpRelationships()
  {
    USA.setChildren(new ArrayList<TestGeoObjectInfo>());
    
    USA.getGeoEntity().addLink(GeoEntity.getRoot(), HIER_ADMIN.getServerObject().getEntityType());

    USA.addChild(COLORADO, HIER_ADMIN);
    COLORADO.addChild(CO_D_ONE, HIER_ADMIN);
    COLORADO.addChild(CO_D_TWO, HIER_ADMIN);
    COLORADO.addChild(CO_D_THREE, HIER_ADMIN);
    COLORADO.addChild(CO_C_ONE, HIER_ADMIN);
    CO_C_ONE.addChild(CO_A_ONE, HIER_ADMIN);

    USA.addChild(WASHINGTON, HIER_ADMIN);
    WASHINGTON.addChild(WA_D_ONE, HIER_ADMIN);
    WASHINGTON.addChild(WA_D_TWO, HIER_ADMIN);

    MEXICO.addChild(MEXICO_STATE_ONE, HIER_ADMIN);
    MEXICO.addChild(MEXICO_STATE_TWO, HIER_ADMIN);
    MEXICO_STATE_TWO.addChild(MEXICO_CITY_ONE, HIER_ADMIN);
    MEXICO_STATE_TWO.addChild(MEXICO_CITY_TWO, HIER_ADMIN);
  }

  @Override
  public String getTestDataKey()
  {
    return TEST_DATA_KEY;
  }
}
