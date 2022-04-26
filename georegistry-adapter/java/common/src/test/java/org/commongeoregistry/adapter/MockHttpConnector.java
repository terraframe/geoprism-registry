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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.commongeoregistry.adapter.http.AbstractHttpConnector;
import org.commongeoregistry.adapter.http.HttpResponse;

public class MockHttpConnector extends AbstractHttpConnector
{
  private ArrayList<MockHttpRequest> requests;
  
  private int current;

  public MockHttpConnector()
  {
    super();
    
    this.requests = new ArrayList<MockHttpRequest>();
  }

  public MockHttpConnector(MockHttpRequest[] requests)
  {
    super();
    
    this.current = 0;
    
    this.requests = new ArrayList<MockHttpRequest>();
    for (MockHttpRequest request : requests)
    {
      this.requests.add(request);
    }
  }

  public String getUrl()
  {
    return this.requests.get(this.current-1).getUrl();
  }

  public Map<String, String> getParams()
  {
    return this.requests.get(this.current-1).getParams();
  }

  public String getBody()
  {
    return this.requests.get(this.current-1).getBody();
  }
  
  public MockHttpRequest getRequest()
  {
    if (this.current >= this.requests.size())
    {
      MockHttpRequest req = new MockHttpRequest();
      this.requests.add(req);
      return req;
    }
    
    return this.requests.get(this.current);
  }
  
  public List<MockHttpRequest> getRequests()
  {
    return this.requests;
  }

  public HttpResponse getResponse()
  {
    return this.requests.get(this.current-1).getResponse();
  }

  public void setNextRequest(MockHttpRequest request)
  {
    this.requests.add(request);
    this.current = this.requests.size()-1;
  }

  @Override
  public HttpResponse httpGet(String url, Map<String, String> params)
  {
    MockHttpRequest curReq = this.getRequest();
    
    curReq.setUrl(url);
    curReq.setParams(params);

    HttpResponse resp = curReq.getResponse();
    this.current++;
    return resp;
  }

  @Override
  public HttpResponse httpPost(String url, String body)
  {
    MockHttpRequest curReq = this.getRequest();
    
    curReq.setUrl(url);
    curReq.setBody(body);

    HttpResponse resp = curReq.getResponse();
    this.current++;
    return resp;
  }

}
