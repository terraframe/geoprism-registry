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
package net.geoprism.registry.test.integration;

import java.util.concurrent.locks.ReentrantLock;

import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestResponse;

import net.geoprism.registry.test.USATestData;

@Controller(url = "integrationtest")
public class IntegrationTestController
{
  private ReentrantLock lock = new ReentrantLock();
  
  protected static USATestData testData;
  
  public IntegrationTestController()
  {
  }
  
  @Endpoint(method = ServletMethod.GET)
  public synchronized ResponseIF testSetUp()
  {
    lock.lock();
    try
    {
      //IntegrationTestController.testData = USATestData.newTestData();
      //AndroidIntegrationTestDatabaseBuilder.build(testData);
    	
      IntegrationTestController.testData = USATestData.newTestDataForClass();
      testData.setUpMetadata();
      testData.setUpInstanceData();
      AndroidIntegrationTestDatabaseBuilder.build(testData);
    }
    finally
    {
      lock.unlock();
    }
    
    return new RestResponse();
  }
  
  @Endpoint(method = ServletMethod.GET)
  public ResponseIF testCleanUp()
  {
    if (IntegrationTestController.testData != null)
    {
      lock.lock();
      try
      {
    	  testData.tearDownInstanceData();
      }
      finally
      {
        lock.unlock();
      }
    }
    
    return new RestResponse();
  }
}
