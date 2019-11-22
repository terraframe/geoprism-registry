package net.geoprism.registry.model.graph;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections4.map.HashedMap;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.DefaultTerms;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.Attribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.UnknownTermException;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.json.JSONArray;
import org.json.JSONException;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.graph.VertexObjectDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import net.geoprism.dashboard.GeometryUpdateException;
import net.geoprism.ontology.Classifier;
import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.GeometryTypeException;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.io.TermValueException;
import net.geoprism.registry.model.AbstractServerGeoObject;
import net.geoprism.registry.model.LocationInfo;
import net.geoprism.registry.model.ServerChildTreeNode;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.service.ConversionService;
import net.geoprism.registry.service.RegistryIdService;
import net.geoprism.registry.service.ServiceFactory;

public class VertexServerGeoObject extends AbstractServerGeoObject implements ServerGeoObjectIF, LocationInfo
{
  private ServerGeoObjectType type;

  private VertexObject        vertex;

  private Date                date;

  public VertexServerGeoObject(ServerGeoObjectType type, VertexObject vertex)
  {
    this(type, vertex, null);
  }

  public VertexServerGeoObject(ServerGeoObjectType type, VertexObject vertex, Date date)
  {
    this.type = type;
    this.vertex = vertex;
    this.date = date;
  }

  public ServerGeoObjectType getType()
  {
    return type;
  }

  public void setType(ServerGeoObjectType type)
  {
    this.type = type;
  }

  public VertexObject getVertex()
  {
    return vertex;
  }

  public void setVertex(VertexObject vertex)
  {
    this.vertex = vertex;
  }

  @Override
  public void setCode(String code)
  {
    this.vertex.setValue(GeoVertex.GEOID, code);
    this.vertex.setValue(DefaultAttribute.CODE.getName(), code);
  }

  @Override
  public void setGeometry(Geometry geometry)
  {
    if (!this.isValidGeometry(geometry))
    {
      GeometryTypeException ex = new GeometryTypeException();
      ex.setActualType(geometry.getGeometryType());
      ex.setExpectedType(this.getType().getGeometryType().name());

      throw ex;
    }

    // Populate the correct geom field
    String geometryAttribute = this.getGeometryAttributeName();

    this.getVertex().setValue(geometryAttribute, geometry);
  }

  @Override
  public void setGeometry(Geometry geometry, Date startDate, Date endDate)
  {
    if (!this.isValidGeometry(geometry))
    {
      GeometryTypeException ex = new GeometryTypeException();
      ex.setActualType(geometry.getGeometryType());
      ex.setExpectedType(this.getType().getGeometryType().name());

      throw ex;
    }

    // Populate the correct geom field
    String geometryAttribute = this.getGeometryAttributeName();

    this.getVertex().setValue(geometryAttribute, geometry, startDate, endDate);
  }

  @Override
  public void setStatus(GeoObjectStatus status)
  {
    this.vertex.setValue(DefaultAttribute.STATUS.getName(), status.getOid());
  }

  @Override
  public void setStatus(GeoObjectStatus status, Date startDate, Date endDate)
  {
    this.vertex.setValue(DefaultAttribute.STATUS.getName(), status.getOid(), startDate, endDate);
  }

  @Override
  public GeoObjectStatus getStatus()
  {
    Set<String> value = this.vertex.getObjectValue(DefaultAttribute.STATUS.getName(), this.date);

    return GeoObjectStatus.get(value.iterator().next());
  }

  @Override
  public void setUid(String uid)
  {
    this.vertex.setValue(RegistryConstants.UUID, uid);
  }

  @Override
  public void setDisplayLabel(LocalizedValue value)
  {
    LocalizedValueConverter.populate(this.vertex, DefaultAttribute.DISPLAY_LABEL.getName(), value);
  }

  @Override
  public void setDisplayLabel(LocalizedValue value, Date startDate, Date endDate)
  {
    LocalizedValueConverter.populate(this.vertex, DefaultAttribute.DISPLAY_LABEL.getName(), value, startDate, endDate);
  }

  @Override
  public String getLabel()
  {
    String value = this.vertex.getEmbeddedValue(DefaultAttribute.DISPLAY_LABEL.getName(), MdAttributeLocalInfo.DEFAULT_LOCALE, this.date);

    if (value == null)
    {
      return "";
    }

    return value;
  }

