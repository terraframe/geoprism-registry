package net.geoprism.registry.ws;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class NotificationFacade
{
  private static final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory()
  {
    public Thread newThread(Runnable r)
    {
      Thread t = Executors.defaultThreadFactory().newThread(r);
      t.setDaemon(true);
      return t;
    }
  });

  public static void queue(NotificationMessage message)
  {
    executor.execute(message);
  }
}
