package net.geoprism.registry.service.business;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeGeometryType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.business.rbac.UserDAO;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.UserInfo;
import com.runwaysdk.constants.graph.MdEdgeInfo;
import com.runwaysdk.constants.graph.MdVertexInfo;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdEdge;
import com.runwaysdk.system.metadata.MdVertex;

import net.geoprism.graph.AttributeBooleanTypeSnapshot;
import net.geoprism.graph.AttributeCharacterTypeSnapshot;
import net.geoprism.graph.AttributeClassificationTypeSnapshot;
import net.geoprism.graph.AttributeDateTypeSnapshot;
import net.geoprism.graph.AttributeDoubleTypeSnapshot;
import net.geoprism.graph.AttributeGeometryTypeSnapshot;
import net.geoprism.graph.AttributeLocalTypeSnapshot;
import net.geoprism.graph.AttributeLongTypeSnapshot;
import net.geoprism.graph.AttributeTermTypeSnapshot;
import net.geoprism.graph.AttributeTypeSnapshot;
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
import net.geoprism.registry.DirectedAcyclicGraphType;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.UndirectedGraphType;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.conversion.RegistryLocalizedValueConverter;
import net.geoprism.registry.graph.GeoObjectTypeAlreadyInHierarchyException;
import net.geoprism.registry.model.EdgeDirection;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.SnapshotContainer;
import net.geoprism.registry.model.graph.EdgeVertexType;
import net.geoprism.registry.view.BusinessEdgeTypeView;
import net.geoprism.registry.view.BusinessGeoEdgeTypeView;

@Service
public class SnapshotBusinessService
{
  @Autowired
  private GeoObjectTypeSnapshotBusinessServiceIF    oSnapshotService;

  @Autowired
  private HierarchyTypeSnapshotBusinessServiceIF    hSnapshotService;

  @Autowired
  private BusinessTypeSnapshotBusinessServiceIF     bTypeSnapshotService;

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF         bEdgeService;

  @Autowired
  private ClassificationTypeBusinessServiceIF       cTypeService;

  @Autowired
  private ClassificationBusinessServiceIF           cService;

  @Autowired
  private DirectedAcyclicGraphTypeBusinessServiceIF dagTypeService;

  @Autowired
  private UndirectedGraphTypeBusinessServiceIF      undirectedTypeService;

  @Autowired
  private HierarchyTypeBusinessServiceIF            hTypeService;

  @Autowired
  private GeoObjectTypeBusinessServiceIF            gTypeService;

  @Autowired
  private BusinessTypeBusinessServiceIF             bTypeService;

  public GraphTypeSnapshot createSnapshot(SnapshotContainer<?> version, GraphTypeReference gtr, GeoObjectTypeSnapshot root)
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
      htsnapshot.setOrigin(graphType.getOrigin());
      htsnapshot.setSequence(graphType.getSequence());
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
      htsnapshot.setOrigin(graphType.getOrigin());
      htsnapshot.setSequence(graphType.getSequence());
      LocalizedValueConverter.populate(htsnapshot.getDisplayLabel(), graphType.getLabel());
      LocalizedValueConverter.populate(htsnapshot.getDescription(), graphType.getDescriptionLV());
      htsnapshot.apply();

