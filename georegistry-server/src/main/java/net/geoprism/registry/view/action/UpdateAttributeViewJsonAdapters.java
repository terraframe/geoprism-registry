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
package net.geoprism.registry.view.action;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.geoprism.registry.action.ChangeRequest;
import net.geoprism.registry.action.geoobject.UpdateAttributeAction;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

public class UpdateAttributeViewJsonAdapters
{
  public static final String PARENT_ATTR_NAME = "_PARENT_";
  
  public static AbstractUpdateAttributeView deserialize(String json, String attributeName, ServerGeoObjectType type)
  {
    GsonBuilder builder = new GsonBuilder();
    
    if (attributeName.equals(PARENT_ATTR_NAME))
    {
      builder.registerTypeAdapter(UpdateValueOverTimeView.class, new UpdateParentValueOverTimeViewDeserializer());
      
      AbstractUpdateAttributeView view = builder.create().fromJson(json, UpdateParentView.class);
      view.setAttributeName(attributeName);
      return view;
    }
    else if (attributeName.equals("geometry"))
    {
      AbstractUpdateAttributeView view = builder.create().fromJson(json, UpdateChangeOverTimeAttributeView.class);
      view.setAttributeName(attributeName);
      return view;
    }
    else
    {
      AttributeType attr = type.getAttribute(attributeName).get();
      
      if (attr.isChangeOverTime())
      {
        AbstractUpdateAttributeView view = builder.create().fromJson(json, UpdateChangeOverTimeAttributeView.class);
        view.setAttributeName(attributeName);
        return view;
      }
      else
      {
        AbstractUpdateAttributeView view = builder.create().fromJson(json, UpdateStandardAttributeView.class);
        view.setAttributeName(attributeName);
        return view;
      }
    }
  }

//  public static class UpdateAttributeViewDeserializer implements JsonDeserializer<UpdateAttributeView>
//  {
//    protected ServerGeoObjectType type;
//    
//    protected String attributeName;
//
//    public UpdateAttributeViewDeserializer(ServerGeoObjectType type, String attributeName)
//    {
//      this.type = type;
//      this.attributeName = attributeName;
//    }
//    
//    @Override
//    public UpdateAttributeView deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
//    {
//      if (this.attributeName.equals(PARENT_ATTR_NAME))
//      {
//        return context.deserialize(json, UpdateParentView.class);
//      }
//      else
//      {
//        AttributeType attr = type.getAttribute(this.attributeName).get();
//        
//        if (attr.isChangeOverTime())
//        {
//          return context.deserialize(json, UpdateChangeOverTimeAttributeView.class);
//        }
//        else
//        {
//          return context.deserialize(json, UpdateAttributeView.class);
//        }
//      }
//    }
//  }
  
