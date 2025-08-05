package net.geoprism.registry.axon.config;

import static java.lang.String.format;
import static org.axonframework.common.jdbc.JdbcUtils.executeQuery;
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

  private final TransactionManager                        transactionManager;

  private final ReadEventDataForCommitStatementBuilder    readEventDataForCommit;

  private final ReadEventDataForAggregateStatementBuilder readEventDataForAggregate;

  private final IndexOfCommitStatementBuilder   lastSequenceNumberForCommit;

  private final IndexOfCommitStatementBuilder   firstSequenceNumberForCommit;

  protected RegistryEventStorageEngine(Builder builder, TransactionManager transactionManager)
  {
    super(builder);

    this.transactionManager = transactionManager;
    this.readEventDataForCommit = CustomJdbcEventStorageEngineStatements::readEventDataForCommit;
    this.readEventDataForAggregate = CustomJdbcEventStorageEngineStatements::readEventDataForAggregate;
    this.lastSequenceNumberForCommit = CustomJdbcEventStorageEngineStatements::lastIndexOf;
    this.firstSequenceNumberForCommit = CustomJdbcEventStorageEngineStatements::firstIndexOf;
  }

  protected PreparedStatement readEventData(Connection connection, Commit commit, long firstIndex, Long lastIndex, int batchSize) throws SQLException
  {
    return readEventDataForCommit.build(connection, schema(), commit, firstIndex, batchSize);
  }

  protected Stream<? extends IndexDomainEventData<?>> readEventData(Commit commit, long firstIndex, Long lastIndex, int batchSize)
  {

    EventStreamSpliterator<? extends IndexDomainEventData<?>> spliterator = new EventStreamSpliterator<>( //
        lastItem -> fetchDomainEvents(commit, //
            lastItem == null ? firstIndex : lastItem.getGlobalIndex(), //
            lastIndex, //
            batchSize),
        (list) -> {
          return list.size() < batchSize || list.get(list.size() - 1).getGlobalIndex() == lastIndex;
        }); //

    return StreamSupport.stream(spliterator, false);
  }

  public List<? extends IndexDomainEventData<?>> fetchDomainEvents(Commit commit, long firstIndex, Long lastIndex, int batchSize)
  {
    return transactionManager.fetchInTransaction( //
        () -> executeQuery( //
            getConnection(), //
            connection -> readEventData(connection, commit, firstIndex, lastIndex, batchSize), //
            JdbcUtils.listResults(this::getIndexDomainEventData), //
            e -> new EventStoreException(format("Failed to read events for commit [%s]", commit), e) //
        ));
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
    return lastSequenceNumberForCommit.build(connection, schema(), commit);
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
    return firstSequenceNumberForCommit.build(connection, schema(), commit);
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
  protected IndexDomainEventData<?> getIndexDomainEventData(ResultSet resultSet) throws SQLException
  {
    return new IndexGenericDomainEventEntry<>( //
        resultSet.getLong(schema().globalIndexColumn()), //
        resultSet.getString(schema().typeColumn()), //
        resultSet.getString(schema().aggregateIdentifierColumn()), //
        resultSet.getLong(schema().sequenceNumberColumn()), //
        resultSet.getString(schema().eventIdentifierColumn()), //
        readTimeStamp(resultSet, schema().timestampColumn()), //
        resultSet.getString(schema().payloadTypeColumn()), //
        resultSet.getString(schema().payloadRevisionColumn()), //
        readPayload(resultSet, schema().payloadColumn()), //
        readPayload(resultSet, schema().metaDataColumn()));
  }

  protected PreparedStatement readEventData(Connection connection, String aggregateIdentifier, long firstIndex, Long lastIndex) throws SQLException
  {
    return readEventDataForAggregate.build(connection, schema(), aggregateIdentifier, firstIndex, lastIndex);
  }

  protected Stream<? extends IndexDomainEventData<?>> readEventData(String aggregateIdentifier, long firstIndex, Long lastIndex)
  {

    EventStreamSpliterator<? extends IndexDomainEventData<?>> spliterator = new EventStreamSpliterator<>( //
        lastItem -> fetchDomainEvents(aggregateIdentifier, //
            lastItem == null ? firstIndex : lastItem.getGlobalIndex() + 1, //
            lastIndex),
        (list) -> true); //

    return StreamSupport.stream(spliterator, false);
  }

  public List<? extends IndexDomainEventData<?>> fetchDomainEvents(String aggregateIdentifier, long firstIndex, Long lastIndex)
  {
    return transactionManager.fetchInTransaction( //
        () -> executeQuery( //
            getConnection(), //
            connection -> readEventData(connection, aggregateIdentifier, firstIndex, lastIndex), //
            JdbcUtils.listResults(this::getIndexDomainEventData), //
            e -> new EventStoreException(format("Failed to read events for aggregateIdentifier [%s]", aggregateIdentifier), e) //
        ));
  }
  

  public DomainEventStream readEvents(String aggregateIdentifier, long firstIndex, Long lastIndex)
  {
    Stream<? extends DomainEventData<?>> input = readEventData(aggregateIdentifier, firstIndex, lastIndex);
    return upcastAndDeserializeDomainEvents(input, getEventSerializer(), upcasterChain);
  }

}
