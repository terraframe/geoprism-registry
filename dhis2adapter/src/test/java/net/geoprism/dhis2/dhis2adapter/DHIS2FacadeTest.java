package net.geoprism.dhis2.dhis2adapter;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;

import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.exception.UnexpectedResponseException;
import net.geoprism.dhis2.dhis2adapter.response.HTTPResponse;

/**
 * Tests the DHIS2 Facade by talking to play.dhis2.org
 */
public class DHIS2FacadeTest
{
  private DHIS2Facade facade;
  
  @Before
  public void setUp()
  {
    SSLTrustConfiguration.trustAll();
    
    HTTPConnector connector = new HTTPConnector();
    connector.setCredentials(Constants.USERNAME, Constants.PASSWORD);
    connector.setServerUrl(Constants.DHIS2_URL);
    
    facade = new DHIS2Facade(connector, Constants.VERSION);
  }
  
  @Test
  public void testSystemInfo() throws InvalidLoginException, HTTPException
  {
    HTTPResponse resp = facade.systemInfo();
    
    Assert.assertEquals(200, resp.getStatusCode());
    
    JsonObject jo = resp.getJsonObject();
    
    Assert.assertEquals(Constants.DHIS2_URL, jo.get("instanceBaseUrl").getAsString());
  }
  
  @Test
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
    
    HTTPResponse resp = facade.metadataPost(params, new StringEntity(payload, Charset.forName("UTF-8")));
    
    Assert.assertEquals(200, resp.getStatusCode());
    
    JsonObject jo = resp.getJsonObject();
    
    Assert.assertEquals("OK", jo.get("status").getAsString());
  }
  
  @Test
  public void testGetDhis2Id() throws HTTPException, InvalidLoginException, UnexpectedResponseException
  {
    Set<String> set = new HashSet<String>();
    
    final int fetchSize = Dhis2IdCache.FETCH_NUM * 3 - 300;
    
    for (int i = 0; i < fetchSize; ++i)
    {
      String id = facade.getDhis2Id();
      
      Assert.assertNotNull(id);
      
      Assert.assertEquals(11, id.length());
      
      Assert.assertTrue(set.add(id));
    }
    
    Assert.assertEquals(new Integer((Dhis2IdCache.FETCH_NUM * 3) - (Dhis2IdCache.FETCH_NUM * 3 - 300)), facade.idCache.getNumIds());
  }
}
