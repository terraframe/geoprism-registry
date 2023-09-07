/**
 *
 */
package org.commongeoregistry.adapter.action;

import org.commongeoregistry.adapter.constants.RegistryUrls;

import com.google.gson.JsonObject;

public class CreateGeoObjectTypeActionDTO extends AbstractActionDTO
{
  private JsonObject geoObjectType;
  
  public CreateGeoObjectTypeActionDTO()
  {
    super(RegistryUrls.GEO_OBJECT_TYPE_CREATE);
  }
  
  @Override
  protected void buildJson(JsonObject json)
  {
    super.buildJson(json);
    
    json.add(RegistryUrls.GEO_OBJECT_TYPE_CREATE_PARAM_GOT, this.geoObjectType);
  }
  
  @Override
  protected void buildFromJson(JsonObject json)
  {
    super.buildFromJson(json);
    
    this.geoObjectType = json.get(RegistryUrls.GEO_OBJECT_TYPE_CREATE_PARAM_GOT).getAsJsonObject();
  }
  
  public void setGeoObjectType(JsonObject geoObject)
  {
    this.geoObjectType = geoObject;
  }
  
  public JsonObject getGeoObjectType()
  {
    return geoObjectType;
  }
}
