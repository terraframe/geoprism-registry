/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.etl.fhir;

import java.security.NoSuchAlgorithmException;

/*
 * #%L HAPI FHIR - Client Framework %% Copyright (C) 2014 - 2020 University
 * Health Network %% Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License. #L%
 */

import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.client.apache.ApacheHttpClient;
import ca.uhn.fhir.rest.client.api.Header;
import ca.uhn.fhir.rest.client.api.IHttpClient;
import ca.uhn.fhir.rest.client.impl.RestfulClientFactory;

public class AllowAllClientFactory extends RestfulClientFactory
{

  private HttpClient myHttpClient;

  private HttpHost   myProxy;

  /**
   * Constructor
   */
  public AllowAllClientFactory()
  {
    super();
  }

  /**
   * Constructor
   * 
   * @param theContext
   *          The context
   */
  public AllowAllClientFactory(FhirContext theContext)
  {
    super(theContext);
  }

  @Override
  protected synchronized ApacheHttpClient getHttpClient(String theServerBase)
  {
    return new ApacheHttpClient(getNativeHttpClient(), new StringBuilder(theServerBase), null, null, null, null);
  }

  @Override
  public synchronized IHttpClient getHttpClient(StringBuilder theUrl, Map<String, List<String>> theIfNoneExistParams, String theIfNoneExistString, RequestTypeEnum theRequestType, List<Header> theHeaders)
  {
    return new ApacheHttpClient(getNativeHttpClient(), theUrl, theIfNoneExistParams, theIfNoneExistString, theRequestType, theHeaders);
  }

  public synchronized HttpClient getNativeHttpClient()
  {
    if (myHttpClient == null)
    {
      try
      {
        HttpClientBuilder builder = getBuilder();

        myHttpClient = builder.build();
      }
      catch (Exception e)
      {
        throw new RuntimeException(e);
      }
    }

    return myHttpClient;
  }

  public HttpClientBuilder getBuilder() throws NoSuchAlgorithmException
  {
    HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

    SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(SSLContext.getDefault(), hostnameVerifier);
    Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create().register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", sslSocketFactory).build();

    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
    connectionManager.setMaxTotal(getPoolMaxTotal());
    connectionManager.setDefaultMaxPerRoute(getPoolMaxPerRoute());

    // TODO: Use of a deprecated method should be resolved.
    RequestConfig defaultRequestConfig = RequestConfig.custom().setSocketTimeout(getSocketTimeout()).setConnectTimeout(getConnectTimeout()).setConnectionRequestTimeout(getConnectionRequestTimeout()).setStaleConnectionCheckEnabled(true).setProxy(myProxy).build();

    HttpClientBuilder builder = HttpClients.custom().setConnectionManager(connectionManager).setDefaultRequestConfig(defaultRequestConfig).disableCookieManagement();

    if (myProxy != null && StringUtils.isNotBlank(getProxyUsername()) && StringUtils.isNotBlank(getProxyPassword()))
    {
      CredentialsProvider credsProvider = new BasicCredentialsProvider();
      credsProvider.setCredentials(new AuthScope(myProxy.getHostName(), myProxy.getPort()), new UsernamePasswordCredentials(getProxyUsername(), getProxyPassword()));
      builder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
      builder.setDefaultCredentialsProvider(credsProvider);
    }
    return builder;
  }

  @Override
  protected synchronized void resetHttpClient()
  {
    this.myHttpClient = null;
  }

  /**
   * Only allows to set an instance of type org.apache.http.client.HttpClient
   * 
   * @see ca.uhn.fhir.rest.client.api.IRestfulClientFactory#setHttpClient(Object)
   */
  @Override
  public synchronized void setHttpClient(Object theHttpClient)
  {
    this.myHttpClient = (HttpClient) theHttpClient;
  }

  @Override
  public void setProxy(String theHost, Integer thePort)
  {
    if (theHost != null)
    {
      myProxy = new HttpHost(theHost, thePort, "http");
    }
    else
    {
      myProxy = null;
    }
  }

}
