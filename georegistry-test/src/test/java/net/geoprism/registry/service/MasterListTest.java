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
package net.geoprism.registry.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
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
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.constants.ComponentInfo;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.graph.MdClassificationInfo;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.database.DuplicateDataDatabaseException;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdClassificationDAO;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.AbstractClassification;

import net.geoprism.registry.ChangeFrequency;
import net.geoprism.registry.DuplicateMasterListException;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.InvalidMasterListException;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.MasterListBuilder;
import net.geoprism.registry.MasterListQuery;
import net.geoprism.registry.MasterListVersion;
import net.geoprism.registry.Organization;
import net.geoprism.registry.TileCache;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestHierarchyTypeInfo;
import net.geoprism.registry.test.TestUserInfo;
import net.geoprism.registry.test.USATestData;

public class MasterListTest
{
  private static String                      CLASSIFICATION_TYPE = "test.classification.TestClassification";

  private static String                      CODE                = "Test Term";

  private static USATestData                 testData;

  private static AttributeTermType           testTerm;

  private static AttributeClassificationType testClassification;

  @BeforeClass
  public static void setUpClass()
  {
    testData = USATestData.newTestData();
    testData.setUpMetadata();

    setUpInReq();
  }

  @Request
  private static void setUpInReq()
  {
    MdClassificationDAO mdClassification = MdClassificationDAO.newInstance();
    mdClassification.setValue(MdClassificationInfo.PACKAGE, "test.classification");
    mdClassification.setValue(MdClassificationInfo.TYPE_NAME, "TestClassification");
    mdClassification.setValue(MdClassificationInfo.GENERATE_SOURCE, MdAttributeBooleanInfo.FALSE);
    mdClassification.setStructValue(MdClassificationInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, "Test Classification");
    mdClassification.apply();

    MdVertexDAOIF referenceMdVertexDAO = mdClassification.getReferenceMdVertexDAO();

    VertexObject root = new VertexObject(referenceMdVertexDAO.definesType());
    root.setValue(AbstractClassification.CODE, CODE);
    root.setEmbeddedValue(AbstractClassification.DISPLAYLABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, "Test Classification");
    root.apply();

    mdClassification.setValue(MdClassificationInfo.ROOT, root.getOid());
    mdClassification.apply();

    testClassification = (AttributeClassificationType) AttributeType.factory("testClassification", new LocalizedValue("testClassificationLocalName"), new LocalizedValue("testClassificationLocalDescrip"), AttributeClassificationType.TYPE, false, false, false);
    testClassification.setClassificationType(CLASSIFICATION_TYPE);
    testClassification.setRootTerm(new Term(CODE, new LocalizedValue("Test Classification"), new LocalizedValue("Test Classification")));

    ServerGeoObjectType got = ServerGeoObjectType.get(USATestData.STATE.getCode());
    testClassification = (AttributeClassificationType) got.createAttributeType(testClassification.toJSON().toString());

    testTerm = (AttributeTermType) AttributeType.factory("testTerm", new LocalizedValue("testTermLocalName"), new LocalizedValue("testTermLocalDescrip"), AttributeTermType.TYPE, false, false, false);

    testTerm = (AttributeTermType) got.createAttributeType(testTerm.toJSON().toString());

    USATestData.COLORADO.setDefaultValue(testClassification.getName(), CODE);
  }

  @AfterClass
  @Request
  public static void classTearDown()
  {
    if (testData != null)
    {
      testData.tearDownMetadata();
    }

    USATestData.COLORADO.removeDefaultValue(testClassification.getName());

    try
    {
      MdClassificationDAO.getMdClassificationDAO(CLASSIFICATION_TYPE).getBusinessDAO().delete();
    }
    catch (Exception e)
    {
      // skip
    }
  }

  @Before
  public void setUp()
  {
    cleanUpExtra();

    testData.setUpInstanceData();

    testData.logIn(USATestData.USER_NPS_RA);
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    cleanUpExtra();

    testData.tearDownInstanceData();
  }

