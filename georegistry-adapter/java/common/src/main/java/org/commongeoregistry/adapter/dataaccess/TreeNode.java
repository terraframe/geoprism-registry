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

import java.io.Serializable;

import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.google.gson.JsonObject;

public abstract class TreeNode implements Serializable
{
  public static final String JSON_GEO_OBJECT            = "geoObject";
  
  public static final String JSON_HIERARCHY_TYPE        = "hierarchyType";
  
  /**
   * 
   */
  private static final long serialVersionUID = 6548238839268982437L;

  private GeoObject geoObject;
  
  private HierarchyType hierarchyType;
  
  public TreeNode(GeoObject geoObject, HierarchyType hierarchyType)
  {
    this.geoObject = geoObject;
    
    this.hierarchyType = hierarchyType;
  }

  public GeoObject getGeoObject() 
  {
    return this.geoObject;
  }

  public HierarchyType getHierachyType() 
  {
    return this.hierarchyType;
  }

  public JsonObject toJSON()
  {
    JsonObject json = new JsonObject();
    
    if (this.geoObject != null)
    {
      json.add(JSON_GEO_OBJECT, this.geoObject.toJSON());
    }
    else
    {
      json.add(JSON_GEO_OBJECT, null);
    }
    
    if (this.hierarchyType != null) // The hierarchyType is null on the root node
    {
      json.addProperty(JSON_HIERARCHY_TYPE, this.hierarchyType.getCode());
    }
    
    json = this.relationshipsToJSON(json);
    
    return json;
  }
  
  /**
   * Returns the relationships of the {@link TreeNode}.
   * 
   * @param json the JSON being constructed.
   * 
   * @return JSON being constructed
   */
  protected abstract JsonObject relationshipsToJSON(JsonObject json);
  

//  public static TreeNode fromJSON(String sJson, RegistryAdapterServer registry)
//  {
//    JsonParser parser = new JsonParser();
//    
//    JsonObject oJson = parser.parse(sJson).getAsJsonObject();
//    
//    GeoObject geoObj = GeoObject.fromJSON(registry, oJson.get("geoObject").getAsJsonObject().toString());
//    
//    HierarchyType hierarchyType = registry.getMetadataCache().getHierachyType(oJson.get("hierarchyType").getAsString()).get();
//    
//    TreeNode tn = new TreeNode(geoObj, hierarchyType);
//    
//    JsonArray jaChildren = oJson.get("children").getAsJsonArray();
//    for (int i = 0; i < jaChildren.size(); ++i)
//    {
//      JsonObject joChild = jaChildren.get(i).getAsJsonObject();
//      
//      TreeNode tnChild = TreeNode.fromJSON(joChild.toString(), registry);
//      
//      tn.addChild(tnChild);
//    }
//    
//    JsonArray jaParents = oJson.get("parents").getAsJsonArray();
//    for (int i = 0; i < jaParents.size(); ++i)
//    {
//      JsonObject joParent = jaParents.get(i).getAsJsonObject();
//      
//      TreeNode tnParent = TreeNode.fromJSON(joParent.toString(), registry);
//      
//      tn.addParent(tnParent);
//    }
//    
//    return tn;
//  }

}
