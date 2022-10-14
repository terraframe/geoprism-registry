package net.geoprism.dhis2.dhis2adapter.response.model;

import java.util.ArrayList;
import java.util.List;

public class ImportReport
{
  private String responseType;
  
  private List<TypeReport> typeReports;
  
  private Stats stats;
  
  private String status;

  public String getResponseType()
  {
    return responseType;
  }

  public void setResponseType(String responseType)
  {
    this.responseType = responseType;
  }

  public List<TypeReport> getTypeReports()
  {
    if (this.typeReports != null) {
      return typeReports;
    } else {
      return new ArrayList<TypeReport>();
    }
  }

  public void setTypeReports(List<TypeReport> typeReports)
  {
    this.typeReports = typeReports;
  }
  
  public boolean hasTypeReports()
  {
    return this.typeReports != null && this.typeReports.size() > 0; 
  }
}