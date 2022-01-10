/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.io;

import org.junit.Assert;
import org.junit.Test;

public class PostalCodeFactoryTest
{
  @Test
  public void testPlaceholder()
  {
    Assert.assertTrue(true);
  }

  // @Test
  // public void testCambodiaProvince()
  // {
  // GeoObjectType type =
  // ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType("Cambodia_Province").get();
  //
  // Assert.assertTrue(PostalCodeFactory.isAvailable(type));
  //
  // HashMap<String, Object> row = new HashMap<String, Object>();
  // row.put("TEST", "20");
  //
  // MapFeatureRow feature = new MapFeatureRow(row);
  //
  // LocationBuilder builder = PostalCodeFactory.get(type);
  // Location location = builder.build(new BasicColumnFunction("TEST"));
  //
  // String result = (String) location.getFunction().getValue(feature);
  //
  // Assert.assertEquals("855", result);
  // }
  //
  // @Test
  // public void testCambodiaDistrct()
  // {
  // GeoObjectType type =
  // ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType("Cambodia_District").get();
  //
  // Assert.assertTrue(PostalCodeFactory.isAvailable(type));
  //
  // HashMap<String, Object> row = new HashMap<String, Object>();
  // row.put("TEST", "2004");
  //
  // MapFeatureRow feature = new MapFeatureRow(row);
  //
  // LocationBuilder builder = PostalCodeFactory.get(type);
  // Location location = builder.build(new BasicColumnFunction("TEST"));
  //
  // String result = (String) location.getFunction().getValue(feature);
  //
  // Assert.assertEquals("855 20", result);
  // }
  //
  // @Test
  // public void testCambodiaCommune()
  // {
  // GeoObjectType type =
  // ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType("Cambodia_Commune").get();
  //
  // Assert.assertTrue(PostalCodeFactory.isAvailable(type));
  //
  // HashMap<String, Object> row = new HashMap<String, Object>();
  // row.put("TEST", "120308");
  //
  // MapFeatureRow feature = new MapFeatureRow(row);
  //
  // LocationBuilder builder = PostalCodeFactory.get(type);
  // Location location = builder.build(new BasicColumnFunction("TEST"));
  //
  // String result = (String) location.getFunction().getValue(feature);
  //
  // Assert.assertEquals("855 1203", result);
  // }
  //
  // @Test
  // public void testCambodiaVillage()
  // {
  // GeoObjectType type =
  // ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType("Cambodia_Village").get();
  //
  // Assert.assertTrue(PostalCodeFactory.isAvailable(type));
  //
  // HashMap<String, Object> row = new HashMap<String, Object>();
  // row.put("TEST", "20071103");
  //
  // MapFeatureRow feature = new MapFeatureRow(row);
  //
  // LocationBuilder builder = PostalCodeFactory.get(type);
  // Location location = builder.build(new BasicColumnFunction("TEST"));
  //
  // String result = (String) location.getFunction().getValue(feature);
  //
  // Assert.assertEquals("855 200711", result);
  // }
}
