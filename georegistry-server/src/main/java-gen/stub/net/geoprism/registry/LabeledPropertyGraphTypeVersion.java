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
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.Authenticate;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.constants.BusinessInfo;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdAttributeConcreteInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.graph.MdEdgeInfo;
import com.runwaysdk.constants.graph.MdVertexInfo;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.graph.GraphRequest;
import com.runwaysdk.dataaccess.metadata.MdAttributeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.dataaccess.metadata.graph.MdGeoVertexDAO;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdEdge;
import com.runwaysdk.system.metadata.MdVertex;

import net.geoprism.rbac.RoleConstants;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.progress.Progress;
import net.geoprism.registry.progress.ProgressService;

public class LabeledPropertyGraphTypeVersion extends LabeledPropertyGraphTypeVersionBase implements LabeledVersion
{
  private static final long  serialVersionUID = -351397872;

  private static Logger      logger           = LoggerFactory.getLogger(LabeledPropertyGraphTypeVersion.class);

  public static final String PREFIX           = "lt_";

  public static final String ORIGINAL_OID     = "originalOid";

  public static final String LEAF             = "leaf";

  public static final String TYPE_CODE        = "typeCode";

  public static final String ORG_CODE         = "orgCode";

  public static final String ATTRIBUTES       = "attributes";

  public static final String HIERARCHIES      = "hierarchies";

  public static final String DEFAULT_LOCALE   = "DefaultLocale";

  public static final String PERIOD           = "period";

  public static final String PUBLISHED        = "PUBLISHED";

  public static final String EXPLORATORY      = "EXPLORATORY";

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

    GraphDBService service = GraphDBService.getInstance();
    GraphRequest ddl = service.getDDLGraphDBRequest();

    while (service.isClassDefined(ddl, name))
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

  private String getEdgeName(ServerHierarchyType type)
  {

    MdEdgeDAOIF mdEdge = type.getMdEdgeDAO();

    String className = mdEdge.getDBClassName();

    return this.getTableName(className);
  }

