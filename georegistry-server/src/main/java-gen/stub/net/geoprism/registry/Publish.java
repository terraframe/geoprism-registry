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

      PublishDTO.Type type = PublishDTO.Type.valueOf(object.get("type").getAsString());
      String code = object.get("code").getAsString();
      
      configuration.addType(type, code);
    });

    return configuration;
  }

  public boolean hasSameTypes(PublishDTO configuration)
  {
    return this.toDTO().hasSameTypes(configuration);
  }

}
