/**
 *
 */
package net.geoprism.registry.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonReader;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.RunwayExceptionDTO;
import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.constants.ComponentInfo;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.database.DuplicateDataDatabaseException;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryQuery;
import com.runwaysdk.system.scheduler.SchedulerManager;

import net.geoprism.registry.ChangeFrequency;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.IncrementalLabeledPropertyGraphType;
import net.geoprism.registry.IntervalLabeledPropertyGraphType;
import net.geoprism.registry.InvalidMasterListException;
import net.geoprism.registry.LabeledPropertyGraphType;
import net.geoprism.registry.LabeledPropertyGraphTypeBuilder;
import net.geoprism.registry.LabeledPropertyGraphTypeEntry;
import net.geoprism.registry.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.Organization;
import net.geoprism.registry.SingleLabeledPropertyGraphType;
import net.geoprism.registry.classification.ClassificationTypeTest;
import net.geoprism.registry.etl.PublishLabeledPropertyGraphTypeVersionJobQuery;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.test.SchedulerTestUtils;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestHierarchyTypeInfo;
import net.geoprism.registry.test.TestUserInfo;
import net.geoprism.registry.test.USATestData;
import net.geoprism.registry.view.JsonSerializable;
import net.geoprism.registry.view.Page;

public class LabeledPropertyGraphTest
{
  private static String                      CODE = "Test Term";

  private static ClassificationType          type;

  private static USATestData                 testData;

  private static AttributeTermType           testTerm;

  private static AttributeClassificationType testClassification;

  @BeforeClass
  public static void setUpClass()
  {
    testData = USATestData.newTestData();
    testData.setUpMetadata();

    setUpInReq();

    if (!SchedulerManager.initialized())
    {
      SchedulerManager.start();
    }
  }

