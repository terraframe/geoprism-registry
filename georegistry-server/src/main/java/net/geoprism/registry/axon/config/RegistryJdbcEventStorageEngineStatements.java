package net.geoprism.registry.axon.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.GenericDomainEventMessage;
import org.axonframework.eventsourcing.eventstore.jdbc.EventSchema;
import org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine;
import org.axonframework.eventsourcing.eventstore.jdbc.statements.TimestampWriter;
import org.axonframework.serialization.SerializedObject;
import org.axonframework.serialization.Serializer;

import net.geoprism.registry.Commit;
import net.geoprism.registry.axon.event.remote.RemoteEvent;
import net.geoprism.registry.axon.event.repository.RepositoryEvent;

public abstract class RegistryJdbcEventStorageEngineStatements
{
  protected static <T> DomainEventMessage<T> asDomainEventMessage(EventMessage<T> event)
  {
    return event instanceof DomainEventMessage<?> ? (DomainEventMessage<T>) event : new GenericDomainEventMessage<>(null, event.getIdentifier(), 0L, event, event::getTimestamp);
  }

  /**
   * Set the PreparedStatement to be used on
   * {@link JdbcEventStorageEngine#appendEvents(List, Serializer)}. Defaults to:
   * <p/>
   * {@code "INSERT INTO [domainEventTable] ([domainEventFields]) VALUES (?,?,?,?,?,?,?,?,?)" }
   * <p/>
   * <b>NOTE:</b> each "?" is a domain event field from
   * {@link EventSchema#domainEventFields()} and should <b>always</b> be present
   * for the PreparedStatement to work.
   *
   * @param connection
   *          The connection to the database.
   * @param schema
   *          The EventSchema to be used.
   * @param dataType
   *          The serialized type of the payload and metadata.
   * @param events
   *          The events to be added.
   * @param serializer
   *          The serializer for the payload and metadata.
   * @param timestampWriter
   *          Writer responsible for writing timestamp in the correct format for
   *          the given database.
   * @return The newly created {@link PreparedStatement}.
   * @throws SQLException
   *           when an exception occurs while creating the prepared statement.
   */
  public static PreparedStatement appendEvents(Connection connection, EventSchema schema, Class<?> dataType, List<? extends EventMessage<?>> events, Serializer serializer, TimestampWriter timestampWriter) throws SQLException
  {
    final String sql = "INSERT INTO " + schema.domainEventTable() + " (" + schema.domainEventFields() //
        + ", " + RegistryEventStore.BASE_OBJECT + ", " + RegistryEventStore.PHASE + ", commit_id" + ") " //
        + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
    PreparedStatement statement = connection.prepareStatement(sql);
    for (EventMessage<?> eventMessage : events)
    {
      DomainEventMessage<?> event = asDomainEventMessage(eventMessage);
      Object payload = event.getPayload();
      SerializedObject<?> sPayload = event.serializePayload(serializer, dataType);
      SerializedObject<?> sMetaData = event.serializeMetaData(serializer, dataType);

      statement.setString(1, event.getIdentifier());
      statement.setString(2, event.getAggregateIdentifier());
      statement.setLong(3, event.getSequenceNumber());
      statement.setString(4, event.getType());
      timestampWriter.writeTimestamp(statement, 5, event.getTimestamp());
      statement.setString(6, sPayload.getType().getName());
      statement.setString(7, sPayload.getType().getRevision());
      statement.setObject(8, sPayload.getData());
      statement.setObject(9, sMetaData.getData());

      if (payload instanceof RepositoryEvent)
      {
        statement.setString(10, ( (RepositoryEvent) payload ).getBaseObjectId());
        statement.setString(11, ( (RepositoryEvent) payload ).getEventPhase().name());
      }
      else
      {
        statement.setNull(10, Types.VARCHAR);
        statement.setNull(11, Types.VARCHAR);
      }

      if (payload instanceof RemoteEvent)
      {
        statement.setString(12, ( (RemoteEvent) payload ).getCommitId());
      }
      else
      {
        statement.setNull(12, Types.VARCHAR);
      }

      statement.addBatch();
    }

    return statement;
  }

