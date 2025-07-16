package net.geoprism.registry.service.request;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.registry.Commit;
import net.geoprism.registry.JsonCollectors;
import net.geoprism.registry.Publish;
import net.geoprism.registry.axon.event.remote.RemoteEvent;
import net.geoprism.registry.service.business.BusinessEdgeTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.CommitBusinessServiceIF;
import net.geoprism.registry.service.business.PublishBusinessServiceIF;
import net.geoprism.registry.view.CommitDTO;

@Service
public class CommitService
{
  @Autowired
  private PublishBusinessServiceIF                  publishService;

  @Autowired
  private CommitBusinessServiceIF                   service;

  @Autowired
  private BusinessEdgeTypeSnapshotBusinessServiceIF edgeTypeService;

  @Request(RequestType.SESSION)
  public CommitDTO get(String sessionId, String publishId, Integer versionNumber)
  {
    Publish publish = this.publishService.getByUidOrThrow(publishId);

    return this.service.getCommit(publish, versionNumber).orElseThrow(() -> {
      throw new ProgrammingErrorException("Unable to find a commit version [" + versionNumber + "]");
    }).toDTO(publish);
  }

  @Request(RequestType.SESSION)
  public List<CommitDTO> getAll(String sessionId, String publishId)
  {
    Publish publish = this.publishService.getByUidOrThrow(publishId);

    return this.service.getCommits(publish).stream().map(c -> c.toDTO(publish)).toList();
  }

  @Request(RequestType.SESSION)
  public JsonArray getBusinessTypes(String sessionId, String uid)
  {
    Commit commit = this.service.getOrThrow(uid);

    return this.service.getBusinessTypes(commit).stream().map(type -> type.toJSON()).collect(JsonCollectors.toJsonArray());
  }

  @Request(RequestType.SESSION)
  public JsonArray getGeoObjectTypes(String sessionId, String uid)
  {
    Commit commit = this.service.getOrThrow(uid);

    return this.service.getTypes(commit).stream().map(type -> type.toJSON()).collect(JsonCollectors.toJsonArray());
  }

  @Request(RequestType.SESSION)
  public JsonArray getBusinessEdgeTypes(String sessionId, String uid)
  {
    Commit commit = this.service.getOrThrow(uid);

    return this.service.getBusinessEdgeTypes(commit).stream().map(type -> this.edgeTypeService.toJSON(type)).collect(JsonCollectors.toJsonArray());
  }

  @Request(RequestType.SESSION)
  public JsonArray getHierarchyTypes(String sessionId, String uid)
  {
    Commit commit = this.service.getOrThrow(uid);

    GeoObjectTypeSnapshot rootType = this.service.getRootType(commit);

    return this.service.getHiearchyTypes(commit).stream().map(type -> type.toJSON(rootType)).collect(JsonCollectors.toJsonArray());
  }

  @Request(RequestType.SESSION)
  public JsonArray getDirectedAcyclicGraphTypes(String sessionId, String uid)
  {
    Commit commit = this.service.getOrThrow(uid);

    GeoObjectTypeSnapshot rootType = this.service.getRootType(commit);

    return this.service.getDirectedAcyclicGraphTypes(commit).stream().map(type -> type.toJSON(rootType)).collect(JsonCollectors.toJsonArray());
  }
  
  @Request(RequestType.SESSION)
  public JsonArray getUndirectedGraphTypes(String sessionId, String uid)
  {
    Commit commit = this.service.getOrThrow(uid);
    
    GeoObjectTypeSnapshot rootType = this.service.getRootType(commit);
    
    return this.service.getUndirectedGraphTypes(commit).stream().map(type -> type.toJSON(rootType)).collect(JsonCollectors.toJsonArray());
  }

  @Request(RequestType.SESSION)
  public List<RemoteEvent> getRemoteEvents(String sessionId, String uid, Integer chunk)
  {
    Commit commit = this.service.getOrThrow(uid);

    return this.service.getRemoteEvents(commit, chunk);
  }

}
