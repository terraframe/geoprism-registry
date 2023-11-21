/**
 *
 */
package net.geoprism.registry.service;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeQuery;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.classification.ClassificationTypeTest;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.business.ClassificationBusinessServiceIF;
import net.geoprism.registry.service.business.ClassificationTypeBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestRegistryClient;
import net.geoprism.registry.test.TestUserInfo;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class AttributeClassificationTest extends FastDatasetTest implements InstanceTestClassListener
{
  public static final String                  TEST_KEY = "ATTRCLASSTEST";

  public static TestGeoObjectTypeInfo         TEST_GOT = new TestGeoObjectTypeInfo("GOTTest_TEST1", FastTestDataset.ORG_CGOV);

  public static final TestGeoObjectInfo       TEST_GO  = new TestGeoObjectInfo(TEST_KEY + "_NeverNeverLand", TEST_GOT);

  private static String                       CODE     = "Classification-ROOT";

  private static ClassificationType           type;

  private static AttributeClassificationType  testClassification;

  @Autowired
  private TestRegistryClient                  client;

  @Autowired
  private GeoObjectTypeBusinessServiceIF      gotService;

  @Autowired
  private ClassificationTypeBusinessServiceIF cTypeService;

  @Autowired
  private ClassificationBusinessServiceIF     cService;

  @Override
  public void beforeClassSetup() throws Exception
  {
    super.beforeClassSetup();

    setUpInReq();
  }

  @Request
  private void setUpInReq()
  {
    type = this.cTypeService.apply(ClassificationTypeTest.createMock());

    TEST_GOT.apply();

    Classification root = this.cService.newInstance(type);
    root.setCode(CODE);
    root.setDisplayLabel(new LocalizedValue("Test Classification"));
    this.cService.apply(root, null);

    testClassification = (AttributeClassificationType) AttributeType.factory("testClassification", new LocalizedValue("testClassificationLocalName"), new LocalizedValue("testClassificationLocalDescrip"), AttributeClassificationType.TYPE, false, false, false);
    testClassification.setClassificationType(type.getCode());
    testClassification.setRootTerm(root.toTerm());

    ServerGeoObjectType got = ServerGeoObjectType.get(TEST_GOT.getCode());
    testClassification = (AttributeClassificationType) gotService.createAttributeType(got, testClassification.toJSON().toString());
  }

  @Override
  public void afterClassSetup() throws Exception
  {
    super.afterClassSetup();

    TEST_GOT.delete();

    deleteMdClassification();
  }

  @Request
  private void deleteMdClassification()
  {
    if (type != null)
    {
      this.cTypeService.delete(type);
    }
  }

  @Before
  public void setUp()
  {
    cleanUpExtra();

    testData.setUpInstanceData();

    testData.logIn(FastTestDataset.USER_CGOV_RA);
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
    ListTypeQuery query = new ListTypeQuery(new QueryFactory());

    OIterator<? extends ListType> it = query.getIterator();

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
  public void testCreateGeoObject()
  {
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA };

    for (TestUserInfo user : allowedUsers)
    {
      TestDataSet.runAsUser(user, (request) -> {
        TestDataSet.populateAdapterIds(user, client.getAdapter());

        GeoObject object = TEST_GO.newGeoObject(client.getAdapter());
        object.setValue(testClassification.getName(), CODE);

        GeoObject returned = client.createGeoObject(object.toJSON().toString(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);

        Assert.assertEquals(CODE, returned.getAttribute(testClassification.getName()).getValue());

        TEST_GO.assertApplied();
        TEST_GO.delete();
      });
    }
  }

  @Test
  public void testCreateGeoObjectOverTime()
  {
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA };

    for (TestUserInfo user : allowedUsers)
    {
      TestDataSet.runAsUser(user, (request) -> {
        TestDataSet.populateAdapterIds(user, client.getAdapter());

        GeoObjectOverTime object = TEST_GO.newGeoObjectOverTime(client.getAdapter());
        object.setValue(testClassification.getName(), CODE, TEST_GO.getDate(), ValueOverTime.INFINITY_END_DATE);

        GeoObjectOverTime returned = client.createGeoObjectOverTime(object.toJSON().toString());

        Assert.assertEquals(CODE, returned.getValue(testClassification.getName(), TEST_GO.getDate()));

        TEST_GO.assertApplied();
        TEST_GO.delete();
      });
    }
  }
}
