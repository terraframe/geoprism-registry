/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyNode;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.business.ontology.Term;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.gis.geo.Universal;

import net.geoprism.ontology.GeoEntityUtil;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.MasterListQuery;
import net.geoprism.registry.geoobjecttype.PrivateTypeHasPublicChildren;
import net.geoprism.registry.geoobjecttype.PrivateTypeIsReferencedInPublicMasterLists;
import net.geoprism.registry.geoobjecttype.TypeHasPrivateParents;
import net.geoprism.registry.service.ServiceFactory;

public class GeoObjectTypeMetadata extends GeoObjectTypeMetadataBase
{
  private static final long serialVersionUID = -427820585;
  
  public static final String TYPE_LABEL = "geoObjectType.label";
  
  public GeoObjectTypeMetadata()
  {
    super();
  }
  
  @Override
  public void apply()
  {
    if (!this.isNew() && this.isModified(GeoObjectTypeMetadata.ISPRIVATE))
    {
      final ServerGeoObjectType type = this.getServerType();
      
      // They aren't allowed to set this to private in certain scenarios
      if (this.getIsPrivate())
      {
        if (this.hasPublicChildren())
        {
          PrivateTypeHasPublicChildren ex = new PrivateTypeHasPublicChildren();
          ex.setTypeLabel(this.getServerType().getLabel().getValue());
          throw ex;
        }
        
        if (this.isReferencedInPublicMasterLists())
        {
          PrivateTypeIsReferencedInPublicMasterLists ex = new PrivateTypeIsReferencedInPublicMasterLists();
          ex.setTypeLabel(this.getServerType().getLabel().getValue());
          throw ex;
        }
      }
      else
      {
        if (this.hasPrivateParents())
        {
          TypeHasPrivateParents ex = new TypeHasPrivateParents();
          ex.setTypeLabel(this.getServerType().getLabel().getValue());
          throw ex;
        }
      }
      
      // Set the isPrivate field for all children
      if (this.isModified(GeoObjectTypeMetadata.ISPRIVATE) && type.getIsAbstract())
      {
        List<ServerGeoObjectType> subtypes = type.getSubtypes();
        
        for (ServerGeoObjectType subtype : subtypes)
        {
          GeoObjectTypeMetadata submetadata = subtype.getMetadata();
          
          submetadata.appLock();
          submetadata.setIsPrivate(this.getIsPrivate());
          submetadata.apply();
        }
      }
    }
    
    super.apply();
  }
  
  private boolean isReferencedInPublicMasterLists()
  {
    Universal uni = this.getUniversal();
    ServerGeoObjectType type = this.getServerType();
    
    QueryFactory qf = new QueryFactory();
    
    MasterListQuery mlq = new MasterListQuery(qf);
    mlq.WHERE(mlq.getVisibility().EQ(MasterList.PUBLIC));
    
    OIterator<? extends MasterList> it = mlq.getIterator();
    
    while (it.hasNext())
    {
      MasterList list = it.next();
      
      if (list.getUniversal().getOid().equals(uni.getOid()))
      {
        PrivateTypeIsReferencedInPublicMasterLists ex = new PrivateTypeIsReferencedInPublicMasterLists();
        ex.setTypeLabel(this.getServerType().getLabel().getValue());
        throw ex;
      }
      else
      {
        JsonArray hierarchies = list.getHierarchiesAsJson();
        
        for (int i = 0; i < hierarchies.size(); i++)
        {
          JsonObject hierarchy = hierarchies.get(i).getAsJsonObject();
          
          JsonArray parents = hierarchy.get("parents").getAsJsonArray();
          
          for (int j = 0; j < parents.size(); ++j)
          {
            JsonObject parent = parents.get(j).getAsJsonObject();
            
            if (parent.has("selected") && parent.get("selected").getAsBoolean())
            {
              if (parent.has("code") && parent.get("code").getAsString().equals(type.getCode()))
              {
                return true;
              }
            }
          }
        }
      }
    }
    
    return false;
  }
  
  public boolean hasPrivateParents()
  {
    List<ServerHierarchyType> hierarchyTypes = ServiceFactory.getMetadataCache().getAllHierarchyTypes();
    
    final Universal root = Universal.getRoot();
    
    for (ServerHierarchyType ht : hierarchyTypes)
    {
      Collection<Term> uniParents = GeoEntityUtil.getOrderedAncestors(root, this.getUniversal(), ht.getUniversalType());

      if (uniParents.size() > 1)
      {
        for (Term uniParent : uniParents)
        {
          if (!this.getKey().equals(uniParent.getKey()) && GeoObjectTypeMetadata.getByKey(uniParent.getKey()).getIsPrivate())
          {
            return true;
          }
        }
      }
    }
    
    return false;
  }

  public boolean hasPublicChildren()
  {
    ServerGeoObjectType type = this.getServerType();
    GeoObjectType typeDTO = type.getType();
    
    List<ServerHierarchyType> hierarchyTypes = ServiceFactory.getMetadataCache().getAllHierarchyTypes();
    
    for (ServerHierarchyType ht : hierarchyTypes)
    {
      List<HierarchyNode> roots = ht.getType().getRootGeoObjectTypes();
      
      for (HierarchyNode root : roots)
      {
        HierarchyNode node = root.findChild(typeDTO);
        
        if (node != null)
        {
          Iterator<HierarchyNode> it = node.getDescendantsIterator();
          
          while (it.hasNext())
          {
            HierarchyNode child = it.next();
            
            if (!child.getGeoObjectType().getIsPrivate())
            {
              return true;
            }
          }
        }
      }
    }
    
    return false;
  }
  
  public ServerGeoObjectType getServerType()
  {
    return ServerGeoObjectType.get(this.getUniversal());
  }
  
  @Override
  protected String buildKey()
  {
    return this.getUniversal().getKey();
  }
  
  public String getClassDisplayLabel()
  {
    return sGetClassDisplayLabel();
  }
  
  public static String sGetClassDisplayLabel()
  {
    return LocalizationFacade.localize(TYPE_LABEL);
  }
  
  public static String getAttributeDisplayLabel(String attributeName)
  {
    return LocalizationFacade.localize("geoObjectType.attr."  + attributeName);
  }
  
}
