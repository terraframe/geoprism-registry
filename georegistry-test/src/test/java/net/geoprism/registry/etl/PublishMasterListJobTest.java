/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.etl;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.constants.ComponentInfo;
import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutableJob;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryRecord;
import com.runwaysdk.system.scheduler.JobHistoryRecordQuery;
import com.runwaysdk.system.scheduler.SchedulerManager;

import net.geoprism.registry.ChangeFrequency;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.MasterListVersion;
import net.geoprism.registry.Organization;
import net.geoprism.registry.service.MasterListService;
import net.geoprism.registry.service.MasterListTest;
import net.geoprism.registry.test.USATestData;

public class PublishMasterListJobTest
{
  protected static USATestData testData;

  private static Organization  org;

  @BeforeClass
  public static void setUpClass()
  {
    setupOrg();

    testData = USATestData.newTestDataForClass(true, new Date());
    testData.setUpMetadata();

    SchedulerManager.start();
  }

  @Request
  private static void setupOrg()
  {
    org = new Organization();
    org.setCode("Testzzz");
    org.getDisplayLabel().setValue("Testzzz");
    org.apply();
  }

  @AfterClass
  public static void cleanUpClass()
  {
    SchedulerManager.shutdown();

    if (testData != null)
    {
      testData.tearDownMetadata();
    }

    tearDownOrg();
  }

  @Request
  private static void tearDownOrg()
  {
    if (org != null)
    {
      org.delete();
    }
  }

  @Before
  public void setUp()
  {
    if (testData != null)
    {
      testData.setUpInstanceData();
    }

    clearData();
  }

  @After
  public void tearDown() throws IOException
  {
    if (testData != null)
    {
      testData.tearDownInstanceData();
    }

    FileUtils.deleteDirectory(new File(VaultProperties.getPath("vault.default"), "files"));

    clearData();
  }

  @Request
  private static void clearData()
  {
    JobHistoryRecordQuery query = new JobHistoryRecordQuery(new QueryFactory());
    OIterator<? extends JobHistoryRecord> jhrs = query.getIterator();

    while (jhrs.hasNext())
    {
      JobHistoryRecord jhr = jhrs.next();

      ExecutableJob job = jhr.getParent();

      if (job instanceof PublishMasterListJob)
      {
        jhr.delete();
        job.delete();
      }
    }
  }

  @Request
  private void waitUntilStatus(String historyId, AllJobStatus status) throws InterruptedException
  {
    int waitTime = 0;
    while (true)
    {
      final JobHistory hist = JobHistory.get(historyId);
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
      if (waitTime > 2000000)
      {
        String extra = "";

        Assert.fail("Job was never scheduled (status is " + hist.getStatus().get(0).getEnumName() + ") " + extra);
        return;
      }
    }

    Thread.sleep(100);
  }

  @Test
  public void testNewAndUpdate() throws InterruptedException
  {
    JsonObject listJson = MasterListTest.getJson(org, testData.STATE, testData.COUNTRY);

    MasterListService service = new MasterListService();
    JsonObject result = service.create(testData.adminClientRequest.getSessionId(), listJson);
    String oid = result.get(ComponentInfo.OID).getAsString();

    try
    {
      String historyId = service.createPublishedVersionsJob(testData.adminClientRequest.getSessionId(), oid);

      this.waitUntilStatus(historyId, AllJobStatus.SUCCESS);

      final JsonObject object = service.getVersions(testData.adminClientRequest.getSessionId(), oid, MasterListVersion.PUBLISHED);
      final JsonArray json = object.get(MasterList.VERSIONS).getAsJsonArray();

      Assert.assertEquals(1, json.size());
    }
    finally
    {
      service.remove(testData.adminClientRequest.getSessionId(), oid);
    }

  }

  @Test
  public void testGetPublishJobs() throws InterruptedException
  {
    JsonObject listJson = MasterListTest.getJson(org, testData.STATE, testData.COUNTRY);

    MasterListService service = new MasterListService();
    JsonObject result = service.create(testData.adminClientRequest.getSessionId(), listJson);
    String oid = result.get(ComponentInfo.OID).getAsString();

    try
    {
      String historyId = service.createPublishedVersionsJob(testData.adminClientRequest.getSessionId(), oid);

      this.waitUntilStatus(historyId, AllJobStatus.SUCCESS);

      final JSONObject response = service.getPublishJobs(testData.adminClientRequest.getSessionId(), oid, 10, 1, null, true);
      Assert.assertEquals(1, response.getInt("count"));
      Assert.assertEquals(1, response.getInt("pageNumber"));
      Assert.assertEquals(10, response.getInt("pageSize"));

      final JSONArray json = response.getJSONArray("results");
      Assert.assertEquals(1, json.length());

      System.out.println(json);

      final JSONObject object = json.getJSONObject(0);

      Assert.assertEquals(oid, object.getString(PublishMasterListJob.MASTERLIST));
      Assert.assertEquals(testData.STATE.getDisplayLabel().getValue(), object.getString(PublishMasterListJob.TYPE));
      Assert.assertEquals(AllJobStatus.SUCCESS.getDisplayLabel(), object.getString(JobHistory.STATUS));
      Assert.assertEquals("admin", object.getString("author"));
      Assert.assertTrue(object.has("createDate"));
      Assert.assertTrue(object.has("lastUpdateDate"));
    }
    finally
    {
      service.remove(testData.adminClientRequest.getSessionId(), oid);
    }

  }

