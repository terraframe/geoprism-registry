package net.geoprism.dhis2.dhis2adapter;

import org.junit.Assert;
import org.junit.Test;

import net.geoprism.dhis2.dhis2adapter.exception.IncompatibleServerVersionException;

/**
 * DHIS2 has a lot of different ways that they version their software. We're going to test a few of the ways I've seen it written.
 * 
 * @author rrowlands
 *
 */
public class DHIS2VersionTest
{
  @Test(expected = IncompatibleServerVersionException.class)
  public void testBadApiVersion() throws Exception
  {
    String versionResponse = TestBridgeBuilder.getVersionResponse();
    
    DHIS2Bridge facade = new DHIS2Bridge(new TestSingleResponseConnector(null, versionResponse, 200), Constants.DHIS2_API_VERSION - 3);
    
    facade.initialize();
  }
  
  @Test
  public void testGetVersion() throws Exception
  {
    final String versionRemote = Constants.DHIS2_VERSION;
    final Integer versionApiRemote = Constants.DHIS2_API_VERSION;
    final Integer versionApiCompat = versionApiRemote - 2;
    
    String versionResponse = TestBridgeBuilder.getVersionResponse(versionRemote);
    
    DHIS2Bridge facade = new DHIS2Bridge(new TestSingleResponseConnector(null, versionResponse, 200), versionApiCompat);
    
    facade.initialize();
    
    Assert.assertEquals(versionApiRemote, facade.getVersionRemoteServerApi());
    Assert.assertEquals(versionApiCompat, facade.getVersionApiCompat());
    Assert.assertEquals(versionRemote, facade.getVersionRemoteServer());
  }
  
  /**
   * Test a version of a snapshot version.
   */
  @Test
  public void testGetVersionSnapshot() throws Exception
  {
    final String versionRemote = "2.35.12-SNAPSHOT";
    final Integer versionApiRemote = 35;
    final Integer versionApiCompat = versionApiRemote - 2;
    
    String versionResponse = TestBridgeBuilder.getVersionResponse(versionRemote);
    
    DHIS2Bridge facade = new DHIS2Bridge(new TestSingleResponseConnector(null, versionResponse, 200), versionApiCompat);
    
    facade.initialize();
    
    Assert.assertEquals(versionApiRemote, facade.getVersionRemoteServerApi());
    Assert.assertEquals(versionApiCompat, facade.getVersionApiCompat());
    Assert.assertEquals(versionRemote, facade.getVersionRemoteServer());
  }
  
  /**
   * Test a version with an extra digit at the end (??? why are they doing this ???)
   */
  @Test
  public void testGetVersionFourDigits() throws Exception
  {
    final String versionRemote = "2.38.1.1";
    final Integer versionApiRemote = 38;
    final Integer versionApiCompat = versionApiRemote - 2;
    
    String versionResponse = TestBridgeBuilder.getVersionResponse(versionRemote);
    
    DHIS2Bridge facade = new DHIS2Bridge(new TestSingleResponseConnector(null, versionResponse, 200), versionApiCompat);
    
    facade.initialize();
    
    Assert.assertEquals(versionApiRemote, facade.getVersionRemoteServerApi());
    Assert.assertEquals(versionApiCompat, facade.getVersionApiCompat());
    Assert.assertEquals(versionRemote, facade.getVersionRemoteServer());
  }
  
  /**
   * Test a version with an extra digit at the end with a snapshot
   */
  @Test
  public void testGetVersionFourDigitsSanpshot() throws Exception
  {
    final String versionRemote = "2.38.1.1-SNAPSHOT";
    final Integer versionApiRemote = 38;
    final Integer versionApiCompat = versionApiRemote - 2;
    
    String versionResponse = TestBridgeBuilder.getVersionResponse(versionRemote);
    
    DHIS2Bridge facade = new DHIS2Bridge(new TestSingleResponseConnector(null, versionResponse, 200), versionApiCompat);
    
    facade.initialize();
    
    Assert.assertEquals(versionApiRemote, facade.getVersionRemoteServerApi());
    Assert.assertEquals(versionApiCompat, facade.getVersionApiCompat());
    Assert.assertEquals(versionRemote, facade.getVersionRemoteServer());
  }
  
  /**
   * Test a version with 'nightly' slapped at the end
   */
  @Test
  public void testGetVersionNightly() throws Exception
  {
    final String versionRemote = "2.38nightly";
    final Integer versionApiRemote = 38;
    final Integer versionApiCompat = versionApiRemote - 2;
    
    String versionResponse = TestBridgeBuilder.getVersionResponse(versionRemote);
    
    DHIS2Bridge facade = new DHIS2Bridge(new TestSingleResponseConnector(null, versionResponse, 200), versionApiCompat);
    
    facade.initialize();
    
    Assert.assertEquals(versionApiRemote, facade.getVersionRemoteServerApi());
    Assert.assertEquals(versionApiCompat, facade.getVersionApiCompat());
    Assert.assertEquals(versionRemote, facade.getVersionRemoteServer());
  }
}
