package net.geoprism.registry.view;

import com.google.gson.JsonElement;

public class JsonWrapper implements JsonSerializable
{
  private JsonElement element;

  public JsonWrapper(JsonElement element)
  {
    super();
    this.element = element;
  }

  @Override
  public JsonElement toJSON()
  {
    return this.element;
  }

}
