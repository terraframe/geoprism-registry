/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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

import net.geoprism.dhis2.dhis2adapter.exception.BadServerUriException;
import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.IncompatibleServerVersionException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.exception.UnexpectedResponseException;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2ImportResponse;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2Response;
import net.geoprism.dhis2.dhis2adapter.response.EntityGetResponse;
import net.geoprism.dhis2.dhis2adapter.response.ImportReportResponse;
import net.geoprism.dhis2.dhis2adapter.response.LocaleGetResponse;
import net.geoprism.dhis2.dhis2adapter.response.MetadataGetResponse;
import net.geoprism.dhis2.dhis2adapter.response.MetadataImportResponse;
import net.geoprism.dhis2.dhis2adapter.response.ObjectReportResponse;

public interface DHIS2TransportServiceIF
{
  public void initialize() throws UnexpectedResponseException, InvalidLoginException, HTTPException, IncompatibleServerVersionException, BadServerUriException;
  
  public String getDhis2Id() throws HTTPException, InvalidLoginException, UnexpectedResponseException, BadServerUriException;

  public DHIS2Response systemInfo() throws InvalidLoginException, HTTPException, BadServerUriException;

  public ObjectReportResponse entityPost(String entityName, List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException, BadServerUriException;

  public <T> EntityGetResponse<T> entityIdGet(String entityName, String entityId, Class<?> entityType, List<NameValuePair> params) throws InvalidLoginException, HTTPException, BadServerUriException;
  
  public DHIS2Response entityIdDelete(String entityName, String entityId, List<NameValuePair> params) throws InvalidLoginException, HTTPException, BadServerUriException;

  public ObjectReportResponse entityIdPatch(String entityName, String entityId, List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException, BadServerUriException;

  public ImportReportResponse entityTranslations(String entityName, String entityId, List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException, BadServerUriException;

  public ImportReportResponse metadataPost(List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException, BadServerUriException;
  
  public <T> MetadataGetResponse<T> metadataGet(Class<?> dhis2Type) throws InvalidLoginException, HTTPException, BadServerUriException;
  
  public <T> MetadataGetResponse<T> metadataGet(Class<?> dhis2Type, List<NameValuePair> params) throws InvalidLoginException, HTTPException, BadServerUriException;
  
  public DHIS2Response apiGet(String url, List<NameValuePair> params) throws InvalidLoginException, HTTPException, BadServerUriException;
  
  public DHIS2ImportResponse apiPost(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException, BadServerUriException;
  
  public DHIS2ImportResponse apiPut(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException, BadServerUriException;
  
  public DHIS2ImportResponse apiPatch(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException, BadServerUriException;
  
  public LocaleGetResponse localesGet() throws InvalidLoginException, HTTPException, BadServerUriException;
  
  public String getRemoteServerUrl();
  
  public String getVersionRemoteServer();
  
  /**
   * Returns the API version of the remote DHIS2 server.
   */
  public Integer getVersionRemoteServerApi();
  
  /**
   * Returns the API version of the compatibility layer used for DHIS2 communication.
   */
  public Integer getVersionApiCompat();
  
  /**
   * Sets the version of the API compatibility layer used for DHIS2 communication.
   * 
   * @param versionApiCompat
   * @throws IncompatibleServerVersionException If the version is not supported by the remote server.
   */
  public void setVersionApiCompat(Integer versionApiCompat) throws IncompatibleServerVersionException;
}
