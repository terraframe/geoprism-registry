package net.geoprism.registry.service.business;

import java.util.List;

import org.axonframework.eventhandling.TrackingToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import net.geoprism.registry.RollbackCheckpoint;
import net.geoprism.registry.RollbackCheckpoint.Status;
import net.geoprism.registry.axon.config.RegistryEventStore;
import net.geoprism.registry.RollbackCheckpointQuery;
import net.geoprism.registry.jobs.GPRJobHistory;

@Service
public class RollbackCheckpointBusinessService
{
  @Autowired
  private RollbackEventService service;

  @Autowired
  private RegistryEventStore   store;

  public RollbackCheckpoint create(GPRJobHistory history)
  {
    TrackingToken head = store.createHeadToken();
    long index = head != null ? head.position().orElse(0L) : 0L;

    return create(history, index);
  }

  public RollbackCheckpoint create(GPRJobHistory history, long globalIndex)
  {
    return create(history, globalIndex, RollbackCheckpoint.Status.AVAILABLE);
  }

  public RollbackCheckpoint create(GPRJobHistory history, long globalIndex, RollbackCheckpoint.Status status)
  {
    RollbackCheckpoint checkpoint = new RollbackCheckpoint();
    checkpoint.setGlobalIndex(globalIndex);
    checkpoint.setHistory(history);
    checkpoint.setStatus(status.name());
    checkpoint.apply();

    return checkpoint;
  }

  public void clear()
  {
    RollbackCheckpointQuery query = new RollbackCheckpointQuery(new QueryFactory());
    query.ORDER_BY_DESC(query.getCreateDate());

    try (OIterator<? extends RollbackCheckpoint> it = query.getIterator())
    {
      while (it.hasNext())
      {
        RollbackCheckpoint checkpoint = it.next();
        checkpoint.delete();
      }
    }
  }

  public void rollback(RollbackCheckpoint checkpoint)
  {
    // Ensure no other checkpoint is scheduled or running
    if (this.getExecutionCount() > 0)
    {
      throw new ProgrammingErrorException("A rollback is already in progress");
    }

    if (GPRJobHistory.getPendingCount() > 0)
    {
      throw new ProgrammingErrorException("The system cannot be rolledback because data imports are running or scheduled");
    }

    List<RollbackCheckpoint> checkpoints = this.getAfter(checkpoint);

    checkpoints.stream().forEach(ch -> {
      ch.appLock();
      ch.setStatus(Status.SCHEDULED.name());
      ch.apply();
    });

    checkpoints.stream().forEach(ch -> {
      ch.appLock();
      ch.setStatus(Status.RUNNING.name());
      ch.apply();

      this.service.rollback(ch);

      ch.delete();
    });
  }

  public long getExecutionCount()
  {
    RollbackCheckpointQuery query = new RollbackCheckpointQuery(new QueryFactory());
    query.WHERE(query.getStatus().NE(RollbackCheckpoint.Status.AVAILABLE.name()));

    return query.getCount();
  }

  @SuppressWarnings("unchecked")
  public List<RollbackCheckpoint> getExecutionList()
  {
    RollbackCheckpointQuery query = new RollbackCheckpointQuery(new QueryFactory());
    query.WHERE(query.getStatus().NE(RollbackCheckpoint.Status.AVAILABLE.name()));
    query.ORDER_BY_DESC(query.getCreateDate());

    try (OIterator<? extends RollbackCheckpoint> it = query.getIterator())
    {
      return (List<RollbackCheckpoint>) it.getAll();
    }

  }

  public long getCount()
  {
    RollbackCheckpointQuery query = new RollbackCheckpointQuery(new QueryFactory());

    return query.getCount();
  }

  @SuppressWarnings("unchecked")
  public List<RollbackCheckpoint> getAll(Integer pageSize, Integer pageNumber)
  {
    RollbackCheckpointQuery query = new RollbackCheckpointQuery(new QueryFactory());
    query.ORDER_BY_DESC(query.getCreateDate());
    query.restrictRows(pageSize, pageNumber);

    try (OIterator<? extends RollbackCheckpoint> it = query.getIterator())
    {
      return (List<RollbackCheckpoint>) it.getAll();
    }
  }

  @SuppressWarnings("unchecked")
  public List<RollbackCheckpoint> getAfter(RollbackCheckpoint checkpoint)
  {
    RollbackCheckpointQuery query = new RollbackCheckpointQuery(new QueryFactory());
    query.WHERE(query.getGlobalIndex().GE(checkpoint.getGlobalIndex()));
    query.ORDER_BY_DESC(query.getCreateDate());

    try (OIterator<? extends RollbackCheckpoint> it = query.getIterator())
    {
      return (List<RollbackCheckpoint>) it.getAll();
    }

  }

  public RollbackCheckpoint get(String oid)
  {
    return RollbackCheckpoint.get(oid);
  }

  @EventListener
  @Request
  public void onApplicationEvent(ContextRefreshedEvent event)
  {
    try
    {

      List<RollbackCheckpoint> list = this.getExecutionList();

      if (list.size() > 0)
      {
        this.rollback(list.get(list.size() - 1));
      }
    }
    catch (Exception e)
    {
      // Ignore. This will happen when the metadata is being imported
    }
  }

}
