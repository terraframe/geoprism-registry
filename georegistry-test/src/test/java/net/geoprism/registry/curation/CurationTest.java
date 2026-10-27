/**
 *
 */
package net.geoprism.registry.curation;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistoryRecord;
import com.runwaysdk.system.scheduler.SchedulerManager;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.ListTypeEntry;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.OrganizationRAException;
import net.geoprism.registry.OrganizationRMException;
import net.geoprism.registry.SingleListType;
import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.curation.CurationProblem.CurationResolution;
import net.geoprism.registry.curation.GeoObjectProblem.GeoObjectProblemType;
import net.geoprism.registry.service.ListTypeTest;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.SchedulerTestUtils;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestUserInfo;
import net.geoprism.registry.test.curation.CurationControllerWrapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
public class CurationTest extends FastDatasetTest implements InstanceTestClassListener
{
  @Autowired
  private CurationControllerWrapper controller;

  private ListCurationHistory       history;

  private String                    historyId;

  private ListCurationJob           job;

  private JobHistoryRecord          jobHistoryRecord;

  private ListTypeVersion           version;

  private SingleListType            list;

  private ListTypeEntry             entry;

  private GeoObjectProblem          curationProblem;

  private String                    curationProblemId;

  @Override
  public void beforeClassSetup() throws Exception
  {
    super.beforeClassSetup();

    TestDataSet.deleteAllSchedulerData();
    TestDataSet.deleteAllListData();

    if (!SchedulerManager.initialized())
    {
      SchedulerManager.start();
    }
  }

  @BeforeEach
  @Override
  public void setUp() throws Exception
  {
    super.setUp();

    createTestInstanceData();
  }

  @AfterEach
  @Override
  public void tearDown() throws Exception
  {
    super.tearDown();

    TestDataSet.deleteAllSchedulerData();
    TestDataSet.deleteAllListData();
  }

  @Request
  private void createTestInstanceData()
  {
    list = new SingleListType();
    list.setGeoObjectType(FastTestDataset.PROVINCE.getServerObject().getType());
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

    Assertions.assertEquals(2, versions.size());

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
      FastTestDataset.runAsUser(user, (request) -> {
        JsonObject details = controller.details(history.getOid(), false, 10, 1);

        Assertions.assertTrue(details.has("page"));
        JsonObject page = details.get("page").getAsJsonObject();
        Assertions.assertTrue(page.has("count"));
        Assertions.assertTrue(page.has("pageNumber"));
        Assertions.assertTrue(page.has("pageSize"));
        Assertions.assertTrue(page.has("resultSet"));

        Assertions.assertEquals(AllJobStatus.RUNNING.name(), details.get("status").getAsString());
        Assertions.assertEquals(GeoRegistryUtil.formatDate(history.getCreateDate(), false), details.get("lastRun").getAsString());
        Assertions.assertEquals(historyId, details.get("historyId").getAsString());
        Assertions.assertEquals(job.getOid(), details.get("jobId").getAsString());
        Assertions.assertFalse(details.has("exception"));
        Assertions.assertEquals(TestDataSet.ADMIN_USER_NAME, details.get("lastRunBy").getAsString());
      });
    }

