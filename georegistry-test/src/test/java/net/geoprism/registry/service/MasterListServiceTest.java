package net.geoprism.registry.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.constants.ComponentInfo;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.SessionFacade;

import net.geoprism.registry.MasterList;
import net.geoprism.registry.test.TestDataSet.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.USATestData;

public class MasterListServiceTest
{
  private static USATestData       testData;

  private static AttributeTermType testTerm;

  private static ClientRequestIF   adminCR;

  @BeforeClass
  public static void setUp()
  {
    testData = USATestData.newTestData(GeometryType.POLYGON, true);

    adminCR = testData.adminClientRequest;

    testTerm = (AttributeTermType) AttributeType.factory("testTerm", new LocalizedValue("testTermLocalName"), new LocalizedValue("testTermLocalDescrip"), AttributeTermType.TYPE, false, false);
    testTerm = (AttributeTermType) ServiceFactory.getRegistryService().createAttributeType(adminCR.getSessionId(), testData.STATE.getCode(), testTerm.toJSON().toString());

    reload();
  }

  @Request
  public static void reload()
  {
    /*
     * Reload permissions for the new attributes
     */
    SessionFacade.getSessionForRequest(adminCR.getSessionId()).reloadPermissions();
  }

  @AfterClass
  public static void tearDown() throws IOException
  {
    testData.cleanUp();
  }

  @Test
  @Request
  public void testSerialization()
  {
    MasterList list = new MasterList();
    list.setUniversal(testData.STATE.getUniversal());
    list.getDisplayLabel().setValue("Test List");
    list.setCode("TEST_CODE");
    list.setRepresentativityDate(new Date());
    list.setPublishDate(new Date());
    list.setListAbstract("My Abstract");
    list.setProcess("Process");
    list.setProgress("Progress");
    list.setAccessConstraints("Access Contraints");
    list.setUseConstraints("User Constraints");
    list.setAcknowledgements("Acknowledgements");
    list.setDisclaimer("Disclaimer");
    list.setContactName("Contact Name");
    list.setOrganization("Organization");
    list.setTelephoneNumber("Telephone Number");
    list.setEmail("Email");

    JsonObject json = list.toJSON();
    MasterList test = MasterList.fromJSON(json);

    Assert.assertEquals(list.getUniversalOid(), test.getUniversalOid());
    Assert.assertEquals(list.getDisplayLabel().getValue(), test.getDisplayLabel().getValue());
    Assert.assertEquals(list.getRepresentativityDate(), test.getRepresentativityDate());
    Assert.assertEquals(list.getPublishDate(), test.getPublishDate());
    Assert.assertEquals(list.getListAbstract(), test.getListAbstract());
    Assert.assertEquals(list.getProcess(), test.getProcess());
    Assert.assertEquals(list.getProgress(), test.getProgress());
    Assert.assertEquals(list.getAccessConstraints(), test.getAccessConstraints());
    Assert.assertEquals(list.getUseConstraints(), test.getUseConstraints());
    Assert.assertEquals(list.getAcknowledgements(), test.getAcknowledgements());
    Assert.assertEquals(list.getDisclaimer(), test.getDisclaimer());
    Assert.assertEquals(list.getContactName(), test.getContactName());
    Assert.assertEquals(list.getOrganization(), test.getOrganization());
    Assert.assertEquals(list.getTelephoneNumber(), test.getTelephoneNumber());
    Assert.assertEquals(list.getEmail(), test.getEmail());
    Assert.assertEquals(list.getCode(), test.getCode());
    Assert.assertEquals(list.getHierarchiesAsJson().toString(), test.getHierarchiesAsJson().toString());
  }

  @Test
  @Request
  public void testCreateEntity() throws SQLException
  {
    JsonObject json = getJson(testData.STATE);

    MasterList test = MasterList.create(json);

    try
    {
      MdBusinessDAOIF mdTable = MdBusinessDAO.get(test.getMdBusinessOid());

      Assert.assertNotNull(mdTable);
    }
    finally
    {
      test.delete();
    }
  }

  @Test
  @Request
  public void testCreateLeaf() throws SQLException
  {
    JsonObject json = getJson(testData.DISTRICT);

    MasterList test = MasterList.create(json);

    try
    {
      MdBusinessDAOIF mdTable = MdBusinessDAO.get(test.getMdBusinessOid());

      Assert.assertNotNull(mdTable);
    }
    finally
    {
      test.delete();
    }
  }

  @Test
  @Request
  public void testCreateMultiple()
  {
    JsonObject json = getJson(testData.STATE);

    MasterList test1 = MasterList.create(json);

    try
    {
      json.addProperty(MasterList.CODE, "CODE_2");

      MasterList test2 = MasterList.create(json);
      test2.delete();
    }
    finally
    {
      test1.delete();
    }
  }

  @Test
  public void testServiceCreateAndRemove()
  {
    JsonObject listJson = getJson(testData.STATE);

    MasterListService service = new MasterListService();
    JsonObject result = service.create(adminCR.getSessionId(), listJson);

    String oid = result.get(ComponentInfo.OID).getAsString();

    service.remove(adminCR.getSessionId(), oid);
  }

  @Test
  public void testList()
  {
    JsonObject listJson = getJson(testData.STATE);

    MasterListService service = new MasterListService();
    JsonObject result = service.create(adminCR.getSessionId(), listJson);

    try
    {
      JsonArray results = service.listAll(adminCR.getSessionId());

      Assert.assertEquals(1, results.size());
    }
    finally
    {
      String oid = result.get(ComponentInfo.OID).getAsString();
      service.remove(adminCR.getSessionId(), oid);
    }
  }

  @Request
  public JsonObject getJson(TestGeoObjectTypeInfo info)
  {
    MasterList list = new MasterList();
    list.setUniversal(info.getUniversal());
    list.getDisplayLabel().setValue("Test List");
    list.setCode("TEST_CODE");
    list.setRepresentativityDate(new Date());
    list.setPublishDate(new Date());
    list.setListAbstract("My Abstract");
    list.setProcess("Process");
    list.setProgress("Progress");
    list.setAccessConstraints("Access Contraints");
    list.setUseConstraints("User Constraints");
    list.setAcknowledgements("Acknowledgements");
    list.setDisclaimer("Disclaimer");
    list.setContactName("Contact Name");
    list.setOrganization("Organization");
    list.setTelephoneNumber("Telephone Number");
    list.setEmail("Email");

    JsonObject listJson = list.toJSON();
    return listJson;
  }
}
