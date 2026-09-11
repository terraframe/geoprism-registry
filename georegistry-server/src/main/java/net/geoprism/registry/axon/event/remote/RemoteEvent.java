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
