package net.geoprism.georegistry.action;

import org.commongeoregistry.adapter.constants.RegistryUrls;

import net.geoprism.georegistry.action.geoobject.CreateGeoObjectAction;
import net.geoprism.georegistry.action.geoobject.UpdateGeoObjectAction;
import net.geoprism.georegistry.action.tree.AddChildAction;
import net.geoprism.georegistry.action.tree.RemoveChildAction;

public class ActionFactory
{
  public static AbstractAction newAction(String actionType)
  {
    if (actionType.equals(RegistryUrls.GEO_OBJECT_CREATE))
    {
      return new CreateGeoObjectAction();
    }
    else if (actionType.equals(RegistryUrls.GEO_OBJECT_UPDATE))
    {
      return new UpdateGeoObjectAction();
    }
    else if (actionType.equals(RegistryUrls.GEO_OBJECT_ADD_CHILD))
    {
      return new AddChildAction();
    }
    else if (actionType.equals(RegistryUrls.GEO_OBJECT_REMOVE_CHILD))
    {
      return new RemoveChildAction();
    }
    else
    {
      throw new UnsupportedOperationException("The supplied actionType [" + actionType + "] is invalid.");
    }
  }
}
