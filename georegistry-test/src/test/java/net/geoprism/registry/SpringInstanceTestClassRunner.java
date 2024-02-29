package net.geoprism.registry;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

public class SpringInstanceTestClassRunner extends SpringJUnit4ClassRunner
{

  private InstanceTestClassListener InstanceSetupListener;

  public SpringInstanceTestClassRunner(Class<?> clazz) throws InitializationError
  {
    super(clazz);
  }

  @Override
  protected Object createTest() throws Exception
  {
    Object test = super.createTest();
    // Note that JUnit4 will call this createTest() multiple times for each
    // test method, so we need to ensure to call "beforeClassSetup" only once.
    if (test instanceof InstanceTestClassListener && InstanceSetupListener == null)
    {
      InstanceSetupListener = (InstanceTestClassListener) test;
      InstanceSetupListener.beforeClassSetup();
    }
    return test;
  }

  @Override
  public void run(RunNotifier notifier)
  {
    try
    {
      super.run(notifier);
    }
    finally
    {
      if (InstanceSetupListener != null)
      {
        try
        {
          InstanceSetupListener.afterClassSetup();
        }
        catch (Exception e)
        {
          throw new RuntimeException(e);
        }
      }
    }

  }
}
