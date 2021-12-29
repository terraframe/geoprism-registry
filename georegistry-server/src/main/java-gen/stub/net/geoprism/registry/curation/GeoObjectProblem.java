package net.geoprism.registry.curation;

import com.google.gson.JsonObject;

public class GeoObjectProblem extends GeoObjectProblemBase
{
  private static final long serialVersionUID = 908308239;
  
  public static enum GeoObjectProblemType
  {
    NO_GEOMETRY,
    INVALID_GEOMETRY
  }
  
  public GeoObjectProblem()
  {
    super();
  }
  
  @Override
  public JsonObject toJson()
  {
    JsonObject json = super.toJson();
    
    json.addProperty("typeCode", this.getTypeCode());
    json.addProperty("goCode", this.getGoCode());
    
    return json;
  }
  
}
