/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.action.geoobject;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import net.geoprism.registry.action.ActionJsonAdapters;
import net.geoprism.registry.action.ChangeRequest;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.view.action.AbstractUpdateAttributeView;
import net.geoprism.registry.view.action.ActionEventBuilder;
import net.geoprism.registry.view.action.UpdateAttributeViewJsonAdapters;

public class UpdateAttributeAction extends UpdateAttributeActionBase
{
  private static final long serialVersionUID = -1324656697;

  public UpdateAttributeAction()
  {
    super();
  }

  @Override
  public void execute(ActionEventBuilder builder)
  {
    ChangeRequest cr = this.getAllRequest().next();

    ServerGeoObjectType type = ServerGeoObjectType.get(cr.getGeoObjectTypeCode());

    AbstractUpdateAttributeView view = UpdateAttributeViewJsonAdapters.deserialize(this.getJson(), this.getAttributeName(), type);
    
    view.build(builder);
  }

  public AbstractUpdateAttributeView getUpdateView()
  {
    return UpdateAttributeViewJsonAdapters.deserialize(this.getJson(), this.getAttributeName(), this.getChangeRequest().getGeoObjectType());
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
