package net.geoprism.registry.etl;

import org.json.JSONObject;

import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.scheduler.JobHistory;

public class ImportError extends ImportErrorBase
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
  
  public JSONObject toJSON()
  {
    JSONObject jo = new JSONObject();
    
    JSONObject exception = new JSONObject();
    exception.put("type", new JSONObject(this.getErrorJson()).get("type"));
    exception.put("message", JobHistory.readLocalizedException(new JSONObject(this.getErrorJson()), Session.getCurrentLocale()));
    jo.put("exception", exception);
    
    if (this.getObjectJson() != null && this.getObjectJson().length() > 0)
    {
      jo.put("object", new JSONObject(this.getObjectJson()));
    }
    
    jo.put("objectType", this.getObjectType());
    
    jo.put("id", this.getOid());
    
    jo.put("resolution", this.getResolution());
    
    jo.put("rowNum", this.getRowIndex());
    
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
