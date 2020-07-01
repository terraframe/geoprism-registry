package net.geoprism.dhis2.dhis2adapter;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;

import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2ImportResponse;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2Response;

/**
 * Tests basic HTTP communication architecture by talking to play.dhis2.org
 */
public class HTTPConnectorTest
{
  @Before
  public void setUp()
  {
  }

  @Test
  public void testGet() throws InvalidLoginException, HTTPException
  {
    HTTPConnector connector = new HTTPConnector();
    
    connector.setCredentials(Constants.USERNAME, Constants.PASSWORD);
    
    connector.setServerUrl(Constants.DHIS2_URL);
    
    DHIS2Response resp = connector.httpGet("api/" + Constants.API_VERSION + "/system/info", null);
    
    Assert.assertEquals(200, resp.getStatusCode());
    
    JsonObject jo = resp.getJsonObject();
    
    Assert.assertEquals(Constants.DHIS2_VERSION, jo.get("version").getAsString());
    Assert.assertEquals(Constants.DHIS2_URL, jo.get("contextPath").getAsString());
  }
  
  @Test
  public void testPost() throws InvalidLoginException, HTTPException
  {
    // Payload taken from https://docs.dhis2.org/2.34/en/dhis2_developer_manual/web-api.html#metadata-import
    final String payload = "{\n" + 
        "  \"dataElements\": [\n" + 
        "    {\n" + 
        "      \"name\": \"EPI - IPV 3 doses given\",\n" + 
        "      \"shortName\": \"EPI - IPV 3 doses given\",\n" + 
        "      \"aggregationType\": \"SUM\",\n" + 
        "      \"domainType\": \"AGGREGATE\",\n" + 
        "      \"valueType\": \"INTEGER_ZERO_OR_POSITIVE\"\n" + 
        "    }\n" + 
        "  ]\n" + 
        "}";
    
    HTTPConnector connector = new HTTPConnector();
    
    connector.setCredentials(Constants.USERNAME, Constants.PASSWORD);
    
    connector.setServerUrl(Constants.DHIS2_URL);
    
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("importMode", "VALIDATE"));
    
    DHIS2Response resp = connector.httpPost("api/" + Constants.API_VERSION + "/metadata", params, new StringEntity(payload, Charset.forName("UTF-8")));
    
    Assert.assertEquals(200, resp.getStatusCode());
    
    JsonObject jo = resp.getJsonObject();
    
    Assert.assertEquals("OK", jo.get("status").getAsString());
  }
}
