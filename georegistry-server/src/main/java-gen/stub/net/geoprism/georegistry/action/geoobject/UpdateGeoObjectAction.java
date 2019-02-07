package net.geoprism.georegistry.action.geoobject;

import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.action.geoobject.UpdateGeoObjectActionDTO;

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
    
    this.registry.updateGeoObject(sessionId, sJson);
  }
  
  @Override
  protected void buildFromDTO(AbstractActionDTO dto)
  {
    super.buildFromDTO(dto);
    
    UpdateGeoObjectActionDTO castedDTO = (UpdateGeoObjectActionDTO) dto;
    
    this.setGeoObjectJson(castedDTO.getGeoObject().toString());
  }
  
}
