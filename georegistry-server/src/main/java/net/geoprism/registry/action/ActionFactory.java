/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.action;

import org.commongeoregistry.adapter.constants.RegistryUrls;

import net.geoprism.registry.action.geoobject.CreateGeoObjectAction;
import net.geoprism.registry.action.geoobject.UpdateGeoObjectAction;
import net.geoprism.registry.action.tree.AddChildAction;
import net.geoprism.registry.action.tree.RemoveChildAction;

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
