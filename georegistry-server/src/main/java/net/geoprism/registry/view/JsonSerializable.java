package net.geoprism.registry.view;

import com.google.gson.JsonElement;

public interface JsonSerializable
{
  public JsonElement toJSON();
}
