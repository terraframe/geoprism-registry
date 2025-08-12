package net.geoprism.registry.jobs;

import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.scheduler.JobHistory;

public class RowValidationProblem extends RowValidationProblemBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 659627473;
  
  public static final String TYPE = "RowValidationProblem";
  
  public RowValidationProblem()
  {
    super();
  }
  
  public RowValidationProblem(Throwable exception)
  {
    this.setExceptionJson(JobHistory.exceptionToJson(exception).toString());
  }

  @Override
  public String buildKey()
  {
    JSONObject err = new JSONObject(this.getExceptionJson());
    
    return this.getValidationProblemType() + "-" + this.getHistoryOid() + "-" + err.getString("message");
  }
  
  @Override
  public String getValidationProblemType()
  {
    return TYPE;
  }

  @Override
  public JsonObject toJSON()
  {
    JsonObject object = super.toJSON();
    
    JsonObject errJson = JsonParser.parseString(this.getExceptionJson()).getAsJsonObject();
    
    JsonObject exception = new JsonObject();
    exception.addProperty("type", errJson.get("type").getAsString());
    exception.addProperty("message", JobHistory.readLocalizedException(new JSONObject(this.getExceptionJson()), Session.getCurrentLocale()));
    
    object.add("exception", exception);

    return object;
  }
  
  @Override
  public void apply()
  {
    if (this.getSeverity() == null || this.getSeverity() == 0)
    {
      this.setSeverity(100);
    }
    
    super.apply();
  }
  
}
