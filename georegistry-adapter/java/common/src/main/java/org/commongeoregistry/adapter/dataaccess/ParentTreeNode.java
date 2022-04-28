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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ParentTreeNode extends TreeNode
{

  /**
   * 
   */
  private static final long serialVersionUID      = -942907390110427275L;

  public static final String JSON_PARENTS         = "parents";
  
  private List<ParentTreeNode> parents;
  
  /**
   * 
   * 
   * @param geoObject
   * @param hierarchyType
   */
  public ParentTreeNode(GeoObject geoObject, HierarchyType hierarchyType)
  {
    super(geoObject, hierarchyType);
    
    this.parents = Collections.synchronizedList(new LinkedList<ParentTreeNode>());
  }
  
  /**
   * Returns the parents of the {@link GeoObject} of this {@link ParentTreeNode}
   * 
   * @return parents of the {@link GeoObject} of this {@link ParentTreeNode}
   */
  public List<ParentTreeNode> getParents() 
  {
    return this.parents;
  }
  
  /**
   * Locates and returns parents who are of the given GeoObjectType.
   * 
   * @param typeCode
   * @return
   */
  public List<ParentTreeNode> findParentOfType(String typeCode)
  {
    List<ParentTreeNode> ret = new ArrayList<ParentTreeNode>();
    
    if (this.parents != null)
    {
      for (ParentTreeNode parent : parents)
      {
        if (parent.getGeoObject().getType().getCode().equals(typeCode))
        {
          ret.add(parent);
        }
        else
        {
          List<ParentTreeNode> parentOfParent = parent.findParentOfType(typeCode);
          
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
  public void addParent(ParentTreeNode parents)
  {
    this.parents.add(parents);
  }
  
  /**
   * Returns the relationships of the {@link ParentTreeNode}.
   * 
   * @param json the JSON being constructed.
   * 
   * @return JSON being constructed
   */
  @Override
  protected JsonObject relationshipsToJSON(JsonObject json)
  {
    JsonArray jaParents = new JsonArray();
    for (int i = 0; i < this.parents.size(); ++i)
    {
      ParentTreeNode parent = this.parents.get(i);
      
      jaParents.add(parent.toJSON());
    }
    
    json.add(JSON_PARENTS, jaParents);
    
    return json;
  }
  
  /**
   * Constructs a {@link ParentTreeNode} from the given JSON string.
   * 
   * @param sJson
   * @param registry Adapter class containing cached metadata.
   * @return
   */
  public static ParentTreeNode fromJSON(String sJson, RegistryAdapter registry)
  {
    JsonParser parser = new JsonParser();
    
    JsonObject oJson = parser.parse(sJson).getAsJsonObject();
    
    GeoObject geoObj = GeoObject.fromJSON(registry, oJson.get(JSON_GEO_OBJECT).getAsJsonObject().toString());
    
    HierarchyType hierarchyType = null;
    if (oJson.has(JSON_HIERARCHY_TYPE))
    {
      hierarchyType = registry.getMetadataCache().getHierachyType(oJson.get(JSON_HIERARCHY_TYPE).getAsString()).get();
    }
    
    ParentTreeNode tn = new ParentTreeNode(geoObj, hierarchyType);
    
    if (oJson.has(JSON_PARENTS))
    {
      JsonArray jaParents = oJson.get(JSON_PARENTS).getAsJsonArray();
      for (int i = 0; i < jaParents.size(); ++i)
      {
        JsonObject joParent = jaParents.get(i).getAsJsonObject();
        
        ParentTreeNode tnParent = ParentTreeNode.fromJSON(joParent.toString(), registry);
        
        tn.addParent(tnParent);
      }
    }
    
    return tn;
  }

}
