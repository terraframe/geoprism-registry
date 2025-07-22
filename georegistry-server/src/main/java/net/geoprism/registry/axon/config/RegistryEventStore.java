package net.geoprism.registry.axon.config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventhandling.GapAwareTrackingToken;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStore;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.database.Database;

import net.geoprism.registry.Commit;
import net.geoprism.registry.axon.event.remote.RemoteEvent;

public class RegistryEventStore extends EmbeddedEventStore implements EventStore
{
  public static final String DOMAIN_EVENT_ENTRY_TABLE = "domainevententry";

  protected RegistryEventStore(Builder builder)
  {
    super(builder);
  }

  public void delete(Class<?>... payloadTypes)
  {
    Arrays.asList(payloadTypes).forEach(cl -> {
      Database.deleteWhere(RegistryEventStore.DOMAIN_EVENT_ENTRY_TABLE, "payloadtype = '" + cl.getName() + "'");
    });

    StringBuilder statement = new StringBuilder();
    statement.append("TRUNCATE " + DOMAIN_EVENT_ENTRY_TABLE);

    Database.executeStatement(statement.toString());
  }

  public void truncate()
  {
    StringBuilder statement = new StringBuilder();
    statement.append("TRUNCATE " + DOMAIN_EVENT_ENTRY_TABLE);

    Database.executeStatement(statement.toString());
  }

  public void delete(Commit commit)
  {
    StringBuilder statement = new StringBuilder();
    statement.append("DELETE FROM " + DOMAIN_EVENT_ENTRY_TABLE);
    statement.append(" WHERE commit_id = '" + commit.getUid() + "'");

    Database.executeStatement(statement.toString());
  }

  public List<String> getAggregateIds(GapAwareTrackingToken start, GapAwareTrackingToken end)
  {
    LinkedList<String> aggregateIds = new LinkedList<>();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT DISTINCT aggregateidentifier");
    statement.append(" FROM " + DOMAIN_EVENT_ENTRY_TABLE);
    statement.append(" WHERE globalindex < " + end.getIndex());

    if (start != null)
    {
      statement.append(" AND globalindex >= " + start.getIndex());
    }

    try (ResultSet resultSet = Database.query(statement.toString()))
    {
      while (resultSet.next())
      {
        aggregateIds.add(resultSet.getString(1));
      }
    }
    catch (SQLException e)
    {
      throw new ProgrammingErrorException(e);
    }

    return aggregateIds;
  }

  public Optional<Long> lastIndexFor(Commit commit)
  {
    Optional<Long> highestStaged = stagedDomainEventMessages(commit) //
        .map(DomainEventMessage::getSequenceNumber)//
        .max(Long::compareTo);//

    if (highestStaged.isPresent())
    {
      return highestStaged;
    }

    return storageEngine().lastSequenceNumberFor(commit);
  }

  public Optional<Long> firstIndexFor(Commit commit)
  {
    Optional<Long> lowestStaged = stagedDomainEventMessages(commit) //
        .map(DomainEventMessage::getSequenceNumber)//
        .min(Long::compareTo);//

    if (lowestStaged.isPresent())
    {
      return lowestStaged;
    }

    return storageEngine().firstSequenceNumberFor(commit);
  }

  @Override
  protected RegistryEventStorageEngine storageEngine()
  {
    return (RegistryEventStorageEngine) super.storageEngine();
  }

  public DomainEventStream readEvents(@Nonnull Commit commit, long firstSequenceNumber, Long lastSequenceNumber, int batchSize)
  {
    return DomainEventStream.concat(//
        storageEngine().readEvents(commit, firstSequenceNumber, lastSequenceNumber, batchSize), //
        DomainEventStream.of(stagedDomainEventMessages(commit).filter(m -> m.getSequenceNumber() >= firstSequenceNumber)));
  }

  /**
   * Returns a Stream of all DomainEventMessages that have been staged for
   * publication by an Aggregate with given {@code aggregateIdentifier}.
   *
   * @param aggregateIdentifier
   *          The identifier of the aggregate to get staged events for
   * @return a Stream of DomainEventMessage of the identified aggregate
   */
  protected Stream<? extends DomainEventMessage<?>> stagedDomainEventMessages(Commit commit)
  {
    return queuedMessages().stream() //
        .filter(m -> {
          if (RemoteEvent.class.isAssignableFrom(m.getPayloadType()))
          {
            return ( (RemoteEvent) m.getPayload() ).getCommitId().equals(commit.getUid());
          }
          return false;
        }) //
        .map(m -> (DomainEventMessage<?>) m); //
  }

}
