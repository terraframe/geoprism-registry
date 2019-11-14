package net.geoprism.registry.model;

import java.util.List;
import java.util.Map;

import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;

import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.registry.GeoObjectStatus;

public class CompositeServerGeoObject implements ServerGeoObjectIF
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
  public GeoObject getGeoObject()
  {
    return this.business.getGeoObject();
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
  public void setStatus(GeoObjectStatus status)
  {
    this.business.setStatus(status);
    this.vertex.setStatus(status);
  }

  @Override
  public void setLabel(LocalizedValue label)
  {
    this.business.setLabel(label);
    this.vertex.setLabel(label);
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
  public String getValue(String attributeName)
  {
    return this.business.getValue(attributeName);
  }

  @Override
  public void setValue(String attributeName, Object value)
  {
    this.business.setValue(attributeName, value);
    this.vertex.setValue(attributeName, value);
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
  public ChildTreeNode getChildGeoObjects(String[] childrenTypes, Boolean recursive)
  {
    return this.business.getChildGeoObjects(childrenTypes, recursive);
  }

  @Override
  public ParentTreeNode getParentGeoObjects(String[] parentTypes, Boolean recursive)
  {
    return this.business.getParentGeoObjects(parentTypes, recursive);
  }

  @Override
  public ParentTreeNode addChild(ServerGeoObjectIF child, String hierarchyCode)
  {
    CompositeServerGeoObject cChild = (CompositeServerGeoObject) child;

    ParentTreeNode ptn = this.business.addChild(cChild.business, hierarchyCode);
    this.vertex.addChild(cChild.vertex, hierarchyCode);

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
  public ParentTreeNode addParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType)
  {
    CompositeServerGeoObject cParent = (CompositeServerGeoObject) parent;

    ParentTreeNode ptn = this.business.addParent(cParent.business, hierarchyType);
    this.vertex.addParent(cParent.vertex, hierarchyType);

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
  public void apply(boolean isImport)
  {
    this.business.apply(isImport);
    this.vertex.apply(isImport);
  }

}
