/**
 *
 */
package org.commongeoregistry.adapter.metadata;

import java.util.Collection;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public interface CustomSerializer
{
  public Collection<AttributeType> attributes(GeoObjectType type);

  public JsonArray serialize(GeoObjectType type, Collection<AttributeType> attributes);

  public void configure(GeoObjectType type, JsonObject json);

  public void configure(HierarchyType type, JsonObject json);

  public void configure(AttributeType attributeType, JsonObject json);

  public void configure(LocalizedValue localizedValue, JsonObject object);

}
