package net.geoprism.registry.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTimeCollection;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

/**
 * A wrapper class around a relational server geo object and a vertex server geo
 * object. Used to populate both the relational object and the vertex object
 * when importing data from the shapefile importer
 * 
 * @author terraframe
 */
public class CompositeServerGeoObject extends AbstractServerGeoObject implements ServerGeoObjectIF
{
  private ServerGeoObjectIF     business;

  private VertexServerGeoObject vertex;

  public CompositeServerGeoObject(ServerGeoObjectIF business, VertexServerGeoObject vertex)
  {
    super();
    this.business = business;
    this.vertex = vertex;
  }

  public ServerGeoObjectIF getBusiness()
  {
    return business;
  }

  public void setBusiness(ServerGeoObjectIF business)
  {
    this.business = business;
  }

  public VertexServerGeoObject getVertex()
  {
    return vertex;
  }

  public void setVertex(VertexServerGeoObject vertex)
  {
    this.vertex = vertex;
  }

  @Override
  public GeoObject toGeoObject()
  {
    return this.business.toGeoObject();
  }

  @Override
  public void setCode(String code)
  {
    this.business.setCode(code);
    this.vertex.setCode(code);
  }

  @Override
  public String getCode()
  {
    return this.business.getCode();
  }

  @Override
  public void setUid(String uid)
  {
    this.business.setUid(uid);
    this.vertex.setUid(uid);
  }

  @Override
  public String getUid()
  {
    return this.business.getUid();
  }

  @Override
  public void setGeometry(Geometry geometry)
  {
    this.business.setGeometry(geometry);
    this.vertex.setGeometry(geometry);
  }

  @Override
  public void setGeometry(Geometry geometry, Date startDate, Date endDate)
  {
    this.business.setGeometry(geometry, startDate, endDate);
    this.vertex.setGeometry(geometry, startDate, endDate);
  }

  @Override
  public Geometry getGeometry()
  {
    return this.business.getGeometry();
  }

  @Override
  public void setStatus(GeoObjectStatus status)
  {
    this.business.setStatus(status);
    this.vertex.setStatus(status);
  }

  @Override
  public void setStatus(GeoObjectStatus status, Date startDate, Date endDate)
  {
    this.business.setStatus(status, startDate, endDate);
    this.vertex.setStatus(status, startDate, endDate);
  }

  @Override
  public GeoObjectStatus getStatus()
  {
    return this.business.getStatus();
  }

  @Override
  public void setDisplayLabel(LocalizedValue label)
  {
    this.business.setDisplayLabel(label);
    this.vertex.setDisplayLabel(label);
  }

  @Override
  public void setDisplayLabel(LocalizedValue value, Date startDate, Date endDate)
  {
    this.business.setDisplayLabel(value, startDate, endDate);
    this.vertex.setDisplayLabel(value, startDate, endDate);
  }

  @Override
  public LocalizedValue getDisplayLabel()
  {
    return this.business.getDisplayLabel();
  }

  @Override
  public String getRunwayId()
  {
    return this.business.getRunwayId();
  }

  @Override
  public ServerGeoObjectType getType()
  {
    return this.business.getType();
  }

  @Override
  public List<? extends MdAttributeConcreteDAOIF> getMdAttributeDAOs()
  {
    return this.business.getMdAttributeDAOs();
  }

  @Override
  public Object getValue(String attributeName)
  {
    return this.business.getValue(attributeName);
  }

  @Override
  public Object getValue(String attributeName, Date date)
  {
    return this.vertex.getValue(attributeName, date);
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
  public void setValue(String attributeName, Object value)
  {
    this.business.setValue(attributeName, value);
    this.vertex.setValue(attributeName, value);
  }

  @Override
  public void setValue(String attributeName, Object value, Date startDate, Date endDate)
  {
    this.business.setValue(attributeName, value, startDate, endDate);
    this.vertex.setValue(attributeName, value, startDate, endDate);
  }

  @Override
  public String bbox()
  {
    return this.business.bbox();
  }

  @Override
  public Map<String, ServerHierarchyType> getHierarchyTypeMap(String[] relationshipTypes)
  {
    return this.business.getHierarchyTypeMap(relationshipTypes);
  }

  @Override
  public Map<String, LocationInfo> getAncestorMap(ServerHierarchyType hierarchy)
  {
    return this.business.getAncestorMap(hierarchy);
  }

  @Override
  public ServerChildTreeNode getChildGeoObjects(String[] childrenTypes, Boolean recursive)
  {
    return this.business.getChildGeoObjects(childrenTypes, recursive);
  }

  @Override
  public ServerParentTreeNode getParentGeoObjects(String[] parentTypes, Boolean recursive)
  {
    return this.business.getParentGeoObjects(parentTypes, recursive);
  }

  @Override
  public ServerParentTreeNodeOverTime getParentsOverTime(String[] parentTypes, Boolean recursive)
  {
    return this.vertex.getParentsOverTime(parentTypes, recursive);
  }

  @Override
  public ServerParentTreeNode addChild(ServerGeoObjectIF child, ServerHierarchyType hierarchy)
  {
    CompositeServerGeoObject cChild = (CompositeServerGeoObject) child;

    ServerParentTreeNode ptn = this.business.addChild(cChild.business, hierarchy);
    this.vertex.addChild(cChild.vertex, hierarchy);

    return ptn;
  }

  @Override
  public ServerParentTreeNode addChild(ServerGeoObjectIF child, ServerHierarchyType hierarchy, Date startDate, Date endDate)
  {
    CompositeServerGeoObject cChild = (CompositeServerGeoObject) child;

    ServerParentTreeNode ptn = this.business.addChild(cChild.business, hierarchy, startDate, endDate);
    this.vertex.addChild(cChild.vertex, hierarchy, startDate, endDate);

    return ptn;
  }

  @Override
  public void removeChild(ServerGeoObjectIF child, String hierarchyCode)
  {
    CompositeServerGeoObject cChild = (CompositeServerGeoObject) child;

    this.business.removeChild(cChild.business, hierarchyCode);
    this.vertex.removeChild(cChild.vertex, hierarchyCode);
  }

  @Override
  public void removeParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType)
  {
    CompositeServerGeoObject cParent = (CompositeServerGeoObject) parent;

    this.business.removeParent(cParent.business, hierarchyType);
    this.vertex.removeParent(cParent.vertex, hierarchyType);
  }

  @Override
  public ServerParentTreeNode addParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType)
  {
    CompositeServerGeoObject cParent = (CompositeServerGeoObject) parent;

    ServerParentTreeNode ptn = this.business.addParent(cParent.business, hierarchyType);
    this.vertex.addParent(cParent.vertex, hierarchyType);

    return ptn;
  }

  @Override
  public ServerParentTreeNode addParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType, Date startDate, Date endDate)
  {
    CompositeServerGeoObject cParent = (CompositeServerGeoObject) parent;

    ServerParentTreeNode ptn = this.business.addParent(cParent.business, hierarchyType, startDate, endDate);
    this.vertex.addParent(cParent.vertex, hierarchyType, startDate, endDate);

    return ptn;
  }

  @Override
  public void lock()
  {
    this.business.lock();
    this.vertex.lock();
  }

  @Override
  public void populate(GeoObject geoObject)
  {
    this.business.populate(geoObject);
    this.vertex.populate(geoObject);
  }
  
  @Override
  public void populate(GeoObjectOverTime goTime)
  {
    this.business.populate(goTime);
    this.vertex.populate(goTime);
  }

  @Override
  public void apply(boolean isImport)
  {
    this.business.apply(isImport);
    this.vertex.apply(isImport);
  }
}
