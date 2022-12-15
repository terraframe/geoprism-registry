package net.geoprism.registry.service;

import java.util.Locale;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.session.Request;

import net.geoprism.registry.localization.LocaleView;
import net.geoprism.registry.localization.LocalizationService;
import net.geoprism.registry.test.FastTestDataset;

public class SupportedLocaleInstallAndRefreshDeadlockTest
{
  protected static FastTestDataset testData;
  
  protected static final Locale testLocale = Locale.SIMPLIFIED_CHINESE;
  
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
  
  public static class InstallLocaleThread implements Runnable
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
      
      FastTestDataset.runAsUser(FastTestDataset.USER_ADMIN, (request, adapter) -> {
        new LocalizationService().installLocaleInRequest(request.getSessionId(), json);
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
