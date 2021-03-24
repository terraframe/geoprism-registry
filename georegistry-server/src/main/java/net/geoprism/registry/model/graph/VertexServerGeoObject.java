/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.model.graph;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.DefaultTerms;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.Attribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.UnknownTermException;
import org.commongeoregistry.adapter.dataaccess.ValueOverTimeCollectionDTO;
import org.commongeoregistry.adapter.dataaccess.ValueOverTimeDTO;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.json.JSONArray;
import org.json.JSONException;

import com.runwaysdk.Pair;
import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.constants.ElementInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.DuplicateDataException;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.graph.VertexObjectDAO;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTimeCollection;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.CreatePermissionException;
import com.runwaysdk.session.ReadPermissionException;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.WritePermissionException;
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
import net.geoprism.registry.DuplicateGeoObjectCodeException;
import net.geoprism.registry.DuplicateGeoObjectException;
import net.geoprism.registry.DuplicateGeoObjectMultipleException;
import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.GeometryTypeException;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.RequiredAttributeException;
import net.geoprism.registry.StatusValueException;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.graph.GeoVertexSynonym;
import net.geoprism.registry.io.TermValueException;
import net.geoprism.registry.model.AbstractServerGeoObject;
import net.geoprism.registry.model.GeoObjectMetadata;
import net.geoprism.registry.model.GeoObjectTypeMetadata;
import net.geoprism.registry.model.LocationInfo;
import net.geoprism.registry.model.ServerChildTreeNode;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.roles.CreateGeoObjectPermissionException;
import net.geoprism.registry.roles.ReadGeoObjectPermissionException;
import net.geoprism.registry.roles.WriteGeoObjectPermissionException;
import net.geoprism.registry.service.ConversionService;
import net.geoprism.registry.service.RegistryIdService;
import net.geoprism.registry.service.SearchService;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

public class VertexServerGeoObject extends AbstractServerGeoObject implements ServerGeoObjectIF, LocationInfo, VertexComponent
{
  private static class EdgeComparator implements Comparator<EdgeObject>
  {
    @Override
    public int compare(EdgeObject o1, EdgeObject o2)
    {
      Date d1 = o1.getObjectValue(GeoVertex.START_DATE);
      Date d2 = o2.getObjectValue(GeoVertex.START_DATE);

      return d1.compareTo(d2);
    }
  }

  private ServerGeoObjectType type;

  private VertexObject        vertex;

  private Date                date;

  public VertexServerGeoObject(ServerGeoObjectType type, VertexObject vertex)
  {
    this(type, vertex, null);
  }

  public VertexServerGeoObject(ServerGeoObjectType type, VertexObject vertex, Date date)
  {
    MdVertexDAOIF actualVertexType = (MdVertexDAOIF) vertex.getMdClass();
    ServerGeoObjectType actualType = ServerGeoObjectType.get(actualVertexType);

    this.type = actualType;
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
  public Date getCreateDate()
  {
    return this.vertex.getObjectValue(GeoVertex.CREATEDATE);
  }

  @Override
  public Date getLastUpdateDate()
  {
    return this.vertex.getObjectValue(GeoVertex.LASTUPDATEDATE);
  }

  @Override
  public void setDate(Date date)
  {
    this.date = date;
  }

  public Date getDate()
  {
    return date;
  }

  @Override
  public void setCode(String code)
  {
    code = code.trim();
    code = code.replaceAll("\\s+","");
    
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

    this.getVertex().setValue(geometryAttribute, geometry, this.date, this.date);
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
    if (status != null)
    {
      this.vertex.setValue(DefaultAttribute.STATUS.getName(), status.getOid(), this.date, this.date);
    }
    else
    {
      this.vertex.setValue(DefaultAttribute.STATUS.getName(), null, this.date, this.date);
    }
  }

  @Override
  public void setStatus(GeoObjectStatus status, Date startDate, Date endDate)
  {
    if (status != null)
    {
      this.vertex.setValue(DefaultAttribute.STATUS.getName(), status.getOid(), startDate, endDate);
    }
    else
    {
      this.vertex.setValue(DefaultAttribute.STATUS.getName(), null, startDate, endDate);
    }
  }

  @Override
  public GeoObjectStatus getStatus()
  {
    Set<String> value = this.vertex.getObjectValue(DefaultAttribute.STATUS.getName(), this.date);

    if (value != null && value.size() > 0)
    {
      return GeoObjectStatus.get(value.iterator().next());
    }

//    return GeoObjectStatus.INACTIVE;
    return null; // Null status is assumed to be "does not exist", which is different than inactive.
  }

  public GeoObjectStatus getStatus(Date date)
  {
    Set<String> value = this.vertex.getObjectValue(DefaultAttribute.STATUS.getName(), date);

    if (value != null && value.size() > 0)
    {
      return GeoObjectStatus.get(value.iterator().next());
    }

    return GeoObjectStatus.INACTIVE;
  }

  @Override
  public void setUid(String uid)
  {
    this.vertex.setValue(RegistryConstants.UUID, uid, this.date, this.date);
  }

  @Override
  public void setDisplayLabel(LocalizedValue value)
  {
    LocalizedValueConverter.populate(this.vertex, DefaultAttribute.DISPLAY_LABEL.getName(), value, this.date, null);
  }

  @Override
  public void setDisplayLabel(LocalizedValue value, Date startDate, Date endDate)
  {
    if (this.isRedundantVOTAdd(DefaultAttribute.DISPLAY_LABEL.getName(), value, startDate, endDate, this.getValuesOverTime(DefaultAttribute.DISPLAY_LABEL.getName())))
    {
      return;
    }
    
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
      this.vertex.setValue(attributeName, value, this.date, this.date);
    }
  }