  @Request
  public void cleanUpExtra()
  {
    MasterListQuery query = new MasterListQuery(new QueryFactory());

    OIterator<? extends MasterList> it = query.getIterator();

    try
    {
      while (it.hasNext())
      {
        it.next().delete();
      }
    }
    finally
    {
      it.close();
    }
  }

  @Test
  @Request
  public void testSerialization()
  {
    MasterList list = new MasterList();
    list.setUniversal(USATestData.STATE.getUniversal());
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
    list.setOrganization(USATestData.ORG_NPS.getServerObject());
    list.setTelephoneNumber("Telephone Number");
    list.setEmail("Email");
    list.setIsMaster(false);
    list.setVisibility(MasterList.PUBLIC);

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
  public void testCreateMultiple()
  {
    JsonObject json = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.STATE, MasterList.PUBLIC, true);

    MasterList test1 = MasterList.create(json);

    try
    {
      json.addProperty(MasterList.CODE, "CODE_2");

      MasterList test2 = MasterList.create(json);
      test2.delete();

      Assert.fail("Able to create multiple masterlists with the same universal");
    }
    catch (DuplicateMasterListException e)
    {
      test1.delete();
    }
  }

  @Test
  @Request
  public void testCreateMultipleNonMaster()
  {
    JsonObject json = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.STATE, MasterList.PUBLIC, false);

    MasterList test1 = MasterList.create(json);