  public static class UpdateParentValueOverTimeViewDeserializer implements JsonDeserializer<UpdateParentValueOverTimeView>
  {
    @Override
    public UpdateParentValueOverTimeView deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
      return context.deserialize(json, UpdateParentValueOverTimeView.class);
    }
  }
  
  public static class UpdateParentValueOverTimeViewSerializer implements JsonSerializer<UpdateParentValueOverTimeView>
  {
    UpdateAttributeAction action;
    
    UpdateParentView updateParentView;
    
    public UpdateParentValueOverTimeViewSerializer(UpdateAttributeAction action, UpdateParentView updateParentView)
    {
      this.action = action;
      this.updateParentView = updateParentView;
    }
    
    @Override
    public JsonElement serialize(UpdateParentValueOverTimeView src, Type typeOfSrc, JsonSerializationContext context)
    {
//      CustomSerializer serializer = RegistryService.getInstance().serializer(Session.getCurrentSession().getOid());
      final ChangeRequest cr = this.action.getChangeRequest();
      final VertexServerGeoObject child = cr.getGeoObject();
      
      final ServerGeoObjectType cType = child.getType();
      
      Date startDate = src.newStartDate;
      if (startDate == null)
      {
        startDate = src.oldStartDate;
      }
      
      JsonObject jo = context.serialize(src).getAsJsonObject();
      
      VertexServerGeoObject newParent = src.getNewValueAsGO();
      
      ServerHierarchyType sht = ServiceFactory.getMetadataCache().getHierachyType(updateParentView.getHierarchyCode()).get();
      
//      ServerParentTreeNodeOverTime parents;
//      ServerParentTreeNode tnChild;
//      
//      if (newParent == null)
//      {
//        parents = child.getParentsOverTime(null, true);
//        tnChild = parents.getEntries(sht).get(0);
//      }
//      else
//      {
//        ServerParentTreeNodeOverTime grandParents = newParent.getParentsOverTime(null, true);
//        
//        tnChild = new ServerParentTreeNode(child, sht, null, null, src.oid);
//        
//        for (ServerParentTreeNode grandParent : grandParents.getEntries(sht))
//        {
//          tnChild.addParent(grandParent);
//        }
//        
//        parents = new ServerParentTreeNodeOverTime(cr.getGeoObjectType());
//        
//        final List<ServerHierarchyType> hierarchies = cType.getHierarchies();
//        
//        for (ServerHierarchyType ht : hierarchies)
//        {
//          parents.add(ht);
//        }
//        
//        parents.add(sht, tnChild);
//      }
//      
//      Date startDate = src.newStartDate;
//      if (startDate == null)
//      {
//        startDate = src.oldStartDate;
//      }
//      
//      Date endDate = src.newEndDate;
//      if (endDate == null)
//      {
//        endDate = src.oldEndDate;
//      }
//      
//      tnChild.setStartDate(startDate);
//      tnChild.setEndDate(endDate);
//      
//      JsonArray hierarchies = parents.toJSON();
//      
//      for (int i = 0; i < hierarchies.size(); ++i)
//      {
//        JsonObject hierarchy = hierarchies.get(i).getAsJsonObject();
//        
//        if (hierarchy.get("code").getAsString().equals(sht.getCode()))
//        {
//          jo.add("parents", hierarchy);
//          break;
//        }
//      }
      
      JsonObject parents = new JsonObject();
      
      ServerParentTreeNode sptns;
      
      if (newParent == null)
      {
        newParent = cr.getGeoObject();
        
        sptns = newParent.getParentsForHierarchy(sht, true, startDate);
      }
      else
      {
        JsonObject parent = new JsonObject();
        parent.addProperty(ServerParentTreeNodeOverTime.JSON_ENTRY_PARENT_TEXT, newParent.getDisplayLabel().getValue() + " : " + newParent.getCode());
        parent.add(ServerParentTreeNodeOverTime.JSON_ENTRY_PARENT_GEOOBJECT, newParent.toGeoObject(startDate).toJSON());
        parents.add(newParent.getType().getCode(), parent);
        
        sptns = newParent.getParentsForHierarchy(sht, true, startDate);
      }
      
      List<GeoObjectType> parentTypes = cType.getTypeAncestors(sht, false);
      
      for (ServerParentTreeNode node : sptns.getParents())
      {
        for (GeoObjectType parent : parentTypes)
        {
          ServerGeoObjectType pType = ServerGeoObjectType.get(parent);
          
          if (!pType.getCode().equals(cType.getCode()))
          {
            ServerParentTreeNode match = null;
            
            if (node.getGeoObject().getType().getCode().equals(pType.getCode()))
            {
              match = node;
            }
            else
            {
              final List<ServerParentTreeNode> ptns = node.findParentOfType(pType.getCode());
              
              if (ptns.size() > 0)
              {
                match = ptns.get(0);
              }
            }
            
            if (match != null)
            {
              final ServerGeoObjectIF sGeoObject = match.getGeoObject();
              final GeoObject geoObject = sGeoObject.toGeoObject(startDate);
              geoObject.setGeometry(null);
              
              JsonObject pObject = new JsonObject();
              pObject.add(ServerParentTreeNodeOverTime.JSON_ENTRY_PARENT_GEOOBJECT, geoObject.toJSON());
              
              LocalizedValue label = sGeoObject.getDisplayLabel();
              if (label != null)
              {
                pObject.addProperty(ServerParentTreeNodeOverTime.JSON_ENTRY_PARENT_TEXT, label.getValue() + " : " + sGeoObject.getCode());
              }
              else
              {
                pObject.addProperty(ServerParentTreeNodeOverTime.JSON_ENTRY_PARENT_TEXT, "null" + " : " + sGeoObject.getCode());
              }

              parents.add(pType.getCode(), pObject);
            }
          }
        }
      }
      
      jo.add("parents", parents);
      
      return jo;
    }
  }
}
