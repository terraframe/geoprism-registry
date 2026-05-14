/**
 *
 */
package net.geoprism.registry.service;

import java.util.List;

import org.axonframework.eventhandling.TrackingToken;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Request;

import net.geoprism.registry.EventDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.RollbackCheckpoint;
import net.geoprism.registry.RollbackCheckpoint.Status;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.axon.config.RegistryEventStore;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.jobs.GPRJobHistory;
import net.geoprism.registry.service.business.RollbackCheckpointBusinessService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class RollbackCheckpointBusinessServiceTest extends EventDatasetTest implements InstanceTestClassListener
{
  @Autowired
  private RollbackCheckpointBusinessService service;

  @Autowired
  private RegistryEventStore                store;

  public static GPRJobHistory               history;

  @Override
  @Request

  public void setUp()
  {
    history = new GPRJobHistory();
    history.addStage(ImportStage.IMPORT);
    history.apply();
  }

  @After
  @Request
  public void tearDown()
  {
    if (history != null)
    {
      history.delete();

      history = null;
    }

    if (pObject != null)
    {
      this.bObjectService.delete(pObject);

      pObject = null;
    }

    if (cObject != null)
    {
      this.bObjectService.delete(cObject);

      cObject = null;
    }

    this.store.truncate();

    this.projection.clearCache();

    testData.tearDownInstanceData();
  }

  @Test
  @Request
  public void testCreate()
  {
    TrackingToken head = store.createHeadToken();
    long index = head != null ? head.position().orElse(0L) : 0L;

    RollbackCheckpoint checkpoint = this.service.create(history);

    Assert.assertNotNull(checkpoint);
    Assert.assertEquals(Long.valueOf(index), checkpoint.getGlobalIndex());
    Assert.assertEquals(1, this.service.getCount());
  }

  @Test
  @Request
  public void testClear()
  {
    this.service.create(history);

    Assert.assertEquals(1, this.service.getCount());

    this.service.clear();

    Assert.assertEquals(0, this.service.getCount());

  }

  @Test
  @Request
  public void testGetAfter() throws InterruptedException
  {
    RollbackCheckpoint first = this.service.create(history, 1L);
    RollbackCheckpoint second = this.service.create(history, 2L);

    Assert.assertEquals(2, this.service.getCount());
    Assert.assertEquals(2, this.service.getAfter(first).size());
    Assert.assertEquals(1, this.service.getAfter(second).size());
  }

  @Test
  @Request
  public void testGetAll() throws InterruptedException
  {
    this.service.create(history, 1L);
    this.service.create(history, 2L);

    Assert.assertEquals(2, this.service.getAll(20, 1).size());
    Assert.assertEquals(0, this.service.getAll(20, 2).size());
    Assert.assertEquals(1, this.service.getAll(1, 1).size());
  }

  @Test
  @Request
  public void testExecutionCount() throws InterruptedException
  {
    this.service.create(history, 1L, Status.AVAILABLE);
    this.service.create(history, 2L, Status.RUNNING);

    Assert.assertEquals(1, this.service.getExecutionCount());
  }

  @Test
  @Request
  public void testExecutionList() throws InterruptedException
  {
    this.service.create(history, 1L, Status.AVAILABLE);
    RollbackCheckpoint checkpoint = this.service.create(history, 2L, Status.RUNNING);

    List<RollbackCheckpoint> results = this.service.getExecutionList();

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(checkpoint.getOid(), results.get(0).getOid());
  }

  @Test
  @Request
  public void testRollback() throws InterruptedException
  {
    this.service.create(history, 100000L, Status.AVAILABLE);
    RollbackCheckpoint checkpoint = this.service.create(history, 999999L, Status.AVAILABLE);

    this.service.rollback(checkpoint);

    Assert.assertEquals(1, this.service.getCount());
  }

  @Test(expected = ProgrammingErrorException.class)
  @Request
  public void testRollbackInProgress() throws InterruptedException
  {
    this.service.create(history, 100000L, Status.SCHEDULED);
    RollbackCheckpoint checkpoint = this.service.create(history, 999999L, Status.RUNNING);

    this.service.rollback(checkpoint);

    Assert.assertEquals(1, this.service.getCount());
  }

}
