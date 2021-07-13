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
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.action.AbstractAction;
import net.geoprism.registry.action.AllGovernanceStatus;
import net.geoprism.registry.action.ChangeRequest;
import net.geoprism.registry.action.geoobject.CreateGeoObjectAction;
import net.geoprism.registry.action.geoobject.CreateGeoObjectActionBase;
import net.geoprism.registry.action.geoobject.UpdateAttributeAction;
import net.geoprism.registry.action.geoobject.UpdateAttributeActionBase;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.roles.CreateGeoObjectTypePermissionException;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestUserInfo;
import net.geoprism.registry.view.Page;

public class ChangeRequestServiceTest
{
  protected static FastTestDataset    testData;
  
  private static final String NEW_ANTHEM = "NEW_ANTHEM";
  
  private String UPDATE_ATTR_JSON = null;
  
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
    
    ServerGeoObjectIF cambodia = FastTestDataset.CAMBODIA.getServerObject();
    String votOid = cambodia.getValuesOverTime(FastTestDataset.AT_National_Anthem.getAttributeName()).getValueOverTime(FastTestDataset.DEFAULT_OVER_TIME_DATE, ValueOverTime.INFINITY_END_DATE).getOid();
    
    UPDATE_ATTR_JSON = "{"
    + "\"valuesOverTime\" : ["
    +     "{"
    +         "\"oid\" : \"" + votOid + "\","
    +         "\"action\" : \"UPDATE\","
    +         "\"oldValue\" : \"" + FastTestDataset.CAMBODIA.getDefaultValue(FastTestDataset.AT_National_Anthem.getAttributeName()) + "\","
    +         "\"newValue\" : \"" + NEW_ANTHEM + "\","
    +         "\"newStartDate\" : \"2020-05-04\","
    +         "\"newEndDate\" : \"2021-05-04\","
    +         "\"oldStartDate\" : \"2020-04-04\","
    +         "\"oldEndDate\" : \"2021-04-04\""
    +     "}"
    + "]"
    + "}";
  }

  @After
  public void tearDown()
  {
    testData.tearDownInstanceData();
    
    TestDataSet.deleteAllChangeRequests();
  }
  
  @Request
  public String createTestChangeRequest(boolean isCreate)
  {
    return createCRTrans(isCreate);
  }
  @Transaction
  private String createCRTrans(boolean isCreate)
  {
    ChangeRequest cr = new ChangeRequest();
    cr.addApprovalStatus(AllGovernanceStatus.PENDING);
    cr.setGeoObjectCode(FastTestDataset.CAMBODIA.getCode());
    cr.setGeoObjectTypeCode(FastTestDataset.CAMBODIA.getGeoObjectType().getCode());
    cr.setOrganizationCode(FastTestDataset.ORG_CGOV.getCode());
    cr.apply();
    
    AbstractAction action;
    
    if (isCreate)
    {
      action = new CreateGeoObjectAction();
      action.setApiVersion("1.0");
      ( (CreateGeoObjectActionBase) action ).setGeoObjectJson(FastTestDataset.CAMBODIA.fetchGeoObjectOverTime().toJSON().toString());
      action.addApprovalStatus(AllGovernanceStatus.PENDING);
      action.setCreateActionDate(new Date());
      action.apply();
    }
    else
    {
      action = new UpdateAttributeAction();
      action.setApiVersion("1.0");
      ( (UpdateAttributeActionBase) action ).setAttributeName(FastTestDataset.AT_National_Anthem.getAttributeName());
      ( (UpdateAttributeActionBase) action ).setJson(UPDATE_ATTR_JSON);
      action.addApprovalStatus(AllGovernanceStatus.PENDING);
      action.setCreateActionDate(new Date());
      action.apply();
    }
    
    cr.addAction(action).apply();
    
    return cr.getOid();
  }
  
  @Test
  public void testGetAllCR()
  {
    createTestChangeRequest(true);
    createTestChangeRequest(false);
    
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
    
    System.out.println(joPage);
    
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
}
