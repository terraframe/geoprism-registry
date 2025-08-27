package net.geoprism.registry.axon.config;

import org.axonframework.common.transaction.Transaction;
import org.axonframework.common.transaction.TransactionManager;

public class RunwayTransactionManager implements TransactionManager
{

//  @Request
//  public void executeInTransaction(Runnable task)
//  {
//    _executeInTransaction(task);
//  }
//
//  @com.runwaysdk.dataaccess.transaction.Transaction
//  protected void _executeInTransaction(Runnable task)
//  {
//    TransactionManager.super.executeInTransaction(task);
//  }
//
//  @Override
//  @Request
//  public <T> T fetchInTransaction(Supplier<T> supplier)
//  {
//    return _fetchInTransaction(supplier);
//  }
//
//  @com.runwaysdk.dataaccess.transaction.Transaction
//  protected <T> T _fetchInTransaction(Supplier<T> supplier)
//  {
//    return TransactionManager.super.fetchInTransaction(supplier);
//  }

  @Override
  public Transaction startTransaction()
  {
    return new Transaction()
    {

      @Override
      public void rollback()
      {
        // No Op
        System.out.println("Rollback");
      }

      @Override
      public void commit()
      {
        // No Op
        System.out.println("Commit");
      }
    };
  }

}