  @Override
  public String getLabel(Locale locale)
  {
    String value = this.vertex.getEmbeddedValue(DefaultAttribute.DISPLAY_LABEL.getName(), locale.toString(), this.date);

    if (value == null)
    {
      return "";
    }

    return value;
  }

  @Override
  public void setValue(String attributeName, Object value)
  {
    if (attributeName.contentEquals(DefaultAttribute.DISPLAY_LABEL.getName()))
    {
      this.setDisplayLabel((LocalizedValue) value);
    }
    else
    {
      this.vertex.setValue(attributeName, value);
    }
  }

  @Override
  public void setValue(String attributeName, Object value, Date startDate, Date endDate)
  {
    if (attributeName.contentEquals(DefaultAttribute.DISPLAY_LABEL.getName()))
    {
      this.setDisplayLabel((LocalizedValue) value, startDate, endDate);
    }
    else
    {
      this.vertex.setValue(attributeName, value, startDate, endDate);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void populate(GeoObject geoObject)
  {
    GeoObjectStatus gos = this.vertex.isNew() ? GeoObjectStatus.PENDING : ConversionService.getInstance().termToGeoObjectStatus(geoObject.getStatus());

    Map<String, AttributeType> attributes = geoObject.getType().getAttributeMap();
    attributes.forEach((attributeName, attribute) -> {
      if (attributeName.equals(DefaultAttribute.STATUS.getName()) || attributeName.equals(DefaultAttribute.DISPLAY_LABEL.getName()) || attributeName.equals(DefaultAttribute.CODE.getName()) || attributeName.equals(DefaultAttribute.UID.getName()))
      {
        // Ignore the attributes
      }
      else if (this.vertex.hasAttribute(attributeName) && !this.vertex.getMdAttributeDAO(attributeName).isSystem())
      {
        if (attribute instanceof AttributeTermType)
        {
          Iterator<String> it = (Iterator<String>) geoObject.getValue(attributeName);

          if (it.hasNext())
          {
            String code = it.next();

            String classifierKey = Classifier.buildKey(RegistryConstants.REGISTRY_PACKAGE, code);
            Classifier classifier = Classifier.getByKey(classifierKey);

            this.vertex.setValue(attributeName, classifier.getOid());
          }
          else
          {
            this.vertex.setValue(attributeName, (String) null);
          }
        }
        else
        {
          Object value = geoObject.getValue(attributeName);

          if (value != null)
          {
            this.vertex.setValue(attributeName, value);
          }
          else
          {
            this.vertex.setValue(attributeName, (String) null);
          }
        }
      }
    });

    this.setUid(geoObject.getUid());
    this.setCode(geoObject.getCode());
    this.setStatus(gos);
    this.setDisplayLabel(geoObject.getDisplayLabel());
    this.setGeometry(geoObject.getGeometry());
  }

  private String getGeometryAttributeName()
  {
    GeometryType geometryType = this.type.getGeometryType();

    if (geometryType.equals(GeometryType.LINE))
    {
      return GeoVertex.GEOLINE;
    }
    else if (geometryType.equals(GeometryType.MULTILINE))
    {
      return GeoVertex.GEOMULTILINE;
    }
    else if (geometryType.equals(GeometryType.POINT))
    {
      return GeoVertex.GEOPOINT;
    }
    else if (geometryType.equals(GeometryType.MULTIPOINT))
    {
      return GeoVertex.GEOMULTIPOINT;
    }
    else if (geometryType.equals(GeometryType.POLYGON))
    {
      return GeoVertex.GEOPOLYGON;
    }
    else if (geometryType.equals(GeometryType.MULTIPOLYGON))
    {
      return GeoVertex.GEOMULTIPOLYGON;
    }

    throw new UnsupportedOperationException("Unsupported geometry type [" + geometryType.name() + "]");
  }

  public Map<String, ServerHierarchyType> getHierarchyTypeMap(String[] relationshipTypes)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, LocationInfo> getAncestorMap(ServerHierarchyType hierarchy)
  {
    TreeMap<String, LocationInfo> map = new TreeMap<String, LocationInfo>();

    GraphQuery<VertexObject> query = buildAncestorQuery(hierarchy);

    List<VertexObject> results = query.getResults();
    results.forEach(result -> {
      MdVertexDAOIF mdClass = (MdVertexDAOIF) result.getMdClass();
      ServerGeoObjectType vType = ServerGeoObjectType.get(mdClass);

      map.put(vType.getUniversal().getKey(), new VertexServerGeoObject(type, result, this.date));
    });

    return map;
  }

  private GraphQuery<VertexObject> buildAncestorQuery(ServerHierarchyType hierarchy)
  {
    String dbClassName = this.getMdClass().getDBClassName();

    if (this.date == null)
    {
      StringBuilder statement = new StringBuilder();
      statement.append("MATCH ");
      statement.append("{class:" + dbClassName + ", where: (@rid=:rid)}");
      statement.append(".in('" + hierarchy.getMdEdge().getDBClassName() + "')");
      statement.append("{as: ancestor, where: (status CONTAINS :status), while: (true)}");
      statement.append("RETURN $elements");

      GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());
      query.setParameter("rid", this.vertex.getRID());
      query.setParameter("status", GeoObjectStatus.ACTIVE.getOid());

      return query;
    }
    else
    {
      StringBuilder statement = new StringBuilder();
      statement.append("MATCH ");
      statement.append("{class:" + dbClassName + ", where: (@rid=:rid)}");
      statement.append(".(inE('" + hierarchy.getMdEdge().getDBClassName() + "'){where: (:date BETWEEN startDate AND endDate)}.outV())");
      statement.append("{as: ancestor, where: (status_cot CONTAINS (value CONTAINS :status AND :date BETWEEN startDate AND endDate )), while: (true)}");
      statement.append("RETURN $elements");

      GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());
      query.setParameter("rid", this.vertex.getRID());
      query.setParameter("date", this.date);
      query.setParameter("status", GeoObjectStatus.ACTIVE.getOid());

      return query;
    }
  }

