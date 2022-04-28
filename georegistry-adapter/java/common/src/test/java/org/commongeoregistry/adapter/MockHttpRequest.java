/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Common Geo Registry Adapter(tm).
 *
 * Common Geo Registry Adapter(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Common Geo Registry Adapter(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Common Geo Registry Adapter(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package org.commongeoregistry.adapter;

import java.util.Map;

import org.commongeoregistry.adapter.http.HttpResponse;

public class MockHttpRequest
{
  private String              url;

  private Map<String, String> params;

  private String              body;

  private HttpResponse        response;
  
  private boolean             hasExecuted;

  public MockHttpRequest()
  {
    this.initialize(null);
  }
  
  public MockHttpRequest(HttpResponse response)
  {
    this.initialize(response);
  }
  
  private void initialize(HttpResponse response)
  {
    this.hasExecuted = false;
    this.response = response;
  }
  
  public boolean hasExecuted()
  {
    return hasExecuted;
  }

  public void setHasExecuted(boolean hasExecuted)
  {
    this.hasExecuted = hasExecuted;
  }

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public Map<String, String> getParams()
  {
    return params;
  }

  public void setParams(Map<String, String> params)
  {
    this.params = params;
  }

  public String getBody()
  {
    return body;
  }

  public void setBody(String body)
  {
    this.body = body;
  }

  public HttpResponse getResponse()
  {
    return response;
  }

  public void setResponse(HttpResponse response)
  {
    this.response = response;
  }
}
