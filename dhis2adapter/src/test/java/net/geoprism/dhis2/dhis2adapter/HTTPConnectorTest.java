package net.geoprism.dhis2.dhis2adapter;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Assert;

import com.google.gson.JsonObject;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests basic HTTP communication architecture by talking to play.dhis2.org
 */
public class HTTPConnectorTest extends TestCase
{
  public HTTPConnectorTest( String testName )
  {
    super( testName );
  }

  public static Test suite()
  {
    return new TestSuite( HTTPConnectorTest.class );
  }

  public void testGet() throws InvalidLoginException, HTTPException
  {
    HTTPConnector connector = new HTTPConnector();
    
    connector.setCredentials(Constants.USERNAME, Constants.PASSWORD);
    
    connector.setServerUrl(Constants.DHIS2_URL);
    
    HTTPResponse resp = connector.httpGet("api/" + Constants.VERSION + "/system/info", null);
    
    Assert.assertEquals(200, resp.getStatusCode());
    
    JsonObject jo = resp.getJsonObject();
    
    Assert.assertEquals(Constants.DHIS2_URL, jo.get("instanceBaseUrl").getAsString());
  }
  
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
    
    HTTPResponse resp = connector.httpPost("api/" + Constants.VERSION + "/metadata", params, payload);
    
    Assert.assertEquals(200, resp.getStatusCode());
    
    JsonObject jo = resp.getJsonObject();
    
    Assert.assertEquals("OK", jo.get("status").getAsString());
  }
}
