package net.geoprism.dhis2.dhis2adapter;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Assert;
import org.junit.Before;

import com.google.gson.JsonObject;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the DHIS2 Facade by talking to play.dhis2.org
 */
public class DHIS2FacadeTest extends TestCase
{
  private DHIS2Facade facade;
  
  public DHIS2FacadeTest( String testName )
  {
    super( testName );
  }

  public static Test suite()
  {
    return new TestSuite( DHIS2FacadeTest.class );
  }
  
  @Before
  public void setUp()
  {
    HTTPConnector connector = new HTTPConnector();
    connector.setCredentials(Constants.USERNAME, Constants.PASSWORD);
    connector.setServerUrl(Constants.DHIS2_URL);
    
    facade = new DHIS2Facade(connector, Constants.VERSION);
  }
  
  public void testSystemInfo() throws InvalidLoginException, HTTPException
  {
    HTTPResponse resp = facade.systemInfo();
    
    Assert.assertEquals(200, resp.getStatusCode());
    
    JsonObject jo = resp.getJsonObject();
    
    Assert.assertEquals(Constants.DHIS2_URL, jo.get("instanceBaseUrl").getAsString());
  }
  
  public void testMetadataPost() throws InvalidLoginException, HTTPException
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
    
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("importMode", "VALIDATE"));
    
    HTTPResponse resp = facade.metadataPost(params, payload);
    
    Assert.assertEquals(200, resp.getStatusCode());
    
    JsonObject jo = resp.getJsonObject();
    
    Assert.assertEquals("OK", jo.get("status").getAsString());
  }
}
