package net.geoprism.registry;

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

      if (type.equals("GeoObjectType"))
      {
        configuration.addGeoObjectType(code);
      }
      else if (type.equals("HierarchyType"))
      {
        configuration.addHierarchyType(code);
      }

    });

    return configuration;
  }

}
