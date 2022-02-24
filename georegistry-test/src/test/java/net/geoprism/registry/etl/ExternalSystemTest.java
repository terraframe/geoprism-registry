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
package net.geoprism.registry.etl;

import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.session.Request;

import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.RevealExternalSystem;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.query.ServerExternalIdRestriction;
import net.geoprism.registry.query.graph.VertexGeoObjectQuery;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;

public class ExternalSystemTest
{
  protected static FastTestDataset testData;

  public static final String       EXTERNAL_SYSTEM_ID = "ExternalSystemTest";

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

    TestDataSet.deleteExternalSystems(EXTERNAL_SYSTEM_ID);

    testData.logIn(FastTestDataset.USER_CGOV_RA);
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    testData.tearDownInstanceData();
  }

  @Test
  @Request
  public void testCreateRevealExternalSystem()
  {
    RevealExternalSystem system = new RevealExternalSystem();
    system.setId(EXTERNAL_SYSTEM_ID);
    system.setOrganization(FastTestDataset.ORG_CGOV.getServerObject());
    system.getEmbeddedComponent(ExternalSystem.LABEL).setValue("defaultLocale", "Test");
    system.getEmbeddedComponent(ExternalSystem.DESCRIPTION).setValue("defaultLocale", "Test");
    system.apply();

    ExternalSystem test = ExternalSystem.get(system.getOid());

    Assert.assertEquals(system.getId(), test.getId());
  }

  @Test
  @Request
  public void testAddExternalId()
  {
    RevealExternalSystem system = new RevealExternalSystem();
    system.setId(EXTERNAL_SYSTEM_ID);
    system.setOrganization(FastTestDataset.ORG_CGOV.getServerObject());
    system.getEmbeddedComponent(ExternalSystem.LABEL).setValue("defaultLocale", "Test");
    system.getEmbeddedComponent(ExternalSystem.DESCRIPTION).setValue("defaultLocale", "Test");
    system.apply();

    String expected = "EXTERNAL ID";

    ServerGeoObjectIF serverGO = FastTestDataset.PROV_CENTRAL.getServerObject();

    serverGO.createExternalId(system, expected, ImportStrategy.NEW_ONLY);

    String actual = serverGO.getExternalId(system);

    Assert.assertEquals(expected, actual);
  }

  @Test
  @Request
  public void testVertexExternalIdRestriction()
  {
    RevealExternalSystem system = new RevealExternalSystem();
    system.setId(EXTERNAL_SYSTEM_ID);
    system.setOrganization(FastTestDataset.ORG_CGOV.getServerObject());
    system.getEmbeddedComponent(ExternalSystem.LABEL).setValue("defaultLocale", "Test");
    system.getEmbeddedComponent(ExternalSystem.DESCRIPTION).setValue("defaultLocale", "Test");
    system.apply();

    String externalId = "EXTERNAL ID";

    ServerGeoObjectIF serverGO = FastTestDataset.PROV_CENTRAL.getServerObject();

    serverGO.createExternalId(system, externalId, ImportStrategy.NEW_ONLY);

    VertexGeoObjectQuery query = new VertexGeoObjectQuery(FastTestDataset.PROVINCE.getServerObject(), new Date());
    query.setRestriction(new ServerExternalIdRestriction(system, externalId));

    ServerGeoObjectIF result = query.getSingleResult();

    Assert.assertNotNull(result);
    Assert.assertEquals(serverGO.getCode(), result.getCode());
  }

}
