package net.geoprism.registry.service.business;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.axonframework.eventhandling.TrackingToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.stereotype.Service;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import net.geoprism.registry.RollbackCheckpoint;
import net.geoprism.registry.RollbackCheckpoint.Status;
import net.geoprism.registry.RollbackCheckpointQuery;
import net.geoprism.registry.axon.config.RegistryEventStore;
import net.geoprism.registry.io.view.ImportConfigurationDTO;
import net.geoprism.registry.jobs.GPRJobHistory;
import net.geoprism.registry.progress.Progress;
import net.geoprism.registry.progress.ProgressService;

@Service
public class RollbackCheckpointBusinessService
{
  private static final String   PROGRESS_KEY = "rollback";

  private static Logger         logger       = LoggerFactory.getLogger(RollbackCheckpointBusinessService.class);

  @Autowired
  private RollbackEventService  service;

  @Autowired
  private RegistryEventStore    store;

  @Autowired
  public ConcurrentTaskExecutor executor;

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

  public Future<?> rollback(RollbackCheckpoint checkpoint)
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

    return this.executor.submit(() -> this.execute(checkpoint));
  }

  @Request
  private void execute(RollbackCheckpoint checkpoint)
  {
    logger.info("Initiate rollback for checkpoint: " + checkpoint.getOid());

    List<RollbackCheckpoint> checkpoints = this.getAfter(checkpoint);

    logger.info("Total checkpoints to rollback: " + checkpoints.size());

    try
    {
      ProgressService.put(PROGRESS_KEY, new Progress(0L, checkpoints.size(), ""));

      checkpoints.stream().forEach(ch -> {
        ch.appLock();
        ch.setStatus(Status.SCHEDULED.name());
        ch.apply();
      });

      AtomicInteger count = new AtomicInteger(0);

      checkpoints.stream().forEach(ch -> {
        ch.appLock();
        ch.setStatus(Status.RUNNING.name());
        ch.apply();

        ProgressService.put(PROGRESS_KEY, new Progress(count.getAndIncrement(), checkpoints.size(), getDescription(ch)));

        this.service.rollback(ch);

        logger.info("Deleting checkpoint: " + ch.getOid());

        ch.delete();
      });
    }
    finally
    {
      ProgressService.put(PROGRESS_KEY, new Progress(100L, 100L, "Finished"));

      ProgressService.remove(PROGRESS_KEY);
    }
  }

  private String getDescription(RollbackCheckpoint ch)
  {
    GPRJobHistory history = ch.getHistory();

    if (StringUtils.isNotBlank(history.getConfigJson()) && history.getConfigJson().startsWith("{"))
    {
      ImportConfigurationDTO config = ImportConfigurationDTO.parseJson(history.getConfigJson());

      if (StringUtils.isNotBlank(config.getFileName()))
      {
        return "Rolling back [" + config.getFileName() + "]. This may take awhile.";
      }
    }

    return "Rolling back commit.  This may take awhile.";
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
    query.ORDER_BY_DESC(query.getGlobalIndex());

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
    query.ORDER_BY_DESC(query.getGlobalIndex());
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
    query.ORDER_BY_DESC(query.getGlobalIndex());

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

      if (list.size() > 0 && !this.store.isLocked())
      {
        executor.execute(() -> {

          // TODO: Find a better solution for this race condition
          // Give time for the metadata cache to be populated
          try
          {
            Thread.sleep(15000);
          }
          catch (InterruptedException e)
          {
          }

          this.execute(list.get(list.size() - 1));
        });
      }
    }
    catch (Exception e)
    {
      // Ignore. This will happen when the metadata is being imported
    }
  }

}
