/*******************************************************************************
 * Copyright (C) 2018 IVCC
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.geoprism.dhis2.dhis2adapter.response.HTTPResponse;

public class HTTPConnector
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
  
  private HttpClientContext getContext() throws URISyntaxException
  {
    URIBuilder uri = new URIBuilder(this.getServerUrl());
    
    final HttpHost targetHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
    
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
  
  public HTTPResponse httpGet(String url, List<NameValuePair> params) throws InvalidLoginException, HTTPException
  {
    try
    {
      if (!isInitialized())
      {
        initialize();
      }
      
      HttpGet get = new HttpGet(this.buildUri(url, params));
      
      get.addHeader("Accept", "application/json");
      
      try (CloseableHttpResponse response = client.execute(get, getContext()))
      {
        if (response.getStatusLine().getStatusCode() == 401)
        {
          throw new InvalidLoginException("Unable to log in to " + this.getServerUrl());
        }
        
        try (InputStream is = response.getEntity().getContent())
        {
          String resp = IOUtils.toString(is);
          
          return new HTTPResponse(resp, response.getStatusLine().getStatusCode());
        }
      }
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
  
  public HTTPResponse httpPost(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException
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
      
      try (CloseableHttpResponse response = client.execute(post, this.getContext()))
      {
        int statusCode = response.getStatusLine().getStatusCode();
        
        if (statusCode == 401 || (
              statusCode == 302 && response.getFirstHeader("Location") != null && 
              response.getFirstHeader("Location").getValue().contains("security/login")
            ))
        {
          throw new InvalidLoginException("Unable to log in to " + this.getServerUrl());
        }
        
        try (InputStream is = response.getEntity().getContent())
        {
          String resp = IOUtils.toString(is);
          
          return new HTTPResponse(resp, response.getStatusLine().getStatusCode());
        }
      }
    }
    catch (IOException | URISyntaxException e)
    {
      throw new HTTPException(e);
    }
  }
}