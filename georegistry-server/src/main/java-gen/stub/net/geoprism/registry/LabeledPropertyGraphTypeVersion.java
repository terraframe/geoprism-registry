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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;

import com.google.gson.JsonObject;
import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.business.rbac.Authenticate;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdAttributeConcreteInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.graph.MdEdgeInfo;
import com.runwaysdk.constants.graph.MdVertexInfo;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.metadata.MdAttributeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.constants.MdAttributePointInfo;
import com.runwaysdk.gis.dataaccess.MdAttributeGeometryDAOIF;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.metadata.MdEdge;
import com.runwaysdk.system.metadata.MdGraphClassQuery;
import com.runwaysdk.system.metadata.MdVertex;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutableJob;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryQuery;

import net.geoprism.rbac.RoleConstants;
import net.geoprism.registry.action.GraphHasEdge;
import net.geoprism.registry.action.GraphHasVertex;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.etl.DuplicateJobException;
import net.geoprism.registry.etl.PublishLabeledPropertyGraphTypeVersionJob;
import net.geoprism.registry.etl.PublishLabeledPropertyGraphTypeVersionJobQuery;
import net.geoprism.registry.graph.GeoVertex;
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
  private static final long  serialVersionUID = -351397872;

  public static final String PREFIX           = "gt_";

  public static final String TYPE_CODE        = "typeCode";

  public static final String ATTRIBUTES       = "attributes";

  public static final String HIERARCHIES      = "hierarchies";

  public static final String PERIOD           = "period";

  public LabeledPropertyGraphTypeVersion()
  {
    super();
  }

  private String getTableName(ServerGeoObjectType type)
  {

    MdVertexDAOIF mdVertex = type.getMdVertex();

    String className = mdVertex.getDBClassName();

    return this.getTableName(className);
  }

  public String getTableName(String className)
  {
    int count = 0;

    String name = PREFIX + count + className;

    if (name.length() > 25)
    {
      name = name.substring(0, 25);
    }

    while (isTableNameInUse(name))
    {
      count++;

      name = PREFIX + count + className;

      if (name.length() > 25)
      {
        name = name.substring(0, 25);
      }
    }

    return name;
  }

  private boolean isTableNameInUse(String name)
  {
    MdGraphClassQuery query = new MdGraphClassQuery(new QueryFactory());
    query.WHERE(query.getDbClassName().EQ(name));

    return query.getCount() > 0;
  }

  private String getEdgeName(ServerHierarchyType type)
  {

    MdEdgeDAOIF mdEdge = type.getMdEdgeDAO();

    String className = mdEdge.getDBClassName();

    return this.getTableName(className);
  }

  private MdVertex createTable(ServerGeoObjectType type, MdVertexDAOIF rootMdVertex)
  {
    LabeledPropertyGraphType masterlist = this.getGraphType();

    String viewName = this.getTableName(type);

    // Create the MdTable
    MdVertexDAO mdTableDAO = MdVertexDAO.newInstance();
    mdTableDAO.setValue(MdVertexInfo.NAME, viewName);
    mdTableDAO.setValue(MdVertexInfo.PACKAGE, RegistryConstants.TABLE_PACKAGE);
    mdTableDAO.setStructValue(MdVertexInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, masterlist.getDisplayLabel().getValue());
    mdTableDAO.setValue(MdVertexInfo.DB_CLASS_NAME, viewName);
    mdTableDAO.setValue(MdVertexInfo.GENERATE_SOURCE, MdAttributeBooleanInfo.FALSE);
    mdTableDAO.setValue(MdVertexInfo.ENABLE_CHANGE_OVER_TIME, MdAttributeBooleanInfo.FALSE);
    mdTableDAO.setValue(MdVertexInfo.SUPER_MD_VERTEX, rootMdVertex.getOid());
    mdTableDAO.apply();

    this.createGeometryAttribute(type, mdTableDAO);

    List<String> existingAttributes = mdTableDAO.getAllDefinedMdAttributes().stream().map(attribute -> attribute.definesAttribute()).collect(Collectors.toList());

    MdVertexDAOIF sourceMdVertex = type.getMdVertex();

    List<? extends MdAttributeDAOIF> attributes = sourceMdVertex.getAllDefinedMdAttributes();

    attributes.forEach(attribute -> {
      String attributeName = attribute.definesAttribute();
      if (!attribute.isSystem() && ! ( attribute instanceof MdAttributeGeometryDAOIF ) && !existingAttributes.contains(attributeName))
      {
        MdAttributeDAO mdAttribute = (MdAttributeDAO) attribute.copy();
        mdAttribute.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
        mdAttribute.apply();
      }
    });

    return (MdVertex) BusinessFacade.get(mdTableDAO);
  }

  private void createGeometryAttribute(ServerGeoObjectType type, MdVertexDAO mdTableDAO)
  {
    MdVertexDAOIF mdGeoVertex = MdVertexDAO.getMdVertexDAO(GeoVertex.CLASS);
    Map<String, ? extends MdAttributeDAOIF> map = mdGeoVertex.getAllDefinedMdAttributeMap();

    // Create the geometry attribute
    if (type.getGeometryType().equals(GeometryType.LINE))
    {
      MdAttributeDAO mdAttribute = (MdAttributeDAO) map.get(GeoVertex.GEOLINE.toLowerCase()).copy();
      mdAttribute.setValue(MdAttributePointInfo.NAME, DefaultAttribute.GEOMETRY.getName());
      mdAttribute.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
      mdAttribute.apply();
    }
    else if (type.getGeometryType().equals(GeometryType.MULTILINE))
    {
      MdAttributeDAO mdAttribute = (MdAttributeDAO) map.get(GeoVertex.GEOMULTILINE.toLowerCase()).copy();
      mdAttribute.setValue(MdAttributePointInfo.NAME, DefaultAttribute.GEOMETRY.getName());
      mdAttribute.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
      mdAttribute.apply();
    }
    else if (type.getGeometryType().equals(GeometryType.POINT))
    {
      MdAttributeDAO mdAttribute = (MdAttributeDAO) map.get(GeoVertex.GEOPOINT.toLowerCase()).copy();
      mdAttribute.setValue(MdAttributePointInfo.NAME, DefaultAttribute.GEOMETRY.getName());
      mdAttribute.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
      mdAttribute.apply();
    }
    else if (type.getGeometryType().equals(GeometryType.MULTIPOINT))
    {
      MdAttributeDAO mdAttribute = (MdAttributeDAO) map.get(GeoVertex.GEOMULTIPOINT.toLowerCase()).copy();
      mdAttribute.setValue(MdAttributePointInfo.NAME, DefaultAttribute.GEOMETRY.getName());
      mdAttribute.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
      mdAttribute.apply();
    }
    else if (type.getGeometryType().equals(GeometryType.POLYGON))
    {
      MdAttributeDAO mdAttribute = (MdAttributeDAO) map.get(GeoVertex.GEOPOLYGON.toLowerCase()).copy();
      mdAttribute.setValue(MdAttributePointInfo.NAME, DefaultAttribute.GEOMETRY.getName());
      mdAttribute.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
      mdAttribute.apply();
    }
    else if (type.getGeometryType().equals(GeometryType.MULTIPOLYGON))
    {
      MdAttributeDAO mdAttribute = (MdAttributeDAO) map.get(GeoVertex.GEOMULTIPOLYGON.toLowerCase()).copy();
      mdAttribute.setValue(MdAttributePointInfo.NAME, DefaultAttribute.GEOMETRY.getName());
      mdAttribute.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
      mdAttribute.apply();
    }
    else if (type.getGeometryType().equals(GeometryType.MIXED))
    {
      MdAttributeDAO mdAttribute = (MdAttributeDAO) map.get(GeoVertex.SHAPE.toLowerCase()).copy();
      mdAttribute.setValue(MdAttributePointInfo.NAME, DefaultAttribute.GEOMETRY.getName());
      mdAttribute.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
      mdAttribute.apply();
    }
  }

  private MdEdge createEdge(ServerHierarchyType type, MdVertexDAOIF mdBusGeoEntity)
  {
    String viewName = this.getEdgeName(type);

    MdEdgeDAO mdEdgeDAO = MdEdgeDAO.newInstance();
    mdEdgeDAO.setValue(MdEdgeInfo.PACKAGE, RegistryConstants.UNIVERSAL_GRAPH_PACKAGE);
    mdEdgeDAO.setValue(MdEdgeInfo.NAME, viewName);
    mdEdgeDAO.setValue(MdEdgeInfo.DB_CLASS_NAME, viewName);
    mdEdgeDAO.setValue(MdEdgeInfo.PARENT_MD_VERTEX, mdBusGeoEntity.getOid());
    mdEdgeDAO.setValue(MdEdgeInfo.CHILD_MD_VERTEX, mdBusGeoEntity.getOid());
    LocalizedValueConverter.populate(mdEdgeDAO, MdEdgeInfo.DISPLAY_LABEL, type.getLabel());
    LocalizedValueConverter.populate(mdEdgeDAO, MdEdgeInfo.DESCRIPTION, LocalizedValueConverter.convertNoAutoCoalesce(type.getDescription()));
    mdEdgeDAO.setValue(MdEdgeInfo.ENABLE_CHANGE_OVER_TIME, MdAttributeBooleanInfo.FALSE);
    mdEdgeDAO.apply();

    return (MdEdge) BusinessFacade.get(mdEdgeDAO);
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

    this.getEdges().forEach(e -> LabeledPropertyGraphEdge.get(e.getOid()).delete());
    this.getVertices().forEach(v -> LabeledPropertyGraphVertex.get(v.getOid()).delete());

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

    MdVertexDAOIF mdVertex = (MdVertexDAOIF) BusinessFacade.getEntityDAO(this.getMdVertexForType(type).getGraphMdVertex());
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(RegistryConstants.UUID);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" WHERE " + mdAttribute.getColumnName() + " = :uid");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());
    query.setParameter("uid", uid);

    return query.getSingleResult();
  }

  public List<LabeledPropertyGraphVertex> getVertices()
  {
    try (OIterator<? extends Business> it = this.getChildren(GraphHasVertex.CLASS))
    {
      return it.getAll().stream().map(b -> (LabeledPropertyGraphVertex) b).collect(Collectors.toList());
    }
  }

  public List<LabeledPropertyGraphEdge> getEdges()
  {
    try (OIterator<? extends Business> it = this.getChildren(GraphHasEdge.CLASS))
    {
      return it.getAll().stream().map(b -> (LabeledPropertyGraphEdge) b).collect(Collectors.toList());
    }
  }

  public LabeledPropertyGraphVertex getMdVertexForType(ServerGeoObjectType type)
  {
    return this.getVertices().stream().filter(mdVertex -> mdVertex.getTypeCode().equals(type.getCode())).findFirst().orElseThrow(() -> {
      throw new ProgrammingErrorException("Unable to find Labeled Property Graph Vertex definition for the type [" + type.getCode() + "]");
    });
  }

  public ServerGeoObjectType getTypeForGraphVertex(MdVertexDAOIF mdClass)
  {
    LabeledPropertyGraphVertex lpgv = this.getVertices().stream().filter(mdVertex -> mdVertex.getGraphMdVertexOid().equals(mdClass.getOid())).findFirst().orElseThrow(() -> {
      throw new ProgrammingErrorException("Unable to find Labeled Property Graph Vertex definition for the type [" + mdClass.definesType() + "]");
    });

    return ServerGeoObjectType.get(lpgv.getTypeCode());
  }

  public LabeledPropertyGraphEdge getMdEdgeForType(ServerHierarchyType type)
  {
    return this.getEdges().stream().filter(mdEdge -> mdEdge.getTypeCode().equals(type.getCode())).findFirst().orElseThrow(() -> {
      throw new ProgrammingErrorException("Unable to find Labeled Property Graph Edge definition for the type [" + type.getCode() + "]");
    });
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
    LabeledPropertyGraphType graphType = this.getGraphType();

    graphType.getGeoObjectTypes().forEach(type -> {
      MdVertex mdVertex = this.getMdVertexForType(type).getGraphMdVertex();

      GraphDBService service = GraphDBService.getInstance();
      service.command(service.getGraphDBRequest(), "DELETE VERTEX FROM " + mdVertex.getDbClassName(), new HashMap<>());
    });

  }

  public JsonObject toJSON()
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    LabeledPropertyGraphType masterlist = this.getGraphType();

    JsonObject object = new JsonObject();

    if (this.isAppliedToDB())
    {
      object.addProperty(LabeledPropertyGraphTypeVersion.OID, this.getOid());
    }

    object.addProperty(LabeledPropertyGraphType.DISPLAYLABEL, masterlist.getDisplayLabel().getValue());
    object.addProperty(LabeledPropertyGraphTypeVersion.GRAPHTYPE, masterlist.getOid());
    object.addProperty(LabeledPropertyGraphTypeVersion.FORDATE, format.format(this.getForDate()));
    object.addProperty(LabeledPropertyGraphTypeVersion.CREATEDATE, format.format(this.getCreateDate()));
    object.addProperty(LabeledPropertyGraphTypeVersion.VERSIONNUMBER, this.getVersionNumber());
    // object.addProperty(LabeledPropertyGraphTypeVersion.WORKING,
    // this.getWorking());
    object.add(LabeledPropertyGraphTypeVersion.PERIOD, masterlist.formatVersionLabel(this));

    Progress progress = ProgressService.get(this.getOid());

    if (progress != null)
    {
      object.add("refreshProgress", progress.toJson());
    }

    if (this.getPublishDate() != null)
    {
      object.addProperty(LabeledPropertyGraphTypeVersion.PUBLISHDATE, format.format(this.getPublishDate()));
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

    String tableName = version.getTableName(listType.getCode());

    MdVertexDAO rootMdVertexDAO = MdVertexDAO.newInstance();
    rootMdVertexDAO.setValue(MdVertexInfo.NAME, tableName);
    rootMdVertexDAO.setValue(MdVertexInfo.PACKAGE, RegistryConstants.TABLE_PACKAGE);
    rootMdVertexDAO.setStructValue(MdVertexInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, "Root Type");
    rootMdVertexDAO.setValue(MdVertexInfo.DB_CLASS_NAME, tableName);
    rootMdVertexDAO.setValue(MdVertexInfo.GENERATE_SOURCE, MdAttributeBooleanInfo.FALSE);
    rootMdVertexDAO.setValue(MdVertexInfo.ENABLE_CHANGE_OVER_TIME, MdAttributeBooleanInfo.FALSE);
    rootMdVertexDAO.setValue(MdVertexInfo.ABSTRACT, MdAttributeBooleanInfo.TRUE);
    rootMdVertexDAO.apply();

    version.addChild(rootMdVertexDAO.getOid(), (ServerGeoObjectType) null);

    LabeledPropertyGraphTypeVersion.assignDefaultRolePermissions(rootMdVertexDAO);

    List<ServerGeoObjectType> types = listType.getGeoObjectTypes();

    for (ServerGeoObjectType type : types)
    {

      // if (type.getIsPrivate() && (
      // version.getListVisibility().equals(LabeledPropertyGraphType.PUBLIC) ||
      // version.getGeospatialVisibility().equals(LabeledPropertyGraphType.PUBLIC)
      // ))
      // {
      // throw new UnsupportedOperationException("A list version cannot be
      // public if the Geo-Object Type is private");
      // }

      MdVertex mdVertex = version.createTable(type, rootMdVertexDAO);

      version.addChild(mdVertex.getOid(), type);

      LabeledPropertyGraphTypeVersion.assignDefaultRolePermissions(mdVertex);
    }

    List<ServerHierarchyType> hierarchies = listType.getHierarchyTypes();

    for (ServerHierarchyType hierarchy : hierarchies)
    {

      // if (type.getIsPrivate() && (
      // version.getListVisibility().equals(LabeledPropertyGraphType.PUBLIC) ||
      // version.getGeospatialVisibility().equals(LabeledPropertyGraphType.PUBLIC)
      // ))
      // {
      // throw new UnsupportedOperationException("A list version cannot be
      // public if the Geo-Object Type is private");
      // }

      MdEdge mdEdge = version.createEdge(hierarchy, rootMdVertexDAO);

      version.addChild(mdEdge, hierarchy);

      LabeledPropertyGraphTypeVersion.assignDefaultRolePermissions(mdEdge);
    }

    return version;
  }

  private GraphHasVertex addChild(String graphVertexOid, ServerGeoObjectType type)
  {
    LabeledPropertyGraphVertex vertex = new LabeledPropertyGraphVertex();
    vertex.setGraphMdVertexId(graphVertexOid);

    if (type != null)
    {
      vertex.setTypeCode(type.getCode());
    }

    vertex.apply();

    GraphHasVertex relationship = this.addVertices(vertex);
    relationship.apply();

    return relationship;
  }

  private GraphHasEdge addChild(MdEdge graphEdge, ServerHierarchyType type)
  {
    LabeledPropertyGraphEdge edge = new LabeledPropertyGraphEdge();
    edge.setGraphMdEdgeId(graphEdge.getOid());

    if (type != null)
    {
      edge.setTypeCode(type.getCode());
    }

    edge.apply();

    GraphHasEdge relationship = this.addEdges(edge);
    relationship.apply();

    return relationship;
  }

  private static void assignDefaultRolePermissions(ComponentIF component)
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
