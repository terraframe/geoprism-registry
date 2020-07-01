package net.geoprism.dhis2.dhis2adapter.response;

public class MetadataImportResponse extends DHIS2Response
{

  public MetadataImportResponse(String response, int statusCode)
  {
    super(response, statusCode);
  }
  
  public MetadataImportResponse(DHIS2Response http)
  {
    super(http.getResponse(), http.getStatusCode());
  }
  
//  public static class Stats
//  {
//    
//  }

}
