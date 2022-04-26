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
package org.commongeoregistry.adapter.dataaccess;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This object is used to model a tree of {@link GeoObject}s representing children relationships 
 * of the given {@link HierarchyType}
 * 
 * @author nathan
 *
 */
public class ChildTreeNode extends TreeNode
{

  /**
   * 
   */
  private static final long serialVersionUID       = -5433588672816363747L;
  
  public static final String JSON_CHILDREN         = "children";
  
 
  private List<ChildTreeNode> children;
  
  /**
   * 
   * 
   * @param _geoObject
   * @param _hierarchyType
   */
  public ChildTreeNode(GeoObject geoObject, HierarchyType hierarchyType)
  {
    super(geoObject, hierarchyType);
    
    this.children = Collections.synchronizedList(new LinkedList<ChildTreeNode>());
  }
  
  /**
   * Returns the children of the {@link GeoObject} of this {@link ChildTreeNode}
   * 
   * @return children of the {@link GeoObject} of this {@link ChildTreeNode}
   */
  public List<ChildTreeNode> getChildren() 
  {
    return this.children;
  }

  /**
   * Add a child to the current node.
   * 
   * @param _child
   */
  public void addChild(ChildTreeNode child)
  {
    this.children.add(child);
  }
  
  /**
   * Returns the relationships of the {@link ChildTreeNode}.
   * 
   * @param _json the JSON being constructed.
   * 
   * @return JSON being constructed
   */
  @Override
  protected JsonObject relationshipsToJSON(JsonObject json)
  {
    JsonArray jaChildren = new JsonArray();
    for (int i = 0; i < this.children.size(); ++i)
    {
      ChildTreeNode child = this.children.get(i);
      
      jaChildren.add(child.toJSON());
    }
    
    json.add(JSON_CHILDREN, jaChildren);
    
    return json;
  }
  
  /**
   * Constructs a {@link ChildTreeNode} from the given JSON string.
   * 
   * @param sJson
   * @param _registry Adapter class containing cached metadata.
   * @return
   */
  public static ChildTreeNode fromJSON(String sJson, RegistryAdapter registry)
  {
    JsonParser parser = new JsonParser();
    
    JsonObject oJson = parser.parse(sJson).getAsJsonObject();
    
    GeoObject geoObj = GeoObject.fromJSON(registry, oJson.get(JSON_GEO_OBJECT).getAsJsonObject().toString());
    
    HierarchyType hierarchyType = null;
    if (oJson.has(JSON_HIERARCHY_TYPE))
    {
      hierarchyType = registry.getMetadataCache().getHierachyType(oJson.get(JSON_HIERARCHY_TYPE).getAsString()).get();
    }
    
    ChildTreeNode tn = new ChildTreeNode(geoObj, hierarchyType);
    
    if (oJson.has(JSON_CHILDREN))
    {
      JsonArray jaChildren = oJson.get(JSON_CHILDREN).getAsJsonArray();
      for (int i = 0; i < jaChildren.size(); ++i)
      {
        JsonObject joChild = jaChildren.get(i).getAsJsonObject();
        
        ChildTreeNode tnChild = ChildTreeNode.fromJSON(joChild.toString(), registry);
        
        tn.addChild(tnChild);
      }
    }
    
    return tn;
  }
}
