package net.geoprism.registry.test;

import org.junit.Assert;

import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.JobHistory;

public class SchedulerTestUtils
{
  public static void waitUntilStatus(JobHistory hist, AllJobStatus status) throws InterruptedException
  {
    int waitTime = 0;
    while (true)
    {
      hist = JobHistory.get(hist.getOid());
      if (hist.getStatus().get(0) == status)
      {
        break;
      }
      else if (hist.getStatus().get(0) == AllJobStatus.SUCCESS || hist.getStatus().get(0) == AllJobStatus.FAILURE)
      {
        Assert.fail("Job has a finished status [" + hist.getStatus().get(0) + "] which is not what we expected.");
      }

      Thread.sleep(10);

      waitTime += 10;
      if (waitTime > 20000000)
      {
//        String extra = "";
//        if (hist.getStatus().get(0).equals(AllJobStatus.FEEDBACK))
//        {
//          extra = new ETLService().getImportErrors(Session.getCurrentSession().getOid(), hist.getOid(), false, 100, 1).toString();
//
//          extra = extra + " " + ( (ImportHistory) hist ).getValidationProblems();
//        }

        Assert.fail("Job was never scheduled (status is " + hist.getStatus().get(0).getEnumName() + ") ");
        return;
      }
    }

    Thread.sleep(100);
  }
}
