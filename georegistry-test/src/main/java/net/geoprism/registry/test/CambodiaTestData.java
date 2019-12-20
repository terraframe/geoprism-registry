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
