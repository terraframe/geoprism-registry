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
package net.geoprism.registry.view.action;

import java.util.ArrayList;
import java.util.List;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.AlternateId;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.business.GPRGeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;

public class UpdateStandardAttributeView extends AbstractUpdateAttributeView
{

  protected JsonElement oldValue;
  
  protected JsonElement newValue;
  
  @Override
  public void execute(VertexServerGeoObject go)
  {
    ServerGeoObjectType type = go.getType();
    AttributeType attr = type.toDTO().getAttribute(this.getAttributeName()).get();
    
    if (newValue != null)
    {
      Object converted;
      
      if (attr.getName().equals(DefaultAttribute.ALT_IDS.getName()))
      {
        JsonArray ja = newValue.getAsJsonArray();
        List<AlternateId> ids = new ArrayList<AlternateId>();
        
        ja.forEach(ele -> ids.add(AlternateId.fromJSON(ele)));
        
        ServiceFactory.getBean(GPRGeoObjectBusinessServiceIF.class).setAlternateIds(go, ids);
        return;
      }
      else if (attr instanceof AttributeBooleanType)
      {
        converted = newValue.getAsBoolean();
      }
      else
      {
        throw new UnsupportedOperationException();
      }
      
      go.setValue(this.getAttributeName(), converted);
    }
  }

}
