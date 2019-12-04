package net.geoprism.registry.model;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ServerParentTreeNodeOverTime
{
  private Map<String, List<ServerParentTreeNode>> hierarchies;

  public ServerParentTreeNodeOverTime(Map<String, List<ServerParentTreeNode>> hierarchies)
  {
    this.hierarchies = hierarchies;
  }

  public JsonArray toJSON()
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    format.setTimeZone(TimeZone.getTimeZone("GMT"));

    final JsonArray response = new JsonArray();

    final Set<Entry<String, List<ServerParentTreeNode>>> entrySet = this.hierarchies.entrySet();

    for (Entry<String, List<ServerParentTreeNode>> entry : entrySet)
    {
      final List<ServerParentTreeNode> nodes = entry.getValue();

      final JsonArray entries = new JsonArray();

      for (ServerParentTreeNode node : nodes)
      {
        JsonObject object = new JsonObject();
        object.addProperty("startDate", format.format(node.getDate()));
        object.add("ptn", node.toNode().toJSON());

        entries.add(object);
      }

      JsonObject object = new JsonObject();
      object.addProperty("hierarchy", entry.getKey());
      object.add("entries", entries);
    }

    return response;
  }
}
