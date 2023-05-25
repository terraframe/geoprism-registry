package net.geoprism.registry;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdAttributeConcreteInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.MdAttributeTermInfo;
import com.runwaysdk.constants.graph.MdVertexInfo;
import com.runwaysdk.dataaccess.BusinessDAO;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.RelationshipDAOIF;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.metadata.MdAttributeDAO;
import com.runwaysdk.dataaccess.metadata.MdAttributeTermDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.constants.MdAttributePointInfo;
import com.runwaysdk.gis.dataaccess.MdAttributeGeometryDAOIF;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdGraphClassQuery;
import com.runwaysdk.system.metadata.MdVertex;

import net.geoprism.registry.conversion.AttributeTypeConverter;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.ServerGeoObjectType;

public class GeoObjectTypeSnapshot extends GeoObjectTypeSnapshotBase
{
  public static final String PREFIX           = "g_";

  public static final String SPLIT            = "__";

  @SuppressWarnings("unused")
  private static final long  serialVersionUID = -1232639915;

  public GeoObjectTypeSnapshot()
  {
    super();
  }

  @Override
  public String toString()
  {
    return this.getCode();
  }

  public boolean isRoot()
  {
    return StringUtils.isEmpty(this.getGeometryType());
  }

  @Override
  public void delete()
  {
    MdVertex mdVertex = this.getGraphMdVertex();

    super.delete();

    mdVertex.delete();
  }

  public void truncate()
  {
    MdVertex mdVertex = this.getGraphMdVertex();

    GraphDBService service = GraphDBService.getInstance();
    service.command(service.getGraphDBRequest(), "DELETE VERTEX FROM " + mdVertex.getDbClassName(), new HashMap<>());
  }

  public List<AttributeType> getAttributeTypes()
  {
    AttributeTypeConverter converter = new AttributeTypeConverter();

    List<AttributeType> attributes = new LinkedList<>();

    MdVertexDAOIF mdVertex = MdVertexDAO.get(this.getGraphMdVertexOid());
    List<? extends MdAttributeConcreteDAOIF> mdAttributes = mdVertex.definesAttributes();
    mdAttributes.forEach(attribute -> {
      if (! ( attribute instanceof MdAttributeGeometryDAOIF ))
      {
        attributes.add(converter.build(attribute));
      }

      // TODO Handle Term and Classification attributes
    });
    return attributes;
  }

  public GeoObjectType toGeoObjectType()
  {
    String code = this.getCode();
    GeometryType geometryType = GeometryType.valueOf(this.getGeometryType());
    LocalizedValue label = LocalizedValueConverter.convertNoAutoCoalesce(this.getDisplayLabel());
    LocalizedValue description = LocalizedValueConverter.convertNoAutoCoalesce(this.getDescription());

    GeoObjectType type = new GeoObjectType(code, geometryType, label, description, getIsGeometryEditable(), null, null);

    List<AttributeType> attributes = getAttributeTypes();

    for (AttributeType attribute : attributes)
    {
      type.addAttribute(attribute);
    }

    return type;
  }

  public JsonObject toJSON()
  {
    JsonArray attributes = new JsonArray();

    this.getAttributeTypes().forEach(attribute -> attributes.add(attribute.toJSON()));

    JsonObject typeObject = new JsonObject();
    typeObject.addProperty(CODE, this.getCode());
    typeObject.add(DISPLAYLABEL, AttributeTypeConverter.convertNoAutoCoalesce(this.getDisplayLabel()).toJSON());
    typeObject.add(DESCRIPTION, AttributeTypeConverter.convertNoAutoCoalesce(this.getDescription()).toJSON());
    typeObject.addProperty(GEOMETRYTYPE, this.getGeometryType());
    typeObject.addProperty(ISABSTRACT, this.getIsAbstract());
    typeObject.addProperty(ISROOT, this.getIsRoot());
    typeObject.addProperty(ISPRIVATE, this.getIsPrivate());
    typeObject.add("attributes", attributes);

    GeoObjectTypeSnapshot parent = this.getParent();

    if (parent != null)
    {
      typeObject.addProperty(PARENT, parent.getCode());
    }

    return typeObject;
  }

