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

import java.io.IOException;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.VaultFile;

import junit.framework.Assert;
import net.geoprism.registry.action.AllGovernanceStatus;
import net.geoprism.registry.action.ChangeRequest;

public class ChangeRequestServiceTest
{
//  protected static FastTestDataset    testData;
//  
//  @BeforeClass
//  public static void setUpClass()
//  {
//    testData = FastTestDataset.newTestData();
//    testData.setUpMetadata();
//  }
//
//  @AfterClass
//  public static void cleanUpClass()
//  {
//    testData.tearDownMetadata();
//  }
//
//  @Before
//  public void setUp()
//  {
//    testData.setUpInstanceData();
//  }
//
//  @After
//  public void tearDown()
//  {
//    testData.tearDownInstanceData();
//  }
  
  @Test
  @Request
  public void testDeleteDocument()
  {
    ChangeRequestService service = new ChangeRequestService();
    
    ChangeRequest cr = new ChangeRequest();
    cr.addApprovalStatus(AllGovernanceStatus.PENDING);
    cr.apply();
    
    try
    {
      String vfOid = service.uploadFileInTransaction(cr.getOid(), "parent-test.xlsx", ChangeRequestServiceTest.class.getResourceAsStream("/parent-test.xlsx"));
      
      service.deleteDocument(cr.getOid(), vfOid);
      
      try
      {
        VaultFile.get(vfOid);
        
        Assert.fail("Vault file was not deleted.");
        Assert.assertEquals(0, cr.getAllDocument().getAll().size());
      }
      catch (DataNotFoundException ex)
      {
        // Expected
      }
    }
    finally
    {
      cr.delete();
    }
  }
  
  @Test
  @Request
  public void testListDocuments()
  {
    ChangeRequestService service = new ChangeRequestService();
    
    ChangeRequest cr = new ChangeRequest();
    cr.addApprovalStatus(AllGovernanceStatus.PENDING);
    cr.apply();
    
    try
    {
      service.uploadFileInTransaction(cr.getOid(), "parent-test.xlsx", ChangeRequestServiceTest.class.getResourceAsStream("/parent-test.xlsx"));
      service.uploadFileInTransaction(cr.getOid(), "parent-test.xlsx", ChangeRequestServiceTest.class.getResourceAsStream("/parent-test.xlsx"));
      
      JsonArray ja = JsonParser.parseString(service.listDocuments(cr.getOid())).getAsJsonArray();
      
      Assert.assertEquals(2, ja.size());
      
      for (int i = 0; i < ja.size(); ++i)
      {
        JsonObject jo = ja.get(i).getAsJsonObject();
        
        Assert.assertEquals("parent-test.xlsx", jo.get("fileName").getAsString());
        
        Assert.assertEquals("parent-test.xlsx", VaultFile.get(jo.get("oid").getAsString()).getName());
        VaultFile.get(jo.get("oid").getAsString()).delete();
      }
    }
    finally
    {
      cr.delete();
    }
  }
  
  @Test
  @Request
  public void testUploadDocument()
  {
    ChangeRequest cr = new ChangeRequest();
    cr.addApprovalStatus(AllGovernanceStatus.PENDING);
    cr.apply();
    
    try
    {
      new ChangeRequestService().uploadFileInTransaction(cr.getOid(), "parent-test.xlsx", ChangeRequestServiceTest.class.getResourceAsStream("/parent-test.xlsx"));
      
      
    }
    finally
    {
      cr.delete();
    }
  }
  
  @Test
  @Request
  public void testDownloadDocument() throws IOException
  {
    ChangeRequestService service = new ChangeRequestService();
    
    ChangeRequest cr = new ChangeRequest();
    cr.addApprovalStatus(AllGovernanceStatus.PENDING);
    cr.apply();
    
    try
    {
      String vfOid = service.uploadFileInTransaction(cr.getOid(), "parent-test.xlsx", ChangeRequestServiceTest.class.getResourceAsStream("/parent-test.xlsx"));
      
      try (ApplicationResource res = service.downloadDocument(cr.getOid(), vfOid))
      {
        Assert.assertEquals("parent-test.xlsx", res.getName());
        Assert.assertNotNull(res.getUnderlyingFile());
        res.delete();
      }
    }
    finally
    {
      cr.delete();
    }
  }
}
