package net.geoprism.registry.model;

import java.util.Date;

import org.commongeoregistry.adapter.dataaccess.TreeNode;

public abstract class ServerTreeNode
{
  private ServerGeoObjectIF   geoObject;

  private ServerHierarchyType hierarchyType;

  private Date                date;

  public ServerTreeNode(ServerGeoObjectIF geoObject, ServerHierarchyType hierarchyType, Date date)
  {
    this.geoObject = geoObject;
    this.hierarchyType = hierarchyType;
    this.date = date;
  }

  public ServerGeoObjectIF getGeoObject()
  {
    return geoObject;
  }

  public ServerHierarchyType getHierarchyType()
  {
    return hierarchyType;
  }

  public Date getDate()
  {
    return date;
  }

  public abstract TreeNode toNode();

}
