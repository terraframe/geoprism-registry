package net.geoprism.registry.model;

import java.util.Date;

import org.commongeoregistry.adapter.dataaccess.TreeNode;

public abstract class ServerTreeNode
{
  private ServerGeoObjectIF   geoObject;

  private ServerHierarchyType hierarchyType;

  private Date                date;

  private Date                endDate;

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

  public void setEndDate(Date endDate)
  {
    this.endDate = endDate;
  }

  public Date getEndDate()
  {
    return endDate;
  }

  public abstract TreeNode toNode();

}
