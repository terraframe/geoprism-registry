package net.geoprism.registry.service.business;

import java.util.List;
import java.util.Map;
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
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
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

import net.geoprism.graph.BusinessEdgeTypeSnapshot;
import net.geoprism.graph.BusinessTypeSnapshot;
import net.geoprism.graph.BusinessTypeSnapshotQuery;
import net.geoprism.graph.DirectedAcyclicGraphTypeSnapshot;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.GraphTypeReference;
import net.geoprism.graph.GraphTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.graph.MetadataSnapshot;
import net.geoprism.graph.ObjectTypeSnapshot;
import net.geoprism.graph.UndirectedGraphTypeSnapshot;
import net.geoprism.rbac.RoleConstants;
import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.Commit;
import net.geoprism.registry.CommitHasSnapshotQuery;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.conversion.RegistryLocalizedValueConverter;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.model.GeoObjectMetadata;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.SnapshotContainer;
import net.geoprism.registry.model.graph.EdgeVertexType;

@Service
public class SnapshotBusinessService
{
  @Autowired
  private GeoObjectTypeSnapshotBusinessServiceIF oSnapshotService;

  @Autowired
  private HierarchyTypeSnapshotBusinessServiceIF hSnapshotService;

  @Autowired
  private BusinessTypeSnapshotBusinessServiceIF  bTypeSnapshotService;

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF      bEdgeService;

  @Autowired
  private ClassificationTypeBusinessServiceIF    cTypeService;

  @Autowired
  private ClassificationBusinessServiceIF        cService;

  protected GraphTypeSnapshot createGraphTypeSnapshot(SnapshotContainer<?> version, GraphTypeReference gtr, GeoObjectTypeSnapshot root)
  {
    MdEdge mdEdge = null;

    GraphType graphType = GraphType.resolve(gtr);

    if (version.createTablesWithSnapshot())
    {
      String viewName = getEdgeName(graphType);

      MdEdgeDAO mdEdgeDAO = MdEdgeDAO.newInstance();
      mdEdgeDAO.setValue(MdEdgeInfo.PACKAGE, RegistryConstants.UNIVERSAL_GRAPH_PACKAGE);
      mdEdgeDAO.setValue(MdEdgeInfo.NAME, viewName);
      mdEdgeDAO.setValue(MdEdgeInfo.DB_CLASS_NAME, viewName);
      mdEdgeDAO.setValue(MdEdgeInfo.PARENT_MD_VERTEX, root.getGraphMdVertexOid());
      mdEdgeDAO.setValue(MdEdgeInfo.CHILD_MD_VERTEX, root.getGraphMdVertexOid());
      RegistryLocalizedValueConverter.populate(mdEdgeDAO, MdEdgeInfo.DISPLAY_LABEL, graphType.getLabel());
      RegistryLocalizedValueConverter.populate(mdEdgeDAO, MdEdgeInfo.DESCRIPTION, graphType.getDescriptionLV());
      mdEdgeDAO.setValue(MdEdgeInfo.ENABLE_CHANGE_OVER_TIME, MdAttributeBooleanInfo.FALSE);
      mdEdgeDAO.apply();

      mdEdge = (MdEdge) BusinessFacade.get(mdEdgeDAO);

      this.assignPermissions(mdEdge);
    }

    MetadataSnapshot snapshot;

    if (gtr.typeCode.equals(GraphTypeSnapshot.DIRECTED_ACYCLIC_GRAPH_TYPE))
    {
      DirectedAcyclicGraphTypeSnapshot htsnapshot = new DirectedAcyclicGraphTypeSnapshot();
      htsnapshot.setGraphMdEdge(mdEdge);
      htsnapshot.setCode(gtr.code);
      LocalizedValueConverter.populate(htsnapshot.getDisplayLabel(), graphType.getLabel());
      LocalizedValueConverter.populate(htsnapshot.getDescription(), graphType.getDescriptionLV());
      htsnapshot.apply();

      snapshot = htsnapshot;
    }
    else if (gtr.typeCode.equals(GraphTypeSnapshot.UNDIRECTED_GRAPH_TYPE))
    {
      UndirectedGraphTypeSnapshot htsnapshot = new UndirectedGraphTypeSnapshot();
      htsnapshot.setGraphMdEdge(mdEdge);
      htsnapshot.setCode(gtr.code);
      LocalizedValueConverter.populate(htsnapshot.getDisplayLabel(), graphType.getLabel());
      LocalizedValueConverter.populate(htsnapshot.getDescription(), graphType.getDescriptionLV());
      htsnapshot.apply();

      snapshot = htsnapshot;
    }
    else if (gtr.typeCode.equals(GraphTypeSnapshot.HIERARCHY_TYPE))
    {
      HierarchyTypeSnapshot htsnapshot = new HierarchyTypeSnapshot();
      htsnapshot.setGraphMdEdge(mdEdge);
      htsnapshot.setCode(gtr.code);
      LocalizedValueConverter.populate(htsnapshot.getDisplayLabel(), graphType.getLabel());
      LocalizedValueConverter.populate(htsnapshot.getDescription(), graphType.getDescriptionLV());
      htsnapshot.apply();

      snapshot = htsnapshot;
    }
    else
    {
      throw new UnsupportedOperationException();
    }

    version.addSnapshot(snapshot).apply();

    return (GraphTypeSnapshot) snapshot;
  }

