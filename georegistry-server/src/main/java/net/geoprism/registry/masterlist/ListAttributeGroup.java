package net.geoprism.registry.masterlist;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ListAttributeGroup implements ListColumn
{
  private String           label;

  private String           name;

  private List<ListColumn> columns;

  public ListAttributeGroup()
  {
    this("", null);
  }

  public ListAttributeGroup(String label)
  {
    this(label, null);
  }

  public ListAttributeGroup(String label, String name)
  {
    this.label = label;
    this.name = name;
    this.columns = new LinkedList<ListColumn>();
  }

  public void add(ListColumn column)
  {
    this.columns.add(column);
  }

  @Override
  public JsonObject toJSON()
  {
    JsonArray array = this.columns.stream().map(m -> m.toJSON()).collect(() -> new JsonArray(), (a, e) -> a.add(e), (listA, listB) -> {
    });

    JsonObject object = new JsonObject();
    object.addProperty("label", label);
    object.addProperty("colspan", this.getNumberOfColumns());
    object.add("columns", array);

    if (this.name != null)
    {
      object.addProperty("name", this.name);
    }

    return object;
  }

  @Override
  public int getNumberOfColumns()
  {
    return this.columns.stream().map(m -> m.getNumberOfColumns()).reduce((a, b) -> a + b).orElseGet(() -> 0);
  }

  @Override
  public Set<String> getColumnsIds()
  {
    Set<String> set = new TreeSet<String>();

    this.columns.forEach(column -> set.addAll(column.getColumnsIds()));

    return set;
  }
}
