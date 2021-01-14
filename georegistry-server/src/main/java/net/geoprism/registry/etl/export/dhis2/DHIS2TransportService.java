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
package net.geoprism.registry.etl.export.dhis2;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

import net.geoprism.dhis2.dhis2adapter.DHIS2Bridge;
import net.geoprism.dhis2.dhis2adapter.HTTPConnector;
import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.IncompatibleServerVersionException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.exception.UnexpectedResponseException;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2ImportResponse;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2Response;
import net.geoprism.dhis2.dhis2adapter.response.MetadataGetResponse;
import net.geoprism.dhis2.dhis2adapter.response.MetadataImportResponse;
import net.geoprism.dhis2.dhis2adapter.response.ObjectReportResponse;
import net.geoprism.dhis2.dhis2adapter.response.TypeReportResponse;

public class DHIS2TransportService implements DHIS2TransportServiceIF
{
  private DHIS2Bridge dhis2;
  
  public DHIS2TransportService(HTTPConnector connector, Integer apiVersion)
  {
    this.dhis2 = new DHIS2Bridge(connector, apiVersion);
  }
  
  public void initialize() throws UnexpectedResponseException, InvalidLoginException, HTTPException, IncompatibleServerVersionException
  {
    this.dhis2.initialize();
  }

  @Override
  public String getDhis2Id() throws HTTPException, InvalidLoginException, UnexpectedResponseException
  {
    return this.dhis2.getDhis2Id();
  }

  @Override
  public DHIS2Response systemInfo() throws InvalidLoginException, HTTPException
  {
    return this.dhis2.systemInfo();
  }

  @Override
  public ObjectReportResponse entityPost(String entityName, List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException
  {
    return this.dhis2.entityPost(entityName, params, payload);
  }

  @Override
  public DHIS2Response entityIdGet(String entityName, String entityId, List<NameValuePair> params) throws InvalidLoginException, HTTPException
  {
    return this.dhis2.entityIdGet(entityName, entityId, params);
  }
  
  @Override
  public DHIS2Response entityIdDelete(String entityName, String entityId, List<NameValuePair> params) throws InvalidLoginException, HTTPException
  {
    return this.dhis2.entityIdDelete(entityName, entityId, params);
  }

  @Override
  public ObjectReportResponse entityIdPatch(String entityName, String entityId, List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException
  {
    return this.dhis2.entityIdPatch(entityName, entityId, params, payload);
  }

  @Override
  public TypeReportResponse entityTranslations(String entityName, String entityId, List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException
  {
    return this.dhis2.entityTranslations(entityName, entityId, params, payload);
  }

  @Override
  public MetadataImportResponse metadataPost(List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException
  {
    return this.dhis2.metadataPost(params, payload);
  }

  @Override
  public <T> MetadataGetResponse<T> metadataGet(Class<?> dhis2Type) throws InvalidLoginException, HTTPException
  {
    return this.dhis2.metadataGet(dhis2Type);
  }

  @Override
  public <T> MetadataGetResponse<T> metadataGet(Class<?> dhis2Type, List<NameValuePair> params) throws InvalidLoginException, HTTPException
  {
    return this.dhis2.metadataGet(dhis2Type, params);
  }

  @Override
  public DHIS2Response apiGet(String url, List<NameValuePair> params) throws InvalidLoginException, HTTPException
  {
    return this.dhis2.apiGet(url, params);
  }

  @Override
  public DHIS2ImportResponse apiPost(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException
  {
    return this.dhis2.apiPost(url, params, body);
  }

  @Override
  public DHIS2ImportResponse apiPut(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException
  {
    return this.dhis2.apiPut(url, params, body);
  }

  @Override
  public DHIS2ImportResponse apiPatch(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException
  {
    return this.dhis2.apiPatch(url, params, body);
  }
  
  @Override
  public String getVersionRemoteServer()
  {
    return this.dhis2.getVersionRemoteServer();
  }

  @Override
  public Integer getVersionRemoteServerApi()
  {
    return this.dhis2.getVersionRemoteServerApi();
  }

  @Override
  public Integer getVersionApiCompat()
  {
    return this.dhis2.getVersionApiCompat();
  }
}
