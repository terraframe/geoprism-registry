package net.geoprism.registry.curation;

import com.google.gson.JsonObject;

public class CurationProblem extends CurationProblemBase
{
  private static final long serialVersionUID = -1322764109;
  
  public static enum CurationResolution
  {
    RESOLVED,
    UNRESOLVED
  }
  
  public CurationProblem()
  {
    super();
  }
  
  public JsonObject toJson()
  {
    JsonObject json = new JsonObject();
    
    json.addProperty("affectedRows", this.getAffectedRows());
    json.addProperty("resolution", this.getResolution());
    json.addProperty("historyId", this.getHistory().getOid());
    json.addProperty("type", this.getProblemType());
    json.addProperty("id", this.getOid());
    
    return json;
  }
  
}
