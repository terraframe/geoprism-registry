package net.geoprism.georegistry.action.geoobject;

import net.geoprism.georegistry.service.ServiceFactory;

import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.action.geoobject.UpdateGeoObjectActionDTO;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.json.JSONObject;

public class UpdateGeoObjectAction extends UpdateGeoObjectActionBase
{
  private static final long serialVersionUID = 2090460439;

  public UpdateGeoObjectAction()
  {
    super();
  }

  @Override
  public void execute()
  {
    String sJson = this.getGeoObjectJson();

    this.registry.updateGeoObjectInTransaction(sJson);
  }

  @Override
  protected void buildFromDTO(AbstractActionDTO dto)
  {
    super.buildFromDTO(dto);

    UpdateGeoObjectActionDTO castedDTO = (UpdateGeoObjectActionDTO) dto;

    this.setGeoObjectJson(castedDTO.getGeoObject().toString());
  }

  @Override
  public JSONObject serialize()
  {
    JSONObject object = super.serialize();
    object.put(UpdateGeoObjectAction.GEOOBJECTJSON, new JSONObject(this.getGeoObjectJson()));
    addGeoObjectType(object);
    return object;
  }
  
  private void addGeoObjectType(JSONObject object)
  {
    GeoObject go = GeoObject.fromJSON(ServiceFactory.getAdapter(), this.getGeoObjectJson());
    GeoObjectType got = go.getType();
    
    object.put("geoObjectType", new JSONObject(got.toJSON().toString()));
  }
  
  @Override
  public void buildFromJson(JSONObject joAction)
  {
    super.buildFromJson(joAction);
    
    this.setGeoObjectJson(joAction.getJSONObject(UpdateGeoObjectAction.GEOOBJECTJSON).toString());
  }

}
