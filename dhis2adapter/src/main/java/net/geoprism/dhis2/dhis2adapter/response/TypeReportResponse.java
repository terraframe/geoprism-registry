package net.geoprism.dhis2.dhis2adapter.response;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.geoprism.dhis2.dhis2adapter.response.model.ErrorReport;
import net.geoprism.dhis2.dhis2adapter.response.model.ObjectReport;
import net.geoprism.dhis2.dhis2adapter.response.model.TypeReport;

public class TypeReportResponse extends DHIS2Response
{

  private TypeReport typeReport;
  
  public TypeReportResponse(String response, int statusCode)
  {
    super(response, statusCode);
    
    init();
  }
  
  public TypeReportResponse(DHIS2Response http)
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
      
      Type tt = new TypeToken<TypeReport>() {}.getType();
      
      this.typeReport =  gson.fromJson(this.getJsonObject().get("response"), tt);
    }
  }
  
  public Boolean hasErrorReports()
  {
    if (this.typeReport != null)
    {
      return this.typeReport.hasErrorReports();
    }
    
    return false;
  }
  
  public List<ErrorReport> getErrorReports()
  {
    if (this.typeReport != null)
    {
      return this.typeReport.getErrorReports();
    }
    
    return null;
  }

  public TypeReport getTypeReport()
  {
    return this.typeReport;
  }
}
