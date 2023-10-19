package net.geoprism.registry.service.business;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.business.rbac.UserDAO;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdAttributeConcreteInfo;
import com.runwaysdk.constants.MdAttributeTermInfo;
import com.runwaysdk.constants.UserInfo;
import com.runwaysdk.constants.graph.MdEdgeInfo;
import com.runwaysdk.constants.graph.MdVertexInfo;
import com.runwaysdk.dataaccess.BusinessDAO;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.RelationshipDAOIF;
import com.runwaysdk.dataaccess.metadata.MdAttributeDAO;
import com.runwaysdk.dataaccess.metadata.MdAttributeTermDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.dataaccess.MdAttributeGeometryDAOIF;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.metadata.MdEdge;
import com.runwaysdk.system.metadata.MdVertex;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutableJob;
import com.runwaysdk.system.scheduler.JobHistoryQuery;

import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeEntry;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.graph.PublishLabeledPropertyGraphTypeVersionJob;
import net.geoprism.graph.PublishLabeledPropertyGraphTypeVersionJobQuery;
import net.geoprism.rbac.RoleConstants;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.conversion.RegistryLocalizedValueConverter;
import net.geoprism.registry.etl.DuplicateJobException;
import net.geoprism.registry.lpg.StrategyConfiguration;
import net.geoprism.registry.lpg.TreeStrategyConfiguration;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.request.TreeStrategyPublisherService;
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
  private GeoObjectTypeSnapshotBusinessServiceIF oSnapshotService;

  @Autowired
  private HierarchyTypeSnapshotBusinessServiceIF hSnapshotService;

  @Autowired
  private HierarchyTypeBusinessServiceIF         hierarchyService;

  @Autowired
  private GeoObjectTypeBusinessServiceIF         typeService;

  @Autowired
  private TreeStrategyPublisherService           publisherService;

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
  public void publishNoAuth(LabeledPropertyGraphTypeVersion version)
  {
    LabeledPropertyGraphType type = version.getGraphType();
    StrategyConfiguration configuration = type.toStrategyConfiguration();

    this.publisherService.publish((TreeStrategyConfiguration) configuration, version);
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

    LabeledPropertyGraphType graphType = version.getGraphType();
    ServerHierarchyType hierarchy = ServerHierarchyType.get(graphType.getHierarchy());

    GeoObjectTypeSnapshot root = this.oSnapshotService.createRoot(version);

    this.create(version, hierarchy, root);

    Stack<StackItem> stack = new Stack<StackItem>();

    this.hierarchyService.getDirectRootNodes(hierarchy).forEach(type -> stack.push(new StackItem(type, root)));

    while (!stack.isEmpty())
    {
      StackItem item = stack.pop();
      ServerGeoObjectType type = item.type;

      GeoObjectTypeSnapshot parent = this.create(version, type, root);

      if (type.getIsAbstract())
      {
        this.typeService.getSubtypes(type).forEach(subtype -> {
          this.create(version, subtype, parent);
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

    return version;
  }

  private String getEdgeName(ServerHierarchyType type)
  {
    MdEdgeDAOIF mdEdge = type.getMdEdgeDAO();

    String className = mdEdge.getDBClassName();

    return this.hSnapshotService.getTableName(className);
  }

  public HierarchyTypeSnapshot create(LabeledPropertyGraphTypeVersion version, ServerHierarchyType type, GeoObjectTypeSnapshot root)
  {
    String viewName = getEdgeName(type);

    MdEdgeDAO mdEdgeDAO = MdEdgeDAO.newInstance();
    mdEdgeDAO.setValue(MdEdgeInfo.PACKAGE, RegistryConstants.UNIVERSAL_GRAPH_PACKAGE);
    mdEdgeDAO.setValue(MdEdgeInfo.NAME, viewName);
    mdEdgeDAO.setValue(MdEdgeInfo.DB_CLASS_NAME, viewName);
    mdEdgeDAO.setValue(MdEdgeInfo.PARENT_MD_VERTEX, root.getGraphMdVertexOid());
    mdEdgeDAO.setValue(MdEdgeInfo.CHILD_MD_VERTEX, root.getGraphMdVertexOid());
    RegistryLocalizedValueConverter.populate(mdEdgeDAO, MdEdgeInfo.DISPLAY_LABEL, type.getLabel());
    RegistryLocalizedValueConverter.populate(mdEdgeDAO, MdEdgeInfo.DESCRIPTION, RegistryLocalizedValueConverter.convertNoAutoCoalesce(type.getDescription()));
    mdEdgeDAO.setValue(MdEdgeInfo.ENABLE_CHANGE_OVER_TIME, MdAttributeBooleanInfo.FALSE);
    mdEdgeDAO.apply();

    MdEdge mdEdge = (MdEdge) BusinessFacade.get(mdEdgeDAO);

    this.assignPermissions(mdEdge);

    HierarchyTypeSnapshot snapshot = new HierarchyTypeSnapshot();
    snapshot.setVersion(version);
    snapshot.setGraphMdEdge(mdEdge);
    snapshot.setCode(type.getCode());
    RegistryLocalizedValueConverter.populate(snapshot.getDisplayLabel(), type.getLabel());
    RegistryLocalizedValueConverter.populate(snapshot.getDescription(), type.getDescription());
    snapshot.apply();

    return snapshot;
  }

  @Transaction
  public GeoObjectTypeSnapshot create(LabeledPropertyGraphTypeVersion version, ServerGeoObjectType type, GeoObjectTypeSnapshot parent)
  {
    String viewName = this.oSnapshotService.getTableName(type.getMdVertex().getDBClassName());

    // Create the MdTable
    MdVertexDAO mdVertexDAO = MdVertexDAO.newInstance();
    mdVertexDAO.setValue(MdVertexInfo.NAME, viewName);
    mdVertexDAO.setValue(MdVertexInfo.PACKAGE, RegistryConstants.TABLE_PACKAGE);
    mdVertexDAO.setValue(MdVertexInfo.ABSTRACT, type.getIsAbstract());
    RegistryLocalizedValueConverter.populate(mdVertexDAO, MdVertexInfo.DISPLAY_LABEL, type.getLabel());
    RegistryLocalizedValueConverter.populate(mdVertexDAO, MdVertexInfo.DESCRIPTION, type.getDescription());
    mdVertexDAO.setValue(MdVertexInfo.DB_CLASS_NAME, viewName);
    mdVertexDAO.setValue(MdVertexInfo.GENERATE_SOURCE, MdAttributeBooleanInfo.FALSE);
    mdVertexDAO.setValue(MdVertexInfo.ENABLE_CHANGE_OVER_TIME, MdAttributeBooleanInfo.FALSE);
    mdVertexDAO.setValue(MdVertexInfo.SUPER_MD_VERTEX, parent.getGraphMdVertexOid());
    mdVertexDAO.apply();

    if (!type.getIsAbstract())
    {
      this.oSnapshotService.createGeometryAttribute(type.getGeometryType(), mdVertexDAO);
    }

    List<String> existingAttributes = mdVertexDAO.getAllDefinedMdAttributes().stream().map(attribute -> attribute.definesAttribute()).collect(Collectors.toList());

    MdVertexDAOIF sourceMdVertex = type.getMdVertex();
    MdBusinessDAOIF sourceMdBusiness = type.getMdBusinessDAO();

    List<? extends MdAttributeDAOIF> attributes = sourceMdVertex.getAllDefinedMdAttributes();

    attributes.forEach(attribute -> {
      String attributeName = attribute.definesAttribute();
      if (!attribute.isSystem() && ! ( attribute instanceof MdAttributeGeometryDAOIF ) && !existingAttributes.contains(attributeName))
      {
        MdAttributeDAO mdAttribute = (MdAttributeDAO) attribute.copy();
        mdAttribute.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdVertexDAO.getOid());
        mdAttribute.apply();

        if (attribute instanceof MdAttributeTermDAO)
        {
          MdAttributeTermDAO targetAttribute = (MdAttributeTermDAO) mdAttribute;

          // Roots are defined on the MdBusiness of the ServerGeoObjectType not
          // the MdVertex
          List<RelationshipDAOIF> roots = ( (MdAttributeTermDAOIF) sourceMdBusiness.definesAttribute(attribute.definesAttribute()) ).getAllAttributeRoots();

          roots.forEach(relationship -> {
            BusinessDAO term = (BusinessDAO) relationship.getChild();
            Boolean selectable = Boolean.valueOf(relationship.getValue(MdAttributeTermInfo.SELECTABLE));
            targetAttribute.addAttributeRoot(term, selectable);
          });
        }
      }
    });
    MdVertex graphMdVertex = (MdVertex) BusinessFacade.get(mdVertexDAO);

    GeoObjectTypeSnapshot snapshot = new GeoObjectTypeSnapshot();
    snapshot.setVersion(version);
    snapshot.setGraphMdVertex(graphMdVertex);
    snapshot.setCode(type.getCode());
    snapshot.setGeometryType(type.getGeometryType().name());
    snapshot.setIsAbstract(type.getIsAbstract());
    snapshot.setIsRoot(false);
    snapshot.setIsPrivate(type.getIsPrivate());
    snapshot.setParent(parent);
    RegistryLocalizedValueConverter.populate(snapshot.getDisplayLabel(), type.getLabel());
    RegistryLocalizedValueConverter.populate(snapshot.getDescription(), type.getDescription());
    snapshot.apply();

    assignPermissions(mdVertexDAO);

    return snapshot;
  }

  public void assignPermissions(ComponentIF component)
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

    UserDAO publicRole = UserDAO.findUser(UserInfo.PUBLIC_USER_NAME).getBusinessDAO();
    publicRole.grantPermission(Operation.READ, component.getOid());
    publicRole.grantPermission(Operation.READ_ALL, component.getOid());
  }

}
