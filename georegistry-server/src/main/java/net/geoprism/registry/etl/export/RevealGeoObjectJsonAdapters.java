/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.etl.export;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.wololo.jts2geojson.GeoJSONWriter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.geoprism.registry.AdapterUtilities;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.service.ServiceFactory;

public class RevealGeoObjectJsonAdapters
{
  public static class RevealSerializer implements JsonSerializer<GeoObject>
  {
    private ServerHierarchyType hierarchyType;
    
    private Boolean includeLevel;
    
    private ServerGeoObjectType got;
    
    private Integer depth;
    
    private String externalSystemId;
    
    public RevealSerializer(ServerGeoObjectType got, ServerHierarchyType hierarchyType, Boolean includeLevel, String externalSystemId)
    {
      this.got = got;
      this.hierarchyType = hierarchyType;
      this.includeLevel = includeLevel;
      this.externalSystemId = externalSystemId;
      
      calculateDepth();
    }
    
    @Override
    public JsonElement serialize(GeoObject go, Type typeOfSrc, JsonSerializationContext context)
    {
      ExternalSystem system = ExternalSystem.getByExternalSystemId(this.externalSystemId);
      ServerGeoObjectIF serverGo = ServiceFactory.getGeoObjectService().getGeoObject(go);
      
      JsonObject joGO = new JsonObject();
      {
        joGO.addProperty("type", "feature");
        
        joGO.addProperty("id", serverGo.getExternalId(system));
        
        if (go.getGeometry() != null)
        {
          GeoJSONWriter gw = new GeoJSONWriter();
          org.wololo.geojson.Geometry gJSON = gw.write(go.getGeometry());
    
          JsonObject joGeom = new JsonParser().parse(gJSON.toString()).getAsJsonObject();
          
          joGO.add("geometry", joGeom);
        }
        
        JsonObject props = new JsonObject();
        {
          props.addProperty("status", go.getStatus().getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
          
          props.addProperty("name", go.getDisplayLabel().getValue());
          
          props.addProperty("version", 0);
          
          props.addProperty("OpenMRS_Id", 0);
          
          props.addProperty("externalId", go.getCode());
          
          props.addProperty("name_en", go.getDisplayLabel().getValue(Locale.ENGLISH.toString()));
          
          props.addProperty("createDate", go.getCreateDate().getTime());
          
          props.addProperty("lastUpdateDate", go.getLastUpdateDate().getTime());
          
          if (this.includeLevel)
          {
            props.addProperty("geographicLevel", this.depth);
          }
          
          if (this.depth == null || this.depth > 0)
          {
            ServerGeoObjectIF parent = getParent(serverGo, this.hierarchyType.getCode());
            
            if (parent != null)
            {
              props.addProperty("parentId", parent.getExternalId(system));
              
              props.addProperty("externalParentId", parent.getCode());
            }
          }
        }
        joGO.add("properties", props);
        
//        joGO.addProperty("serverVersion", 0);
      }
      return joGO;
    }
    
    public static ServerGeoObjectIF getParent(ServerGeoObjectIF serverGo, String hierarchyCode)
    {
      ServerParentTreeNode sptn = serverGo.getParentGeoObjects(null, false);
      
      List<ServerParentTreeNode> parents = sptn.getParents();
      
      for (ServerParentTreeNode parent : parents)
      {
        if (hierarchyCode == null || parent.getHierarchyType().getCode().equals(hierarchyCode))
        {
          return parent.getGeoObject();
        }
      }
      
      return null;
    }
    
    public void calculateDepth()
    {
      if (!this.includeLevel) { return; }
      
      if (got.getUniversal().getParents(hierarchyType.getUniversalType()).getAll().size() > 1)
      {
        throw new UnsupportedOperationException("Multiple GeoObjectType parents not supported when 'includeLevel' is specified.");
      }
      
      List<GeoObjectType> ancestors = AdapterUtilities.getInstance().getTypeAncestors(this.got, this.hierarchyType.getCode());
      
      this.depth = ancestors.size();
    }
  }
}
