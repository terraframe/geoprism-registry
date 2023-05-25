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
package net.geoprism.registry;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.business.rbac.Authenticate;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutableJob;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryQuery;

import net.geoprism.rbac.RoleConstants;
import net.geoprism.registry.etl.DuplicateJobException;
import net.geoprism.registry.etl.PublishLabeledPropertyGraphTypeVersionJob;
import net.geoprism.registry.etl.PublishLabeledPropertyGraphTypeVersionJobQuery;
import net.geoprism.registry.graph.StrategyPublisher;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.progress.Progress;
import net.geoprism.registry.progress.ProgressService;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

public class LabeledPropertyGraphTypeVersion extends LabeledPropertyGraphTypeVersionBase implements LabeledVersion
{
  private static class StackItem
  {
    private GeoObjectTypeSnapshot parent;

    private ServerGeoObjectType   type;

    public StackItem(ServerGeoObjectType type, GeoObjectTypeSnapshot parent)
    {
      this.type = type;
      this.parent = parent;
    }

  }

  private static final long  serialVersionUID = -351397872;

  public static final String PREFIX           = "g_";

  public static final String SPLIT            = "__";

  public static final String TYPE_CODE        = "typeCode";

  public static final String ATTRIBUTES       = "attributes";

  public static final String HIERARCHIES      = "hierarchies";

  public static final String PERIOD           = "period";

  public LabeledPropertyGraphTypeVersion()
  {
    super();
  }

  @Override
  @Transaction
  public void delete()
  {
    // Delete all jobs
    List<ExecutableJob> jobs = this.getJobs();

    for (ExecutableJob job : jobs)
    {
      job.delete();
    }

    this.getHierarchies().forEach(e -> HierarchyTypeSnapshot.get(e.getOid()).delete());

    // Delete the non-root snapshots first
    this.getTypes().stream().filter(v -> !v.getIsAbstract()).forEach(v -> GeoObjectTypeSnapshot.get(v.getOid()).delete());

    // Delete the abstract snapshots after all the sub snapshots have been
    // deleted
    this.getTypes().stream().filter(v -> !v.getIsRoot()).forEach(v -> GeoObjectTypeSnapshot.get(v.getOid()).delete());

    // Delete the root snapshots after all the sub snapshots have been deleted
    this.getTypes().stream().filter(v -> v.isRoot()).forEach(v -> GeoObjectTypeSnapshot.get(v.getOid()).delete());

    super.delete();
  }

  @Override
  @Transaction
  @Authenticate
  public void remove()
  {
    this.delete();
  }

