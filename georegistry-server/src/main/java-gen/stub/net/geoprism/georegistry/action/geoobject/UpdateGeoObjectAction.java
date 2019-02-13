package net.geoprism.georegistry.action.geoobject;

import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.action.geoobject.UpdateGeoObjectActionDTO;
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
    return object;
  }
  
  @Override
  public void buildFromJson(JSONObject joAction)
  {
    this.setGeoObjectJson(joAction.getJSONObject(UpdateGeoObjectAction.GEOOBJECTJSON).toString());
  }

}