  private static void createGeometryAttribute(ServerGeoObjectType type, MdVertexDAO mdTableDAO)
  {
    createGeometryAttribute(type.getGeometryType(), mdTableDAO);
  }

  private static void createGeometryAttribute(GeometryType geometryType, MdVertexDAO mdTableDAO)
  {
    MdVertexDAOIF mdGeoVertex = MdVertexDAO.getMdVertexDAO(GeoVertex.CLASS);
    Map<String, ? extends MdAttributeDAOIF> map = mdGeoVertex.getAllDefinedMdAttributeMap();

    // Create the geometry attribute
    if (geometryType.equals(GeometryType.LINE))
    {
      MdAttributeDAO mdAttribute = (MdAttributeDAO) map.get(GeoVertex.GEOLINE.toLowerCase()).copy();
      mdAttribute.setValue(MdAttributePointInfo.NAME, DefaultAttribute.GEOMETRY.getName());
      mdAttribute.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
      mdAttribute.apply();
    }
    else if (geometryType.equals(GeometryType.MULTILINE))
    {
      MdAttributeDAO mdAttribute = (MdAttributeDAO) map.get(GeoVertex.GEOMULTILINE.toLowerCase()).copy();
      mdAttribute.setValue(MdAttributePointInfo.NAME, DefaultAttribute.GEOMETRY.getName());
      mdAttribute.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
      mdAttribute.apply();
    }
    else if (geometryType.equals(GeometryType.POINT))
    {
      MdAttributeDAO mdAttribute = (MdAttributeDAO) map.get(GeoVertex.GEOPOINT.toLowerCase()).copy();
      mdAttribute.setValue(MdAttributePointInfo.NAME, DefaultAttribute.GEOMETRY.getName());
      mdAttribute.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
      mdAttribute.apply();
    }
    else if (geometryType.equals(GeometryType.MULTIPOINT))
    {
      MdAttributeDAO mdAttribute = (MdAttributeDAO) map.get(GeoVertex.GEOMULTIPOINT.toLowerCase()).copy();
      mdAttribute.setValue(MdAttributePointInfo.NAME, DefaultAttribute.GEOMETRY.getName());
      mdAttribute.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
      mdAttribute.apply();
    }
    else if (geometryType.equals(GeometryType.POLYGON))
    {
      MdAttributeDAO mdAttribute = (MdAttributeDAO) map.get(GeoVertex.GEOPOLYGON.toLowerCase()).copy();
      mdAttribute.setValue(MdAttributePointInfo.NAME, DefaultAttribute.GEOMETRY.getName());
      mdAttribute.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
      mdAttribute.apply();
    }
    else if (geometryType.equals(GeometryType.MULTIPOLYGON))
    {
      MdAttributeDAO mdAttribute = (MdAttributeDAO) map.get(GeoVertex.GEOMULTIPOLYGON.toLowerCase()).copy();
      mdAttribute.setValue(MdAttributePointInfo.NAME, DefaultAttribute.GEOMETRY.getName());
      mdAttribute.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
      mdAttribute.apply();
    }
    else if (geometryType.equals(GeometryType.MIXED))
    {
      MdAttributeDAO mdAttribute = (MdAttributeDAO) map.get(GeoVertex.SHAPE.toLowerCase()).copy();
      mdAttribute.setValue(MdAttributePointInfo.NAME, DefaultAttribute.GEOMETRY.getName());
      mdAttribute.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
      mdAttribute.apply();
    }
  }

  private static String getTableName(ServerGeoObjectType type)
  {

    MdVertexDAOIF mdVertex = type.getMdVertex();

    String className = mdVertex.getDBClassName();

    return getTableName(className);
  }

