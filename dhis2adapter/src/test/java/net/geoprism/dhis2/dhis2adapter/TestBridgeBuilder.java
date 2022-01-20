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
    return new DHIS2Bridge(new TestSingleResponseConnector(response, getVersionResponse(Constants.DHIS2_API_VERSION), statusCode), Constants.DHIS2_API_VERSION);
  }
  
  public static DHIS2Bridge buildFakeId()
  {
    return new DHIS2Bridge(new FakeIdConnector(getVersionResponse(Constants.DHIS2_API_VERSION)), Constants.DHIS2_API_VERSION);
  }
}
