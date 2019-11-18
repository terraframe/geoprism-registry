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

public interface ServerGeoObjectIF
{
  public ServerGeoObjectType getType();

  public GeoObject toGeoObject();

  public String getCode();

  public void setCode(String geoId);

  public void setStatus(GeoObjectStatus status);

  public void setGeometry(Geometry geometry);

  public Geometry getGeometry();

  public String getUid();

  public void setUid(String uid);

  public String getRunwayId();

  public Object getValue(String attributeName);

  public void setValue(String attributeName, Object value);

  public void setDisplayLabel(LocalizedValue label);

  public LocalizedValue getDisplayLabel();

  public List<? extends MdAttributeConcreteDAOIF> getMdAttributeDAOs();

  public String bbox();

  public ChildTreeNode getChildGeoObjects(String[] childrenTypes, Boolean recursive);

  public ParentTreeNode getParentGeoObjects(String[] parentTypes, Boolean recursive);

  public ParentTreeNode addChild(ServerGeoObjectIF child, String hierarchyCode);

  public void removeChild(ServerGeoObjectIF child, String hierarchyCode);

  public void removeParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType);

  public ParentTreeNode addParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType);

  public void lock();

  public void populate(GeoObject geoObject);

  public void apply(boolean isImport);

  public Map<String, ServerHierarchyType> getHierarchyTypeMap(String[] relationshipTypes);

  public Map<String, LocationInfo> getAncestorMap(ServerHierarchyType hierarchy);
}
