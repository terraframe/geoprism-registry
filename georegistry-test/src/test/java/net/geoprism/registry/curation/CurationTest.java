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
package net.geoprism.registry.curation;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistoryRecord;
import com.runwaysdk.system.scheduler.MockScheduler;
import com.runwaysdk.system.scheduler.SchedulerManager;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.ListTypeEntry;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.OrganizationRAException;
import net.geoprism.registry.OrganizationRMException;
import net.geoprism.registry.SingleListType;
import net.geoprism.registry.curation.CurationProblem.CurationResolution;
import net.geoprism.registry.curation.GeoObjectProblem.GeoObjectProblemType;
import net.geoprism.registry.service.ListTypeTest;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.SchedulerTestUtils;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestUserInfo;
import net.geoprism.registry.test.curation.CurationControllerWrapper;

public class CurationTest
{
  protected static FastTestDataset testData;

  private ListCurationHistory      history;

  private String                   historyId;

  private ListCurationJob          job;

  private JobHistoryRecord         jobHistoryRecord;

  private ListTypeVersion          version;

  private SingleListType           list;

  private ListTypeEntry            entry;

  private GeoObjectProblem         curationProblem;

  private String                   curationProblemId;

  @BeforeClass
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();
    testData.setUpMetadata();

    TestDataSet.deleteAllSchedulerData();
    TestDataSet.deleteAllListData();

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

    createTestInstanceData();
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    testData.tearDownInstanceData();

