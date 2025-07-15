package net.geoprism.registry.axon.config;

import static java.lang.String.format;
import static org.axonframework.common.jdbc.JdbcUtils.executeQuery;
import static org.axonframework.common.jdbc.JdbcUtils.nextAndExtract;
import static org.axonframework.eventsourcing.EventStreamUtils.upcastAndDeserializeDomainEvents;

import java.sql.Connection;
import java.sql.PreparedStatement;
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

  private final TransactionManager                            transactionManager;

  private final ReadEventDataForCommitStatementBuilder        readEventDataForCommit;

  private final SequenceNumberForCommitStatementBuilder       lastSequenceNumberForCommit;

  private final SequenceNumberForCommitStatementBuilder       firstSequenceNumberForCommit;

  protected RegistryEventStorageEngine(Builder builder, TransactionManager transactionManager)
  {
    super(builder);

    this.transactionManager = transactionManager;
    this.readEventDataForCommit = CustomJdbcEventStorageEngineStatements::readEventDataForCommit;
    this.lastSequenceNumberForCommit = CustomJdbcEventStorageEngineStatements::lastSequenceNumberFor;
    this.firstSequenceNumberForCommit = CustomJdbcEventStorageEngineStatements::firstSequenceNumberFor;
  }

  protected PreparedStatement readEventData(Connection connection, Commit commit, long firstSequenceNumber, Long lastSequenceNumber, int batchSize) throws SQLException
  {
    return readEventDataForCommit.build(connection, schema(), commit, firstSequenceNumber, batchSize);
  }

  protected Stream<? extends DomainEventData<?>> readEventData(Commit commit, long firstIndex, Long lastIndex, int batchSize)
  {

    EventStreamSpliterator<? extends DomainEventData<?>> spliterator = new EventStreamSpliterator<>( //
        lastItem -> fetchDomainEvents(commit, //
            lastItem == null ? firstIndex : lastItem.getSequenceNumber() + 1, //
            lastIndex, //
            batchSize),
        (list) -> {
          return list.size() < batchSize || list.get(list.size() - 1).getSequenceNumber() == lastIndex;          
        }); //
    
    return StreamSupport.stream(spliterator, false);
  }

  public List<? extends DomainEventData<?>> fetchDomainEvents(Commit commit, long firstIndex, Long lastIndex, int batchSize)
  {
    return transactionManager.fetchInTransaction( //
        () -> executeQuery( //
            getConnection(), //
            connection -> readEventData(connection, commit, firstIndex, lastIndex, batchSize), //
            JdbcUtils.listResults(this::getDomainEventData), //
            e -> new EventStoreException(format("Failed to read events for commit [%s]", commit), e) //
        ));
  }

  public DomainEventStream readEvents(Commit commit, long firstIndex, Long lastIndex, int batchSize)
  {
    Stream<? extends DomainEventData<?>> input = readEventData(commit, firstIndex, lastIndex, batchSize);
    return upcastAndDeserializeDomainEvents(input, getEventSerializer(), upcasterChain);
  }

  public Optional<Long> lastSequenceNumberFor(@Nonnull Commit commit)
  {
    return Optional.ofNullable(transactionManager.fetchInTransaction(//
        () -> executeQuery(getConnection(), //
            connection -> lastSequenceNumberFor(connection, commit), //
            resultSet -> nextAndExtract(resultSet, 1, Long.class), //
            e -> new EventStoreException( //
                format("Failed to read events for commit [%s]", commit.getUid()), e//
            )//
        )));
  }

  protected PreparedStatement lastSequenceNumberFor(Connection connection, Commit commit) throws SQLException
  {
    return lastSequenceNumberForCommit.build(connection, schema(), commit);
  }
  
  public Optional<Long> firstSequenceNumberFor(@Nonnull Commit commit)
  {
    return Optional.ofNullable(transactionManager.fetchInTransaction(//
        () -> executeQuery(getConnection(), //
            connection -> firstSequenceNumberFor(connection, commit), //
            resultSet -> nextAndExtract(resultSet, 1, Long.class), //
            e -> new EventStoreException( //
                format("Failed to read events for commit [%s]", commit.getUid()), e//
                )//
            )));
  }
  
  protected PreparedStatement firstSequenceNumberFor(Connection connection, Commit commit) throws SQLException
  {
    return firstSequenceNumberForCommit.build(connection, schema(), commit);
  }

}
