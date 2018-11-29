package net.geoprism.georegistry.action;

import net.geoprism.georegistry.service.RegistryService;

import org.commongeoregistry.adapter.action.CreateAction;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.google.gson.JsonObject;

public class RegistryCreateAction extends RegistryAction
{
    private CreateAction action;
    
    private RegistryService registry;
    
    private String sessionId;

    public RegistryCreateAction(CreateAction action, RegistryService registry, String sessionId)
    {
      this.action = action;
      this.registry = registry;
      this.sessionId = sessionId;
    }

    @Override
    public void execute()
    {
      String type = this.action.getObjType();
      JsonObject json = this.action.getObjJson();
      
      if (type.equals(GeoObject.class.getName()))
      {
        this.registry.createGeoObject(sessionId, json.toString());
      }
      else if (type.equals(GeoObjectType.class.getName()))
      {
        // TODO
        throw new UnsupportedOperationException("TODO : Not implemented yet");
      }
      else
      {
        throw new UnsupportedOperationException(type);
      }
    }
}