  public VertexObject getVertex(String uid, String typeCode)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);

    MdVertexDAOIF mdVertex = (MdVertexDAOIF) BusinessFacade.getEntityDAO(this.getSnapshot(type).getGraphMdVertex());
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(RegistryConstants.UUID);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" WHERE " + mdAttribute.getColumnName() + " = :uid");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());
    query.setParameter("uid", uid);

    return query.getSingleResult();
  }

  public GeoObjectTypeSnapshot getRootType()
  {
    return GeoObjectTypeSnapshot.getRoot(this);
  }

  public List<GeoObjectTypeSnapshot> getTypes()
  {
    GeoObjectTypeSnapshotQuery query = new GeoObjectTypeSnapshotQuery(new QueryFactory());
    query.WHERE(query.getVersion().EQ(this));

    try (OIterator<? extends GeoObjectTypeSnapshot> it = query.getIterator())
    {
      return it.getAll().stream().map(b -> (GeoObjectTypeSnapshot) b).collect(Collectors.toList());
    }
  }

  public List<HierarchyTypeSnapshot> getHierarchies()
  {
    HierarchyTypeSnapshotQuery query = new HierarchyTypeSnapshotQuery(new QueryFactory());
    query.WHERE(query.getVersion().EQ(this));

    try (OIterator<? extends HierarchyTypeSnapshot> it = query.getIterator())
    {
      return it.getAll().stream().map(b -> (HierarchyTypeSnapshot) b).collect(Collectors.toList());
    }
  }

  public GeoObjectTypeSnapshot getSnapshot(ServerGeoObjectType type)
  {
    GeoObjectTypeSnapshotQuery query = new GeoObjectTypeSnapshotQuery(new QueryFactory());
    query.WHERE(query.getVersion().EQ(this));
    query.AND(query.getCode().EQ(type.getCode()));

    try (OIterator<? extends GeoObjectTypeSnapshot> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    throw new ProgrammingErrorException("Unable to find Geo-Object Type Snapshot definition for the type [" + type.getCode() + "]");
  }

  public HierarchyTypeSnapshot getSnapshot(ServerHierarchyType type)
  {
    HierarchyTypeSnapshotQuery query = new HierarchyTypeSnapshotQuery(new QueryFactory());
    query.WHERE(query.getVersion().EQ(this));
    query.AND(query.getCode().EQ(type.getCode()));

    try (OIterator<? extends HierarchyTypeSnapshot> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    throw new ProgrammingErrorException("Unable to find Hierarchy Type Snapshot definition for the type [" + type.getCode() + "]");
  }

  public List<ExecutableJob> getJobs()
  {
    LinkedList<ExecutableJob> jobs = new LinkedList<ExecutableJob>();

    PublishLabeledPropertyGraphTypeVersionJobQuery pmlvj = new PublishLabeledPropertyGraphTypeVersionJobQuery(new QueryFactory());
    pmlvj.WHERE(pmlvj.getVersion().EQ(this));

    try (OIterator<? extends PublishLabeledPropertyGraphTypeVersionJob> it = pmlvj.getIterator())
    {
      jobs.addAll(it.getAll());
    }

    return jobs;
  }

  public JobHistory createPublishJob()
  {
    QueryFactory factory = new QueryFactory();

    PublishLabeledPropertyGraphTypeVersionJobQuery query = new PublishLabeledPropertyGraphTypeVersionJobQuery(factory);
    query.WHERE(query.getVersion().EQ(this));

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
    job.setVersion(this);
    job.setGraphType(this.getGraphType());
    job.apply();

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.PUBLISH_JOB_CHANGE, null));

    return job.start();
  }

  @Transaction
  @Authenticate
  public String publish()
  {
    return this.publishNoAuth();
  }

  @Transaction
  public String publishNoAuth()
  {
    LabeledPropertyGraphType type = this.getGraphType();
    StrategyPublisher publisher = type.toStrategyConfiguration().getPublisher();

    publisher.publish(this);

    return null;
  }

  public void truncate()
  {
    this.getTypes().forEach(type -> {
      type.truncate();
    });

  }

  public JsonObject toJSON()
  {
    return this.toJSON(false);
  }

  public JsonObject toJSON(boolean includeTableDefinitions)
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    LabeledPropertyGraphType graphType = this.getGraphType();

    JsonObject object = new JsonObject();

    if (this.isAppliedToDB())
    {
      object.addProperty(LabeledPropertyGraphTypeVersion.OID, this.getOid());
    }

    object.addProperty(LabeledPropertyGraphType.DISPLAYLABEL, graphType.getDisplayLabel().getValue());
    object.addProperty(LabeledPropertyGraphTypeVersion.GRAPHTYPE, graphType.getOid());
    object.addProperty(LabeledPropertyGraphTypeVersion.ENTRY, this.getEntryOid());
    object.addProperty(LabeledPropertyGraphTypeVersion.FORDATE, format.format(this.getForDate()));
    object.addProperty(LabeledPropertyGraphTypeVersion.CREATEDATE, format.format(this.getCreateDate()));
    object.addProperty(LabeledPropertyGraphTypeVersion.VERSIONNUMBER, this.getVersionNumber());
    object.add(LabeledPropertyGraphTypeVersion.PERIOD, graphType.formatVersionLabel(this));

    Progress progress = ProgressService.get(this.getOid());

    if (progress != null)
    {
      object.add("refreshProgress", progress.toJson());
    }

    if (this.getPublishDate() != null)
    {
      object.addProperty(LabeledPropertyGraphTypeVersion.PUBLISHDATE, format.format(this.getPublishDate()));
    }

    if (includeTableDefinitions)
    {
      JsonArray types = new JsonArray();

      List<GeoObjectTypeSnapshot> vertices = this.getTypes();
      vertices.stream().sorted((a, b) -> b.getIsAbstract().compareTo(a.getIsAbstract())).filter(type -> !type.isRoot()).forEach(type -> {
        types.add(type.toJSON());
      });

      object.add("types", types);

      JsonArray hierarchies = new JsonArray();
      
      GeoObjectTypeSnapshot root = this.getRootType();

      this.getHierarchies().forEach(hierarchy -> {

        hierarchies.add(hierarchy.toJSON(root));
      });

      object.add("hierarchies", hierarchies);
    }

    return object;
  }

  @Transaction
  public static LabeledPropertyGraphTypeVersion create(LabeledPropertyGraphTypeEntry listEntry, boolean working, int versionNumber)
  {
    LabeledPropertyGraphType listType = listEntry.getGraphType();

    LabeledPropertyGraphTypeVersion version = new LabeledPropertyGraphTypeVersion();
    version.setEntry(listEntry);
    version.setGraphType(listType);
    version.setForDate(listEntry.getForDate());
    version.setVersionNumber(versionNumber);
    version.apply();

    GeoObjectTypeSnapshot root = GeoObjectTypeSnapshot.createRoot(version);

    ServerHierarchyType hierarchy = listType.getHierarchyType();

    HierarchyTypeSnapshot.create(version, hierarchy, root);

    Stack<StackItem> stack = new Stack<StackItem>();

    hierarchy.getDirectRootNodes().forEach(type -> stack.push(new StackItem(type, root)));

    while (!stack.isEmpty())
    {
      StackItem item = stack.pop();
      ServerGeoObjectType type = item.type;

      // if (type.getIsPrivate() && (
      // version.getListVisibility().equals(LabeledPropertyGraphType.PUBLIC) ||
      // version.getGeospatialVisibility().equals(LabeledPropertyGraphType.PUBLIC)
      // ))
      // {
      // throw new UnsupportedOperationException("A list version cannot be
      // public if the Geo-Object Type is private");
      // }

      GeoObjectTypeSnapshot parent = GeoObjectTypeSnapshot.create(version, type, root);

      if (type.getIsAbstract())
      {
        type.getSubtypes().forEach(subtype -> {
          GeoObjectTypeSnapshot.create(version, subtype, parent);
        });
      }

      if (item.parent != null)
      {
        item.parent.addChildSnapshot(parent).apply();
      }

      hierarchy.getChildren(type).forEach(child -> {
        stack.push(new StackItem(child, parent));
      });

    }

    return version;
  }

  @Transaction
  public static LabeledPropertyGraphTypeVersion create(LabeledPropertyGraphTypeEntry entry, JsonObject json)
  {
    LabeledPropertyGraphType graphType = entry.getGraphType();

    LabeledPropertyGraphTypeVersion version = new LabeledPropertyGraphTypeVersion();
    version.setEntry(entry);
    version.setGraphType(graphType);
    version.setForDate(entry.getForDate());
    version.setVersionNumber(json.get(VERSIONNUMBER).getAsInt());
    version.apply();

    GeoObjectTypeSnapshot root = GeoObjectTypeSnapshot.createRoot(version);

    JsonArray types = json.get("types").getAsJsonArray();

    for (JsonElement element : types)
    {
      GeoObjectTypeSnapshot.create(version, element.getAsJsonObject());
    }

    JsonArray hierarchies = json.get("hierarchies").getAsJsonArray();

    for (JsonElement element : hierarchies)
    {
      HierarchyTypeSnapshot.create(version, element.getAsJsonObject(), root);
    }

    return version;
  }

  public static void assignDefaultRolePermissions(ComponentIF component)
  {
    RoleDAO adminRole = RoleDAO.findRole(RoleConstants.ADMIN).getBusinessDAO();
    adminRole.grantPermission(Operation.CREATE, component.getOid());
    adminRole.grantPermission(Operation.DELETE, component.getOid());
    adminRole.grantPermission(Operation.WRITE, component.getOid());
    adminRole.grantPermission(Operation.WRITE_ALL, component.getOid());

    RoleDAO maintainer = RoleDAO.findRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE).getBusinessDAO();
    maintainer.grantPermission(Operation.CREATE, component.getOid());
    maintainer.grantPermission(Operation.DELETE, component.getOid());
    maintainer.grantPermission(Operation.WRITE, component.getOid());
    maintainer.grantPermission(Operation.WRITE_ALL, component.getOid());

    RoleDAO consumer = RoleDAO.findRole(RegistryConstants.API_CONSUMER_ROLE).getBusinessDAO();
    consumer.grantPermission(Operation.READ, component.getOid());
    consumer.grantPermission(Operation.READ_ALL, component.getOid());

    RoleDAO contributor = RoleDAO.findRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE).getBusinessDAO();
    contributor.grantPermission(Operation.READ, component.getOid());
    contributor.grantPermission(Operation.READ_ALL, component.getOid());
  }

  public static List<? extends LabeledPropertyGraphTypeVersion> getAll()
  {
    LabeledPropertyGraphTypeVersionQuery query = new LabeledPropertyGraphTypeVersionQuery(new QueryFactory());

    try (OIterator<? extends LabeledPropertyGraphTypeVersion> it = query.getIterator())
    {
      return it.getAll();
    }
  }

}
