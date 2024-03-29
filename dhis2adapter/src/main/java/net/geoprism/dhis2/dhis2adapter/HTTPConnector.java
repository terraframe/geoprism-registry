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
/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.dhis2.dhis2adapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.geoprism.dhis2.dhis2adapter.exception.BadServerUriException;
import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2Response;

public class HTTPConnector implements ConnectorIF
{
  CloseableHttpClient client;
  
  Logger logger = LoggerFactory.getLogger(HTTPConnector.class);
  
  String serverurl;
  
  String username;
  
  String password;
  
  public void setCredentials(String username, String password)
  {
    this.username = username;
    this.password = password;
  }
  
  public String getServerUrl()
  {
    return serverurl;
  }
  
  public void setServerUrl(String url)
  {
    if (!url.endsWith("/"))
    {
      url = url + "/";
    }
    
    this.serverurl = url;
  }
  
  synchronized public void initialize()
  {
    this.client = HttpClients.createDefault(); // TODO : Thread safety ?
  }
  
  public boolean isInitialized()
  {
    return client != null;
  }
  
  private HttpClientContext getContext() throws BadServerUriException
  {
    try
    {
      URIBuilder uri = new URIBuilder(this.getServerUrl());
      
      final HttpHost targetHost;
      try
      {
        targetHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
      }
      catch (IllegalArgumentException e)
      {
        throw new BadServerUriException(e, this.getServerUrl());
      }
      
      CredentialsProvider credsProvider = new BasicCredentialsProvider();
      credsProvider.setCredentials(
          new AuthScope(targetHost.getHostName(), targetHost.getPort()), 
          new UsernamePasswordCredentials(username, password));
      
      // This authcache code is used to enable "Preemptive authentication".
      // http://hc.apache.org/httpcomponents-client-4.5.x/tutorial/html/authentication.html#d5e613
      // TODO : We shouldn't need to be using preemtive authentication (because it's a potential
      //        security risk), however I'm not sure how else to get this working.
      AuthCache authCache = new BasicAuthCache();
      BasicScheme basicAuth = new BasicScheme();
      authCache.put(targetHost, basicAuth);
      
      HttpClientContext context = HttpClientContext.create();
      context.setCredentialsProvider(credsProvider);
      context.setAuthCache(authCache);
      
      return context;
    }
    catch (URISyntaxException e)
    {
      throw new BadServerUriException(e, this.getServerUrl());
    }
  }
  
  private URI buildUri(String searchPath, List<NameValuePair> params) throws URISyntaxException
  {
    if (!searchPath.startsWith("/"))
    {
      searchPath = "/" + searchPath;
    }
    
    URIBuilder uriBuilder = new URIBuilder(this.getServerUrl());
    
    List<String> pathSegsBefore = uriBuilder.getPathSegments();
    List<String> additionalPathSegs = uriBuilder.setPath(searchPath).getPathSegments();
    pathSegsBefore.addAll(additionalPathSegs);
    uriBuilder.setPathSegments(pathSegsBefore);
    
    if (params != null)
    {
      uriBuilder.addParameters(params);
    }
    
    return uriBuilder.build();
  }
  
  private DHIS2Response convertResponse(CloseableHttpResponse response) throws InvalidLoginException, UnsupportedOperationException, IOException
  {
    int statusCode = response.getStatusLine().getStatusCode();
    
    if (statusCode == 401 || (
          statusCode == 302 && response.getFirstHeader("Location") != null && 
          response.getFirstHeader("Location").getValue().contains("security/login")
        ))
    {
      throw new InvalidLoginException("Unable to log in to " + this.getServerUrl());
    }
    
    if (response.getEntity() != null)
    {
      try (InputStream is = response.getEntity().getContent())
      {
        String resp = IOUtils.toString(is, "UTF-8");
        
        return new DHIS2Response(resp, response.getStatusLine().getStatusCode());
      }
    }
    else
    {
      return new DHIS2Response(null, response.getStatusLine().getStatusCode());
    }
  }
  
