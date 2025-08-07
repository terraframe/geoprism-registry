/**
 *
 */
package net.geoprism.registry.hierarchy;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.session.Request;

import net.geoprism.registry.DirectedAcyclicGraphType;
import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.model.ServerChildGraphNode;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerParentGraphNode;
import net.geoprism.registry.service.business.DirectedAcyclicGraphTypeBusinessServiceIF;
import net.geoprism.registry.test.FastTestDataset;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class DirectedAcyclicGraphTest extends FastDatasetTest implements InstanceTestClassListener
{
  protected static DirectedAcyclicGraphType         type;

  @Autowired
  private DirectedAcyclicGraphTypeBusinessServiceIF service;

  @Override
  @Request
  public void beforeClassSetup() throws Exception
  {
    super.beforeClassSetup();

    type = this.service.create("TEST_DAG", new LocalizedValue("TEST_DAG"), new LocalizedValue("TEST_DAG"), 0L);
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
    if (type != null)
    {
      this.service.delete(type);
    }

    super.afterClassSetup();
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

    ServerParentGraphNode node = child.addGraphParent(parent, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), true);

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
  public void testAddParentCyclic()
  {
    ServerGeoObjectIF provCentral = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerGeoObjectIF provWestern = FastTestDataset.PROV_WESTERN.getServerObject();
    ServerGeoObjectIF distCentral = FastTestDataset.DIST_CENTRAL.getServerObject();
    ServerGeoObjectIF cambodia = FastTestDataset.CAMBODIA.getServerObject();
    ServerGeoObjectIF privateCentral = FastTestDataset.PROV_CENTRAL_PRIVATE.getServerObject();
    ServerGeoObjectIF centralHospital = FastTestDataset.CENTRAL_HOSPITAL.getServerObject();

    provWestern.addGraphParent(provCentral, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), true);
    provCentral.addGraphParent(distCentral, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), true);
    provCentral.addGraphParent(cambodia, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), true);
    cambodia.addGraphParent(privateCentral, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), true);
    cambodia.addGraphParent(centralHospital, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), true);
    distCentral.addGraphParent(provWestern, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), true);
  }

  @Test
  @Request
  public void testAddParentCyclic_DifferentTimes()
  {
    ServerGeoObjectIF provCentral = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerGeoObjectIF provWestern = FastTestDataset.PROV_WESTERN.getServerObject();
    ServerGeoObjectIF distCentral = FastTestDataset.DIST_CENTRAL.getServerObject();
    ServerGeoObjectIF cambodia = FastTestDataset.CAMBODIA.getServerObject();
    ServerGeoObjectIF privateCentral = FastTestDataset.PROV_CENTRAL_PRIVATE.getServerObject();
    ServerGeoObjectIF centralHospital = FastTestDataset.CENTRAL_HOSPITAL.getServerObject();

    provWestern.addGraphParent(provCentral, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), true);
    provCentral.addGraphParent(distCentral, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), true);
    provCentral.addGraphParent(cambodia, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), true);
    cambodia.addGraphParent(privateCentral, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), true);
    cambodia.addGraphParent(centralHospital, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), true);
    distCentral.addGraphParent(provWestern, type, new Date(), ValueOverTime.INFINITY_END_DATE, UUID.randomUUID().toString(), true);

    ServerParentGraphNode node = provWestern.getGraphParents(type, false, FastTestDataset.DEFAULT_OVER_TIME_DATE);

    Assert.assertEquals(1, node.getParents().size());
  }

  @Test
  @Request
  public void testGetParents()
  {
    ServerGeoObjectIF parent = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerGeoObjectIF child = FastTestDataset.PROV_WESTERN.getServerObject();

    child.addGraphParent(parent, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), true);

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

    provCentral.addGraphParent(provWestern, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), true);
    provCentral.addGraphParent(distCentral, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), true);

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

    provCentral.addGraphParent(provWestern, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), true);
    provCentral.addGraphParent(provWestern, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), true);

    Assert.fail("Able to add the same overlapping parent multiple times");
  }

  @Test
  @Request
  public void testAddChild()
  {
    ServerGeoObjectIF parent = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerGeoObjectIF child = FastTestDataset.PROV_WESTERN.getServerObject();

    ServerParentGraphNode node = parent.addGraphChild(child, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), true);

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

    parent.addGraphChild(child, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, UUID.randomUUID().toString(), true);

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

    provCentral.addGraphChild(provWestern, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, UUID.randomUUID().toString(), true);
    provCentral.addGraphChild(distCentral, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, UUID.randomUUID().toString(), true);

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

    provCentral.addGraphChild(provWestern, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, UUID.randomUUID().toString(), true);
    provCentral.removeGraphChild(provWestern, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);

    ServerChildGraphNode node = provCentral.getGraphChildren(type, false, FastTestDataset.DEFAULT_END_TIME_DATE);

    List<ServerChildGraphNode> children = node.getChildren();

    Assert.assertEquals(0, children.size());
  }

}
