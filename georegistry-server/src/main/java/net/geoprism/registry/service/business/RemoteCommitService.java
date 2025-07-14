package net.geoprism.registry.service.business;

import java.util.List;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.dataaccess.ProgrammingErrorException;

import net.geoprism.graph.BusinessEdgeTypeSnapshot;
import net.geoprism.graph.BusinessTypeSnapshot;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.registry.Commit;
import net.geoprism.registry.Publish;
import net.geoprism.registry.axon.event.remote.RemoteEvent;
import net.geoprism.registry.view.CommitDTO;
import net.geoprism.registry.view.PublishDTO;

@Service
public class RemoteCommitService
{
  @Autowired
  private RemoteClientBuilderServiceIF              service;

  @Autowired
  private PublishBusinessServiceIF                  publishService;

  @Autowired
  private CommitBusinessServiceIF                   commitService;

  @Autowired
  private GeoObjectTypeSnapshotBusinessServiceIF    gTypeService;

  @Autowired
  private BusinessEdgeTypeSnapshotBusinessServiceIF bEdgeTypeService;

  @Autowired
  private BusinessTypeSnapshotBusinessServiceIF     bTypeService;

  @Autowired
  private HierarchyTypeSnapshotBusinessServiceIF    hTypeService;

  @Autowired
  private SnapshotBusinessService                   snapshotService;

  @Autowired
  private CommandGateway                            gateway;

  public void pull(String source, String publishId, Integer versionNumber)
  {
    // TODO: Should this be in a transaction??
    try (RemoteClientIF client = service.open(source))
    {
      PublishDTO remotePublish = client.getPublish(publishId).orElseThrow(() -> {
        throw new ProgrammingErrorException("The remote server has not publish data with the uid of [" + publishId + "]");
      });

      Publish publish = this.publishService.getByUid(remotePublish.getUid()).orElseGet(() -> {
        return this.publishService.create(remotePublish);
      });

      // Ensure that the commit has not already been pulled
      this.commitService.getCommit(publish, versionNumber).ifPresent(commit -> {
        throw new ProgrammingErrorException("The commit [" + commit.getUid() + "] has already been pulled");
      });

      CommitDTO remoteCommit = client.getCommit(publish.getUid(), versionNumber).orElseThrow(() -> {
        throw new ProgrammingErrorException("The remote server does not have the commit");
      });

      Commit commit = this.commitService.create(publish, remoteCommit);

      // Copy the metadata for the remote types
      GeoObjectTypeSnapshot root = this.snapshotService.createRoot(commit);

      client.getBusinessTypes(commit.getUid()).forEach(dto -> {
        BusinessTypeSnapshot snapshot = this.bTypeService.create(commit, dto);

        this.snapshotService.createType(snapshot);
      });

      client.getGeoObjectTypes(commit.getUid()).forEach(dto -> {
        GeoObjectTypeSnapshot snapshot = this.gTypeService.create(commit, dto);

        root.addChildSnapshot(snapshot).apply();

        this.snapshotService.createType(snapshot);
      });

      client.getHierarchyTypes(commit.getUid()).forEach(dto -> {
        HierarchyTypeSnapshot snapshot = this.hTypeService.create(commit, dto, root);

        this.snapshotService.createType(snapshot);
      });

      client.getBusinessEdgeTypes(commit.getUid()).forEach(dto -> {
        BusinessEdgeTypeSnapshot snapshot = this.bEdgeTypeService.create(commit, dto);

        this.snapshotService.createType(snapshot);
      });

      int chunk = 0;
      List<RemoteEvent> remoteEvents = null;

      while ( ( remoteEvents = client.getRemoteEvents(commit.getUid(), chunk) ).size() > 0)
      {
        remoteEvents.forEach(event -> this.gateway.sendAndWait(event.toCommand()));

        chunk++;
      }

    }
  }
}
