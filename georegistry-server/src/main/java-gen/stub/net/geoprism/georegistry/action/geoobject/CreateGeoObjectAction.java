package net.geoprism.georegistry.action.geoobject;

import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.action.geoobject.CreateGeoObjectActionDTO;

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
    
    this.registry.createGeoObject(sessionId, sJson);
  }
  
  @Override
  protected void buildFromDTO(AbstractActionDTO dto)
  {
    super.buildFromDTO(dto);
    
    CreateGeoObjectActionDTO castedDTO = (CreateGeoObjectActionDTO) dto;
    
    this.setGeoObjectJson(castedDTO.getGeoObject().toString());
  }
  
}
