package net.geoprism.georegistry.io;

import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.google.gson.JsonObject;
import com.runwaysdk.system.gis.geo.Universal;

import net.geoprism.data.importer.BasicColumnFunction;

public class Location
{

  private GeoObjectType       type;

  private Universal           universal;

  private BasicColumnFunction function;

  public Location(GeoObjectType type, Universal universal, BasicColumnFunction function)
  {
    this.type = type;
    this.universal = universal;
    this.function = function;
  }

  public GeoObjectType getType()
  {
    return type;
  }

  public void setType(GeoObjectType type)
  {
    this.type = type;
  }

  public Universal getUniversal()
  {
    return universal;
  }

  public void setUniversal(Universal universal)
  {
    this.universal = universal;
  }

  public BasicColumnFunction getFunction()
  {
    return function;
  }

  public void setFunction(BasicColumnFunction function)
  {
    this.function = function;
  }

  public JsonObject toJson()
  {
    JsonObject object = new JsonObject();
    object.addProperty("label", this.type.getLocalizedLabel());
    object.addProperty("code", this.type.getCode());
    object.addProperty("target", this.function.toJson());

    return object;
  }
}
