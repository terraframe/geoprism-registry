package net.geoprism.dhis2.dhis2adapter.response;

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
  
  public String getError()
  {
    return this.getResponse(); // TODO
  }
  
//  public static class Stats
//  {
//    
//  }

}
