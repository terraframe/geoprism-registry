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
import net.geoprism.dhis2.dhis2adapter.response.model.DHIS2Locale;

public class LocaleGetResponse extends DHIS2Response
{

  private List<DHIS2Locale> locales = new ArrayList<DHIS2Locale>();
  
  public LocaleGetResponse(String response, int statusCode)
  {
    super(response, statusCode);
    
    init();
  }
  
  public LocaleGetResponse(DHIS2Response http)
  {
    super(http.getResponse(), http.getStatusCode());
    
    init();
  }
  
  // https://exceptionshub.com/java-type-generic-as-argument-for-gson.html
  private void init()
  {
    GsonBuilder builder = new GsonBuilder();
    builder.setDateFormat(DHIS2Constants.DATE_FORMAT);
    Gson gson = builder.create();
    
    if (this.getJsonArray() != null)
    {
      Type list = TypeToken.getParameterized(List.class, DHIS2Locale.class).getType();
      this.locales =  gson.fromJson(this.getJsonArray(), list);
    }
  }

  public List<DHIS2Locale> getLocales()
  {
    return this.locales;
  }

  public void setLocales(List<DHIS2Locale> locales)
  {
    this.locales = locales;
  }
  
}
