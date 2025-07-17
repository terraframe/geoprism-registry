package net.geoprism.registry.axon.event.remote;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({ //
    @JsonSubTypes.Type(value = RemoteGeoObjectEvent.class), //
    @JsonSubTypes.Type(value = RemoteGeoObjectSetParentEvent.class), //
    @JsonSubTypes.Type(value = RemoteBusinessObjectEvent.class), //
    @JsonSubTypes.Type(value = RemoteBusinessObjectAddGeoObjectEvent.class), //
})
public interface RemoteEvent
{
  public String getCommitId();

  public Object toCommand();
}
