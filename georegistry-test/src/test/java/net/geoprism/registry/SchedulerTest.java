/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
/**
*
*/
package net.geoprism.registry;

import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;

import com.runwaysdk.ClientSession;
import com.runwaysdk.RunwayException;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.constants.ServerConstants;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.metadata.BackupReadException;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutableJob;
import com.runwaysdk.system.scheduler.ExecutableJobIF;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryQuery;
import com.runwaysdk.system.scheduler.JobHistoryRecord;
import com.runwaysdk.system.scheduler.JobHistoryRecordQuery;
import com.runwaysdk.system.scheduler.QualifiedTypeJob;
import com.runwaysdk.system.scheduler.QuartzRunwayJob;
import com.runwaysdk.system.scheduler.QueueingQuartzJob;
import com.runwaysdk.system.scheduler.SchedulerJobCannotResumeException;
import com.runwaysdk.system.scheduler.SchedulerManager;

public class SchedulerTest
{

  private static final boolean     HALT          = false;

  protected static ClientSession   systemSession = null;

  protected static ClientRequestIF clientRequest = null;
  
  private static final Integer TEST_JOB_RUN_TIME = 2000;
  
  private static final Integer QUEUE_JOB_RUN_TIME = 3000;

  // TEST BOILERPLATE

  /*
   * Tracks execution by Jobs.
   */
  private static class TestRecord
  {
    private static Map<String, TestRecord> records = new ConcurrentHashMap<String, TestRecord>();

    /**
     * The oid of the Job that this is recorded against.
     */
    private final String                   oid;

    /**
     * The number of executions.
     */
    private int                            count;

    /**
     * Denotes if the job was executed.
     */
    private boolean                        executed;

    private TestRecord(String oid)
    {
      this.oid = oid;
      this.count = 0;
      this.executed = false;
    }

    /**
     * @return the oid
     */
    public String getOid()
    {
      return oid;
    }

    /**
     * @return the count
     */
    public synchronized int getCount()
    {
      return count;
    }

    /**
     * @return the executed
     */
    public synchronized boolean isExecuted()
    {
      return executed;
    }

    public synchronized void recordOnce()
    {
      if (this.count > 0)
      {
        throw new ProgrammingErrorException("Job [" + oid + "] has already executed.");
      }
      else
      {
        this.record();
      }
    }

    public synchronized void record()
    {
      this.executed = true;
      this.count++;
    }

    public static TestRecord newRecord(ExecutableJob job)
    {
      String oid = job.getOid();
      synchronized (records)
      {
        if (records.containsKey(oid))
        {
          throw new ProgrammingErrorException("Job [" + oid + "] already recorded.");
        }
        else
        {
          TestRecord tr = new TestRecord(oid);
          records.put(oid, tr);
          return tr;
        }

      }
    }
  }

  /*
   * Basic job that records its execution count.
   */
  public static class TestJob implements ExecutableJobIF
  {
    /**
     * Execution method that modifies its associated TestRecord.
     */
    @Override
    public void execute(ExecutionContext executionContext)
    {
      ExecutableJob job = executionContext.getJob();
      String oid = job.getOid();

      JobHistory history = executionContext.getHistory();
      history.lock();
      try
      {
        Thread.sleep(TEST_JOB_RUN_TIME);
      }
      catch (InterruptedException e)
      {
        throw new RuntimeException(e);
      }
      history.unlock();

      TestRecord testRecord = TestRecord.records.get(oid);
      testRecord.recordOnce();
    }
    
    @Override
    public QuartzRunwayJob getQuartzJob(ExecutableJob execJob)
    {
      return new TestQuartzJob(execJob);
    }
  }
  
  /*
   * Test job that only allows one to be running at the same time. Subsequent jobs will be queued.
   */
  public static class TestQueueingQuartzJob implements ExecutableJobIF
  {
    /**
     * Execution method that modifies its associated TestRecord.
     */
    @Override
    public void execute(ExecutionContext executionContext)
    {
      ExecutableJob job = executionContext.getJob();
      String oid = job.getOid();

      JobHistory history = executionContext.getHistory();
      history.lock();
      try
      {
        System.out.println("Quartz queueing job [" + executionContext.getJobHistoryRecord().getOid() + "] is executing"); // TODO delete
        Thread.sleep(QUEUE_JOB_RUN_TIME);
      }
      catch (InterruptedException e)
      {
        throw new RuntimeException(e);
      }
      history.unlock();

      TestRecord testRecord = TestRecord.records.get(oid);
//      testRecord.recordOnce();
      testRecord.record();
    }
    
    @Override
    public QuartzRunwayJob getQuartzJob(ExecutableJob execJob)
    {
      return new QueueingQuartzJob(execJob);
    }
  }
  
