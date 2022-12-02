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