    // Disallowed Users
    TestUserInfo[] disallowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM };

    for (TestUserInfo user : disallowedUsers)
    {
      FastTestDataset.runAsUser(user, (request) -> {
        try
        {
          controller.details(history.getOid(), false, 10, 1);

          Assertions.fail("Expected to get an exception.");
        }
        catch (SmartExceptionDTO e)
        {
          Assertions.assertTrue(OrganizationRAException.CLASS.equals(e.getType()) || OrganizationRMException.CLASS.equals(e.getType()));
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
      FastTestDataset.runAsUser(user, (request) -> {
        JsonObject page = controller.page(history.getOid(), false, 10, 1);

        Assertions.assertEquals(1, page.get("pageNumber").getAsInt());
        Assertions.assertEquals(10, page.get("pageSize").getAsInt());
        Assertions.assertEquals(1, page.get("count").getAsInt());

        Assertions.assertTrue(page.has("resultSet"));
        JsonArray results = page.get("resultSet").getAsJsonArray();
        Assertions.assertEquals(1, results.size());

        for (int i = 0; i < results.size(); ++i)
        {
          JsonObject problem = results.get(0).getAsJsonObject();
          Assertions.assertEquals(historyId, problem.get("historyId").getAsString());
          Assertions.assertEquals(CurationResolution.UNRESOLVED.name(), problem.get("resolution").getAsString());
          Assertions.assertEquals(GeoObjectProblemType.NO_GEOMETRY.name(), problem.get("type").getAsString());
          Assertions.assertEquals(curationProblemId, problem.get("id").getAsString());
          Assertions.assertEquals(FastTestDataset.PROVINCE.getCode(), problem.get("typeCode").getAsString());
          Assertions.assertEquals(FastTestDataset.PROV_CENTRAL.getCode(), problem.get("goCode").getAsString());
        }
      });
    }

    // Disallowed Users
    TestUserInfo[] disallowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM };

    for (TestUserInfo user : disallowedUsers)
    {
      FastTestDataset.runAsUser(user, (request) -> {
        try
        {
          controller.details(history.getOid(), false, 10, 1);

          Assertions.fail("Expected to get an exception.");
        }
        catch (SmartExceptionDTO e)
        {
          Assertions.assertTrue(OrganizationRAException.CLASS.equals(e.getType()) || OrganizationRMException.CLASS.equals(e.getType()));
        }
      });
    }
  }

  @Test
  @Request
  public void testCurator() throws Throwable
  {
    TestGeoObjectInfo noGeomGo = testData.newTestGeoObjectInfo("curationJobTest-NoGeom", FastTestDataset.PROVINCE, FastTestDataset.SOURCE);
    noGeomGo.setWkt(null);
    noGeomGo.apply();

    version.publishNoAuth();

    ListCurationHistory history = (ListCurationHistory) job.createNewHistory();
    history.appLock();
    history.clearStatus();
    history.addStatus(AllJobStatus.RUNNING);
    history.setVersion(version);
    history.apply();

    ExecutionContext context = job.startSynchronously(history);

    ListCurationHistory hist = (ListCurationHistory) context.getHistory();

    Assertions.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    Long count = version.buildQuery(new JsonObject()).getCount();

    Assertions.assertEquals(count, hist.getWorkTotal());
    Assertions.assertEquals(count, hist.getWorkProgress());

    List<? extends CurationProblem> problems = hist.getAllCurationProblems();

    Assertions.assertEquals(1, problems.size());

    CurationProblem problem = problems.get(0);

    Assertions.assertEquals(CurationResolution.UNRESOLVED.name(), problem.getResolution());
    Assertions.assertEquals(GeoObjectProblemType.NO_GEOMETRY.name(), problem.getProblemType());

    Assertions.assertEquals(history.getOid(), problem.getHistory().getOid());

    Assertions.assertTrue(problem instanceof GeoObjectProblem);

    GeoObjectProblem goProblem = (GeoObjectProblem) problem;

    Assertions.assertEquals(FastTestDataset.PROVINCE.getCode(), goProblem.getTypeCode());
    Assertions.assertEquals(noGeomGo.getCode(), goProblem.getGoCode());
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
      FastTestDataset.runAsUser(user, (request) -> {

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

        Assertions.assertEquals(1, results.size());

        for (int i = 0; i < results.size(); ++i)
        {
          JsonObject problem = results.get(i).getAsJsonObject();

          Assertions.assertEquals(joHistory.get("historyId").getAsString(), problem.get("historyId").getAsString());
          Assertions.assertEquals(CurationResolution.UNRESOLVED.name(), problem.get("resolution").getAsString());
          Assertions.assertEquals(GeoObjectProblemType.NO_GEOMETRY.name(), problem.get("type").getAsString());
          Assertions.assertTrue(problem.get("id").getAsString() != null && problem.get("id").getAsString() != "");
          Assertions.assertEquals(FastTestDataset.PROVINCE.getCode(), problem.get("typeCode").getAsString());
          Assertions.assertEquals(noGeomGo.getCode(), problem.get("goCode").getAsString());
        }

      });
    }

    // Disallowed Users
    TestUserInfo[] disallowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM };

    for (TestUserInfo user : disallowedUsers)
    {
      FastTestDataset.runAsUser(user, (request) -> {

        try
        {
          controller.curate(listTypeVersionId);

          Assertions.fail("Expected to get an exception.");
        }
        catch (SmartExceptionDTO e)
        {
          Assertions.assertTrue(OrganizationRAException.CLASS.equals(e.getType()) || OrganizationRMException.CLASS.equals(e.getType()));
        }

      });
    }
  }

  @Request
  private Object[] setUpTestCurate() throws Throwable
  {
    TestGeoObjectInfo noGeomGo = testData.newTestGeoObjectInfo("curationJobTest-NoGeom", FastTestDataset.PROVINCE, FastTestDataset.SOURCE);
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