  public static String getTableName(String className)
  {
    int count = 0;

    String name = PREFIX + count + SPLIT + className;

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

  private static boolean isTableNameInUse(String name)
  {
    MdGraphClassQuery query = new MdGraphClassQuery(new QueryFactory());
    query.WHERE(query.getDbClassName().EQ(name));

    return query.getCount() > 0;
  }

  @Transaction
  public static GeoObjectTypeSnapshot createRoot(LabeledPropertyGraphTypeVersion version)
  {
    String viewName = getTableName("root_vertex");

    // Create the MdTable
    MdVertexDAO rootMdVertexDAO = MdVertexDAO.newInstance();
    rootMdVertexDAO.setValue(MdVertexInfo.NAME, viewName);
    rootMdVertexDAO.setValue(MdVertexInfo.PACKAGE, RegistryConstants.TABLE_PACKAGE);
    rootMdVertexDAO.setStructValue(MdVertexInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, "Root Type");
    rootMdVertexDAO.setValue(MdVertexInfo.DB_CLASS_NAME, viewName);
    rootMdVertexDAO.setValue(MdVertexInfo.GENERATE_SOURCE, MdAttributeBooleanInfo.FALSE);
    rootMdVertexDAO.setValue(MdVertexInfo.ENABLE_CHANGE_OVER_TIME, MdAttributeBooleanInfo.FALSE);
    rootMdVertexDAO.setValue(MdVertexInfo.ABSTRACT, MdAttributeBooleanInfo.TRUE);
    rootMdVertexDAO.apply();

    MdVertex graphMdVertex = (MdVertex) BusinessFacade.get(rootMdVertexDAO);

    GeoObjectTypeSnapshot snapshot = new GeoObjectTypeSnapshot();
    snapshot.setVersion(version);
    snapshot.setGraphMdVertex(graphMdVertex);
    snapshot.setCode(viewName);
    snapshot.setIsAbstract(true);
    snapshot.setIsRoot(true);
    snapshot.setIsPrivate(true);
    LocalizedValueConverter.populate(snapshot.getDisplayLabel(), LocalizedValueConverter.convertNoAutoCoalesce(graphMdVertex.getDisplayLabel()));
    LocalizedValueConverter.populate(snapshot.getDescription(), LocalizedValueConverter.convertNoAutoCoalesce(graphMdVertex.getDescription()));
    snapshot.apply();

    return snapshot;
  }

  @Transaction
  public static GeoObjectTypeSnapshot create(LabeledPropertyGraphTypeVersion version, ServerGeoObjectType type, GeoObjectTypeSnapshot parent)
  {
    String viewName = getTableName(type);

    // Create the MdTable
    MdVertexDAO mdVertexDAO = MdVertexDAO.newInstance();
    mdVertexDAO.setValue(MdVertexInfo.NAME, viewName);
    mdVertexDAO.setValue(MdVertexInfo.PACKAGE, RegistryConstants.TABLE_PACKAGE);
    mdVertexDAO.setValue(MdVertexInfo.ABSTRACT, type.getIsAbstract());
    LocalizedValueConverter.populate(mdVertexDAO, MdVertexInfo.DISPLAY_LABEL, type.getLabel());
    LocalizedValueConverter.populate(mdVertexDAO, MdVertexInfo.DESCRIPTION, type.getDescription());
    mdVertexDAO.setValue(MdVertexInfo.DB_CLASS_NAME, viewName);
    mdVertexDAO.setValue(MdVertexInfo.GENERATE_SOURCE, MdAttributeBooleanInfo.FALSE);
    mdVertexDAO.setValue(MdVertexInfo.ENABLE_CHANGE_OVER_TIME, MdAttributeBooleanInfo.FALSE);
    mdVertexDAO.setValue(MdVertexInfo.SUPER_MD_VERTEX, parent.getGraphMdVertexOid());
    mdVertexDAO.apply();

    if (!type.getIsAbstract())
    {
      createGeometryAttribute(type, mdVertexDAO);
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
    LocalizedValueConverter.populate(snapshot.getDisplayLabel(), type.getLabel());
    LocalizedValueConverter.populate(snapshot.getDescription(), type.getDescription());
    snapshot.apply();

    LabeledPropertyGraphTypeVersion.assignDefaultRolePermissions(mdVertexDAO);

    return snapshot;
  }

  @Transaction
  public static GeoObjectTypeSnapshot create(LabeledPropertyGraphTypeVersion version, JsonObject type)
  {
    GeoObjectTypeSnapshot parent = GeoObjectTypeSnapshot.get(version, type.get(PARENT).getAsString());

    String code = type.get(CODE).getAsString();
    String viewName = getTableName(code);
    boolean isAbstract = type.get(ISABSTRACT).getAsBoolean();
    boolean isRoot = type.get(ISROOT).getAsBoolean();
    boolean isPrivate = type.get(ISPRIVATE).getAsBoolean();
    GeometryType geometryType = GeometryType.valueOf(type.get(GEOMETRYTYPE).getAsString());
    LocalizedValue label = LocalizedValue.fromJSON(type.get(DISPLAYLABEL).getAsJsonObject());
    LocalizedValue description = LocalizedValue.fromJSON(type.get(DESCRIPTION).getAsJsonObject());

    // Create the MdTable
    MdVertexDAO mdTableDAO = MdVertexDAO.newInstance();
    mdTableDAO.setValue(MdVertexInfo.NAME, viewName);
    mdTableDAO.setValue(MdVertexInfo.PACKAGE, RegistryConstants.TABLE_PACKAGE);
    LocalizedValueConverter.populate(mdTableDAO, MdVertexInfo.DISPLAY_LABEL, label);
    LocalizedValueConverter.populate(mdTableDAO, MdVertexInfo.DESCRIPTION, description);
    mdTableDAO.setValue(MdVertexInfo.DB_CLASS_NAME, viewName);
    mdTableDAO.setValue(MdVertexInfo.GENERATE_SOURCE, MdAttributeBooleanInfo.FALSE);
    mdTableDAO.setValue(MdVertexInfo.ENABLE_CHANGE_OVER_TIME, MdAttributeBooleanInfo.FALSE);
    mdTableDAO.setValue(MdVertexInfo.SUPER_MD_VERTEX, parent.getGraphMdVertexOid());
    mdTableDAO.apply();

    MdVertex mdTable = (MdVertex) BusinessFacade.get(mdTableDAO);

    if (!isAbstract)
    {
      createGeometryAttribute(geometryType, mdTableDAO);
    }

    JsonArray attributes = type.get("attributes").getAsJsonArray();

    attributes.forEach(joAttr -> {
      AttributeType attributeType = AttributeType.parse(joAttr.getAsJsonObject());

      ServerGeoObjectType.createMdAttributeFromAttributeType(mdTable, attributeType);
    });

    GeoObjectTypeSnapshot snapshot = new GeoObjectTypeSnapshot();
    snapshot.setVersion(version);
    snapshot.setGraphMdVertex(mdTable);
    snapshot.setCode(code);
    snapshot.setGeometryType(geometryType.name());
    snapshot.setIsAbstract(isAbstract);
    snapshot.setIsRoot(isRoot);
    snapshot.setIsPrivate(isPrivate);
    LocalizedValueConverter.populate(snapshot.getDisplayLabel(), label);
    LocalizedValueConverter.populate(snapshot.getDescription(), description);
    snapshot.setParent(parent);
    snapshot.apply();

    return snapshot;
  }

  public static GeoObjectTypeSnapshot get(LabeledPropertyGraphTypeVersion version, String code)
  {
    GeoObjectTypeSnapshotQuery query = new GeoObjectTypeSnapshotQuery(new QueryFactory());
    query.WHERE(query.getVersion().EQ(version));
    query.AND(query.getCode().EQ(code));

    try (OIterator<? extends GeoObjectTypeSnapshot> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    return null;
  }

  public static GeoObjectTypeSnapshot getRoot(LabeledPropertyGraphTypeVersion version)
  {
    GeoObjectTypeSnapshotQuery query = new GeoObjectTypeSnapshotQuery(new QueryFactory());
    query.WHERE(query.getVersion().EQ(version));
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
}
