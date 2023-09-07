/**
 *
 */
package org.commongeoregistry.adapter.action;

import org.commongeoregistry.adapter.action.geoobject.CreateGeoObjectActionDTO;
import org.commongeoregistry.adapter.action.geoobject.UpdateGeoObjectActionDTO;
import org.commongeoregistry.adapter.action.tree.AddChildActionDTO;
import org.commongeoregistry.adapter.action.tree.RemoveChildActionDTO;
import org.commongeoregistry.adapter.constants.RegistryUrls;

public class ActionDTOFactory
{

  public static AbstractActionDTO newAction(String actionType)
  {
    if (actionType.equals(RegistryUrls.GEO_OBJECT_CREATE))
    {
      return new CreateGeoObjectActionDTO();
    }
    else if (actionType.equals(RegistryUrls.GEO_OBJECT_UPDATE))
    {
      return new UpdateGeoObjectActionDTO();
    }
    else if (actionType.equals(RegistryUrls.GEO_OBJECT_ADD_CHILD))
    {
      return new AddChildActionDTO();
    }
    else if (actionType.equals(RegistryUrls.GEO_OBJECT_REMOVE_CHILD))
    {
      return new RemoveChildActionDTO();
    }
    else
    {
      throw new UnsupportedOperationException("The supplied actionType [" + actionType + "] is invalid.");
    }
  }
  
}
