package net.geoprism.registry.model;

import java.util.List;
import java.util.Map;

import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;

import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;

public interface ServerGeoObjectIF
{
  public GeoObject getGeoObject();

  public String getCode();

  public String getUid();

  public String getRunwayId();

  public ServerGeoObjectType getType();

  public List<? extends MdAttributeConcreteDAOIF> getMdAttributeDAOs();

  public String getValue(String attributeName);

  public String bbox();

  public Map<String, ServerHierarchyType> getHierarchyTypeMap(String[] relationshipTypes);

  public ChildTreeNode getChildGeoObjects(String[] childrenTypes, Boolean recursive);

  public ParentTreeNode getParentGeoObjects(String[] parentTypes, Boolean recursive);

  public ParentTreeNode addChild(ServerGeoObjectIF child, String hierarchyCode);

  public void removeChild(ServerGeoObjectIF child, String hierarchyCode);

  public void removeParent(ServerTreeGeoObject parent, ServerHierarchyType hierarchyType);

  public ParentTreeNode addParent(ServerTreeGeoObject parent, ServerHierarchyType hierarchyType);
}
