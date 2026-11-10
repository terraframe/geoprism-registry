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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import net.geoprism.registry.view.PublishDTO;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({ //
    @JsonSubTypes.Type(value = RemoteGeoObjectEvent.class), //
    @JsonSubTypes.Type(value = RemoteGeoObjectSetParentEvent.class), //
    @JsonSubTypes.Type(value = RemoteGeoObjectCreateEdgeEvent.class), //
    @JsonSubTypes.Type(value = RemoteGeoObjectApplyExternalIdEvent.class), //
    @JsonSubTypes.Type(value = RemoteGeoObjectRemoveExternalIdEvent.class), //
    @JsonSubTypes.Type(value = RemoteGeoObjectCreateEdgeEvent.class), //
    @JsonSubTypes.Type(value = RemoteObjectApplyEvent.class), //
    @JsonSubTypes.Type(value = RemoteObjectApplyEdgeEvent.class), //
    @JsonSubTypes.Type(value = RemoteObjectRemoveEdgeEvent.class) //
})
public interface RemoteEvent
{
  public String getCommitId();

  public void setCommitId(String commitId);

  public boolean isValid(PublishDTO dto);

  public String getBaseObjectId();
}
