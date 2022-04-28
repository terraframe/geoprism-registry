/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Common Geo Registry Adapter(tm).
 *
 * Common Geo Registry Adapter(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Common Geo Registry Adapter(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Common Geo Registry Adapter(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package org.commongeoregistry.adapter.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.RegistryAdapter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Represents a node in a {@link HierarchyType} where the node value is a
 * {@link GeoObjectType} and the children are also {@link GeoObjectType}s in
 * the {@link HierarchyType}.
 * 
 * @author nathan
 * @author rrowlands
 *
 */
public class HierarchyNode
{
  /**
   * {@link GeoObjectType} in the hierarchies node.
   */
  private GeoObjectType       geoObjectType;

  /**
   * Children {@link GeoObjectType}s in the hierarchy.
   */
  private List<HierarchyNode> children;

  /**
   * If the node is from an inherited hierarchy, this is the code of that
   * hierarchy. If the node is not inherited, this field will be null.
   */
  private String              inheritedHierarchyCode;

  /**
   * 
   * @param _geoObjectType
   *          {@link GeoObjectType} in the hierarchies node.
   */
  public HierarchyNode(GeoObjectType _geoObjectType)
  {
    this(_geoObjectType, null);
  }

  /**
   * 
   * @param _geoObjectType
   *          {@link GeoObjectType} in the hierarchies node.
   */
  public HierarchyNode(GeoObjectType _geoObjectType, String _inheritedHierarchyCode)
  {
    this.geoObjectType = _geoObjectType;
    this.inheritedHierarchyCode = _inheritedHierarchyCode;
    this.children = Collections.synchronizedList(new LinkedList<HierarchyNode>());
  }

  /**
   * Returns the {@link GeoObjectType} defined on this node in the hierarchy.
   * 
   * @return the {@link GeoObjectType} defined on this node in the hierarchy.
   */
  public GeoObjectType getGeoObjectType()
  {
    return this.geoObjectType;
  }
  
  public void setGeoObjectType(GeoObjectType _geoObjectType)
  {
    this.geoObjectType = _geoObjectType;
  }

  /**
   * Returns true if the hierarchy (this node or all children) contains the
   * provided GeoObjectType.
   */
  public boolean hierarchyHasGeoObjectType(String typeCode, boolean excludeInherited)
  {
    if ( ( !excludeInherited || ( excludeInherited && this.inheritedHierarchyCode == null ) ) && this.geoObjectType.getCode().equals(typeCode))
    {
      return true;
    }

    for (HierarchyNode node : this.children)
    {
      if (node.hierarchyHasGeoObjectType(typeCode, excludeInherited))
      {
        return true;
      }
    }

    return false;
  }

  /**
   * Add the given child {@link GeoObjectType} to this node in the hierarchy.
   * 
   * @param _child
   *          Child {@link GeoObjectType} to add to the hierarchy.
   */
  public void addChild(HierarchyNode _hierarchyNode)
  {
    this.children.add(_hierarchyNode);
  }
  
  /**
   * Removes the child from the list of children in this node.
   * 
   * @param hn
   */
  public void removeChild(HierarchyNode hn)
  {
    Iterator<HierarchyNode> it = this.children.iterator();
    
    while (it.hasNext())
    {
      HierarchyNode child = it.next();
      
      if (hn.equals(child))
      {
        it.remove();
      }
    }
  }

  /**
   * Returns the child nodes of this current node.
   * 
   * @return child nodes of this current node.
   */
  public List<HierarchyNode> getChildren()
  {
    return this.children;
  }

  /**
   * @return The code of the hierarchy which this node is inherited from, if
   *         the node is inherited. If the node is not inherited, then this
   *         will return null.
   */
  public String getInheritedHierarchyCode()
  {
    return this.inheritedHierarchyCode;
  }

  /**
   * Generates JSON for this object.
   * 
   * @return JSON representation of this object.
   */
  public JsonObject toJSON()
  {
    JsonObject jsonObj = new JsonObject();

    jsonObj.addProperty(HierarchyType.JSON_GEOOBJECTTYPE, geoObjectType.getCode());
    jsonObj.addProperty(HierarchyType.JSON_INHERITED_HIER_CODE, this.getInheritedHierarchyCode());

    JsonArray jaChildren = new JsonArray();
    for (int i = 0; i < children.size(); ++i)
    {
      HierarchyNode hnode = children.get(i);

      jaChildren.add(hnode.toJSON());
    }
    jsonObj.add(HierarchyType.JSON_CHILDREN, jaChildren);

    return jsonObj;
  }

  /**
   * Generates JSON for the Hierarchy Node.
   * 
   * @param sJson
   * @param registry
   * @return JSON for the Hierarchy Node.
   */
  protected static HierarchyNode fromJSON(String sJson, RegistryAdapter registry)
  {
    JsonParser parser = new JsonParser();

    JsonObject oJson = parser.parse(sJson).getAsJsonObject();

    GeoObjectType got = registry.getMetadataCache().getGeoObjectType(oJson.get(HierarchyType.JSON_GEOOBJECTTYPE).getAsString()).get();

    String inheritedHierarchyCode = null;
    if (oJson.has(HierarchyType.JSON_INHERITED_HIER_CODE) && !oJson.get(HierarchyType.JSON_INHERITED_HIER_CODE).isJsonNull())
    {
      inheritedHierarchyCode = oJson.get(HierarchyType.JSON_INHERITED_HIER_CODE).getAsString();
    }
    
    HierarchyNode node = new HierarchyNode(got, inheritedHierarchyCode);

    JsonArray jaChildren = oJson.getAsJsonArray(HierarchyType.JSON_CHILDREN);
    for (int i = 0; i < jaChildren.size(); ++i)
    {
      JsonObject joChild = jaChildren.get(i).getAsJsonObject();

      HierarchyNode hnChild = HierarchyNode.fromJSON(joChild.toString(), registry);

      node.addChild(hnChild);
    }
    
    return node;
  }
  
  public HierarchyNode findChild(GeoObjectType type)
  {
    Iterator<HierarchyNode> it = getDescendantsIterator();
    
    while (it.hasNext())
    {
      HierarchyNode node = it.next();
      
      if (node.getGeoObjectType().getCode().equals(type.getCode()))
      {
        return node;
      }
    }
    
    return null;
  }
  
  public Iterator<HierarchyNode> getDescendantsIterator()
  {
    return this.getAllDescendants().iterator(); // TODO : There's definitely a better way to do this but I don't really have time
  }
  
  public List<HierarchyNode> getAllDescendants()
  {
    ArrayList<HierarchyNode> descends = new ArrayList<HierarchyNode>();
    
    for (HierarchyNode child : this.getChildren())
    {
      descends.add(child);
      
      descends.addAll(child.getAllDescendants());
    }
    
    return descends;
  }
  
//  public class DescendantsIterator implements Iterator<HierarchyNode>
//  {
//
//    private HierarchyNode root;
//    
//    private HierarchyNode current;
//    
//    public DescendantsIterator(HierarchyNode rootNode)
//    {
//      this.root = rootNode;
//      
//    }
//
//    @Override
//    public boolean hasNext()
//    {
//      return false;
//    }
//
//    @Override
//    public HierarchyNode next()
//    {
//      return null;
//    }
//    
//  }
}