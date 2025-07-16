package net.geoprism.registry.service.business;

import java.util.List;
import java.util.Optional;

import com.google.gson.JsonArray;

import net.geoprism.registry.axon.event.remote.RemoteEvent;
import net.geoprism.registry.view.CommitDTO;
import net.geoprism.registry.view.PublishDTO;

public interface RemoteClientIF extends AutoCloseable
{
  public void close();

  public Optional<PublishDTO> getPublish(String publishId);

  public Optional<CommitDTO> getCommit(String uid, Integer versionNumber);

  public JsonArray getBusinessTypes(String commitId);

  public JsonArray getGeoObjectTypes(String commitId);

  public JsonArray getBusinessEdgeTypes(String uid);

  public JsonArray getHierarchyTypes(String uid);

  public JsonArray getDirectedAcyclicGraphTypes(String uid);

  public JsonArray getUndirectedGraphTypes(String uid);

  public List<RemoteEvent> getRemoteEvents(String uid, Integer chunk);

}
