package net.geoprism.registry.axon.config;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.axonframework.common.jdbc.ConnectionProvider;

import com.runwaysdk.session.RequestState;

public class RunwayRequestConnectionProvider implements ConnectionProvider
{
  public static class RequestConnectionWrapper implements Connection
  {
    protected RequestState state;

    public RequestConnectionWrapper(RequestState state)
    {
      this.state = state;
    }

    @Override
    public int getNetworkTimeout() throws SQLException
    {
      return this.state.getDatabaseConnection().getNetworkTimeout();
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException
    {
      this.state.getDatabaseConnection().setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public void close() throws SQLException
    {
      // state.getDatabaseConnection().commit();
      // state.close();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
      return this.state.getDatabaseConnection().isWrapperFor(iface);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
      return this.state.getDatabaseConnection().unwrap(iface);
    }

    @Override
    public void abort(Executor executor) throws SQLException
    {
      this.state.getDatabaseConnection().abort(executor);
    }

    @Override
    public void clearWarnings() throws SQLException
    {
      this.state.getDatabaseConnection().clearWarnings();
    }

    @Override
    public void commit() throws SQLException
    {
      // this.state.getDatabaseConnection().commit();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException
    {
      return this.state.getDatabaseConnection().createArrayOf(typeName, elements);
    }

    @Override
    public Blob createBlob() throws SQLException
    {
      return this.state.getDatabaseConnection().createBlob();
    }

    @Override
    public Clob createClob() throws SQLException
    {
      return this.state.getDatabaseConnection().createClob();
    }

    @Override
    public NClob createNClob() throws SQLException
    {
      return this.state.getDatabaseConnection().createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException
    {
      return this.state.getDatabaseConnection().createSQLXML();
    }

    @Override
    public Statement createStatement() throws SQLException
    {
      return this.state.getDatabaseConnection().createStatement();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException
    {
      return this.state.getDatabaseConnection().createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
    {
      return this.state.getDatabaseConnection().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException
    {
      return this.state.getDatabaseConnection().createStruct(typeName, attributes);
    }

    @Override
    public boolean getAutoCommit() throws SQLException
    {
      return this.state.getDatabaseConnection().getAutoCommit();
    }

    @Override
    public String getCatalog() throws SQLException
    {
      return this.state.getDatabaseConnection().getCatalog();
    }

    @Override
    public Properties getClientInfo() throws SQLException
    {
      return this.state.getDatabaseConnection().getClientInfo();
    }

    @Override
    public String getClientInfo(String name) throws SQLException
    {
      return this.state.getDatabaseConnection().getClientInfo(name);
    }

    @Override
    public int getHoldability() throws SQLException
    {
      return this.state.getDatabaseConnection().getHoldability();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException
    {
      return this.state.getDatabaseConnection().getMetaData();
    }

    @Override
    public String getSchema() throws SQLException
    {
      return this.state.getDatabaseConnection().getSchema();
    }

    @Override
    public int getTransactionIsolation() throws SQLException
    {
      return this.state.getDatabaseConnection().getTransactionIsolation();
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException
    {
      return this.state.getDatabaseConnection().getTypeMap();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException
    {
      return this.state.getDatabaseConnection().getWarnings();
    }

    @Override
    public boolean isClosed() throws SQLException
    {
      return this.state.getDatabaseConnection().isClosed();
    }

    @Override
    public boolean isReadOnly() throws SQLException
    {
      return this.state.getDatabaseConnection().isReadOnly();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException
    {
      return this.state.getDatabaseConnection().isValid(timeout);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException
    {
      return this.state.getDatabaseConnection().nativeSQL(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException
    {
      return this.state.getDatabaseConnection().prepareCall(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
    {
      return this.state.getDatabaseConnection().prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
    {
      return this.state.getDatabaseConnection().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException
    {
      return this.state.getDatabaseConnection().prepareStatement(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
    {
      return this.state.getDatabaseConnection().prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException
    {
      return this.state.getDatabaseConnection().prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException
    {
      return this.state.getDatabaseConnection().prepareStatement(sql, columnNames);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
    {
      return this.state.getDatabaseConnection().prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
    {
      return this.state.getDatabaseConnection().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException
    {
      this.state.getDatabaseConnection().releaseSavepoint(savepoint);
    }

    @Override
    public void rollback() throws SQLException
    {
      this.state.getDatabaseConnection().rollback();
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException
    {
      this.state.getDatabaseConnection().rollback(savepoint);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException
    {
      this.state.getDatabaseConnection().setAutoCommit(autoCommit);
    }

    @Override
    public void setCatalog(String catalog) throws SQLException
    {
      this.state.getDatabaseConnection().setCatalog(catalog);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException
    {
      this.state.getDatabaseConnection().setClientInfo(properties);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException
    {
      this.state.getDatabaseConnection().setClientInfo(name, value);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException
    {
      this.state.getDatabaseConnection().setHoldability(holdability);
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException
    {
      this.state.getDatabaseConnection().setReadOnly(readOnly);
    }

    @Override
    public Savepoint setSavepoint() throws SQLException
    {
      return this.state.getDatabaseConnection().setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException
    {
      return this.state.getDatabaseConnection().setSavepoint(name);
    }

    @Override
    public void setSchema(String schema) throws SQLException
    {
      this.state.getDatabaseConnection().setSchema(schema);
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException
    {
      this.state.getDatabaseConnection().setTransactionIsolation(level);
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException
    {
      this.state.getDatabaseConnection().setTypeMap(map);
    }

  }

  public static class StandaloneRequestConnectionWrapper extends RequestConnectionWrapper
  {

    public StandaloneRequestConnectionWrapper(RequestState state)
    {
      super(state);
    }

    @Override
    public void close() throws SQLException
    {
      try
      {
        state.getDatabaseConnection().commit();
      }
      finally
      {
        state.close();
      }
    }

    @Override
    public void commit() throws SQLException
    {
      this.state.getDatabaseConnection().commit();
    }
  }

  @Override
  public Connection getConnection() throws SQLException
  {
    if (RequestState.getCurrentRequestState() != null)
    {
      return new RequestConnectionWrapper(RequestState.getCurrentRequestState());
    }

    return new StandaloneRequestConnectionWrapper(RequestState.createRequestState());
  }

}
