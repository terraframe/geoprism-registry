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
package net.geoprism.registry.etl.fhir;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IRestfulClientFactory;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import net.geoprism.account.OauthServer;
import net.geoprism.registry.etl.export.HttpError;
import net.geoprism.registry.graph.FhirExternalSystem;

public class OauthFhirConnection implements FhirConnection
{
  private FhirExternalSystem externalSystem;

  private OauthServer        oauth;

  private FhirContext        ctx;

  private IGenericClient     client;

  private String             accessToken;

  private Integer            expiresIn;

  private Long               lastSessionRefresh;

  public OauthFhirConnection(FhirExternalSystem externalSystem, OauthServer oauth)
  {
    this.externalSystem = externalSystem;
    this.oauth = oauth;

    this.ctx = FhirContext.forR4();

    this.open();
  }

  @Override
  public IGenericClient getClient()
  {
    return this.client;
  }

  @Override
  public FhirContext getFhirContext()
  {
    return this.ctx;
  }

  @Override
  public FhirExternalSystem getExternalSystem()
  {
    return externalSystem;
  }

  @Override
  public String getSystem()
  {
    return this.externalSystem.getSystem();
  }

  public String getAccessToken()
  {
    return accessToken;
  }

  public Integer getExpiresIn()
  {
    return expiresIn;
  }

  public Long getLastSessionRefresh()
  {
    return lastSessionRefresh;
  }

  @Override
  public void open()
  {
    // TODO: This is garbage and needs to be removed after the demo and we can
    // assume the oauth server has an actual url and legitimate https
    // certificate
    AllowAllClientFactory clientFactory = new AllowAllClientFactory(ctx);

    try (CloseableHttpClient httpclient = clientFactory.getBuilder().build())
    {
      HttpPost httpPost = new HttpPost(this.oauth.getTokenLocation());

      List<NameValuePair> nvps = new ArrayList<>();
      nvps.add(new BasicNameValuePair("username", this.externalSystem.getUsername()));
      nvps.add(new BasicNameValuePair("password", this.externalSystem.getPassword()));
      nvps.add(new BasicNameValuePair("grant_type", "password"));
      nvps.add(new BasicNameValuePair("client_id", this.oauth.getClientId()));
      nvps.add(new BasicNameValuePair("client_secret", this.oauth.getSecretKey()));

      httpPost.setEntity(new UrlEncodedFormEntity(nvps));

      httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");

      try (CloseableHttpResponse response = httpclient.execute(httpPost))
      {
        HttpEntity entity2 = response.getEntity();
        String sResponse = IOUtils.toString(entity2.getContent(), "UTF-8");

        if (response.getStatusLine().getStatusCode() >= 400)
        {
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(sResponse);

        // this.refreshToken = rootNode.get("refresh_token").textValue();
        this.accessToken = rootNode.get("access_token").textValue();
        this.expiresIn = rootNode.get("expires_in").asInt();
        this.lastSessionRefresh = new Date().getTime();
      }

      ctx.setRestfulClientFactory(clientFactory);

      IRestfulClientFactory factory = ctx.getRestfulClientFactory();
      factory.setSocketTimeout(-1);

      BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(this.accessToken);

      this.client = factory.newGenericClient(this.externalSystem.getUrl());
      this.client.registerInterceptor(authInterceptor);
    }
    catch (IOException | NoSuchAlgorithmException t)
    {
      throw new HttpError(t);
    }
  }

  @Override
  public void close() throws Exception
  {
    // this.connection.close();
  }
}