  private MdVertexDAOIF getMdClass()
  {
    return (MdVertexDAOIF) this.vertex.getMdClass();
  }

  protected boolean isValidGeometry(Geometry geometry)
  {
    if (geometry != null)
    {
      GeometryType type = this.type.getGeometryType();

      if (type.equals(GeometryType.LINE) && ! ( geometry instanceof LineString ))
      {
        return false;
      }
      else if (type.equals(GeometryType.MULTILINE) && ! ( geometry instanceof MultiLineString ))
      {
        return false;
      }
      else if (type.equals(GeometryType.POINT) && ! ( geometry instanceof Point ))
      {
        return false;
      }
      else if (type.equals(GeometryType.MULTIPOINT) && ! ( geometry instanceof MultiPoint ))
      {
        return false;
      }
      else if (type.equals(GeometryType.POLYGON) && ! ( geometry instanceof Polygon ))
      {
        return false;
      }
      else if (type.equals(GeometryType.MULTIPOLYGON) && ! ( geometry instanceof MultiPolygon ))
      {
        return false;
      }

      return true;
    }

    return true;
  }

  @Override
  public String getCode()
  {
    return (String) this.vertex.getObjectValue(DefaultAttribute.CODE.getName());
  }

  @Override
  public String getUid()
  {
    return (String) this.vertex.getObjectValue(RegistryConstants.UUID);
  }

