package net.geoprism.registry.service.business;

import java.util.List;

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
import net.geoprism.registry.RollbackCheckpointQuery;

@Service
public class RollbackCheckpointBusinessService
{
  @Autowired
  private RollbackEventService service;

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
    query.WHERE(query.getCreateDate().GE(checkpoint.getCreateDate()));
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
    }
  }

}