  /**
   * Set the PreparedStatement to be used on
   * {@link JdbcEventStorageEngine#fetchDomainEvents(String, long, int)}
   * <p/>
   * {@code "SELECT [trackedEventFields] FROM [domainEventTable] WHERE [aggregateIdentifierColumn] = ?1 AND
   * [sequenceNumberColumn] >= ?2 AND [sequenceNumberColumn] < ?3 ORDER BY [sequenceNumberColumn] ASC" }
   * <p/>
   * <b>NOTE:</b> "?1" is the identifier, "?2" is the firstSequenceNumber and
   * "?3" is based on batchSize parameters from
   * {@link JdbcEventStorageEngine#fetchDomainEvents(String, long, int)} and
   * they should <b>always</b> be present for the PreparedStatement to work.
   *
   * @param connection
   *          The connection to the database.
   * @param schema
   *          The EventSchema to be used
   * @param firstIndexNumber
   *          The expected sequence number of the first returned entry.
   * @param batchSize
   *          The number of items to include in the batch.
   * @param identifier
   *          The identifier of the aggregate.
   * @return The newly created {@link PreparedStatement}.
   * @throws SQLException
   *           when an exception occurs while creating the prepared statement.
   */
  public static PreparedStatement readEventDataForCommit(Connection connection, EventSchema schema, //
      Commit commit, long firstIndexNumber, //
      int batchSize) throws SQLException
  {
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT " + schema.trackedEventFields() + "," + RegistryEventStore.BASE_OBJECT + " FROM " + schema.domainEventTable());
    sql.append(" WHERE " + "commit_id" + " = ?");
    sql.append(" AND " + schema.globalIndexColumn() + " >= ?");
    sql.append(" AND " + schema.globalIndexColumn() + " < ?");
    sql.append(" ORDER BY " + schema.globalIndexColumn() + " ASC");

    PreparedStatement statement = connection.prepareStatement(sql.toString());
    statement.setString(1, commit.getUid());
    statement.setLong(2, firstIndexNumber);
    statement.setLong(3, firstIndexNumber + batchSize);

    return statement;
  }

  public static PreparedStatement readEventDataForBaseObject(Connection connection, EventSchema schema, //
      String baseObjectId, long firstIndex, //
      Long lastIndex) throws SQLException
  {
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT " + schema.trackedEventFields() + "," + RegistryEventStore.BASE_OBJECT + " FROM " + schema.domainEventTable());
    sql.append(" WHERE " + RegistryEventStore.BASE_OBJECT + " = ?");
    sql.append(" AND " + schema.globalIndexColumn() + " > ?");

    if (lastIndex != null)
    {
      sql.append(" AND " + schema.globalIndexColumn() + " <= ?");
    }

    sql.append(" ORDER BY " + schema.globalIndexColumn() + " ASC");

    PreparedStatement statement = connection.prepareStatement(sql.toString());
    statement.setString(1, baseObjectId);
    statement.setLong(2, firstIndex);

    if (lastIndex != null)
    {
      statement.setLong(3, lastIndex);
    }

    return statement;
  }

  /**
   * Set the PreparedStatement to be used on
   * {@link JdbcEventStorageEngine#lastSequenceNumberFor(String)}. Defaults to:
   * <p/>
   * {@code "SELECT max([sequenceNumberColumn]) FROM [domainEventTable] WHERE [aggregateIdentifierColumn] = ?" }
   * <p/>
   * <b>NOTE:</b> "?" is the aggregateIdentifier parameter from
   * {@link JdbcEventStorageEngine#lastSequenceNumberFor(String)} and should
   * <b>always</b> be present for the PreparedStatement to work.
   *
   * @param connection
   *          The connection to the database.
   * @param schema
   *          The EventSchema to be used
   * @param aggregateIdentifier
   *          The identifier of the aggregate.
   * @return The newly created {@link PreparedStatement}.
   * @throws SQLException
   *           when an exception occurs while creating the prepared statement.
   */
  public static PreparedStatement lastIndexOf(Connection connection, EventSchema schema, Commit commit) throws SQLException
  {
    final String sql = "SELECT max(" + schema.globalIndexColumn() + ") FROM " + schema.domainEventTable() + " WHERE " + "commit_id" + " = ?";

    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setString(1, commit.getUid());
    return statement;
  }

  public static PreparedStatement firstIndexOf(Connection connection, EventSchema schema, Commit commit) throws SQLException
  {
    final String sql = "SELECT min(" + schema.globalIndexColumn() + ") FROM " + schema.domainEventTable() + " WHERE " + "commit_id" + " = ?";

    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setString(1, commit.getUid());
    return statement;
  }
}
