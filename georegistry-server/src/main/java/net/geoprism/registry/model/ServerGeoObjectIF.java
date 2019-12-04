package net.geoprism.registry.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.ValueOverTimeCollectionDTO;

import com.google.gson.JsonArray;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTimeCollection;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.registry.GeoObjectStatus;

public interface ServerGeoObjectIF
{
  public ServerGeoObjectType getType();

  public GeoObject toGeoObject();

  public String getCode();

  public void setCode(String code);

  public GeoObjectStatus getStatus();

  public void setStatus(GeoObjectStatus status);

  public void setStatus(GeoObjectStatus status, Date startDate, Date endDate);

  public void setGeometry(Geometry geometry);

  public void setGeometry(Geometry geometry, Date startDate, Date endDate);

  public Geometry getGeometry();

  public String getUid();

  public void setUid(String uid);

  public String getRunwayId();

  public Object getValue(String attributeName);

  public Object getValue(String attributeName, Date date);

  public ValueOverTimeCollection getValuesOverTime(String attributeName);

  public void setValuesOverTime(String attributeName, ValueOverTimeCollection collection);

  public void setValue(String attributeName, Object value);

  public void setValue(String attributeName, Object value, Date startDate, Date endDate);

  public void setDisplayLabel(LocalizedValue label);

  public void setDisplayLabel(LocalizedValue value, Date startDate, Date endDate);

  public LocalizedValue getDisplayLabel();

  public List<? extends MdAttributeConcreteDAOIF> getMdAttributeDAOs();

  public String bbox();

  public ServerChildTreeNode getChildGeoObjects(String[] childrenTypes, Boolean recursive);

  public ServerParentTreeNode getParentGeoObjects(String[] parentTypes, Boolean recursive);

  public ServerParentTreeNodeOverTime getParentsOverTime(String[] parentTypes, Boolean recursive);

  public ServerParentTreeNode addChild(ServerGeoObjectIF child, ServerHierarchyType hierarchy);

  public ServerParentTreeNode addChild(ServerGeoObjectIF entity, ServerHierarchyType hierarchy, Date startDate, Date endDate);

  public void removeChild(ServerGeoObjectIF child, String hierarchyCode);

  public ServerParentTreeNode addParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType);

  public ServerParentTreeNode addParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType, Date startDate, Date endDate);

  public void removeParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType);

  public void lock();

  public void populate(GeoObject geoObject);

  public void apply(boolean isImport);

  public Map<String, ServerHierarchyType> getHierarchyTypeMap(String[] relationshipTypes);

  public Map<String, LocationInfo> getAncestorMap(ServerHierarchyType hierarchy);

  public JsonArray getHierarchiesForGeoObject();

}
