package net.geoprism.registry.shapefile;

import org.commongeoregistry.adapter.dataaccess.GeoObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.registry.model.ServerGeoObjectType;

public class GeoObjectLocationProblem implements Comparable<GeoObjectLocationProblem>
{
  private ServerGeoObjectType type;

  private GeoObject           parent;

  private String              label;

  private JsonArray           context;

  public GeoObjectLocationProblem(ServerGeoObjectType type, String label, GeoObject parent, JsonArray context)
  {
    this.type = type;
    this.label = label;
    this.context = context;
    this.parent = parent;
  }

  public String getKey()
  {
    if (this.parent != null)
    {
      return this.parent.getCode() + "-" + this.label;
    }
    else
    {
      return this.label;
    }
  }

  public JsonObject toJSON()
  {
    JsonObject object = new JsonObject();
    object.addProperty("label", label);
    object.addProperty("type", this.type.getCode());
    object.addProperty("typeLabel", this.type.getLabel().getValue());
    object.add("context", context);

    if (this.parent != null)
    {
      object.addProperty("parent", this.parent.getCode());
    }

    return object;
  }

  @Override
  public int compareTo(GeoObjectLocationProblem o)
  {
    return this.getKey().compareTo(o.getKey());
  }
}
