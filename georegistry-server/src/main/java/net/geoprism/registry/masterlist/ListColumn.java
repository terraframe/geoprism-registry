package net.geoprism.registry.masterlist;

import java.util.Set;

import com.google.gson.JsonObject;

public interface ListColumn
{
  public JsonObject toJSON();

  public int getNumberOfColumns();

  public int getRowspan();

  public Set<String> getColumnsIds();
}
