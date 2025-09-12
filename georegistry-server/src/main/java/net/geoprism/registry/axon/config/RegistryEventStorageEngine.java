package net.geoprism.registry.axon.config;

import static java.lang.String.format;
import static org.axonframework.common.jdbc.JdbcUtils.executeQuery;
import static org.axonframework.common.jdbc.JdbcUtils.executeUpdates;
import static org.axonframework.common.jdbc.JdbcUtils.nextAndExtract;
import static org.axonframework.eventsourcing.EventStreamUtils.upcastAndDeserializeDomainEvents;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;

import org.axonframework.common.jdbc.JdbcUtils;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventhandling.DomainEventData;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.EventStoreException;
import org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine;

import net.geoprism.registry.Commit;

public class RegistryEventStorageEngine extends JdbcEventStorageEngine
{
  private static class EventStreamSpliterator<T> extends Spliterators.AbstractSpliterator<T>
  {

    private final Function<T, List<? extends T>> fetchFunction;

    private final Predicate<List<? extends T>>   finalBatchPredicate;

    private Iterator<? extends T>                iterator;

    private T                                    lastItem;

    private boolean                              lastBatchFound;

    private EventStreamSpliterator(Function<T, List<? extends T>> fetchFunction, Predicate<List<? extends T>> finalBatchPredicate)
    {
      super(Long.MAX_VALUE, NONNULL | ORDERED | DISTINCT | CONCURRENT);
      this.fetchFunction = fetchFunction;
      this.finalBatchPredicate = finalBatchPredicate;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action)
    {
      Objects.requireNonNull(action);
      if (iterator == null || !iterator.hasNext())
      {
        if (lastBatchFound)
        {
          return false;
        }
        List<? extends T> items = fetchFunction.apply(lastItem);
        lastBatchFound = finalBatchPredicate.test(items);
        iterator = items.iterator();
      }
      if (!iterator.hasNext())
      {
        return false;
      }

      action.accept(lastItem = iterator.next());
      return true;
    }
  }

  private final TransactionManager                         transactionManager;

  private final ReadEventDataForCommitStatementBuilder     readEventDataForCommit;

  private final ReadEventDataForBaseObjectStatementBuilder readEventDataForBaseObject;

  private final IndexOfCommitStatementBuilder              lastIndexForCommit;

  private final IndexOfCommitStatementBuilder              firstIndexForCommit;

  protected RegistryEventStorageEngine(Builder builder, TransactionManager transactionManager)
  {
    super(builder);

    this.transactionManager = transactionManager;
    this.readEventDataForCommit = RegistryJdbcEventStorageEngineStatements::readEventDataForCommit;
    this.readEventDataForBaseObject = RegistryJdbcEventStorageEngineStatements::readEventDataForBaseObject;
    this.lastIndexForCommit = RegistryJdbcEventStorageEngineStatements::lastIndexOf;
    this.firstIndexForCommit = RegistryJdbcEventStorageEngineStatements::firstIndexOf;
  }

  @SuppressWarnings("resource")
  public void createSchema(RegistryPostgresEventTableFactory schemaFactory)
  {
    super.createSchema(schemaFactory);

    executeUpdates(getConnection(), e -> {
      throw new EventStoreException("Failed to create event index", e);
    }, connection -> schemaFactory.createBaseObjectIndex(connection, schema()), //
        connection -> schemaFactory.createCommitIndex(connection, schema()), //
        connection -> schemaFactory.createLookupIndex(connection, schema()));
  }

  protected PreparedStatement readEventData(Connection connection, Commit commit, long firstIndex, Long lastIndex, int batchSize) throws SQLException
  {
    return readEventDataForCommit.build(connection, schema(), commit, firstIndex, batchSize);
  }

  protected Stream<? extends IndexEventData<?>> readEventData(Commit commit, long firstIndex, Long lastIndex, int batchSize)
  {

    EventStreamSpliterator<? extends IndexEventData<?>> spliterator = new EventStreamSpliterator<>( //
        lastItem -> fetchDomainEvents(commit, //
            lastItem == null ? firstIndex : ( lastItem.getGlobalIndex() + 1 ), //
            lastIndex, //
            batchSize),
        (list) -> {
          return list.size() < batchSize || list.get(list.size() - 1).getGlobalIndex().equals(lastIndex);
        }); //

    return StreamSupport.stream(spliterator, false);
  }

  public List<? extends IndexEventData<?>> fetchDomainEvents(Commit commit, long firstIndex, Long lastIndex, int batchSize)
  {
    return transactionManager.fetchInTransaction( //
        () -> executeQuery( //
            getConnection(), //
            connection -> readEventData(connection, commit, firstIndex, lastIndex, batchSize), //
            JdbcUtils.listResults(result -> this.getIndexDomainEventData(result, false)), //
            e -> new EventStoreException(format("Failed to read events for commit [%s]", commit), e) //
        ));
  }

