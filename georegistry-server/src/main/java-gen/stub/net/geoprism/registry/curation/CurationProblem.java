package net.geoprism.registry.curation;

import com.google.gson.JsonObject;

import net.geoprism.registry.view.JsonSerializable;

public class CurationProblem extends CurationProblemBase implements JsonSerializable
{
  private static final long serialVersionUID = -1322764109;

  public static enum CurationResolution {
    RESOLVED, UNRESOLVED
  }

  public CurationProblem()
  {
    super();
  }

  public JsonObject toJSON()
  {
    JsonObject json = new JsonObject();

    json.addProperty("resolution", this.getResolution());
    json.addProperty("historyId", this.getHistory().getOid());
    json.addProperty("type", this.getProblemType());
    json.addProperty("id", this.getOid());

    return json;
  }

}
