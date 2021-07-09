package net.geoprism.registry.action.geoobject;

import com.google.gson.JsonObject;

import net.geoprism.registry.action.ChangeRequest;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

public class UpdateAttributeAction extends UpdateAttributeActionBase
{
  private static final long serialVersionUID = -1324656697;
  
  public UpdateAttributeAction()
  {
    super();
  }
  
  @Override
  public void execute()
  {
    ChangeRequest cr = this.getAllRequest().next();
    
    ServerGeoObjectType type = ServerGeoObjectType.get(cr.getType());
    
    VertexServerGeoObject.getVertexByCode(type, cr.getGeoObjectCode());
  }

  @Override
  protected String getMessage()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JsonObject toJson()
  {
    // TODO Auto-generated method stub
    return null;
  }
  
}
