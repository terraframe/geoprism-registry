package net.geoprism.dhis2.dhis2adapter;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.exception.UnexpectedResponseException;
import net.geoprism.dhis2.dhis2adapter.response.EntityImportResponse;
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
  
  /**
   * Used to create or update a DHIS2 entity. Required attributes are enforced.
   * 
   * https://docs.dhis2.org/2.34/en/dhis2_developer_manual/web-api.html#metadata-create-read-update-delete-validate
   * 
   * @param entityName
   * @param params
   * @param payload
   * @return
   * @throws InvalidLoginException
   * @throws HTTPException
   */
  public EntityImportResponse entityPost(String entityName, List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException
  {
    return new EntityImportResponse(this.apiPost(entityName, params, payload));
  }
  
  /**
   * Used to update an existing DHIS2 entity. May be used to submit a 'partial update'.
   * 
   * https://docs.dhis2.org/2.34/en/dhis2_developer_manual/web-api.html#metadata-create-read-update-delete-validate
   * 
   * @param entityName
   * @param entityId
   * @param params
   * @param payload
   * @return
   * @throws InvalidLoginException
   * @throws HTTPException
   */
  public EntityImportResponse entityIdPatch(String entityName, String entityId, List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException
  {
    return new EntityImportResponse(this.apiPatch(entityName + "/" + entityId, params, payload));
  }
  
  /**
   * https://docs.dhis2.org/2.34/en/dhis2_developer_manual/web-api.html#metadata-import
   * 
   * @param params
   * @param payload
   * @return
   * @throws InvalidLoginException
   * @throws HTTPException
   */
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
  
  public HTTPResponse apiPatch(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException
  {
    if (!url.contains("?") && !url.endsWith(".json"))
    {
      url = url + ".json";
    }
    
    return connector.httpPatch("api/" + version + "/" + url, params, body);
  }
}
