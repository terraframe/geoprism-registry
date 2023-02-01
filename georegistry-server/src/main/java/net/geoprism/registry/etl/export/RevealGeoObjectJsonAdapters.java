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
package net.geoprism.registry.etl.export;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentTreeNode;

public class RevealGeoObjectJsonAdapters
{
  public static class RevealSerializer implements JsonSerializer<ServerGeoObjectIF>
  {
    private ServerHierarchyType hierarchyType;

    private Boolean             includeLevel;

    private ServerGeoObjectType got;

    private Integer             depth;

    private ExternalSystem      externalSystem;

    public RevealSerializer(ServerGeoObjectType got, ServerHierarchyType hierarchyType, Boolean includeLevel, ExternalSystem externalSystem)
    {
      this.got = got;
      this.hierarchyType = hierarchyType;
      this.includeLevel = includeLevel;
      this.externalSystem = externalSystem;

      calculateDepth();
    }

    @Override
    public JsonElement serialize(ServerGeoObjectIF serverGo, Type typeOfSrc, JsonSerializationContext context)
    {
      JsonObject joGO = new JsonObject();
      {
        joGO.addProperty("type", "Feature");

        joGO.addProperty("id", serverGo.getExternalId(this.externalSystem));

        if (serverGo.getGeometry() != null)
        {
          GeoJsonWriter gw = new GeoJsonWriter();
          String json = gw.write(serverGo.getGeometry());

          JsonObject joGeom = JsonParser.parseString(json).getAsJsonObject();

          joGO.add("geometry", joGeom);
        }

        JsonObject props = new JsonObject();
        {
          props.addProperty("exists", serverGo.getExists());

          LocalizedValue dl = serverGo.getDisplayLabel();
          if (dl != null)
          {
            props.addProperty("name", dl.getValue());
            props.addProperty("name_en", dl.getValue(Locale.ENGLISH.toString()));
          }

          props.addProperty("version", 0);

          props.addProperty("OpenMRS_Id", 0);

          props.addProperty("externalId", serverGo.getCode());

          props.addProperty("createDate", formatDate(serverGo.getCreateDate()));

          props.addProperty("lastUpdateDate", formatDate(serverGo.getLastUpdateDate()));

          if (this.includeLevel)
          {
            props.addProperty("geographicLevel", this.depth);
          }

          if (this.depth == null || this.depth > 0)
          {
            ServerGeoObjectIF parent = getParent(serverGo, this.hierarchyType.getCode());

            if (parent != null)
            {
              props.addProperty("parentId", parent.getExternalId(this.externalSystem));

              props.addProperty("externalParentId", parent.getCode());
            }
          }
        }
        joGO.add("properties", props);

        // joGO.addProperty("serverVersion", 0);
      }
      return joGO;
    }

    private Long formatDate(Date date)
    {
      if (date != null)
      {
        return date.getTime();
      }
      else
      {
        return null;
      }
    }

    public static ServerGeoObjectIF getParent(ServerGeoObjectIF serverGo, String hierarchyCode)
    {
      ServerParentTreeNode sptn = serverGo.getParentGeoObjects(null, false, null);

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
      if (!this.includeLevel)
      {
        return;
      }

      if (got.getUniversal().getParents(hierarchyType.getUniversalType()).getAll().size() > 1)
      {
        throw new UnsupportedOperationException("Multiple GeoObjectType parents not supported when 'includeLevel' is specified.");
      }

      List<ServerGeoObjectType> ancestors = this.got.getTypeAncestors(this.hierarchyType, true);

      this.depth = ancestors.size();
    }
  }
}
