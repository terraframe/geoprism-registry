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
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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
import com.runwaysdk.constants.ComponentInfo;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.database.DuplicateDataDatabaseException;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import net.geoprism.registry.ChangeFrequency;
import net.geoprism.registry.InheritedHierarchyAnnotation;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.MasterListQuery;
import net.geoprism.registry.MasterListVersion;
import net.geoprism.registry.Organization;
import net.geoprism.registry.TileCache;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestHierarchyTypeInfo;
import net.geoprism.registry.test.TestUserInfo;
import net.geoprism.registry.test.USATestData;

public class MasterListInheritedHierarchyTest
{
  private static USATestData testData;

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
    ServerGeoObjectType sGO = USATestData.DISTRICT.getServerObject();
    sGO.setInheritedHierarchy(USATestData.HIER_SCHOOL.getServerObject(), USATestData.HIER_ADMIN.getServerObject());
  }

  @AfterClass
  @Request
  public static void cleanUpClass()
  {
    if (testData != null)
    {
      testData.tearDownMetadata();
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
  public void testPublishVersion()
  {
    JsonObject json = getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_SCHOOL, USATestData.SCHOOL_ZONE, MasterList.PUBLIC, USATestData.COUNTRY, USATestData.STATE, USATestData.DISTRICT);

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
  }

  @Request
  public static JsonObject getJson(Organization org, TestHierarchyTypeInfo ht, TestGeoObjectTypeInfo info, String visibility, TestGeoObjectTypeInfo... parents)
  {
    JsonArray pArray = new JsonArray();
    for (TestGeoObjectTypeInfo parent : parents)
    {
      JsonObject object = new JsonObject();
      object.addProperty("code", parent.getCode());
      object.addProperty("selected", true);

      pArray.add(object);
    }

    JsonObject hierarchy = new JsonObject();
    hierarchy.addProperty("code", ht.getCode());
    hierarchy.add("parents", pArray);

    JsonArray array = new JsonArray();
    array.add(hierarchy);

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
    list.setOrganization(org);
    list.setTelephoneNumber("Telephone Number");
    list.setEmail("Email");
    list.setHierarchies(array.toString());
    list.addFrequency(ChangeFrequency.ANNUAL);
    list.setIsMaster(false);
    list.setVisibility(visibility);

    return list.toJSON();
  }

}
