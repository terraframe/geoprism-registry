package net.geoprism.registry.service.business;

import java.util.List;
import java.util.Optional;

import com.google.gson.JsonObject;

import net.geoprism.registry.axon.event.remote.RemoteEvent;
import net.geoprism.registry.view.CommitDTO;
import net.geoprism.registry.view.PublishDTO;

public interface RemoteClientIF extends AutoCloseable
{
  public abstract void close();

  public abstract Optional<PublishDTO> getPublish(String publishId);

  public abstract Optional<CommitDTO> getCommit(String uid, Integer versionNumber);

  public abstract List<JsonObject> getBusinessTypes(String commitId);

  public abstract List<JsonObject> getGeoObjectTypes(String commitId);

  public abstract List<JsonObject> getBusinessEdgeTypes(String uid);

  public abstract List<JsonObject> getHierarchyTypes(String uid);

  public abstract List<RemoteEvent> getRemoteEvents(String uid, int chunk);
}
