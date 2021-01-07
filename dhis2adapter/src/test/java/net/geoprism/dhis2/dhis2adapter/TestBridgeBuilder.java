package net.geoprism.dhis2.dhis2adapter;

import java.io.IOException;

import org.apache.commons.io.IOUtils;

public class TestBridgeBuilder
{
  public static String getVersionResponse(Integer version)
  {
    try
    {
      return IOUtils.toString(DHIS2BridgeTest.class.getResourceAsStream("/default/system-info.json"), "UTF-8");
    }
    catch (IOException t)
    {
      throw new RuntimeException(t);
    }
  }
  
  public static DHIS2Bridge buildDefault(String response, Integer version, int statusCode)
  {
    return new DHIS2Bridge(new TestSingleResponseConnector(response, getVersionResponse(version), statusCode), version);
  }
  
  public static DHIS2Bridge buildDefault(String response, int statusCode)
  {
    return new DHIS2Bridge(new TestSingleResponseConnector(response, getVersionResponse(Constants.DHIS2_VERSION), statusCode), Constants.DHIS2_VERSION);
  }
  
  public static DHIS2Bridge buildFakeId()
  {
    return new DHIS2Bridge(new FakeIdConnector(getVersionResponse(Constants.DHIS2_VERSION)), Constants.DHIS2_VERSION);
  }
}
