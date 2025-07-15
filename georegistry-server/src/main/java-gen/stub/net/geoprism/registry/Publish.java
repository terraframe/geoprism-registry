package net.geoprism.registry;

import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.geoprism.registry.view.PublishDTO;

public class Publish extends PublishBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 827828570;

  public Publish()
  {
    super();
  }

  public PublishDTO toDTO()
  {
    PublishDTO configuration = new PublishDTO(this.getForDate(), this.getStartDate(), this.getEndDate());

    JsonArray array = JsonParser.parseString(this.getTypeCodes()).getAsJsonArray();

    array.forEach(element -> {
      JsonObject object = element.getAsJsonObject();

      String type = object.get("type").getAsString();
      String code = object.get("code").getAsString();

      if (type.equals(GeoObjectType.class.getSimpleName()))
      {
        configuration.addGeoObjectType(code);
      }
      else if (type.equals(HierarchyType.class.getSimpleName()))
      {
        configuration.addHierarchyType(code);
      }
      else if (type.equals(BusinessEdgeType.class.getSimpleName()))
      {
        configuration.addBusinessEdgeType(code);
      }
      else if (type.equals(BusinessType.class.getSimpleName()))
      {
        configuration.addBusinessType(code);
      }
      else if (type.equals(DirectedAcyclicGraphType.class.getSimpleName()))
      {
        configuration.addDagType(code);
      }
      else if (type.equals(UndirectedGraphType.class.getSimpleName()))
      {
        configuration.addUndirectedType(code);
      }

    });

    return configuration;
  }

}
