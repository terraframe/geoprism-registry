package net.geoprism.registry.axon.config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.axonframework.eventhandling.GapAwareTrackingToken;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStore;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.database.Database;

import net.geoprism.registry.Commit;

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

  public Long size()
  {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*)");
    statement.append(" FROM " + DOMAIN_EVENT_ENTRY_TABLE);

    try (ResultSet resultSet = Database.query(statement.toString()))
    {
      if (resultSet.next())
      {
        return resultSet.getLong(1);
      }

      return 0L;
    }
    catch (SQLException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public List<String> getAggregateIds(GapAwareTrackingToken start, GapAwareTrackingToken end)
  {
    LinkedList<String> aggregateIds = new LinkedList<>();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT DISTINCT aggregateidentifier");
    statement.append(" FROM " + DOMAIN_EVENT_ENTRY_TABLE);
    statement.append(" WHERE commit_id IS NULL");
    statement.append(" AND globalindex <= " + end.getIndex() );

    if (start != null)
    {
      statement.append(" AND globalindex > " + start.getIndex());
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
    return storageEngine().lastIndexOf(commit);
  }

  public Optional<Long> firstIndexFor(Commit commit)
  {
    return storageEngine().firstIndexOf(commit);
  }

  @Override
  protected RegistryEventStorageEngine storageEngine()
  {
    return (RegistryEventStorageEngine) super.storageEngine();
  }

  public DomainEventStream readEvents(@Nonnull Commit commit, long firstIndex, Long lastIndex, int batchSize)
  {
    return storageEngine().readEvents(commit, firstIndex, lastIndex, batchSize);
  }

  public DomainEventStream readEvents(String aggregateIdentifier, GapAwareTrackingToken start, GapAwareTrackingToken end)
  {
    return storageEngine().readEvents(aggregateIdentifier, start.getIndex(), end.getIndex());
  }

}
