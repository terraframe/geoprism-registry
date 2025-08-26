package net.geoprism.registry.service.business;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Request;

import net.geoprism.graph.BusinessEdgeTypeSnapshot;
import net.geoprism.graph.BusinessTypeSnapshot;
import net.geoprism.graph.DirectedAcyclicGraphTypeSnapshot;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.UndirectedGraphTypeSnapshot;
import net.geoprism.registry.Commit;
import net.geoprism.registry.Publish;
import net.geoprism.registry.axon.event.remote.RemoteEvent;
import net.geoprism.registry.graph.DataSource;
import net.geoprism.registry.view.CommitDTO;
import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeAndCode;

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
  private GraphTypeSnapshotBusinessServiceIF        graphTypeService;

  @Autowired
  private DataSourceBusinessServiceIF               sourceService;

  @Autowired
  private SnapshotBusinessService                   snapshotService;

  @Autowired
  private CommandGateway                            gateway;

  @Autowired
  private GraphRepoServiceIF                        metadataService;

  @Request
  public Commit pull(String source, String publishId, List<TypeAndCode> exclusions)
  {
    try (RemoteClientIF client = service.open(source))
    {

      CommitDTO remoteCommit = client.getLatest(publishId).orElseThrow(() -> {
        throw new ProgrammingErrorException("The remote server does not have the commit");
      });

      return pull(client, remoteCommit, exclusions);
    }
  }

  // TODO: Should this be in a transaction??
  @Request
  public Commit pull(RemoteClientIF client, CommitDTO remoteCommit, List<TypeAndCode> exclusions)
  {
    Publish publish = getOrCreate(client, remoteCommit.getPublishId(), exclusions);

    // Determine if the commit has already been pulled
    Optional<Commit> optional = this.commitService.getCommit(remoteCommit.getUid());

    if (optional.isPresent())
    {
      System.out.println("Skipping commit [" + remoteCommit.getUid() + "] it has already been pulled");

      return optional.get();
    }

    // First pull all of the commits which this commit is dependent upon
    List<Commit> dependencies = client.getDependencies(remoteCommit.getUid()) //
        .stream() //
        .map(commit -> this.pull(client, commit, new LinkedList<>())) //
        .toList();

    Commit commit = this.commitService.create(publish, remoteCommit);

    dependencies.forEach(dependency -> commit.addDependency(dependency).apply());

    client.getDataSources(commit.getUid()).forEach(dto -> {
      DataSource source = this.sourceService.getByCode(dto.getCode()).orElseGet(() -> {
        return this.sourceService.apply(dto);
      });
      
      this.commitService.addSource(commit, source);
    });

    // Copy the metadata for the remote types
    GeoObjectTypeSnapshot root = this.snapshotService.createRoot(commit);

    client.getBusinessTypes(commit.getUid()).forEach(element -> {
      BusinessTypeSnapshot snapshot = this.bTypeService.create(commit, element.getAsJsonObject());

      this.snapshotService.createType(snapshot);
    });

    client.getGeoObjectTypes(commit.getUid()).forEach(element -> {
      GeoObjectTypeSnapshot snapshot = this.gTypeService.create(commit, element.getAsJsonObject());

      this.snapshotService.createType(snapshot);
    });

    client.getHierarchyTypes(commit.getUid()).forEach(element -> {
      HierarchyTypeSnapshot snapshot = (HierarchyTypeSnapshot) this.graphTypeService.create(commit, element.getAsJsonObject(), root);

      this.snapshotService.createType(snapshot, root);
    });

    client.getDirectedAcyclicGraphTypes(commit.getUid()).forEach(element -> {
      DirectedAcyclicGraphTypeSnapshot snapshot = (DirectedAcyclicGraphTypeSnapshot) this.graphTypeService.create(commit, element.getAsJsonObject(), root);

      this.snapshotService.createType(snapshot, root);
    });

    client.getUndirectedGraphTypes(commit.getUid()).forEach(element -> {
      UndirectedGraphTypeSnapshot snapshot = (UndirectedGraphTypeSnapshot) this.graphTypeService.create(commit, element.getAsJsonObject(), root);

      this.snapshotService.createType(snapshot, root);
    });

    client.getBusinessEdgeTypes(commit.getUid()).forEach(element -> {
      BusinessEdgeTypeSnapshot snapshot = this.bEdgeTypeService.create(commit, element.getAsJsonObject());

      this.snapshotService.createType(snapshot);
    });

    this.metadataService.refreshMetadataCache();

    int chunk = 0;
    List<RemoteEvent> remoteEvents = null;

    PublishDTO dto = publish.toDTO();

    while ( ( remoteEvents = client.getRemoteEvents(commit.getUid(), chunk) ).size() > 0)
    {
      remoteEvents.stream() //
          .filter(event -> event.isValid(dto))//
          .forEach(event -> {
            this.gateway.sendAndWait(event.toCommand());
          });

      chunk++;
    }

    return commit;

  }

  protected Publish getOrCreate(RemoteClientIF client, final String publishId, List<TypeAndCode> exclusions)
  {
    return this.publishService.getByUid(publishId).orElseGet(() -> {
      PublishDTO dto = client.getPublish(publishId).orElseThrow(() -> {
        throw new ProgrammingErrorException("The remote server has no publish data with the uid of [" + publishId + "]");
      });
      dto.setExclusions(exclusions);

      return this.publishService.create(dto);
    });
  }
}
