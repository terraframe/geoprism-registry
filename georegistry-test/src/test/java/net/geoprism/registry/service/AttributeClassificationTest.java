/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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

import java.io.InputStream;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.graph.MdClassificationInfo;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.dataaccess.metadata.graph.MdClassificationDAO;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.AbstractClassification;
import com.runwaysdk.system.scheduler.AllJobStatus;

import net.geoprism.registry.MasterList;
import net.geoprism.registry.MasterListQuery;
import net.geoprism.registry.etl.ETLService;
import net.geoprism.registry.etl.ImportHistory;
import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.SchedulerTestUtils;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestUserInfo;
import net.geoprism.registry.test.USATestData;

public class AttributeClassificationTest
{
  public static final TestGeoObjectInfo      TEST_GO             = new TestGeoObjectInfo(USATestData.TEST_DATA_KEY + "NeverNeverLand", USATestData.STATE);

  protected static String                    CLASSIFICATION_TYPE = "test.classification.TestClassification";

  protected static String                    CODE                = "Classification-ROOT";

  private static USATestData                 testData;

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
    mdClassification.setStructValue(MdClassificationInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, "Test Classification");
    mdClassification.setValue(MdClassificationInfo.GENERATE_SOURCE, MdAttributeBooleanInfo.FALSE);
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
  }

  @AfterClass
  public static void cleanUpClass()
  {
    if (testData != null)
    {
      testData.tearDownMetadata();
    }

    deleteMdClassification();
  }

  @Request
  private static void deleteMdClassification()
  {
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
  public void testCreateGeoObject()
  {
    TestUserInfo[] allowedUsers = new TestUserInfo[] { USATestData.USER_NPS_RA };

    for (TestUserInfo user : allowedUsers)
    {
      TestDataSet.runAsUser(user, (request, adapter) -> {
        TestDataSet.populateAdapterIds(user, adapter);

        GeoObject object = TEST_GO.newGeoObject(adapter);
        object.setValue(testClassification.getName(), CODE);

        GeoObject returned = adapter.createGeoObject(object.toJSON().toString(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);

        Assert.assertEquals(CODE, returned.getAttribute(testClassification.getName()).getValue());

        TEST_GO.assertApplied();
        TEST_GO.delete();
      });
    }
  }

  @Test
  public void testCreateGeoObjectOverTime()
  {
    TestUserInfo[] allowedUsers = new TestUserInfo[] { USATestData.USER_NPS_RA };

    for (TestUserInfo user : allowedUsers)
    {
      TestDataSet.runAsUser(user, (request, adapter) -> {
        TestDataSet.populateAdapterIds(user, adapter);

        GeoObjectOverTime object = TEST_GO.newGeoObjectOverTime(adapter);
        object.setValue(testClassification.getName(), CODE, TEST_GO.getDate(), ValueOverTime.INFINITY_END_DATE);

        GeoObjectOverTime returned = adapter.createGeoObjectOverTime(object.toJSON().toString());

        Assert.assertEquals(CODE, returned.getValue(testClassification.getName(), TEST_GO.getDate()));

        TEST_GO.assertApplied();
        TEST_GO.delete();
      });
    }
  }
}
