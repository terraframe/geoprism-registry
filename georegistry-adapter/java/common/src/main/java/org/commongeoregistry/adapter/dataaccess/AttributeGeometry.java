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
package org.commongeoregistry.adapter.dataaccess;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.metadata.AttributeGeometryType;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.wololo.jts2geojson.GeoJSONReader;
import org.wololo.jts2geojson.GeoJSONWriter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class AttributeGeometry extends Attribute
{

  private static final long serialVersionUID = 6874451021655655964L;
  
  private Geometry geom;
  
  private GeometryType geomType;

  public AttributeGeometry(String name)
  {
    super(name, AttributeGeometryType.TYPE);
  }

  @Override
  public Geometry getValue()
  {
    return this.geom;
  }
  
  public GeometryType getGeometryType()
  {
    return geomType;
  }

  public void setGeometryType(GeometryType geomType)
  {
    this.geomType = geomType;
  }

  @Override
  public void setValue(Object value)
  {
    this.geom = (Geometry) value;
  }
  
  public void setWKTGeometry(String wkt)
  {
    Geometry wktObj = null;
    WKTReader wktReader = new WKTReader();
    try
    {
      wktObj = wktReader.read(wkt);
    }
    catch (ParseException e)
    {
      throw new RuntimeException(e); // TODO : Exception handling
    }
    
    if (wktObj == null)
    {
      throw new RuntimeException("Cannot parse geometry."); // TODO : Exception handling
    }

    this.setValue(wktObj);
  }
  
  public String getGeometryAsGeoJson()
  {
    GeoJSONWriter gw = new GeoJSONWriter();
    org.wololo.geojson.Geometry gJSON = gw.write(this.getValue());

    return gJSON.toString();
  }
  
  public void setGeometryAsGeoJson(String geoJson)
  {
    if (geoJson == null || geoJson.length() == 0)
    {
      this.setValue(null);
    }
    else
    {
      GeoJSONReader reader = new GeoJSONReader();
      Geometry jtsGeom = reader.read(geoJson);
  
      this.setValue(jtsGeom);
    }
  }
  
  @Override
  public JsonObject toJSON(CustomSerializer serializer)
  {
    if (this.getValue() != null)
    {
      String geoJson = this.getGeometryAsGeoJson();
      
      JsonParser parser = new JsonParser();
      JsonObject geomObj = parser.parse(geoJson).getAsJsonObject();

      return geomObj;
    }
    
    return null;
  }
  
  @Override
  public void fromJSON(JsonElement jValue, RegistryAdapter registry)
  {
    if (jValue == null || jValue.isJsonNull())
    {
      this.setValue(null);
    }
    else
    {
      this.setGeometryAsGeoJson(jValue.toString());
    }
  }

}
