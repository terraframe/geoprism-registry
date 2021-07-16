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
package net.geoprism.registry.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTimeCollection;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.CGRPermissionException;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.action.AbstractAction;
import net.geoprism.registry.action.AllGovernanceStatus;
import net.geoprism.registry.action.ChangeRequest;
import net.geoprism.registry.action.geoobject.CreateGeoObjectAction;
import net.geoprism.registry.action.geoobject.CreateGeoObjectActionBase;
import net.geoprism.registry.action.geoobject.UpdateAttributeAction;
import net.geoprism.registry.action.geoobject.UpdateAttributeActionBase;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestUserInfo;
import net.geoprism.registry.view.Page;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;
import net.geoprism.registry.view.action.UpdateAttributeViewJsonAdapters;
import net.geoprism.registry.view.action.UpdateParentValueOverTimeView;

public class ChangeRequestServiceTest
{
  public static final TestGeoObjectInfo BELIZE = new TestGeoObjectInfo("Belize", FastTestDataset.COUNTRY);
  
  protected static FastTestDataset    testData;
  
  private static final String NEW_ANTHEM = "NEW_ANTHEM";
  
  private static final String NEW_START_DATE = "2020-05-04";
  
  private static final String NEW_END_DATE = "2021-05-04";
  
  private String UPDATE_ATTR_JSON = null;
  
  private String UPDATE_PARENT_ATTR_JSON = null;
  
  @BeforeClass
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();
    testData.setUpMetadata();
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
    
//    TestDataSet.populateAdapterIds(null, testData.adapter);
    
    BELIZE.apply();
    
    ServerGeoObjectIF cambodia = FastTestDataset.CAMBODIA.getServerObject();
    String votOid = cambodia.getValuesOverTime(FastTestDataset.AT_National_Anthem.getAttributeName()).getValueOverTime(FastTestDataset.DEFAULT_OVER_TIME_DATE, ValueOverTime.INFINITY_END_DATE).getOid();
    
    UPDATE_ATTR_JSON = "{"
    + "\"valuesOverTime\" : ["
    +     "{"
    +         "\"oid\" : \"" + votOid + "\","
    +         "\"action\" : \"UPDATE\","
    +         "\"oldValue\" : \"" + FastTestDataset.CAMBODIA.getDefaultValue(FastTestDataset.AT_National_Anthem.getAttributeName()) + "\","
    +         "\"newValue\" : \"" + NEW_ANTHEM + "\","
    +         "\"newStartDate\" : \"" + NEW_START_DATE + "\","
    +         "\"newEndDate\" : \"" + NEW_END_DATE + "\","
    +         "\"oldStartDate\" : \"2020-04-04\","
    +         "\"oldEndDate\" : \"2021-04-04\""
    +     "}"
    + "]"
    + "}";
    
    ServerGeoObjectIF central_prov = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerParentTreeNode ptn = central_prov.getParentGeoObjects(new String[] {FastTestDataset.COUNTRY.getCode()}, false).getParents().get(0);
    
    UPDATE_PARENT_ATTR_JSON = "{"
        + "\"hierarchyCode\" : \"" + FastTestDataset.HIER_ADMIN.getCode() + "\","
        + "\"valuesOverTime\" : ["
        +     "{"
        +         "\"oid\" : \"" + ptn.getOid() + "\","
        +         "\"action\" : \"UPDATE\","
        +         "\"oldValue\" : \"" + FastTestDataset.COUNTRY.getCode() + UpdateParentValueOverTimeView.VALUE_SPLIT_TOKEN + FastTestDataset.CAMBODIA.getCode() + "\","
        +         "\"newValue\" : \"" + FastTestDataset.COUNTRY.getCode() + UpdateParentValueOverTimeView.VALUE_SPLIT_TOKEN + BELIZE.getCode() + "\","
        +         "\"newStartDate\" : \"" + NEW_START_DATE + "\","
        +         "\"newEndDate\" : \"" + NEW_END_DATE + "\","
        +         "\"oldStartDate\" : \"2020-04-04\","
        +         "\"oldEndDate\" : \"2021-04-04\""
        +     "}"
        + "]"
        + "}";
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
    
