/**
 *
 */
package net.geoprism.registry.service;

import java.util.Locale;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.session.Request;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.model.localization.LocaleView;
import net.geoprism.registry.service.request.GPRLocalizationService;
import net.geoprism.registry.test.FastTestDataset;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc

@RunWith(SpringInstanceTestClassRunner.class)
public class SupportedLocaleInstallAndRefreshDeadlockTest extends FastDatasetTest implements InstanceTestClassListener
{
  protected static final Locale  testLocale = Locale.SIMPLIFIED_CHINESE;

  @Autowired
  private GPRLocalizationService service;

  @Override
  public void beforeClassSetup() throws Exception
  {
    super.beforeClassSetup();

    cleanupLocale();
  }

  @Override
  public void afterClassSetup() throws Exception
  {
    super.afterClassSetup();

    cleanupLocale();
  }

  @Request
  public void cleanupLocale()
  {
    if (testLocale != null && LocalizationFacade.getInstalledLocales().contains(testLocale))
    {
      LocaleView lv = new LocaleView();
      lv.setLocale(testLocale);
      lv.setLabel(new LocalizedValue("Test Locale"));

      this.service.uninstallLocale(lv);
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
