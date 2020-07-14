/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.dhis2.dhis2adapter;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.exception.UnexpectedResponseException;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2ImportResponse;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2Response;
import net.geoprism.dhis2.dhis2adapter.response.MetadataGetResponse;
import net.geoprism.dhis2.dhis2adapter.response.MetadataImportResponse;
import net.geoprism.dhis2.dhis2adapter.response.ObjectReportResponse;
import net.geoprism.dhis2.dhis2adapter.response.TypeReportResponse;

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
  
  public DHIS2Response systemInfo() throws InvalidLoginException, HTTPException
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
  public ObjectReportResponse entityPost(String entityName, List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException
  {
    return new ObjectReportResponse(this.apiPost(entityName, params, payload));
  }
  
  public DHIS2Response entityIdGet(String entityName, String entityId, List<NameValuePair> params) throws InvalidLoginException, HTTPException
  {
    return this.apiGet(entityName + "/" + entityId, params);
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
  public ObjectReportResponse entityIdPatch(String entityName, String entityId, List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException
  {
    return new ObjectReportResponse(this.apiPatch(entityName + "/" + entityId, params, payload));
  }
  
  
  // TODO : It says in their docs that they should support this, however it doesn't work on any DHIS2 server I've tried. (server responds 404)
  // https://docs.dhis2.org/2.34/en/dhis2_developer_manual/web-api.html#translations
//  public HTTPResponse translationsPost(List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException
//  {
//    return this.apiPost("translations", params, payload);
//  }
  
  
  /**
   * Used to replace translations for a particular entity with the provided entityId.
   * 
   * @see https://github.com/dhis2/dhis2-core/blob/cfcfff74561aeefcdbb01d5d189c6de5ef2df63c/dhis-2/dhis-web/dhis-web-api/src/main/java/org/hisp/dhis/webapi/controller/AbstractCrudController.java#L354
   * 
   * @param entityName
   * @param entityId
   * @param params
   * @param payload
   * @return
   * @throws InvalidLoginException
   * @throws HTTPException
   */
  public TypeReportResponse entityTranslations(String entityName, String entityId, List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException
  {
    return new TypeReportResponse(this.apiPut(entityName + "/" + entityId + "/translations", params, payload));
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
  
  public <T> MetadataGetResponse<T> metadataGet(String objectNamePlural) throws InvalidLoginException, HTTPException
  {
    return this.metadataGet(objectNamePlural, null);
  }
  
  public <T> MetadataGetResponse<T> metadataGet(String objectNamePlural, List<NameValuePair> params) throws InvalidLoginException, HTTPException
  {
    if (params == null)
    {
      params = new ArrayList<NameValuePair>();
    }
    
    boolean hasObjectName = false;
    
    for (NameValuePair param : params)
    {
      if (param.getName().equals(objectNamePlural))
      {
        hasObjectName = true;
      }
    }
    
    if (!hasObjectName)
    {
      params.add(new BasicNameValuePair(objectNamePlural, "true"));
    }
    
    return new MetadataGetResponse<T>(this.apiGet("metadata", params), objectNamePlural);
  }
  
  public DHIS2Response apiGet(String url, List<NameValuePair> params) throws InvalidLoginException, HTTPException
  {
    if (!url.contains("?") && !url.endsWith(".json"))
    {
      url = url + ".json";
    }
    
    return connector.httpGet("api/" + version + "/" + url, params);
  }
  
  public DHIS2ImportResponse apiPost(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException
  {
    if (!url.contains("?") && !url.endsWith(".json"))
    {
      url = url + ".json";
    }
    
    return new DHIS2ImportResponse(connector.httpPost("api/" + version + "/" + url, params, body));
  }
  
  public DHIS2ImportResponse apiPut(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException
  {
    if (!url.contains("?") && !url.endsWith(".json"))
    {
      url = url + ".json";
    }
    
    return new DHIS2ImportResponse(connector.httpPut("api/" + version + "/" + url, params, body));
  }
  
  public DHIS2ImportResponse apiPatch(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException
  {
    if (!url.contains("?") && !url.endsWith(".json"))
    {
      url = url + ".json";
    }
    
    return new DHIS2ImportResponse(connector.httpPatch("api/" + version + "/" + url, params, body));
  }
}
