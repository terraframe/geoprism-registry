package net.geoprism.registry.axon.event.remote;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import net.geoprism.registry.view.PublishDTO;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({ //
    @JsonSubTypes.Type(value = RemoteGeoObjectEvent.class), //
    @JsonSubTypes.Type(value = RemoteGeoObjectSetParentEvent.class), //
    @JsonSubTypes.Type(value = RemoteGeoObjectCreateEdgeEvent.class), //
    @JsonSubTypes.Type(value = RemoteBusinessObjectEvent.class), //
    @JsonSubTypes.Type(value = RemoteBusinessObjectAddGeoObjectEvent.class), //
    @JsonSubTypes.Type(value = RemoteBusinessObjectCreateEdgeEvent.class), //
})
public interface RemoteEvent
{
  public String getCommitId();

  public Object toCommand();

  public boolean isValid(PublishDTO dto);
}
