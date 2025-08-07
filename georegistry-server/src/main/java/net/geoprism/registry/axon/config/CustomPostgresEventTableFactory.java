package net.geoprism.registry.axon.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.axonframework.eventsourcing.eventstore.jdbc.AbstractEventTableFactory;
import org.axonframework.eventsourcing.eventstore.jdbc.EventSchema;

/**
 * Jdbc table factory for Postgresql databases.
 *
 * @author Rene de Waele
 * @since 3.0
 */
public class CustomPostgresEventTableFactory extends AbstractEventTableFactory
{

  /**
   * Singleton PostgresEventTableFactory instance
   */
  public static final CustomPostgresEventTableFactory INSTANCE = new CustomPostgresEventTableFactory();

  private CustomPostgresEventTableFactory()
  {
  }

  @Override
  public PreparedStatement createDomainEventTable(Connection connection, EventSchema schema) throws SQLException
  {
    String sql = "CREATE TABLE IF NOT EXISTS " + schema.domainEventTable() + " (\n" + //
        schema.globalIndexColumn() + " " + idColumnType() + " NOT NULL,\n" + //
        schema.aggregateIdentifierColumn() + " VARCHAR(255) NOT NULL,\n" + //
        schema.sequenceNumberColumn() + " BIGINT NOT NULL,\n" + //
        schema.typeColumn() + " VARCHAR(255),\n" + //
        schema.eventIdentifierColumn() + " VARCHAR(255) NOT NULL,\n" + //
        schema.metaDataColumn() + " " + payloadType() + ",\n" + //
        schema.payloadColumn() + " " + payloadType() + " NOT NULL,\n" + //
        schema.payloadRevisionColumn() + " VARCHAR(255),\n" + //
        schema.payloadTypeColumn() + " VARCHAR(255) NOT NULL,\n" + //
        schema.timestampColumn() + " " + timestampType() + " ,\n" + //
        "commit_id" + " " + " VARCHAR(255),\n" + //
        "PRIMARY KEY (" + schema.globalIndexColumn() + "),\n" + //
        "UNIQUE (" + schema.aggregateIdentifierColumn() + ", " + //
        schema.sequenceNumberColumn() + "),\n" + //
        "UNIQUE (" + schema.eventIdentifierColumn() + ")\n" + //
        ")";
    return connection.prepareStatement(sql);
  }

  @Override
  protected String idColumnType()
  {
    return "BIGSERIAL";
  }

  @Override
  protected String payloadType()
  {
    return "bytea";
  }
}