  public String getEdgeName(String className)
  {
    int count = 0;

    String name = PREFIX + count + className;

    if (name.length() > 25)
    {
      name = name.substring(0, 25);
    }

    GraphDBService service = GraphDBService.getInstance();
    GraphRequest ddl = service.getDDLGraphDBRequest();

    while (service.isEdgeClassDefined(ddl, name))
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

  private boolean isValid(AttributeType attributeType)
  {
    if (attributeType.getName().equals(DefaultAttribute.UID.getName()))
    {
      return true;
    }

    if (attributeType.getName().equals(DefaultAttribute.SEQUENCE.getName()))
    {
      return false;
    }

    if (attributeType.getName().equals(DefaultAttribute.LAST_UPDATE_DATE.getName()))
    {
      return false;
    }

    if (attributeType.getName().equals(DefaultAttribute.CREATE_DATE.getName()))
    {
      return false;
    }

    if (attributeType.getName().equals(DefaultAttribute.TYPE.getName()))
    {
      return false;
    }

    if (attributeType.getName().equals(DefaultAttribute.EXISTS.getName()))
    {
      return false;
    }

    return true;
  }

  public boolean isValid(MdAttributeConcreteDAOIF mdAttribute)
  {
    if (mdAttribute.isSystem() || mdAttribute.definesAttribute().equals(DefaultAttribute.UID.getName()))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(DefaultAttribute.SEQUENCE.getName()))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(DefaultAttribute.LAST_UPDATE_DATE.getName()))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(DefaultAttribute.CREATE_DATE.getName()))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(DefaultAttribute.TYPE.getName()))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(DefaultAttribute.EXISTS.getName()))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(ORIGINAL_OID))
    {
      return false;
    }

    // if (mdAttribute.definesAttribute().endsWith("Oid"))
    // {
    // return false;
    // }

    if (mdAttribute.definesAttribute().equals(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(BusinessInfo.OWNER))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(BusinessInfo.KEY))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(BusinessInfo.DOMAIN))
    {
      return false;
    }

    return true;
  }

  private MdVertex createTable(ServerGeoObjectType type, MdVertexDAOIF rootMdVertex)
  {
    LabeledPropertyGraphType masterlist = this.getGraphType();

    String viewName = this.getTableName(type);

    // Create the MdTable
    MdGeoVertexDAO mdTableDAO = MdGeoVertexDAO.newInstance();
    mdTableDAO.setValue(MdVertexInfo.NAME, viewName);
    mdTableDAO.setValue(MdVertexInfo.PACKAGE, RegistryConstants.TABLE_PACKAGE);
    mdTableDAO.setStructValue(MdVertexInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, masterlist.getDisplayLabel().getValue());
    mdTableDAO.setValue(MdVertexInfo.DB_CLASS_NAME, viewName);
    mdTableDAO.setValue(MdVertexInfo.GENERATE_SOURCE, MdAttributeBooleanInfo.FALSE);
    mdTableDAO.setValue(MdVertexInfo.ENABLE_CHANGE_OVER_TIME, MdAttributeBooleanInfo.FALSE);
    mdTableDAO.setValue(MdVertexInfo.SUPER_MD_VERTEX, rootMdVertex.getOid());
    mdTableDAO.apply();

    MdVertexDAOIF sourceMdVertex = type.getMdVertex();

    List<? extends MdAttributeDAOIF> attributes = sourceMdVertex.getAllDefinedMdAttributes();

    attributes.forEach(attribute -> {
      if (!attribute.isSystem())
      {
        MdAttributeDAO mdAttribute = (MdAttributeDAO) attribute.copy();
        mdAttribute.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
        mdAttribute.apply();
      }
    });

    MdVertex mdVertex = (MdVertex) BusinessFacade.get(mdTableDAO);

    return mdVertex;
  }

  private MdEdge createEdge(ServerHierarchyType type, MdVertexDAOIF mdBusGeoEntity)
  {
    String viewName = this.getEdgeName(type);

    MdEdgeDAO mdEdgeDAO = MdEdgeDAO.newInstance();
    mdEdgeDAO.setValue(MdEdgeInfo.PACKAGE, RegistryConstants.UNIVERSAL_GRAPH_PACKAGE);
    mdEdgeDAO.setValue(MdEdgeInfo.NAME, viewName);
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
    // // Delete all jobs
    // List<ExecutableJob> jobs = this.getJobs();
    //
    // for (ExecutableJob job : jobs)
    // {
    // job.delete();
    // }
    //

    List<? extends Business> mdVertices = this.getChildren("net.geoprism.registry.action.GraphHasVertex").getAll();
    List<? extends Business> mdEdges = this.getChildren("net.geoprism.registry.action.GraphHasEdge").getAll();

    super.delete();

    mdEdges.forEach(e -> e.delete());
    mdVertices.forEach(v -> v.delete());
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
    // this.lock();
    //
    // try
    // {
    // LabeledPropertyGraphType masterlist = this.getGraphType();
    //
    // if (!masterlist.isValid())
    // {
    // throw new InvalidMasterListException();
    // }
    //
    // // Delete tile cache
    // MdBusinessDAO mdBusiness =
    // MdBusinessDAO.get(this.getMdBusinessOid()).getBusinessDAO();
    // mdBusiness.deleteAllRecords();
    //
    // MdAttributeConcreteDAO status = (MdAttributeConcreteDAO)
    // mdBusiness.definesAttribute("status");
    // if (status != null)
    // {
    // status.delete();
    // }
    //
    // MdAttributeConcreteDAO statusDefaultLocale = (MdAttributeConcreteDAO)
    // mdBusiness.definesAttribute("statusDefaultLocale");
    // if (statusDefaultLocale != null)
    // {
    // statusDefaultLocale.delete();
    // }
    //
    // ServerGeoObjectType type =
    // ServerGeoObjectType.get(masterlist.getUniversal());
    //
    // Collection<Locale> locales = LocalizationFacade.getInstalledLocales();
    //
    // // Add the type ancestor fields
    // Map<ServerHierarchyType, List<ServerGeoObjectType>> ancestorMap =
    // masterlist.getAncestorMap(type);
    // Collection<AttributeType> attributes = type.getAttributeMap().values();
    // Set<ServerHierarchyType> hierarchiesOfSubTypes =
    // type.getHierarchiesOfSubTypes();
    //
    // // ServerGeoObjectService service = new ServerGeoObjectService();
    // // ServerGeoObjectQuery query = service.createQuery(type,
    // // this.getPeriod());
    //
    // Date forDate = this.getForDate();
    //
    // BasicVertexRestriction restriction = masterlist.getRestriction(type,
    // forDate);
    // BasicVertexQuery query = new BasicVertexQuery(type, forDate);
    // query.setRestriction(restriction);
    // Long count = query.getCount();
    //
    // if (count == null)
    // {
    // count = 0L;
    // }
    //
    // long current = 0;
    //
    // try
    // {
    // ProgressService.put(this.getOid(), new Progress(0L, count, ""));
    // int pageSize = 1000;
    //
    // long skip = 0;
    //
    // while (skip < count)
    // {
    // query = new BasicVertexQuery(type, forDate);
    // query.setRestriction(restriction);
    // query.setLimit(pageSize);
    // query.setSkip(skip);
    //
    // // List<GeoObjectStatus> validStats = new
    // // ArrayList<GeoObjectStatus>();
    // // validStats.add(GeoObjectStatus.ACTIVE);
    // // validStats.add(GeoObjectStatus.INACTIVE);
    // // validStats.add(GeoObjectStatus.PENDING);
    // // validStats.add(GeoObjectStatus.NEW);
    // // query.setRestriction(new ServerStatusRestriction(validStats,
    // // this.getForDate(), JoinOp.OR));
    //
    // List<ServerGeoObjectIF> results = query.getResults();
    //
    // for (ServerGeoObjectIF result : results)
    // {
    // if (result.getExists(forDate))
    // {
    // Business business = new Business(mdBusiness.definesType());
    //
    // publish(masterlist, type, result, business, attributes, ancestorMap,
    // hierarchiesOfSubTypes, locales);
    //
    // Thread.yield();
    //
    // ProgressService.put(this.getOid(), new Progress(current++, count, ""));
    // }
    // }
    //
    // skip += pageSize;
    // }
    //
    // this.setPublishDate(new Date());
    // this.apply();
    //
    // return this.toJSON(true).toString();
    // }
    // finally
    // {
    // ProgressService.remove(this.getOid());
    // }
    // }
    // finally
    // {
    // this.unlock();
    // }

    return null;
  }

  private void publish(LabeledPropertyGraphType listType, ServerGeoObjectType type, ServerGeoObjectIF go, Business business, Collection<AttributeType> attributes, Map<ServerHierarchyType, List<ServerGeoObjectType>> ancestorMap, Set<ServerHierarchyType> hierarchiesOfSubTypes, Collection<Locale> locales)
  {
    // VertexServerGeoObject vertexGo = (VertexServerGeoObject) go;
    //
    // business.setValue(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME,
    // go.getGeometry());
    //
    // for (AttributeType attribute : attributes)
    // {
    // String name = attribute.getName();
    //
    // business.setValue(ORIGINAL_OID, go.getRunwayId());
    //
    // if (this.isValid(attribute))
    // {
    // Object value = go.getValue(name, this.getForDate());
    //
    // if (value != null)
    // {
    // if (value instanceof LocalizedValue && ( (LocalizedValue) value
    // ).isNull())
    // {
    // continue;
    // }
    //
    // if (attribute instanceof AttributeTermType)
    // {
    // Classifier classifier = (Classifier) value;
    //
    // Term term = ( (AttributeTermType) attribute
    // ).getTermByCode(classifier.getClassifierId()).get();
    // LocalizedValue label = term.getLabel();
    //
    // this.setValue(business, name, term.getCode());
    // this.setValue(business, name + DEFAULT_LOCALE,
    // label.getValue(LocalizedValue.DEFAULT_LOCALE));
    //
    // for (Locale locale : locales)
    // {
    // this.setValue(business, name + locale.toString(),
    // label.getValue(locale));
    // }
    // }
    // else if (attribute instanceof AttributeClassificationType)
    // {
    // String classificationTypeCode = ( (AttributeClassificationType) attribute
    // ).getClassificationType();
    // ClassificationType classificationType =
    // ClassificationType.getByCode(classificationTypeCode);
    // Classification classification =
    // Classification.getByOid(classificationType, (String) value);
    //
    // LocalizedValue label = classification.getDisplayLabel();
    //
    // this.setValue(business, name, classification.getCode());
    // this.setValue(business, name + DEFAULT_LOCALE,
    // label.getValue(LocalizedValue.DEFAULT_LOCALE));
    //
    // for (Locale locale : locales)
    // {
    // this.setValue(business, name + locale.toString(),
    // label.getValue(locale));
    // }
    // }
    // else if (attribute instanceof AttributeLocalType)
    // {
    // LocalizedValue label = (LocalizedValue) value;
    //
    // String defaultLocale = label.getValue(LocalizedValue.DEFAULT_LOCALE);
    //
    // if (defaultLocale == null)
    // {
    // defaultLocale = "";
    // }
    //
    // this.setValue(business, name + DEFAULT_LOCALE, defaultLocale);
    //
    // for (Locale locale : locales)
    // {
    // String localeValue = label.getValue(locale);
    //
    // if (localeValue == null)
    // {
    // localeValue = "";
    // }
    //
    // this.setValue(business, name + locale.toString(), localeValue);
    // }
    // }
    // else
    // {
    // this.setValue(business, name, value);
    // }
    // }
    // }
    // }
    //
    // Set<Entry<ServerHierarchyType, List<ServerGeoObjectType>>> entries =
    // ancestorMap.entrySet();
    //
    // for (Entry<ServerHierarchyType, List<ServerGeoObjectType>> entry :
    // entries)
    // {
    // ServerHierarchyType hierarchy = entry.getKey();
    //
    // Map<String, LocationInfo> map = vertexGo.getAncestorMap(hierarchy,
    // entry.getValue());
    //
    // Set<Entry<String, LocationInfo>> locations = map.entrySet();
    //
    // for (Entry<String, LocationInfo> location : locations)
    // {
    // String pCode = location.getKey();
    // LocationInfo vObject = location.getValue();
    //
    // if (vObject != null)
    // {
    // String attributeName = hierarchy.getCode().toLowerCase() +
    // pCode.toLowerCase();
    //
    // this.setValue(business, attributeName, vObject.getCode());
    // this.setValue(business, attributeName + DEFAULT_LOCALE,
    // vObject.getLabel());
    //
    // for (Locale locale : locales)
    // {
    // this.setValue(business, attributeName + locale.toString(),
    // vObject.getLabel(locale));
    // }
    // }
    // }
    // }
    //
    // for (ServerHierarchyType hierarchy : hierarchiesOfSubTypes)
    // {
    // ServerParentTreeNode node = go.getParentsForHierarchy(hierarchy, false,
    // this.getForDate());
    // List<ServerParentTreeNode> parents = node.getParents();
    //
    // if (parents.size() > 0)
    // {
    // ServerParentTreeNode parent = parents.get(0);
    //
    // String attributeName = hierarchy.getCode().toLowerCase();
    // ServerGeoObjectIF geoObject = parent.getGeoObject();
    // LocalizedValue label = geoObject.getDisplayLabel();
    //
    // this.setValue(business, attributeName, geoObject.getCode());
    // this.setValue(business, attributeName + DEFAULT_LOCALE,
    // label.getValue(DEFAULT_LOCALE));
    //
    // for (Locale locale : locales)
    // {
    // this.setValue(business, attributeName + locale.toString(),
    // label.getValue(locale));
    // }
    // }
    // }
    //
    // if (type.getGeometryType().equals(GeometryType.MULTIPOINT) ||
    // type.getGeometryType().equals(GeometryType.POINT) &&
    // listType.getIncludeLatLong())
    // {
    // Geometry geom = vertexGo.getGeometry();
    //
    // if (geom instanceof MultiPoint)
    // {
    // MultiPoint mp = (MultiPoint) geom;
    //
    // Coordinate[] coords = mp.getCoordinates();
    //
    // Coordinate firstCoord = coords[0];
    //
    // this.setValue(business, "latitude", String.valueOf(firstCoord.y));
    // this.setValue(business, "longitude", String.valueOf(firstCoord.x));
    // }
    // else if (geom instanceof Point)
    // {
    // Point point = (Point) geom;
    //
    // Coordinate firstCoord = point.getCoordinate();
    //
    // this.setValue(business, "latitude", String.valueOf(firstCoord.y));
    // this.setValue(business, "longitude", String.valueOf(firstCoord.x));
    // }
    // }
    //
    // business.apply();
  }

  private void setValue(Business business, String name, Object value)
  {
    // if (business.hasAttribute(name))
    // {
    // if (value != null)
    // {
    // business.setValue(name, value);
    // }
    // else
    // {
    // business.setValue(name, "");
    // }
    // }
  }

  public JsonObject toJSON(boolean includeAttribute)
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
  public static LabeledPropertyGraphTypeVersion create(LabeledPropertyGraphTypeEntry listEntry, boolean working, int versionNumber, JsonObject metadata)
  {
    LabeledPropertyGraphType listType = listEntry.getGraphType();

    LabeledPropertyGraphTypeVersion version = new LabeledPropertyGraphTypeVersion();
    version.setEntry(listEntry);
    version.setGraphType(listType);
    version.setForDate(listEntry.getForDate());
    version.setVersionNumber(versionNumber);
    version.apply();

    String tableName = version.getTableName(listType.getCode());

    MdGeoVertexDAO rootMdVertexDAO = MdGeoVertexDAO.newInstance();
    rootMdVertexDAO.setValue(MdVertexInfo.NAME, tableName);
    rootMdVertexDAO.setValue(MdVertexInfo.PACKAGE, RegistryConstants.TABLE_PACKAGE);
    rootMdVertexDAO.setStructValue(MdVertexInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, "Root Type");
    rootMdVertexDAO.setValue(MdVertexInfo.DB_CLASS_NAME, tableName);
    rootMdVertexDAO.setValue(MdVertexInfo.GENERATE_SOURCE, MdAttributeBooleanInfo.FALSE);
    rootMdVertexDAO.setValue(MdVertexInfo.ENABLE_CHANGE_OVER_TIME, MdAttributeBooleanInfo.FALSE);
    rootMdVertexDAO.apply();

    version.addChild(rootMdVertexDAO.getOid(), "net.geoprism.registry.action.GraphHasVertex").apply();

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

      version.addChild(mdVertex, "net.geoprism.registry.action.GraphHasVertex").apply();

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

      version.addChild(mdEdge, "net.geoprism.registry.action.GraphHasEdge").apply();

      LabeledPropertyGraphTypeVersion.assignDefaultRolePermissions(mdEdge);
    }

    return version;
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
