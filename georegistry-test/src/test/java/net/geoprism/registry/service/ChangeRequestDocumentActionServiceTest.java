/**
 *
 */
package net.geoprism.registry.service;

import java.io.IOException;
import java.util.Date;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.VaultFile;

import net.geoprism.registry.CGRPermissionException;
import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.action.AbstractAction;
import net.geoprism.registry.action.AllGovernanceStatus;
import net.geoprism.registry.action.ChangeRequest;
import net.geoprism.registry.action.geoobject.CreateGeoObjectAction;
import net.geoprism.registry.service.request.ChangeRequestService;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestUserInfo;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class ChangeRequestDocumentActionServiceTest extends FastDatasetTest implements InstanceTestClassListener
{
  private AbstractAction       action;

  private ChangeRequest        cr;

  private String               crOid;

  @Autowired
  private ChangeRequestService crService;

  @Before
  public void setUp()
  {
    testData.setUpInstanceData();

    // TestDataSet.populateAdapterIds(null, testData.adapter);

    createTestChangeRequest();
  }

  @After
  public void tearDown()
  {
    testData.tearDownInstanceData();

    TestDataSet.deleteAllChangeRequests();
    TestDataSet.deleteAllVaultFiles();
  }

  @Request
  public void createTestChangeRequest()
  {
    createCRTrans();
  }

  @Transaction
  private void createCRTrans()
  {
    ChangeRequest cr = new ChangeRequest();
    cr.addApprovalStatus(AllGovernanceStatus.PENDING);
    cr.setGeoObjectCode(FastTestDataset.CAMBODIA.getCode());
    cr.setGeoObjectTypeCode(FastTestDataset.COUNTRY.getCode());
    cr.setOrganizationCode(FastTestDataset.ORG_CGOV.getCode());
    cr.apply();

    CreateGeoObjectAction action = new CreateGeoObjectAction();
    action.setApiVersion("1.0");
    action.setGeoObjectJson(FastTestDataset.CAMBODIA.fetchGeoObjectOverTime().toJSON().toString());
    action.addApprovalStatus(AllGovernanceStatus.PENDING);
    action.setCreateActionDate(new Date());
    action.apply();

    cr.addAction(action).apply();

    this.cr = cr;
    this.action = action;
    this.crOid = cr.getOid();
  }

  @Test
  public void testDeleteDocument()
  {
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_ADMIN, FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };

    for (TestUserInfo user : allowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {
          testDeleteDocumentAsUser(request);
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
        FastTestDataset.runAsUser(user, (request) -> {
          testDeleteDocumentAsUser(request);

          Assert.fail("Expected a permission exception.");
        });
      }
      catch (SmartExceptionDTO e)
      {
        Assert.assertEquals(CGRPermissionException.CLASS, e.getType());
        // Expected
      }
    }
  }

  private void testDeleteDocumentAsUser(ClientRequestIF request)
  {
    String sJson = crService.uploadFileCR(request.getSessionId(), crOid, "parent-test.xlsx", ChangeRequestDocumentActionServiceTest.class.getResourceAsStream("/parent-test.xlsx"));

    JsonObject jsonVF = JsonParser.parseString(sJson).getAsJsonObject();

    final String vfOid = jsonVF.get("oid").getAsString();

    crService.deleteDocumentCR(request.getSessionId(), crOid, vfOid);

    assertVaultFileDeleted(vfOid);
  }

  @Request
  private void assertVaultFileDeleted(String vfOid)
  {
    try
    {
      VaultFile.get(vfOid);

      Assert.fail("Vault file was not deleted.");
      Assert.assertEquals(0, action.getAllDocument().getAll().size());
    }
    catch (DataNotFoundException ex)
    {
      // Expected
    }
  }

  @Test
  public void testListDocuments()
  {
    uploadDocumentsAsAdmin();

    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_ADMIN, FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };

    for (TestUserInfo user : allowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {
          testListDocumentsAsUser(request);
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
        FastTestDataset.runAsUser(user, (request) -> {
          testListDocumentsAsUser(request);

          Assert.fail("Expected a permission exception.");
        });
      }
      catch (SmartExceptionDTO e)
      {
        Assert.assertEquals(CGRPermissionException.CLASS, e.getType());
        // Expected
      }
    }
  }

  public void testListDocumentsAsUser(ClientRequestIF request)
  {
    JsonArray ja = JsonParser.parseString(crService.listDocumentsCR(request.getSessionId(), crOid)).getAsJsonArray();

    Assert.assertEquals(2, ja.size());

    for (int i = 0; i < ja.size(); ++i)
    {
      JsonObject jo = ja.get(i).getAsJsonObject();

      Assert.assertEquals("parent-test.xlsx", jo.get("fileName").getAsString());
    }
  }

  @Request
  private String uploadDocumentsAsAdmin()
  {
    String json = null;

    for (int i = 0; i < 2; ++i)
    {
      json = crService.uploadFileInTransactionCR(crOid, "parent-test.xlsx", ChangeRequestDocumentActionServiceTest.class.getResourceAsStream("/parent-test.xlsx"));
    }

    JsonObject jsonVF = JsonParser.parseString(json).getAsJsonObject();

    Assert.assertEquals("parent-test.xlsx", jsonVF.get("fileName").getAsString());

    final String vfOid = jsonVF.get("oid").getAsString();

    return vfOid;
  }

  @Test
  public void testUploadDocument()
  {
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_ADMIN, FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };

    for (TestUserInfo user : allowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {
          crService.uploadFileCR(request.getSessionId(), crOid, "parent-test.xlsx", ChangeRequestDocumentActionServiceTest.class.getResourceAsStream("/parent-test.xlsx"));

          TestDataSet.deleteAllVaultFiles();
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
        FastTestDataset.runAsUser(user, (request) -> {
          crService.uploadFileCR(request.getSessionId(), crOid, "parent-test.xlsx", ChangeRequestDocumentActionServiceTest.class.getResourceAsStream("/parent-test.xlsx"));

          Assert.fail("Expected a permission exception.");
        });
      }
      catch (SmartExceptionDTO e)
      {
        Assert.assertEquals(CGRPermissionException.CLASS, e.getType());
        // Expected
      }

      TestDataSet.deleteAllVaultFiles();
    }
  }

  @Test
  public void testDownloadDocument() throws IOException
  {
    String vfOid = uploadDocumentsAsAdmin();

    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_ADMIN, FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };

    for (TestUserInfo user : allowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {
          downloadDocumentAsUser(request, vfOid);
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
        FastTestDataset.runAsUser(user, (request) -> {
          downloadDocumentAsUser(request, vfOid);

          Assert.fail("Expected a permission exception.");
        });
      }
      catch (SmartExceptionDTO e)
      {
        Assert.assertEquals(CGRPermissionException.CLASS, e.getType());
        // Expected
      }

      TestDataSet.deleteAllVaultFiles();
    }
  }

  private void downloadDocumentAsUser(ClientRequestIF request, String vfOid)
  {
    try (ApplicationResource res = crService.downloadDocumentCR(request.getSessionId(), crOid, vfOid))
    {
      Assert.assertEquals("parent-test.xlsx", res.getName());
      Assert.assertTrue(res.exists());
    }
  }
}
