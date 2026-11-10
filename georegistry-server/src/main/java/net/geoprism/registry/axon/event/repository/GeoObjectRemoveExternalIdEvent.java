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
package net.geoprism.registry.axon.event.repository;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeInfo;

public class GeoObjectRemoveExternalIdEvent extends AbstractGeoObjectEvent implements GeoObjectEvent
{
  private String   code;

  private TypeInfo type;

  private String   authority;

  public GeoObjectRemoveExternalIdEvent()
  {
  }

  public GeoObjectRemoveExternalIdEvent(String code, TypeInfo type, String authority)
  {
    super(UUID.randomUUID().toString());

    this.code = code;
    this.type = type;
    this.authority = authority;
  }

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public TypeInfo getType()
  {
    return type;
  }

  public void setType(TypeInfo type)
  {
    this.type = type;
  }

  public String getAuthority()
  {
    return authority;
  }

  public void setAuthority(String authority)
  {
    this.authority = authority;
  }

  @Override
  @JsonIgnore
  public String getBaseObjectId()
  {
    return this.code + "#" + this.type + "_S_" + this.authority;
  }

  @Override
  @JsonIgnore
  public EventPhase getEventPhase()
  {
    return EventPhase.EDGE;
  }

  @Override
  public Boolean isValidFor(PublishDTO dto)
  {
    return dto.getTypes().stream().anyMatch(this.getType()::equals);
  }
}
