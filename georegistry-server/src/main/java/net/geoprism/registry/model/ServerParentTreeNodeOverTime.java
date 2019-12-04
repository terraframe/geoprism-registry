package net.geoprism.registry.model;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.DefaultSerializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.system.gis.geo.Universal;

import net.geoprism.ontology.GeoEntityUtil;

public class ServerParentTreeNodeOverTime
{
  private static class Hierarchy
  {
    private ServerHierarchyType        type;

    private List<ServerParentTreeNode> nodes;

    public Hierarchy(ServerHierarchyType type)
    {
      this.type = type;
      this.nodes = new LinkedList<ServerParentTreeNode>();
    }

    public ServerHierarchyType getType()
    {
      return type;
    }

    public void add(ServerParentTreeNode node)
    {
      this.nodes.add(node);
    }

    public List<ServerParentTreeNode> getNodes()
    {
      return nodes;
    }
  }

  private Map<String, Hierarchy> hierarchies;

  private ServerGeoObjectType    type;

  public ServerParentTreeNodeOverTime(ServerGeoObjectType type)
  {
    this.type = type;
    this.hierarchies = new TreeMap<String, ServerParentTreeNodeOverTime.Hierarchy>();
  }

  public void add(ServerHierarchyType type, ServerParentTreeNode node)
  {
    if (!this.hierarchies.containsKey(type.getCode()))
    {
      this.hierarchies.put(type.getCode(), new Hierarchy(type));
    }

    this.hierarchies.get(type.getCode()).add(node);
  }

  public JsonArray toJSON()
  {
    Universal root = Universal.getRoot();

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    format.setTimeZone(TimeZone.getTimeZone("GMT"));

    final JsonArray response = new JsonArray();

    final Set<Entry<String, Hierarchy>> entrySet = this.hierarchies.entrySet();

    for (Entry<String, Hierarchy> entry : entrySet)
    {
      final Hierarchy hierarchy = entry.getValue();
      final ServerHierarchyType ht = hierarchy.getType();

      final JsonArray entries = new JsonArray();

      Collection<?> uniParents = GeoEntityUtil.getOrderedAncestors(root, this.type.getUniversal(), ht.getUniversalType());

      final List<ServerParentTreeNode> nodes = hierarchy.getNodes();

      for (ServerParentTreeNode node : nodes)
      {
        JsonArray pArray = new JsonArray();

        for (Object parent : uniParents)
        {
          ServerGeoObjectType pType = ServerGeoObjectType.get((Universal) parent);

          if (!pType.getCode().equals(this.type.getCode()))
          {
            JsonObject pObject = new JsonObject();
            pObject.addProperty("code", pType.getCode());
            pObject.addProperty("label", pType.getLabel().getValue());

            final List<ServerParentTreeNode> ptns = node.findParentOfType(pType.getCode());

            if (ptns.size() > 0)
            {
              final GeoObject geoObject = ptns.get(0).getGeoObject().toGeoObject();
              geoObject.setGeometry(null);

              pObject.add("ptn", geoObject.toJSON());
            }

            pArray.add(pObject);
          }
        }

        JsonObject object = new JsonObject();
        object.addProperty("startDate", format.format(node.getDate()));
        object.add("parents", pArray);

        entries.add(object);
      }

      JsonObject object = new JsonObject();
      object.addProperty("code", ht.getCode());
      object.addProperty("label", ht.getDisplayLabel().getValue());
      object.add("entries", entries);

      response.add(object);
    }

    return response;
  }
}
