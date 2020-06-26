package net.geoprism.dhis2.dhis2adapter.response;

import com.google.gson.JsonObject;

public class MetadataImportResponse extends HTTPResponse
{

  public MetadataImportResponse(String response, int statusCode)
  {
    super(response, statusCode);
  }
  
  public MetadataImportResponse(HTTPResponse http)
  {
    super(http.getResponse(), http.getStatusCode());
  }
  
//  public static class Stats
//  {
//    
//  }

}
