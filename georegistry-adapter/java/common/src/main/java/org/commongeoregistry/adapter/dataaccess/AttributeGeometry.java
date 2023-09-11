/**
 *
 */
package org.commongeoregistry.adapter.dataaccess;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.metadata.AttributeGeometryType;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
    GeoJsonWriter gw = new GeoJsonWriter();

    return gw.write(this.getValue());
  }
  
  public void setGeometryAsGeoJson(String geoJson)
  {
    if (geoJson == null || geoJson.length() == 0)
    {
      this.setValue(null);
    }
    else
    {
      GeoJsonReader reader = new GeoJsonReader();
      Geometry jtsGeom;
      try
      {
        jtsGeom = reader.read(geoJson);
      }
      catch (ParseException e)
      {
        throw new RuntimeException(e);
      }
  
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
