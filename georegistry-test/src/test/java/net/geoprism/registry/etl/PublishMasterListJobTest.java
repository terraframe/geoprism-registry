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
package net.geoprism.registry.etl;

import java.io.File;
import java.io.IOException;

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
import com.runwaysdk.business.SmartExceptionDTO;
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
import net.geoprism.registry.service.MasterListService;
import net.geoprism.registry.service.MasterListTest;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.SchedulerTestUtils;
import net.geoprism.registry.test.TestUserInfo;
import net.geoprism.registry.test.USATestData;

public class PublishMasterListJobTest
{
  protected static FastTestDataset testData;

  @BeforeClass
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();
    testData.setUpMetadata();

    if (!SchedulerManager.initialized())
    {
      SchedulerManager.start();
    }
  }

  @AfterClass
  public static void cleanUpClass()
  {
    testData.tearDownMetadata();
  }

  @Before
  public void setUp()
  {
    testData.setUpInstanceData();

    clearData();

    testData.logIn(FastTestDataset.USER_CGOV_RA);
  }

  @After
  public void tearDown() throws IOException
  {
    testData.logOut();

    testData.tearDownInstanceData();

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

  @Test
  public void testNewAndUpdate() throws InterruptedException
  {
    JsonObject listJson = MasterListTest.getJson(FastTestDataset.ORG_CGOV.getServerObject(), FastTestDataset.HIER_ADMIN, FastTestDataset.PROVINCE, MasterList.PUBLIC, false, FastTestDataset.COUNTRY);

    MasterListService service = new MasterListService();
    JsonObject result = service.create(testData.clientRequest.getSessionId(), listJson);
    String oid = result.get(ComponentInfo.OID).getAsString();

    try
    {
      String historyId = service.createPublishedVersionsJob(testData.clientRequest.getSessionId(), oid);

      SchedulerTestUtils.waitUntilStatus(historyId, AllJobStatus.SUCCESS);

      final JsonObject object = service.getVersions(testData.clientRequest.getSessionId(), oid, MasterListVersion.PUBLISHED);
      final JsonArray json = object.get(MasterList.VERSIONS).getAsJsonArray();

      Assert.assertEquals(USATestData.DEFAULT_TIME_YEAR_DIFF, json.size());
    }
    finally
    {
      service.remove(testData.clientRequest.getSessionId(), oid);
    }

  }

  @Test
  public void testGetPublishJobs() throws InterruptedException
  {
    TestUserInfo[] users = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM };

    for (TestUserInfo user : users)
    {
      JsonObject listJson = MasterListTest.getJson(FastTestDataset.ORG_CGOV.getServerObject(), FastTestDataset.HIER_ADMIN, FastTestDataset.PROVINCE, MasterList.PUBLIC, false, FastTestDataset.COUNTRY);

      MasterListService service = new MasterListService();
      JsonObject result = service.create(testData.clientRequest.getSessionId(), listJson);
      String oid = result.get(ComponentInfo.OID).getAsString();

      try
      {
        FastTestDataset.runAsUser(user, (request, adapter) -> {

          String historyId = service.createPublishedVersionsJob(request.getSessionId(), oid);

          SchedulerTestUtils.waitUntilStatus(historyId, AllJobStatus.SUCCESS);

          final JsonObject response = service.getPublishJobs(request.getSessionId(), oid, 10, 1, null, true);
          Assert.assertEquals(1, response.get("count").getAsInt());
          Assert.assertEquals(1, response.get("pageNumber").getAsInt());
          Assert.assertEquals(10, response.get("pageSize").getAsInt());

          final JsonArray json = response.get("results").getAsJsonArray();
          Assert.assertEquals(1, json.size());

          final JsonObject object = json.get(0).getAsJsonObject();

          Assert.assertEquals(oid, object.get(PublishMasterListJob.MASTERLIST).getAsString());
          Assert.assertEquals(FastTestDataset.PROVINCE.getDisplayLabel().getValue(), object.get(PublishMasterListJob.TYPE).getAsString());
          Assert.assertEquals(AllJobStatus.SUCCESS.getDisplayLabel(), object.get(JobHistory.STATUS).getAsString());
          Assert.assertEquals(user.getUsername(), object.get("author").getAsString());
          Assert.assertTrue(object.has("createDate"));
          Assert.assertTrue(object.has("lastUpdateDate"));
        });
      }
      finally
      {
        service.remove(testData.clientRequest.getSessionId(), oid);
      }
    }

  }

  @Test
  public void testCreateMultiple() throws InterruptedException
  {
    JsonObject listJson = MasterListTest.getJson(FastTestDataset.ORG_CGOV.getServerObject(), FastTestDataset.HIER_ADMIN, FastTestDataset.PROVINCE, MasterList.PUBLIC, false, FastTestDataset.COUNTRY);

    MasterListService service = new MasterListService();
    JsonObject result = service.create(testData.clientRequest.getSessionId(), listJson);
    String oid = result.get(ComponentInfo.OID).getAsString();

    try
    {
      String historyId = service.createPublishedVersionsJob(testData.clientRequest.getSessionId(), oid);

      try
      {
        service.createPublishedVersionsJob(testData.clientRequest.getSessionId(), oid);

        Assert.fail("Able to publish a masterlist multiple times concurrently");
      }
      catch (Exception e)
      {
        // This is expected
      }

      SchedulerTestUtils.waitUntilStatus(historyId, AllJobStatus.SUCCESS);

      final JsonObject object = service.getVersions(testData.clientRequest.getSessionId(), oid, MasterListVersion.PUBLISHED);
      final JsonArray json = object.get(MasterList.VERSIONS).getAsJsonArray();

      Assert.assertEquals(USATestData.DEFAULT_TIME_YEAR_DIFF, json.size());
    }
    finally
    {
      service.remove(testData.clientRequest.getSessionId(), oid);
    }

  }

  @Test
  public void testChangeFrequency() throws InterruptedException
  {
    JsonObject listJson = MasterListTest.getJson(FastTestDataset.ORG_CGOV.getServerObject(), FastTestDataset.HIER_ADMIN, FastTestDataset.PROVINCE, MasterList.PUBLIC, false, FastTestDataset.COUNTRY);

    MasterListService service = new MasterListService();
    JsonObject result = service.create(testData.clientRequest.getSessionId(), listJson);
    String oid = result.get(ComponentInfo.OID).getAsString();

    try
    {
      String historyId = service.createPublishedVersionsJob(testData.clientRequest.getSessionId(), oid);

      SchedulerTestUtils.waitUntilStatus(historyId, AllJobStatus.SUCCESS);

      JsonObject object = service.getVersions(testData.clientRequest.getSessionId(), oid, MasterListVersion.PUBLISHED);
      JsonArray json = object.get(MasterList.VERSIONS).getAsJsonArray();

      Assert.assertEquals(USATestData.DEFAULT_TIME_YEAR_DIFF, json.size());

      result.addProperty(MasterList.FREQUENCY, ChangeFrequency.QUARTER.name());

      service.create(testData.clientRequest.getSessionId(), result);

      object = service.getVersions(testData.clientRequest.getSessionId(), oid, MasterListVersion.PUBLISHED);
      json = object.get(MasterList.VERSIONS).getAsJsonArray();

      Assert.assertEquals(0, json.size());
    }
    finally
    {
      service.remove(testData.clientRequest.getSessionId(), oid);
    }
  }

  @Test
  public void testUpdate() throws InterruptedException
  {
    JsonObject listJson = MasterListTest.getJson(FastTestDataset.ORG_CGOV.getServerObject(), FastTestDataset.HIER_ADMIN, FastTestDataset.PROVINCE, MasterList.PUBLIC, false, FastTestDataset.COUNTRY);

    MasterListService service = new MasterListService();
    JsonObject result = service.create(testData.clientRequest.getSessionId(), listJson);
    String oid = result.get(ComponentInfo.OID).getAsString();

    try
    {
      String historyId = service.createPublishedVersionsJob(testData.clientRequest.getSessionId(), oid);

      SchedulerTestUtils.waitUntilStatus(historyId, AllJobStatus.SUCCESS);

      JsonObject object = service.getVersions(testData.clientRequest.getSessionId(), oid, MasterListVersion.PUBLISHED);
      JsonArray json = object.get(MasterList.VERSIONS).getAsJsonArray();

      Assert.assertEquals(USATestData.DEFAULT_TIME_YEAR_DIFF, json.size());

      service.create(testData.clientRequest.getSessionId(), result);

      object = service.getVersions(testData.clientRequest.getSessionId(), oid, MasterListVersion.PUBLISHED);
      json = object.get(MasterList.VERSIONS).getAsJsonArray();

      Assert.assertEquals(USATestData.DEFAULT_TIME_YEAR_DIFF, json.size());
    }
    finally
    {
      service.remove(testData.clientRequest.getSessionId(), oid);
    }
  }

  @Test
  public void testPublishJobsAsBadUser() throws InterruptedException
  {
    TestUserInfo[] users = new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_CGOV_RC };

    for (TestUserInfo user : users)
    {

      JsonObject listJson = MasterListTest.getJson(FastTestDataset.ORG_CGOV.getServerObject(), FastTestDataset.HIER_ADMIN, FastTestDataset.PROVINCE, MasterList.PUBLIC, false, FastTestDataset.COUNTRY);

      MasterListService service = new MasterListService();
      JsonObject result = service.create(testData.clientRequest.getSessionId(), listJson);
      String oid = result.get(ComponentInfo.OID).getAsString();

      try
      {

        FastTestDataset.runAsUser(user, (request, adapter) -> {
          String historyId = service.createPublishedVersionsJob(request.getSessionId(), oid);
          SchedulerTestUtils.waitUntilStatus(historyId, AllJobStatus.SUCCESS);

          Assert.fail("Able to publish master list versions as the user [" + user.getUsername() + "]");
        });
      }
      catch (SmartExceptionDTO e)
      {
        // This is expected
      }
      finally
      {
        service.remove(testData.clientRequest.getSessionId(), oid);
      }
    }

  }
}
