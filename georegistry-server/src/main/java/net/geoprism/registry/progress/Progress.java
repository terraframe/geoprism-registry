package net.geoprism.registry.progress;

import com.google.gson.JsonObject;

public class Progress
{
  private Long   current;

  private Long   total;

  private String description;

  public Progress(Long current, Long total, String description)
  {
    super();
    this.current = current;
    this.total = total;
    this.description = description;
  }

  public JsonObject toJson()
  {
    JsonObject object = new JsonObject();
    object.addProperty("current", this.current);
    object.addProperty("total", this.total);
    object.addProperty("description", this.description);

    return object;
  }

}
