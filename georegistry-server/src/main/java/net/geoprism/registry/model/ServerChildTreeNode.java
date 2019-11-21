package net.geoprism.registry.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;

public class ServerChildTreeNode extends ServerTreeNode
{
  private List<ServerChildTreeNode> children;

  /**
   * 
   * 
   * @param _geoObject
   * @param _hierarchyType
   */
  public ServerChildTreeNode(ServerGeoObjectIF geoObject, ServerHierarchyType hierarchyType)
  {
    super(geoObject, hierarchyType);

    this.children = Collections.synchronizedList(new LinkedList<ServerChildTreeNode>());
  }

  /**
   * Returns the children of the {@link GeoObject} of this
   * {@link ServerChildTreeNode}
   * 
   * @return children of the {@link GeoObject} of this
   *         {@link ServerChildTreeNode}
   */
  public List<ServerChildTreeNode> getChildren()
  {
    return this.children;
  }

  /**
   * Add a child to the current node.
   * 
   * @param _child
   */
  public void addChild(ServerChildTreeNode child)
  {
    this.children.add(child);
  }

  public ChildTreeNode toNode()
  {
    ChildTreeNode node = new ChildTreeNode(this.getGeoObject().toGeoObject(), this.getHierarchyType() != null ? this.getHierarchyType().getType() : null);

    for (ServerChildTreeNode child : this.children)
    {
      node.addChild(child.toNode());
    }

    return node;
  }
}