  public DHIS2Response httpGet(String url, List<NameValuePair> params) throws InvalidLoginException, HTTPException, BadServerUriException
  {
    try
    {
      if (!isInitialized())
      {
        initialize();
      }
      
      HttpGet get = new HttpGet(this.buildUri(url, params));
      
      get.addHeader("Accept", "application/json");
      
      return this.httpRequest(get);
    }
    catch (URISyntaxException | IOException e)
    {
      throw new HTTPException(e);
    }
  }
  
//  public HTTPResponse postAsMultipart(String url, File file)
//  {
//    try {
//      if (!isInitialized())
//      {
//        initialize();
//      }
//      
//      HttpPost post = new HttpPost(this.getServerUrl() + url);
//      
//      post.setRequestHeader("Content-Type", "multipart/form-data");
//      
//      FilePart filePart;
//      
//      filePart = new FilePart("form_def_file", file, "application/xml", "UTF-8");
//      
//      
//      Part[] parts = { filePart };
//      MultipartRequestEntity multipartRequestEntity = new MultipartRequestEntity(parts, post.getParams());
//      
//      post.setRequestEntity(multipartRequestEntity);
//      
//      HTTPResponse response = this.httpRequest(post);
//      
//      if (response.getStatusCode() == 401)
//      {
//        throw new InvalidLoginException("Unable to log in to " + this.getServerUrl());
//      }
//      
//      return response;
//    } catch (FileNotFoundException e) {
//      throw new RuntimeException(e);
//    }
//  }
  
  public DHIS2Response httpPost(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException, BadServerUriException
  {
    try
    {
      if (!isInitialized())
      {
        initialize();
      }
      
      HttpPost post = new HttpPost(this.buildUri(url, params));
      
      post.addHeader("Content-Type", "application/json");
      
      post.setEntity(body);
      
      return this.httpRequest(post);
    }
    catch (IOException | URISyntaxException e)
    {
      throw new HTTPException(e);
    }
  }
  
  public DHIS2Response httpPut(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException, BadServerUriException
  {
    try
    {
      if (!isInitialized())
      {
        initialize();
      }
      
      HttpPut put = new HttpPut(this.buildUri(url, params));
      
      put.addHeader("Content-Type", "application/json");
      
      put.setEntity(body);
      
      return this.httpRequest(put);
    }
    catch (IOException | URISyntaxException e)
    {
      throw new HTTPException(e);
    }
  }
  
  public DHIS2Response httpPatch(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException, BadServerUriException
  {
    try
    {
      if (!isInitialized())
      {
        initialize();
      }
      
      HttpPatch patch = new HttpPatch(this.buildUri(url, params));
      
      patch.addHeader("Content-Type", "application/json");
      
      patch.setEntity(body);
      
      return this.httpRequest(patch);
    }
    catch (IOException | URISyntaxException e)
    {
      throw new HTTPException(e);
    }
  }

  @Override
  public DHIS2Response httpDelete(String url, List<NameValuePair> params) throws InvalidLoginException, HTTPException, BadServerUriException
  {
    try
    {
      if (!isInitialized())
      {
        initialize();
      }
      
      HttpDelete delete = new HttpDelete(this.buildUri(url, params));
      
      delete.addHeader("Content-Type", "application/json");
      
      return this.httpRequest(delete);
    }
    catch (URISyntaxException | IOException e)
    {
      throw new HTTPException(e);
    }
  }
  
  public DHIS2Response httpRequest(HttpRequestBase method) throws InvalidLoginException, ClientProtocolException, IOException, URISyntaxException, BadServerUriException
  {
    this.logger.debug("Sending request to " + method.getURI());

    // Execute the method.
    try (CloseableHttpResponse response = client.execute(method, getContext()))
    {
      int statusCode = response.getStatusLine().getStatusCode();
      
      // Follow Redirects
      if (statusCode == HttpStatus.SC_MOVED_TEMPORARILY || statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_TEMPORARY_REDIRECT || statusCode == HttpStatus.SC_SEE_OTHER)
      {
        String location = response.getFirstHeader("Location") != null ? response.getFirstHeader("Location").getValue() : ""; 
        
        if (location.contains("security/login"))
        {
          throw new InvalidLoginException("Unable to log in to " + this.getServerUrl());
        }
        else if (! "".equals(location))
        {
          this.logger.debug("Redirected [" + statusCode + "] to [" + location + "].");
          
          method.setURI(new URI(location));
          method.releaseConnection();
          return httpRequest(method);
        }
      }
      
      return this.convertResponse(response);
    }
  }
}