      snapshot = htsnapshot;
    }
    else if (gtr.typeCode.equals(GraphTypeSnapshot.HIERARCHY_TYPE))
    {
      snapshot = this.createSnapshot(version, (ServerHierarchyType) graphType, root);
    }
    else
    {
      throw new UnsupportedOperationException();
    }

    version.addSnapshot(snapshot).apply();

    return (GraphTypeSnapshot) snapshot;
  }

  public BusinessEdgeTypeSnapshot createSnapshot(SnapshotContainer<?> version, BusinessEdgeType edgeType, GeoObjectTypeSnapshot root)
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
    snapshot.setOrgCode(edgeType.getOrganization().getCode());
    snapshot.setOrigin(edgeType.getOrigin());
    snapshot.setSequence(edgeType.getSequence());
    snapshot.setIsChildGeoObject(child.isGeoObjectType());
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

  public HierarchyTypeSnapshot createSnapshot(SnapshotContainer<?> version, ServerHierarchyType type, GeoObjectTypeSnapshot root)
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
    snapshot.setOrgCode(type.getOrganization().getCode());
    snapshot.setOrigin(type.getOrigin());
    snapshot.setSequence(type.getSequence());
    RegistryLocalizedValueConverter.populate(snapshot.getDisplayLabel(), type.getLabel());
    RegistryLocalizedValueConverter.populate(snapshot.getDescription(), type.getDescription());
    snapshot.apply();

    version.addSnapshot(snapshot).apply();

    // Create the hierarchy nodes
    this.hTypeService.getDirectRootNodes(type).forEach(parent -> {
      createHierarchyRelationship(version, type, parent, snapshot, root);
    });

    return snapshot;
  }

  protected void createHierarchyRelationship(SnapshotContainer<?> version, ServerHierarchyType type, ServerGeoObjectType child, HierarchyTypeSnapshot hierarchySnapshot, GeoObjectTypeSnapshot parent)
  {
    GeoObjectTypeSnapshot snapshot = this.oSnapshotService.get(version, child.getCode());

    // Snapshot might be null if the type wasn't included in the geo object
    // types to publish
    if (snapshot != null)
    {
      this.hSnapshotService.createHierarchyRelationship(hierarchySnapshot, parent, snapshot);

      this.hTypeService.getChildren(type, child).forEach(node -> {
        this.createHierarchyRelationship(version, type, node, hierarchySnapshot, snapshot);
      });
    }
  }

  @Transaction
  public GeoObjectTypeSnapshot createSnapshot(SnapshotContainer<?> version, ServerGeoObjectType type, GeoObjectTypeSnapshot root)
  {
    GeoObjectTypeSnapshot parent = type.getSuperType() != null ? this.oSnapshotService.get(version, type.getSuperType().getCode()) : root;

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

      final MdVertex mdVertex = (MdVertex) BusinessFacade.get(mdVertexDAO);

      List<String> existingAttributes = mdVertexDAO.getAllDefinedMdAttributes().stream().map(attribute -> attribute.definesAttribute()).collect(Collectors.toList());

      type.getAttributeMap().values().stream() //
          .map(a -> a.toDTO()) //
          .filter(a -> ! ( a instanceof AttributeGeometryType )) //
          .filter(a -> ! ( a instanceof AttributeTermType )) //
          .filter(a -> !existingAttributes.contains(a.getName())) //
          .forEach(attributeType -> {
            this.oSnapshotService.createMdAttributeFromAttributeType(mdVertex, attributeType);
          });

      graphMdVertex = mdVertex;

      assignPermissions(mdVertexDAO);
    }

    GeoObjectTypeSnapshot snapshot = new GeoObjectTypeSnapshot();
    snapshot.setGraphMdVertex(graphMdVertex);
    snapshot.setCode(type.getCode());
    snapshot.setOrgCode(type.getOrganizationCode());
    snapshot.setOrigin(type.getOrigin());
    snapshot.setSequence(type.getSequence());
    snapshot.setGeometryType(type.getGeometryType().name());
    snapshot.setIsAbstract(type.getIsAbstract());
    snapshot.setIsRoot(false);
    snapshot.setIsPrivate(type.getIsPrivate());
    RegistryLocalizedValueConverter.populate(snapshot.getDisplayLabel(), type.getLabel());
    RegistryLocalizedValueConverter.populate(snapshot.getDescription(), type.getDescription());
    snapshot.setParent(parent);
    snapshot.apply();

    version.addSnapshot(snapshot).apply();

    if (!type.getIsAbstract())
    {
      this.oSnapshotService.createAttributeTypeSnapshot(snapshot, type.getGeometryType());
    }

    type.getAttributeMap().values().stream() //
        .map(a -> a.toDTO()) //
        .filter(a -> ! ( a instanceof AttributeGeometryType )) //
        .filter(a -> ! ( a instanceof AttributeTermType )) //
        .forEach(attributeType -> {
          this.oSnapshotService.createAttributeTypeSnapshot(snapshot, attributeType);
        });

    return snapshot;
  }

  @Transaction
  public BusinessTypeSnapshot createSnapshot(SnapshotContainer<?> version, BusinessType type)
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

      final MdVertex mdVertex = (MdVertex) BusinessFacade.get(mdVertexDAO);

      List<String> existingAttributes = mdVertexDAO.getAllDefinedMdAttributes().stream().map(attribute -> attribute.definesAttribute()).collect(Collectors.toList());

      type.getAttributeMap().values().stream() //
          .filter(a -> ! ( a instanceof AttributeGeometryType )) //
          .filter(a -> ! ( a instanceof AttributeTermType )) //
          .filter(a -> !existingAttributes.contains(a.getName())) //
          .forEach(attributeType -> {
            this.bTypeSnapshotService.createMdAttributeFromAttributeType(mdVertex, attributeType);
          });

      graphMdVertex = mdVertex;

      assignPermissions(mdVertexDAO);
    }

    BusinessTypeSnapshot snapshot = new BusinessTypeSnapshot();
    snapshot.setGraphMdVertex(graphMdVertex);
    snapshot.setCode(type.getCode());
    snapshot.setOrgCode(type.getOrganization().getCode());
    snapshot.setOrigin(type.getOrigin());
    snapshot.setSequence(type.getSequence());
    RegistryLocalizedValueConverter.populate(snapshot.getDisplayLabel(), type.getLabel());
    snapshot.apply();

    version.addSnapshot(snapshot).apply();

    type.getAttributeMap().values().stream() //
        .filter(a -> ! ( a instanceof AttributeGeometryType )) //
        .filter(a -> ! ( a instanceof AttributeTermType )) //
        .forEach(attributeType -> {
          this.bTypeSnapshotService.createAttributeTypeSnapshot(snapshot, attributeType);
        });

    return snapshot;
  }

  public GraphType createType(GraphTypeSnapshot snapshot, GeoObjectTypeSnapshot root)
  {
    if (snapshot instanceof DirectedAcyclicGraphTypeSnapshot)
    {
      DirectedAcyclicGraphTypeSnapshot concrete = (DirectedAcyclicGraphTypeSnapshot) snapshot;

      DirectedAcyclicGraphType type = DirectedAcyclicGraphType.getByCode(concrete.getCode()).orElseGet(() -> {
        LocalizedValue label = LocalizedValueConverter.convertNoAutoCoalesce(concrete.getDisplayLabel());
        LocalizedValue description = LocalizedValueConverter.convertNoAutoCoalesce(concrete.getDescription());

        return this.dagTypeService.create(concrete.getCode(), label, description, concrete.getOrigin(), concrete.getSequence());
      });

      return type;
    }
    else if (snapshot instanceof UndirectedGraphTypeSnapshot)
    {
      UndirectedGraphTypeSnapshot concrete = (UndirectedGraphTypeSnapshot) snapshot;

      UndirectedGraphType type = UndirectedGraphType.getByCode(concrete.getCode()).orElseGet(() -> {
        LocalizedValue label = LocalizedValueConverter.convertNoAutoCoalesce(concrete.getDisplayLabel());
        LocalizedValue description = LocalizedValueConverter.convertNoAutoCoalesce(concrete.getDescription());

        return this.undirectedTypeService.create(concrete.getCode(), label, description, concrete.getOrigin(), concrete.getSequence());
      });

      return type;
    }
    else if (snapshot instanceof HierarchyTypeSnapshot)
    {
      return this.createType((HierarchyTypeSnapshot) snapshot, root);
    }
    else
    {
      throw new UnsupportedOperationException();
    }
  }

  public BusinessEdgeType createType(BusinessEdgeTypeSnapshot snapshot)
  {
    LocalizedValue label = LocalizedValueConverter.convertNoAutoCoalesce(snapshot.getDisplayLabel());
    LocalizedValue description = LocalizedValueConverter.convertNoAutoCoalesce(snapshot.getDescription());

    BusinessEdgeType type = this.bEdgeService.getByCode(snapshot.getCode()).map(t -> {
      if (t.getSequence() < snapshot.getSequence())
      {
        this.bEdgeService.update(t, label, description);
      }

      return t;
    }).orElseGet(() -> {
      if (snapshot.getIsParentGeoObject() || snapshot.getIsChildGeoObject())
      {
        String businssTypeCode = snapshot.getIsParentGeoObject() ? //
            snapshot.getChildType().getCode() : //
            snapshot.getParentType().getCode();

        EdgeDirection direction = snapshot.getIsParentGeoObject() ? EdgeDirection.PARENT : EdgeDirection.CHILD;

        BusinessGeoEdgeTypeView dto = new BusinessGeoEdgeTypeView();
        dto.setCode(snapshot.getCode());
        dto.setDescription(description);
        dto.setDirection(direction);
        dto.setLabel(label);
        dto.setOrganizationCode(snapshot.getOrgCode());
        dto.setOrigin(snapshot.getOrigin());
        dto.setSeq(snapshot.getSequence());
        dto.setTypeCode(businssTypeCode);

        return this.bEdgeService.createGeoEdge(dto);
      }
      else
      {
        BusinessEdgeTypeView dto = new BusinessEdgeTypeView();
        dto.setCode(snapshot.getCode());
        dto.setDescription(description);
        dto.setLabel(label);
        dto.setOrganizationCode(snapshot.getOrgCode());
        dto.setOrigin(snapshot.getOrigin());
        dto.setSeq(snapshot.getSequence());
        dto.setParentTypeCode(snapshot.getParentType().getCode());
        dto.setChildTypeCode(snapshot.getChildType().getCode());

        return this.bEdgeService.create(dto);
      }
    });

    return type;
  }

  public ServerHierarchyType createType(HierarchyTypeSnapshot snapshot, GeoObjectTypeSnapshot root)
  {
    LocalizedValue label = LocalizedValueConverter.convertNoAutoCoalesce(snapshot.getDisplayLabel());
    LocalizedValue description = LocalizedValueConverter.convertNoAutoCoalesce(snapshot.getDescription());

    HierarchyType dto = new HierarchyType(snapshot.getCode(), label, description, snapshot.getOrgCode());
    dto.setOrigin(snapshot.getOrigin());
    dto.setSequenceNumber(snapshot.getSequence());

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(dto.getCode(), false);

    if (hierarchyType == null)
    {
      hierarchyType = this.hTypeService.createHierarchyType(dto);
    }
    else if (hierarchyType.getSequence() < snapshot.getSequence())
    {
      this.hTypeService.update(hierarchyType, dto);
    }

    final ServerHierarchyType sHierarchyType = hierarchyType;

    this.hSnapshotService.getChildren(snapshot, root).forEach(childSnapshot -> {
      createHierarchyRelationship(sHierarchyType, ServerGeoObjectType.get(Universal.ROOT), snapshot, childSnapshot);
    });

    return hierarchyType;
  }

  protected void createHierarchyRelationship(ServerHierarchyType hierarchyType, ServerGeoObjectType parent, HierarchyTypeSnapshot hierarchy, GeoObjectTypeSnapshot childSnapshot)
  {
    ServerGeoObjectType child = ServerGeoObjectType.get(childSnapshot.getCode());

    try
    {
      this.hTypeService.addToHierarchy(hierarchyType, parent, child);
    }
    catch (GeoObjectTypeAlreadyInHierarchyException e)
    {
      // Ignore
    }

    this.hSnapshotService.getChildren(hierarchy, childSnapshot).forEach(node -> {
      this.createHierarchyRelationship(hierarchyType, child, hierarchy, node);
    });
  }

  @Transaction
  public ServerGeoObjectType createType(GeoObjectTypeSnapshot snapshot)
  {
    LocalizedValue label = LocalizedValueConverter.convertNoAutoCoalesce(snapshot.getDisplayLabel());
    LocalizedValue description = LocalizedValueConverter.convertNoAutoCoalesce(snapshot.getDescription());
    GeoObjectTypeSnapshot parentType = snapshot.getParent();

    GeoObjectType dto = new GeoObjectType(snapshot.getCode(), GeometryType.valueOf(snapshot.getGeometryType()), label, description, snapshot.getIsGeometryEditable(), snapshot.getOrgCode(), ServiceFactory.getAdapter());
    dto.setIsAbstract(snapshot.getIsAbstract());
    dto.setIsPrivate(snapshot.getIsPrivate());
    dto.setOrigin(snapshot.getOrigin());
    dto.setSequenceNumber(snapshot.getSequence());

    if (parentType != null && !parentType.getCode().equals(GeoObjectTypeSnapshot.ROOT))
    {
      dto.setSuperTypeCode(parentType.getCode());
    }

    ServerGeoObjectType type = ServerGeoObjectType.get(snapshot.getCode(), true);

    if (type == null)
    {
      type = this.gTypeService.create(dto);
    }
    else if (type.getSequence() < snapshot.getSequence())
    {
      type = this.gTypeService.updateGeoObjectType(type, dto);
    }

    Map<String, net.geoprism.registry.graph.AttributeType> attributeMap = type.getAttributeMap();

    List<AttributeTypeSnapshot> attributeTypeSnapshots = this.oSnapshotService.getAttributeTypes(snapshot);

    final ServerGeoObjectType geoObjectType = type;

    attributeTypeSnapshots.stream() //
        .filter(aSnapshot -> !aSnapshot.getIsDefault())//
        .filter(aSnapshot -> !attributeMap.containsKey(aSnapshot.getCode()))//
        .filter(aSnapshot -> ! ( aSnapshot instanceof AttributeGeometryTypeSnapshot ))//
        .filter(aSnapshot -> ! ( aSnapshot instanceof AttributeTermTypeSnapshot ))//
        .forEach(attribute -> {
          AttributeType attributeType = createType(attribute);

          this.gTypeService.createAttributeType(geoObjectType, attributeType);
        });

    return type;
  }

  @Transaction
  public BusinessType createType(BusinessTypeSnapshot snapshot)
  {
    JsonObject dto = snapshot.toJSON();
    dto.addProperty(BusinessType.ORGANIZATION, snapshot.getOrgCode());

    BusinessType existing = this.bTypeService.getByCode(snapshot.getCode());

    if (existing == null || existing.getSequence() < snapshot.getSequence())
    {
      if (existing != null)
      {
        dto.addProperty(BusinessType.OID, existing.getOid());
      }

      final BusinessType type = this.bTypeService.apply(dto);

      Map<String, AttributeType> attributeMap = type.getAttributeMap();

      List<AttributeTypeSnapshot> attributeTypeSnapshots = this.bTypeSnapshotService.getAttributeTypes(snapshot);

      attributeTypeSnapshots.stream() //
          .filter(aSnapshot -> !aSnapshot.getIsDefault())//
          .filter(aSnapshot -> !attributeMap.containsKey(aSnapshot.getCode()))//
          .filter(aSnapshot -> ! ( aSnapshot instanceof AttributeGeometryTypeSnapshot ))//
          .filter(aSnapshot -> ! ( aSnapshot instanceof AttributeTermTypeSnapshot ))//
          .forEach(attribute -> {
            AttributeType attributeType = createType(attribute);

            this.bTypeService.createAttributeType(type, attributeType);
          });

      return type;
    }

    return existing;
  }

  public AttributeType createType(AttributeTypeSnapshot attribute)
  {
    AttributeType attributeType = null;

    LocalizedValue attributeLabel = LocalizedValueConverter.convertNoAutoCoalesce(attribute.getLabel());
    LocalizedValue attributeDescription = LocalizedValueConverter.convertNoAutoCoalesce(attribute.getDescription());

    if (attribute instanceof AttributeCharacterTypeSnapshot)
    {
      attributeType = new AttributeCharacterType(attribute.getCode(), attributeLabel, attributeDescription, attribute.getIsDefault(), attribute.getIsRequired(), attribute.getIsUnique());
      attributeType.setIsChangeOverTime(attribute.getIsChangeOverTime());
    }
    else if (attribute instanceof AttributeDateTypeSnapshot)
    {
      attributeType = new AttributeDateType(attribute.getCode(), attributeLabel, attributeDescription, attribute.getIsDefault(), attribute.getIsRequired(), attribute.getIsUnique());
      attributeType.setIsChangeOverTime(attribute.getIsChangeOverTime());
    }
    else if (attribute instanceof AttributeLongTypeSnapshot)
    {
      attributeType = new AttributeIntegerType(attribute.getCode(), attributeLabel, attributeDescription, attribute.getIsDefault(), attribute.getIsRequired(), attribute.getIsUnique());
      attributeType.setIsChangeOverTime(attribute.getIsChangeOverTime());
    }
    else if (attribute instanceof AttributeDoubleTypeSnapshot)
    {
      AttributeFloatType attributeFloatType = new AttributeFloatType(attribute.getCode(), attributeLabel, attributeDescription, attribute.getIsDefault(), attribute.getIsRequired(), attribute.getIsUnique());
      attributeFloatType.setIsChangeOverTime(attribute.getIsChangeOverTime());
      attributeFloatType.setPrecision( ( (AttributeDoubleTypeSnapshot) attribute ).getPrecision());
      attributeFloatType.setScale( ( (AttributeDoubleTypeSnapshot) attribute ).getScale());

      attributeType = attributeFloatType;
    }
    else if (attribute instanceof AttributeClassificationTypeSnapshot)
    {
      AttributeClassificationType attributeClassificationType = new AttributeClassificationType(attribute.getCode(), attributeLabel, attributeDescription, attribute.getIsDefault(), attribute.getIsRequired(), attribute.getIsUnique());
      attributeClassificationType.setIsChangeOverTime(attribute.getIsChangeOverTime());
      attributeClassificationType.setClassificationType( ( (AttributeClassificationTypeSnapshot) attribute ).getClassificationType());
      attributeClassificationType.setRootTerm(new Term( ( (AttributeClassificationTypeSnapshot) attribute ).getRootTerm(), attributeLabel, attributeDescription));

      attributeType = attributeClassificationType;
    }
    else if (attribute instanceof AttributeBooleanTypeSnapshot)
    {
      attributeType = new AttributeBooleanType(attribute.getCode(), attributeLabel, attributeDescription, attribute.getIsDefault(), attribute.getIsRequired(), attribute.getIsUnique());
      attributeType.setIsChangeOverTime(attribute.getIsChangeOverTime());
    }
    else if (attribute instanceof AttributeLocalTypeSnapshot)
    {
      attributeType = new AttributeLocalType(attribute.getCode(), attributeLabel, attributeDescription, attribute.getIsDefault(), attribute.getIsRequired(), attribute.getIsUnique());
      attributeType.setIsChangeOverTime(attribute.getIsChangeOverTime());
    }
    else
    {
      throw new UnsupportedOperationException();
    }
    return attributeType;
  }

  public GeoObjectTypeSnapshot createRoot(SnapshotContainer<?> version)
  {
    return this.oSnapshotService.createRoot(version);
  }

  protected void assignPermissions(ComponentIF component)
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

  public BusinessTypeSnapshot getBusinessType(SnapshotContainer<?> version, String code)
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

  private String getEdgeName(GraphType type)
  {
    MdEdgeDAOIF mdEdge = type.getMdEdgeDAO();

    return getEdgeName(mdEdge);
  }

  private String getEdgeName(MdEdgeDAOIF mdEdge)
  {
    String className = mdEdge.getDBClassName();

    return this.hSnapshotService.getTableName(className);
  }

}
