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
package net.geoprism.registry.axon.event.remote;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeInfo;

public class RemoteGeoObjectRemoveExternalIdEvent implements RemoteEvent
{
  private String   commitId;

  private String   code;

  private TypeInfo type;

  private String   authority;

  public RemoteGeoObjectRemoveExternalIdEvent()
  {
  }

  public RemoteGeoObjectRemoveExternalIdEvent(String commitId, String code, TypeInfo type, String authority)
  {
    super();
    this.commitId = commitId;
    this.code = code;
    this.type = type;
    this.authority = authority;
  }

  public String getCommitId()
  {
    return commitId;
  }

  public void setCommitId(String commitId)
  {
    this.commitId = commitId;
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
    return this.code + "#" + this.type.getTypeCode() + "_S_" + this.authority;
  }

  @Override
  public boolean isValid(PublishDTO dto)
  {
    return !dto.getExclusions().contains(type);
  }
}
