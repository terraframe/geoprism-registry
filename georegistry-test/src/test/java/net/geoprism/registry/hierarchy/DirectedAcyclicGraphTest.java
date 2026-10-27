/**
 *
 */
package net.geoprism.registry.hierarchy;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.ValueOverTimeDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.SchedulerManager;

import net.geoprism.GenericException;
import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.etl.upload.EdgeObjectImportConfiguration;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.DirectedAcyclicGraphType;
import net.geoprism.registry.jobs.ImportHistory;
import net.geoprism.registry.model.ServerChildGraphNode;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerParentGraphNode;
import net.geoprism.registry.service.business.DirectedAcyclicGraphTypeBusinessServiceIF;
import net.geoprism.registry.service.business.EdgeImportTestService;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.SchedulerTestUtils;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.view.ImportHistoryView;
import net.geoprism.registry.view.TypeClass;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
public class DirectedAcyclicGraphTest extends FastDatasetTest implements InstanceTestClassListener
{
  protected static DirectedAcyclicGraphType         type;

  @Autowired
  private DirectedAcyclicGraphTypeBusinessServiceIF service;

  @Autowired
  private EdgeImportTestService                     importService;

  @Autowired
  private GeoObjectBusinessServiceIF                objectService;

  @Override
  @Request
  public void beforeClassSetup() throws Exception
  {
    super.beforeClassSetup();

    type = this.service.create("TEST_DAG", new LocalizedValue("TEST_DAG"), new LocalizedValue("TEST_DAG"), 0L);

    if (!SchedulerManager.initialized())
    {
      SchedulerManager.start();
    }

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

  @Test
  @Request
  public void testAddParent()
  {
    ServerGeoObjectIF parent = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerGeoObjectIF child = FastTestDataset.PROV_WESTERN.getServerObject();
    String uid = UUID.randomUUID().toString();

    child.addGraphParent(parent, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, uid, FastTestDataset.SOURCE.getDataSource(), true);

    ServerParentGraphNode node = child.getGraphParents(type, false, FastTestDataset.DEFAULT_OVER_TIME_DATE);

    Assertions.assertNotNull(node);

    Assertions.assertEquals(child.getCode(), node.getGeoObject().getCode());

    List<ServerParentGraphNode> parents = node.getParents();

    Assertions.assertEquals(1, parents.size());

    ServerParentGraphNode pNode = parents.get(0);
    Assertions.assertEquals(parent.getCode(), pNode.getGeoObject().getCode());
    Assertions.assertEquals(FastTestDataset.SOURCE.getCode(), pNode.getSource().getCode());
    Assertions.assertEquals(uid, pNode.getUid());
    Assertions.assertEquals(FastTestDataset.DEFAULT_OVER_TIME_DATE, pNode.getStartDate());
    Assertions.assertEquals(FastTestDataset.DEFAULT_OVER_TIME_DATE, pNode.getEndDate());
  }

  @Test
  @Request
  public void testUpdateParent()
  {
    ServerGeoObjectIF parent = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerGeoObjectIF child = FastTestDataset.PROV_WESTERN.getServerObject();
    String uid = UUID.randomUUID().toString();

    child.addGraphParent(parent, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, uid, FastTestDataset.SOURCE.getDataSource(), true);
    child.addGraphParent(parent, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, uid, null, false);

    ServerParentGraphNode node = child.getGraphParents(type, false, FastTestDataset.DEFAULT_OVER_TIME_DATE);

    Assertions.assertNotNull(node);

    Assertions.assertEquals(child.getCode(), node.getGeoObject().getCode());

    List<ServerParentGraphNode> parents = node.getParents();

    Assertions.assertEquals(1, parents.size());

    ServerParentGraphNode pNode = parents.get(0);
    Assertions.assertEquals(parent.getCode(), pNode.getGeoObject().getCode());
    Assertions.assertNull(pNode.getSource());
    Assertions.assertEquals(uid, pNode.getUid());
    Assertions.assertEquals(FastTestDataset.DEFAULT_OVER_TIME_DATE, pNode.getStartDate());
    Assertions.assertEquals(FastTestDataset.DEFAULT_OVER_TIME_DATE, pNode.getEndDate());
  }

  @Test
  @Request
  public void testAddParentCyclic()
  {
    assertThrows(GenericException.class, () -> {
      ServerGeoObjectIF provCentral = FastTestDataset.PROV_CENTRAL.getServerObject();
      ServerGeoObjectIF provWestern = FastTestDataset.PROV_WESTERN.getServerObject();
      ServerGeoObjectIF distCentral = FastTestDataset.DIST_CENTRAL.getServerObject();
      ServerGeoObjectIF cambodia = FastTestDataset.CAMBODIA.getServerObject();
      ServerGeoObjectIF privateCentral = FastTestDataset.PROV_CENTRAL_PRIVATE.getServerObject();
      ServerGeoObjectIF centralHospital = FastTestDataset.CENTRAL_HOSPITAL.getServerObject();

      provWestern.addGraphParent(provCentral, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), FastTestDataset.SOURCE.getDataSource(), true);
      provCentral.addGraphParent(distCentral, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), FastTestDataset.SOURCE.getDataSource(), true);
      provCentral.addGraphParent(cambodia, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), FastTestDataset.SOURCE.getDataSource(), true);
      cambodia.addGraphParent(privateCentral, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), FastTestDataset.SOURCE.getDataSource(), true);
      cambodia.addGraphParent(centralHospital, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), FastTestDataset.SOURCE.getDataSource(), true);
      distCentral.addGraphParent(provWestern, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), FastTestDataset.SOURCE.getDataSource(), true);
    });
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

    provWestern.addGraphParent(provCentral, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), FastTestDataset.SOURCE.getDataSource(), true);
    provCentral.addGraphParent(distCentral, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), FastTestDataset.SOURCE.getDataSource(), true);
    provCentral.addGraphParent(cambodia, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), FastTestDataset.SOURCE.getDataSource(), true);
    cambodia.addGraphParent(privateCentral, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), FastTestDataset.SOURCE.getDataSource(), true);
    cambodia.addGraphParent(centralHospital, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), FastTestDataset.SOURCE.getDataSource(), true);
    distCentral.addGraphParent(provWestern, type, new Date(), ValueOverTimeDTO.INFINITY_END_DATE, UUID.randomUUID().toString(), FastTestDataset.SOURCE.getDataSource(), true);

    ServerParentGraphNode node = provWestern.getGraphParents(type, false, FastTestDataset.DEFAULT_OVER_TIME_DATE);

    Assertions.assertEquals(1, node.getParents().size());
  }

  @Test
  @Request
  public void testGetParents()
  {
    ServerGeoObjectIF parent = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerGeoObjectIF child = FastTestDataset.PROV_WESTERN.getServerObject();

    child.addGraphParent(parent, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), FastTestDataset.SOURCE.getDataSource(), true);

    ServerParentGraphNode node = child.getGraphParents(type, false, FastTestDataset.DEFAULT_OVER_TIME_DATE);

    List<ServerParentGraphNode> parents = node.getParents();

    Assertions.assertEquals(1, parents.size());

    Assertions.assertEquals(parent.getCode(), parents.get(0).getGeoObject().getCode());
  }

  @Test
  @Request
  public void testAddMultipleParents()
  {
    ServerGeoObjectIF provCentral = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerGeoObjectIF provWestern = FastTestDataset.PROV_WESTERN.getServerObject();
    ServerGeoObjectIF distCentral = FastTestDataset.DIST_CENTRAL.getServerObject();

    provCentral.addGraphParent(provWestern, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), FastTestDataset.SOURCE.getDataSource(), true);
    provCentral.addGraphParent(distCentral, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), FastTestDataset.SOURCE.getDataSource(), true);

    ServerParentGraphNode node = provCentral.getGraphParents(type, false, FastTestDataset.DEFAULT_OVER_TIME_DATE);

    List<ServerParentGraphNode> parents = node.getParents();

    Assertions.assertEquals(2, parents.size());
  }

  @Test
  @Request
  public void testAddSameParents()
  {
    assertThrows(GenericException.class, () -> {
      ServerGeoObjectIF provCentral = FastTestDataset.PROV_CENTRAL.getServerObject();
      ServerGeoObjectIF provWestern = FastTestDataset.PROV_WESTERN.getServerObject();

      provCentral.addGraphParent(provWestern, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), FastTestDataset.SOURCE.getDataSource(), true);
      provCentral.addGraphParent(provWestern, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, UUID.randomUUID().toString(), FastTestDataset.SOURCE.getDataSource(), true);

      Assertions.fail("Able to add the same overlapping parent multiple times");
    });
  }

  @Test
  @Request
  public void testAddChild()
  {
    ServerGeoObjectIF parent = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerGeoObjectIF child = FastTestDataset.PROV_WESTERN.getServerObject();
    String uid = UUID.randomUUID().toString();

    parent.addGraphChild(child, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, uid, FastTestDataset.SOURCE.getDataSource(), true);

    ServerParentGraphNode node = child.getGraphParents(type, false, FastTestDataset.DEFAULT_OVER_TIME_DATE);

    Assertions.assertNotNull(node);

    Assertions.assertEquals(child.getCode(), node.getGeoObject().getCode());

    List<ServerParentGraphNode> parents = node.getParents();

    Assertions.assertEquals(1, parents.size());

    ServerParentGraphNode pNode = parents.get(0);

    Assertions.assertEquals(parent.getCode(), pNode.getGeoObject().getCode());

    Assertions.assertEquals(FastTestDataset.SOURCE.getCode(), pNode.getSource().getCode());
    Assertions.assertEquals(uid, pNode.getUid());
    Assertions.assertEquals(FastTestDataset.DEFAULT_OVER_TIME_DATE, pNode.getStartDate());
    Assertions.assertEquals(FastTestDataset.DEFAULT_OVER_TIME_DATE, pNode.getEndDate());
  }

  @Test
  @Request
  public void testGetChildren()
  {
    ServerGeoObjectIF parent = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerGeoObjectIF child = FastTestDataset.PROV_WESTERN.getServerObject();

    parent.addGraphChild(child, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, UUID.randomUUID().toString(), FastTestDataset.SOURCE.getDataSource(), true);

    ServerChildGraphNode node = parent.getGraphChildren(type, false, FastTestDataset.DEFAULT_END_TIME_DATE);

    List<ServerChildGraphNode> children = node.getChildren();

    Assertions.assertEquals(1, children.size());

    Assertions.assertEquals(child.getCode(), children.get(0).getGeoObject().getCode());
  }

  @Test
  @Request
  public void testAddMultipleChildren()
  {
    ServerGeoObjectIF provCentral = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerGeoObjectIF provWestern = FastTestDataset.PROV_WESTERN.getServerObject();
    ServerGeoObjectIF distCentral = FastTestDataset.DIST_CENTRAL.getServerObject();

    provCentral.addGraphChild(provWestern, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, UUID.randomUUID().toString(), FastTestDataset.SOURCE.getDataSource(), true);
    provCentral.addGraphChild(distCentral, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, UUID.randomUUID().toString(), FastTestDataset.SOURCE.getDataSource(), true);

    ServerChildGraphNode node = provCentral.getGraphChildren(type, false, FastTestDataset.DEFAULT_END_TIME_DATE);

    List<ServerChildGraphNode> children = node.getChildren();

    Assertions.assertEquals(2, children.size());
  }

  @Test
  @Request
  public void testRemoveChild()
  {
    ServerGeoObjectIF provCentral = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerGeoObjectIF provWestern = FastTestDataset.PROV_WESTERN.getServerObject();

    provCentral.addGraphChild(provWestern, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, UUID.randomUUID().toString(), FastTestDataset.SOURCE.getDataSource(), true);
    provCentral.removeGraphChild(provWestern, type, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);

    ServerChildGraphNode node = provCentral.getGraphChildren(type, false, FastTestDataset.DEFAULT_END_TIME_DATE);

    List<ServerChildGraphNode> children = node.getChildren();

    Assertions.assertEquals(0, children.size());
  }

  @Test
  public void testEdgeObjectImport() throws InterruptedException
  {
    TestDataSet.executeRequestAsUser(FastTestDataset.USER_ADMIN, () -> {
      InputStream istream = this.importService.generateEdgeJson(FastTestDataset.PROV_CENTRAL, FastTestDataset.PROV_WESTERN);

      Assertions.assertNotNull(istream);

      EdgeObjectImportConfiguration config = this.importService.getTestConfiguration(TypeClass.DAG.getCode(), type.getCode(), istream, ImportStrategy.NEW_AND_UPDATE);

      ImportHistory hist = this.importService.importJsonFile(config.toDTO());

      try
      {
        SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

        List<ImportHistoryView> histories = this.importService.getHistory(TypeClass.DAG.getCode(), type.getCode());

        Assertions.assertEquals(1, histories.size());

        ServerGeoObjectIF sourceObject = this.objectService.getGeoObjectByCode(FastTestDataset.PROV_CENTRAL.getCode(), FastTestDataset.PROV_CENTRAL.getGeoObjectType().getCode());
        ServerChildGraphNode node = sourceObject.getGraphChildren(type, false, TestDataSet.DEFAULT_OVER_TIME_DATE);

        List<ServerChildGraphNode> children = node.getChildren();

        Assertions.assertEquals(1, children.size());

      }
      finally
      {
        ImportHistory.get(hist.getOid()).delete();
      }
    });
  }

}
