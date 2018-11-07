package net.geoprism.georegistry.action;

import net.geoprism.georegistry.service.RegistryService;

import org.commongeoregistry.adapter.action.DeleteAction;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

public class RegistryDeleteAction extends RegistryAction
{
    private DeleteAction action;
    
    private RegistryService registry;
    
    private String sessionId;

    public RegistryDeleteAction(DeleteAction action, RegistryService registry, String sessionId)
    {
      this.action = action;
      this.registry = registry;
      this.sessionId = sessionId;
    }

    @Override
    public void execute()
    {
      String type = this.action.getObjType();
      String oid = this.action.getObjOid(); // TODO : This is a code, not an id
      
      if (type.equals(GeoObject.class.getName()))
      {
        this.registry.deleteGeoObject(sessionId, oid);
      }
      else if (type.equals(GeoObjectType.class.getName()))
      {
        this.registry.deleteGeoObjectType(sessionId, oid);
      }
      else
      {
        throw new UnsupportedOperationException(type);
      }
    }
}
