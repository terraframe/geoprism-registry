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
import org.axonframework.eventsourcing.eventstore.jdbc.statements.TimestampWriter;
import org.axonframework.serialization.SerializedObject;
import org.axonframework.serialization.Serializer;

import net.geoprism.registry.axon.event.remote.RemoteEvent;

public abstract class CustomJdbcEventStorageEngineStatements
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
    final String sql = "INSERT INTO " + schema.domainEventTable() + " (" + schema.domainEventFields() + ", commit_id" + ") " + "VALUES (?,?,?,?,?,?,?,?,?,?)";
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

      if (payload instanceof RemoteEvent)
      {
        statement.setString(10, ( (RemoteEvent) payload ).getCommitId());
      }
      else
      {
        statement.setNull(10, Types.VARCHAR);
      }

      statement.addBatch();
    }

    return statement;
  }

}
