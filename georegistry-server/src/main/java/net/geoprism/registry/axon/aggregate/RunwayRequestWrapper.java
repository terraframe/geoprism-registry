package net.geoprism.registry.axon.aggregate;

import com.runwaysdk.session.Request;

public class RunwayRequestWrapper
{
  @Request
  public static void run(Runnable r)
  {
    r.run();
  }
}
