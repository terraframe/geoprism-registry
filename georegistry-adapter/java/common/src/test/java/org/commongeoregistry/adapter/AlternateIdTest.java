/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Common Geo Registry Adapter(tm).
 *
 * Common Geo Registry Adapter(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Common Geo Registry Adapter(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Common Geo Registry Adapter(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package org.commongeoregistry.adapter;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.AlternateId;
import org.commongeoregistry.adapter.dataaccess.ExternalId;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.MetadataFactory;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonObject;

public class AlternateIdTest
{
  @Test
  public void testSerialize()
  {
    ExternalId id = new ExternalId("01", "ABC-123", "DHIS2");
    
    JsonObject jo = id.toJSON().getAsJsonObject();
    
    Assert.assertEquals(ExternalId.TYPE, jo.get(AlternateId.TYPE).getAsString());
    Assert.assertEquals("01", jo.get("id").getAsString());
    Assert.assertEquals("ABC-123", jo.get("externalSystemId").getAsString());
    Assert.assertEquals("DHIS2", jo.get("externalSystemLabel").getAsString());
    
    id = (ExternalId) ExternalId.fromJSON(jo);
    Assert.assertEquals("01", id.getId());
    Assert.assertEquals("ABC-123", id.getExternalSystemId());
    Assert.assertEquals("DHIS2", id.getExternalSystemLabel());
  }
  
  @Test
  public void testGeoObjectOverTimeSerialize()
  {
    ExternalId id1 = new ExternalId("01", "ABC-123", "DHIS2");
    ExternalId id2 = new ExternalId("02", "ABC-1234", "DHIS3");
    ExternalId id3 = new ExternalId("03", "ABC-12345", "DHIS4");
    
    RegistryAdapterServer registry = new RegistryAdapterServer(new MockIdService());
    
    MetadataFactory.newGeoObjectType("State", GeometryType.POLYGON, new LocalizedValue("State"), new LocalizedValue("State"), true, null, registry);

    String geom = "POLYGON ((10000 10000, 12300 40000, 16800 50000, 12354 60000, 13354 60000, 17800 50000, 13300 40000, 11000 10000, 10000 10000))";

    GeoObjectOverTime geoObject = registry.newGeoObjectOverTimeInstance("State");
    
    geoObject.setWKTGeometry(geom, null);
    geoObject.setCode("Colorado");
    geoObject.setUid("CO");
    geoObject.setDisplayLabel(new LocalizedValue("Colorado Display Label"), null, null);
    geoObject.setExists(true, null, null);
    
    geoObject.addAlternateId(id1);
    geoObject.addAlternateId(id2);
    geoObject.addAlternateId(id3);

    String sJson = geoObject.toJSON().toString();
    
    GeoObjectOverTime geoObject2 = GeoObjectOverTime.fromJSON(registry, sJson);

    ExternalId out1 = geoObject2.getExternalId("ABC-123").get();
    ExternalId out2 = geoObject2.getExternalId("ABC-1234").get();
    ExternalId out3 = geoObject2.getExternalId("ABC-12345").get();
    
    Assert.assertEquals(id1.getId(), out1.getId());
    Assert.assertEquals(id2.getId(), out2.getId());
    Assert.assertEquals(id3.getId(), out3.getId());
  }
  
  @Test
  public void testGeoObjectSerialize()
  {
    ExternalId id1 = new ExternalId("01", "ABC-123", "DHIS2");
    ExternalId id2 = new ExternalId("02", "ABC-1234", "DHIS3");
    ExternalId id3 = new ExternalId("03", "ABC-12345", "DHIS4");
    
    RegistryAdapterServer registry = new RegistryAdapterServer(new MockIdService());
    
    MetadataFactory.newGeoObjectType("State", GeometryType.POLYGON, new LocalizedValue("State"), new LocalizedValue("State"), true, null, registry);

    String geom = "POLYGON ((10000 10000, 12300 40000, 16800 50000, 12354 60000, 13354 60000, 17800 50000, 13300 40000, 11000 10000, 10000 10000))";

    GeoObject geoObject = registry.newGeoObjectInstance("State");
    
    geoObject.setWKTGeometry(geom);
    geoObject.setCode("Colorado");
    geoObject.setUid("CO");
    geoObject.setDisplayLabel(new LocalizedValue("Colorado Display Label"));
    geoObject.setExists(true);
    
    geoObject.addAlternateId(id1);
    geoObject.addAlternateId(id2);
    geoObject.addAlternateId(id3);

    String sJson = geoObject.toJSON().toString();
    
    GeoObject geoObject2 = GeoObject.fromJSON(registry, sJson);

    ExternalId out1 = geoObject2.getExternalId("ABC-123").get();
    ExternalId out2 = geoObject2.getExternalId("ABC-1234").get();
    ExternalId out3 = geoObject2.getExternalId("ABC-12345").get();
    
    Assert.assertEquals(id1.getId(), out1.getId());
    Assert.assertEquals(id2.getId(), out2.getId());
    Assert.assertEquals(id3.getId(), out3.getId());
  }
}
