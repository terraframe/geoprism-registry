package net.geoprism.registry.axon;

import java.sql.Connection;
import java.sql.SQLException;

import org.axonframework.common.transaction.Transaction;
import org.axonframework.common.transaction.TransactionManager;

import com.runwaysdk.dataaccess.database.DatabaseException;

public class RunwayTransactionManager implements TransactionManager
{
  private RunwayRequestConnectionProvider provider;

  public RunwayTransactionManager(RunwayRequestConnectionProvider provider)
  {
    this.provider = provider;
  }
  
  @Override
  public Transaction startTransaction()
  {
    try
    {
      final Connection connection = this.provider.getConnection();

      return new Transaction()
      {

        @Override
        public void commit()
        {
          try
          {
            connection.commit();
          }
          catch (SQLException e)
          {
            throw new DatabaseException(e);
          }
        }

        @Override
        public void rollback()
        {
          try
          {
            connection.rollback();
          }
          catch (SQLException e)
          {
            throw new DatabaseException(e);
          }
        }
      };
    }
    catch (SQLException e)
    {
      throw new DatabaseException(e);
    }

  }

}
