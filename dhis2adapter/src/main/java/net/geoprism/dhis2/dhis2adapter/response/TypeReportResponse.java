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
import com.google.gson.reflect.TypeToken;

import net.geoprism.dhis2.dhis2adapter.DHIS2Constants;
import net.geoprism.dhis2.dhis2adapter.response.model.ErrorReport;
import net.geoprism.dhis2.dhis2adapter.response.model.TypeReport;

public class TypeReportResponse extends DHIS2ImportResponse
{

  private TypeReport typeReport;
  
  public TypeReportResponse(String response, int statusCode)
  {
    super(response, statusCode);
    
    init();
  }
  
  public TypeReportResponse(DHIS2ImportResponse http)
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
      
      Type tt = new TypeToken<TypeReport>() {}.getType();
      
      this.typeReport =  gson.fromJson(this.getJsonObject().get("response"), tt);
    }
  }

  public TypeReport getTypeReport()
  {
    return this.typeReport;
  }
}
