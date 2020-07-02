/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
