/**
 *
 */
package org.commongeoregistry.adapter.action.geoobject;

import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.constants.RegistryUrls;

import com.google.gson.JsonObject;

public class CreateGeoObjectActionDTO extends AbstractActionDTO
{
  private JsonObject geoObject;
  
  public CreateGeoObjectActionDTO()
  {
    super(RegistryUrls.GEO_OBJECT_CREATE);
  }

  @Override
  protected void buildJson(JsonObject json)
  {
    super.buildJson(json);
    
    json.add(RegistryUrls.GEO_OBJECT_CREATE_PARAM_GEOOBJECT, this.geoObject);
  }
  
  @Override
  protected void buildFromJson(JsonObject json)
  {
    super.buildFromJson(json);
    
    this.geoObject = json.get(RegistryUrls.GEO_OBJECT_CREATE_PARAM_GEOOBJECT).getAsJsonObject();
  }
  
  public void setGeoObject(JsonObject geoObject)
  {
    this.geoObject = geoObject;
  }
  
  public JsonObject getGeoObject()
  {
    return geoObject;
  }
}