  public static class TestQuartzJob extends QuartzRunwayJob implements Job
  {
    public TestQuartzJob()
    {
      super();
    }
    
    public TestQuartzJob(ExecutableJob execJob)
    {
      super(execJob);
    }
    
    @Override
    public void jobExecutionVetoed(JobExecutionContext context)
    {
      throw new RuntimeException("Job execution was vetoed");
    }
    
    @Override
    public void triggerMisfired(Trigger trigger)
    {
      super.triggerMisfired(trigger);
      
      System.out.println("Trigger misfired");
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, CompletedExecutionInstruction triggerInstructionCode)
    {
      super.triggerComplete(trigger, context, triggerInstructionCode);
      
      System.out.println("triggerComplete");
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context)
    {
      super.jobToBeExecuted(context);
      
      System.out.println("jobToBeExecuted");
    }
    
    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException)
    {
      super.jobWasExecuted(context, jobException);
      
      System.out.println("jobWasExecuted");
    }
    
    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context)
    {
      System.out.println("vetoJobExecution");
      
      return super.vetoJobExecution(trigger, context);
    }
    
    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context)
    {
      super.triggerFired(trigger, context);
      
      System.out.println("Trigger fired");
    }
  }

  public static class TestSmartErrorJob implements ExecutableJobIF
  {
    @Override
    public void execute(ExecutionContext executionContext)
    {
      try
      {
        Thread.sleep(100);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
      
      ExecutableJob job = executionContext.getJob();
      String oid = job.getOid();
      
      TestRecord testRecord = TestRecord.records.get(oid);
      testRecord.record();
      
      BackupReadException ex = new BackupReadException();
      ex.setLocation("/test/123");
      throw ex;
    }
    
    @Override
    public QuartzRunwayJob getQuartzJob(ExecutableJob execJob)
    {
      return null;
    }
  }
  
  public static class TestRunwayErrorJob implements ExecutableJobIF
  {
    @Override
    public void execute(ExecutionContext executionContext) throws Throwable
    {
      try
      {
        Thread.sleep(100);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
      
      ExecutableJob job = executionContext.getJob();
      String oid = job.getOid();
      
      TestRecord testRecord = TestRecord.records.get(oid);
      testRecord.record();
      
      throw buildRunwayJobError();
    }
    
    @Override
    public QuartzRunwayJob getQuartzJob(ExecutableJob execJob)
    {
      return null;
    }
  }
  
  public static class TestArithmeticErrorJob implements ExecutableJobIF
  {
    @Override
    public void execute(ExecutionContext executionContext) throws Throwable
    {
      try
      {
        Thread.sleep(100);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
      
      ExecutableJob job = executionContext.getJob();
      String oid = job.getOid();
      
      TestRecord testRecord = TestRecord.records.get(oid);
      testRecord.record();
      
      int fail = 10 / 0;
      System.out.println(fail);
    }
    
    @Override
    public QuartzRunwayJob getQuartzJob(ExecutableJob execJob)
    {
      return null;
    }
  }
  
  @BeforeClass
  @Request
  public static void classSetUp()
  {
    systemSession = ClientSession.createUserSession("default", ServerConstants.SYSTEM_USER_NAME, ServerConstants.SYSTEM_DEFAULT_PASSWORD, new Locale[] { CommonProperties.getDefaultLocale() });
    clientRequest = systemSession.getRequest();

    SchedulerManager.start();
  }

  @AfterClass
  @Request
  public static void classTearDown()
  {
    systemSession.logout();
    SchedulerManager.shutdown();
  }

  /**
   * Custom wait method that stalls until the TestRecord is modified and only
   * within a specific number of retries.
   * 
   * @param tr
   * @param maxWaits
   */
  private void wait(TestRecord tr, int maxWaits)
  {
    try
    {
      int runs = 0;
      while (!tr.isExecuted())
      {
        if (runs > maxWaits)
        {
          Assert.fail("The record [" + tr.getOid() + "] took longer than [" + maxWaits + "] retries to complete.");
        }

        // Let's wait a while and try again.
        Thread.sleep(HALT ? Long.MAX_VALUE : 1000);
        runs++;
      }
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }

  private void clearHistory()
  {
    JobHistoryRecordQuery query = new JobHistoryRecordQuery(new QueryFactory());
    OIterator<? extends JobHistoryRecord> jhrs = query.getIterator();

    while (jhrs.hasNext())
    {
      JobHistoryRecord jhr = jhrs.next();
      jhr.getChild().delete();
    }
  }

  /**
   * Tests the execution of a job via CRON scheduling.
   * 
   * @throws InterruptedException
   */
  @Request
  @Test
  public void testCRONSchedule() throws InterruptedException
  {
    ExecutableJob job = QualifiedTypeJob.newInstance(TestJob.class);
    job.getDisplayLabel().setValue("testCRONSchedule");
    job.setCronExpression("0/5 * * * * ?");

    try
    {
      job.apply();

      TestRecord tr = TestRecord.newRecord(job);

      wait(tr, 20);

      if (tr.isExecuted() && tr.getCount() == 1)
      {
        Assert.assertEquals(0, SchedulerManager.getRunningJobs().size());
        
        OIterator<? extends JobHistory> it = job.getAllJobHistory();
        Assert.assertTrue(it.hasNext());
        
        while (it.hasNext())
        {
          JobHistory history = it.next();
          Assert.assertTrue(!it.hasNext());
          
          Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), history.getStatus().get(0).getEnumName());
          Assert.assertEquals(0, SchedulerManager.getRunningJobs().size());
          Assert.assertNotNull(history.getEndTime());
          Assert.assertTrue(history.getEndTime().after(history.getStartTime()));
        }
      }
      else
      {
        Assert.fail("The job was not completed.");
      }
    }
    finally
    {
      Thread.sleep(500);
      ExecutableJob.get(job.getOid()).delete();
      clearHistory();
    }
  }

  /**
   * Tests the execution of a job once via invoking the "start" MDMethod.
   * 
   * @throws InterruptedException
   */
  @Request
  @Test
  public void testManuallyStartJob() throws InterruptedException
  {
    ExecutableJob job = QualifiedTypeJob.newInstance(TestJob.class);
    job.getDisplayLabel().setValue("testManuallyStartJob");

    try
    {
      job.apply();

      TestRecord tr = TestRecord.newRecord(job);

      JobHistory history = job.start();
      Date startTime = history.getStartTime();

      Assert.assertNotNull(startTime);
      Assert.assertTrue("Expected status of NEW or RUNNING", history.getStatus().get(0).equals(AllJobStatus.RUNNING) || history.getStatus().get(0).equals(AllJobStatus.NEW));
      
      this.waitUntilRunning(history);
      
      history = JobHistory.get(history.getOid());
      Assert.assertEquals(AllJobStatus.RUNNING.getEnumName(), history.getStatus().get(0).getEnumName());

      wait(tr, 10);
      
      if (tr.isExecuted() && tr.getCount() == 1)
      {
        Thread.sleep(500);
        
        JobHistory updated = JobHistory.getByKey(history.getKey());
        Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), updated.getStatus().get(0).getEnumName());
        Assert.assertEquals(0, SchedulerManager.getRunningJobs().size());
        Assert.assertNotNull(updated.getEndTime());
        Assert.assertTrue(updated.getStartTime() + " was expected to be after " + updated.getEndTime(), updated.getEndTime().after(startTime));
      }
      else
      {
        Assert.fail("The job was not completed.");
      }
    }
    finally
    {
      Thread.sleep(1000);
      ExecutableJob.get(job.getOid()).delete();
      clearHistory();
    }
  }

  /**
   * Tests to make sure that a job will stop running when the CRON string is
   * modified. Also tests to make sure that jobs can be modified while running.
   * 
   * @throws InterruptedException
   */
  @Request
  @Test
  public void testModifyCRONSchedule() throws InterruptedException
  {
    ExecutableJob job = QualifiedTypeJob.newInstance(TestJob.class);
    job.getDisplayLabel().setValue("testModifyCRONSchedule");
    job.setCronExpression("0/5 * * * * ?");

    try
    {
      job.apply();

      TestRecord tr = TestRecord.newRecord(job);

      // Wait till the job is running
      int waitTime = 0;
      while (SchedulerManager.getRunningJobs().size() == 0)
      {
        Thread.sleep(10);

        waitTime += 10;
        if (waitTime > 6000)
        {
          Assert.fail("Job was never scheduled");
          return;
        }
      }
      
      // Modify the CRON string to never run
      job = ExecutableJob.get(job.getOid());
      job.setCronExpression("");
      job.apply();
      
      // Wait till the job is no longer running
      Thread.sleep((long) (TEST_JOB_RUN_TIME*1.5));

      // Make sure the job never starts up again.
      waitTime = 0;
      while (waitTime < TEST_JOB_RUN_TIME * 5)
      {
        Thread.sleep(100);
        waitTime += 100;

        Assert.assertEquals(0, SchedulerManager.getRunningJobs().size());
      }
    }
    finally
    {
      Thread.sleep(500);
      ExecutableJob.get(job.getOid()).delete();
      clearHistory();
    }
  }

  /**
   * Tests the clearHistory MdMethod defined on JobHistory.
   */
  @Request
  @Test
  public void testClearHistory()
  {
    ExecutableJob job1 = QualifiedTypeJob.newInstance(TestJob.class);
    job1.getDisplayLabel().setValue("testClearHistory1");
    job1.apply();
    TestRecord tr1 = TestRecord.newRecord(job1);

    ExecutableJob job2 = QualifiedTypeJob.newInstance(TestJob.class);
    job2.getDisplayLabel().setValue("testClearHistory2");
    job2.apply();
    // TestRecord tr2 = TestRecord.newRecord(job2);
    TestRecord.newRecord(job2);

    try
    {
      // First create a history item by running job1.
      job1.start();
      wait(tr1, 10);

      Assert.assertEquals(1, new JobHistoryQuery(new QueryFactory()).getCount());

      JobHistory hist = job2.start();

      waitUntilRunning(hist);

      Assert.assertEquals(2, new JobHistoryQuery(new QueryFactory()).getCount());
      Assert.assertEquals(1, SchedulerManager.getRunningJobs().size());

      // Invoke the md method. This should only remove 1 of the histories,
      // because the other one is currently running.
      JobHistory.clearHistory();

      Assert.assertEquals(1, new JobHistoryQuery(new QueryFactory()).getCount());

      Thread.sleep(TEST_JOB_RUN_TIME*2);

      JobHistory.clearHistory();

      Assert.assertEquals(0, new JobHistoryQuery(new QueryFactory()).getCount());
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
    finally
    {
      ExecutableJob.get(job1.getOid()).delete();
      ExecutableJob.get(job2.getOid()).delete();
      clearHistory();
    }
  }

  @Request
  @Test
  public void testQueueDifferentJobs() throws InterruptedException
  {
    ExecutableJob job1 = QualifiedTypeJob.newInstance(TestQueueingQuartzJob.class);
    job1.getDisplayLabel().setValue("testQueue1");
    job1.apply();
    TestRecord tr1 = TestRecord.newRecord(job1);

    ExecutableJob job2 = QualifiedTypeJob.newInstance(TestQueueingQuartzJob.class);
    job2.getDisplayLabel().setValue("testQueue2");
    job2.apply();
    TestRecord tr2 = TestRecord.newRecord(job2);

    try
    {
      // Start both jobs
      JobHistory hist1 = job1.start();
      JobHistory hist2 = job2.start();
      
      // Wait for one of them to be running
      waitUntilRunning(hist1);
      
      // Assert that hist1 is running and hist2 is queued
      hist1 = JobHistory.get(hist1.getOid());
      hist2 = JobHistory.get(hist2.getOid());
      Assert.assertEquals(AllJobStatus.RUNNING.getEnumName(), hist1.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.QUEUED.getEnumName(), hist2.getStatus().get(0).getEnumName());
      
      // Wait for history 2 to be running
      waitUntilRunning(hist2);
      
      // Assert job one is now finished and job two is running
      hist1 = JobHistory.get(hist1.getOid());
      hist2 = JobHistory.get(hist2.getOid());
      Assert.assertEquals(hist1.getStatus().get(0).getEnumName(), AllJobStatus.SUCCESS.getEnumName());
      Assert.assertEquals(hist2.getStatus().get(0).getEnumName(), AllJobStatus.RUNNING.getEnumName());
      
      Thread.sleep(SchedulerTest.QUEUE_JOB_RUN_TIME * 2); // Wait for the last one to finish
      
      // Assert both jobs are now success
      hist1 = JobHistory.get(hist1.getOid());
      hist2 = JobHistory.get(hist2.getOid());
      Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), hist1.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), hist2.getStatus().get(0).getEnumName());
      Assert.assertEquals(1, tr1.getCount());
      Assert.assertEquals(1, tr2.getCount());
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
    finally
    {
      Thread.sleep(500);
      ExecutableJob.get(job1.getOid()).delete();
      ExecutableJob.get(job2.getOid()).delete();
      clearHistory();
    }
  }
  
  @Request
  @Test
  public void testQueueSameJobs() throws InterruptedException
  {
    ExecutableJob job1 = QualifiedTypeJob.newInstance(TestQueueingQuartzJob.class);
    job1.getDisplayLabel().setValue("testQueue1");
    job1.apply();
    TestRecord tr1 = TestRecord.newRecord(job1);

    try
    {
      // Start both jobs
      JobHistory hist1 = job1.start();
      JobHistory hist2 = job1.start();
      
      // Wait for one of them to be running
      waitUntilRunning(hist1);
      
      // Assert that hist1 is running and hist2 is queued
      hist1 = JobHistory.get(hist1.getOid());
      hist2 = JobHistory.get(hist2.getOid());
      Assert.assertEquals(AllJobStatus.RUNNING.getEnumName(), hist1.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.QUEUED.getEnumName(), hist2.getStatus().get(0).getEnumName());
      
      // Wait for history 2 to be running
      waitUntilRunning(hist2);
      
      // Assert job one is now finished and job two is running
      hist1 = JobHistory.get(hist1.getOid());
      hist2 = JobHistory.get(hist2.getOid());
      Assert.assertEquals(hist1.getStatus().get(0).getEnumName(), AllJobStatus.SUCCESS.getEnumName());
      Assert.assertEquals(hist2.getStatus().get(0).getEnumName(), AllJobStatus.RUNNING.getEnumName());
      
      Thread.sleep(SchedulerTest.QUEUE_JOB_RUN_TIME * 2); // Wait for the last one to finish
      
      // Assert both jobs are now success
      hist1 = JobHistory.get(hist1.getOid());
      hist2 = JobHistory.get(hist2.getOid());
      Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), hist1.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), hist2.getStatus().get(0).getEnumName());
      Assert.assertEquals(2, tr1.getCount());
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
    finally
    {
      Thread.sleep(500);
      ExecutableJob.get(job1.getOid()).delete();
      clearHistory();
    }
  }
  
  @Request
  @Test
  public void testQueueManyJobs() throws InterruptedException
  {
    ExecutableJob job1 = QualifiedTypeJob.newInstance(TestQueueingQuartzJob.class);
    job1.getDisplayLabel().setValue("testQueue1");
    job1.apply();
    TestRecord tr1 = TestRecord.newRecord(job1);
    
    ExecutableJob job2 = QualifiedTypeJob.newInstance(TestQueueingQuartzJob.class);
    job2.getDisplayLabel().setValue("testQueue2");
    job2.apply();
    TestRecord tr2 = TestRecord.newRecord(job2);

    try
    {
      // Start both jobs
      JobHistory hist11 = job1.start();
      System.out.println("hist11 is " + getRecord(job1, hist11).getOid());
      JobHistory hist12 = job1.start();
      System.out.println("hist12 is " + getRecord(job1, hist12).getOid());
      JobHistory hist13 = job1.start();
      System.out.println("hist13 is " + getRecord(job1, hist13).getOid());
      
      JobHistory hist21 = job2.start();
      System.out.println("hist21 is " + getRecord(job2, hist21).getOid());
      JobHistory hist22 = job2.start();
      System.out.println("hist22 is " + getRecord(job2, hist22).getOid());
      JobHistory hist23 = job2.start();
      System.out.println("hist23 is " + getRecord(job2, hist23).getOid());
      
      // Wait for one of them to be running
      waitUntilRunning(hist11);
      
      // Assert that hist1 is running and hist2 is queued
      hist11 = JobHistory.get(hist11.getOid());
      hist12 = JobHistory.get(hist12.getOid());
      hist13 = JobHistory.get(hist13.getOid());
      hist21 = JobHistory.get(hist21.getOid());
      hist22 = JobHistory.get(hist22.getOid());
      hist23 = JobHistory.get(hist23.getOid());
      Assert.assertEquals(AllJobStatus.RUNNING.getEnumName(), hist11.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.QUEUED.getEnumName(), hist12.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.QUEUED.getEnumName(), hist13.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.QUEUED.getEnumName(), hist21.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.QUEUED.getEnumName(), hist22.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.QUEUED.getEnumName(), hist23.getStatus().get(0).getEnumName());
      
      // Wait for history to be running
      waitUntilRunning(hist12);
      
      // Assert job one is now finished and job two is running
      hist11 = JobHistory.get(hist11.getOid());
      hist12 = JobHistory.get(hist12.getOid());
      hist13 = JobHistory.get(hist13.getOid());
      hist21 = JobHistory.get(hist21.getOid());
      hist22 = JobHistory.get(hist22.getOid());
      hist23 = JobHistory.get(hist23.getOid());
      Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), hist11.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.RUNNING.getEnumName(), hist12.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.QUEUED.getEnumName(), hist13.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.QUEUED.getEnumName(), hist21.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.QUEUED.getEnumName(), hist22.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.QUEUED.getEnumName(), hist23.getStatus().get(0).getEnumName());
      
      // Wait for history to be running
      waitUntilRunning(hist13);
      
      // Assert job one is now finished and job two is running
      hist11 = JobHistory.get(hist11.getOid());
      hist12 = JobHistory.get(hist12.getOid());
      hist13 = JobHistory.get(hist13.getOid());
      hist21 = JobHistory.get(hist21.getOid());
      hist22 = JobHistory.get(hist22.getOid());
      hist23 = JobHistory.get(hist23.getOid());
      Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), hist11.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), hist12.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.RUNNING.getEnumName(), hist13.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.QUEUED.getEnumName(), hist21.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.QUEUED.getEnumName(), hist22.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.QUEUED.getEnumName(), hist23.getStatus().get(0).getEnumName());
      
      // Wait for history to be running
      waitUntilRunning(hist21);
      
      // Assert job one is now finished and job two is running
      hist11 = JobHistory.get(hist11.getOid());
      hist12 = JobHistory.get(hist12.getOid());
      hist13 = JobHistory.get(hist13.getOid());
      hist21 = JobHistory.get(hist21.getOid());
      hist22 = JobHistory.get(hist22.getOid());
      hist23 = JobHistory.get(hist23.getOid());
      Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), hist11.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), hist12.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), hist13.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.RUNNING.getEnumName(), hist21.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.QUEUED.getEnumName(), hist22.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.QUEUED.getEnumName(), hist23.getStatus().get(0).getEnumName());
      
      // Wait for history to be running
      waitUntilRunning(hist22);
      
      // Assert job one is now finished and job two is running
      hist11 = JobHistory.get(hist11.getOid());
      hist12 = JobHistory.get(hist12.getOid());
      hist13 = JobHistory.get(hist13.getOid());
      hist21 = JobHistory.get(hist21.getOid());
      hist22 = JobHistory.get(hist22.getOid());
      hist23 = JobHistory.get(hist23.getOid());
      Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), hist11.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), hist12.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), hist13.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), hist21.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.RUNNING.getEnumName(), hist22.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.QUEUED.getEnumName(), hist23.getStatus().get(0).getEnumName());
      
      // Wait for history to be running
      waitUntilRunning(hist23);
      
      // Assert job one is now finished and job two is running
      hist11 = JobHistory.get(hist11.getOid());
      hist12 = JobHistory.get(hist12.getOid());
      hist13 = JobHistory.get(hist13.getOid());
      hist21 = JobHistory.get(hist21.getOid());
      hist22 = JobHistory.get(hist22.getOid());
      hist23 = JobHistory.get(hist23.getOid());
      Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), hist11.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), hist12.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), hist13.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), hist21.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), hist22.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.RUNNING.getEnumName(), hist23.getStatus().get(0).getEnumName());
      
      Thread.sleep(SchedulerTest.QUEUE_JOB_RUN_TIME * 2); // Wait for the last one to finish
      
      // Assert job one is now finished and job two is running
      hist11 = JobHistory.get(hist11.getOid());
      hist12 = JobHistory.get(hist12.getOid());
      hist13 = JobHistory.get(hist13.getOid());
      hist21 = JobHistory.get(hist21.getOid());
      hist22 = JobHistory.get(hist22.getOid());
      hist23 = JobHistory.get(hist23.getOid());
      Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), hist11.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), hist12.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), hist13.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), hist21.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), hist22.getStatus().get(0).getEnumName());
      Assert.assertEquals(AllJobStatus.SUCCESS.getEnumName(), hist23.getStatus().get(0).getEnumName());
      
      // Assert both jobs are now success
      Assert.assertEquals(3, tr1.getCount());
      Assert.assertEquals(3, tr2.getCount());
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
    finally
    {
      Thread.sleep(500);
      ExecutableJob.get(job1.getOid()).delete();
      ExecutableJob.get(job2.getOid()).delete();
      clearHistory();
    }
  }
  
  private void waitUntilRunning(JobHistory hist) throws InterruptedException
  {
    int waitTime = 0;
    while (true)
    {
      hist = JobHistory.get(hist.getOid());
      if (hist.getStatus().get(0) == AllJobStatus.RUNNING)
      {
        break;
      }
      
      Thread.sleep(10);

      waitTime += 10;
      if (waitTime > 10000)
      {
        Assert.fail("Job was never scheduled (status is " + hist.getStatus().get(0).getEnumName() + ")");
        return;
      }
    }
    
    Thread.sleep(100);
  }
  
  @Test
  @Request
  public void testQueueMultithreading() throws InterruptedException
  {
    ExecutableJob job1 = QualifiedTypeJob.newInstance(TestQueueingQuartzJob.class);
    job1.getDisplayLabel().setValue("testMultithreadedJob");
    job1.apply();
    
    TestRecord tr = TestRecord.newRecord(job1);
    
    Queue<JobHistory> pool = new ConcurrentLinkedQueue<JobHistory>();
    
    int threadJobs = 10;
    
    Thread t1 = new Thread(new Runnable(){
      @Override
      @Request
      public void run()
      {
        int i = threadJobs;
        
        while (i > 0)
        {
          synchronized(pool)
          {
            System.out.println("Thread 1 is starting job");
            JobHistory history = job1.start();
            
            pool.add(history);
          }
          
          try
          {
            Thread.sleep(new Random().nextInt(3) * 1000);
          }
          catch (InterruptedException e)
          {
            e.printStackTrace();
          }
          
          i = i - 1;
        }
      }
    });
    
    Thread t2 = new Thread(new Runnable(){
      @Override
      @Request
      public void run()
      {
        int i = threadJobs;
        
        while (i > 0)
        {
          synchronized(pool)
          {
            System.out.println("Thread 2 is starting job");
            JobHistory history = job1.start();
            
            pool.add(history);
          }
          
          try
          {
            Thread.sleep(new Random().nextInt(3) * 1000);
          }
          catch (InterruptedException e)
          {
            e.printStackTrace();
          }
          
          i = i - 1;
        }
      }
    });
    
    t1.start();
    t2.start();
    
    Thread.sleep(1000);
    
    while (pool.size() > 0)
    {
      synchronized(pool)
      {
        JobHistory jh = JobHistory.get(pool.peek().getOid());
        jh.lock();
        
        AllJobStatus status = jh.getStatus().get(0);
        
        try
        {
          if (status.equals(AllJobStatus.RUNNING) || status.equals(AllJobStatus.NEW) || status.equals(AllJobStatus.QUEUED))
          {
            Iterator<JobHistory> it = pool.iterator();
            
            while (it.hasNext())
            {
              JobHistory itHist = it.next();
              
              if (!itHist.getOid().equals(jh.getOid()))
              {
                Assert.assertTrue("Expected QUEUED or NEW but was " + itHist.getStatus().get(0).getEnumName(), itHist.getStatus().get(0).equals(AllJobStatus.QUEUED) || itHist.getStatus().get(0).equals(AllJobStatus.NEW));
              }
            }
          }
          else if (status.equals(AllJobStatus.SUCCESS))
          {
            pool.poll();
          }
          else
          {
            Assert.fail("Expected either RUNNING, SUCCESS or QUEUED but was " + status.getEnumName() + ". " + jh.getHistoryInformation().getValue());
          }
        }
        finally
        {
          jh.unlock();
        }
      }
    }
    
    Assert.assertEquals(threadJobs * 2, tr.getCount());
  }
  
  private JobHistoryRecord getRecord(ExecutableJob execJob, JobHistory hist)
  {
    return hist.getJobRel(execJob).getAll().get(0);
  }
  
  @Test
  @Request
  public void testMultiStageJob() throws Exception
  {
    
  }
  
  /**
   * Tests a job which errors out.
   * 
   * @throws InterruptedException
   */
  @Request
  @Test
  public void testSmartJobError() throws Throwable
  {
    ExecutableJob job = QualifiedTypeJob.newInstance(TestSmartErrorJob.class);
    job.getDisplayLabel().setValue("testSmartJobError");

    try
    {
      job.apply();

      TestRecord tr = TestRecord.newRecord(job);

      JobHistory history = job.start();
      Date startTime = history.getStartTime();

      Assert.assertNotNull(startTime);
      Assert.assertTrue("Expected status of NEW or RUNNING", history.getStatus().get(0).equals(AllJobStatus.RUNNING) || history.getStatus().get(0).equals(AllJobStatus.NEW));
      
      this.waitUntilRunning(history);
      
      history = JobHistory.get(history.getOid());
      Assert.assertEquals(AllJobStatus.RUNNING.getEnumName(), history.getStatus().get(0).getEnumName());

      wait(tr, 10);
      
      if (tr.isExecuted() && tr.getCount() == 1)
      {
        Thread.sleep(1000);
        
        JobHistory updated = JobHistory.getByKey(history.getKey());
        Assert.assertEquals(AllJobStatus.FAILURE.getEnumName(), updated.getStatus().get(0).getEnumName());
        Assert.assertEquals(0, SchedulerManager.getRunningJobs().size());
        Assert.assertNotNull(updated.getEndTime());
        Assert.assertTrue(updated.getStartTime() + " was expected to be after " + updated.getEndTime(), updated.getEndTime().after(startTime));
        
        BackupReadException ex = new BackupReadException();
        ex.setLocation("/test/123");
        
        String json = updated.getErrorJson();
        System.out.println(json);
        
        JSONObject jo = new JSONObject(json);
        Assert.assertEquals(ex.getType(), jo.get("type"));
        
        Assert.assertEquals(2, jo.getJSONArray("attributes").length());
        
        String msg = ex.localize(Session.getCurrentLocale());
        Assert.assertEquals(msg, jo.get("message"));
        Assert.assertEquals(msg, updated.getLocalizedError(Session.getCurrentLocale()));
      }
      else
      {
        Assert.fail("The job was not completed.");
      }
    }
    finally
    {
      Thread.sleep(1000);
      ExecutableJob.get(job.getOid()).delete();
      clearHistory();
    }
  }
  
  @Request
  @Test
  public void testArithmeticJobError() throws Throwable
  {
    ExecutableJob job = QualifiedTypeJob.newInstance(TestArithmeticErrorJob.class);
    job.getDisplayLabel().setValue("testBasicJobError");

    try
    {
      job.apply();

      TestRecord tr = TestRecord.newRecord(job);

      JobHistory history = job.start();
      Date startTime = history.getStartTime();

      Assert.assertNotNull(startTime);
      Assert.assertTrue("Expected status of NEW or RUNNING", history.getStatus().get(0).equals(AllJobStatus.RUNNING) || history.getStatus().get(0).equals(AllJobStatus.NEW));
      
      this.waitUntilRunning(history);
      
      history = JobHistory.get(history.getOid());
      Assert.assertEquals(AllJobStatus.RUNNING.getEnumName(), history.getStatus().get(0).getEnumName());

      wait(tr, 10);
      
      if (tr.isExecuted() && tr.getCount() == 1)
      {
        Thread.sleep(1000);
        
        JobHistory updated = JobHistory.getByKey(history.getKey());
        Assert.assertEquals(AllJobStatus.FAILURE.getEnumName(), updated.getStatus().get(0).getEnumName());
        Assert.assertEquals(0, SchedulerManager.getRunningJobs().size());
        Assert.assertNotNull(updated.getEndTime());
        Assert.assertTrue(updated.getStartTime() + " was expected to be after " + updated.getEndTime(), updated.getEndTime().after(startTime));
        
        ArithmeticException ex = null;
        try
        {
          int fail = 10 / 0;
          System.out.println(fail);
        }
        catch (ArithmeticException e)
        {
          ex = e;
        }
        
        String json = updated.getErrorJson();
        System.out.println(json);
        
        JSONObject jo = new JSONObject(json);
        Assert.assertEquals(ex.getClass().getName(), jo.get("type"));
        
        String msg = ex.getMessage();
        Assert.assertEquals(msg, jo.get("message"));
        Assert.assertEquals(msg, updated.getLocalizedError(Session.getCurrentLocale()));
      }
      else
      {
        Assert.fail("The job was not completed.");
      }
    }
    finally
    {
      Thread.sleep(1000);
      ExecutableJob.get(job.getOid()).delete();
      clearHistory();
    }
  }
  
  private static RunwayException buildRunwayJobError()
  {
//    LinkageError le = new LinkageError();
//    
//    List<ProblemIF> problems = new ArrayList<ProblemIF>();
//    problems.add(new WKTParsingProblem());
//    
//    ProblemException probEx = new ProblemException(le, problems);
//    
//    return probEx;
    
    return new SchedulerJobCannotResumeException("");
  }
  
  @Request
  @Test
  public void testRunwayJobError() throws Throwable
  {
    ExecutableJob job = QualifiedTypeJob.newInstance(TestRunwayErrorJob.class);
    job.getDisplayLabel().setValue("testRunwayErrorJob");

    try
    {
      job.apply();

      TestRecord tr = TestRecord.newRecord(job);

      JobHistory history = job.start();
      Date startTime = history.getStartTime();

      Assert.assertNotNull(startTime);
      Assert.assertTrue("Expected status of NEW or RUNNING", history.getStatus().get(0).equals(AllJobStatus.RUNNING) || history.getStatus().get(0).equals(AllJobStatus.NEW));
      
      this.waitUntilRunning(history);
      
      history = JobHistory.get(history.getOid());
      Assert.assertEquals(AllJobStatus.RUNNING.getEnumName(), history.getStatus().get(0).getEnumName());

      wait(tr, 10);
      
      if (tr.isExecuted() && tr.getCount() == 1)
      {
        Thread.sleep(1000);
        
        JobHistory updated = JobHistory.getByKey(history.getKey());
        Assert.assertEquals(AllJobStatus.FAILURE.getEnumName(), updated.getStatus().get(0).getEnumName());
        Assert.assertEquals(0, SchedulerManager.getRunningJobs().size());
        Assert.assertNotNull(updated.getEndTime());
        Assert.assertTrue(updated.getStartTime() + " was expected to be after " + updated.getEndTime(), updated.getEndTime().after(startTime));
        
        String json = updated.getErrorJson();
        System.out.println(json);
        
        RunwayException rwEx = buildRunwayJobError();
        
        JSONObject jo = new JSONObject(json);
        Assert.assertEquals(rwEx.getClass().getName(), jo.get("type"));
        
        String msg = rwEx.getLocalizedMessage();
        Assert.assertEquals(msg, jo.get("message"));
        Assert.assertEquals(msg, updated.getLocalizedError(Session.getCurrentLocale()));
      }
      else
      {
        Assert.fail("The job was not completed.");
      }
    }
    finally
    {
      Thread.sleep(1000);
      ExecutableJob.get(job.getOid()).delete();
      clearHistory();
    }
  }
}
