/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.dhis2.dhis2adapter.response;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import net.geoprism.dhis2.dhis2adapter.DHIS2Constants;
import net.geoprism.dhis2.dhis2adapter.response.model.ErrorReport;
import net.geoprism.dhis2.dhis2adapter.response.model.ImportReport;
import net.geoprism.dhis2.dhis2adapter.response.model.TypeReport;

public class ImportReportResponse extends DHIS2ImportResponse
{

  private ImportReport importReport;
  
  public ImportReportResponse(String response, int statusCode)
  {
    super(response, statusCode);
    
    init();
  }
  
  public ImportReportResponse(DHIS2ImportResponse http)
  {
    super(http.getResponse(), http.getStatusCode());
    
    init();
  }
  
  private void init()
  {
    if (this.getJsonObject() != null && this.getJsonObject().has("response"))
    {
      GsonBuilder builder = new GsonBuilder();
      builder.setDateFormat(DHIS2Constants.DATE_FORMAT);
      Gson gson = builder.create();
      
      Type tt = new TypeToken<ImportReport>() {}.getType();
      
      this.importReport =  gson.fromJson(this.getJsonObject().get("response"), tt);
    }
  }
  
  public Boolean hasTypeReports()
  {
    if (this.importReport != null)
    {
      return this.importReport.hasTypeReports();
    }
    
    return false;
  }
  
  public List<TypeReport> getTypeReports()
  {
    if (this.importReport != null)
    {
      return this.importReport.getTypeReports();
    }
    
    return new ArrayList<TypeReport>();
  }

  public ImportReport getImportReport()
  {
    return this.importReport;
  }
  
  @Override
  public Boolean hasErrorReports()
  {
    return this.getErrorReports().size() > 0;
  }
  
  @Override
  public List<ErrorReport> getErrorReports()
  {
    List<ErrorReport> reports = new ArrayList<ErrorReport>();
    
    for (TypeReport tr : this.getTypeReports()) {
      reports.addAll(tr.getErrorReports());
    }
    
    return reports;
  }
  
  @Override
  public String getMessage()
  {
    List<ErrorReport> er = this.getErrorReports();
    
    if (er.size() > 0) {
      return er.get(0).getMessage();
    }
    
    return super.getMessage();
  }
  
  @Override
  public boolean hasMessage()
  {
    return this.getMessage() != null;
  }
}
