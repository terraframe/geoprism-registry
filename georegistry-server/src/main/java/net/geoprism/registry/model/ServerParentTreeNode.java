package net.geoprism.registry.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;

public class ServerParentTreeNode extends ServerTreeNode
{
  private List<ServerParentTreeNode> parents;

  /**
   * 
   * 
   * @param geoObject
   * @param hierarchyType
   */
  public ServerParentTreeNode(ServerGeoObjectIF geoObject, ServerHierarchyType hierarchyType)
  {
    super(geoObject, hierarchyType);

    this.parents = Collections.synchronizedList(new LinkedList<ServerParentTreeNode>());
  }

  /**
   * Returns the parents of the {@link ServerGeoObjectIF} of this
   * {@link ServerParentTreeNode}
   * 
   * @return parents of the {@link ServerGeoObjectIF} of this
   *         {@link ServerParentTreeNode}
   */
  public List<ServerParentTreeNode> getParents()
  {
    return this.parents;
  }

  /**
   * Locates and returns parents who are of the given ServerGeoObjectIFType.
   * 
   * @param typeCode
   * @return
   */
  public List<ServerParentTreeNode> findParentOfType(String typeCode)
  {
    List<ServerParentTreeNode> ret = new ArrayList<ServerParentTreeNode>();

    if (this.parents != null)
    {
      for (ServerParentTreeNode parent : parents)
      {
        if (parent.getGeoObject().getType().getCode().equals(typeCode))
        {
          ret.add(parent);
        }
        else
        {
          List<ServerParentTreeNode> parentOfParent = parent.findParentOfType(typeCode);

          ret.addAll(parentOfParent);
        }
      }
    }

    return ret;
  }

  /**
   * Add a parent to the current node.
   * 
   * @param parents
   */
  public void addParent(ServerParentTreeNode parents)
  {
    this.parents.add(parents);
  }

  public ParentTreeNode toNode()
  {
    ParentTreeNode node = new ParentTreeNode(this.getGeoObject().toGeoObject(), this.getHierarchyType() != null ? this.getHierarchyType().getType() : null);

    for (ServerParentTreeNode parent : this.parents)
    {
      node.addParent(parent.toNode());
    }

    return node;
  }
}
