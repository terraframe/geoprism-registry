package net.geoprism.registry.graph;

import org.commongeoregistry.adapter.dataaccess.GeoObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.system.metadata.MdEdge;
import com.runwaysdk.system.metadata.MdVertex;

import net.geoprism.registry.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.ServiceFactory;

public class JsonGraphVersionPublisher extends AbstractGraphVersionPublisher
{
  private LabeledPropertyGraphTypeVersion version;

  public JsonGraphVersionPublisher(LabeledPropertyGraphTypeVersion version)
  {
    this.version = version;
  }

  public void publish(JsonObject graph)
  {
    JsonArray array = graph.get("geoObjects").getAsJsonArray();

    for (int i = 0; i < array.size(); i++)
    {
      JsonObject object = array.get(i).getAsJsonObject();

      GeoObject geoObject = GeoObject.fromJSON(ServiceFactory.getAdapter(), object.toString());

      ServerGeoObjectType type = ServerGeoObjectType.get(geoObject.getType());

      MdVertex mdVertex = this.version.getSnapshot(type).getGraphMdVertex();

      this.publish(mdVertex, geoObject);
    }

    JsonArray edges = graph.get("edges").getAsJsonArray();

    for (int i = 0; i < edges.size(); i++)
    {
      JsonObject object = edges.get(i).getAsJsonObject();
      String parentUid = object.get("startNode").getAsString();
      String parentType = object.get("startType").getAsString();
      String childUid = object.get("endNode").getAsString();
      String childType = object.get("endType").getAsString();
      String typeCode = object.get("type").getAsString();

      ServerHierarchyType hierarchy = ServerHierarchyType.get(typeCode);

      MdEdge mdEdge = this.version.getSnapshot(hierarchy).getGraphMdEdge();

      VertexObject parent = version.getVertex(parentUid, parentType);
      VertexObject child = version.getVertex(childUid, childType);

      parent.addChild(child, mdEdge.definesType()).apply();
    }
  }

}