    return cr.getOid();
  }
  
  @Test
  public void testGetAllCR()
  {
    createTestChangeRequest(CreateGeoObjectAction.CLASS);
    createTestChangeRequest(UpdateAttributeAction.CLASS);
    
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_ADMIN, FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };

    for (TestUserInfo user : allowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request, adapter) -> {
          testGetAllCR(request, true);
        });
      }
      catch (SmartExceptionDTO e)
      {
        e.printStackTrace();
        Assert.fail("Unexpected permission exception was thrown on user [" + user.getUsername() + "].");
      }
    }
    
    TestUserInfo[] disAllowedUsers = new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC };

    for (TestUserInfo user : disAllowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request, adapter) -> {
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
    ChangeRequestService service = new ChangeRequestService();
    
    Page<ChangeRequest> page = service.getAllRequests(request.getSessionId(), 10, 1, "");
    
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
        
//        JsonObject geoObject = current.get("geoObject").getAsJsonObject();
//        JsonObject geoObjectType = current.get("geoObjectType").getAsJsonObject();
//        
//        Assert.assertNotNull(geoObject);
//        Assert.assertNotNull(geoObjectType);
        
//        Assert.assertEquals(FastTestDataset.CAMBODIA.getDisplayLabel(), geoObject.get("label").getAsString());
//        Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), geoObject.get("code").getAsString());
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
    String crOid = createTestChangeRequest(UpdateAttributeAction.CLASS);
    
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_ADMIN, FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM };

    for (TestUserInfo user : allowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request, adapter) -> {
          testSetActionStatus(request, crOid);
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
        FastTestDataset.runAsUser(user, (request, adapter) -> {
          testSetActionStatus(request, crOid);
          
          Assert.fail("Expected a permission exception to be thrown on user [" + user.getUsername() + "].");
        });
      }
      catch (SmartExceptionDTO e)
      {
        Assert.assertEquals(e.getType(), CGRPermissionException.CLASS);
      }
    }
  }
  
  private void testSetActionStatus(ClientRequestIF request, String crOid)
  {
    ChangeRequestService service = new ChangeRequestService();
    
    service.setActionStatus(request.getSessionId(), testSetActionStatusGetCRAction(crOid), AllGovernanceStatus.ACCEPTED.name());
    
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
        FastTestDataset.runAsUser(user, (request, adapter) -> {
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
        FastTestDataset.runAsUser(user, (request, adapter) -> {
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
  
  private void testImplementDecisions(ClientRequestIF request, String crOid) throws Exception
  {
    ChangeRequestService service = new ChangeRequestService();
    
    testSetActionStatus(request, crOid);
    
    service.implementDecisions(request.getSessionId(), crOid);
    
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
        FastTestDataset.runAsUser(user, (request, adapter) -> {
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
        FastTestDataset.runAsUser(user, (request, adapter) -> {
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
  
  private void testImplementParentDecisions(ClientRequestIF request, String crOid) throws Exception
  {
    ChangeRequestService service = new ChangeRequestService();
    
    testSetActionStatus(request, crOid);
    
    service.implementDecisions(request.getSessionId(), crOid);
    
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
    
    List<ServerParentTreeNode> ptns = serverGo.getParentsOverTime(new String[] {FastTestDataset.COUNTRY.getCode()}, false).getEntries(FastTestDataset.HIER_ADMIN.getServerObject());
    Assert.assertEquals(1, ptns.size());
    
    ServerParentTreeNode ptn = ptns.get(0);
    
    List<ServerParentTreeNode> parents = ptn.getParents();
    Assert.assertEquals(1, parents.size());
    
    ServerParentTreeNode parent = parents.get(0);
    
    Assert.assertEquals(newStartDate, ptn.getStartDate());
    Assert.assertEquals(newEndDate, ptn.getEndDate());
    Assert.assertEquals(BELIZE.getCode(), parent.getGeoObject().getCode());
  }
}