  @Override
  public String getRunwayId()
  {
    return this.vertex.getOid();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<? extends MdAttributeConcreteDAOIF> getMdAttributeDAOs()
  {
    return (List<? extends MdAttributeConcreteDAOIF>) this.vertex.getMdAttributeDAOs();
  }

  @Override
  public Object getValue(String attributeName)
  {
    if (attributeName.equals(DefaultAttribute.CODE.getName()))
    {
      return this.getCode();
    }
    else if (attributeName.equals(DefaultAttribute.UID.getName()))
    {
      return this.getUid();
    }
    else if (attributeName.equals(DefaultAttribute.DISPLAY_LABEL.getName()))
    {
      return this.getDisplayLabel();
    }

    MdAttributeConcreteDAOIF mdAttribute = this.vertex.getMdAttributeDAO(attributeName);

    Object value = this.vertex.getObjectValue(attributeName, this.date);

    if (value != null && mdAttribute instanceof MdAttributeTermDAOIF)
    {
      return Classifier.get((String) value);
    }

    return value;
  }

  @Override
  public void lock()
  {
    // Do nothing
  }

  @Override
  public void apply(boolean isImport)
  {
    if (!isImport && !this.vertex.isNew() && !this.getType().isGeometryEditable() && this.vertex.isModified(this.getGeometryAttributeName()))
    {
      throw new GeometryUpdateException();
    }

    this.getVertex().apply();
  }

  @Override
  public String bbox()
  {
    Geometry geometry = this.getGeometry();

    if (geometry != null)
    {
      try
      {
        Envelope e = geometry.getEnvelopeInternal();

        JSONArray bboxArr = new JSONArray();
        bboxArr.put(e.getMinX());
        bboxArr.put(e.getMinY());
        bboxArr.put(e.getMaxX());
        bboxArr.put(e.getMaxY());

        return bboxArr.toString();
      }
      catch (JSONException ex)
      {
        throw new ProgrammingErrorException(ex);
      }
    }

    return null;
  }

  @Transaction
  public void removeChild(ServerGeoObjectIF child, String hierarchyCode)
  {
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(hierarchyCode);
    child.removeParent(this, hierarchyType);
  }

  @Transaction
  @Override
  public ServerParentTreeNode addChild(ServerGeoObjectIF child, ServerHierarchyType hierarchy)
  {
    return child.addParent(this, hierarchy);
  }

  @Transaction
  @Override
  public ServerParentTreeNode addChild(ServerGeoObjectIF child, ServerHierarchyType hierarchy, Date startDate, Date endDate)
  {
    return child.addParent(this, hierarchy, startDate, endDate);
  }

  @Override
  public ServerChildTreeNode getChildGeoObjects(String[] childrenTypes, Boolean recursive)
  {
    return internalGetChildGeoObjects(this, childrenTypes, recursive, null, this.date);
  }

  @Override
  public ServerParentTreeNode getParentGeoObjects(String[] parentTypes, Boolean recursive)
  {
    return internalGetParentGeoObjects(this, parentTypes, recursive, null, this.date);
  }

  @Override
  public ServerParentTreeNode addParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType)
  {
    if (!hierarchyType.getUniversalType().equals(AllowedIn.CLASS))
    {
      GeoEntity.validateUniversalRelationship(this.getType().getUniversal(), parent.getType().getUniversal(), hierarchyType.getUniversalType());
    }

    if (this.getVertex().isNew() || !this.exists(parent, hierarchyType, null, null))
    {
      EdgeObject edge = this.getVertex().addParent( ( (VertexServerGeoObject) parent ).getVertex(), hierarchyType.getMdEdge());
      edge.apply();
    }

    ServerParentTreeNode node = new ServerParentTreeNode(this, hierarchyType, this.date);
    node.addParent(new ServerParentTreeNode(parent, hierarchyType, this.date));

    return node;
  }

