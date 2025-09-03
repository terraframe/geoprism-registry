package net.geoprism.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeAndCode.Type;

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
    configuration.setUid(this.getUid());

    JsonArray types = JsonParser.parseString(this.getTypeCodes()).getAsJsonArray();

    types.forEach(element -> {
      JsonObject object = element.getAsJsonObject();

      Type type = Type.valueOf(object.get("type").getAsString());
      String code = object.get("code").getAsString();

      configuration.addType(type, code);
    });

    JsonArray exclusions = JsonParser.parseString(this.getExclusions()).getAsJsonArray();
    
    exclusions.forEach(element -> {
      JsonObject object = element.getAsJsonObject();
      
      Type type = Type.valueOf(object.get("type").getAsString());
      String code = object.get("code").getAsString();
      
      configuration.addExclusions(type, code);
    });
    
    return configuration;
  }

  public boolean hasSameTypes(PublishDTO configuration)
  {
    return this.toDTO().hasSameTypes(configuration);
  }

}
