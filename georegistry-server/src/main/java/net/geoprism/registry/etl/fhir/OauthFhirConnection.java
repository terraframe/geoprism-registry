package net.geoprism.registry.etl.fhir;

import java.io.IOException;
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
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IRestfulClientFactory;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import net.geoprism.account.OauthServer;
import net.geoprism.registry.graph.FhirExternalSystem;

public class OauthFhirConnection implements FhirConnection
{
  private FhirExternalSystem externalSystem;

  private OauthServer        oauth;

  private HttpClientBuilder  builder;

  private FhirContext        ctx;

  private IGenericClient     client;

  private String             accessToken;

  private Integer            expiresIn;

  private Long               lastSessionRefresh;

  public OauthFhirConnection(FhirExternalSystem externalSystem, OauthServer oauth)
  {
    this(externalSystem, oauth, HttpClientBuilder.create());
  }

  public OauthFhirConnection(FhirExternalSystem externalSystem, OauthServer oauth, HttpClientBuilder builder)
  {
    this.externalSystem = externalSystem;
    this.oauth = oauth;
    this.builder = builder;

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
    final String authUrl = this.oauth.getTokenLocation();

    try (CloseableHttpClient httpclient = this.builder.build())
    {
      HttpPost httpPost = new HttpPost(authUrl);

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

      ctx.setRestfulClientFactory(new AllowAllClientFactory(ctx));

      IRestfulClientFactory factory = ctx.getRestfulClientFactory();
      factory.setSocketTimeout(-1);

      BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(this.accessToken);

      this.client = factory.newGenericClient(this.externalSystem.getUrl());
      this.client.registerInterceptor(authInterceptor);
    }
    catch (IOException t)
    {
      t.printStackTrace();
    }
  }

  @Override
  public void close() throws Exception
  {
    // this.connection.close();
  }
}