  @Override
  public ServerParentTreeNode addParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType, Date startDate, Date endDate)
  {
    if (!hierarchyType.getUniversalType().equals(AllowedIn.CLASS))
    {
      GeoEntity.validateUniversalRelationship(this.getType().getUniversal(), parent.getType().getUniversal(), hierarchyType.getUniversalType());
    }

    if (this.getVertex().isNew() || !this.exists(parent, hierarchyType, startDate, endDate))
    {
      EdgeObject edge = this.getVertex().addParent( ( (VertexServerGeoObject) parent ).getVertex(), hierarchyType.getMdEdge());
      edge.setValue(GeoVertex.START_DATE, startDate);
      edge.setValue(GeoVertex.END_DATE, endDate);
      edge.apply();
    }

    ServerParentTreeNode node = new ServerParentTreeNode(this, hierarchyType, startDate);
    node.addParent(new ServerParentTreeNode(parent, hierarchyType, startDate));

    return node;
  }

  @Override
  public void removeParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType)
  {
    this.getVertex().removeParent( ( (VertexServerGeoObject) parent ).getVertex(), hierarchyType.getMdEdge());
  }

  @Override
  public GeoObject toGeoObject()
  {
    Map<String, Attribute> attributeMap = GeoObject.buildAttributeMap(type.getType());

    GeoObject geoObj = new GeoObject(type.getType(), type.getGeometryType(), attributeMap);

    if (vertex.isNew())// && !vertex.isAppliedToDB())
    {
      geoObj.setUid(RegistryIdService.getInstance().next());

      geoObj.setStatus(ServiceFactory.getAdapter().getMetadataCache().getTerm(DefaultTerms.GeoObjectStatusTerm.NEW.code).get());
    }
    else
    {
      Map<String, AttributeType> attributes = type.getAttributeMap();
      attributes.forEach((attributeName, attribute) -> {
        if (attributeName.equals(DefaultAttribute.STATUS.getName()))
        {
          Set<String> value = vertex.getObjectValue(attributeName, this.date);
          String oid = value.iterator().next();

          GeoObjectStatus gos = GeoObjectStatus.valueOf(oid);
          Term statusTerm = ServiceFactory.getConversionService().geoObjectStatusToTerm(gos);

          geoObj.setStatus(statusTerm);
        }
        else if (attributeName.equals(DefaultAttribute.TYPE.getName()))
        {
          // Ignore
        }
        else if (vertex.hasAttribute(attributeName))
        {
          Object value = vertex.getObjectValue(attributeName, this.date);

          if (value != null)
          {
            if (attribute instanceof AttributeTermType)
            {
              Classifier classifier = Classifier.get((String) value);

              try
              {
                geoObj.setValue(attributeName, classifier.getClassifierId());
              }
              catch (UnknownTermException e)
              {
                TermValueException ex = new TermValueException();
                ex.setAttributeLabel(e.getAttribute().getLabel().getValue());
                ex.setCode(e.getCode());

                throw e;
              }
            }
            else
            {
              geoObj.setValue(attributeName, value);
            }
          }
        }
      });
    }

    geoObj.setUid(vertex.getObjectValue(RegistryConstants.UUID));
    geoObj.setCode(vertex.getObjectValue(DefaultAttribute.CODE.getName()));
    geoObj.setGeometry(this.getGeometry());
    geoObj.setDisplayLabel(this.getDisplayLabel());

    return geoObj;
  }

  public LocalizedValue getDisplayLabel()
  {
    GraphObject graphObject = vertex.getEmbeddedComponent(DefaultAttribute.DISPLAY_LABEL.getName(), this.date);

    return LocalizedValueConverter.convert(graphObject);
  }

  public Geometry getGeometry()
  {
    GeometryType geometryType = this.getType().getGeometryType();

    if (geometryType.equals(GeometryType.LINE))
    {
      return (Geometry) vertex.getObjectValue(GeoVertex.GEOLINE, this.date);
    }
    else if (geometryType.equals(GeometryType.MULTILINE))
    {
      return (Geometry) vertex.getObjectValue(GeoVertex.GEOMULTILINE, this.date);
    }
    else if (geometryType.equals(GeometryType.POINT))
    {
      return (Geometry) vertex.getObjectValue(GeoVertex.GEOPOINT, this.date);
    }
    else if (geometryType.equals(GeometryType.MULTIPOINT))
    {
      return (Geometry) vertex.getObjectValue(GeoVertex.GEOMULTIPOINT, this.date);
    }
    else if (geometryType.equals(GeometryType.POLYGON))
    {
      return (Geometry) vertex.getObjectValue(GeoVertex.GEOPOLYGON, this.date);
    }
    else if (geometryType.equals(GeometryType.MULTIPOLYGON))
    {
      return (Geometry) vertex.getObjectValue(GeoVertex.GEOMULTIPOLYGON, this.date);
    }

    throw new UnsupportedOperationException("Unsupported geometry type [" + geometryType.name() + "]");
  }

  private boolean exists(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType, Date startDate, Date endDate)
  {
    EdgeObject edge = this.getEdge(parent, hierarchyType, startDate, endDate);

    return ( edge != null );

  }

  private EdgeObject getEdge(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType, Date startDate, Date endDate)
  {
    String statement = "SELECT FROM " + hierarchyType.getMdEdge().getDBClassName();
    statement += " WHERE out = :parent";
    statement += " AND in = :child";

    if (startDate != null)
    {
      statement += " AND startDate = :startDate";
    }

    if (endDate != null)
    {
      statement += " AND endDate = :endDate";
    }

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement);
    query.setParameter("parent", ( (VertexServerGeoObject) parent ).getVertex().getRID());
    query.setParameter("child", this.getVertex().getRID());

    if (startDate != null)
    {
      query.setParameter("startDate", startDate);
    }

    if (endDate != null)
    {
      query.setParameter("endDate", endDate);
    }

    return query.getSingleResult();
  }

  public static VertexObject getVertex(ServerGeoObjectType type, String uuid)
  {
    String statement = "SELECT FROM " + type.getMdVertex().getDBClassName();
    statement += " WHERE uuid = :uuid";

    GraphQuery<GeoVertex> query = new GraphQuery<GeoVertex>(statement);
    query.setParameter("uuid", uuid);

    return query.getSingleResult();
  }

  public static VertexObject getVertexByCode(ServerGeoObjectType type, String code)
  {
    String statement = "SELECT FROM " + type.getMdVertex().getDBClassName();
    statement += " WHERE code = :code";

    GraphQuery<GeoVertex> query = new GraphQuery<GeoVertex>(statement);
    query.setParameter("code", code);

    return query.getSingleResult();
  }

  public static VertexObject newInstance(ServerGeoObjectType type)
  {
    VertexObjectDAO dao = VertexObjectDAO.newInstance(type.getMdVertex());

    return VertexObject.instantiate(dao);
  }

  private static ServerChildTreeNode internalGetChildGeoObjects(VertexServerGeoObject parent, String[] childrenTypes, Boolean recursive, ServerHierarchyType htIn, Date date)
  {
    ServerChildTreeNode tnRoot = new ServerChildTreeNode(parent, htIn, date);

    Map<String, Object> parameters = new HashedMap<String, Object>();
    parameters.put("rid", parent.getVertex().getRID());

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT OUT(");

    if (htIn != null)
    {
      statement.append("'" + htIn.getMdEdge().getDBClassName() + "'");
    }
    statement.append(")");

    if (childrenTypes.length > 0)
    {
      statement.append("[");

      for (int i = 0; i < childrenTypes.length; i++)
      {
        ServerGeoObjectType type = ServerGeoObjectType.get(childrenTypes[i]);

        if (i > 0)
        {
          statement.append(" OR ");
        }

        statement.append("@class = :" + i);

        parameters.put(Integer.toString(i), type.getMdVertex().getDBClassName());
      }

      statement.append("]");
    }

    statement.append(" FROM :rid");

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement.toString(), parameters);

    List<EdgeObject> edges = query.getResults();

    for (EdgeObject edge : edges)
    {
      MdEdgeDAOIF mdEdge = (MdEdgeDAOIF) edge.getMdClass();
      VertexObject childVertex = edge.getChild();

      MdVertexDAOIF mdVertex = (MdVertexDAOIF) childVertex.getMdClass();

      ServerHierarchyType ht = ServerHierarchyType.get(mdEdge);
      ServerGeoObjectType childType = ServerGeoObjectType.get(mdVertex);

      VertexServerGeoObject child = new VertexServerGeoObject(childType, childVertex, date);

      ServerChildTreeNode tnChild;

      if (recursive)
      {
        tnChild = internalGetChildGeoObjects(child, childrenTypes, recursive, ht, date);
      }
      else
      {
        tnChild = new ServerChildTreeNode(child, ht, date);
      }

      tnRoot.addChild(tnChild);
    }

    return tnRoot;
  }

  protected static ServerParentTreeNode internalGetParentGeoObjects(VertexServerGeoObject child, String[] parentTypes, boolean recursive, ServerHierarchyType htIn, Date date)
  {
    ServerParentTreeNode tnRoot = new ServerParentTreeNode(child, htIn, date);

    Map<String, Object> parameters = new HashedMap<String, Object>();
    parameters.put("rid", child.getVertex().getRID());

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT IN(");

    if (htIn != null)
    {
      statement.append("'" + htIn.getMdEdge().getDBClassName() + "'");
    }
    statement.append(")");

    if (parentTypes.length > 0)
    {
      statement.append("[");

      for (int i = 0; i < parentTypes.length; i++)
      {
        ServerGeoObjectType type = ServerGeoObjectType.get(parentTypes[i]);

        if (i > 0)
        {
          statement.append(" OR ");
        }

        statement.append("@class = :" + i);

        parameters.put(Integer.toString(i), type.getMdVertex().getDBClassName());
      }

      statement.append("]");
    }

    statement.append(" FROM :rid");

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement.toString(), parameters);

    List<EdgeObject> edges = query.getResults();

    for (EdgeObject edge : edges)
    {
      MdEdgeDAOIF mdEdge = (MdEdgeDAOIF) edge.getMdClass();
      VertexObject parentVertex = edge.getParent();

      MdVertexDAOIF mdVertex = (MdVertexDAOIF) parentVertex.getMdClass();

      ServerHierarchyType ht = ServerHierarchyType.get(mdEdge);
      ServerGeoObjectType parentType = ServerGeoObjectType.get(mdVertex);

      VertexServerGeoObject parent = new VertexServerGeoObject(parentType, parentVertex, date);

      ServerParentTreeNode tnParent;

      if (recursive)
      {
        tnParent = internalGetParentGeoObjects(parent, parentTypes, recursive, ht, date);
      }
      else
      {
        tnParent = new ServerParentTreeNode(parent, ht, date);
      }

      tnRoot.addParent(tnParent);
    }

    return tnRoot;
  }
}
