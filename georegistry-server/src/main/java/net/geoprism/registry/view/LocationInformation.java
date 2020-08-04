/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.view;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectJsonAdapters;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.HierarchyType.HierarchyNode;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.geoprism.registry.model.ServerChildTreeNode;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;

public class LocationInformation
{
  public static class LocationInformationSerializer implements JsonSerializer<LocationInformation>
  {
    private CustomSerializer serializer;

    public LocationInformationSerializer(CustomSerializer serializer)
    {
      this.serializer = serializer;
    }

    @Override
    public JsonElement serialize(LocationInformation obj, Type typeOfSrc, JsonSerializationContext context)
    {
      JsonObject response = new JsonObject();

      JsonArray types = new JsonArray();

      for (GeoObjectType type : obj.childTypes)
      {
        types.add(type.toJSON(serializer));
      }

      JsonArray hierarchies = new JsonArray();

      for (HierarchyType type : obj.hierarchies)
      {
        hierarchies.add(type.toJSON(serializer));
      }

      response.add("types", types);
      response.add("hierarchies", hierarchies);
      response.addProperty("hierarchy", obj.hierarchy);

      if (obj.entity != null)
      {
        response.add("entity", obj.entity.toJSON(this.serializer));
      }

      if (obj.childType != null)
      {
        response.addProperty("childType", obj.childType.getCode());
      }

      JsonArray features = new JsonArray();

      for (GeoObject child : obj.children)
      {
        features.add(context.serialize(child));
      }

      JsonObject featureCollection = new JsonObject();
      featureCollection.addProperty("type", "FeatureCollection");
      featureCollection.add("features", features);

      response.add("geojson", featureCollection);

      return response;
    }
  }

  private List<GeoObject>     children;

  private List<GeoObjectType> childTypes;

  private List<HierarchyType> hierarchies;

  private String              hierarchy;

  private GeoObject           entity;

  private GeoObjectType       childType;

  public LocationInformation()
  {
    this.children = new LinkedList<GeoObject>();
    this.childTypes = new LinkedList<GeoObjectType>();
  }

  public void addChild(GeoObject child)
  {
    this.children.add(child);
  }

  public void setChildren(List<ServerChildTreeNode> nodes)
  {
    this.children = new LinkedList<GeoObject>();

    for (ServerChildTreeNode node : nodes)
    {
      GeoObject geoObject = node.getGeoObject().toGeoObject();
      this.children.add(geoObject);
    }
  }

  public List<GeoObject> getChildren()
  {
    return children;
  }

  public void setChildTypes(List<ServerGeoObjectType> childTypes)
  {
    this.childTypes = new LinkedList<GeoObjectType>();

    for (ServerGeoObjectType node : childTypes)
    {
      this.childTypes.add(node.getType());
    }
  }

  public void setChildTypesFromNodes(List<HierarchyNode> nodes)
  {
    this.childTypes = new LinkedList<GeoObjectType>();

    for (HierarchyNode node : nodes)
    {
      this.childTypes.add(node.getGeoObjectType());
    }
  }

  public List<GeoObjectType> getChildTypes()
  {
    return childTypes;
  }

  public void setHierarchies(HierarchyType[] hierarchies)
  {
    this.hierarchies = new LinkedList<HierarchyType>();

    for (HierarchyType hierarchy : hierarchies)
    {
      this.hierarchies.add(hierarchy);
    }
  }

  public void setHierarchies(List<ServerHierarchyType> hierarchies)
  {
    this.hierarchies = new LinkedList<HierarchyType>();

    for (ServerHierarchyType node : hierarchies)
    {
      this.hierarchies.add(node.getType());
    }
  }

  public List<HierarchyType> getHierarchies()
  {
    return hierarchies;
  }

  public void setHierarchy(String hierarchy)
  {
    this.hierarchy = hierarchy;
  }

  public String getHierarchy()
  {
    return hierarchy;
  }

  public void setEntity(GeoObject entity)
  {
    this.entity = entity;
  }

  public GeoObject getEntity()
  {
    return entity;
  }

  public void setChildType(GeoObjectType childType)
  {
    this.childType = childType;
  }

  public GeoObjectType getChildType()
  {
    return childType;
  }

  public JsonElement toJson(CustomSerializer serializer)
  {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(GeoObject.class, new GeoObjectJsonAdapters.GeoObjectSerializer());
    builder.registerTypeAdapter(LocationInformation.class, new LocationInformationSerializer(serializer));

    return builder.create().toJsonTree(this, this.getClass());
  }
}
