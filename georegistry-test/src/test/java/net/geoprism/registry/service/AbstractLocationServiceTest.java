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
package net.geoprism.registry.service;

import java.util.Date;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.DefaultSerializer;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.view.LocationInformation;

public abstract class AbstractLocationServiceTest
{
  protected static FastTestDataset testData;

  @AfterClass
  public static void cleanUpClass()
  {
    testData.logOut();
    
    testData.tearDownMetadata();
  }
  
  private int getNumProvinces()
  {
    return FastTestDataset.CAMBODIA.getChildren().size();
  }

  @Test
  public void testGetLocationInformationNullTypeAndHierarchy()
  {
    LocationService service = new LocationService();
    LocationInformation information = service.getLocationInformation(testData.clientRequest.getSessionId(), new Date(), null, null);

    Assert.assertNotNull(information);
    Assert.assertEquals(FastTestDataset.HIER_ADMIN.getCode(), information.getHierarchy());
    Assert.assertEquals(FastTestDataset.COUNTRY.getCode(), information.getChildType().getCode());
    Assert.assertEquals(1, information.getChildren().size());
    Assert.assertNull(information.getEntity());
    Assert.assertEquals(testData.getManagedHierarchyTypes().size(), information.getHierarchies().size());
    Assert.assertEquals(1, information.getChildTypes().size());
  }

//  @Test
//  public void testGetLocationInformationWithParent()
//  {
//    LocationService service = new LocationService();
//    LocationInformation information = service.getLocationInformation(testData.clientRequest.getSessionId(), FastTestDataset.CAMBODIA.getCode(), FastTestDataset.COUNTRY.getCode(), new Date(), FastTestDataset.PROVINCE.getCode(), null);
//
//    Assert.assertNotNull(information);
//    Assert.assertEquals(FastTestDataset.HIER_ADMIN.getCode(), information.getHierarchy());
//    Assert.assertEquals(FastTestDataset.PROVINCE.getCode(), information.getChildType().getCode());
//    Assert.assertEquals(1, information.getChildren().size());
//    Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), information.getEntity().getCode());
//    Assert.assertEquals(testData.getManagedHierarchyTypes().size(), information.getHierarchies().size());
//    Assert.assertEquals(getNumProvinces(), information.getChildTypes().size());
//  }

//  @Test
//  public void testSerialize()
//  {
//    LocationService service = new LocationService();
//    LocationInformation information = service.getLocationInformation(testData.clientRequest.getSessionId(), FastTestDataset.CAMBODIA.getCode(), FastTestDataset.COUNTRY.getCode(), new Date(), FastTestDataset.PROVINCE.getCode(), null);
//
//    JsonObject response = (JsonObject) information.toJson(new DefaultSerializer());
//
//    Assert.assertNotNull(response);
//    Assert.assertEquals(FastTestDataset.HIER_ADMIN.getCode(), response.get("hierarchy").getAsString());
//    Assert.assertEquals(FastTestDataset.PROVINCE.getCode(), response.get("childType").getAsString());
//    Assert.assertEquals(testData.getManagedHierarchyTypes().size(), response.get("hierarchies").getAsJsonArray().size());
//    Assert.assertEquals(getNumProvinces(), response.get("types").getAsJsonArray().size());
//
//    JsonObject geojson = response.get("geojson").getAsJsonObject();
//    JsonArray features = geojson.get("features").getAsJsonArray();
//
//    Assert.assertEquals(1, features.size());
//
//    JsonObject entity = response.get("entity").getAsJsonObject();
//
//    Assert.assertNotNull(entity);
//    Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), entity.get("properties").getAsJsonObject().get("code").getAsString());
//  }

}