    try
    {
      json.addProperty(MasterList.CODE, "CODE_2");

      MasterList test2 = MasterList.create(json);
      test2.delete();
    }
    catch (DuplicateDataDatabaseException e)
    {
      test1.delete();

      Assert.fail("Not able to create multiple masterlists with the same universal when list is not a master");
    }
  }

  @Test
  public void testServiceCreateAndRemove()
  {
    JsonObject listJson = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.STATE, MasterList.PUBLIC, false);

    MasterListService service = new MasterListService();
    JsonObject result = service.create(testData.clientRequest.getSessionId(), listJson);

    String oid = result.get(ComponentInfo.OID).getAsString();

    service.remove(testData.clientRequest.getSessionId(), oid);
  }

  @Test
  public void testListByOrg()
  {
    JsonObject listJson = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.STATE, MasterList.PUBLIC, false);

    MasterListService service = new MasterListService();
    JsonObject result = service.create(testData.clientRequest.getSessionId(), listJson);

    try
    {
      JsonArray orgs = service.listByOrg(testData.clientRequest.getSessionId());

      JsonObject org = null;
      for (int i = 0; i < orgs.size(); ++i)
      {
        if (orgs.get(i).getAsJsonObject().get("oid").getAsString().equals(USATestData.ORG_NPS.getServerObject().getOid()))
        {
          org = orgs.get(i).getAsJsonObject();
        }
      }

      Assert.assertNotNull(org.get("oid").getAsString());
      Assert.assertEquals(USATestData.ORG_NPS.getDisplayLabel(), org.get("label").getAsString());
      Assert.assertTrue(org.get("write").getAsBoolean());
      Assert.assertTrue(org.get("lists").getAsJsonArray().size() > 0);
    }
    finally
    {
      String oid = result.get(ComponentInfo.OID).getAsString();
      service.remove(testData.clientRequest.getSessionId(), oid);
    }
  }

  @Test
  public void testListPublicByOrgFromOtherOrg()
  {
    JsonObject listJson = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.STATE, MasterList.PUBLIC, false);

    MasterListService service = new MasterListService();
    JsonObject result = service.create(testData.clientRequest.getSessionId(), listJson);

    try
    {
      USATestData.runAsUser(USATestData.USER_PPP_RA, (request, adapter) -> {

        JsonArray orgs = service.listByOrg(request.getSessionId());

        JsonObject org = null;
        for (int i = 0; i < orgs.size(); ++i)
        {
          if (orgs.get(i).getAsJsonObject().get("oid").getAsString().equals(USATestData.ORG_NPS.getServerObject().getOid()))
          {
            org = orgs.get(i).getAsJsonObject();
          }
        }

        Assert.assertNotNull(org.get("oid").getAsString());
        Assert.assertEquals(USATestData.ORG_NPS.getDisplayLabel(), org.get("label").getAsString());
        Assert.assertFalse(org.get("write").getAsBoolean());
        Assert.assertTrue(org.get("lists").getAsJsonArray().size() > 0);
      });
    }
    finally
    {
      String oid = result.get(ComponentInfo.OID).getAsString();
      service.remove(testData.clientRequest.getSessionId(), oid);
    }
  }

  @Test
  public void testPrivateListByOrgFromOtherOrg()
  {
    JsonObject listJson = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.STATE, MasterList.PRIVATE, false);

    MasterListService service = new MasterListService();
    JsonObject result = service.create(testData.clientRequest.getSessionId(), listJson);

    try
    {
      USATestData.runAsUser(USATestData.USER_PPP_RA, (request, adapter) -> {
        JsonArray orgs = service.listByOrg(request.getSessionId());

        JsonObject org = null;
        for (int i = 0; i < orgs.size(); ++i)
        {
          if (orgs.get(i).getAsJsonObject().get("oid").getAsString().equals(USATestData.ORG_NPS.getServerObject().getOid()))
          {
            org = orgs.get(i).getAsJsonObject();
          }
        }

        Assert.assertNotNull(org.get("oid").getAsString());
        Assert.assertEquals(USATestData.ORG_NPS.getDisplayLabel(), org.get("label").getAsString());
        Assert.assertFalse(org.get("write").getAsBoolean());
        Assert.assertTrue(org.get("lists").getAsJsonArray().size() == 0);
      });
    }
    finally
    {
      String oid = result.get(ComponentInfo.OID).getAsString();
      service.remove(testData.clientRequest.getSessionId(), oid);
    }
  }

  @Test
  @Request
  public void testPublishVersion()
  {
    TestDataSet.runAsUser(USATestData.USER_ADMIN, (request, adapter) -> {

      JsonObject json = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.STATE, MasterList.PUBLIC, false, USATestData.COUNTRY);

      MasterList test = MasterList.create(json);

      try
      {
        MasterListVersion version = test.getOrCreateVersion(new Date(), MasterListVersion.EXPLORATORY);

        try
        {
          MdBusinessDAOIF mdTable = MdBusinessDAO.get(version.getMdBusinessOid());

          Assert.assertNotNull(mdTable);

          version.publish();
        }
        finally
        {
          version.delete();
        }
      }
      finally
      {
        test.delete();
      }
    });
  }

  @Test
  @Request
  public void testPublishVersionOfAbstract()
  {
    TestDataSet.runAsUser(USATestData.USER_ADMIN, (request, adapter) -> {

      MasterListBuilder builder = new MasterListBuilder();
      builder.setOrg(USATestData.ORG_NPS.getServerObject());
      builder.setHt(USATestData.HIER_ADMIN);
      builder.setInfo(USATestData.HEALTH_FACILITY);
      builder.setVisibility(MasterList.PUBLIC);
      builder.setMaster(false);
      builder.setParents(USATestData.COUNTRY, USATestData.STATE, USATestData.DISTRICT);
      builder.setSubtypeHierarchies(USATestData.HIER_REPORTS_TO);

      MasterList test = builder.build();

      try
      {
        MasterListVersion version = test.getOrCreateVersion(new Date(), MasterListVersion.EXPLORATORY);

        try
        {
          MdBusinessDAOIF mdTable = MdBusinessDAO.get(version.getMdBusinessOid());

          Assert.assertNotNull(mdTable);

          version.publish();

          JsonObject data = version.data(1, 100, null, null);

          // Entries should be HP_1, HP_2, HS_1, HS_2
          Assert.assertEquals(4, data.get("count").getAsLong());

          JsonArray results = data.get("results").getAsJsonArray();

          for (int i = 0; i < results.size(); i++)
          {
            JsonObject result = results.get(i).getAsJsonObject();

            String code = result.get("code").getAsString();
            String reportsTo = result.get("usatestdatareportstocode").getAsString();

            if (code.equals(USATestData.HS_ONE.getCode()))
            {
              Assert.assertEquals(USATestData.HP_ONE.getCode(), reportsTo);
            }
            else if (code.equals(USATestData.HS_TWO.getCode()))
            {
              Assert.assertEquals(USATestData.HP_TWO.getCode(), reportsTo);
            }
            else
            {
              Assert.assertEquals("", reportsTo);
            }
          }
        }
        finally
        {
          version.delete();
        }
      }
      finally
      {
        test.delete();
      }
    });
  }

  @Test(expected = InvalidMasterListException.class)
  @Request
  public void testPublishInvalidVersion()
  {
    JsonObject json = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.STATE, MasterList.PUBLIC, false, USATestData.COUNTRY);

    MasterList test = MasterList.create(json);

    try
    {
      test.appLock();
      test.setValid(false);
      test.apply();

      MasterListVersion version = test.getOrCreateVersion(new Date(), MasterListVersion.EXPLORATORY);
      version.delete();
    }
    finally
    {
      test.delete();
    }
  }

  @Test
  public void testCreatePublishedVersions()
  {
    JsonObject listJson = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.STATE, MasterList.PUBLIC, false, USATestData.COUNTRY);

    MasterListService service = new MasterListService();
    JsonObject result = service.create(testData.clientRequest.getSessionId(), listJson);
    String oid = result.get(ComponentInfo.OID).getAsString();

    try
    {
      service.createPublishedVersions(testData.clientRequest.getSessionId(), oid);

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
  public void testCreateFromBadRole()
  {
    JsonObject listJson = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.STATE, MasterList.PUBLIC, false, USATestData.COUNTRY);

    TestUserInfo[] users = new TestUserInfo[] { USATestData.USER_ADMIN, USATestData.USER_PPP_RA };

    for (TestUserInfo user : users)
    {
      try
      {
        USATestData.runAsUser(user, (request, adapter) -> {
          MasterListService service = new MasterListService();
          service.create(request.getSessionId(), listJson);
        });
      }
      catch (SmartExceptionDTO e)
      {
        // This is expected
      }
    }
  }

  @Test
  public void testRemoveFromBadRole()
  {
    JsonObject listJson = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.STATE, MasterList.PUBLIC, false, USATestData.COUNTRY);

    MasterListService service = new MasterListService();
    JsonObject result = service.create(testData.clientRequest.getSessionId(), listJson);
    String oid = result.get(ComponentInfo.OID).getAsString();

    try
    {
      TestUserInfo[] users = new TestUserInfo[] { USATestData.USER_ADMIN, USATestData.USER_PPP_RA };

      for (TestUserInfo user : users)
      {
        try
        {
          USATestData.runAsUser(user, (request, adapter) -> {

            service.remove(request.getSessionId(), oid);
          });
        }
        catch (SmartExceptionDTO e)
        {
          // This is expected
        }
      }
    }
    finally
    {
      service.remove(testData.clientRequest.getSessionId(), oid);
    }
  }

  @Test
  public void testCreatePublishedVersionsFromOtherOrg()
  {
    JsonObject listJson = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.STATE, MasterList.PUBLIC, false, USATestData.COUNTRY);

    MasterListService service = new MasterListService();
    JsonObject result = service.create(testData.clientRequest.getSessionId(), listJson);
    String oid = result.get(ComponentInfo.OID).getAsString();

    try
    {
      TestUserInfo[] users = new TestUserInfo[] { USATestData.USER_PPP_RA };

      for (TestUserInfo user : users)
      {
        try
        {
          FastTestDataset.runAsUser(user, (request, adapter) -> {

            service.createPublishedVersions(request.getSessionId(), oid);

            Assert.fail("Able to publish a master list as a user with bad roles");
          });
        }
        catch (SmartExceptionDTO e)
        {
          // This is expected
        }
      }
    }
    finally
    {
      service.remove(testData.clientRequest.getSessionId(), oid);
    }

  }

  @Test
  public void testGetTile() throws IOException
  {
    JsonObject listJson = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.STATE, MasterList.PUBLIC, false, USATestData.COUNTRY);

    MasterListService service = new MasterListService();
    JsonObject result = service.create(testData.clientRequest.getSessionId(), listJson);
    String oid = result.get(ComponentInfo.OID).getAsString();

    try
    {
      service.createPublishedVersions(testData.clientRequest.getSessionId(), oid);

      final JsonObject object = service.getVersions(testData.clientRequest.getSessionId(), oid, MasterListVersion.PUBLISHED);
      final JsonArray json = object.get(MasterList.VERSIONS).getAsJsonArray();

      Assert.assertEquals(USATestData.DEFAULT_TIME_YEAR_DIFF, json.size());

      String versionId = json.get(0).getAsJsonObject().get("oid").getAsString();

      JSONObject tileObj = new JSONObject();
      tileObj.put("oid", versionId);
      tileObj.put("x", 1);
      tileObj.put("y", 1);
      tileObj.put("z", 1);

      try (InputStream tile = service.getTile(testData.clientRequest.getSessionId(), tileObj))
      {
        Assert.assertNotNull(tile);

        byte[] ctile = getCachedTile(versionId);

        Assert.assertNotNull(ctile);
        Assert.assertTrue(ctile.length > 0);
      }
    }
    finally
    {
      service.remove(testData.clientRequest.getSessionId(), oid);
    }
  }

  @Request
  private byte[] getCachedTile(String versionId)
  {
    return TileCache.getCachedTile(versionId, 1, 1, 1);
  }

  @Test
  @Request
  public void testGetAnnualFrequencyDates()
  {
    final MasterList list = new MasterList();
    list.addFrequency(ChangeFrequency.ANNUAL);

    Calendar calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
    calendar.clear();
    calendar.set(2012, Calendar.MARCH, 3);

    final Date startDate = calendar.getTime();

    calendar.set(2017, Calendar.OCTOBER, 21);

    final Date endDate = calendar.getTime();

    List<Date> dates = list.getFrequencyDates(startDate, endDate);

    Assert.assertEquals(6, dates.size());

    for (int i = 0; i < dates.size(); i++)
    {
      calendar.clear();
      calendar.set( ( 2012 + i ), Calendar.DECEMBER, 31);

      Assert.assertEquals(calendar.getTime(), dates.get(i));
    }
  }

  @Test
  @Request
  public void testGetQuarterFrequencyDates()
  {
    final MasterList list = new MasterList();
    list.addFrequency(ChangeFrequency.QUARTER);

    Calendar calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
    calendar.clear();
    calendar.set(2012, Calendar.MARCH, 3);

    final Date startDate = calendar.getTime();

    calendar.set(2013, Calendar.JANUARY, 2);

    final Date endDate = calendar.getTime();

    List<Date> dates = list.getFrequencyDates(startDate, endDate);

    Assert.assertEquals(5, dates.size());
  }

  @Test
  @Request
  public void testGetBiannualFrequencyDates()
  {
    final MasterList list = new MasterList();
    list.addFrequency(ChangeFrequency.BIANNUAL);

    Calendar calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
    calendar.clear();
    calendar.set(2012, Calendar.MARCH, 3);

    final Date startDate = calendar.getTime();

    calendar.set(2013, Calendar.JANUARY, 2);

    final Date endDate = calendar.getTime();

    List<Date> dates = list.getFrequencyDates(startDate, endDate);

    Assert.assertEquals(3, dates.size());
  }

  @Test
  @Request
  public void testGetMonthFrequencyDates()
  {
    final MasterList list = new MasterList();
    list.addFrequency(ChangeFrequency.MONTHLY);

    Calendar calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
    calendar.clear();
    calendar.set(2012, Calendar.MARCH, 3);

    final Date startDate = calendar.getTime();

    calendar.set(2013, Calendar.JANUARY, 2);

    final Date endDate = calendar.getTime();

    List<Date> dates = list.getFrequencyDates(startDate, endDate);

    Assert.assertEquals(11, dates.size());
  }

  @Test
  @Request
  public void testMarkAsInvalidByParent()
  {
    JsonObject json = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.DISTRICT, MasterList.PUBLIC, false, USATestData.COUNTRY, USATestData.STATE);

    MasterList masterlist = MasterList.create(json);

    try
    {
      masterlist.markAsInvalid(USATestData.HIER_ADMIN.getServerObject(), USATestData.STATE.getServerObject());

      Assert.assertFalse(masterlist.getValid());
    }
    catch (DuplicateDataDatabaseException e)
    {
      masterlist.delete();
    }
  }

  @Test
  @Request
  public void testMarkAsInvalidByDirectType()
  {
    JsonObject json = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.DISTRICT, MasterList.PUBLIC, false, USATestData.COUNTRY, USATestData.STATE);

    MasterList masterlist = MasterList.create(json);

    try
    {
      masterlist.markAsInvalid(USATestData.HIER_ADMIN.getServerObject(), USATestData.DISTRICT.getServerObject());

      Assert.assertFalse(masterlist.getValid());
    }
    catch (DuplicateDataDatabaseException e)
    {
      masterlist.delete();
    }
  }

  @Test
  @Request
  public void testFailMarkAsInvalidByType()
  {
    JsonObject json = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.DISTRICT, MasterList.PUBLIC, false, USATestData.COUNTRY, USATestData.STATE);

    MasterList masterlist = MasterList.create(json);

    try
    {
      masterlist.markAsInvalid(USATestData.HIER_ADMIN.getServerObject(), USATestData.COUNTY.getServerObject());

      Assert.assertTrue(masterlist.isValid());
    }
    catch (DuplicateDataDatabaseException e)
    {
      masterlist.delete();
    }
  }

  @Test
  @Request
  public void testFailMarkAsInvalidByHierarchy()
  {
    JsonObject json = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.DISTRICT, MasterList.PUBLIC, false, USATestData.COUNTRY, USATestData.STATE);

    MasterList masterlist = MasterList.create(json);

    try
    {
      masterlist.markAsInvalid(USATestData.HIER_SCHOOL.getServerObject(), USATestData.DISTRICT.getServerObject());

      Assert.assertTrue(masterlist.isValid());
    }
    catch (DuplicateDataDatabaseException e)
    {
      masterlist.delete();
    }
  }

  @Test
  @Request
  public void testMarkAllAsInvalid()
  {
    JsonObject json = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.STATE, MasterList.PUBLIC, false);

    MasterList masterlist = MasterList.create(json);

    try
    {
      MasterList.markAllAsInvalid(USATestData.HIER_ADMIN.getServerObject(), USATestData.STATE.getServerObject());

      MasterList test = MasterList.get(masterlist.getOid());

      Assert.assertFalse(test.getValid());
    }
    catch (DuplicateDataDatabaseException e)
    {
      masterlist.delete();
    }
  }

  @Request
  public static JsonObject getJson(Organization org, TestHierarchyTypeInfo ht, TestGeoObjectTypeInfo info, String visibility, boolean isMaster, TestGeoObjectTypeInfo... parents)
  {
    MasterListBuilder builder = new MasterListBuilder();
    builder.setOrg(org);
    builder.setHt(ht);
    builder.setInfo(info);
    builder.setVisibility(visibility);
    builder.setMaster(isMaster);
    builder.setParents(parents);

    return builder.buildJSON();
  }

}
