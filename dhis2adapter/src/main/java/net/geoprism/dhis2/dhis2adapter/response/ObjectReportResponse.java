package net.geoprism.dhis2.dhis2adapter.response;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import net.geoprism.dhis2.dhis2adapter.response.model.ErrorReport;
import net.geoprism.dhis2.dhis2adapter.response.model.ObjectReport;

public class ObjectReportResponse extends DHIS2ImportResponse
{

  private ObjectReport objReport;
  
  public ObjectReportResponse(String response, int statusCode)
  {
    super(response, statusCode);
    
    init();
  }
  
  public ObjectReportResponse(DHIS2ImportResponse http)
  {
    super(http.getResponse(), http.getStatusCode());
    
    init();
  }
  
  private void init()
  {
    if (this.getJsonObject() != null && this.getJsonObject().has("response"))
    {
      GsonBuilder builder = new GsonBuilder();
      Gson gson = builder.create();
      
      Type tt = new TypeToken<ObjectReport>() {}.getType();
      
      this.objReport =  gson.fromJson(this.getJsonObject().get("response"), tt);
    }
  }
  
  public Boolean hasErrorReports()
  {
    if (this.objReport != null)
    {
      return this.objReport.hasErrorReports();
    }
    
    return false;
  }
  
  public List<ErrorReport> getErrorReports()
  {
    if (this.objReport != null)
    {
      return this.objReport.getErrorReports();
    }
    
    return null;
  }

  public ObjectReport getObjectReport()
  {
    return this.objReport;
  }
}
