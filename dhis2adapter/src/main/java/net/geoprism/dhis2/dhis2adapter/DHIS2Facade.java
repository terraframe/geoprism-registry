package net.geoprism.dhis2.dhis2adapter;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

import net.geoprism.dhis2.dhis2adapter.response.HTTPResponse;
import net.geoprism.dhis2.dhis2adapter.response.MetadataImportResponse;

public class DHIS2Facade
{
  private String version;
  
  private HTTPConnector connector;
  
  public DHIS2Facade(HTTPConnector connector, String version)
  {
    this.connector = connector;
    this.version = version;
  }
  
  public HTTPResponse systemInfo() throws InvalidLoginException, HTTPException
  {
    return this.apiGet("system/info", null);
  }
  
  public MetadataImportResponse metadataPost(List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException
  {
    return new MetadataImportResponse(this.apiPost("metadata", params, payload));
  }
  
  public HTTPResponse apiGet(String url, List<NameValuePair> params) throws InvalidLoginException, HTTPException
  {
    if (!url.contains("?") && !url.endsWith(".json"))
    {
      url = url + ".json";
    }
    
    return connector.httpGet("api/" + version + "/" + url, params);
  }
  
  public HTTPResponse apiPost(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException
  {
    if (!url.contains("?") && !url.endsWith(".json"))
    {
      url = url + ".json";
    }
    
    return connector.httpPost("api/" + version + "/" + url, params, body);
  }
}
