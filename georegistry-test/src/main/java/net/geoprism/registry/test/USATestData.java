/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.test;

import org.commongeoregistry.adapter.constants.DefaultTerms;
import org.commongeoregistry.adapter.constants.GeometryType;

import com.runwaysdk.constants.LocalProperties;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;

import net.geoprism.gis.geoserver.GeoserverFacade;
import net.geoprism.gis.geoserver.NullGeoserverService;

public class USATestData extends TestDataSet
{
  public final String                TEST_DATA_KEY    = "USATestData";
  
  public final TestOrganizationInfo  ORG_NPS          = new TestOrganizationInfo(this.getTestDataKey() + "NPS");
  
  public final TestHierarchyTypeInfo HIER_ADMIN       = new TestHierarchyTypeInfo(this.getTestDataKey() +  "Admin", ORG_NPS);
  
  public final TestGeoObjectTypeInfo COUNTRY          = new TestGeoObjectTypeInfo(this.getTestDataKey() +  "Country", GeometryType.MULTIPOLYGON, ORG_NPS);

  public final TestGeoObjectTypeInfo STATE            = new TestGeoObjectTypeInfo(this.getTestDataKey() +  "State", GeometryType.MULTIPOLYGON, ORG_NPS);

  public final TestGeoObjectTypeInfo COUNTY           = new TestGeoObjectTypeInfo(this.getTestDataKey() +  "County", GeometryType.MULTIPOLYGON, ORG_NPS);

  public final TestGeoObjectTypeInfo AREA             = new TestGeoObjectTypeInfo(this.getTestDataKey() +  "Area", GeometryType.POLYGON, ORG_NPS);

  public final TestGeoObjectTypeInfo DISTRICT         = new TestGeoObjectTypeInfo(this.getTestDataKey() +  "District", GeometryType.POINT, ORG_NPS);

  public final TestGeoObjectInfo     USA              = new TestGeoObjectInfo(this.getTestDataKey() +  "USA", COUNTRY);

  public final TestGeoObjectInfo     COLORADO         = new TestGeoObjectInfo(this.getTestDataKey() +  "Colorado", STATE);

  public final TestGeoObjectInfo     CO_D_ONE         = new TestGeoObjectInfo(this.getTestDataKey() +  "ColoradoDistrictOne", DISTRICT);

  public final TestGeoObjectInfo     CO_D_TWO         = new TestGeoObjectInfo(this.getTestDataKey() +  "ColoradoDistrictTwo", DISTRICT);

  public final TestGeoObjectInfo     CO_D_THREE       = new TestGeoObjectInfo(this.getTestDataKey() +  "ColoradoDistrictThree", DISTRICT, TestDataSet.WKT_DEFAULT_POINT, DefaultTerms.GeoObjectStatusTerm.INACTIVE.code, true);

  public final TestGeoObjectInfo     CO_C_ONE         = new TestGeoObjectInfo(this.getTestDataKey() +  "ColoradoCountyOne", COUNTY);

  public final TestGeoObjectInfo     CO_A_ONE         = new TestGeoObjectInfo(this.getTestDataKey() +  "ColoradoAreaOne", AREA);

  public final TestGeoObjectInfo     WASHINGTON       = new TestGeoObjectInfo(this.getTestDataKey() +  "Washington", STATE, "MULTIPOLYGON(((1 1,5 1,5 5,1 5,1 1),(2 2, 3 2, 3 3, 2 3,2 2)))", DefaultTerms.GeoObjectStatusTerm.ACTIVE.code, true);

  public final TestGeoObjectInfo     WA_D_ONE         = new TestGeoObjectInfo(this.getTestDataKey() +  "WashingtonDistrictOne", DISTRICT);

  public final TestGeoObjectInfo     WA_D_TWO         = new TestGeoObjectInfo(this.getTestDataKey() +  "WashingtonDistrictTwo", DISTRICT);

  public final TestGeoObjectInfo     CANADA           = new TestGeoObjectInfo(this.getTestDataKey() +  "CANADA", COUNTRY);

  /**
   * The Mexico Hierarchy cannot have any leaf nodes in it.
   */

  public final TestGeoObjectTypeInfo MEXICO_CITY_GOT  = new TestGeoObjectTypeInfo(this.getTestDataKey() +  "Mexico_City_GOT", ORG_NPS);

  public final TestGeoObjectTypeInfo MEXICO_STATE     = new TestGeoObjectTypeInfo(this.getTestDataKey() +  "Mexico_State_GOT", ORG_NPS);

  public final TestGeoObjectInfo     MEXICO           = new TestGeoObjectInfo(this.getTestDataKey() +  "Mexico", COUNTRY);

  public final TestGeoObjectInfo     MEXICO_CITY_ONE  = new TestGeoObjectInfo(this.getTestDataKey() +  "Mexico City One", MEXICO_CITY_GOT);

  public final TestGeoObjectInfo     MEXICO_CITY_TWO  = new TestGeoObjectInfo(this.getTestDataKey() +  "Mexico City Two", MEXICO_CITY_GOT);

  public final TestGeoObjectInfo     MEXICO_STATE_ONE = new TestGeoObjectInfo(this.getTestDataKey() +  "Mexico State One", MEXICO_STATE);

  public final TestGeoObjectInfo     MEXICO_STATE_TWO = new TestGeoObjectInfo(this.getTestDataKey() +  "Mexico State Two", MEXICO_STATE);

  {
    managedOrganizationInfos.add(ORG_NPS);
    
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
