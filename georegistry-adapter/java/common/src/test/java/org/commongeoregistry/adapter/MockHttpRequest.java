/**
 *
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
