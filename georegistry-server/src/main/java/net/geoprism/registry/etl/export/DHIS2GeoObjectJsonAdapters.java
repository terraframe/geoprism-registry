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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.runwaysdk.LocalizationFacade;

import net.geoprism.registry.AdapterUtilities;
import net.geoprism.registry.geoobject.AllowAllGeoObjectPermissionService;
import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentTreeNode;

public class DHIS2GeoObjectJsonAdapters
{
  public static class DHIS2Serializer implements JsonSerializer<GeoObject>
  {
    private ServerHierarchyType hierarchyType;
    
    private ServerGeoObjectType got;
    
    private Integer depth;
    
    private ExternalSystem ex;
    
    public DHIS2Serializer(ServerGeoObjectType got, ServerHierarchyType hierarchyType, ExternalSystem ex)
    {
      this.got = got;
      this.hierarchyType = hierarchyType;
      
      this.calculateDepth();
    }
    
    @Override
    public JsonElement serialize(GeoObject go, Type typeOfSrc, JsonSerializationContext context)
    {
      ServerGeoObjectIF serverGo = new ServerGeoObjectService(new AllowAllGeoObjectPermissionService()).getGeoObject(go);
      
      JsonObject jo = new JsonObject();
      
      jo.addProperty("code", go.getCode());
      
      jo.addProperty("level", this.depth);
      
      jo.addProperty("created", formatDate(go.getCreateDate()));
      
      jo.addProperty("lastUpdated", formatDate(go.getLastUpdateDate()));
      
      jo.addProperty("name", go.getDisplayLabel().getValue(Locale.ENGLISH)); // TODO : Which locale?
      
      jo.addProperty("id", serverGo.getExternalId(ex)); // TODO : Is this the correct id?
      
      jo.addProperty("shortName", go.getDisplayLabel().getValue(Locale.ENGLISH)); // TODO : Which locale?
      
      jo.addProperty("path", calculatePath());
      
      // TODO : openingDate ?
      
      ServerGeoObjectIF goParent = getParent(serverGo, this.hierarchyType.getCode());
      JsonObject parent = new JsonObject();
      parent.addProperty("id", goParent.getExternalId(this.ex)); // TODO : Is this the correct id?
      jo.add("parent", parent);
      
      // TODO : attributeValues ?
      
      JsonArray translations = new JsonArray();
      LocalizedValue lv = go.getDisplayLabel();
      
      List<Locale> locales = LocalizationFacade.getInstalledLocales();
      for (Locale locale : locales)
      {
        if (lv.contains(locale))
        {
          JsonObject joLocale = new JsonObject();
          
          joLocale.addProperty("property", "SHORT_NAME");
          joLocale.addProperty("locale", locale.toString());
          joLocale.addProperty("value", lv.getValue(locale));
          
          translations.add(joLocale);
        }
      }
      
      return jo;
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
    
    private String formatDate(Date date)
    {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ss.SSS");
      
      return format.format(date);
    }
    
    public String calculatePath()
    {
      return null; // TODO
    }
    
    public void calculateDepth()
    {
      if (got.getUniversal().getParents(hierarchyType.getUniversalType()).getAll().size() > 1)
      {
        throw new UnsupportedOperationException("Multiple GeoObjectType parents not supported.");
      }
      
      List<GeoObjectType> ancestors = AdapterUtilities.getInstance().getTypeAncestors(this.got, this.hierarchyType.getCode());
      
      this.depth = ancestors.size();
    }
  }
}
