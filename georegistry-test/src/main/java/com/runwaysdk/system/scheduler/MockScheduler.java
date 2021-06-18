package com.runwaysdk.system.scheduler;

import java.util.Date;

import net.geoprism.registry.etl.ImportHistory;

public class MockScheduler
{
  public static ExecutionContext executeJob(ExecutableJob job) throws Throwable
  {
    JobHistory history = job.createNewHistory();
    
    return executeJob(job, history);
  }
  
  public static ExecutionContext executeJob(ExecutableJob job, JobHistory history) throws Throwable
  {
    ExecutionContext context = new ExecutionContext();
    
    history.appLock();
    history.clearStatus();
    history.addStatus(AllJobStatus.RUNNING);
    history.apply();
    
    JobHistoryRecord record = new JobHistoryRecord(job, history);
    record.apply();
    
    context.setHistory(history);
    context.setJob(job);
    context.setHistory(history);
    context.setJobHistoryRecord(record);
    context.setRunAsUser(job.getRunAsUser());
    context.setRunAsDimension(job.getRunAsDimension());
    context.setExecutableJobToString(job.toString());
    
    Throwable error = null;
    
    try
    {
      job.execute(context);
    }
    catch (Throwable t)
    {
      error = t;
      
      throw t;
    }
    finally
    {
      JobHistory jh = JobHistory.get(history.getOid());

      jh.appLock();
      jh.setEndTime(new Date());
      jh.clearStatus();
      
      if (error != null)
      {
        jh.addStatus(AllJobStatus.FAILURE);
        
        jh.setError(error);
      }
      else
      {
        if(context.getStatus() != null)
        {
          jh.addStatus(context.getStatus());
        }
        else
        {
          jh.addStatus(AllJobStatus.SUCCESS);
        }
      }
      jh.apply();
      
      context.setStatus(jh.getStatus().get(0));
      context.setHistory(jh);
    }
    
    return context;
  }
}
