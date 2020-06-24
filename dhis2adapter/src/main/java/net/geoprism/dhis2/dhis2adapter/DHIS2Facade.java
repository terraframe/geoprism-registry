package net.geoprism.dhis2.dhis2adapter;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.exception.UnexpectedResponseException;
import net.geoprism.dhis2.dhis2adapter.response.HTTPResponse;
import net.geoprism.dhis2.dhis2adapter.response.MetadataImportResponse;

public class DHIS2Facade
{
  private String version;
  
  private HTTPConnector connector;
  
  Dhis2IdCache idCache;
  
  public DHIS2Facade(HTTPConnector connector, String version)
  {
    this.connector = connector;
    this.version = version;
    this.idCache = new Dhis2IdCache(this);
  }
  
  public String getDhis2Id() throws HTTPException, InvalidLoginException, UnexpectedResponseException
  {
    return this.idCache.next();
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
