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
package net.geoprism.registry.service;

import java.util.Locale;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.session.Request;

import net.geoprism.registry.TestConfig;
import net.geoprism.registry.localization.LocaleView;
import net.geoprism.registry.test.FastTestDataset;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
public class SupportedLocaleInstallAndRefreshDeadlockTest
{
  protected static FastTestDataset testData;
  
  protected static final Locale testLocale = Locale.SIMPLIFIED_CHINESE;
  
  @Autowired
  private LocalizationService                 service;
  
  @BeforeClass
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();
    testData.setUpMetadata();
    
    cleanupLocale();
  }

  @AfterClass
  public static void cleanUpClass()
  {
    testData.tearDownMetadata();
    
    cleanupLocale();
  }
  
  @Request
  public static void cleanupLocale()
  {
    if (LocalizationFacade.getInstalledLocales().contains(testLocale))
    {
      LocalizationFacade.uninstall(testLocale);
    }
  }
  
  @Test
  public void testDeadlock() throws InterruptedException
  {
    Thread refresher = new Thread(new RefreshLocaleCacheThreadPooler(), "SupportedLocaleRefreshDeadlockTest Locale Refresher Pooler");
    refresher.start();
    
    Thread.sleep(300);
    
    Thread installer = new Thread(new InstallLocaleThread(), "SupportedLocaleRefreshDeadlockTest Locale Installer");
    installer.start();
    
    installer.join();
    refresher.join();
  }
  
  public class InstallLocaleThread implements Runnable
  {
    @Override
    public void run()
    {
      this.installLocale();
    }
    
    @Request
    public void installLocale()
    {
      LocaleView lv = new LocaleView();
      lv.setLocale(testLocale);
      lv.setLabel(new LocalizedValue("SupportedLocaleRefreshDeadlockTest"));
      
      String json = lv.toJson().toString();
      
      FastTestDataset.runAsUser(FastTestDataset.USER_ADMIN, (request) -> {
        service.installLocaleInRequest(request.getSessionId(), json);
      });
    }
  }
  
  public static class RefreshLocaleCacheThreadPooler implements Runnable
  {
    @Override
    public void run()
    {
      int elapsedMilis = 0;
      
      while (elapsedMilis < 10000)
      {
        try
        {
          Thread.sleep(10);
        }
        catch (InterruptedException e)
        {
          throw new RuntimeException(e);
        }
        
        Thread refresher = new Thread(new RefreshLocaleCacheThread(), "SupportedLocaleRefreshDeadlockTest Locale Refresher");
        refresher.start();
        
        elapsedMilis += 10;
      }
    }
  }
  
  public static class RefreshLocaleCacheThread implements Runnable
  {

    @Override
    public void run()
    {
      Assert.assertNotNull(LocalizationFacade.getSupportedLocales());
    }
  }
}
