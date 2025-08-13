/**
 *
 */
package net.geoprism.registry.test;

import java.util.List;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.Synonym;
import com.runwaysdk.system.gis.geo.SynonymQuery;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutableJob;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryRecord;
import com.runwaysdk.system.scheduler.JobHistoryRecordQuery;
import com.runwaysdk.system.scheduler.SchedulerManager;

import net.geoprism.registry.jobs.ImportError;
import net.geoprism.registry.jobs.ImportErrorQuery;
import net.geoprism.registry.jobs.ImportHistory;
import net.geoprism.registry.jobs.ValidationProblem;
import net.geoprism.registry.jobs.ValidationProblemQuery;
import net.geoprism.registry.service.request.ETLService;

public class SchedulerTestUtils
{
  private static final Logger logger = LoggerFactory.getLogger(SchedulerTestUtils.class);
  
  public static void waitUntilStatus(String histId, AllJobStatus status) throws InterruptedException
  {
    waitUntilStatus(histId, status, 1);
  }
  
  /**
   * 
   * @param histId
   * @param status
   * @param minWaitTime In the rare scenario where we are resuming an import from a previously completed history, we need to wait a little for the job to spool up, otherwise we could exit before the job even starts.
   * @throws InterruptedException
   */
  @Request
  public static void waitUntilStatus(String histId, AllJobStatus status, int minWaitTime) throws InterruptedException
  {
    int waitTime = 0;
    while (true)
    {
      JobHistory hist = JobHistory.get(histId);
      if (hist.getStatus().get(0) == status)
      {
        break;
      }
      else if (waitTime > minWaitTime && (hist.getStatus().get(0) == AllJobStatus.SUCCESS || hist.getStatus().get(0) == AllJobStatus.FAILURE
          || hist.getStatus().get(0) == AllJobStatus.FEEDBACK || hist.getStatus().get(0) == AllJobStatus.CANCELED
          || hist.getStatus().get(0) == AllJobStatus.STOPPED || hist.getStatus().get(0) == AllJobStatus.WARNING))
      {
        String extra = "";
        if (hist.getStatus().get(0).equals(AllJobStatus.FEEDBACK))
        {
          extra = new ETLService().getImportErrors(Session.getCurrentSession().getOid(), hist.getOid(), false, 10, 1).toString();
          
          String validationProblems = new ETLService().getValidationProblems(Session.getCurrentSession().getOid(), hist.getOid(), false, 10, 1).toString();

          extra = extra + " " + validationProblems;
        }
        
        Assert.fail("Job has a finished status [" + hist.getStatus().get(0) + "] which is not what we expected. " + extra);
      }

      Thread.sleep(10);

      waitTime += 10;
      if (waitTime > 200000)
      {
        String extra = "";
        if (hist.getStatus().get(0).equals(AllJobStatus.FEEDBACK))
        {
          extra = new ETLService().getImportErrors(Session.getCurrentSession().getOid(), hist.getOid(), false, 10, 1).toString();
          
          String validationProblems = new ETLService().getValidationProblems(Session.getCurrentSession().getOid(), hist.getOid(), false, 10, 1).toString();

          extra = extra + " " + validationProblems;
        }

        Assert.fail("Job was never scheduled (status is " + hist.getStatus().get(0).getEnumName() + "). " + extra);
        return;
      }
    }

    Thread.sleep(100);
    waitTime += 100;
  }
  
  @Request
  public static void clearImportData()
  {
    List<JobHistoryRecord> stoppedJobs = SchedulerManager.interruptAllRunningJobs();
    
    if (stoppedJobs.size() > 0)
    {
      logger.error("Forcefully interrupted " + stoppedJobs.size() + " running threads because they were still running after the test had finished. This will cause lots of cascading Interrupt exceptions in the running jobs!");
      
      try
      {
        Thread.sleep(2000); // Wait a few seconds for the job to stop
      }
      catch (InterruptedException e)
      {
        throw new RuntimeException(e);
      }
    }
    
    ValidationProblemQuery vpq = new ValidationProblemQuery(new QueryFactory());
    OIterator<? extends ValidationProblem> vpit = vpq.getIterator();
    while (vpit.hasNext())
    {
      ValidationProblem.lock(vpit.next().getOid()).delete();
    }

    ImportErrorQuery ieq = new ImportErrorQuery(new QueryFactory());
    OIterator<? extends ImportError> ieit = ieq.getIterator();
    while (ieit.hasNext())
    {
      ImportError.lock(ieit.next().getOid()).delete();
    }
    
    JobHistoryRecordQuery query = new JobHistoryRecordQuery(new QueryFactory());
    OIterator<? extends JobHistoryRecord> jhrs = query.getIterator();
    
    while (jhrs.hasNext())
    {
      JobHistoryRecord jhr = JobHistoryRecord.lock(jhrs.next().getOid());
      jhr.appLock();

      JobHistory hist = jhr.getChild();
      if (hist instanceof ImportHistory)
      {
        hist = ImportHistory.lock(hist.getOid());
        hist.appLock();
        
        ExecutableJob job = jhr.getParent();
        job.appLock();
        
        // If any tests are currently running, they will be errored out as a result of this.
        if (hist.getStatus().get(0).equals(AllJobStatus.RUNNING) || hist.getStatus().get(0).equals(AllJobStatus.NEW) || hist.getStatus().get(0).equals(AllJobStatus.QUEUED))
        {
          logger.error("History with oid [" + hist.getOid() + "] currently has status [" + hist.getStatus().get(0).getEnumName() + "] which is concerning because it is about to be deleted. This will probably cause errors in the running job.");
        }
        
//        hist = ImportHistory.lock(hist.getOid());
//        hist.appLock();
//        hist = ImportHistory.lock(hist.getOid());
//        VaultFile vf = ( (ImportHistory) hist ).getImportFile();
//        hist.setValue(ImportHistory.IMPORTFILE, null);
//        JobHistoryHistoryComment comment = hist.getHistoryComment();
//        hist.setValue(JobHistory.HISTORYCOMMENT, null);
//        JobHistoryHistoryInformation information = hist.getHistoryInformation();
//        hist.setValue(JobHistory.HISTORYINFORMATION, null);
//        hist.apply();
//        vf.delete();
//        comment.delete();
//        information.delete();
//        hist = ImportHistory.lock(hist.getOid());
//        hist.appLock();
//        hist = ImportHistory.lock(hist.getOid());
//        hist.delete();
        
        JobHistoryRecord.lock(jhr.getOid()).delete(); // This will also delete the history.
        ExecutableJob.lock(job.getOid()).delete();
      }
    }

    SynonymQuery sq = new SynonymQuery(new QueryFactory());
    sq.WHERE(sq.getDisplayLabel().localize().EQ("00"));
    OIterator<? extends Synonym> it = sq.getIterator();

    while (it.hasNext())
    {
      Synonym.lock(it.next().getOid()).delete();
    }
  }
}
