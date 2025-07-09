package net.geoprism.registry.service.business;

import java.util.List;

import net.geoprism.graph.BusinessEdgeTypeSnapshot;
import net.geoprism.graph.BusinessTypeSnapshot;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.GraphTypeSnapshot;
import net.geoprism.registry.Commit;
import net.geoprism.registry.Publish;
import net.geoprism.registry.view.EventPublishingConfiguration;

public interface CommitBusinessServiceIF
{

  void remove(Commit commit);

  void delete(Commit commit);

  GeoObjectTypeSnapshot getRootType(Commit commit);

  List<GeoObjectTypeSnapshot> getTypes(Commit commit);

  List<BusinessTypeSnapshot> getBusinessTypes(Commit commit);

  List<BusinessEdgeTypeSnapshot> getBusinessEdgeTypes(Commit commit);

  <T extends GraphTypeSnapshot> List<T> getHiearchyTypes(Commit commit);

  <T extends GraphTypeSnapshot> List<T> getDirectedAcyclicGraphTypes(Commit commit);

  <T extends GraphTypeSnapshot> List<T> getUndirectedGraphTypes(Commit commit);

  List<GraphTypeSnapshot> getGraphSnapshots(Commit commit);

  GeoObjectTypeSnapshot getSnapshot(Commit commit, String typeCode);

  GraphTypeSnapshot getHierarchyType(Commit commit, String typeCode);

  List<? extends Commit> getAll();

  Commit get(String oid);

  Commit create(Publish publish, EventPublishingConfiguration configuration, int versionNumber, long lastSequenceNumber);

}