  @Request
  private static void setUpInReq()
  {
    type = ClassificationType.apply(ClassificationTypeTest.createMock());

    Classification root = Classification.newInstance(type);
    root.setCode(CODE);
    root.setDisplayLabel(new LocalizedValue("Test Classification"));
    root.apply(null);

    testClassification = (AttributeClassificationType) AttributeType.factory("testClassification", new LocalizedValue("testClassificationLocalName"), new LocalizedValue("testClassificationLocalDescrip"), AttributeClassificationType.TYPE, false, false, false);
    testClassification.setClassificationType(type.getCode());
    testClassification.setRootTerm(root.toTerm());

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

    if (type != null)
    {
      type.delete();
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
    TestDataSet.deleteAllListData();
  }

  @Test
  @Request
  public void testSingleLabeledPropertyGraphTypeSerialization()
  {
    SingleLabeledPropertyGraphType type = new SingleLabeledPropertyGraphType();
    type.setUniversal(USATestData.STATE.getUniversal());
    type.getDisplayLabel().setValue("Test List");
    type.setCode("TEST_CODE");
    type.setOrganization(USATestData.ORG_NPS.getServerObject());
    type.getDescription().setValue("My Overal Description");
    type.setValidOn(USATestData.DEFAULT_OVER_TIME_DATE);

    JsonObject json = type.toJSON();
    SingleLabeledPropertyGraphType test = (SingleLabeledPropertyGraphType) LabeledPropertyGraphType.fromJSON(json);

    Assert.assertEquals(type.getDisplayLabel().getValue(), test.getDisplayLabel().getValue());
    Assert.assertEquals(type.getDescription().getValue(), test.getDescription().getValue());
    Assert.assertEquals(type.getCode(), test.getCode());
    Assert.assertEquals(type.getHierarchiesAsJson().toString(), test.getHierarchiesAsJson().toString());
    Assert.assertEquals(type.getValidOn(), test.getValidOn());
  }

  @Test
  @Request
  public void testIntervalLabeledPropertyGraphTypeSerialization()
  {
    JsonObject interval = new JsonObject();
    interval.addProperty(IntervalLabeledPropertyGraphType.START_DATE, GeoRegistryUtil.formatDate(USATestData.DEFAULT_OVER_TIME_DATE, false));
    interval.addProperty(IntervalLabeledPropertyGraphType.END_DATE, GeoRegistryUtil.formatDate(USATestData.DEFAULT_END_TIME_DATE, false));

    JsonArray intervalJson = new JsonArray();
    intervalJson.add(interval);

    IntervalLabeledPropertyGraphType type = new IntervalLabeledPropertyGraphType();
    type.setUniversal(USATestData.STATE.getUniversal());
    type.getDisplayLabel().setValue("Test List");
    type.setCode("TEST_CODE");
    type.setOrganization(USATestData.ORG_NPS.getServerObject());
    type.getDescription().setValue("My Overal Description");
    type.setIntervalJson(intervalJson.toString());

    JsonObject json = type.toJSON();
    IntervalLabeledPropertyGraphType test = (IntervalLabeledPropertyGraphType) LabeledPropertyGraphType.fromJSON(json);

    Assert.assertEquals(type.getUniversalOid(), test.getUniversalOid());
    Assert.assertEquals(type.getDisplayLabel().getValue(), test.getDisplayLabel().getValue());
    Assert.assertEquals(type.getDescription().getValue(), test.getDescription().getValue());
    Assert.assertEquals(type.getOrganization(), test.getOrganization());
    Assert.assertEquals(type.getCode(), test.getCode());
    Assert.assertEquals(type.getHierarchiesAsJson().toString(), test.getHierarchiesAsJson().toString());
    Assert.assertEquals(type.getIntervalJson(), test.getIntervalJson());
  }

  @Test
  @Request
  public void testIncrementLabeledPropertyGraphTypeSerialization()
  {
    IncrementalLabeledPropertyGraphType type = new IncrementalLabeledPropertyGraphType();
    type.setUniversal(USATestData.STATE.getUniversal());
    type.getDisplayLabel().setValue("Test List");
    type.setCode("TEST_CODE");
    type.setOrganization(USATestData.ORG_NPS.getServerObject());
    type.getDescription().setValue("My Overal Description");
    type.setPublishingStartDate(USATestData.DEFAULT_OVER_TIME_DATE);
    type.addFrequency(ChangeFrequency.ANNUAL);

    JsonObject json = type.toJSON();
    IncrementalLabeledPropertyGraphType test = (IncrementalLabeledPropertyGraphType) LabeledPropertyGraphType.fromJSON(json);

    Assert.assertEquals(type.getUniversalOid(), test.getUniversalOid());
    Assert.assertEquals(type.getDisplayLabel().getValue(), test.getDisplayLabel().getValue());
    Assert.assertEquals(type.getDescription().getValue(), test.getDescription().getValue());
    Assert.assertEquals(type.getOrganization(), test.getOrganization());
    Assert.assertEquals(type.getCode(), test.getCode());
    Assert.assertEquals(type.getHierarchiesAsJson().toString(), test.getHierarchiesAsJson().toString());
    Assert.assertEquals(type.getFrequency().get(0), test.getFrequency().get(0));
    Assert.assertEquals(type.getPublishingStartDate(), test.getPublishingStartDate());
  }

  // @Test
  // @Request
  // public void testCreateMultiple()
  // {
  // JsonObject json = getJson(USATestData.ORG_NPS.getServerObject(),
  // USATestData.HIER_ADMIN, USATestData.STATE, LabeledPropertyGraphType.PUBLIC, true);
  //
  // LabeledPropertyGraphType test1 = LabeledPropertyGraphType.apply(json);
  //
  // try
  // {
  // json.addProperty(LabeledPropertyGraphType.CODE, "CODE_2");
  //
  // LabeledPropertyGraphType test2 = LabeledPropertyGraphType.apply(json);
  // test2.delete();
  //
  // Assert.fail("Able to apply multiple mastertypes with the same universal");
  // }
  // catch (DuplicateLabeledPropertyGraphTypeException e)
  // {
  // test1.delete();
  // }
  // }
  //

  @Test
  @Request
  public void testCreateMultipleNonMaster()
  {
    JsonObject json = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.STATE);

    LabeledPropertyGraphType test1 = LabeledPropertyGraphType.apply(json);

    try
    {
      json.addProperty(LabeledPropertyGraphType.CODE, "CODE_2");

      LabeledPropertyGraphType test2 = LabeledPropertyGraphType.apply(json);
      test2.delete();
    }
    catch (DuplicateDataDatabaseException e)
    {
      test1.delete();

      Assert.fail("Not able to apply multiple mastertypes with the same universal when type is not a master");
    }
  }

  @Test(expected = RunwayExceptionDTO.class)
  public void testServiceCreateAndRemove()
  {
    JsonObject typeJson = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.STATE);

    LabeledPropertyGraphTypeService service = new LabeledPropertyGraphTypeService();
    JsonObject result = service.apply(testData.clientRequest.getSessionId(), typeJson);

    String oid = result.get(ComponentInfo.OID).getAsString();

    this.waitUntilPublished(oid);

    service.remove(testData.clientRequest.getSessionId(), oid);

