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
package net.geoprism.dhis2.dhis2adapter;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2Response;

public class TestSingleResponseConnector extends AbstractTestConnector
{
  
  private String response;
  
  private int statusCode;
  
  public TestSingleResponseConnector(String response, String versionResponse, int statusCode)
  {
    super(versionResponse);
    
    this.response = response;
    this.statusCode = statusCode;
  }
  
  @Override
  public DHIS2Response httpGetSubclass(String url, List<NameValuePair> params) throws InvalidLoginException, HTTPException
  {
    return new DHIS2Response(this.response, this.statusCode);
  }

  @Override
  public DHIS2Response httpPost(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException
  {
    return new DHIS2Response(this.response, this.statusCode);
  }

  @Override
  public DHIS2Response httpPatch(String string, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException
  {
    return new DHIS2Response(this.response, this.statusCode);
  }
  
}
