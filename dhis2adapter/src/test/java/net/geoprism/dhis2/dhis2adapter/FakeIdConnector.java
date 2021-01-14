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
package net.geoprism.dhis2.dhis2adapter;

import java.util.List;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2Response;

public class FakeIdConnector extends AbstractTestConnector
{

  public FakeIdConnector(String versionResponse)
  {
    super(versionResponse);
  }

  @Override
  public DHIS2Response httpGetSubclass(String string, List<NameValuePair> params) throws InvalidLoginException, HTTPException
  {
    Integer limit = null;
    
    for (int i = 0; i < params.size(); ++i)
    {
      NameValuePair param = params.get(i);
      
      if (param.getName().equals("limit"))
      {
        limit = Integer.valueOf(param.getValue());
      }
    }
    
    JsonArray codes = new JsonArray();
    
    for (int i = 0; i < limit; ++i)
    {
      codes.add(UUID.randomUUID().toString().substring(0, 11));
    }
    
    JsonObject resp = new JsonObject();
    resp.add("codes", codes);
    
    return new DHIS2Response(resp.toString(), 200);
  }

}
