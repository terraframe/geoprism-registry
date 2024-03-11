package net.geoprism.registry.service.business;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
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
import com.runwaysdk.constants.MdAttributeCharacterInfo;
import com.runwaysdk.constants.MdAttributeConcreteInfo;
import com.runwaysdk.constants.MdAttributeDoubleInfo;
import com.runwaysdk.constants.UserInfo;
import com.runwaysdk.constants.graph.MdEdgeInfo;
import com.runwaysdk.constants.graph.MdVertexInfo;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.metadata.MdAttributeBoolean;
import com.runwaysdk.system.metadata.MdAttributeCharacter;
import com.runwaysdk.system.metadata.MdAttributeClassification;
import com.runwaysdk.system.metadata.MdAttributeConcrete;
import com.runwaysdk.system.metadata.MdAttributeDateTime;
import com.runwaysdk.system.metadata.MdAttributeDouble;
import com.runwaysdk.system.metadata.MdAttributeLocalCharacterEmbedded;
import com.runwaysdk.system.metadata.MdAttributeLong;
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
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.model.GeoObjectMetadata;
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
  private ClassificationTypeBusinessServiceIF         cTypeService;
  
  @Autowired
  private ClassificationBusinessServiceIF         cService;
  
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
    RegistryLocalizedValueConverter.populate(mdEdgeDAO, MdEdgeInfo.DESCRIPTION, type.getDescription());
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

    GeoObjectType dto = type.toDTO();
    Map<String, net.geoprism.registry.graph.AttributeType> attributes = type.getAttributeMap();
    
    attributes.forEach((attributeName, attribute) -> {
      if (! ( attribute instanceof net.geoprism.registry.graph.AttributeGeometryType )
          && ! ( attribute instanceof net.geoprism.registry.graph.AttributeTermType )
          && !existingAttributes.contains(attributeName))
      {
        AttributeType attributeType = dto.getAttribute(attributeName).get();
        
        MdAttributeConcrete mdAttribute = null;

        if (attributeType.getType().equals(AttributeCharacterType.TYPE))
        {
          mdAttribute = new MdAttributeCharacter();
          MdAttributeCharacter mdAttributeCharacter = (MdAttributeCharacter) mdAttribute;
          mdAttributeCharacter.setDatabaseSize(MdAttributeCharacterInfo.MAX_CHARACTER_SIZE);
        }
        else if (attributeType.getType().equals(AttributeDateType.TYPE))
        {
          mdAttribute = new MdAttributeDateTime();
        }
        else if (attributeType.getType().equals(AttributeIntegerType.TYPE))
        {
          mdAttribute = new MdAttributeLong();
        }
        else if (attributeType.getType().equals(AttributeFloatType.TYPE))
        {
          AttributeFloatType attributeFloatType = (AttributeFloatType) attributeType;

          mdAttribute = new MdAttributeDouble();
          mdAttribute.setValue(MdAttributeDoubleInfo.LENGTH, Integer.toString(attributeFloatType.getPrecision()));
          mdAttribute.setValue(MdAttributeDoubleInfo.DECIMAL, Integer.toString(attributeFloatType.getScale()));
        }
//        else if (attributeType.getType().equals(AttributeTermType.TYPE))
//        {
//          mdAttribute = new MdAttributeTerm();
//          MdAttributeTerm mdAttributeTerm = (MdAttributeTerm) mdAttribute;
//
//          MdBusiness classifierMdBusiness = MdBusiness.getMdBusiness(Classifier.CLASS);
//          mdAttributeTerm.setMdBusiness(classifierMdBusiness);
//        }
        else if (attributeType.getType().equals(AttributeClassificationType.TYPE))
        {
          AttributeClassificationType attributeClassificationType = (AttributeClassificationType) attributeType;
          String classificationTypeCode = attributeClassificationType.getClassificationType();

          ClassificationType classificationType = this.cTypeService.getByCode(classificationTypeCode);

          mdAttribute = new MdAttributeClassification();
          MdAttributeClassification mdAttributeTerm = (MdAttributeClassification) mdAttribute;
          mdAttributeTerm.setReferenceMdClassification(classificationType.getMdClassificationObject());

          Term root = attributeClassificationType.getRootTerm();

          if (root != null)
          {
            Classification classification = this.cService.get(classificationType, root.getCode());

            if (classification == null)
            {
              net.geoprism.registry.DataNotFoundException ex = new net.geoprism.registry.DataNotFoundException();
              ex.setTypeLabel(classificationType.getDisplayLabel().getValue());
              ex.setDataIdentifier(root.getCode());
              ex.setAttributeLabel(GeoObjectMetadata.get().getAttributeDisplayLabel(DefaultAttribute.CODE.getName()));

              throw ex;
            }

            mdAttributeTerm.setValue(MdAttributeClassification.ROOT, classification.getOid());
          }
        }
        else if (attributeType.getType().equals(AttributeBooleanType.TYPE))
        {
          mdAttribute = new MdAttributeBoolean();
        }
        else if (attributeType.getType().equals(AttributeLocalType.TYPE))
        {
          mdAttribute = new MdAttributeLocalCharacterEmbedded();
        }
        else
        {
          throw new UnsupportedOperationException();
        }

        mdAttribute.setAttributeName(attributeType.getName());
        
        RegistryLocalizedValueConverter.populate(mdAttribute.getDisplayLabel(), attributeType.getLabel());
        RegistryLocalizedValueConverter.populate(mdAttribute.getDescription(), attributeType.getDescription());

        mdAttribute.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdVertexDAO.getOid());
        mdAttribute.apply();
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
