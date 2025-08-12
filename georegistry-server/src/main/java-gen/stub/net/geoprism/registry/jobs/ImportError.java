package net.geoprism.registry.jobs;

import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.scheduler.JobHistory;

import net.geoprism.registry.view.JsonSerializable;

public class ImportError extends ImportErrorBase implements JsonSerializable
{
  private static final long serialVersionUID = 1410792643;
  
  public static enum ErrorResolution
  {
    IGNORE,
    APPLY_GEO_OBJECT,
    UNRESOLVED
  }
  
  public ImportError()
  {
    super();
  }
  
  public static ImportErrorQuery queryResolutionStatus(String historyId, String resolutionStatus)
  {
    ImportErrorQuery ieq = new ImportErrorQuery(new QueryFactory());
    
    ieq.WHERE(ieq.getResolution().EQ(resolutionStatus));
    
    return ieq;
  }
  
  public JsonObject toJSON()
  {
    JsonObject jo = new JsonObject();
    
    JsonObject errJson = JsonParser.parseString(this.getErrorJson()).getAsJsonObject();
    
    JsonObject exception = new JsonObject();
    exception.addProperty("type", errJson.get("type").getAsString());
    exception.addProperty("message", JobHistory.readLocalizedException(new JSONObject(this.getErrorJson()), Session.getCurrentLocale()));
    jo.add("exception", exception);
    
    if (this.getObjectJson() != null && this.getObjectJson().length() > 0)
    {
      jo.add("object", JsonParser.parseString(this.getObjectJson()));
    }
    
    jo.addProperty("objectType", this.getObjectType());
    
    jo.addProperty("id", this.getOid());
    
    jo.addProperty("resolution", this.getResolution());
    
    jo.addProperty("rowNum", this.getRowIndex());
    
    return jo;
  }
  
  @Override
  public void apply()
  {
    if (this.getResolution().equals(""))
    {
      this.setResolution(ErrorResolution.UNRESOLVED.name());
    }
    
    super.apply();
  }
  
}
