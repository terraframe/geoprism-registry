package net.geoprism.registry.io;

import com.google.gson.JsonObject;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.registry.model.ServerGeoObjectType;

public class Location
{
  private ServerGeoObjectType type;

  private ShapefileFunction   function;

  public Location(ServerGeoObjectType type, ShapefileFunction function)
  {
    this.type = type;
    this.function = function;
  }

  public ServerGeoObjectType getType()
  {
    return type;
  }

  public void setType(ServerGeoObjectType type)
  {
    this.type = type;
  }

  public ShapefileFunction getFunction()
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
    object.addProperty("label", this.type.getLabel().getValue());
    object.addProperty("code", this.type.getCode());
    object.addProperty("target", this.function.toJson());

    return object;
  }
}
