/**
 *
 */
package org.commongeoregistry.adapter.action.geoobject;

import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.constants.RegistryUrls;

import com.google.gson.JsonObject;

public class UpdateGeoObjectActionDTO extends AbstractActionDTO
{
  private JsonObject geoObject;
  
  public UpdateGeoObjectActionDTO()
  {
    super(RegistryUrls.GEO_OBJECT_UPDATE);
  }
  
  @Override
  protected void buildJson(JsonObject json)
  {
    super.buildJson(json);
    
    json.add(RegistryUrls.GEO_OBJECT_UPDATE_PARAM_GEOOBJECT, this.geoObject);
  }
  
  @Override
  protected void buildFromJson(JsonObject json)
  {
    super.buildFromJson(json);
    
    this.geoObject = json.get(RegistryUrls.GEO_OBJECT_UPDATE_PARAM_GEOOBJECT).getAsJsonObject();
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
