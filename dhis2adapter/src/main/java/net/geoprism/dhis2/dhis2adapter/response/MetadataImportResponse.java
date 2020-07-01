package net.geoprism.dhis2.dhis2adapter.response;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.geoprism.dhis2.dhis2adapter.response.model.ErrorReport;
import net.geoprism.dhis2.dhis2adapter.response.model.ImportParams;
import net.geoprism.dhis2.dhis2adapter.response.model.Stats;
import net.geoprism.dhis2.dhis2adapter.response.model.TypeReport;

public class MetadataImportResponse extends DHIS2ImportResponse
{

  private ImportParams importParams;
  
  private Stats stats;
  
  private List<TypeReport> typeReports;
  
  public MetadataImportResponse(String response, int statusCode)
  {
    super(response, statusCode);
    
    init();
  }
  
  public MetadataImportResponse(DHIS2ImportResponse http)
  {
    super(http.getResponse(), http.getStatusCode());
    
    init();
  }
  
  private void init()
  {
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();
    
    if (this.getJsonObject() != null && this.getJsonObject().has("typeReports"))
    {
      Type list = new TypeToken<List<TypeReport>>() {}.getType();
      this.typeReports =  gson.fromJson(this.getJsonObject().get("typeReports"), list);
    }
    
    if (this.getJsonObject() != null && this.getJsonObject().has("stats"))
    {
      this.stats = gson.fromJson(this.getJsonObject().get("stats"), Stats.class);
    }
    
    if (this.getJsonObject() != null && this.getJsonObject().has("importParams"))
    {
      this.importParams = gson.fromJson(this.getJsonObject().get("importParams"), ImportParams.class);
    }
  }

  public ImportParams getImportParams()
  {
    return importParams;
  }

  public void setImportParams(ImportParams importParams)
  {
    this.importParams = importParams;
  }

  public Stats getStats()
  {
    return stats;
  }

  public void setStats(Stats stats)
  {
    this.stats = stats;
  }

  public List<TypeReport> getTypeReports()
  {
    return typeReports;
  }

  public void setTypeReports(List<TypeReport> typeReports)
  {
    this.typeReports = typeReports;
  }
  
  public Boolean hasErrorReports()
  {
    if (this.typeReports != null)
    {
      for (TypeReport tr : this.typeReports)
      {
        if (tr.hasErrorReports())
        {
          return true;
        }
      }
    }
    
    return false;
  }
  
  public List<ErrorReport> getErrorReports()
  {
    List<ErrorReport> reports = new ArrayList<ErrorReport>();
    
    for (TypeReport tr : this.typeReports)
    {
      if (tr.hasErrorReports())
      {
        reports.addAll(tr.getErrorReports());
      }
    }
    
    return reports;
  }

}
