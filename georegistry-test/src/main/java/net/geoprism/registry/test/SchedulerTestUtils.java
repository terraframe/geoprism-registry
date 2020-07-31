/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.test;

import org.junit.Assert;

import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.JobHistory;

public class SchedulerTestUtils
{
  @Request
  public static void waitUntilStatus(String histId, AllJobStatus status) throws InterruptedException
  {
    int waitTime = 0;
    while (true)
    {
      JobHistory hist = JobHistory.get(histId);
      if (hist.getStatus().get(0) == status)
      {
        break;
      }
      else if (hist.getStatus().get(0) == AllJobStatus.SUCCESS || hist.getStatus().get(0) == AllJobStatus.FAILURE
          || hist.getStatus().get(0) == AllJobStatus.FEEDBACK || hist.getStatus().get(0) == AllJobStatus.CANCELED
          || hist.getStatus().get(0) == AllJobStatus.STOPPED || hist.getStatus().get(0) == AllJobStatus.WARNING)
      {
        Assert.fail("Job has a finished status [" + hist.getStatus().get(0) + "] which is not what we expected.");
      }

      Thread.sleep(10);

      waitTime += 10;
      if (waitTime > 600000)
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
    waitTime += 100;
  }
}