  public DomainEventStream readBatch(Commit commit, Long firstIndex, Long lastIndex, int batchSize)
  {
    List<? extends IndexEventData<?>> events = fetchDomainEvents(commit, firstIndex, lastIndex, batchSize);

    return upcastAndDeserializeDomainEvents(events.stream(), getEventSerializer(), upcasterChain);
  }

  public DomainEventStream readEvents(Commit commit, long firstIndex, Long lastIndex, int batchSize)
  {
    Stream<? extends DomainEventData<?>> input = readEventData(commit, firstIndex, lastIndex, batchSize);
    return upcastAndDeserializeDomainEvents(input, getEventSerializer(), upcasterChain);
  }

  public Optional<Long> lastIndexOf(@Nonnull Commit commit)
  {
    return Optional.ofNullable(transactionManager.fetchInTransaction(//
        () -> executeQuery(getConnection(), //
            connection -> lastIndexOf(connection, commit), //
            resultSet -> nextAndExtract(resultSet, 1, Long.class), //
            e -> new EventStoreException( //
                format("Failed to read events for commit [%s]", commit.getUid()), e//
            )//
        )));
  }

  protected PreparedStatement lastIndexOf(Connection connection, Commit commit) throws SQLException
  {
    return lastIndexForCommit.build(connection, schema(), commit);
  }

  public Optional<Long> firstIndexOf(@Nonnull Commit commit)
  {
    return Optional.ofNullable(transactionManager.fetchInTransaction(//
        () -> executeQuery(getConnection(), //
            connection -> firstIndexOf(connection, commit), //
            resultSet -> nextAndExtract(resultSet, 1, Long.class), //
            e -> new EventStoreException( //
                format("Failed to read events for commit [%s]", commit.getUid()), e//
            )//
        )));
  }

  protected PreparedStatement firstIndexOf(Connection connection, Commit commit) throws SQLException
  {
    return firstIndexForCommit.build(connection, schema(), commit);
  }

  /**
   * Extracts the next domain event entry from the given {@code resultSet}.
   *
   * @param resultSet
   *          The results of a query for domain events of an aggregate.
   *
   * @return The next domain event.
   * @throws SQLException
   *           when an exception occurs while creating the event data.
   */
  protected IndexEventData<?> getIndexDomainEventData(ResultSet resultSet, boolean useBaseObjectId) throws SQLException
  {
    return new IndexGenericDomainEventEntry<>( //
        resultSet.getLong(schema().globalIndexColumn()), //
        "NoAggregate", //
        useBaseObjectId ? resultSet.getString(RegistryEventStore.BASE_OBJECT) : resultSet.getString(schema().aggregateIdentifierColumn()), //
        resultSet.getLong(schema().sequenceNumberColumn()), //
        resultSet.getString(schema().eventIdentifierColumn()), //
        readTimeStamp(resultSet, schema().timestampColumn()), //
        resultSet.getString(schema().payloadTypeColumn()), //
        resultSet.getString(schema().payloadRevisionColumn()), //
        readPayload(resultSet, schema().payloadColumn()), //
        readPayload(resultSet, schema().metaDataColumn()));
  }

  protected PreparedStatement readEventData(Connection connection, String baseObjectId, long firstIndex, Long lastIndex) throws SQLException
  {
    return readEventDataForBaseObject.build(connection, schema(), baseObjectId, firstIndex, lastIndex);
  }

  protected Stream<? extends IndexEventData<?>> readEventData(String baseObjectIdentifier, long firstIndex, Long lastIndex)
  {

    EventStreamSpliterator<? extends IndexEventData<?>> spliterator = new EventStreamSpliterator<>( //
        lastItem -> fetchEvents(baseObjectIdentifier, //
            lastItem == null ? firstIndex : ( lastItem.getGlobalIndex() + 1 ), //
            lastIndex),
        (list) -> true); //

    return StreamSupport.stream(spliterator, false);
  }

  public List<? extends IndexEventData<?>> fetchEvents(String baseObjectId, long firstIndex, Long lastIndex)
  {
    return transactionManager.fetchInTransaction( //
        () -> executeQuery( //
            getConnection(), //
            connection -> readEventData(connection, baseObjectId, firstIndex, lastIndex), //
            JdbcUtils.listResults(result -> this.getIndexDomainEventData(result, true)), //
            e -> new EventStoreException(format("Failed to read events for aggregateIdentifier [%s]", baseObjectId), e) //
        ));
  }

  public DomainEventStream readEvents(String baseObjectId, long firstIndex, Long lastIndex)
  {
    Stream<? extends IndexEventData<?>> input = readEventData(baseObjectId, firstIndex, lastIndex);
    return upcastAndDeserializeDomainEvents(input, getEventSerializer(), upcasterChain);
  }

}
