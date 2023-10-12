/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.metadata.MdEdge;
import com.runwaysdk.system.metadata.MdVertex;

import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.graph.lpg.TreeStrategyConfiguration;
import net.geoprism.graph.lpg.business.GeoObjectTypeSnapshotBusinessServiceIF;
import net.geoprism.graph.lpg.business.HierarchyTypeSnapshotBusinessServiceIF;
import net.geoprism.graph.lpg.service.AbstractGraphVersionPublisherService;
import net.geoprism.registry.InvalidMasterListException;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.business.HierarchyTypeBusinessServiceIF;
import net.geoprism.registry.model.ServerChildTreeNode;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.progress.Progress;
import net.geoprism.registry.progress.ProgressService;

@Service
public class TreeStrategyPublisherService extends AbstractGraphVersionPublisherService
{
  private static class Snapshot
  {
    private ServerGeoObjectIF node;

    private MdEdge            hierarchy;

    private VertexObject      parent;

    public Snapshot(ServerGeoObjectIF node, MdEdge hierarchy, VertexObject parent)
    {
      super();
      this.node = node;
      this.hierarchy = hierarchy;
      this.parent = parent;
    }

    public Snapshot(ServerGeoObjectIF node)
    {
      this(node, null, null);
    }

  }

  private static class TreeState extends State
  {
    private Set<String>     uids;

    private Stack<Snapshot> stack;

    public TreeState(LabeledPropertyGraphSynchronization synchronization, LabeledPropertyGraphTypeVersion version)
    {
      super(synchronization, version);

      this.uids = new TreeSet<String>();
      this.stack = new Stack<Snapshot>();
    }

  }

  @Autowired
  private GeoObjectBusinessServiceIF             objectService;

  @Autowired
  private GeoObjectTypeBusinessServiceIF         typeService;

  @Autowired
  private HierarchyTypeBusinessServiceIF         hierarchyService;

  @Autowired
  private GeoObjectTypeSnapshotBusinessServiceIF tSnapshotService;

  @Autowired
  private HierarchyTypeSnapshotBusinessServiceIF hSnapshotService;

  public void publish(TreeStrategyConfiguration configuration, LabeledPropertyGraphTypeVersion version)
  {
    TreeState state = new TreeState(null, version);

    long startTime = System.currentTimeMillis();

    System.out.println("Started publishing");

    version.lock();

    long count = 0;

    try
    {
      LabeledPropertyGraphType type = version.getGraphType();

      ProgressService.put(type.getOid(), new Progress(0L, 1L, version.getOid()));

      try
      {

        if (!type.isValid())
        {
          throw new InvalidMasterListException();
        }

        ServerHierarchyType hierarchyType = ServerHierarchyType.get(type.getHierarchy());
        HierarchyTypeSnapshot graphEdge = this.hSnapshotService.get(version, hierarchyType.getCode());
        MdEdge mdEdge = graphEdge.getGraphMdEdge();

        List<ServerGeoObjectType> geoObjectTypes = this.hierarchyService.getAllTypes(hierarchyType, false);

        geoObjectTypes.stream().filter(t -> t.getIsAbstract()).collect(Collectors.toList()).forEach(t -> {
          geoObjectTypes.addAll(this.typeService.getSubtypes(t));
        });

        Date forDate = version.getForDate();

        ServerGeoObjectIF root = this.objectService.getGeoObjectByCode(configuration.getCode(), configuration.getTypeCode());

        if (root != null && geoObjectTypes.contains(root.getType()))
        {
          root.setDate(forDate);

          if (!root.getInvalid() && root.getExists(forDate))
          {
            state.stack.push(new Snapshot(root));
          }
        }

        while (!state.stack.isEmpty())
        {
          publish(state, hierarchyType, mdEdge, geoObjectTypes, forDate);

          count++;

          ProgressService.put(type.getOid(), new Progress(count, ( count + state.stack.size() ), version.getOid()));
        }

        ProgressService.put(type.getOid(), new Progress(1L, 1L, version.getOid()));
      }
      finally
      {
        ProgressService.remove(type.getOid());
      }
    }
    finally
    {
      version.unlock();
    }

    System.out.println("Finished publishing: " + ( ( System.currentTimeMillis() - startTime ) / 1000 ) + " sec");
  }

  @Transaction
  protected void publish(TreeState state, ServerHierarchyType hierarchyType, MdEdge mdEdge, List<ServerGeoObjectType> geoObjectTypes, Date forDate)
  {
    Snapshot snapshot = state.stack.pop();

    long startTime = System.currentTimeMillis();
    GeoObjectTypeSnapshot graphVertex = this.tSnapshotService.get(state.version, snapshot.node.getType().getCode());
    MdVertex mdVertex = graphVertex.getGraphMdVertex();

    VertexObject vertex = null;

    if (!state.uids.contains(snapshot.node.getUid()))
    {
      vertex = this.publish(state, mdVertex, this.objectService.toGeoObject(snapshot.node, forDate, false));

      final VertexObject parent = vertex;

      ServerChildTreeNode node = this.objectService.getChildGeoObjects(snapshot.node, hierarchyType, null, false, forDate);
      List<ServerChildTreeNode> children = node.getChildren();

      if (children.size() > 0)
      {

        for (ServerChildTreeNode childNode : children)
        {
          ServerGeoObjectIF child = childNode.getGeoObject();
          child.setDate(forDate);

          if (child.getExists(forDate) && !child.getInvalid() && geoObjectTypes.contains(child.getType()))
          {
            state.stack.push(new Snapshot(child, mdEdge, parent));
          }
        }
      }

      state.uids.add(snapshot.node.getUid());
    }
    else
    {
      vertex = this.get(mdVertex, snapshot.node.getUid());
    }

    if (snapshot.parent != null && snapshot.hierarchy != null)
    {

      snapshot.parent.addChild(vertex, snapshot.hierarchy.definesType()).apply();
    }

    // System.out.println("Items remaining: " + stack.size() + " - Row time: " +
    // ( System.currentTimeMillis() - startTime ) + " ms");
  }

  private VertexObject get(MdVertex mdVertex, String uid)
  {
    MdVertexDAOIF mdVertexDAO = (MdVertexDAOIF) BusinessFacade.getEntityDAO(mdVertex);
    MdAttributeDAOIF attribute = mdVertexDAO.definesAttribute(RegistryConstants.UUID);

    StringBuffer statement = new StringBuffer();
    statement.append("SELECT FROM " + mdVertex.getDbClassName());
    statement.append(" WHERE " + attribute.getColumnName() + " = :uid");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());
    query.setParameter("uid", uid);

    return query.getSingleResult();
  }

}