  protected BusinessEdgeTypeSnapshot create(SnapshotContainer<?> version, BusinessEdgeType edgeType, GeoObjectTypeSnapshot root)
  {
    EdgeVertexType parent = this.bEdgeService.getParent(edgeType);
    EdgeVertexType child = this.bEdgeService.getChild(edgeType);

    ObjectTypeSnapshot pSnapshot = parent.isGeoObjectType() ? root : this.getBusinessType(version, parent.getCode());
    ObjectTypeSnapshot cSnapshot = child.isGeoObjectType() ? root : this.getBusinessType(version, child.getCode());

    MdEdge mdEdge = null;

    if (version.createTablesWithSnapshot())
    {
      String viewName = getEdgeName(edgeType.getMdEdgeDAO());

      MdEdgeDAO mdEdgeDAO = MdEdgeDAO.newInstance();
      mdEdgeDAO.setValue(MdEdgeInfo.PACKAGE, RegistryConstants.UNIVERSAL_GRAPH_PACKAGE);
      mdEdgeDAO.setValue(MdEdgeInfo.NAME, viewName);
      mdEdgeDAO.setValue(MdEdgeInfo.DB_CLASS_NAME, viewName);
      mdEdgeDAO.setValue(MdEdgeInfo.PARENT_MD_VERTEX, pSnapshot.getGraphMdVertexOid());
      mdEdgeDAO.setValue(MdEdgeInfo.CHILD_MD_VERTEX, cSnapshot.getGraphMdVertexOid());
      RegistryLocalizedValueConverter.populate(mdEdgeDAO, MdEdgeInfo.DISPLAY_LABEL, edgeType.getLabel());
      mdEdgeDAO.setValue(MdEdgeInfo.ENABLE_CHANGE_OVER_TIME, MdAttributeBooleanInfo.FALSE);
      mdEdgeDAO.apply();

      mdEdge = (MdEdge) BusinessFacade.get(mdEdgeDAO);

      this.assignPermissions(mdEdge);
    }

    BusinessEdgeTypeSnapshot snapshot = new BusinessEdgeTypeSnapshot();
    snapshot.setGraphMdEdge(mdEdge);
    snapshot.setCode(edgeType.getCode());
    snapshot.setIsChildGeoObject(child.isGeoObjectType());
    snapshot.setIsParentGeoObject(parent.isGeoObjectType());
    snapshot.setParentType(pSnapshot);
    snapshot.setIsParentGeoObject(parent.isGeoObjectType());
    snapshot.setChildType(cSnapshot);
    LocalizedValueConverter.populate(snapshot.getDisplayLabel(), edgeType.getLabel());
    snapshot.apply();

    version.addSnapshot(snapshot).apply();

    return snapshot;
  }

  protected BusinessTypeSnapshot getBusinessType(SnapshotContainer<?> version, String code)
  {
    if (version instanceof LabeledPropertyGraphTypeVersion)
    {
      return this.bTypeSnapshotService.get((LabeledPropertyGraphTypeVersion) version, code);
    }

    QueryFactory factory = new QueryFactory();

    CommitHasSnapshotQuery vQuery = new CommitHasSnapshotQuery(factory);
    vQuery.WHERE(vQuery.getParent().EQ((Commit) version));

    BusinessTypeSnapshotQuery query = new BusinessTypeSnapshotQuery(factory);
    query.WHERE(query.EQ(vQuery.getChild()));
    query.AND(query.getCode().EQ(code));

    try (OIterator<? extends BusinessTypeSnapshot> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    return null;

  }

  protected String getEdgeName(GraphType type)
  {
    MdEdgeDAOIF mdEdge = type.getMdEdgeDAO();

    return getEdgeName(mdEdge);
  }

  protected String getEdgeName(MdEdgeDAOIF mdEdge)
  {
    String className = mdEdge.getDBClassName();

    return this.hSnapshotService.getTableName(className);
  }

  protected HierarchyTypeSnapshot create(SnapshotContainer<?> version, ServerHierarchyType type, GeoObjectTypeSnapshot root)
  {
    MdEdge mdEdge = null;

    if (version.createTablesWithSnapshot())
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

      mdEdge = (MdEdge) BusinessFacade.get(mdEdgeDAO);

      this.assignPermissions(mdEdge);
    }

    HierarchyTypeSnapshot snapshot = new HierarchyTypeSnapshot();
    snapshot.setGraphMdEdge(mdEdge);
    snapshot.setCode(type.getCode());
    RegistryLocalizedValueConverter.populate(snapshot.getDisplayLabel(), type.getLabel());
    RegistryLocalizedValueConverter.populate(snapshot.getDescription(), type.getDescription());
    snapshot.apply();

    version.addSnapshot(snapshot).apply();

    return snapshot;
  }

