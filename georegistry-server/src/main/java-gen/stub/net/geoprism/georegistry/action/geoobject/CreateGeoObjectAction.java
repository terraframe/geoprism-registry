package net.geoprism.georegistry.action.geoobject;

import net.geoprism.georegistry.service.ServiceFactory;

import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.action.geoobject.CreateGeoObjectActionDTO;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.json.JSONObject;

public class CreateGeoObjectAction extends CreateGeoObjectActionBase
{
  private static final long serialVersionUID = 154658500;

  public CreateGeoObjectAction()
  {
    super();
  }

  @Override
  public void execute()
  {
    String sJson = this.getGeoObjectJson();

    this.registry.createGeoObjectInTransaction(sJson);
  }

  @Override
  protected void buildFromDTO(AbstractActionDTO dto)
  {
    super.buildFromDTO(dto);

    CreateGeoObjectActionDTO castedDTO = (CreateGeoObjectActionDTO) dto;

    this.setGeoObjectJson(castedDTO.getGeoObject().toString());
  }

  @Override
  public JSONObject serialize()
  {
    JSONObject object = super.serialize();
    object.put(CreateGeoObjectAction.GEOOBJECTJSON, new JSONObject(this.getGeoObjectJson()));
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
    
    this.setGeoObjectJson(joAction.getJSONObject(CreateGeoObjectAction.GEOOBJECTJSON).toString());
  }

}
