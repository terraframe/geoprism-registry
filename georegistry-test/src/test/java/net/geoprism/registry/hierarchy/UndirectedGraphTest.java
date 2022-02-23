/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.hierarchy;

import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Request;

import net.geoprism.registry.UndirectedGraphType;
import net.geoprism.registry.model.ServerChildGraphNode;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerParentGraphNode;
import net.geoprism.registry.test.FastTestDataset;

public class UndirectedGraphTest
{
  protected static FastTestDataset     testData;

  protected static UndirectedGraphType type;

  @BeforeClass
  @Request
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();
    testData.setUpMetadata();

    type = UndirectedGraphType.create("TEST_DAG", new LocalizedValue("TEST_DAG"), new LocalizedValue("TEST_DAG"));
  }

  @AfterClass
  @Request
  public static void cleanUpClass()
  {
    if (type != null)
    {
      type.delete();
    }

    testData.tearDownMetadata();
  }

  @Before
  public void setUp()
  {
    testData.setUpInstanceData();

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
  public void testAddParent()
  {
    ServerGeoObjectIF parent = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerGeoObjectIF child = FastTestDataset.PROV_WESTERN.getServerObject();

    ServerParentGraphNode node = child.addGraphParent(parent, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE);

    Assert.assertNotNull(node);

    Assert.assertEquals(child.getCode(), node.getGeoObject().getCode());
    Assert.assertEquals(FastTestDataset.DEFAULT_OVER_TIME_DATE, node.getStartDate());
    Assert.assertEquals(FastTestDataset.DEFAULT_OVER_TIME_DATE, node.getEndDate());

    List<ServerParentGraphNode> parents = node.getParents();

    Assert.assertEquals(1, parents.size());

    Assert.assertEquals(parent.getCode(), parents.get(0).getGeoObject().getCode());
  }

  @Test(expected = ProgrammingErrorException.class)
  @Request
  public void testAddParent_Duplicate()
  {
    ServerGeoObjectIF parent = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerGeoObjectIF child = FastTestDataset.PROV_WESTERN.getServerObject();

    child.addGraphParent(parent, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE);
    parent.addGraphParent(child, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE);
  }

  @Test
  @Request
  public void testGetParentCyclic()
  {
    ServerGeoObjectIF provCentral = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerGeoObjectIF provWestern = FastTestDataset.PROV_WESTERN.getServerObject();
    ServerGeoObjectIF distCentral = FastTestDataset.DIST_CENTRAL.getServerObject();
    ServerGeoObjectIF cambodia = FastTestDataset.CAMBODIA.getServerObject();
    ServerGeoObjectIF privateCentral = FastTestDataset.PROV_CENTRAL_PRIVATE.getServerObject();
    ServerGeoObjectIF centralHospital = FastTestDataset.CENTRAL_HOSPITAL.getServerObject();

    provWestern.addGraphParent(provCentral, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE);
    provCentral.addGraphParent(distCentral, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE);
    provCentral.addGraphParent(cambodia, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE);
    cambodia.addGraphParent(privateCentral, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE);
    cambodia.addGraphParent(centralHospital, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE);
    distCentral.addGraphParent(provWestern, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE);

    ServerParentGraphNode node = provWestern.getGraphParents(type, true, FastTestDataset.DEFAULT_OVER_TIME_DATE);

    Assert.assertNotNull(node);
  }

  @Test
  @Request
  public void testGetChildrenCyclic()
  {
    ServerGeoObjectIF provCentral = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerGeoObjectIF provWestern = FastTestDataset.PROV_WESTERN.getServerObject();
    ServerGeoObjectIF distCentral = FastTestDataset.DIST_CENTRAL.getServerObject();
    ServerGeoObjectIF cambodia = FastTestDataset.CAMBODIA.getServerObject();
    ServerGeoObjectIF privateCentral = FastTestDataset.PROV_CENTRAL_PRIVATE.getServerObject();
    ServerGeoObjectIF centralHospital = FastTestDataset.CENTRAL_HOSPITAL.getServerObject();

    provWestern.addGraphParent(provCentral, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE);
    provCentral.addGraphParent(distCentral, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE);
    provCentral.addGraphParent(cambodia, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE);
    cambodia.addGraphParent(privateCentral, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE);
    cambodia.addGraphParent(centralHospital, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE);
    distCentral.addGraphParent(provWestern, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE);

    ServerChildGraphNode node = provWestern.getGraphChildren(type, true, FastTestDataset.DEFAULT_OVER_TIME_DATE);

    Assert.assertNotNull(node);
  }

  @Test
  @Request
  public void testGetParents()
  {
    ServerGeoObjectIF parent = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerGeoObjectIF child = FastTestDataset.PROV_WESTERN.getServerObject();

    child.addGraphParent(parent, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE);

    ServerParentGraphNode node = child.getGraphParents(type, false, FastTestDataset.DEFAULT_OVER_TIME_DATE);

    List<ServerParentGraphNode> parents = node.getParents();

    Assert.assertEquals(1, parents.size());

    Assert.assertEquals(parent.getCode(), parents.get(0).getGeoObject().getCode());
  }

  @Test
  @Request
  public void testAddMultipleParents()
  {
    ServerGeoObjectIF provCentral = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerGeoObjectIF provWestern = FastTestDataset.PROV_WESTERN.getServerObject();
    ServerGeoObjectIF distCentral = FastTestDataset.DIST_CENTRAL.getServerObject();

    provCentral.addGraphParent(provWestern, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE);
    provCentral.addGraphParent(distCentral, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE);

    ServerParentGraphNode node = provCentral.getGraphParents(type, false, FastTestDataset.DEFAULT_OVER_TIME_DATE);

    List<ServerParentGraphNode> parents = node.getParents();

    Assert.assertEquals(2, parents.size());
  }

  @Test(expected = ProgrammingErrorException.class)
  @Request
  public void testAddSameParents()
  {
    ServerGeoObjectIF provCentral = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerGeoObjectIF provWestern = FastTestDataset.PROV_WESTERN.getServerObject();

    provCentral.addGraphParent(provWestern, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE);
    provCentral.addGraphParent(provWestern, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE);

    Assert.fail("Able to add the same overlapping parent multiple times");
  }

  @Test
  @Request
  public void testAddChild()
  {
    ServerGeoObjectIF parent = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerGeoObjectIF child = FastTestDataset.PROV_WESTERN.getServerObject();

    ServerParentGraphNode node = parent.addGraphChild(child, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE);

    Assert.assertNotNull(node);

    Assert.assertEquals(child.getCode(), node.getGeoObject().getCode());
    Assert.assertEquals(FastTestDataset.DEFAULT_OVER_TIME_DATE, node.getStartDate());
    Assert.assertEquals(FastTestDataset.DEFAULT_OVER_TIME_DATE, node.getEndDate());

    List<ServerParentGraphNode> parents = node.getParents();

    Assert.assertEquals(1, parents.size());

    Assert.assertEquals(parent.getCode(), parents.get(0).getGeoObject().getCode());
  }

  @Test
  @Request
  public void testGetChildren()
  {
    ServerGeoObjectIF parent = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerGeoObjectIF child = FastTestDataset.PROV_WESTERN.getServerObject();

    parent.addGraphChild(child, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);

    ServerChildGraphNode node = parent.getGraphChildren(type, false, FastTestDataset.DEFAULT_END_TIME_DATE);

    List<ServerChildGraphNode> children = node.getChildren();

    Assert.assertEquals(1, children.size());

    Assert.assertEquals(child.getCode(), children.get(0).getGeoObject().getCode());
  }

  @Test
  @Request
  public void testAddMultipleChildren()
  {
    ServerGeoObjectIF provCentral = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerGeoObjectIF provWestern = FastTestDataset.PROV_WESTERN.getServerObject();
    ServerGeoObjectIF distCentral = FastTestDataset.DIST_CENTRAL.getServerObject();

    provCentral.addGraphChild(provWestern, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);
    provCentral.addGraphChild(distCentral, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);

    ServerChildGraphNode node = provCentral.getGraphChildren(type, false, FastTestDataset.DEFAULT_END_TIME_DATE);

    List<ServerChildGraphNode> children = node.getChildren();

    Assert.assertEquals(2, children.size());
  }

  @Test
  @Request
  public void testRemoveChild()
  {
    ServerGeoObjectIF provCentral = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerGeoObjectIF provWestern = FastTestDataset.PROV_WESTERN.getServerObject();

    provCentral.addGraphChild(provWestern, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);
    provCentral.removeGraphChild(provWestern, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);

    ServerChildGraphNode node = provCentral.getGraphChildren(type, false, FastTestDataset.DEFAULT_END_TIME_DATE);

    List<ServerChildGraphNode> children = node.getChildren();

    Assert.assertEquals(0, children.size());
  }

}