    service.get(testData.clientRequest.getSessionId(), oid);
  }

  @Test
  public void testListForType()
  {
    JsonObject typeJson = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.STATE);

    LabeledPropertyGraphTypeService service = new LabeledPropertyGraphTypeService();
    JsonObject result = service.apply(testData.clientRequest.getSessionId(), typeJson);

    String oid = result.get(ComponentInfo.OID).getAsString();
    this.waitUntilPublished(oid);

    try
    {
      JsonObject org = service.typeForType(testData.clientRequest.getSessionId(), USATestData.STATE.getCode());

      Assert.assertEquals(USATestData.ORG_NPS.getDisplayLabel(), org.get("orgLabel").getAsString());
      Assert.assertEquals(USATestData.ORG_NPS.getCode(), org.get("orgCode").getAsString());
      Assert.assertEquals(USATestData.STATE.getDisplayLabel().getValue(), org.get("typeLabel").getAsString());
      Assert.assertEquals(USATestData.STATE.getCode(), org.get("typeCode").getAsString());
      Assert.assertTrue(org.get("write").getAsBoolean());
      Assert.assertTrue(org.get("types").getAsJsonArray().size() > 0);
    }
    finally
    {
      service.remove(testData.clientRequest.getSessionId(), oid);
    }
  }

