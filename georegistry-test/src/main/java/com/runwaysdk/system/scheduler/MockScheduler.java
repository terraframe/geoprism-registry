/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
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
