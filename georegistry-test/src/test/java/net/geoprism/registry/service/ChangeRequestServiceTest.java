/**
 *
 */
package net.geoprism.registry.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeGeometryType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTimeCollection;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.CGRPermissionException;
import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.action.AbstractAction;
import net.geoprism.registry.action.AllGovernanceStatus;
import net.geoprism.registry.action.ChangeRequest;
import net.geoprism.registry.action.ChangeRequestQuery;
import net.geoprism.registry.action.geoobject.CreateGeoObjectAction;
import net.geoprism.registry.action.geoobject.CreateGeoObjectActionBase;
import net.geoprism.registry.action.geoobject.UpdateAttributeAction;
import net.geoprism.registry.action.geoobject.UpdateAttributeActionBase;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.request.ChangeRequestService;
import net.geoprism.registry.service.request.GeoObjectEditorServiceIF;
import net.geoprism.registry.service.request.ServiceFactory;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestRegistryAdapter;
import net.geoprism.registry.test.TestUserInfo;
import net.geoprism.registry.view.Page;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;
import net.geoprism.registry.view.action.UpdateAttributeViewJsonAdapters;
import net.geoprism.registry.view.action.UpdateParentValueOverTimeView;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class ChangeRequestServiceTest extends FastDatasetTest implements InstanceTestClassListener
{
  public static final TestGeoObjectInfo BELIZE                  = new TestGeoObjectInfo("Belize", FastTestDataset.COUNTRY);

  public static final TestGeoObjectInfo TEST_NEW_PROVINCE       = new TestGeoObjectInfo("CR_TEST_NEW_PROVINCE", FastTestDataset.PROVINCE);

  private static final String           NEW_ANTHEM              = "NEW_ANTHEM";

  // The
  // complex
  // update
  // test
  // assumes
  // this
  // date
  // is
  // AFTER
  // the
  // old
  // date.
  private static final String           NEW_START_DATE          = "2020-05-04";

  private static final String           NEW_END_DATE            = "2021-05-04";

  private static final String           OLD_START_DATE          = "2020-04-04";

  private static final String           OLD_END_DATE            = "2020-04-04";

  private String                        UPDATE_ATTR_JSON        = null;

  private String                        UPDATE_PARENT_ATTR_JSON = null;

  @Autowired
  GeoObjectBusinessServiceIF            goBizService;

  @Autowired
  GeoObjectEditorServiceIF              goEditorService;

  @Autowired
  ChangeRequestService                  changeService;

  @Autowired
  TestRegistryAdapter                   adapter;

  @Before
  public void setUp()
  {
    testData.setUpInstanceData();

    BELIZE.apply();

    this.setUpTestInstanceData();
  }

  @Request
  private void setUpTestInstanceData()
  {
    ServerGeoObjectIF cambodia = FastTestDataset.CAMBODIA.getServerObject();
    String votOid = cambodia.getValuesOverTime(FastTestDataset.AT_National_Anthem.getAttributeName()).getValueOverTime(FastTestDataset.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE).getOid();

    UPDATE_ATTR_JSON = "{" + "\"valuesOverTime\" : [" + "{" + "\"oid\" : \"" + votOid + "\"," + "\"action\" : \"UPDATE\"," + "\"oldValue\" : \"" + FastTestDataset.CAMBODIA.getDefaultValue(FastTestDataset.AT_National_Anthem.getAttributeName()) + "\"," + "\"newValue\" : \"" + NEW_ANTHEM + "\"," + "\"newStartDate\" : \"" + NEW_START_DATE + "\"," + "\"newEndDate\" : \"" + NEW_END_DATE + "\"," + "\"oldStartDate\" : \"" + OLD_START_DATE + "\"," + "\"oldEndDate\" : \"" + OLD_END_DATE + "\"" + "}" + "]" + "}";

    ServerGeoObjectIF central_prov = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerParentTreeNode ptn = goBizService.getParentGeoObjects(central_prov, null, new String[] { FastTestDataset.COUNTRY.getCode() }, false, false, TestDataSet.DEFAULT_OVER_TIME_DATE).getParents().get(0);

    UPDATE_PARENT_ATTR_JSON = "{" + "\"hierarchyCode\" : \"" + FastTestDataset.HIER_ADMIN.getCode() + "\"," + "\"valuesOverTime\" : [" + "{" + "\"oid\" : \"" + ptn.getOid() + "\"," + "\"action\" : \"UPDATE\"," + "\"oldValue\" : \"" + FastTestDataset.COUNTRY.getCode() + UpdateParentValueOverTimeView.VALUE_SPLIT_TOKEN + FastTestDataset.CAMBODIA.getCode() + "\"," + "\"newValue\" : \"" + FastTestDataset.COUNTRY.getCode() + UpdateParentValueOverTimeView.VALUE_SPLIT_TOKEN + BELIZE.getCode() + "\"," + "\"newStartDate\" : \"" + NEW_START_DATE + "\"," + "\"newEndDate\" : \"" + NEW_END_DATE + "\"," + "\"oldStartDate\" : \"" + OLD_START_DATE + "\"," + "\"oldEndDate\" : \"" + OLD_END_DATE + "\"" + "}" + "]" + "}";
  }

  @After
  public void tearDown()
  {
    BELIZE.delete();

    testData.tearDownInstanceData();

    TestDataSet.deleteAllChangeRequests();
  }

  @Request
  private String createTestChangeRequest(String actionType)
  {
    return createCRTrans(actionType);
  }

  @Transaction
  private String createCRTrans(String actionType)
  {
    ChangeRequest cr = new ChangeRequest();
    cr.addApprovalStatus(AllGovernanceStatus.PENDING);
    cr.setOrganizationCode(FastTestDataset.ORG_CGOV.getCode());
    if (actionType.equals(UpdateAttributeViewJsonAdapters.PARENT_ATTR_NAME))
    {

      cr.setGeoObjectCode(FastTestDataset.PROV_CENTRAL.getCode());
      cr.setGeoObjectTypeCode(FastTestDataset.PROV_CENTRAL.getGeoObjectType().getCode());
    }
    else
    {
      cr.setGeoObjectCode(FastTestDataset.CAMBODIA.getCode());
      cr.setGeoObjectTypeCode(FastTestDataset.CAMBODIA.getGeoObjectType().getCode());
    }
    cr.apply();

    AbstractAction action = null;

    if (actionType.equals(CreateGeoObjectAction.CLASS))
    {
      action = new CreateGeoObjectAction();
      action.setApiVersion("1.0");
      ( (CreateGeoObjectActionBase) action ).setGeoObjectJson(FastTestDataset.CAMBODIA.fetchGeoObjectOverTime().toJSON().toString());
      action.addApprovalStatus(AllGovernanceStatus.PENDING);
      action.setCreateActionDate(new Date());
      action.apply();
    }
    else if (actionType.equals(UpdateAttributeAction.CLASS))
    {
      action = new UpdateAttributeAction();
      action.setApiVersion("1.0");
      ( (UpdateAttributeActionBase) action ).setAttributeName(FastTestDataset.AT_National_Anthem.getAttributeName());
      ( (UpdateAttributeActionBase) action ).setJson(UPDATE_ATTR_JSON);
      action.addApprovalStatus(AllGovernanceStatus.PENDING);
      action.setCreateActionDate(new Date());
      action.apply();
    }
    else if (actionType.equals(UpdateAttributeViewJsonAdapters.PARENT_ATTR_NAME))
    {
      action = new UpdateAttributeAction();
      action.setApiVersion("1.0");
      ( (UpdateAttributeActionBase) action ).setAttributeName(UpdateAttributeViewJsonAdapters.PARENT_ATTR_NAME);
      ( (UpdateAttributeActionBase) action ).setJson(UPDATE_PARENT_ATTR_JSON);
      action.addApprovalStatus(AllGovernanceStatus.PENDING);
      action.setCreateActionDate(new Date());
      action.apply();
    }
    else
    {
      throw new UnsupportedOperationException();
    }

    cr.addAction(action).apply();

    return cr.toJSON().toString();
  }

  @Test
  public void testGetAllCR()
  {
    createTestChangeRequest(CreateGeoObjectAction.CLASS);
    createTestChangeRequest(UpdateAttributeAction.CLASS);

    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_ADMIN, FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC };

    for (TestUserInfo user : allowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {
          testGetAllCR(request, true);
        });
      }
      catch (SmartExceptionDTO e)
      {
        e.printStackTrace();
        Assert.fail("Unexpected permission exception was thrown on user [" + user.getUsername() + "].");
      }
    }

    TestUserInfo[] disAllowedUsers = new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC };

    for (TestUserInfo user : disAllowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {
          testGetAllCR(request, false);
        });
      }
      catch (SmartExceptionDTO e)
      {
        e.printStackTrace();
        Assert.fail("Unexpected permission exception was thrown on user [" + user.getUsername() + "].");
      }
    }
  }

  private void testGetAllCR(ClientRequestIF request, boolean hasPermission)
  {
    Page<ChangeRequest> page = changeService.getAllRequests(request.getSessionId(), 10, 1, "", "", null);

    JsonObject joPage = toJson(request.getSessionId(), page);
    JsonArray jaResults = joPage.get("resultSet").getAsJsonArray();

    final int count = joPage.get("count").getAsInt();
    final int pageNumber = joPage.get("pageNumber").getAsInt();

    if (hasPermission)
    {
      Assert.assertEquals(2, count);
      Assert.assertEquals(1, pageNumber);
      Assert.assertEquals(2, jaResults.size());

      for (int i = 0; i < jaResults.size(); ++i)
      {
        JsonObject joRequest = jaResults.get(i).getAsJsonObject();

        JsonObject current = joRequest.get("current").getAsJsonObject();

        // JsonObject geoObject = current.get("geoObject").getAsJsonObject();
        // JsonObject geoObjectType =
        // current.get("geoObjectType").getAsJsonObject();
        //
        // Assert.assertNotNull(geoObject);
        // Assert.assertNotNull(geoObjectType);

        // Assert.assertEquals(FastTestDataset.CAMBODIA.getDisplayLabel(),
        // geoObject.get("label").getAsString());
        // Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(),
        // geoObject.get("code").getAsString());
      }
    }
    else
    {
      Assert.assertEquals(0, count);
      Assert.assertEquals(0, jaResults.size());
    }
  }

  @Request(RequestType.SESSION)
  private JsonObject toJson(String sessionId, Page<ChangeRequest> page)
  {
    return page.toJSON();
  }

  @Test
  public void testSetActionStatus()
  {
    String serializedCR = createTestChangeRequest(UpdateAttributeAction.CLASS);

    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_ADMIN, FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM };

    for (TestUserInfo user : allowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {
          testSetActionStatus(request, serializedCR);
        });
      }
      catch (SmartExceptionDTO e)
      {
        e.printStackTrace();
        Assert.fail("Unexpected permission exception was thrown on user [" + user.getUsername() + "].");
      }
    }

    TestUserInfo[] disAllowedUsers = new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };

    for (TestUserInfo user : disAllowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {
          testSetActionStatus(request, serializedCR);

          Assert.fail("Expected a permission exception to be thrown on user [" + user.getUsername() + "].");
        });
      }
      catch (SmartExceptionDTO e)
      {
        Assert.assertEquals(e.getType(), CGRPermissionException.CLASS);
      }
    }
  }

  private void testSetActionStatus(ClientRequestIF request, String serializedCR)
  {
    final String crOid = JsonParser.parseString(serializedCR).getAsJsonObject().get("oid").getAsString();

    changeService.setActionStatus(request.getSessionId(), testSetActionStatusGetCRAction(crOid), AllGovernanceStatus.ACCEPTED.name());

    testSetActionStatusVerifyCRAction(crOid);
  }

  @Request
  private String testSetActionStatusGetCRAction(String crOid)
  {
    ChangeRequest cr = ChangeRequest.get(crOid);
    return cr.getAllAction().next().getOid();
  }

  @Request
  private void testSetActionStatusVerifyCRAction(String crOid)
  {
    ChangeRequest cr2 = ChangeRequest.get(crOid);
    AbstractAction action2 = cr2.getAllAction().next();

    Assert.assertEquals(AllGovernanceStatus.ACCEPTED.name(), action2.getGovernanceStatus().name());
  }

  @Test
  public void testImplementDecisions()
  {
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_ADMIN, FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM };

    for (TestUserInfo user : allowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {
          testImplementDecisions(request, createTestChangeRequest(UpdateAttributeAction.CLASS));
        });
      }
      catch (SmartExceptionDTO e)
      {
        e.printStackTrace();
        Assert.fail("Unexpected exception was thrown on user [" + user.getUsername() + "].");
      }
    }

    TestUserInfo[] disAllowedUsers = new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };

    for (TestUserInfo user : disAllowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {
          testImplementDecisions(request, createTestChangeRequest(UpdateAttributeAction.CLASS));

          Assert.fail("Expected a permission exception to be thrown on user [" + user.getUsername() + "].");
        });
      }
      catch (SmartExceptionDTO e)
      {
        Assert.assertEquals(e.getType(), CGRPermissionException.CLASS);
      }
    }
  }

  private void testImplementDecisions(ClientRequestIF request, String serializedCR) throws Exception
  {
    String crOid = JsonParser.parseString(serializedCR).getAsJsonObject().get("oid").getAsString();

    testSetActionStatus(request, serializedCR);

    changeService.implementDecisions(request.getSessionId(), serializedCR, null);

    testImplementDecisionsVerify(crOid);
  }

  @Request
  private void testImplementDecisionsVerify(String crOid) throws Exception
  {
    ChangeRequest cr = ChangeRequest.get(crOid);

    Assert.assertEquals(AllGovernanceStatus.ACCEPTED.name(), cr.getApprovalStatus().get(0).name());

    AbstractAction action = cr.getAllAction().next();

    Assert.assertEquals(AllGovernanceStatus.ACCEPTED.name(), action.getApprovalStatus().get(0).name());

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    sdf.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    Date newStartDate = sdf.parse(NEW_START_DATE);
    Date newEndDate = sdf.parse(NEW_END_DATE);

    ServerGeoObjectIF serverGo = FastTestDataset.CAMBODIA.getServerObject();

    ValueOverTimeCollection votc = serverGo.getValuesOverTime(FastTestDataset.AT_National_Anthem.getAttributeName());

    Assert.assertEquals(1, votc.size());

    ValueOverTime vot = votc.get(0);

    Assert.assertEquals(newStartDate, vot.getStartDate());
    Assert.assertEquals(newEndDate, vot.getEndDate());
    Assert.assertEquals(NEW_ANTHEM, vot.getValue());
    Assert.assertEquals(NEW_ANTHEM, serverGo.getValue(FastTestDataset.AT_National_Anthem.getAttributeName(), newStartDate));
  }

  @Test
  public void testImplementParentDecisions()
  {
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_ADMIN, FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM };

    for (TestUserInfo user : allowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {
          testImplementParentDecisions(request, createTestChangeRequest(UpdateAttributeViewJsonAdapters.PARENT_ATTR_NAME));
        });
      }
      catch (SmartExceptionDTO e)
      {
        e.printStackTrace();
        Assert.fail("Unexpected exception was thrown on user [" + user.getUsername() + "].");
      }

      tearDown();
      setUp();
    }

    TestUserInfo[] disAllowedUsers = new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };

    for (TestUserInfo user : disAllowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {
          testImplementParentDecisions(request, createTestChangeRequest(UpdateAttributeViewJsonAdapters.PARENT_ATTR_NAME));

          Assert.fail("Expected a permission exception to be thrown on user [" + user.getUsername() + "].");
        });
      }
      catch (SmartExceptionDTO e)
      {
        Assert.assertEquals(e.getType(), CGRPermissionException.CLASS);
      }
    }
  }

  private void testImplementParentDecisions(ClientRequestIF request, String serializedCR) throws Exception
  {
    final String crOid = JsonParser.parseString(serializedCR).getAsJsonObject().get("oid").getAsString();

    testSetActionStatus(request, serializedCR);

    changeService.implementDecisions(request.getSessionId(), serializedCR, null);

    testImplementParentDecisionsVerify(crOid);
  }

  @Request
  private void testImplementParentDecisionsVerify(String crOid) throws Exception
  {
    ChangeRequest cr = ChangeRequest.get(crOid);

    Assert.assertEquals(AllGovernanceStatus.ACCEPTED.name(), cr.getApprovalStatus().get(0).name());

    AbstractAction action = cr.getAllAction().next();

    Assert.assertEquals(AllGovernanceStatus.ACCEPTED.name(), action.getApprovalStatus().get(0).name());

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    sdf.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    Date newStartDate = sdf.parse(NEW_START_DATE);
    Date newEndDate = sdf.parse(NEW_END_DATE);

    ServerGeoObjectIF serverGo = FastTestDataset.PROV_CENTRAL.getServerObject();

    List<ServerParentTreeNode> ptns = goBizService.getParentsOverTime(serverGo, new String[] { FastTestDataset.COUNTRY.getCode() }, false, false).getEntries(FastTestDataset.HIER_ADMIN.getServerObject());
    Assert.assertEquals(1, ptns.size());

    ServerParentTreeNode ptn = ptns.get(0);

    List<ServerParentTreeNode> parents = ptn.getParents();
    Assert.assertEquals(1, parents.size());

    ServerParentTreeNode parent = parents.get(0);

    Assert.assertEquals(newStartDate, ptn.getStartDate());
    Assert.assertEquals(newEndDate, ptn.getEndDate());
    Assert.assertEquals(BELIZE.getCode(), parent.getGeoObject().getCode());
  }

  @Test
  public void testCreateGeoObjectCR() throws Exception
  {
    String[] json = testCreateGeoObjectCR_Json();

    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RC };

    for (TestUserInfo user : allowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {
          testCreateGeoObjectCR(json, request);
        });
      }
      catch (SmartExceptionDTO e)
      {
        e.printStackTrace();
        Assert.fail("Unexpected exception was thrown on user [" + user.getUsername() + "].");
      }

      tearDown();
      setUp();
    }

    TestUserInfo[] disAllowedUsers = new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC, FastTestDataset.USER_CGOV_AC };

    for (TestUserInfo user : disAllowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {
          testCreateGeoObjectCR(json, request);

          Assert.fail("Expected a permission exception to be thrown on user [" + user.getUsername() + "].");
        });
      }
      catch (SmartExceptionDTO e)
      {
        Assert.assertEquals(e.getType(), CGRPermissionException.CLASS);
      }
    }
  }

  private void testCreateGeoObjectCR(String[] json, ClientRequestIF request) throws Exception
  {
    JsonObject jo = goEditorService.createGeoObject(request.getSessionId(), json[0], json[1], null, "test-notes");

    Assert.assertEquals(true, jo.get("isChangeRequest").getAsBoolean());
    Assert.assertEquals(36, jo.get("changeRequestId").getAsString().length());

    testCreateGeoObjectCR_Verify();
  }

  @Request
  private String[] testCreateGeoObjectCR_Json() throws Exception
  {
    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    sdf.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    final Date newStartDate = sdf.parse(NEW_START_DATE);
    final Date newEndDate = sdf.parse(NEW_END_DATE);

    String[] ret = new String[2];

    ServerParentTreeNodeOverTime ptnot = new ServerParentTreeNodeOverTime(FastTestDataset.PROVINCE.getServerObject());
    ServerParentTreeNode childNode = new ServerParentTreeNode(null, FastTestDataset.HIER_ADMIN.getServerObject(), newStartDate, newEndDate, null);
    childNode.addParent(new ServerParentTreeNode(FastTestDataset.CAMBODIA.getServerObject(), FastTestDataset.HIER_ADMIN.getServerObject(), newStartDate, newEndDate, null));
    ptnot.add(FastTestDataset.HIER_ADMIN.getServerObject(), childNode);

    ret[0] = ptnot.toJSON().toString();

    ret[1] = TEST_NEW_PROVINCE.newGeoObjectOverTime(adapter).toJSON().toString();

    return ret;
  }

  @Request
  private void testCreateGeoObjectCR_Verify()
  {
    ChangeRequestQuery crq = new ChangeRequestQuery(new QueryFactory());

    Assert.assertEquals(1, crq.getCount());

    ChangeRequest cr = crq.getIterator().next();

    Assert.assertEquals(AllGovernanceStatus.PENDING.name(), cr.getGovernanceStatus().name());

    AbstractAction action = cr.getAllAction().next();

    Assert.assertTrue(action instanceof CreateGeoObjectAction);

    Assert.assertEquals(TEST_NEW_PROVINCE.getCode(), cr.getGeoObjectCode());
    Assert.assertEquals(TEST_NEW_PROVINCE.getGeoObjectType().getCode(), cr.getGeoObjectTypeCode());
    Assert.assertEquals(FastTestDataset.ORG_CGOV.getCode(), cr.getOrganizationCode());
  }

  @Test
  public void testUpdateGeoObjectCR() throws Exception
  {
    String json = testUpdateGeoObjectCR_Json();

    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RC };

    for (TestUserInfo user : allowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {
          testUpdateGeoObjectCR(json, request);
        });
      }
      catch (SmartExceptionDTO e)
      {
        e.printStackTrace();
        Assert.fail("Unexpected exception was thrown on user [" + user.getUsername() + "].");
      }

      tearDown();
      setUp();
    }

    TestUserInfo[] disAllowedUsers = new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC, FastTestDataset.USER_CGOV_AC };

    for (TestUserInfo user : disAllowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {
          testUpdateGeoObjectCR(json, request);

          Assert.fail("Expected a permission exception to be thrown on user [" + user.getUsername() + "].");
        });
      }
      catch (SmartExceptionDTO e)
      {
        Assert.assertEquals(e.getType(), CGRPermissionException.CLASS);
      }
    }
  }

  private void testUpdateGeoObjectCR(String json, ClientRequestIF request) throws Exception
  {
    JsonObject jo = goEditorService.updateGeoObject(request.getSessionId(), FastTestDataset.CAMBODIA.getCode(), FastTestDataset.COUNTRY.getCode(), json, null, "test-notes");

    Assert.assertEquals(true, jo.get("isChangeRequest").getAsBoolean());
    Assert.assertEquals(36, jo.get("changeRequestId").getAsString().length());

    testUpdateGeoObjectCR_Verify();
  }

  @Request
  private String testUpdateGeoObjectCR_Json() throws Exception
  {
    JsonArray ja = new JsonArray();

    JsonObject jo = new JsonObject();

    jo.addProperty("attributeName", FastTestDataset.AT_National_Anthem.getAttributeName());
    jo.add("attributeDiff", JsonParser.parseString(UPDATE_ATTR_JSON).getAsJsonObject());

    ja.add(jo);

    return ja.toString();
  }

  @Request
  private void testUpdateGeoObjectCR_Verify()
  {
    ChangeRequestQuery crq = new ChangeRequestQuery(new QueryFactory());

    Assert.assertEquals(1, crq.getCount());

    ChangeRequest cr = crq.getIterator().next();

    Assert.assertEquals(AllGovernanceStatus.PENDING.name(), cr.getGovernanceStatus().name());

    AbstractAction action = cr.getAllAction().next();

    Assert.assertTrue(action instanceof UpdateAttributeAction);

    Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), cr.getGeoObjectCode());
    Assert.assertEquals(FastTestDataset.CAMBODIA.getGeoObjectType().getCode(), cr.getGeoObjectTypeCode());
    Assert.assertEquals(FastTestDataset.ORG_CGOV.getCode(), cr.getOrganizationCode());
  }

  /**
   * This usecase tests the create and delete actions for value over time
   * objects.
   */
  @Test
  public void testComplexUpdateGeoObjectCR() throws Exception
  {
    String[] data = testComplexUpdateGeoObjectCR_applyCR();

    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA };

    for (TestUserInfo user : allowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {
          testComplexUpdateGeoObjectCR(data, request);
        });
      }
      catch (SmartExceptionDTO e)
      {
        e.printStackTrace();
        Assert.fail("Unexpected exception was thrown on user [" + user.getUsername() + "].");
      }
    }
  }

  private void testComplexUpdateGeoObjectCR(String[] data, ClientRequestIF request) throws Exception
  {
    changeService.implementDecisions(request.getSessionId(), data[0], null);

    testComplexUpdateGeoObjectCR_Verify(data);
  }

  @Request
  private String[] testComplexUpdateGeoObjectCR_applyCR() throws Exception
  {
    ChangeRequest cr = new ChangeRequest();
    cr.addApprovalStatus(AllGovernanceStatus.PENDING);
    cr.setOrganizationCode(FastTestDataset.ORG_CGOV.getCode());
    cr.setGeoObjectCode(FastTestDataset.CAMBODIA.getCode());
    cr.setGeoObjectTypeCode(FastTestDataset.CAMBODIA.getGeoObjectType().getCode());
    cr.apply();

    UpdateAttributeAction action = new UpdateAttributeAction();
    action.setApiVersion("1.0");
    ( (UpdateAttributeActionBase) action ).setAttributeName(FastTestDataset.AT_National_Anthem.getAttributeName());

    JsonObject diff = new JsonObject();

    ServerGeoObjectIF cambodia = FastTestDataset.CAMBODIA.getServerObject();
    String votOid = cambodia.getValuesOverTime(FastTestDataset.AT_National_Anthem.getAttributeName()).getValueOverTime(FastTestDataset.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE).getOid();

    JsonArray valuesOverTime = JsonParser.parseString("[" + "{" + "  \"oid\": \"" + votOid + "\"," + "  \"action\": \"DELETE\"" + "}," + "{" + "  \"action\": \"CREATE\"," + "  \"newValue\": \"" + NEW_ANTHEM + "\"," + "  \"newStartDate\": \"" + NEW_START_DATE + "\"," + "  \"newEndDate\": \"" + NEW_END_DATE + "\"" + "}," + "{" + "  \"action\": \"CREATE\"," + "  \"newValue\": \"" + NEW_ANTHEM + "\"," + "  \"newStartDate\": \"" + OLD_START_DATE + "\"," + "  \"newEndDate\": \"" + OLD_END_DATE + "\"" + "}" + "]").getAsJsonArray();

    diff.add("valuesOverTime", valuesOverTime);

    ( (UpdateAttributeActionBase) action ).setJson(diff.toString());
    action.addApprovalStatus(AllGovernanceStatus.ACCEPTED);
    action.setCreateActionDate(new Date());
    action.apply();

    cr.addAction(action).apply();

    String serializedCR = cr.toJSON().toString();

    return new String[] { serializedCR, votOid };
  }

  @Request
  private void testComplexUpdateGeoObjectCR_Verify(String[] data) throws Exception
  {
    final String oldOid = data[1];

    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    sdf.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    final Date newStartDate = sdf.parse(NEW_START_DATE);
    final Date newEndDate = sdf.parse(NEW_END_DATE);
    final Date oldStartDate = sdf.parse(OLD_START_DATE);
    final Date oldEndDate = sdf.parse(OLD_END_DATE);

    ChangeRequestQuery crq = new ChangeRequestQuery(new QueryFactory());

    Assert.assertEquals(1, crq.getCount());

    ChangeRequest cr = crq.getIterator().next();

    Assert.assertEquals(AllGovernanceStatus.ACCEPTED.name(), cr.getGovernanceStatus().name());

    AbstractAction action = cr.getAllAction().next();

    Assert.assertTrue(action instanceof UpdateAttributeAction);

    Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), cr.getGeoObjectCode());
    Assert.assertEquals(FastTestDataset.CAMBODIA.getGeoObjectType().getCode(), cr.getGeoObjectTypeCode());
    Assert.assertEquals(FastTestDataset.ORG_CGOV.getCode(), cr.getOrganizationCode());

    ServerGeoObjectIF cambodia = FastTestDataset.CAMBODIA.getServerObject();
    ValueOverTimeCollection votc = cambodia.getValuesOverTime(FastTestDataset.AT_National_Anthem.getAttributeName());

    Assert.assertEquals(2, votc.size());

    ValueOverTime vot1 = votc.get(0);

    Assert.assertNotNull(vot1.getOid());
    Assert.assertTrue(! ( vot1.getOid().equals(oldOid) ));
    Assert.assertEquals(NEW_ANTHEM, vot1.getValue());
    Assert.assertEquals(oldStartDate, vot1.getStartDate());
    Assert.assertEquals(oldEndDate, vot1.getEndDate());

    ValueOverTime vot2 = votc.get(1);

    Assert.assertNotNull(vot2.getOid());
    Assert.assertTrue(! ( vot2.getOid().equals(oldOid) ));
    Assert.assertEquals(NEW_ANTHEM, vot2.getValue());
    Assert.assertEquals(newStartDate, vot2.getStartDate());
    Assert.assertEquals(newEndDate, vot2.getEndDate());
  }

  /**
   * Update Geometry Test
   */
  @Test
  public void testUpdateGeoObjectGeometryCR() throws Exception
  {
    String[] data = testUpdateGeoObjectGeometryCR_applyCR();

    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA };

    for (TestUserInfo user : allowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {
          testUpdateGeoObjectGeometryCR(data, request);
        });
      }
      catch (SmartExceptionDTO e)
      {
        e.printStackTrace();
        Assert.fail("Unexpected exception was thrown on user [" + user.getUsername() + "].");
      }
    }
  }

  private void testUpdateGeoObjectGeometryCR(String[] data, ClientRequestIF request) throws Exception
  {
    changeService.implementDecisions(request.getSessionId(), data[0], null);

    testUpdateGeoObjectGeometryCR_Verify(data);
  }

  @Request
  private String[] testUpdateGeoObjectGeometryCR_applyCR() throws Exception
  {
    AttributeGeometryType geomType = FastTestDataset.CAMBODIA.fetchGeoObjectOverTime().getGeometryAttributeType();

    ChangeRequest cr = new ChangeRequest();
    cr.addApprovalStatus(AllGovernanceStatus.PENDING);
    cr.setOrganizationCode(FastTestDataset.ORG_CGOV.getCode());
    cr.setGeoObjectCode(FastTestDataset.CAMBODIA.getCode());
    cr.setGeoObjectTypeCode(FastTestDataset.CAMBODIA.getGeoObjectType().getCode());
    cr.apply();

    UpdateAttributeAction action = new UpdateAttributeAction();
    action.setApiVersion("1.0");
    ( (UpdateAttributeActionBase) action ).setAttributeName(geomType.getName());

    JsonObject diff = new JsonObject();

    VertexServerGeoObject cambodia = (VertexServerGeoObject) FastTestDataset.CAMBODIA.getServerObject();
    ValueOverTime vot = cambodia.getValuesOverTime(DefaultAttribute.GEOMETRY.getName()).getValueOverTime(FastTestDataset.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);

    GeoJsonWriter gw = new GeoJsonWriter();
    String json = gw.write((Geometry) vot.getValue());
    JsonObject geojson = JsonParser.parseString(json).getAsJsonObject();

    JsonArray valuesOverTime = JsonParser.parseString("[" + "{" + "  \"oid\": \"" + vot.getOid() + "\"," + "  \"action\": \"UPDATE\"," + "  \"oldEndDate\": \"" + OLD_END_DATE + "\"," + "  \"oldStartDate\": \"" + OLD_START_DATE + "\"," + "  \"oldValue\": \"\"," + "  \"newValue\": \"\"," + "  \"newStartDate\": \"" + NEW_START_DATE + "\"," + "  \"newEndDate\": \"" + NEW_END_DATE + "\"" + "}" + "]").getAsJsonArray();

    valuesOverTime.get(0).getAsJsonObject().add("oldValue", geojson);
    valuesOverTime.get(0).getAsJsonObject().add("newValue", geojson);

    diff.add("valuesOverTime", valuesOverTime);

    ( (UpdateAttributeActionBase) action ).setJson(diff.toString());
    action.addApprovalStatus(AllGovernanceStatus.ACCEPTED);
    action.setCreateActionDate(new Date());
    action.apply();

    cr.addAction(action).apply();

    String serializedCR = cr.toJSON().toString();

    return new String[] { serializedCR, vot.getOid() };
  }

  @Request
  private void testUpdateGeoObjectGeometryCR_Verify(String[] data) throws Exception
  {
    final String oldOid = data[1];

    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    sdf.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    final Date newStartDate = sdf.parse(NEW_START_DATE);
    final Date newEndDate = sdf.parse(NEW_END_DATE);

    ChangeRequestQuery crq = new ChangeRequestQuery(new QueryFactory());

    Assert.assertEquals(1, crq.getCount());

    ChangeRequest cr = crq.getIterator().next();

    Assert.assertEquals(AllGovernanceStatus.ACCEPTED.name(), cr.getGovernanceStatus().name());

    AbstractAction action = cr.getAllAction().next();

    Assert.assertTrue(action instanceof UpdateAttributeAction);

    Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), cr.getGeoObjectCode());
    Assert.assertEquals(FastTestDataset.CAMBODIA.getGeoObjectType().getCode(), cr.getGeoObjectTypeCode());
    Assert.assertEquals(FastTestDataset.ORG_CGOV.getCode(), cr.getOrganizationCode());

    VertexServerGeoObject cambodia = (VertexServerGeoObject) FastTestDataset.CAMBODIA.getServerObject();
    ValueOverTimeCollection votc = cambodia.getValuesOverTime(DefaultAttribute.GEOMETRY.getName());

    Assert.assertEquals(1, votc.size());

    ValueOverTime vot1 = votc.get(0);

    Assert.assertNotNull(vot1.getOid());
    Assert.assertEquals(oldOid, vot1.getOid());
    Assert.assertEquals(newStartDate, vot1.getStartDate());
    Assert.assertEquals(newEndDate, vot1.getEndDate());
    Assert.assertTrue(vot1.getValue() instanceof Geometry);
  }

  /**
   * Update Localized Value Test
   */
  @Test
  public void testUpdateGeoObjectLocalizedValueCR() throws Exception
  {
    String[] data = testUpdateGeoObjectLocalizedValueCR_applyCR();

    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA };

    for (TestUserInfo user : allowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {
          testUpdateGeoObjectLocalizedValueCR(data, request);
        });
      }
      catch (SmartExceptionDTO e)
      {
        e.printStackTrace();
        Assert.fail("Unexpected exception was thrown on user [" + user.getUsername() + "].");
      }
    }
  }

  private void testUpdateGeoObjectLocalizedValueCR(String[] data, ClientRequestIF request) throws Exception
  {
    changeService.implementDecisions(request.getSessionId(), data[0], null);

    testUpdateGeoObjectLocalizedValueCR_Verify(data);
  }

  @Request
  private String[] testUpdateGeoObjectLocalizedValueCR_applyCR() throws Exception
  {
    ChangeRequest cr = new ChangeRequest();
    cr.addApprovalStatus(AllGovernanceStatus.PENDING);
    cr.setOrganizationCode(FastTestDataset.ORG_CGOV.getCode());
    cr.setGeoObjectCode(FastTestDataset.CAMBODIA.getCode());
    cr.setGeoObjectTypeCode(FastTestDataset.CAMBODIA.getGeoObjectType().getCode());
    cr.apply();

    UpdateAttributeAction action = new UpdateAttributeAction();
    action.setApiVersion("1.0");
    ( (UpdateAttributeActionBase) action ).setAttributeName(DefaultAttribute.DISPLAY_LABEL.getName());

    JsonObject diff = new JsonObject();

    VertexServerGeoObject cambodia = (VertexServerGeoObject) FastTestDataset.CAMBODIA.getServerObject();
    ValueOverTime vot = cambodia.getValuesOverTime(DefaultAttribute.DISPLAY_LABEL.getName()).getValueOverTime(FastTestDataset.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);

    JsonArray valuesOverTime = JsonParser.parseString("[" + "{" + "  \"oid\": \"" + vot.getOid() + "\"," + "  \"action\": \"UPDATE\"," + "  \"oldEndDate\": \"" + OLD_END_DATE + "\"," + "  \"oldStartDate\": \"" + OLD_START_DATE + "\"," + "  \"oldValue\": \"\"," + "  \"newValue\": \"\"," + "  \"newStartDate\": \"" + NEW_START_DATE + "\"," + "  \"newEndDate\": \"" + NEW_END_DATE + "\"" + "}" + "]").getAsJsonArray();

    valuesOverTime.get(0).getAsJsonObject().add("oldValue", cambodia.getDisplayLabel(FastTestDataset.DEFAULT_OVER_TIME_DATE).toJSON());

    LocalizedValue newValue = new LocalizedValue("localizeTest");
    valuesOverTime.get(0).getAsJsonObject().add("newValue", newValue.toJSON());

    diff.add("valuesOverTime", valuesOverTime);

    ( (UpdateAttributeActionBase) action ).setJson(diff.toString());
    action.addApprovalStatus(AllGovernanceStatus.ACCEPTED);
    action.setCreateActionDate(new Date());
    action.apply();

    cr.addAction(action).apply();

    String serializedCR = cr.toJSON().toString();

    return new String[] { serializedCR, vot.getOid() };
  }

  @Request
  private void testUpdateGeoObjectLocalizedValueCR_Verify(String[] data) throws Exception
  {
    final String oldOid = data[1];

    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    sdf.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    final Date newStartDate = sdf.parse(NEW_START_DATE);
    final Date newEndDate = sdf.parse(NEW_END_DATE);

    ChangeRequestQuery crq = new ChangeRequestQuery(new QueryFactory());

    Assert.assertEquals(1, crq.getCount());

    ChangeRequest cr = crq.getIterator().next();

    Assert.assertEquals(AllGovernanceStatus.ACCEPTED.name(), cr.getGovernanceStatus().name());

    AbstractAction action = cr.getAllAction().next();

    Assert.assertTrue(action instanceof UpdateAttributeAction);

    Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), cr.getGeoObjectCode());
    Assert.assertEquals(FastTestDataset.CAMBODIA.getGeoObjectType().getCode(), cr.getGeoObjectTypeCode());
    Assert.assertEquals(FastTestDataset.ORG_CGOV.getCode(), cr.getOrganizationCode());

    VertexServerGeoObject cambodia = (VertexServerGeoObject) FastTestDataset.CAMBODIA.getServerObject();
    ValueOverTimeCollection votc = cambodia.getValuesOverTime(DefaultAttribute.DISPLAY_LABEL.getName());

    Assert.assertEquals(1, votc.size());

    ValueOverTime vot1 = votc.get(0);

    Assert.assertNotNull(vot1.getOid());
    Assert.assertEquals(oldOid, vot1.getOid());
    Assert.assertEquals(newStartDate, vot1.getStartDate());
    Assert.assertEquals(newEndDate, vot1.getEndDate());
    Assert.assertEquals("localizeTest", cambodia.getDisplayLabel(newStartDate).getValue());
  }

  /**
   * Update Term Attr Test
   */
  @Test
  public void testUpdateGeoObjectTermCR() throws Exception
  {
    String[] data = testUpdateGeoObjectTermCR_applyCR();

    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA };

    for (TestUserInfo user : allowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {
          testUpdateGeoObjectTermCR(data, request);
        });
      }
      catch (SmartExceptionDTO e)
      {
        e.printStackTrace();
        Assert.fail("Unexpected exception was thrown on user [" + user.getUsername() + "].");
      }
    }
  }

  private void testUpdateGeoObjectTermCR(String[] data, ClientRequestIF request) throws Exception
  {
    changeService.implementDecisions(request.getSessionId(), data[0], null);

    testUpdateGeoObjectTermCR_Verify(data);
  }

  @Request
  private String[] testUpdateGeoObjectTermCR_applyCR() throws Exception
  {
    final String attrName = FastTestDataset.AT_RELIGION.getAttributeName();

    ChangeRequest cr = new ChangeRequest();
    cr.addApprovalStatus(AllGovernanceStatus.PENDING);
    cr.setOrganizationCode(FastTestDataset.ORG_CGOV.getCode());
    cr.setGeoObjectCode(FastTestDataset.CAMBODIA.getCode());
    cr.setGeoObjectTypeCode(FastTestDataset.CAMBODIA.getGeoObjectType().getCode());
    cr.apply();

    UpdateAttributeAction action = new UpdateAttributeAction();
    action.setApiVersion("1.0");
    ( (UpdateAttributeActionBase) action ).setAttributeName(attrName);

    JsonObject diff = new JsonObject();

    VertexServerGeoObject cambodia = (VertexServerGeoObject) FastTestDataset.CAMBODIA.getServerObject();
    ValueOverTime vot = cambodia.getValuesOverTime(attrName).getValueOverTime(FastTestDataset.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);

    JsonArray valuesOverTime = JsonParser.parseString("[" + "{" + "  \"oid\": \"" + vot.getOid() + "\"," + "  \"action\": \"UPDATE\"," + "  \"oldEndDate\": \"" + OLD_END_DATE + "\"," + "  \"oldStartDate\": \"" + OLD_START_DATE + "\"," + "  \"oldValue\": [\"" + ( (Term) FastTestDataset.CAMBODIA.getDefaultValue(attrName) ).getCode() + "\"]," + "  \"newValue\": [\"" + FastTestDataset.T_Islam.getCode() + "\"]," + "  \"newStartDate\": \"" + NEW_START_DATE + "\"," + "  \"newEndDate\": \"" + NEW_END_DATE + "\"" + "}" + "]").getAsJsonArray();

    diff.add("valuesOverTime", valuesOverTime);

    ( (UpdateAttributeActionBase) action ).setJson(diff.toString());
    action.addApprovalStatus(AllGovernanceStatus.ACCEPTED);
    action.setCreateActionDate(new Date());
    action.apply();

    cr.addAction(action).apply();

    String serializedCR = cr.toJSON().toString();

    return new String[] { serializedCR, vot.getOid() };
  }

  @Request
  private void testUpdateGeoObjectTermCR_Verify(String[] data) throws Exception
  {
    final String attrName = FastTestDataset.AT_RELIGION.getAttributeName();
    final String oldOid = data[1];

    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    sdf.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    final Date newStartDate = sdf.parse(NEW_START_DATE);
    final Date newEndDate = sdf.parse(NEW_END_DATE);

    ChangeRequestQuery crq = new ChangeRequestQuery(new QueryFactory());

    Assert.assertEquals(1, crq.getCount());

    ChangeRequest cr = crq.getIterator().next();

    Assert.assertEquals(AllGovernanceStatus.ACCEPTED.name(), cr.getGovernanceStatus().name());

    AbstractAction action = cr.getAllAction().next();

    Assert.assertTrue(action instanceof UpdateAttributeAction);

    Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), cr.getGeoObjectCode());
    Assert.assertEquals(FastTestDataset.CAMBODIA.getGeoObjectType().getCode(), cr.getGeoObjectTypeCode());
    Assert.assertEquals(FastTestDataset.ORG_CGOV.getCode(), cr.getOrganizationCode());

    VertexServerGeoObject cambodia = (VertexServerGeoObject) FastTestDataset.CAMBODIA.getServerObject();
    ValueOverTimeCollection votc = cambodia.getValuesOverTime(attrName);

    Assert.assertEquals(1, votc.size());

    ValueOverTime vot1 = votc.get(0);

    Assert.assertNotNull(vot1.getOid());
    Assert.assertEquals(oldOid, vot1.getOid());
    Assert.assertEquals(newStartDate, vot1.getStartDate());
    Assert.assertEquals(newEndDate, vot1.getEndDate());

    String classyId = ( (Classifier) cambodia.getValue(attrName, newStartDate) ).getOid();
    Assert.assertEquals(FastTestDataset.T_Islam.fetchClassifier().getOid(), classyId);

    Assert.assertEquals(classyId, vot1.getValue());
  }

  /**
   * Update Date Attr Test
   */
  @Test
  public void testUpdateGeoObjectDateCR() throws Exception
  {
    Object[] data = testUpdateGeoObjectDateCR_applyCR();

    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA };

    for (TestUserInfo user : allowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {
          testUpdateGeoObjectDateCR(data, request);
        });
      }
      catch (SmartExceptionDTO e)
      {
        e.printStackTrace();
        Assert.fail("Unexpected exception was thrown on user [" + user.getUsername() + "].");
      }
    }
  }

  private void testUpdateGeoObjectDateCR(Object[] data, ClientRequestIF request) throws Exception
  {
    changeService.implementDecisions(request.getSessionId(), (String) data[0], null);

    testUpdateGeoObjectDateCR_Verify(data);
  }

  @Request
  private Object[] testUpdateGeoObjectDateCR_applyCR() throws Exception
  {
    final Long newValue = new Date().getTime();
    final String attrName = FastTestDataset.AT_DATE_OF_FORMATION.getAttributeName();

    ChangeRequest cr = new ChangeRequest();
    cr.addApprovalStatus(AllGovernanceStatus.PENDING);
    cr.setOrganizationCode(FastTestDataset.ORG_CGOV.getCode());
    cr.setGeoObjectCode(FastTestDataset.CAMBODIA.getCode());
    cr.setGeoObjectTypeCode(FastTestDataset.CAMBODIA.getGeoObjectType().getCode());
    cr.apply();

    UpdateAttributeAction action = new UpdateAttributeAction();
    action.setApiVersion("1.0");
    ( (UpdateAttributeActionBase) action ).setAttributeName(attrName);

    JsonObject diff = new JsonObject();

    VertexServerGeoObject cambodia = (VertexServerGeoObject) FastTestDataset.CAMBODIA.getServerObject();
    ValueOverTime vot = cambodia.getValuesOverTime(attrName).getValueOverTime(FastTestDataset.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);

    JsonArray valuesOverTime = JsonParser.parseString("[" + "{" + "  \"oid\": \"" + vot.getOid() + "\"," + "  \"action\": \"UPDATE\"," + "  \"oldEndDate\": \"" + OLD_END_DATE + "\"," + "  \"oldStartDate\": \"" + OLD_START_DATE + "\"," + "  \"oldValue\": \"\"," + "  \"newValue\": \"\"," + "  \"newStartDate\": \"" + NEW_START_DATE + "\"," + "  \"newEndDate\": \"" + NEW_END_DATE + "\"" + "}" + "]").getAsJsonArray();

    valuesOverTime.get(0).getAsJsonObject().addProperty("oldValue", ( (Date) FastTestDataset.CAMBODIA.getDefaultValue(attrName) ).getTime());

    valuesOverTime.get(0).getAsJsonObject().addProperty("newValue", newValue);

    diff.add("valuesOverTime", valuesOverTime);

    ( (UpdateAttributeActionBase) action ).setJson(diff.toString());
    action.addApprovalStatus(AllGovernanceStatus.ACCEPTED);
    action.setCreateActionDate(new Date());
    action.apply();

    cr.addAction(action).apply();

    String serializedCR = cr.toJSON().toString();

    return new Object[] { serializedCR, vot.getOid(), newValue };
  }

  @Request
  private void testUpdateGeoObjectDateCR_Verify(Object[] data) throws Exception
  {
    final String attrName = FastTestDataset.AT_DATE_OF_FORMATION.getAttributeName();
    final String oldOid = (String) data[1];

    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    sdf.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    final Date newStartDate = sdf.parse(NEW_START_DATE);
    final Date newEndDate = sdf.parse(NEW_END_DATE);

    ChangeRequestQuery crq = new ChangeRequestQuery(new QueryFactory());

    Assert.assertEquals(1, crq.getCount());

    ChangeRequest cr = crq.getIterator().next();

    Assert.assertEquals(AllGovernanceStatus.ACCEPTED.name(), cr.getGovernanceStatus().name());

    AbstractAction action = cr.getAllAction().next();

    Assert.assertTrue(action instanceof UpdateAttributeAction);

    Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), cr.getGeoObjectCode());
    Assert.assertEquals(FastTestDataset.CAMBODIA.getGeoObjectType().getCode(), cr.getGeoObjectTypeCode());
    Assert.assertEquals(FastTestDataset.ORG_CGOV.getCode(), cr.getOrganizationCode());

    VertexServerGeoObject cambodia = (VertexServerGeoObject) FastTestDataset.CAMBODIA.getServerObject();
    ValueOverTimeCollection votc = cambodia.getValuesOverTime(attrName);

    Assert.assertEquals(1, votc.size());

    ValueOverTime vot1 = votc.get(0);

    Assert.assertNotNull(vot1.getOid());
    Assert.assertEquals(oldOid, vot1.getOid());
    Assert.assertEquals(newStartDate, vot1.getStartDate());
    Assert.assertEquals(newEndDate, vot1.getEndDate());

    Calendar expected = Calendar.getInstance();
    expected.setTime(new Date((Long) data[2]));
    expected.set(Calendar.SECOND, 0); // It's surprising how imprecise Runway's
                                      // dates are...
    expected.set(Calendar.MILLISECOND, 0);

    Calendar actual = Calendar.getInstance();
    actual.setTime((Date) cambodia.getValue(attrName));
    actual.set(Calendar.SECOND, 0);
    actual.set(Calendar.MILLISECOND, 0);

    Assert.assertEquals(expected.getTime(), actual.getTime());
  }
}