  @Transaction
  protected GeoObjectTypeSnapshot create(SnapshotContainer<?> version, ServerGeoObjectType type, GeoObjectTypeSnapshot parent)
  {
    MdVertex graphMdVertex = null;

    if (version.createTablesWithSnapshot())
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
        if (! ( attribute instanceof net.geoprism.registry.graph.AttributeGeometryType ) && ! ( attribute instanceof net.geoprism.registry.graph.AttributeTermType ) && !existingAttributes.contains(attributeName))
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
          // else if (attributeType.getType().equals(AttributeTermType.TYPE))
          // {
          // mdAttribute = new MdAttributeTerm();
          // MdAttributeTerm mdAttributeTerm = (MdAttributeTerm) mdAttribute;
          //
          // MdBusiness classifierMdBusiness =
          // MdBusiness.getMdBusiness(Classifier.CLASS);
          // mdAttributeTerm.setMdBusiness(classifierMdBusiness);
          // }
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

      graphMdVertex = (MdVertex) BusinessFacade.get(mdVertexDAO);

      assignPermissions(mdVertexDAO);
    }

    GeoObjectTypeSnapshot snapshot = new GeoObjectTypeSnapshot();
    snapshot.setGraphMdVertex(graphMdVertex);
    snapshot.setCode(type.getCode());
    snapshot.setOrgCode(type.getOrganizationCode());
    snapshot.setGeometryType(type.getGeometryType().name());
    snapshot.setIsAbstract(type.getIsAbstract());
    snapshot.setIsRoot(false);
    snapshot.setIsPrivate(type.getIsPrivate());
    snapshot.setParent(parent);
    RegistryLocalizedValueConverter.populate(snapshot.getDisplayLabel(), type.getLabel());
    RegistryLocalizedValueConverter.populate(snapshot.getDescription(), type.getDescription());
    snapshot.apply();

    version.addSnapshot(snapshot).apply();

    return snapshot;
  }

  @Transaction
  protected BusinessTypeSnapshot create(SnapshotContainer<?> version, BusinessType type)
  {
    MdVertex graphMdVertex = null;

    if (version.createTablesWithSnapshot())
    {
      String viewName = this.oSnapshotService.getTableName(type.getMdVertex().getDbClassName());

      // Create the MdTable
      MdVertexDAO mdVertexDAO = MdVertexDAO.newInstance();
      mdVertexDAO.setValue(MdVertexInfo.NAME, viewName);
      mdVertexDAO.setValue(MdVertexInfo.PACKAGE, RegistryConstants.TABLE_PACKAGE);
      RegistryLocalizedValueConverter.populate(mdVertexDAO, MdVertexInfo.DISPLAY_LABEL, type.getLabel());
      mdVertexDAO.setValue(MdVertexInfo.DB_CLASS_NAME, viewName);
      mdVertexDAO.setValue(MdVertexInfo.GENERATE_SOURCE, MdAttributeBooleanInfo.FALSE);
      mdVertexDAO.setValue(MdVertexInfo.ENABLE_CHANGE_OVER_TIME, MdAttributeBooleanInfo.FALSE);
      mdVertexDAO.apply();

      List<String> existingAttributes = mdVertexDAO.getAllDefinedMdAttributes().stream().map(attribute -> attribute.definesAttribute()).collect(Collectors.toList());

      Map<String, AttributeType> attributes = type.getAttributeMap();

      attributes.forEach((attributeName, attributeType) -> {
        
        if (! ( attributeType instanceof AttributeTermType ) && !existingAttributes.contains(attributeName))
        {
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

      graphMdVertex = (MdVertex) BusinessFacade.get(mdVertexDAO);

      assignPermissions(mdVertexDAO);
    }

    BusinessTypeSnapshot snapshot = new BusinessTypeSnapshot();
    snapshot.setGraphMdVertex(graphMdVertex);
    snapshot.setCode(type.getCode());
    snapshot.setOrgCode(type.getOrganization().getCode());
    RegistryLocalizedValueConverter.populate(snapshot.getDisplayLabel(), type.getLabel());
    snapshot.apply();

    version.addSnapshot(snapshot).apply();

    return snapshot;
  }

  public GeoObjectTypeSnapshot createRoot(SnapshotContainer<?> version)
  {
    return this.oSnapshotService.createRoot(version);
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
