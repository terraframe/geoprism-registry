package net.geoprism.registry.action.geoobject;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTimeCollection;

import net.geoprism.dhis2.dhis2adapter.DHIS2Constants;
import net.geoprism.registry.action.ActionJsonAdapters;
import net.geoprism.registry.action.ChangeRequest;
import net.geoprism.registry.conversion.VertexGeoObjectStrategy;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.view.action.AbstractUpdateAttributeView;
import net.geoprism.registry.view.action.UpdateAttributeViewJsonAdapters;

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
    
    ServerGeoObjectType type = ServerGeoObjectType.get(cr.getGeoObjectTypeCode());
    
    VertexServerGeoObject go = new VertexGeoObjectStrategy(type).getGeoObjectByCode(cr.getGeoObjectCode());
    
    AbstractUpdateAttributeView view = UpdateAttributeViewJsonAdapters.deserialize(this.getJson(), this.getAttributeName(), type);
    
    view.execute(go);
    
    if (!this.getAttributeName().equals(UpdateAttributeViewJsonAdapters.PARENT_ATTR_NAME))
    {
      String attributeName = this.getAttributeName();
      if (attributeName.equals("geometry"))
      {
        attributeName = go.getGeometryAttributeName();
      }
      
      ValueOverTimeCollection votc = go.getValuesOverTime(attributeName);
      
      votc.reorder();
      
      go.apply(false);
    }
    
    
  }

  @Override
  protected String getMessage()
  {
    return null;
  }

  @Override
  public JsonObject toJson()
  {
    GsonBuilder builder = new GsonBuilder();
    builder.setDateFormat("yyyy-MM-dd");
    builder.registerTypeAdapter(UpdateAttributeAction.class, new ActionJsonAdapters.UpdateAttributeActionSerializer());

    return (JsonObject) builder.create().toJsonTree(this);
  }
  
}
