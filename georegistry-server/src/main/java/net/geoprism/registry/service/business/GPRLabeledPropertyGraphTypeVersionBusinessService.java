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
package net.geoprism.registry.service.business;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutableJob;
import com.runwaysdk.system.scheduler.JobHistoryQuery;

import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.GraphTypeReference;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeEntry;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.graph.PublishLabeledPropertyGraphTypeVersionJob;
import net.geoprism.graph.PublishLabeledPropertyGraphTypeVersionJobQuery;
import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.etl.DuplicateJobException;
import net.geoprism.registry.lpg.LPGPublishProgressMonitorIF;
import net.geoprism.registry.lpg.StrategyConfiguration;
import net.geoprism.registry.lpg.TreeStrategyConfiguration;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

@Service
@Primary
public class GPRLabeledPropertyGraphTypeVersionBusinessService extends LabeledPropertyGraphTypeVersionBusinessService implements LabeledPropertyGraphTypeVersionBusinessServiceIF
{
  private class StackItem
  {
    private GeoObjectTypeSnapshot parent;

    private ServerGeoObjectType   type;

    public StackItem(ServerGeoObjectType type, GeoObjectTypeSnapshot parent)
    {
      this.type = type;
      this.parent = parent;
    }

  }

  @Autowired
  private HierarchyTypeBusinessServiceIF    hierarchyService;

  @Autowired
  private GeoObjectTypeBusinessServiceIF    typeService;

  @Autowired
  private BusinessTypeBusinessServiceIF     bTypeService;

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF bEdgeService;

  @Autowired
  private TreeStrategyPublisherService      treePublisherService;

  @Autowired
  private GraphPublisherService             graphPublisherService;

  @Autowired
  private SnapshotBusinessService           snapshotService;

  @Override
  @Transaction
  public void delete(LabeledPropertyGraphTypeVersion version)
  {
    // Delete all jobs
    List<ExecutableJob> jobs = this.getJobs(version);

    for (ExecutableJob job : jobs)
    {
      job.delete();
    }

    super.delete(version);
  }

  @Override
  public void publishNoAuth(LPGPublishProgressMonitorIF monitor, LabeledPropertyGraphTypeVersion version)
  {
    LabeledPropertyGraphType type = version.getGraphType();
    StrategyConfiguration configuration = type.toStrategyConfiguration();

    if (configuration instanceof TreeStrategyConfiguration)
    {
      this.treePublisherService.publish(monitor, (TreeStrategyConfiguration) configuration, version);
    }
    else if (type.getStrategyType().equals(LabeledPropertyGraphType.GRAPH))
    {
      this.graphPublisherService.publish(monitor, version);
    }
    else
    {
      throw new UnsupportedOperationException("Unsupported publisher " + ( configuration == null ? "null" : configuration.getClass().getName() ));
    }
  }

  @Override
  public void createPublishJob(LabeledPropertyGraphTypeVersion version)
  {
    QueryFactory factory = new QueryFactory();

    PublishLabeledPropertyGraphTypeVersionJobQuery query = new PublishLabeledPropertyGraphTypeVersionJobQuery(factory);
    query.WHERE(query.getVersion().EQ(version));

    JobHistoryQuery q = new JobHistoryQuery(factory);
    q.WHERE(q.getStatus().containsAny(AllJobStatus.NEW, AllJobStatus.QUEUED, AllJobStatus.RUNNING));
    q.AND(q.job(query));

    if (q.getCount() > 0)
    {
      throw new DuplicateJobException("This version has already been queued for publishing");
    }

    SingleActorDAOIF currentUser = Session.getCurrentSession().getUser();

    PublishLabeledPropertyGraphTypeVersionJob job = new PublishLabeledPropertyGraphTypeVersionJob();
    job.setRunAsUserId(currentUser.getOid());
    job.setVersion(version);
    job.setGraphType(version.getGraphType());
    job.apply();

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.PUBLISH_JOB_CHANGE, null));

    job.start();
  }

  public List<ExecutableJob> getJobs(LabeledPropertyGraphTypeVersion version)
  {
    LinkedList<ExecutableJob> jobs = new LinkedList<ExecutableJob>();

    PublishLabeledPropertyGraphTypeVersionJobQuery pmlvj = new PublishLabeledPropertyGraphTypeVersionJobQuery(new QueryFactory());
    pmlvj.WHERE(pmlvj.getVersion().EQ(version));

    try (OIterator<? extends PublishLabeledPropertyGraphTypeVersionJob> it = pmlvj.getIterator())
    {
      jobs.addAll(it.getAll());
    }

    return jobs;
  }

  @Override
  @Transaction
  public LabeledPropertyGraphTypeVersion create(LabeledPropertyGraphTypeEntry listEntry, boolean working, int versionNumber)
  {
    LabeledPropertyGraphTypeVersion version = super.create(listEntry, working, versionNumber);

    GeoObjectTypeSnapshot root = this.snapshotService.createRoot(version);

    LabeledPropertyGraphType lpgt = version.getGraphType();
    List<GraphTypeReference> gtrs = lpgt.getGraphTypeReferences();
    List<String> goTypeCodes = lpgt.getGeoObjectTypeCodesList();

    List<String> businessTypeCodes = lpgt.getBusinessTypeCodesList();
    List<String> businessEdgeCodesList = lpgt.getBusinessEdgeCodesList();

    // Publish snapshots for all business types participating in the graph
    for (String businessTypeCode : businessTypeCodes)
    {
      BusinessType businessType = this.bTypeService.getByCode(businessTypeCode);

      this.snapshotService.createSnapshot(version, businessType);
    }

    if (goTypeCodes.size() > 0)
    {
      // Publish snapshots for all graph types
      for (GraphTypeReference gtr : gtrs)
      {
        this.snapshotService.createSnapshot(version, gtr, root);
      }

      // Publish snapshots for all geo-object types
      for (String goTypeCode : goTypeCodes)
      {
        GeoObjectTypeSnapshot snapshot = this.snapshotService.createSnapshot(version, ServerGeoObjectType.get(goTypeCode), root);
        root.addChildSnapshot(snapshot).apply();
      }
    }
    else if (lpgt.getStrategyType().equals(LabeledPropertyGraphType.TREE))
    {
      ServerHierarchyType hierarchy = ServerHierarchyType.get(lpgt.getHierarchy());

      this.snapshotService.createSnapshot(version, hierarchy, root);

      Stack<StackItem> stack = new Stack<StackItem>();

      this.hierarchyService.getDirectRootNodes(hierarchy).forEach(type -> stack.push(new StackItem(type, root)));

      while (!stack.isEmpty())
      {
        StackItem item = stack.pop();
        ServerGeoObjectType type = item.type;

        GeoObjectTypeSnapshot parent = this.snapshotService.createSnapshot(version, type, root);

        if (type.getIsAbstract())
        {
          this.typeService.getSubtypes(type).forEach(subtype -> {
            this.snapshotService.createSnapshot(version, subtype, parent);
          });
        }

        if (item.parent != null)
        {
          item.parent.addChildSnapshot(parent).apply();
        }

        this.hierarchyService.getChildren(hierarchy, type).forEach(child -> {
          stack.push(new StackItem(child, parent));
        });
      }
    }
    else
    {
      throw new UnsupportedOperationException();
    }

    // Publish snapshots for all business edge types participating in the
    // graph
    for (String businessEdgeCode : businessEdgeCodesList)
    {
      BusinessEdgeType edgeType = this.bEdgeService.getByCode(businessEdgeCode);

      this.snapshotService.createSnapshot(version, edgeType, root);
    }

    return version;
  }
}
