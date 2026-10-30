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
