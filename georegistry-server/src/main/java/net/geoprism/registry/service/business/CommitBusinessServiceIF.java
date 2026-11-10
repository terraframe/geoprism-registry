/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.service.business;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import net.geoprism.graph.BusinessEdgeTypeSnapshot;
import net.geoprism.graph.BusinessTypeSnapshot;
import net.geoprism.graph.ConceptClassSnapshot;
import net.geoprism.graph.ConceptEdgeTypeSnapshot;
import net.geoprism.graph.ConceptSetSnapshot;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.GraphTypeSnapshot;
import net.geoprism.registry.Commit;
import net.geoprism.registry.CommitHasDataSource;
import net.geoprism.registry.Publish;
import net.geoprism.registry.axon.event.remote.RemoteEvent;
import net.geoprism.registry.graph.DataSource;
import net.geoprism.registry.view.CommitDTO;

public interface CommitBusinessServiceIF
{

  void remove(Commit commit);

  void delete(Commit commit);

  GeoObjectTypeSnapshot getRootType(Commit commit);

  List<GeoObjectTypeSnapshot> getTypes(Commit commit);

  List<BusinessTypeSnapshot> getBusinessTypes(Commit commit);

  List<ConceptClassSnapshot> getConceptClasses(Commit commit);

  List<ConceptSetSnapshot> getConceptSets(Commit commit);

  <T extends GraphTypeSnapshot> List<T> getHiearchyTypes(Commit commit);

  <T extends GraphTypeSnapshot> List<T> getDirectedAcyclicGraphTypes(Commit commit);

  <T extends GraphTypeSnapshot> List<T> getUndirectedGraphTypes(Commit commit);

  List<BusinessEdgeTypeSnapshot> getBusinessEdgeTypes(Commit commit);

  List<ConceptEdgeTypeSnapshot> getConceptEdgeTypes(Commit commit);

  List<GraphTypeSnapshot> getGraphSnapshots(Commit commit);

  GeoObjectTypeSnapshot getSnapshot(Commit commit, String typeCode);

  GraphTypeSnapshot getHierarchyType(Commit commit, String typeCode);

  List<? extends Commit> getAll();

  List<Commit> getCommits(Publish publish);

  Optional<Commit> getLatest(Publish publish);

  Optional<Commit> getCommit(String uid);

  Commit getOrThrow(String uid);

  Commit create(Publish publish, int versionNumber, long lastOriginGlobalIndex);

  Commit create(Publish publish, CommitDTO dto);

  List<Commit> getDependencies(Commit commit);

  List<Commit> getDependents(Commit commit);

  List<DataSource> getSources(Commit commit);

  CommitHasDataSource addSource(Commit commit, DataSource source);

  long getEventCount(Commit commit);

  Stream<RemoteEvent> getRemoteEvents(Commit commit);

  List<RemoteEvent> getRemoteEvents(Commit commit, Integer chunk);
}
