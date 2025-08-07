/**
 * Copyright (c) 2023 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism(tm).
 *
 * Geoprism(tm) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either commit 3 of the License, or (at your option) any
 * later commit.
 *
 * Geoprism(tm) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.service.business;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import net.geoprism.graph.BusinessEdgeTypeSnapshot;
import net.geoprism.graph.BusinessEdgeTypeSnapshotQuery;
import net.geoprism.graph.BusinessTypeSnapshot;
import net.geoprism.graph.BusinessTypeSnapshotQuery;
import net.geoprism.graph.DirectedAcyclicGraphTypeSnapshot;
import net.geoprism.graph.DirectedAcyclicGraphTypeSnapshotQuery;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.GeoObjectTypeSnapshotQuery;
import net.geoprism.graph.GraphTypeReference;
import net.geoprism.graph.GraphTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshotQuery;
import net.geoprism.graph.UndirectedGraphTypeSnapshot;
import net.geoprism.graph.UndirectedGraphTypeSnapshotQuery;
import net.geoprism.registry.Commit;
import net.geoprism.registry.CommitHasDependencyQuery;
import net.geoprism.registry.CommitHasSnapshotQuery;
import net.geoprism.registry.CommitQuery;
import net.geoprism.registry.Publish;
import net.geoprism.registry.axon.config.RegistryEventStore;
import net.geoprism.registry.axon.event.remote.RemoteEvent;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.view.CommitDTO;
import net.geoprism.registry.view.PublishDTO;

@Service
public class CommitBusinessService implements CommitBusinessServiceIF
{
  public static final int                           BATCH_SIZE = 1000;

  @Autowired
  private BusinessTypeBusinessServiceIF             bService;

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF         bEdgeService;

  @Autowired
  private GeoObjectTypeSnapshotBusinessServiceIF    gSnapshotService;

  @Autowired
  private BusinessTypeSnapshotBusinessServiceIF     bSnapshotService;

  @Autowired
  private BusinessEdgeTypeSnapshotBusinessServiceIF bEdgeSnapshotService;

  @Autowired
  private GraphTypeSnapshotBusinessServiceIF        graphSnapshotService;

  @Autowired
  private SnapshotBusinessService                   snapshotService;

  @Autowired
  private RegistryEventStore                        store;

  @Override
  @Transaction
  public void delete(Commit commit)
  {
    this.getGraphSnapshots(commit).forEach(e -> this.graphSnapshotService.delete(e));

    // Delete all business edge types
    this.getBusinessEdgeTypes(commit).stream().forEach(v -> this.bEdgeSnapshotService.delete(v));

    // Delete all business types
    this.getBusinessTypes(commit).stream().forEach(v -> this.bSnapshotService.delete(v));

    // Delete the non-root snapshots first
    this.getTypes(commit).stream().filter(v -> !v.getIsAbstract()).forEach(v -> this.gSnapshotService.delete(v));

    // Delete the abstract snapshots after all the sub snapshots have been
    // deleted
    this.getTypes(commit).stream().filter(v -> !v.getIsRoot()).forEach(v -> this.gSnapshotService.delete(v));

    // Delete the root snapshots after all the sub snapshots have been deleted
    this.getTypes(commit).stream().filter(v -> v.isRoot()).forEach(v -> this.gSnapshotService.delete(v));

    this.store.delete(commit);

    commit.delete();
  }

  @Override
  @Request
  public void remove(Commit commit)
  {
    this.delete(commit);
    // new LabeledPropertyGraphUtil(this).removeCommit(commit);
  }

  @Override
  public GeoObjectTypeSnapshot getRootType(Commit commit)
  {
    QueryFactory factory = new QueryFactory();

    CommitHasSnapshotQuery vQuery = new CommitHasSnapshotQuery(factory);
    vQuery.WHERE(vQuery.getParent().EQ(commit));

    GeoObjectTypeSnapshotQuery query = new GeoObjectTypeSnapshotQuery(factory);
    query.WHERE(query.EQ(vQuery.getChild()));
    query.AND(query.getIsRoot().EQ(true));

    try (OIterator<? extends GeoObjectTypeSnapshot> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    return null;
  }

  @Override
  public List<GeoObjectTypeSnapshot> getTypes(Commit commit)
  {
    QueryFactory factory = new QueryFactory();

    CommitHasSnapshotQuery vQuery = new CommitHasSnapshotQuery(factory);
    vQuery.WHERE(vQuery.getParent().EQ(commit));

    GeoObjectTypeSnapshotQuery query = new GeoObjectTypeSnapshotQuery(factory);
    query.WHERE(query.EQ(vQuery.getChild()));

    try (OIterator<? extends GeoObjectTypeSnapshot> it = query.getIterator())
    {
      return it.getAll().stream().map(b -> (GeoObjectTypeSnapshot) b).sorted((a, b) -> a.getIsAbstract().compareTo(b.getIsAbstract())).collect(Collectors.toList());
    }
  }

  @Override
  public List<BusinessTypeSnapshot> getBusinessTypes(Commit commit)
  {
    QueryFactory factory = new QueryFactory();

    CommitHasSnapshotQuery vQuery = new CommitHasSnapshotQuery(factory);
    vQuery.WHERE(vQuery.getParent().EQ(commit));

    BusinessTypeSnapshotQuery query = new BusinessTypeSnapshotQuery(factory);
    query.WHERE(query.EQ(vQuery.getChild()));

    try (OIterator<? extends BusinessTypeSnapshot> it = query.getIterator())
    {
      return it.getAll().stream().map(b -> (BusinessTypeSnapshot) b).collect(Collectors.toList());
    }
  }

  @Override
  public List<BusinessEdgeTypeSnapshot> getBusinessEdgeTypes(Commit commit)
  {
    QueryFactory factory = new QueryFactory();

    CommitHasSnapshotQuery vQuery = new CommitHasSnapshotQuery(factory);
    vQuery.WHERE(vQuery.getParent().EQ(commit));

    BusinessEdgeTypeSnapshotQuery query = new BusinessEdgeTypeSnapshotQuery(factory);
    query.WHERE(query.EQ(vQuery.getChild()));

    try (OIterator<? extends BusinessEdgeTypeSnapshot> it = query.getIterator())
    {
      return it.getAll().stream().map(b -> (BusinessEdgeTypeSnapshot) b).collect(Collectors.toList());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends GraphTypeSnapshot> List<T> getHiearchyTypes(Commit commit)
  {
    QueryFactory factory = new QueryFactory();

    CommitHasSnapshotQuery vQuery = new CommitHasSnapshotQuery(factory);
    vQuery.WHERE(vQuery.getParent().EQ(commit));

    HierarchyTypeSnapshotQuery query = new HierarchyTypeSnapshotQuery(factory);
    query.WHERE(query.EQ(vQuery.getChild()));

    try (OIterator<? extends HierarchyTypeSnapshot> it = query.getIterator())
    {
      return it.getAll().stream().map(b -> (T) b).collect(Collectors.toList());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends GraphTypeSnapshot> List<T> getDirectedAcyclicGraphTypes(Commit commit)
  {
    QueryFactory factory = new QueryFactory();

    CommitHasSnapshotQuery vQuery = new CommitHasSnapshotQuery(factory);
    vQuery.WHERE(vQuery.getParent().EQ(commit));

    DirectedAcyclicGraphTypeSnapshotQuery query = new DirectedAcyclicGraphTypeSnapshotQuery(factory);
    query.WHERE(query.EQ(vQuery.getChild()));

    try (OIterator<? extends DirectedAcyclicGraphTypeSnapshot> it = query.getIterator())
    {
      return it.getAll().stream().map(b -> (T) b).collect(Collectors.toList());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends GraphTypeSnapshot> List<T> getUndirectedGraphTypes(Commit commit)
  {
    QueryFactory factory = new QueryFactory();

    CommitHasSnapshotQuery vQuery = new CommitHasSnapshotQuery(factory);
    vQuery.WHERE(vQuery.getParent().EQ(commit));

    UndirectedGraphTypeSnapshotQuery query = new UndirectedGraphTypeSnapshotQuery(factory);
    query.WHERE(query.EQ(vQuery.getChild()));

    try (OIterator<? extends UndirectedGraphTypeSnapshot> it = query.getIterator())
    {
      return it.getAll().stream().map(b -> (T) b).collect(Collectors.toList());
    }
  }

  @Override
  public List<GraphTypeSnapshot> getGraphSnapshots(Commit commit)
  {
    List<GraphTypeSnapshot> snapshots = new ArrayList<GraphTypeSnapshot>();

    snapshots.addAll(this.getHiearchyTypes(commit));
    snapshots.addAll(this.getDirectedAcyclicGraphTypes(commit));
    snapshots.addAll(this.getUndirectedGraphTypes(commit));

    return snapshots;
  }

  @Override
  public GeoObjectTypeSnapshot getSnapshot(Commit commit, String typeCode)
  {
    QueryFactory factory = new QueryFactory();

    CommitHasSnapshotQuery vQuery = new CommitHasSnapshotQuery(factory);
    vQuery.WHERE(vQuery.getParent().EQ(commit));

    GeoObjectTypeSnapshotQuery query = new GeoObjectTypeSnapshotQuery(factory);
    query.WHERE(query.EQ(vQuery.getChild()));
    query.AND(query.getCode().EQ(typeCode));

    try (OIterator<? extends GeoObjectTypeSnapshot> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    throw new ProgrammingErrorException("Unable to find Geo-Object Type Snapshot definition for the type [" + typeCode + "]");
  }

  @Override
  public HierarchyTypeSnapshot getHierarchyType(Commit commit, String typeCode)
  {
    QueryFactory factory = new QueryFactory();

    CommitHasSnapshotQuery vQuery = new CommitHasSnapshotQuery(factory);
    vQuery.WHERE(vQuery.getParent().EQ(commit));

    HierarchyTypeSnapshotQuery query = new HierarchyTypeSnapshotQuery(factory);
    query.WHERE(query.EQ(vQuery.getChild()));
    query.AND(query.getCode().EQ(typeCode));

    try (OIterator<? extends HierarchyTypeSnapshot> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    throw new ProgrammingErrorException("Unable to find Hierarchy Type Snapshot definition for the type [" + typeCode + "]");
  }

  @Override
  @Transaction
  public Commit create(Publish publish, int versionNumber, long lastOriginalIndex)
  {
    PublishDTO configuration = publish.toDTO();

    Commit commit = create(publish, new CommitDTO(UUID.randomUUID().toString(), publish.getUid(), versionNumber, lastOriginalIndex));

    GeoObjectTypeSnapshot root = this.snapshotService.createRoot(commit);

    // Publish snapshots for all business types participating in the graph
    configuration.getBusinessTypes().forEach(code -> {
      this.snapshotService.createSnapshot(commit, this.bService.getByCode(code));
    });

    // Publish snapshots for all abstract geo object types
    configuration.getGeoObjectTypes().map(code -> ServerGeoObjectType.get(code)).filter(t -> t.getIsAbstract()).forEach(type -> {
      this.snapshotService.createSnapshot(commit, type, root);
    });

    // Publish snapshots for all child geo object types
    configuration.getGeoObjectTypes().map(code -> ServerGeoObjectType.get(code)).filter(t -> !t.getIsAbstract()).forEach(type -> {
      this.snapshotService.createSnapshot(commit, type, root);
    });

    configuration.getHierarchyTypes().forEach(code -> {
      this.snapshotService.createSnapshot(commit, ServerHierarchyType.get(code), root);
    });

    configuration.getDagTypes().forEach(code -> {
      this.snapshotService.createSnapshot(commit, new GraphTypeReference(GraphTypeSnapshot.DIRECTED_ACYCLIC_GRAPH_TYPE, code), root);
    });

    configuration.getUndirectedTypes().forEach(code -> {
      this.snapshotService.createSnapshot(commit, new GraphTypeReference(GraphTypeSnapshot.UNDIRECTED_GRAPH_TYPE, code), root);
    });

    configuration.getBusinessEdgeTypes().forEach(code -> {
      this.snapshotService.createSnapshot(commit, this.bEdgeService.getByCodeOrThrow(code), root);
    });

    return commit;
  }

  @Override
  @Transaction
  public Commit create(Publish publish, CommitDTO dto)
  {
    Commit commit = new Commit();
    commit.setUid(dto.getUid());
    commit.setPublish(publish);
    commit.setLastOriginGlobalIndex(dto.getLastOriginGlobalIndex());
    commit.setVersionNumber(dto.getVersionNumber());
    commit.apply();

    return commit;
  }

  @Override
  public List<? extends Commit> getAll()
  {
    CommitQuery query = new CommitQuery(new QueryFactory());

    try (OIterator<? extends Commit> it = query.getIterator())
    {
      return it.getAll();
    }
  }

  @Override
  public Optional<Commit> getCommit(String uid)
  {
    CommitQuery query = new CommitQuery(new QueryFactory());
    query.WHERE(query.getUid().EQ(uid));
    query.ORDER_BY_DESC(query.getVersionNumber());

    try (OIterator<? extends Commit> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return Optional.ofNullable(it.next());
      }
    }

    return Optional.empty();
  }

  @Override
  public Commit getOrThrow(String uid)
  {
    return this.getCommit(uid).orElseThrow(() -> {
      throw new ProgrammingErrorException("Unable to find commit with uid of [" + uid + "]");
    });
  }

  @Override
  public List<Commit> getCommits(Publish publish)
  {
    CommitQuery query = new CommitQuery(new QueryFactory());
    query.WHERE(query.getPublish().EQ(publish));
    query.ORDER_BY_DESC(query.getVersionNumber());

    try (OIterator<? extends Commit> it = query.getIterator())
    {
      return new LinkedList<Commit>(it.getAll());
    }
  }

  @Override
  public Optional<Commit> getLatest(Publish publish)
  {
    CommitQuery query = new CommitQuery(new QueryFactory());
    query.WHERE(query.getPublish().EQ(publish));
    query.ORDER_BY_DESC(query.getVersionNumber());

    try (OIterator<? extends Commit> iterator = query.getIterator())
    {
      if (iterator.hasNext())
      {
        return Optional.of(iterator.next());
      }
    }

    return Optional.empty();
  }

  @Override
  public List<Commit> getDependencies(Commit commit)
  {
    QueryFactory factory = new QueryFactory();

    CommitHasDependencyQuery vQuery = new CommitHasDependencyQuery(factory);
    vQuery.WHERE(vQuery.getChild().EQ(commit));

    CommitQuery query = new CommitQuery(factory);
    query.WHERE(query.getOid().EQ(vQuery.getParent().oid()));

    try (OIterator<? extends Commit> iterator = query.getIterator())
    {
      return new LinkedList<>(iterator.getAll());
    }
  }

  @Override
  public List<RemoteEvent> getRemoteEvents(Commit commit, Integer chunk)
  {
    Long startIndex = this.store.firstIndexFor(commit).map(seq -> seq + ( chunk * BATCH_SIZE )).orElseThrow(() -> {
      throw new ProgrammingErrorException("Commit [" + commit.getUid() + "] does not have any events");
    });

    Long lastIndex = this.store.lastIndexFor(commit).orElseThrow(() -> {
      throw new ProgrammingErrorException("Commit [" + commit.getUid() + "] does not have any events");
    });

    DomainEventStream stream = this.store.readEvents(commit, startIndex, lastIndex, BATCH_SIZE);

    return stream.asStream() //
        .filter(m -> RemoteEvent.class.isAssignableFrom(m.getPayloadType())) //
        .map(m -> (RemoteEvent) m.getPayload()).toList();
  }

}
