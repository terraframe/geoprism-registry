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
      IntegrationTestController.testData = USATestData.newTestData();
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
        IntegrationTestController.testData.cleanUp();
      }
      finally
      {
        lock.unlock();
      }
    }
    
    return new RestResponse();
  }
}
