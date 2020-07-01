package net.geoprism.dhis2.dhis2adapter.response;

import java.util.Arrays;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class DHIS2Response
{
  protected String response;
  
  protected int statusCode;
  
  public DHIS2Response(String response, int statusCode)
  {
    this.response = response;
    this.statusCode = statusCode;
  }
  
  public boolean isSuccess()
  {
    final int[] successCodes = new int[] {200, 201, 202, 203, 204, 205, 206, 207, 208, 226};
    
    boolean isSuccessCode = Arrays.stream(successCodes).anyMatch(new Integer(this.getStatusCode())::equals);
    
    return isSuccessCode;
  }

  public JsonObject getJsonObject()
  {
    if (this.response == null)
    {
      return null;
    }
    
    try
    {
      return JsonParser.parseString(response).getAsJsonObject();
    }
    catch (JsonParseException e)
    {
      return null;
    }
  }
  
  public JsonArray getJsonArray()
  {
    if (this.response == null)
    {
      return null;
    }
    
    try
    {
      return JsonParser.parseString(response).getAsJsonArray();
    }
    catch (JsonParseException e)
    {
      return null;
    }
  }

  public String getResponse()
  {
    return this.response;
  }
  
  public void setResponse(String response)
  {
    this.response = response;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }
}