  @Override
  public void setValue(String attributeName, Object value, Date startDate, Date endDate)
  {
    if (this.isRedundantVOTAdd(attributeName, value, startDate, endDate, this.getValuesOverTime(attributeName)))
    {
      return;
    }
    
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
      if (attributeName.equals(DefaultAttribute.STATUS.getName()) || attributeName.equals(DefaultAttribute.DISPLAY_LABEL.getName()) || attributeName.equals(DefaultAttribute.CODE.getName()) || attributeName.equals(DefaultAttribute.UID.getName()) || attributeName.equals(GeoVertex.LASTUPDATEDATE) || attributeName.equals(GeoVertex.CREATEDATE))
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

            Term root = ( (AttributeTermType) attribute ).getRootTerm();
            String parent = TermConverter.buildClassifierKeyFromTermCode(root.getCode());

            String classifierKey = Classifier.buildKey(parent, code);
            Classifier classifier = Classifier.getByKey(classifierKey);

            this.vertex.setValue(attributeName, classifier.getOid(), this.date, this.date);
          }
          else
          {
            this.vertex.setValue(attributeName, (String) null, this.date, this.date);
          }
        }
        else
        {
          Object value = geoObject.getValue(attributeName);

          if (value != null)
          {
            this.vertex.setValue(attributeName, value, this.date, this.date);
          }
          else
          {
            this.vertex.setValue(attributeName, (String) null, this.date, this.date);
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

  @SuppressWarnings("unchecked")
  @Override
  public void populate(GeoObjectOverTime goTime)
  {
    Map<String, AttributeType> attributes = goTime.getType().getAttributeMap();
    attributes.forEach((attributeName, attribute) -> {
      if (attributeName.equals(DefaultAttribute.CODE.getName()) || attributeName.equals(DefaultAttribute.UID.getName()) || attributeName.equals(GeoVertex.LASTUPDATEDATE) || attributeName.equals(GeoVertex.CREATEDATE))
      {
        // Ignore the attributes
      }
      else if (attributeName.equals(DefaultAttribute.STATUS.getName()))
      {
        this.getValuesOverTime(attributeName).clear();
        for (ValueOverTimeDTO votDTO : goTime.getAllValues(attributeName))
        {
          Iterator<String> it = (Iterator<String>) votDTO.getValue();

          if (it.hasNext())
          {
            String code = it.next();
            Term value = ( (AttributeTermType) attribute ).getTermByCode(code).get();
            GeoObjectStatus gos = ConversionService.getInstance().termToGeoObjectStatus(value);

            this.setStatus(gos, votDTO.getStartDate(), votDTO.getEndDate());
          }
        }
      }
      // else if (attributeName.equals(DefaultAttribute.GEOMETRY.getName()))
      // {
      // for (ValueOverTimeDTO votDTO : goTime.getAllValues(attributeName))
      // {
      // Geometry geom = goTime.getGeometry(votDTO.getStartDate());
      //
      // this.setGeometry(geom, votDTO.getStartDate(), votDTO.getEndDate());
      // }
      // }
      else if (attributeName.equals(DefaultAttribute.DISPLAY_LABEL.getName()))
      {
        this.getValuesOverTime(attributeName).clear();
        for (ValueOverTimeDTO votDTO : goTime.getAllValues(attributeName))
        {
          LocalizedValue label = (LocalizedValue) votDTO.getValue();

          this.setDisplayLabel(label, votDTO.getStartDate(), votDTO.getEndDate());
        }
      }
      else if (this.vertex.hasAttribute(attributeName) && !this.vertex.getMdAttributeDAO(attributeName).isSystem())
      {
        this.getValuesOverTime(attributeName).clear();
        ValueOverTimeCollectionDTO collection = goTime.getAllValues(attributeName);

        for (ValueOverTimeDTO votDTO : collection)
        {
          if (attribute instanceof AttributeTermType)
          {
            Iterator<String> it = (Iterator<String>) votDTO.getValue();

            if (it.hasNext())
            {
              String code = it.next();

              Term root = ( (AttributeTermType) attribute ).getRootTerm();
              String parent = TermConverter.buildClassifierKeyFromTermCode(root.getCode());

              String classifierKey = Classifier.buildKey(parent, code);
              Classifier classifier = Classifier.getByKey(classifierKey);

              this.vertex.setValue(attributeName, classifier.getOid(), votDTO.getStartDate(), votDTO.getEndDate());
            }
            else
            {
              this.vertex.setValue(attributeName, (String) null, votDTO.getStartDate(), votDTO.getEndDate());
            }
          }
          else
          {
            Object value = votDTO.getValue();

            if (value != null)
            {
              this.vertex.setValue(attributeName, value, votDTO.getStartDate(), votDTO.getEndDate());
            }
            else
            {
              this.vertex.setValue(attributeName, (String) null, votDTO.getStartDate(), votDTO.getEndDate());
            }
          }
        }
      }
    });

    this.getValuesOverTime(this.getGeometryAttributeName()).clear();
    for (ValueOverTimeDTO votDTO : goTime.getAllValues(DefaultAttribute.GEOMETRY.getName()))
    {
      Geometry geom = goTime.getGeometry(votDTO.getStartDate());

      this.setGeometry(geom, votDTO.getStartDate(), votDTO.getEndDate());
    }

    this.setUid(goTime.getUid());
    this.setCode(goTime.getCode());
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

  public List<VertexServerGeoObject> getAncestors(ServerHierarchyType hierarchy)
  {
    List<VertexServerGeoObject> list = new LinkedList<VertexServerGeoObject>();

    GraphQuery<VertexObject> query = buildAncestorQuery(hierarchy);

    List<VertexObject> results = query.getResults();

    results.forEach(result -> {
      list.add(new VertexServerGeoObject(type, result, this.date));
    });

    return list;
  }

  @Override
  public Map<String, LocationInfo> getAncestorMap(ServerHierarchyType hierarchy, boolean includeInheritedTypes)
  {
    TreeMap<String, LocationInfo> map = new TreeMap<String, LocationInfo>();

    GraphQuery<VertexObject> query = buildAncestorQuery(hierarchy);

    List<VertexObject> results = query.getResults();
    results.forEach(result -> {
      MdVertexDAOIF mdClass = (MdVertexDAOIF) result.getMdClass();
      ServerGeoObjectType vType = ServerGeoObjectType.get(mdClass);
      VertexServerGeoObject object = new VertexServerGeoObject(type, result, this.date);

      map.put(vType.getUniversal().getKey(), object);

      if (includeInheritedTypes && vType.isRoot(hierarchy))
      {
        ServerHierarchyType inheritedHierarchy = vType.getInheritedHierarchy(hierarchy);

        if (inheritedHierarchy != null)
        {
          map.putAll(object.getAncestorMap(inheritedHierarchy, true));
        }
      }
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

  public MdAttributeConcreteDAOIF getMdAttributeDAO(String name)
  {
    return this.vertex.getMdAttributeDAO(name);
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
    else if (attributeName.equals(DefaultAttribute.STATUS.getName()))
    {
      return this.getStatus();
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
  public Object getValue(String attributeName, Date date)
  {
    return this.vertex.getObjectValue(attributeName, date);
  }

  @Override
  public ValueOverTimeCollection getValuesOverTime(String attributeName)
  {
    return this.vertex.getValuesOverTime(attributeName);
  }

  @Override
  public void setValuesOverTime(String attributeName, ValueOverTimeCollection collection)
  {
    this.vertex.setValuesOverTime(attributeName, collection);
  }

  @Override
  public void lock()
  {
    // Do nothing
  }

  @Override
  public void unlock()
  {
    // Do nothing?
  }
  
//  private void validateCOTAttr(String attrName)
//  {
//    ValueOverTimeCollection votc = this.vertex.getValuesOverTime(attrName);
//    
//    if (votc == null || votc.size() == 0)
//    {
//      RequiredAttributeException ex = new RequiredAttributeException();
//      ex.setAttributeLabel(GeoObjectTypeMetadata.getAttributeDisplayLabel(attrName));
//      throw ex;
//    }
//    else if (votc != null && votc.size() > 0)
//    {
//      boolean hasValue = false;
//      
//      for (int i = 0; i < votc.size(); ++i)
//      {
//        ValueOverTime vot = votc.get(i);
//        
//        if (vot.getValue() != null)
//        {
//          if (vot.getValue() instanceof String && ((String)vot.getValue()).length() > 0)
//          {
//            hasValue = true;
//            break;
//          }
//          else if (vot.getValue() instanceof Collection)
//          {
//            Collection<?> val = (Collection<?>) vot.getValue();
//            
//            if (val.size() > 0)
//            {
//              hasValue = true;
//              break;
//            }
//          }
//          else
//          {
//            hasValue = true;
//            break;
//          }
//        }
//      }
//      
//      if (!hasValue)
//      {
//        RequiredAttributeException ex = new RequiredAttributeException();
//        ex.setAttributeLabel(GeoObjectTypeMetadata.getAttributeDisplayLabel(attrName));
//        throw ex;
//      }
//    }
//  }
  
  private void validate()
  {
//    this.validateCOTAttr(DefaultAttribute.STATUS.getName());
    
//    this.validateCOTAttr(DefaultAttribute.DISPLAY_LABEL.getName());
    
    String code = this.getCode();
    
    if (code == null || code.length() == 0)
    {
      RequiredAttributeException ex = new RequiredAttributeException();
      ex.setAttributeLabel(GeoObjectTypeMetadata.getAttributeDisplayLabel(DefaultAttribute.CODE.getName()));
      throw ex;
    }
  }

  @Override
  public void apply(boolean isImport)
  {
    if (!isImport && !this.vertex.isNew() && !this.getType().isGeometryEditable() && this.vertex.isModified(this.getGeometryAttributeName()))
    {
      throw new GeometryUpdateException();
    }

    if (this.vertex.isNew() || this.vertex.getObjectValue(GeoVertex.CREATEDATE) == null)
    {
      this.vertex.setValue(GeoVertex.CREATEDATE, new Date());
    }

    this.vertex.setValue(GeoVertex.LASTUPDATEDATE, new Date());
    
    this.validate();

    try
    {
      this.getVertex().apply();
    }
    catch (CreatePermissionException ex)
    {
      CreateGeoObjectPermissionException goex = new CreateGeoObjectPermissionException();
      goex.setGeoObjectType(this.getType().getLabel().getValue());
      goex.setOrganization(this.getType().getOrganization().getDisplayLabel().getValue());
      throw goex;
    }
    catch (WritePermissionException ex)
    {
      WriteGeoObjectPermissionException goex = new WriteGeoObjectPermissionException();
      goex.setGeoObjectType(this.getType().getLabel().getValue());
      goex.setOrganization(this.getType().getOrganization().getDisplayLabel().getValue());
      throw goex;
    }
    catch (ReadPermissionException ex)
    {
      ReadGeoObjectPermissionException goex = new ReadGeoObjectPermissionException();
      goex.setGeoObjectType(this.getType().getLabel().getValue());
      goex.setOrganization(this.getType().getOrganization().getDisplayLabel().getValue());
      throw goex;
    }

    new SearchService().insert(this);
  }

  @Override
  public String bbox(Date date)
  {
    Geometry geometry = (Geometry) this.getValue(getGeometryAttributeName(), date);

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
  public ServerParentTreeNode getParentsForHierarchy(ServerHierarchyType hierarchy, Boolean recursive)
  {
    return internalGetParentGeoObjects(this, null, recursive, hierarchy, this.date);
  }

  @Override
  public ServerParentTreeNodeOverTime getParentsOverTime(String[] parentTypes, Boolean recursive)
  {
    return internalGetParentOverTime(this, parentTypes, recursive);
  }

  @Override
  public void setParents(ServerParentTreeNodeOverTime parentsOverTime)
  {
    parentsOverTime.enforceUserHasPermissionSetParents(this.getType().getCode(), false);

    final Collection<ServerHierarchyType> hierarchyTypes = parentsOverTime.getHierarchies();

    for (ServerHierarchyType hierarchyType : hierarchyTypes)
    {
      final List<ServerParentTreeNode> entries = parentsOverTime.getEntries(hierarchyType);

      this.removeAllEdges(hierarchyType);

      final TreeSet<EdgeObject> edges = new TreeSet<EdgeObject>(new EdgeComparator());

      for (ServerParentTreeNode entry : entries)
      {
        final ServerGeoObjectIF parent = entry.getGeoObject();

        EdgeObject newEdge = this.getVertex().addParent( ( (VertexComponent) parent ).getVertex(), hierarchyType.getMdEdge());
        newEdge.setValue(GeoVertex.START_DATE, entry.getStartDate());

        edges.add(newEdge);
      }

      // // An entry already exists at the given start time
      this.calculateEndDates(edges);

      for (EdgeObject e : edges)
      {
        e.apply();
      }
    }
  }

  @Override
  public ServerParentTreeNode addParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType)
  {
    if (!hierarchyType.getUniversalType().equals(AllowedIn.CLASS))
    {
      hierarchyType.validateUniversalRelationship(this.getType(), parent.getType());
    }

    if (this.getVertex().isNew() || !this.exists(parent, hierarchyType, null, null))
    {
      EdgeObject edge = this.getVertex().addParent( ( (VertexComponent) parent ).getVertex(), hierarchyType.getMdEdge());
      edge.apply();
    }

    ServerParentTreeNode node = new ServerParentTreeNode(this, hierarchyType, this.date, null);
    node.addParent(new ServerParentTreeNode(parent, hierarchyType, this.date, null));

    return node;
  }

  @Override
  public ServerParentTreeNode addParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType, Date startDate, Date endDate)
  {
    if (!hierarchyType.getUniversalType().equals(AllowedIn.CLASS))
    {
      hierarchyType.validateUniversalRelationship(this.getType(), parent.getType());
    }

    SortedSet<EdgeObject> edges;

    if (this.getVertex().isNew())
    {
      edges = new TreeSet<EdgeObject>(new EdgeComparator());
    }
    else
    {
      edges = this.getEdges(hierarchyType);
    }

    EdgeObject newEdge = this.getVertex().addParent( ( (VertexComponent) parent ).getVertex(), hierarchyType.getMdEdge());
    newEdge.setValue(GeoVertex.START_DATE, startDate);
    newEdge.setValue(GeoVertex.END_DATE, endDate);

//    edges.add(newEdge);

    // An entry already exists at the given start time
    // this.calculateEndDates(edges);
    boolean redundant = this.calculateStartDates(edges, parent, startDate, endDate);

    if (!redundant)
    {
      for (EdgeObject e : edges)
      {
        e.apply();
      }

      newEdge.apply();
    }
    
    ServerParentTreeNode node = new ServerParentTreeNode(this, hierarchyType, startDate, null);
    node.addParent(new ServerParentTreeNode(parent, hierarchyType, startDate, null));

    return node;
  }
  
  private boolean isRedundantVOTAdd(String attributeName, Object value, Date startDate, Date endDate, ValueOverTimeCollection existingVotc)
  {
    if (startDate != null && endDate != null)
    {
      Iterator<ValueOverTime> it = existingVotc.iterator();
      LocalDate iStartDate = startDate.toInstant().atZone(ZoneId.of("Z")).toLocalDate();
      LocalDate iEndDate = endDate.toInstant().atZone(ZoneId.of("Z")).toLocalDate();

      while (it.hasNext())
      {
        ValueOverTime vot = it.next();

        Date vSDate = vot.getStartDate();
        Date vEDate = vot.getEndDate();

        LocalDate vStartDate = vSDate.toInstant().atZone(ZoneId.of("Z")).toLocalDate();
        LocalDate vEndDate = vEDate.toInstant().atZone(ZoneId.of("Z")).toLocalDate();
        
        if (vStartDate.isBefore(iStartDate) && vEndDate.isAfter(iEndDate))
        {
          if (areValuesEqual(attributeName, vot.getValue(), value))
          {
            return true;
          }
        }
      }
    }
    
    return false;
  
  }
  
  @SuppressWarnings("unchecked")
  private boolean areValuesEqual(String attributeName, Object val1, Object val2)
  {
    if (val1 == null && val2 == null)
    {
      return true;
    }
    else if ( val1 == null || val2 == null )
    {
      return false;
    }

    if (attributeName.equals(DefaultAttribute.STATUS.getName()))
    {
      List<Object> val1s = toList(((Set<String>) val1).iterator());
      List<Object> val2s = toList(((Set<String>) val2).iterator());

      if (val1s.size() != val2s.size())
      {
         return false;
      }
      
      for (int i = 0; i < val1s.size(); ++i)
      {
        if (!areValuesEqual(attributeName, val1s.get(i), val2s.get(i)))
        {
          return false;
        }
      }
      
      return true;
    }
    else if (attributeName.equals(DefaultAttribute.GEOMETRY.getName()))
    {
      return false;
    }
    else if (val1 instanceof Iterator<?> && val2 instanceof Iterator<?>)
    {
      ArrayList<Object> val1s = toList((Iterator<?>) val1);
      ArrayList<Object> val2s = toList((Iterator<?>) val2);
      
      if (val1s.size() != val2s.size())
      {
         return false;
      }
      
      for (int i = 0; i < val1s.size(); ++i)
      {
        if (!areValuesEqual(attributeName, val1s.get(i), val2s.get(i)))
        {
          return false;
        }
      }
      
      return true;
    }
    else if (val1 instanceof VertexObjectDAO && ((VertexObjectDAO)val1).getType().equals("com.runwaysdk.graph.EmbeddedLocalValue") && val2 instanceof LocalizedValue)
    {
      return areValuesEqual(attributeName, LocalizedValueConverter.convert((VertexObjectDAO)val1), val2);
    }
    else
    {
      return val1.equals(val2);
    }
  }
  
  private ArrayList<Object> toList(Iterator<?> iter)
  {
    ArrayList<Object> al = new ArrayList<Object>();
    
    while (iter.hasNext())
    {
      Object next = iter.next();
      
      al.add(next);
    }
    
    return al;
  }

  private boolean calculateStartDates(SortedSet<EdgeObject> edges, ServerGeoObjectIF parent, Date startDate, Date endDate)
  {
    if (startDate != null && endDate != null)
    {
      Iterator<EdgeObject> it = edges.iterator();
      LocalDate iStartDate = startDate.toInstant().atZone(ZoneId.of("Z")).toLocalDate();
      LocalDate iEndDate = endDate.toInstant().atZone(ZoneId.of("Z")).toLocalDate();

      List<EdgeObject> segments = new LinkedList<EdgeObject>();

      while (it.hasNext())
      {
        EdgeObject vot = it.next();

        Date vSDate = vot.getObjectValue(GeoVertex.START_DATE);
        Date vEDate = vot.getObjectValue(GeoVertex.END_DATE);

        LocalDate vStartDate = vSDate.toInstant().atZone(ZoneId.of("Z")).toLocalDate();
        LocalDate vEndDate = vEDate.toInstant().atZone(ZoneId.of("Z")).toLocalDate();

        // Remove the entry completely if its a subset of the startDate and
        // endDate range
        if (isAfterOrEqual(vStartDate, iStartDate) && isBeforeOrEqual(vEndDate, iEndDate))
        {
          it.remove();

          vot.delete();
        }
        // If the entry is extends after the end date then move the start
        // date of the entry to the end of the clear range
        else if (isAfterOrEqual(vStartDate, iStartDate) && vEndDate.isAfter(iEndDate) && isAfterOrEqual(iEndDate, vStartDate))
        {
          vot.setValue(GeoVertex.START_DATE, this.calculateDatePlusOneDay(endDate));
        }
        // If the entry is extends before the start date then move the end
        // date of the entry to the start of the clear range
        else if (vStartDate.isBefore(iStartDate) && isBeforeOrEqual(vEndDate, iEndDate) && isBeforeOrEqual(iStartDate, vEndDate))
        {
          vot.setValue(GeoVertex.END_DATE, this.calculateDateMinusOneDay(startDate));
        }
        else if (vStartDate.isBefore(iStartDate) && vEndDate.isAfter(iEndDate))
        {
          boolean parentEq = (parent != null && vot.getParent() != null && parent.getCode().equals(vot.getParent().getObjectValue(DefaultAttribute.CODE.getName()))
              || (parent == null && vot.getParent() == null) 
              );
          boolean childEq = (vot.getChild() != null && this.getCode().equals(vot.getChild().getObjectValue(DefaultAttribute.CODE.getName())));
          
          if (parentEq && childEq)
          {
            return true;
          }
          else
          {
            it.remove();
  
            vot.delete();
  
            EdgeObject startSegment = vot.getChild().addParent(vot.getParent(), (MdEdgeDAOIF) vot.getMdClass());
            startSegment.setValue(GeoVertex.START_DATE, vSDate);
            startSegment.setValue(GeoVertex.END_DATE, this.calculateDateMinusOneDay(startDate));
  
            EdgeObject endSegment = vot.getChild().addParent(vot.getParent(), (MdEdgeDAOIF) vot.getMdClass());
            endSegment.setValue(GeoVertex.START_DATE, this.calculateDatePlusOneDay(endDate));
            endSegment.setValue(GeoVertex.END_DATE, vEDate);
  
            segments.add(startSegment);
            segments.add(endSegment);
          }
        }
      }

      for (EdgeObject segment : segments)
      {
        edges.add(segment);
      }
    }
    
    return false;
  }

  protected Date calculateDateMinusOneDay(Date source)
  {
    LocalDate localEnd = source.toInstant().atZone(ZoneId.of("Z")).toLocalDate().minusDays(1);
    Instant instant = localEnd.atStartOfDay().atZone(ZoneId.of("Z")).toInstant();

    return Date.from(instant);
  }

  protected Date calculateDatePlusOneDay(Date source)
  {
    LocalDate localEnd = source.toInstant().atZone(ZoneId.of("Z")).toLocalDate().plusDays(1);
    Instant instant = localEnd.atStartOfDay().atZone(ZoneId.of("Z")).toInstant();

    return Date.from(instant);
  }

  private boolean isAfterOrEqual(LocalDate date1, LocalDate date2)
  {
    return date1.isAfter(date2) || date2.isEqual(date1);
  }

  private boolean isBeforeOrEqual(LocalDate date1, LocalDate date2)
  {
    return date1.isBefore(date2) || date2.isEqual(date1);
  }

  @Override
  public void removeParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType)
  {
    this.getVertex().removeParent( ( (VertexComponent) parent ).getVertex(), hierarchyType.getMdEdge());
  }

  @Override
  public GeoObject toGeoObject()
  {
    Map<String, Attribute> attributeMap = GeoObject.buildAttributeMap(type.getType());

    GeoObject geoObj = new GeoObject(type.getType(), type.getGeometryType(), attributeMap);

    Map<String, AttributeType> attributes = type.getAttributeMap();
    attributes.forEach((attributeName, attribute) -> {
      if (attributeName.equals(DefaultAttribute.STATUS.getName()))
      {
        Term statusTerm = ServiceFactory.getConversionService().geoObjectStatusToTerm(this.getStatus());

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

    geoObj.setUid(vertex.getObjectValue(RegistryConstants.UUID));
    geoObj.setCode(vertex.getObjectValue(DefaultAttribute.CODE.getName()));
    geoObj.setGeometry(this.getGeometry());
    geoObj.setDisplayLabel(this.getDisplayLabel());

    if (vertex.isNew())// && !vertex.isAppliedToDB())
    {
      geoObj.setUid(RegistryIdService.getInstance().next());
      geoObj.setStatus(ServiceFactory.getMetadataCache().getTerm(DefaultTerms.GeoObjectStatusTerm.NEW.code).get());
    }

    return geoObj;
  }

  public GeoObjectOverTime toGeoObjectOverTime()
  {
    return this.toGeoObjectOverTime(true);
  }

  public GeoObjectOverTime toGeoObjectOverTime(boolean generateUid)
  {
    Map<String, ValueOverTimeCollectionDTO> votAttributeMap = GeoObjectOverTime.buildVotAttributeMap(type.getType());
    Map<String, Attribute> attributeMap = GeoObjectOverTime.buildAttributeMap(type.getType());

    GeoObjectOverTime geoObj = new GeoObjectOverTime(type.getType(), votAttributeMap, attributeMap);

    Map<String, AttributeType> attributes = type.getAttributeMap();
    attributes.forEach((attributeName, attribute) -> {
      if (attributeName.equals(DefaultAttribute.DISPLAY_LABEL.getName()))
      {
        ValueOverTimeCollection votc = this.getValuesOverTime(attributeName);

        for (ValueOverTime vot : votc)
        {
          Object value = this.getDisplayLabel(vot.getStartDate());

          geoObj.setValue(attributeName, value, vot.getStartDate(), vot.getEndDate());
        }
      }
      // else if (attributeName.equals(DefaultAttribute.GEOMETRY.getName()))
      // {
      // ValueOverTimeCollection votc =
      // this.getValuesOverTime(this.getGeometryAttributeName());
      //
      // for (ValueOverTime vot : votc)
      // {
      // Object value = vot.getValue();
      //
      // geoObj.setValue(attributeName, value, vot.getStartDate(),
      // vot.getEndDate());
      // }
      // }
      else if (vertex.hasAttribute(attributeName))
      {
        if (attribute.isChangeOverTime())
        {
          ValueOverTimeCollection votc = this.getValuesOverTime(attributeName);

          for (ValueOverTime vot : votc)
          {
            if (attributeName.equals(DefaultAttribute.STATUS.getName()))
            {
              Term statusTerm = ServiceFactory.getConversionService().geoObjectStatusToTerm(this.getStatus(vot.getStartDate()));

              geoObj.setStatus(statusTerm, vot.getStartDate(), vot.getEndDate());
            }
            else
            {
              Object value = vot.getValue();

              if (value != null)
              {
                if (attribute instanceof AttributeTermType)
                {
                  Classifier classifier = Classifier.get((String) value);

                  try
                  {
                    geoObj.setValue(attributeName, classifier.getClassifierId(), vot.getStartDate(), vot.getEndDate());
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
                  geoObj.setValue(attributeName, value, vot.getStartDate(), vot.getEndDate());
                }
              }
            }
          }
        }
        else
        {
          Object value = this.getValue(attributeName);

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
      }
    });

    if (generateUid && vertex.isNew())// && !vertex.isAppliedToDB())
    {
      geoObj.setUid(RegistryIdService.getInstance().next());

      // geoObj.setStatus(ServiceFactory.getAdapter().getMetadataCache().getTerm(DefaultTerms.GeoObjectStatusTerm.NEW.code).get(),
      // this.date, this.date);
    }
    else
    {
      geoObj.setUid(vertex.getObjectValue(RegistryConstants.UUID));
    }

    ValueOverTimeCollection votc = this.getValuesOverTime(this.getGeometryAttributeName());
    for (ValueOverTime vot : votc)
    {
      Object value = vot.getValue();

      geoObj.setValue(DefaultAttribute.GEOMETRY.getName(), value, vot.getStartDate(), vot.getEndDate());
    }

    geoObj.setCode(vertex.getObjectValue(DefaultAttribute.CODE.getName()));

    return geoObj;
  }

  public LocalizedValue getDisplayLabel()
  {
    GraphObject graphObject = vertex.getEmbeddedComponent(DefaultAttribute.DISPLAY_LABEL.getName(), this.date);

    if (graphObject == null)
    {
      return new LocalizedValue(null, new HashMap<String, String>());
    }

    return LocalizedValueConverter.convert(graphObject);
  }

  public LocalizedValue getDisplayLabel(Date date)
  {
    GraphObject graphObject = vertex.getEmbeddedComponent(DefaultAttribute.DISPLAY_LABEL.getName(), date);

    if (graphObject == null)
    {
      return null;
    }

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
    query.setParameter("parent", ( (VertexComponent) parent ).getVertex().getRID());
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

  private SortedSet<EdgeObject> getEdges(ServerHierarchyType hierarchyType)
  {
    TreeSet<EdgeObject> set = new TreeSet<EdgeObject>(new EdgeComparator());

    String statement = "SELECT expand(inE('" + hierarchyType.getMdEdge().getDBClassName() + "'))";
    statement += " FROM :child";

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement);
    query.setParameter("child", this.getVertex().getRID());

    set.addAll(query.getResults());

    return set;
  }

  private void removeAllEdges(ServerHierarchyType hierarchyType)
  {
    // Delete the current edges and recreate the new ones
    final SortedSet<EdgeObject> edges = this.getEdges(hierarchyType);

    for (EdgeObject edge : edges)
    {
      edge.delete();
    }
  }

  private void calculateEndDates(SortedSet<EdgeObject> edges)
  {
    EdgeObject prev = null;

    for (EdgeObject current : edges)
    {
      if (prev != null)
      {
        Calendar cal = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
        cal.setTime(current.getObjectValue(GeoVertex.START_DATE));
        cal.add(Calendar.DAY_OF_YEAR, -1);

        prev.setValue(GeoVertex.END_DATE, cal.getTime());
      }

      prev = current;
    }

    edges.last().setValue(GeoVertex.END_DATE, ValueOverTime.INFINITY_END_DATE);
  }

  public String addSynonym(String label)
  {
    GeoVertexSynonym synonym = new GeoVertexSynonym();
    synonym.setValue(GeoVertexSynonym.LABEL, label);
    synonym.apply();

    this.vertex.addChild(synonym, GeoVertex.HAS_SYNONYM).apply();

    return synonym.getOid();
  }

  private EdgeObject getExternalIdEdge(ExternalSystem system)
  {
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(GeoVertex.EXTERNAL_ID);

    String statement = "SELECT expand(inE('" + mdEdge.getDBClassName() + "')[out = :parent])";
    statement += " FROM :child";

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement);
    query.setParameter("parent", system.getRID());
    query.setParameter("child", this.getVertex().getRID());

    return query.getSingleResult();
  }

  @Override
  public void createExternalId(ExternalSystem system, String id, ImportStrategy importStrategy)
  {
    if (importStrategy.equals(ImportStrategy.NEW_ONLY))
    {
      EdgeObject edge = this.getVertex().addParent(system, GeoVertex.EXTERNAL_ID);
      edge.setValue("id", id);
      edge.apply();
    }
    else
    {
      EdgeObject edge = this.getExternalIdEdge(system);

      if (edge == null)
      {
        edge = this.getVertex().addParent(system, GeoVertex.EXTERNAL_ID);
      }

      edge.setValue("id", id);
      edge.apply();
    }
  }

  @Override
  public String getExternalId(ExternalSystem system)
  {
    EdgeObject edge = this.getExternalIdEdge(system);

    if (edge != null)
    {
      return edge.getObjectValue("id");
    }

    return null;
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
    ServerChildTreeNode tnRoot = new ServerChildTreeNode(parent, htIn, date, null);

    Map<String, Object> parameters = new HashedMap<String, Object>();
    parameters.put("rid", parent.getVertex().getRID());

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND(outE(");

    if (htIn != null)
    {
      statement.append("'" + htIn.getMdEdge().getDBClassName() + "'");
    }
    statement.append(")");

    if (childrenTypes != null && childrenTypes.length > 0)
    {
      statement.append("[");

      for (int i = 0; i < childrenTypes.length; i++)
      {
        ServerGeoObjectType type = ServerGeoObjectType.get(childrenTypes[i]);
        final String paramName = "p" + Integer.toString(i);

        if (i > 0)
        {
          statement.append(" OR ");
        }

        statement.append("in.@class = :" + paramName);

        parameters.put(paramName, type.getMdVertex().getDBClassName());
      }

      statement.append("]");
    }

    statement.append(") FROM :rid");

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement.toString(), parameters);

    List<EdgeObject> edges = query.getResults();

    for (EdgeObject edge : edges)
    {
      MdEdgeDAOIF mdEdge = (MdEdgeDAOIF) edge.getMdClass();

      if (!mdEdge.definesType().equals(GeoVertex.EXTERNAL_ID))
      {
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
          tnChild = new ServerChildTreeNode(child, ht, date, null);
        }

        tnRoot.addChild(tnChild);
      }
    }

    return tnRoot;
  }

  protected static ServerParentTreeNode internalGetParentGeoObjects(VertexServerGeoObject child, String[] parentTypes, boolean recursive, ServerHierarchyType htIn, Date date)
  {
    ServerParentTreeNode tnRoot = new ServerParentTreeNode(child, htIn, date, null);

    Map<String, Object> parameters = new HashedMap<String, Object>();
    parameters.put("rid", child.getVertex().getRID());

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND( inE(");

    if (htIn != null)
    {
      statement.append("'" + htIn.getMdEdge().getDBClassName() + "'");
    }
    statement.append(")");

    if (date != null || ( parentTypes != null && parentTypes.length > 0 ))
    {
      statement.append("[");

      if (date != null)
      {
        statement.append(" :date BETWEEN startDate AND endDate");
        parameters.put("date", date);
      }

      if (parentTypes != null && parentTypes.length > 0)
      {
        if (date != null)
        {
          statement.append(" AND");
        }

        statement.append("(");
        for (int i = 0; i < parentTypes.length; i++)
        {
          ServerGeoObjectType type = ServerGeoObjectType.get(parentTypes[i]);
          final String paramName = "p" + Integer.toString(i);

          if (i > 0)
          {
            statement.append(" OR ");
          }

          statement.append("out.@class = :" + paramName);

          parameters.put(paramName, type.getMdVertex().getDBClassName());
        }
        statement.append(")");
      }

      statement.append("]");
    }

    statement.append(") FROM :rid");

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement.toString(), parameters);

    List<EdgeObject> edges = query.getResults();

    for (EdgeObject edge : edges)
    {
      MdEdgeDAOIF mdEdge = (MdEdgeDAOIF) edge.getMdClass();

      if (! ( mdEdge.definesType().equals(GeoVertex.EXTERNAL_ID) || mdEdge.definesType().startsWith(SearchService.PACKAGE) ))
      {
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
          tnParent = new ServerParentTreeNode(parent, ht, date, null);
        }

        tnRoot.addParent(tnParent);
      }
    }

    return tnRoot;
  }

  protected static ServerParentTreeNodeOverTime internalGetParentOverTime(VertexServerGeoObject child, String[] parentTypes, boolean recursive)
  {
    final ServerGeoObjectType cType = child.getType();
    final List<ServerHierarchyType> hierarchies = cType.getHierarchies();

    ServerParentTreeNodeOverTime response = new ServerParentTreeNodeOverTime(cType);

    for (ServerHierarchyType ht : hierarchies)
    {
      response.add(ht);
    }

    Map<String, Object> parameters = new HashedMap<String, Object>();
    parameters.put("rid", child.getVertex().getRID());

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND(inE()");

    if (parentTypes != null && parentTypes.length > 0)
    {
      statement.append("[");

      for (int i = 0; i < parentTypes.length; i++)
      {
        ServerGeoObjectType type = ServerGeoObjectType.get(parentTypes[i]);

        if (i > 0)
        {
          statement.append(" OR ");
        }

        statement.append("out.@class = :a" + i);

        parameters.put("a" + Integer.toString(i), type.getMdVertex().getDBClassName());
      }

      statement.append("]");
    }

    statement.append(") FROM :rid");
    statement.append(" ORDER BY startDate ASC");

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement.toString(), parameters);

    List<EdgeObject> edges = query.getResults();

    for (EdgeObject edge : edges)
    {
      MdEdgeDAOIF mdEdge = (MdEdgeDAOIF) edge.getMdClass();

      if (! ( mdEdge.definesType().equals(GeoVertex.EXTERNAL_ID) || mdEdge.definesType().startsWith(SearchService.PACKAGE) ))
      {
        ServerHierarchyType ht = ServerHierarchyType.get(mdEdge);

        VertexObject parentVertex = edge.getParent();

        MdVertexDAOIF mdVertex = (MdVertexDAOIF) parentVertex.getMdClass();

        ServerGeoObjectType parentType = ServerGeoObjectType.get(mdVertex);

        Date date = edge.getObjectValue(GeoVertex.START_DATE);
        Date endDate = edge.getObjectValue(GeoVertex.END_DATE);

        ServerParentTreeNode tnRoot = new ServerParentTreeNode(child, null, date, null);
        tnRoot.setEndDate(endDate);

        VertexServerGeoObject parent = new VertexServerGeoObject(parentType, parentVertex, date);

        ServerParentTreeNode tnParent;

        if (recursive)
        {
          tnParent = internalGetParentGeoObjects(parent, parentTypes, recursive, ht, date);
        }
        else
        {
          tnParent = new ServerParentTreeNode(parent, ht, date, null);
        }

        tnRoot.addParent(tnParent);

        response.add(ht, tnRoot);
      }
    }

    return response;
  }

  public static Pair<Date, Date> getDataRange(ServerGeoObjectType type)
  {
    final String dbClassName = type.getMdVertex().getDBClassName();

    final Date startDate = new GraphQuery<Date>("SELECT MIN(status_cot.startDate) FROM " + dbClassName).getSingleResult();
    final Date endDate = new GraphQuery<Date>("SELECT MAX(status_cot.startDate) FROM " + dbClassName).getSingleResult();
    Date current = new Date();

    if (startDate != null && endDate != null)
    {
      if (endDate.before(current))
      {
        return new Pair<Date, Date>(startDate, current);
      }

      return new Pair<Date, Date>(startDate, endDate);
    }

    return null;
  }

  public static boolean hasData(ServerHierarchyType hierarchyType, ServerGeoObjectType childType)
  {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*) FROM " + hierarchyType.getMdEdge().getDBClassName());
    statement.append(" WHERE in.@class = :class");
    statement.append(" OR out.@class = :class");

    GraphQuery<Long> query = new GraphQuery<Long>(statement.toString());
    query.setParameter("class", childType.getMdVertex().getDBClassName());

    Long result = query.getSingleResult();

    return ( result != null && result > 0 );
  }

  public static void removeAllEdges(ServerHierarchyType hierarchyType, ServerGeoObjectType childType)
  {
    StringBuilder statement = new StringBuilder();
    statement.append("DELETE EDGE " + hierarchyType.getMdEdge().getDBClassName());
    statement.append(" WHERE in.@class = :class");
    statement.append(" OR out.@class = :class");

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("class", childType.getMdVertex().getDBClassName());

    GraphDBService service = GraphDBService.getInstance();
    service.command(service.getGraphDBRequest(), statement.toString(), parameters);
  }

  public static void handleDuplicateDataException(ServerGeoObjectType type, DuplicateDataException e)
  {
    if (e.getAttributes().size() == 0)
    {
      throw e;
    }
    else if (e.getAttributes().size() == 1)
    {
      MdAttributeDAOIF attr = e.getAttributes().get(0);

      if (isCodeAttribute(attr))
      {
        DuplicateGeoObjectCodeException ex = new DuplicateGeoObjectCodeException();
        ex.setGeoObjectType(type.getLabel().getValue());
        ex.setValue(e.getValues().get(0));
        throw ex;
      }
      else
      {
        DuplicateGeoObjectException ex = new DuplicateGeoObjectException();
        ex.setGeoObjectType(type.getLabel().getValue());
        ex.setValue(e.getValues().get(0));
        ex.setAttributeName(getAttributeLabel(type, attr));
        throw ex;
      }
    }
    else
    {
      List<String> attrLabels = new ArrayList<String>();

      for (MdAttributeDAOIF attr : e.getAttributes())
      {
        attrLabels.add(getAttributeLabel(type, attr));
      }

      DuplicateGeoObjectMultipleException ex = new DuplicateGeoObjectMultipleException();
      ex.setAttributeLabels(StringUtils.join(attrLabels, ", "));
      throw ex;
    }
  }

  public static boolean isCodeAttribute(MdAttributeDAOIF attr)
  {
    String attributeName = attr.definesAttribute();

    return attributeName.equals(DefaultAttribute.CODE.getName()) || attributeName.equals(ElementInfo.KEY) || attributeName.equals(ElementInfo.OID) || attributeName.equals(GeoEntity.GEOID);
  }

  public static String getAttributeLabel(ServerGeoObjectType type, MdAttributeDAOIF attr)
  {
    if (isCodeAttribute(attr))
    {
      return type.getAttribute(DefaultAttribute.CODE.getName()).get().getLabel().getValue();
    }

    if (type.getAttribute(attr.definesAttribute()).isPresent())
    {
      return type.getAttribute(attr.definesAttribute()).get().getLabel().getValue();
    }

    return attr.getDisplayLabel(Session.getCurrentLocale());
  }
  
  @Override
  public String toString()
  {
    return GeoObjectMetadata.get().getClassDisplayLabel() + " : " + this.getCode();
  }

}
