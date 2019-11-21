package net.geoprism.registry.model;

import org.commongeoregistry.adapter.dataaccess.TreeNode;

public abstract class ServerTreeNode
{
  private ServerGeoObjectIF   geoObject;

  private ServerHierarchyType hierarchyType;

  public ServerTreeNode(ServerGeoObjectIF geoObject, ServerHierarchyType hierarchyType)
  {
    this.geoObject = geoObject;

    this.hierarchyType = hierarchyType;
  }

  public ServerGeoObjectIF getGeoObject()
  {
    return geoObject;
  }

  public ServerHierarchyType getHierarchyType()
  {
    return hierarchyType;
  }

  public abstract TreeNode toNode();

}
