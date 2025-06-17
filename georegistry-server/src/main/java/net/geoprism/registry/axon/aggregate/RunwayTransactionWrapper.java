package net.geoprism.registry.axon.aggregate;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

public class RunwayTransactionWrapper
{
  @Request
  public static void run(Runnable r)
  {
    transaction(r);
  }

  @Transaction
  private static void transaction(Runnable r)
  {
    r.run();
  }
}