//  @Test
//  public void testListPublicByOrgFromOtherOrg()
//  {
//    JsonObject typeJson = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.STATE);
//
//    LabeledPropertyGraphTypeService service = new LabeledPropertyGraphTypeService();
//    JsonObject result = service.apply(testData.clientRequest.getSessionId(), typeJson);
//
//    String oid = result.get(ComponentInfo.OID).getAsString();
//    this.waitUntilPublished(oid);
//
//    try
//    {
//      USATestData.runAsUser(USATestData.USER_PPP_RA, (request) -> {
//
//        JsonObject org = service.typeForType(testData.clientRequest.getSessionId(), USATestData.STATE.getCode());
//
//        Assert.assertEquals(USATestData.ORG_NPS.getDisplayLabel(), org.get("orgLabel").getAsString());
//        Assert.assertEquals(USATestData.ORG_NPS.getCode(), org.get("orgCode").getAsString());
//        Assert.assertEquals(USATestData.STATE.getDisplayLabel().getValue(), org.get("typeLabel").getAsString());
//        Assert.assertEquals(USATestData.STATE.getCode(), org.get("typeCode").getAsString());
//        Assert.assertTrue(org.get("write").getAsBoolean());
//        Assert.assertTrue(org.get("types").getAsJsonArray().size() > 0);
//      });
//    }
//    finally
//    {
//      service.remove(testData.clientRequest.getSessionId(), oid);
//    }
//  }
//
//  //
//  // @Test
//  // public void testPrivateListByOrgFromOtherOrg()
//  // {
//  // JsonObject typeJson = getJson(USATestData.ORG_NPS.getServerObject(),
//  // USATestData.HIER_ADMIN, USATestData.STATE, LabeledPropertyGraphType.PRIVATE, false);
//  //
//  // LabeledPropertyGraphTypeService service = new LabeledPropertyGraphTypeService();
//  // JsonObject result = service.apply(testData.clientRequest.getSessionId(),
//  // typeJson);
//  //
//  // try
//  // {
//  // USATestData.runAsUser(USATestData.USER_PPP_RA, (request, adapter) -> {
//  // JsonArray orgs = service.typeByOrg(request.getSessionId());
//  //
//  // JsonObject org = null;
//  // for (int i = 0; i < orgs.size(); ++i)
//  // {
//  // if
//  // (orgs.get(i).getAsJsonObject().get("oid").getAsString().equals(USATestData.ORG_NPS.getServerObject().getOid()))
//  // {
//  // org = orgs.get(i).getAsJsonObject();
//  // }
//  // }
//  //
//  // Assert.assertNotNull(org.get("oid").getAsString());
//  // Assert.assertEquals(USATestData.ORG_NPS.getDisplayLabel(),
//  // org.get("label").getAsString());
//  // Assert.assertFalse(org.get("write").getAsBoolean());
//  // Assert.assertTrue(org.get("types").getAsJsonArray().size() == 0);
//  // });
//  // }
//  // finally
//  // {
//  // String oid = result.get(ComponentInfo.OID).getAsString();
//  // service.remove(testData.clientRequest.getSessionId(), oid);
//  // }
//  // }
//  //
//
//  @Test
//  public void testPublishVersion()
//  {
//    USATestData.executeRequestAsUser(USATestData.USER_ADMIN, () -> {
//
//      JsonObject json = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.STATE, USATestData.COUNTRY);
//
//      LabeledPropertyGraphType test = LabeledPropertyGraphType.apply(json);
//
//      try
//      {
//        LabeledPropertyGraphTypeEntry entry = test.createEntry(TestDataSet.DEFAULT_OVER_TIME_DATE);
//
//        try
//        {
//          entry.publish(createVersionMetadata().toString());
//
//          List<LabeledPropertyGraphTypeVersion> versions = entry.getVersions();
//
//          Assert.assertEquals(2, versions.size());
//
//          LabeledPropertyGraphTypeVersion version = versions.get(0);
//
//          MdBusinessDAOIF mdTable = MdBusinessDAO.get(version.getMdBusinessOid());
//
//          Assert.assertNotNull(mdTable);
//        }
//        finally
//        {
//          entry.delete();
//        }
//      }
//      catch (Exception e)
//      {
//        e.printStackTrace();
//
//        Assert.fail(e.getLocalizedMessage());
//      }
//      finally
//      {
//        test.delete();
//      }
//    });
//  }
//
//  @Test
//  public void testPublishVersionOfAbstract()
//  {
//    dataTest(null);
//  }
//
//  @Test
//  public void testFetchDataWithGeometries()
//  {
//    dataTest(true);
//  }
//
//  private void dataTest(Boolean includeGeometries)
//  {
//    GeoJsonReader reader = new GeoJsonReader();
//
//    TestDataSet.executeRequestAsUser(USATestData.USER_ADMIN, () -> {
//
//      LabeledPropertyGraphTypeBuilder.Hierarchy hierarchy = new LabeledPropertyGraphTypeBuilder.Hierarchy();
//      hierarchy.setType(USATestData.HIER_ADMIN);
//      hierarchy.setParents(USATestData.COUNTRY, USATestData.STATE, USATestData.DISTRICT);
//      hierarchy.setSubtypeHierarchies(USATestData.HIER_REPORTS_TO);
//
//      LabeledPropertyGraphTypeBuilder builder = new LabeledPropertyGraphTypeBuilder();
//      builder.setOrg(USATestData.ORG_NPS.getServerObject());
//      builder.setInfo(USATestData.HEALTH_FACILITY);
//      builder.setHts(hierarchy);
//
//      LabeledPropertyGraphType test = builder.build();
//
//      try
//      {
//        LabeledPropertyGraphTypeEntry entry = test.createEntry(TestDataSet.DEFAULT_OVER_TIME_DATE);
//
//        try
//        {
//          entry.publish(createVersionMetadata().toString());
//
//          List<LabeledPropertyGraphTypeVersion> versions = entry.getVersions();
//
//          Assert.assertEquals(2, versions.size());
//
//          LabeledPropertyGraphTypeVersion version = versions.get(0);
//
//          MdBusinessDAOIF mdTable = MdBusinessDAO.get(version.getMdBusinessOid());
//
//          Assert.assertNotNull(mdTable);
//
//          Page<JsonSerializable> data = version.data(new JsonObject(), true, includeGeometries);
//
//          // Entries should be HP_1, HP_2, HS_1, HS_2
//          Assert.assertEquals(new Long(4), data.getCount());
//
//          List<JsonSerializable> results = data.getResults();
//
//          for (int i = 0; i < results.size(); i++)
//          {
//            JsonObject result = results.get(i).toJSON().getAsJsonObject();
//
//            String code = result.get("code").getAsString();
//
//            if (code.equals(USATestData.HS_ONE.getCode()))
//            {
//              String reportsTo = result.get("usatestdatareportstocode").getAsString();
//              Assert.assertEquals(USATestData.HP_ONE.getCode(), reportsTo);
//            }
//            else if (code.equals(USATestData.HS_TWO.getCode()))
//            {
//              String reportsTo = result.get("usatestdatareportstocode").getAsString();
//
//              Assert.assertEquals(USATestData.HP_TWO.getCode(), reportsTo);
//            }
//
//            if (includeGeometries != null && includeGeometries.equals(Boolean.TRUE))
//            {
//              Assert.assertEquals(true, result.has("geometry"));
//
//              JsonObject geometries = result.get("geometry").getAsJsonObject();
//
//              Geometry jtsGeom = reader.read(geometries.toString());
//              Assert.assertTrue(jtsGeom.isValid());
//            }
//            else
//            {
//              Assert.assertEquals(false, result.has("geometry"));
//            }
//          }
//        }
//        finally
//        {
//          entry.delete();
//        }
//      }
//      catch (Throwable t)
//      {
//        t.printStackTrace();
//        throw new RuntimeException(t);
//      }
//      finally
//      {
//        test.delete();
//      }
//    });
//  }
//
//  @Test(expected = InvalidMasterListException.class)
//  @Request
//  public void testPublishInvalidVersion()
//  {
//    JsonObject json = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.STATE, USATestData.COUNTRY);
//
//    LabeledPropertyGraphType test = LabeledPropertyGraphType.apply(json);
//
//    try
//    {
//      test.appLock();
//      test.setValid(false);
//      test.apply();
//
//      LabeledPropertyGraphTypeEntry version = test.getOrCreateEntry(TestDataSet.DEFAULT_OVER_TIME_DATE, null);
//      version.delete();
//    }
//    finally
//    {
//      test.delete();
//    }
//  }
//
//  // @Test
//  // public void testCreatePublishedVersions()
//  // {
//  // JsonObject typeJson = getJson(USATestData.ORG_NPS.getServerObject(),
//  // USATestData.HIER_ADMIN, USATestData.STATE, USATestData.COUNTRY);
//  //
//  // LabeledPropertyGraphTypeService service = new LabeledPropertyGraphTypeService();
//  // JsonObject result = service.apply(testData.clientRequest.getSessionId(),
//  // typeJson);
//  // String oid = result.get(ComponentInfo.OID).getAsString();
//  //
//  // try
//  // {
//  // service.createPublishedVersions(testData.clientRequest.getSessionId(),
//  // oid);
//  //
//  // final JsonObject json =
//  // service.getEntries(testData.clientRequest.getSessionId(), oid);
//  //
//  // Assert.assertEquals(USATestData.DEFAULT_TIME_YEAR_DIFF, json.size());
//  // }
//  // finally
//  // {
//  // service.remove(testData.clientRequest.getSessionId(), oid);
//  // }
//  // }
//
//  @Test
//  public void testCreateFromBadRole()
//  {
//    JsonObject typeJson = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.STATE, USATestData.COUNTRY);
//
//    TestUserInfo[] users = new TestUserInfo[] { USATestData.USER_PPP_RA };
//
//    for (TestUserInfo user : users)
//    {
//      USATestData.runAsUser(user, (request) -> {
//
//        LabeledPropertyGraphTypeService service = new LabeledPropertyGraphTypeService();
//
//        try
//        {
//          service.apply(request.getSessionId(), typeJson);
//
//          Assert.fail("Expected an exception to be thrown.");
//        }
//        catch (SmartExceptionDTO e)
//        {
//          // This is expected
//        }
//
//      });
//    }
//  }
//
//  @Test
//  public void testRemoveFromBadRole()
//  {
//    JsonObject typeJson = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.STATE, USATestData.COUNTRY);
//
//    LabeledPropertyGraphTypeService service = new LabeledPropertyGraphTypeService();
//    JsonObject result = service.apply(testData.clientRequest.getSessionId(), typeJson);
//    String oid = result.get(ComponentInfo.OID).getAsString();
//
//    this.waitUntilPublished(oid);
//
//    try
//    {
//      TestUserInfo[] users = new TestUserInfo[] { USATestData.USER_PPP_RA };
//
//      for (TestUserInfo user : users)
//      {
//        USATestData.runAsUser(user, (request) -> {
//
//          try
//          {
//            service.remove(request.getSessionId(), oid);
//
//            Assert.fail("Expected an exception to be thrown.");
//          }
//          catch (SmartExceptionDTO e)
//          {
//            // This is expected
//          }
//
//        });
//      }
//    }
//    finally
//    {
//      service.remove(testData.clientRequest.getSessionId(), oid);
//    }
//  }
//
//  @Request
//  private void waitUntilPublished(String oid)
//  {
//    List<? extends JobHistory> histories = null;
//    int waitTime = 0;
//
//    while (histories == null)
//    {
//      if (waitTime > 10000)
//      {
//        Assert.fail("Job was never scheduled. Unable to find any associated history.");
//      }
//
//      QueryFactory qf = new QueryFactory();
//
//      PublishLabeledPropertyGraphTypeVersionJobQuery jobQuery = new PublishLabeledPropertyGraphTypeVersionJobQuery(qf);
//      jobQuery.WHERE(jobQuery.getLabeledPropertyGraphType().EQ(oid));
//
//      JobHistoryQuery jhq = new JobHistoryQuery(qf);
//      jhq.WHERE(jhq.job(jobQuery));
//
//      List<? extends JobHistory> potentialHistories = jhq.getIterator().getAll();
//
//      if (potentialHistories.size() > 0)
//      {
//        histories = potentialHistories;
//      }
//      else
//      {
//        try
//        {
//          Thread.sleep(1000);
//        }
//        catch (InterruptedException e)
//        {
//          e.printStackTrace();
//          Assert.fail("Interrupted while waiting");
//        }
//
//        waitTime += 1000;
//      }
//    }
//
//    for (JobHistory history : histories)
//    {
//      try
//      {
//        SchedulerTestUtils.waitUntilStatus(history.getOid(), AllJobStatus.SUCCESS);
//      }
//      catch (InterruptedException e)
//      {
//        e.printStackTrace();
//        Assert.fail("Interrupted while waiting");
//      }
//    }
//  }
//
//  // @Test
//  // public void testCreatePublishedVersionsFromOtherOrg()
//  // {
//  // JsonObject typeJson = getJson(USATestData.ORG_NPS.getServerObject(),
//  // USATestData.HIER_ADMIN, USATestData.STATE, USATestData.COUNTRY);
//  //
//  // LabeledPropertyGraphTypeService service = new LabeledPropertyGraphTypeService();
//  // JsonObject result = service.apply(testData.clientRequest.getSessionId(),
//  // typeJson);
//  // String oid = result.get(ComponentInfo.OID).getAsString();
//  //
//  // try
//  // {
//  // TestUserInfo[] users = new TestUserInfo[] { USATestData.USER_PPP_RA };
//  //
//  // for (TestUserInfo user : users)
//  // {
//  // try
//  // {
//  // USATestData.runAsUser(user, (request, adapter) -> {
//  //
//  // service.createPublishedVersions(request.getSessionId(), oid);
//  //
//  // Assert.fail("Able to publish a master type as a user with bad roles");
//  // });
//  // }
//  // catch (SmartExceptionDTO e)
//  // {
//  // // This is expected
//  // }
//  // }
//  // }
//  // finally
//  // {
//  // service.remove(testData.clientRequest.getSessionId(), oid);
//  // }
//  //
//  // }
//
//  //
//  // @Test
//  // public void testGetTile() throws IOException
//  // {
//  // JsonObject typeJson = getJson(USATestData.ORG_NPS.getServerObject(),
//  // USATestData.HIER_ADMIN, USATestData.STATE, LabeledPropertyGraphType.PUBLIC, false,
//  // USATestData.COUNTRY);
//  //
//  // LabeledPropertyGraphTypeService service = new LabeledPropertyGraphTypeService();
//  // JsonObject result = service.apply(testData.clientRequest.getSessionId(),
//  // typeJson);
//  // String oid = result.get(ComponentInfo.OID).getAsString();
//  //
//  // try
//  // {
//  // service.applyPublishedVersions(testData.clientRequest.getSessionId(),
//  // oid);
//  //
//  // final JsonObject object =
//  // service.getVersions(testData.clientRequest.getSessionId(), oid,
//  // LabeledPropertyGraphTypeVersion.PUBLISHED);
//  // final JsonArray json = object.get(LabeledPropertyGraphType.VERSIONS).getAsJsonArray();
//  //
//  // Assert.assertEquals(USATestData.DEFAULT_TIME_YEAR_DIFF, json.size());
//  //
//  // String versionId = json.get(0).getAsJsonObject().get("oid").getAsString();
//  //
//  // JSONObject tileObj = new JSONObject();
//  // tileObj.put("oid", versionId);
//  // tileObj.put("x", 1);
//  // tileObj.put("y", 1);
//  // tileObj.put("z", 1);
//  //
//  // try (InputStream tile =
//  // service.getTile(testData.clientRequest.getSessionId(), tileObj))
//  // {
//  // Assert.assertNotNull(tile);
//  //
//  // byte[] ctile = getCachedTile(versionId);
//  //
//  // Assert.assertNotNull(ctile);
//  // Assert.assertTrue(ctile.length > 0);
//  // }
//  // }
//  // finally
//  // {
//  // service.remove(testData.clientRequest.getSessionId(), oid);
//  // }
//  // }
//  //
//  // @Request
//  // private byte[] getCachedTile(String versionId)
//  // {
//  // return TileCache.getCachedTile(versionId, 1, 1, 1);
//  // }
//
//  @Test
//  @Request
//  public void testGetAnnualFrequencyDates()
//  {
//    final IncrementalLabeledPropertyGraphType type = new IncrementalLabeledPropertyGraphType();
//    type.addFrequency(ChangeFrequency.ANNUAL);
//
//    Calendar calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
//    calendar.clear();
//    calendar.set(2012, Calendar.MARCH, 3);
//
//    final Date startDate = calendar.getTime();
//
//    calendar.set(2017, Calendar.OCTOBER, 21);
//
//    final Date endDate = calendar.getTime();
//
//    List<Date> dates = type.getFrequencyDates(startDate, endDate);
//
//    Assert.assertEquals(6, dates.size());
//
//    for (int i = 0; i < dates.size(); i++)
//    {
//      calendar.clear();
//      calendar.set( ( 2012 + i ), Calendar.MARCH, 3);
//
//      Assert.assertEquals(calendar.getTime(), dates.get(i));
//    }
//  }
//
//  @Test
//  @Request
//  public void testGetQuarterFrequencyDates()
//  {
//    final IncrementalLabeledPropertyGraphType type = new IncrementalLabeledPropertyGraphType();
//    type.addFrequency(ChangeFrequency.QUARTER);
//
//    Calendar calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
//    calendar.clear();
//    calendar.set(2012, Calendar.MARCH, 3);
//
//    final Date startDate = calendar.getTime();
//
//    calendar.set(2013, Calendar.JANUARY, 2);
//
//    final Date endDate = calendar.getTime();
//
//    List<Date> dates = type.getFrequencyDates(startDate, endDate);
//
//    Assert.assertEquals(4, dates.size());
//
//    for (int i = 0; i < dates.size(); i++)
//    {
//      calendar.clear();
//      calendar.set(2012, Calendar.MARCH, 3);
//      calendar.add(Calendar.MONTH, ( 3 * i ));
//
//      Assert.assertEquals(calendar.getTime(), dates.get(i));
//    }
//
//  }
//
//  @Test
//  @Request
//  public void testGetBiannualFrequencyDates()
//  {
//    final IncrementalLabeledPropertyGraphType type = new IncrementalLabeledPropertyGraphType();
//    type.addFrequency(ChangeFrequency.BIANNUAL);
//
//    Calendar calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
//    calendar.clear();
//    calendar.set(2012, Calendar.MARCH, 3);
//
//    final Date startDate = calendar.getTime();
//
//    calendar.set(2013, Calendar.JANUARY, 2);
//
//    final Date endDate = calendar.getTime();
//
//    List<Date> dates = type.getFrequencyDates(startDate, endDate);
//
//    Assert.assertEquals(2, dates.size());
//    
//    for (int i = 0; i < dates.size(); i++)
//    {
//      calendar.clear();
//      calendar.set(2012, Calendar.MARCH, 3);
//      calendar.add(Calendar.MONTH, ( 6 * i ));
//
//      Assert.assertEquals(calendar.getTime(), dates.get(i));
//    }
//  }
//
//  @Test
//  @Request
//  public void testGetMonthFrequencyDates()
//  {
//    final IncrementalLabeledPropertyGraphType type = new IncrementalLabeledPropertyGraphType();
//    type.addFrequency(ChangeFrequency.MONTHLY);
//
//    Calendar calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
//    calendar.clear();
//    calendar.set(2012, Calendar.MARCH, 3);
//
//    final Date startDate = calendar.getTime();
//
//    calendar.set(2013, Calendar.JANUARY, 2);
//
//    final Date endDate = calendar.getTime();
//
//    List<Date> dates = type.getFrequencyDates(startDate, endDate);
//
//    Assert.assertEquals(10, dates.size());
//    
//    for (int i = 0; i < dates.size(); i++)
//    {
//      calendar.clear();
//      calendar.set(2012, Calendar.MARCH, 3);
//      calendar.add(Calendar.MONTH, i);
//
//      Assert.assertEquals(calendar.getTime(), dates.get(i));
//    }
//  }
//
//  @Test
//  @Request
//  public void testMarkAsInvalidByParent()
//  {
//    JsonObject json = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.DISTRICT, USATestData.COUNTRY, USATestData.STATE);
//
//    LabeledPropertyGraphType mastertype = LabeledPropertyGraphType.apply(json);
//
//    try
//    {
//      mastertype.markAsInvalid(USATestData.HIER_ADMIN.getServerObject(), USATestData.STATE.getServerObject());
//
//      Assert.assertFalse(mastertype.getValid());
//    }
//    catch (DuplicateDataDatabaseException e)
//    {
//      mastertype.delete();
//    }
//  }
//
//  @Test
//  @Request
//  public void testMarkAsInvalidByDirectType()
//  {
//    JsonObject json = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.DISTRICT, USATestData.COUNTRY, USATestData.STATE);
//
//    LabeledPropertyGraphType mastertype = LabeledPropertyGraphType.apply(json);
//
//    try
//    {
//      mastertype.markAsInvalid(USATestData.HIER_ADMIN.getServerObject(), USATestData.DISTRICT.getServerObject());
//
//      Assert.assertFalse(mastertype.getValid());
//    }
//    catch (DuplicateDataDatabaseException e)
//    {
//      mastertype.delete();
//    }
//  }
//
//  @Test
//  @Request
//  public void testFailMarkAsInvalidByType()
//  {
//    JsonObject json = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.DISTRICT, USATestData.COUNTRY, USATestData.STATE);
//
//    LabeledPropertyGraphType mastertype = LabeledPropertyGraphType.apply(json);
//
//    try
//    {
//      mastertype.markAsInvalid(USATestData.HIER_ADMIN.getServerObject(), USATestData.COUNTY.getServerObject());
//
//      Assert.assertTrue(mastertype.isValid());
//    }
//    catch (DuplicateDataDatabaseException e)
//    {
//      mastertype.delete();
//    }
//  }
//
//  @Test
//  @Request
//  public void testFailMarkAsInvalidByHierarchy()
//  {
//    JsonObject json = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.DISTRICT, USATestData.COUNTRY, USATestData.STATE);
//
//    LabeledPropertyGraphType mastertype = LabeledPropertyGraphType.apply(json);
//
//    try
//    {
//      mastertype.markAsInvalid(USATestData.HIER_SCHOOL.getServerObject(), USATestData.DISTRICT.getServerObject());
//
//      Assert.assertTrue(mastertype.isValid());
//    }
//    catch (DuplicateDataDatabaseException e)
//    {
//      mastertype.delete();
//    }
//  }
//
//  @Test
//  @Request
//  public void testMarkAllAsInvalid()
//  {
//    JsonObject json = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_ADMIN, USATestData.STATE);
//
//    LabeledPropertyGraphType mastertype = LabeledPropertyGraphType.apply(json);
//
//    try
//    {
//      LabeledPropertyGraphType.markAllAsInvalid(USATestData.HIER_ADMIN.getServerObject(), USATestData.STATE.getServerObject());
//
//      LabeledPropertyGraphType test = LabeledPropertyGraphType.get(mastertype.getOid());
//
//      Assert.assertFalse(test.getValid());
//    }
//    catch (DuplicateDataDatabaseException e)
//    {
//      mastertype.delete();
//    }
//  }

  @Request
  public static JsonObject getJson(Organization org, TestHierarchyTypeInfo ht, TestGeoObjectTypeInfo info, TestGeoObjectTypeInfo... parents)
  {
    LabeledPropertyGraphTypeBuilder.Hierarchy hierarchy = new LabeledPropertyGraphTypeBuilder.Hierarchy();
    hierarchy.setType(ht);
    hierarchy.setParents(parents);

    LabeledPropertyGraphTypeBuilder builder = new LabeledPropertyGraphTypeBuilder();
    builder.setOrg(org);
    builder.setInfo(info);
    builder.setHts(hierarchy);

    return builder.buildJSON();
  }

  public static JsonObject createVersionMetadata()
  {
    JsonObject metadata = new JsonObject();
    metadata.add("description", new LocalizedValue("Test").toJSON());
    metadata.addProperty("visibility", "PRIVATE");
    metadata.addProperty("master", false);
    metadata.add("label", new LocalizedValue("Test").toJSON());
    metadata.add("process", new LocalizedValue("Test").toJSON());
    metadata.add("progress", new LocalizedValue("Test").toJSON());
    metadata.add("accessConstraints", new LocalizedValue("Test").toJSON());
    metadata.add("useConstraints", new LocalizedValue("Test").toJSON());
    metadata.add("acknowledgements", new LocalizedValue("Test").toJSON());
    metadata.add("disclaimer", new LocalizedValue("Test").toJSON());
    metadata.addProperty("contactName", "AAAAA");
    metadata.addProperty("organization", "AAAAA");
    metadata.addProperty("telephoneNumber", "AAAAA");
    metadata.addProperty("email", "AAAAA");
    metadata.addProperty("originator", "AAAAA");
    metadata.addProperty("collectionDate", "2020-02-12");
    metadata.addProperty("topicCategories", "AAAAA");
    metadata.addProperty("placeKeywords", "AAAAA");
    metadata.addProperty("updateFrequency", "AAAAA");
    metadata.addProperty("lineage", "AAAAA");
    metadata.addProperty("languages", "AAAAA");
    metadata.addProperty("scaleResolution", "AAAAA");
    metadata.addProperty("spatialRepresentation", "AAAAA");
    metadata.addProperty("referenceSystem", "AAAAA");
    metadata.addProperty("reportSpecification", "AAAAA");
    metadata.addProperty("distributionFormat", "AAAAA");

    JsonObject versionMetadata = new JsonObject();
    versionMetadata.add(LabeledPropertyGraphType.LIST_METADATA, metadata);
    versionMetadata.add(LabeledPropertyGraphType.GEOSPATIAL_METADATA, metadata);
    return versionMetadata;
  }

}
