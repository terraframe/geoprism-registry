package net.geoprism.registry.model;

import java.util.List;
import java.util.Map;

import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;

import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;

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
  public String getCode()
  {
    return this.business.getCode();
  }

  @Override
  public String getUid()
  {
    return this.business.getUid();
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
  public void apply(String statusCode, boolean isImport)
  {
    this.business.apply(statusCode, isImport);
    this.vertex.apply(statusCode, isImport);
  }

}