    TestDataSet.deleteAllSchedulerData();
    TestDataSet.deleteAllListData();
  }

  @Request
  private void createTestInstanceData()
  {
    list = new SingleListType();
    list.setUniversal(FastTestDataset.PROVINCE.getUniversal());
    list.getDisplayLabel().setValue("Test List");
    list.setCode("TEST_CODE");
    list.setOrganization(FastTestDataset.ORG_CGOV.getServerObject());
    list.getDescription().setValue("My Overal Description");
    list.setValidOn(TestDataSet.DEFAULT_OVER_TIME_DATE);

    list.getListDescription().setValue("My Abstract");
    list.getListProcess().setValue("Process");
    list.getListProgress().setValue("Progress");
    list.getListAccessConstraints().setValue("Access Contraints");
    list.getListUseConstraints().setValue("User Constraints");
    list.getListAcknowledgements().setValue("Acknowledgements");
    list.getListDisclaimer().setValue("Disclaimer");
    list.setListContactName("Contact Name");
    list.setListTelephoneNumber("Telephone Number");
    list.setListEmail("Email");

    list.apply();

    entry = ListTypeEntry.create(list, TestDataSet.DEFAULT_OVER_TIME_DATE, null);

    entry.createVersion(ListTypeTest.createVersionMetadata()).publishNoAuth();

    List<ListTypeVersion> versions = entry.getVersions();

    Assert.assertEquals(2, versions.size());

    version = versions.get(0);

    job = new ListCurationJob();
    job.setRunAsUser(TestDataSet.USER_ADMIN.getGeoprismUser());
    job.apply();

    history = (ListCurationHistory) job.createNewHistory();
    history.appLock();
    history.clearStatus();
    history.addStatus(AllJobStatus.RUNNING);
    history.setVersion(version);
    history.apply();
    historyId = history.getOid();

    jobHistoryRecord = new JobHistoryRecord(job, history);
    jobHistoryRecord.apply();

    curationProblem = new GeoObjectProblem();
    curationProblem.setHistory(history);
    curationProblem.setResolution(CurationResolution.UNRESOLVED.name());
    curationProblem.setProblemType(GeoObjectProblemType.NO_GEOMETRY.name());
    curationProblem.setTypeCode(FastTestDataset.PROVINCE.getCode());
    curationProblem.setGoCode(FastTestDataset.PROV_CENTRAL.getCode());
    curationProblem.apply();
    curationProblemId = curationProblem.getOid();
  }

  @Test
  public void testDetails()
  {
    // Allowed Users
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_ADMIN, FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM };

    for (TestUserInfo user : allowedUsers)
    {
      FastTestDataset.runAsUser(user, (request, adapter) -> {
        CurationControllerWrapper controller = new CurationControllerWrapper(adapter, request);

        JsonObject details = controller.details(history.getOid(), false, 10, 1);

        Assert.assertTrue(details.has("page"));
        JsonObject page = details.get("page").getAsJsonObject();
        Assert.assertTrue(page.has("count"));
        Assert.assertTrue(page.has("pageNumber"));
        Assert.assertTrue(page.has("pageSize"));
        Assert.assertTrue(page.has("resultSet"));

        Assert.assertEquals(AllJobStatus.RUNNING.name(), details.get("status").getAsString());
        Assert.assertEquals(GeoRegistryUtil.formatDate(history.getCreateDate(), false), details.get("lastRun").getAsString());
        Assert.assertEquals(historyId, details.get("historyId").getAsString());
        Assert.assertEquals(job.getOid(), details.get("jobId").getAsString());
        Assert.assertFalse(details.has("exception"));
        Assert.assertEquals(TestDataSet.ADMIN_USER_NAME, details.get("lastRunBy").getAsString());
      });
    }

    // Disallowed Users
    TestUserInfo[] disallowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM };

    for (TestUserInfo user : disallowedUsers)
    {
      FastTestDataset.runAsUser(user, (request, adapter) -> {
        CurationControllerWrapper controller = new CurationControllerWrapper(adapter, request);

        try
        {
          controller.details(history.getOid(), false, 10, 1);

          Assert.fail("Expected to get an exception.");
        }
        catch (SmartExceptionDTO e)
        {
          Assert.assertTrue(OrganizationRAException.CLASS.equals(e.getType()) || OrganizationRMException.CLASS.equals(e.getType()));
        }
      });
    }
  }

  @Test
  public void testPage()
  {
    // Allowed Users
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_ADMIN, FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM };

    for (TestUserInfo user : allowedUsers)
    {
      FastTestDataset.runAsUser(user, (request, adapter) -> {
        CurationControllerWrapper controller = new CurationControllerWrapper(adapter, request);

        JsonObject page = controller.page(history.getOid(), false, 10, 1);

        Assert.assertEquals(1, page.get("pageNumber").getAsInt());
        Assert.assertEquals(10, page.get("pageSize").getAsInt());
        Assert.assertEquals(1, page.get("count").getAsInt());

        Assert.assertTrue(page.has("resultSet"));
        JsonArray results = page.get("resultSet").getAsJsonArray();
        Assert.assertEquals(1, results.size());

        for (int i = 0; i < results.size(); ++i)
        {
          JsonObject problem = results.get(0).getAsJsonObject();
          Assert.assertEquals(historyId, problem.get("historyId").getAsString());
          Assert.assertEquals(CurationResolution.UNRESOLVED.name(), problem.get("resolution").getAsString());
          Assert.assertEquals(GeoObjectProblemType.NO_GEOMETRY.name(), problem.get("type").getAsString());
          Assert.assertEquals(curationProblemId, problem.get("id").getAsString());
          Assert.assertEquals(FastTestDataset.PROVINCE.getCode(), problem.get("typeCode").getAsString());
          Assert.assertEquals(FastTestDataset.PROV_CENTRAL.getCode(), problem.get("goCode").getAsString());
        }
      });
    }

    // Disallowed Users
    TestUserInfo[] disallowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM };

    for (TestUserInfo user : disallowedUsers)
    {
      FastTestDataset.runAsUser(user, (request, adapter) -> {
        CurationControllerWrapper controller = new CurationControllerWrapper(adapter, request);

        try
        {
          controller.details(history.getOid(), false, 10, 1);

          Assert.fail("Expected to get an exception.");
        }
        catch (SmartExceptionDTO e)
        {
          Assert.assertTrue(OrganizationRAException.CLASS.equals(e.getType()) || OrganizationRMException.CLASS.equals(e.getType()));
        }
      });
    }
  }

  @Test
  @Request
  public void testCurator() throws Throwable
  {
    TestGeoObjectInfo noGeomGo = testData.newTestGeoObjectInfo("curationJobTest-NoGeom", FastTestDataset.PROVINCE);
    noGeomGo.setWkt(null);
    noGeomGo.apply();

    version.publishNoAuth();

    ListCurationHistory history = (ListCurationHistory) job.createNewHistory();
    history.appLock();
    history.clearStatus();
    history.addStatus(AllJobStatus.RUNNING);
    history.setVersion(version);
    history.apply();

    ExecutionContext context = MockScheduler.executeJob(job, history);

    ListCurationHistory hist = (ListCurationHistory) context.getHistory();

    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    Long count = version.buildQuery(new JsonObject()).getCount();

    Assert.assertEquals(count, hist.getWorkTotal());
    Assert.assertEquals(count, hist.getWorkProgress());

    List<? extends CurationProblem> problems = hist.getAllCurationProblems();

    Assert.assertEquals(1, problems.size());

    CurationProblem problem = problems.get(0);

    Assert.assertEquals(CurationResolution.UNRESOLVED.name(), problem.getResolution());
    Assert.assertEquals(GeoObjectProblemType.NO_GEOMETRY.name(), problem.getProblemType());

    Assert.assertEquals(history.getOid(), problem.getHistory().getOid());

    Assert.assertTrue(problem instanceof GeoObjectProblem);

    GeoObjectProblem goProblem = (GeoObjectProblem) problem;

    Assert.assertEquals(FastTestDataset.PROVINCE.getCode(), goProblem.getTypeCode());
    Assert.assertEquals(noGeomGo.getCode(), goProblem.getGoCode());
  }

  @Test
  public void testCurate() throws Throwable
  {
    Object[] setup = setUpTestCurate();
    final TestGeoObjectInfo noGeomGo = (TestGeoObjectInfo) setup[0];
    final String listTypeVersionId = (String) setup[1];

    // Allowed Users
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_ADMIN, FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM };

    for (TestUserInfo user : allowedUsers)
    {
      FastTestDataset.runAsUser(user, (request, adapter) -> {

        final CurationControllerWrapper controller = new CurationControllerWrapper(adapter, request);

        JsonObject joHistory = controller.curate(listTypeVersionId);

        try
        {
          waitUntilSuccess(joHistory);
        }
        catch (Throwable e)
        {
          throw new RuntimeException(e);
        }

        JsonObject page = controller.page(joHistory.get("historyId").getAsString(), false, 10, 1);

        JsonArray results = page.get("resultSet").getAsJsonArray();

        Assert.assertEquals(1, results.size());

        for (int i = 0; i < results.size(); ++i)
        {
          JsonObject problem = results.get(i).getAsJsonObject();

          Assert.assertEquals(joHistory.get("historyId").getAsString(), problem.get("historyId").getAsString());
          Assert.assertEquals(CurationResolution.UNRESOLVED.name(), problem.get("resolution").getAsString());
          Assert.assertEquals(GeoObjectProblemType.NO_GEOMETRY.name(), problem.get("type").getAsString());
          Assert.assertTrue(problem.get("id").getAsString() != null && problem.get("id").getAsString() != "");
          Assert.assertEquals(FastTestDataset.PROVINCE.getCode(), problem.get("typeCode").getAsString());
          Assert.assertEquals(noGeomGo.getCode(), problem.get("goCode").getAsString());
        }

      });
    }

    // Disallowed Users
    TestUserInfo[] disallowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM };

    for (TestUserInfo user : disallowedUsers)
    {
      FastTestDataset.runAsUser(user, (request, adapter) -> {

        final CurationControllerWrapper controller = new CurationControllerWrapper(adapter, request);

        try
        {
          controller.curate(listTypeVersionId);

          Assert.fail("Expected to get an exception.");
        }
        catch (SmartExceptionDTO e)
        {
          Assert.assertTrue(OrganizationRAException.CLASS.equals(e.getType()) || OrganizationRMException.CLASS.equals(e.getType()));
        }

      });
    }
  }

  @Request
  private Object[] setUpTestCurate() throws Throwable
  {
    TestGeoObjectInfo noGeomGo = testData.newTestGeoObjectInfo("curationJobTest-NoGeom", FastTestDataset.PROVINCE);
    noGeomGo.setWkt(null);
    noGeomGo.apply();

    version.publishNoAuth();

    return new Object[] { noGeomGo, version.getOid() };
  }

  @Request
  private void waitUntilSuccess(JsonObject joHistory) throws Throwable
  {
    ListCurationHistory history = ListCurationHistory.get(joHistory.get("historyId").getAsString());

    SchedulerTestUtils.waitUntilStatus(history.getOid(), AllJobStatus.SUCCESS);
  }
}
