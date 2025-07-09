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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
import net.geoprism.graph.GraphTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshotQuery;
import net.geoprism.graph.UndirectedGraphTypeSnapshot;
import net.geoprism.graph.UndirectedGraphTypeSnapshotQuery;
import net.geoprism.registry.Commit;
import net.geoprism.registry.CommitHasSnapshotQuery;
import net.geoprism.registry.CommitQuery;
import net.geoprism.registry.Publish;
import net.geoprism.registry.view.EventPublishingConfiguration;

@Service
public class CommitBusinessService implements CommitBusinessServiceIF
{
  @Autowired
  private GeoObjectTypeSnapshotBusinessServiceIF    objectService;

  @Autowired
  private BusinessTypeSnapshotBusinessServiceIF     businessService;

  @Autowired
  private BusinessEdgeTypeSnapshotBusinessServiceIF bEdgeTypeService;

  @Autowired
  private GraphTypeSnapshotBusinessServiceIF        graphService;

  @Autowired
  private SnapshotBusinessService                   snapshotService;

  @Override
  @Transaction
  public void delete(Commit commit)
  {
    this.getGraphSnapshots(commit).forEach(e -> this.graphService.delete(e));

    // Delete all business edge types
    this.getBusinessEdgeTypes(commit).stream().forEach(v -> this.bEdgeTypeService.delete(v));

    // Delete all business types
    this.getBusinessTypes(commit).stream().forEach(v -> this.businessService.delete(v));

    // Delete the non-root snapshots first
    this.getTypes(commit).stream().filter(v -> !v.getIsAbstract()).forEach(v -> this.objectService.delete(v));

    // Delete the abstract snapshots after all the sub snapshots have been
    // deleted
    this.getTypes(commit).stream().filter(v -> !v.getIsRoot()).forEach(v -> this.objectService.delete(v));

    // Delete the root snapshots after all the sub snapshots have been deleted
    this.getTypes(commit).stream().filter(v -> v.isRoot()).forEach(v -> this.objectService.delete(v));

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
    query.LEFT_JOIN_EQ(vQuery.getChild());
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
    query.LEFT_JOIN_EQ(vQuery.getChild());

    try (OIterator<? extends GeoObjectTypeSnapshot> it = query.getIterator())
    {
      return it.getAll().stream().map(b -> (GeoObjectTypeSnapshot) b).collect(Collectors.toList());
    }
  }

  @Override
  public List<BusinessTypeSnapshot> getBusinessTypes(Commit commit)
  {
    QueryFactory factory = new QueryFactory();

    CommitHasSnapshotQuery vQuery = new CommitHasSnapshotQuery(factory);
    vQuery.WHERE(vQuery.getParent().EQ(commit));

    BusinessTypeSnapshotQuery query = new BusinessTypeSnapshotQuery(factory);
    query.LEFT_JOIN_EQ(vQuery.getChild());

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
    query.LEFT_JOIN_EQ(vQuery.getChild());

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
    query.LEFT_JOIN_EQ(vQuery.getChild());

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
    query.LEFT_JOIN_EQ(vQuery.getChild());

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
    query.LEFT_JOIN_EQ(vQuery.getChild());

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
    query.LEFT_JOIN_EQ(vQuery.getChild());
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
    query.LEFT_JOIN_EQ(vQuery.getChild());
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
  public Commit create(Publish publish, EventPublishingConfiguration configuration, int versionNumber, long lastSequenceNumber)
  {
    Commit commit = new Commit();
    commit.setUid(UUID.randomUUID().toString());
    commit.setPublish(publish);
    commit.setLastSequenceNumber(lastSequenceNumber);
    commit.setVersionNumber(versionNumber);
    commit.apply();

    GeoObjectTypeSnapshot root = this.snapshotService.createRoot(commit);

    // Publish snapshots for all business types participating in the graph
    configuration.getBusinessTypes().forEach(businessType -> {
      this.snapshotService.create(commit, businessType);
    });

    // TODO: Publish snapshots for all graph types
    // for (GraphTypeReference gtr : gtrs)
    // {
    // createGraphTypeSnapshot(version, gtr, root);
    // }

    // Publish snapshots for all geo-object types
    configuration.getGeoObjectTypes().forEach(type -> {
      GeoObjectTypeSnapshot snapshot = this.snapshotService.create(commit, type, root);

      root.addChildSnapshot(snapshot).apply();
    });

    // Publish snapshots for all business edge types participating in the
    // graph
    configuration.getBusinessEdgeTypes().forEach(edgeType -> {
      this.snapshotService.create(commit, edgeType, root);
    });

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
  public Commit get(String oid)
  {
    return Commit.get(oid);
  }
}