  @Test
  public void testCreateMultiple() throws InterruptedException
  {
    JsonObject listJson = MasterListTest.getJson(org, testData.STATE, testData.COUNTRY);

    MasterListService service = new MasterListService();
    JsonObject result = service.create(testData.adminClientRequest.getSessionId(), listJson);
    String oid = result.get(ComponentInfo.OID).getAsString();

    try
    {
      String historyId = service.createPublishedVersionsJob(testData.adminClientRequest.getSessionId(), oid);

      try
      {
        service.createPublishedVersionsJob(testData.adminClientRequest.getSessionId(), oid);

        Assert.fail("Able to publish a masterlist multiple times concurrently");
      }
      catch (Exception e)
      {
        // This is expected
      }

      this.waitUntilStatus(historyId, AllJobStatus.SUCCESS);

      final JsonObject object = service.getVersions(testData.adminClientRequest.getSessionId(), oid, MasterListVersion.PUBLISHED);
      final JsonArray json = object.get(MasterList.VERSIONS).getAsJsonArray();

      Assert.assertEquals(1, json.size());
    }
    finally
    {
      service.remove(testData.adminClientRequest.getSessionId(), oid);
    }

  }

  @Test
  public void testChangeFrequency() throws InterruptedException
  {
    JsonObject listJson = MasterListTest.getJson(org, testData.STATE, testData.COUNTRY);

    MasterListService service = new MasterListService();
    JsonObject result = service.create(testData.adminClientRequest.getSessionId(), listJson);
    String oid = result.get(ComponentInfo.OID).getAsString();

    try
    {
      String historyId = service.createPublishedVersionsJob(testData.adminClientRequest.getSessionId(), oid);

      this.waitUntilStatus(historyId, AllJobStatus.SUCCESS);

      JsonObject object = service.getVersions(testData.adminClientRequest.getSessionId(), oid, MasterListVersion.PUBLISHED);
      JsonArray json = object.get(MasterList.VERSIONS).getAsJsonArray();
      
      Assert.assertEquals(1, json.size());

      result.addProperty(MasterList.FREQUENCY, ChangeFrequency.QUARTER.name());

      service.create(testData.adminClientRequest.getSessionId(), result);

      object = service.getVersions(testData.adminClientRequest.getSessionId(), oid, MasterListVersion.PUBLISHED);
      json = object.get(MasterList.VERSIONS).getAsJsonArray();

      Assert.assertEquals(0, json.size());
    }
    finally
    {
      service.remove(testData.adminClientRequest.getSessionId(), oid);
    }
  }
  
  @Test
  public void testUpdate() throws InterruptedException
  {
    JsonObject listJson = MasterListTest.getJson(org, testData.STATE, testData.COUNTRY);
    
    MasterListService service = new MasterListService();
    JsonObject result = service.create(testData.adminClientRequest.getSessionId(), listJson);
    String oid = result.get(ComponentInfo.OID).getAsString();
    
    try
    {
      String historyId = service.createPublishedVersionsJob(testData.adminClientRequest.getSessionId(), oid);
      
      this.waitUntilStatus(historyId, AllJobStatus.SUCCESS);
      
      JsonObject object = service.getVersions(testData.adminClientRequest.getSessionId(), oid, MasterListVersion.PUBLISHED);
      JsonArray json = object.get(MasterList.VERSIONS).getAsJsonArray();
      
      Assert.assertEquals(1, json.size());
      
      service.create(testData.adminClientRequest.getSessionId(), result);
      
      object = service.getVersions(testData.adminClientRequest.getSessionId(), oid, MasterListVersion.PUBLISHED);
      json = object.get(MasterList.VERSIONS).getAsJsonArray();
      
      Assert.assertEquals(1, json.size());
    }
    finally
    {
      service.remove(testData.adminClientRequest.getSessionId(), oid);
    }
    
  }
}
