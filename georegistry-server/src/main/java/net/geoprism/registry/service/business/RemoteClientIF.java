package net.geoprism.registry.service.business;

import java.util.List;
import java.util.Optional;

import net.geoprism.registry.axon.event.remote.RemoteEvent;
import net.geoprism.registry.view.BusinessEdgeTypeSnapshotDTO;
import net.geoprism.registry.view.BusinessTypeSnapshotDTO;
import net.geoprism.registry.view.CommitDTO;
import net.geoprism.registry.view.GeoObjectTypeSnapshotDTO;
import net.geoprism.registry.view.HierarchyTypeSnapshotDTO;
import net.geoprism.registry.view.PublishDTO;

public interface RemoteClientIF extends AutoCloseable
{
  public abstract void close();

  public abstract Optional<PublishDTO> getPublish(String publishId);

  public abstract Optional<CommitDTO> getCommit(String uid, Integer versionNumber);

  public abstract List<BusinessTypeSnapshotDTO> getBusinessTypes(String commitId);

  public abstract List<GeoObjectTypeSnapshotDTO> getGeoObjectTypes(String commitId);

  public abstract List<BusinessEdgeTypeSnapshotDTO> getBusinessEdgeTypes(String uid);

  public abstract List<HierarchyTypeSnapshotDTO> getHierarchyTypes(String uid);

  public abstract List<RemoteEvent> getRemoteEvents(String uid, int chunk);
}
