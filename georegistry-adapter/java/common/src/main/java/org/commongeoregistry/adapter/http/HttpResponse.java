/**
 *
 */
package org.commongeoregistry.adapter.http;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HttpResponse
{
  private String response;

  private int    statusCode;

  public HttpResponse(String sResp, int statusCode)
  {
    this.response = sResp;
    this.statusCode = statusCode;
  }

  public JsonObject getAsJsonObject()
  {
    JsonParser parser = new JsonParser();

    return parser.parse(this.response).getAsJsonObject();
  }

  public JsonArray getAsJsonArray()
  {
    JsonParser parser = new JsonParser();

    return parser.parse(this.response).getAsJsonArray();
  }

  public String getAsString()
  {
    return this.response;
  }

  public int getStatusCode()
  {
    return statusCode;
  }

  public void setStatusCode(int statusCode)
  {
    this.statusCode = statusCode;
  }
  
  @Override
  public String toString()
  {
    String status = "HTTPResponse [" + this.statusCode + "]";
    
    if (this.response.length() > 300)
    {
      return status;
    }
    else
    {
      return status + " : [" + this.response + "]";
    }
  }
}
