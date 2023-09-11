/**
 *
 */
package org.commongeoregistry.adapter.metadata;

import java.util.Collection;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DefaultSerializer implements CustomSerializer
{

  @Override
  public JsonArray serialize(GeoObjectType type, Collection<AttributeType> attributes)
  {
    JsonArray attrs = new JsonArray();

    for (AttributeType attribute : attributes)
    {
      attrs.add(attribute.toJSON(this));
    }

    return attrs;
  }

  @Override
  public Collection<AttributeType> attributes(GeoObjectType type)
  {
    return type.getAttributeMap().values();
  }

  @Override
  public void configure(AttributeType attributeType, JsonObject json)
  {
    // Do nothing
  }

  @Override
  public void configure(LocalizedValue localizedValue, JsonObject object)
  {
    // Do nothing
  }

  @Override
  public void configure(GeoObjectType type, JsonObject json)
  {
    // Do nothing
  }

  @Override
  public void configure(HierarchyType type, JsonObject json)
  {
    // Do nothing
  }

}
