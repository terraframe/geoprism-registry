package net.geoprism.registry.axon;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.DelegatingConnection;
import org.axonframework.common.jdbc.ConnectionProvider;

import com.runwaysdk.session.RequestState;

public class RunwayRequestConnectionProvider implements ConnectionProvider
{
  public static class RequestConnectionDelegate extends DelegatingConnection implements Connection
  {
    private RequestState state;

    public RequestConnectionDelegate(RequestState state)
    {
      super(state.getDatabaseConnection());
      
      this.state = state;
    }

    @Override
    public void close() throws SQLException
    {
      try
      {
        super.close();
      }
      finally
      {
        state.close();
      }
    }

  }

  @Override
  public Connection getConnection() throws SQLException
  {
    return new RequestConnectionDelegate(RequestState.createRequestState());
  }

}
