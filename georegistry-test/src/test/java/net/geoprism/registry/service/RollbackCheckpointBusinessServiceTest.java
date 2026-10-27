/**
 *
 */
package net.geoprism.registry.service;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.axonframework.eventhandling.TrackingToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Request;

import net.geoprism.registry.EventDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.RollbackCheckpoint;
import net.geoprism.registry.RollbackCheckpoint.Status;
import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.axon.config.RegistryEventStore;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.jobs.GPRJobHistory;
import net.geoprism.registry.service.business.RollbackCheckpointBusinessService;
import net.geoprism.registry.service.business.RollbackEventService;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.USATestData;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
public class RollbackCheckpointBusinessServiceTest extends EventDatasetTest implements InstanceTestClassListener
{
  @Autowired
  private RollbackCheckpointBusinessService service;

  @Autowired
  private RegistryEventStore                store;

  public static GPRJobHistory               history;

  @BeforeEach
  @Override
  @Request
  public void setUp() throws Exception
  {
    history = new GPRJobHistory();
    history.addStage(ImportStage.IMPORT);
    history.apply();

    this.store.truncate();
  }

  @AfterEach
  @Request
  public void tearDown() throws Exception
  {
    if (history != null)
    {
      history.delete();

      history = null;
    }

    testData.tearDownInstanceData();

    this.store.truncate();
  }

  @Test
  @Request
  public void testCreate()
  {
    TrackingToken head = store.createHeadToken();
    long index = head != null ? head.position().orElse(0L) : 0L;

    RollbackCheckpoint checkpoint = this.service.create(history);

    Assertions.assertNotNull(checkpoint);
    Assertions.assertEquals(Long.valueOf(index), checkpoint.getGlobalIndex());
    Assertions.assertEquals(1, this.service.getCount());
  }

  @Test
  @Request
  public void testClear()
  {
    this.service.create(history);

    Assertions.assertEquals(1, this.service.getCount());

    this.service.clear();

    Assertions.assertEquals(0, this.service.getCount());

  }

  @Test
  @Request
  public void testGetAfter() throws InterruptedException
  {
    RollbackCheckpoint first = this.service.create(history, 1L);
    RollbackCheckpoint second = this.service.create(history, 2L);

    Assertions.assertEquals(2, this.service.getCount());

    List<RollbackCheckpoint> list = this.service.getAfter(first);

    Assertions.assertEquals(2, list.size());

    Assertions.assertEquals(second.getOid(), list.get(0).getOid());
    Assertions.assertEquals(first.getOid(), list.get(1).getOid());

    Assertions.assertEquals(1, this.service.getAfter(second).size());
  }

  @Test
  @Request
  public void testGetAll() throws InterruptedException
  {
    this.service.create(history, 1L);
    this.service.create(history, 2L);

    Assertions.assertEquals(2, this.service.getAll(20, 1).size());
    Assertions.assertEquals(0, this.service.getAll(20, 2).size());
    Assertions.assertEquals(1, this.service.getAll(1, 1).size());
  }

  @Test
  @Request
  public void testExecutionCount() throws InterruptedException
  {
    this.service.create(history, 1L, Status.AVAILABLE);
    this.service.create(history, 2L, Status.RUNNING);

    Assertions.assertEquals(1, this.service.getExecutionCount());
  }

  @Test
  @Request
  public void testExecutionList() throws InterruptedException
  {
    this.service.create(history, 1L, Status.AVAILABLE);
    RollbackCheckpoint checkpoint = this.service.create(history, 2L, Status.RUNNING);

    List<RollbackCheckpoint> results = this.service.getExecutionList();

    Assertions.assertEquals(1, results.size());
    Assertions.assertEquals(checkpoint.getOid(), results.get(0).getOid());
  }

  @Test
  @Request
  public void testRollback() throws InterruptedException, ExecutionException
  {
    this.service.create(history, 100000L, Status.AVAILABLE);
    RollbackCheckpoint checkpoint = this.service.create(history, 999999L, Status.AVAILABLE);

    this.service.rollback(checkpoint).get();

    Assertions.assertEquals(1, this.service.getCount());
  }

  @Test
  @Request
  public void testRollbackInProgress() throws InterruptedException, ExecutionException
  {
    assertThrows(ProgrammingErrorException.class, () -> {
      this.service.create(history, 100000L, Status.SCHEDULED);
      RollbackCheckpoint checkpoint = this.service.create(history, 999999L, Status.RUNNING);

      this.service.rollback(checkpoint).get();

      Assertions.assertEquals(1, this.service.getCount());
    });
  }

  @Test
  @Request
  public void testMultiCheckpointRollback() throws InterruptedException, ExecutionException
  {
    RollbackCheckpoint first = this.service.create(history);

    USATestData.USA.apply();

    RollbackCheckpoint second = this.service.create(history);

    USATestData.CANADA.apply();

    this.service.create(history);

    USATestData.CO_A_ONE.apply();

    this.service.create(history);

    USATestData.CO_C_ONE.apply();

    Assertions.assertNotNull(this.gObjectService.getGeoObjectByCode(USATestData.USA.getCode(), USATestData.USA.getGeoObjectType().getCode()));
    Assertions.assertNotNull(this.gObjectService.getGeoObjectByCode(USATestData.CANADA.getCode(), USATestData.CANADA.getGeoObjectType().getCode()));
    Assertions.assertNotNull(this.gObjectService.getGeoObjectByCode(USATestData.CO_A_ONE.getCode(), USATestData.CO_A_ONE.getGeoObjectType().getCode()));
    Assertions.assertNotNull(this.gObjectService.getGeoObjectByCode(USATestData.CO_C_ONE.getCode(), USATestData.CO_C_ONE.getGeoObjectType().getCode()));

    Assertions.assertEquals(Long.valueOf(4), this.store.size());

    Assertions.assertEquals(4, this.service.getCount());

    List<RollbackCheckpoint> list = this.service.getAfter(second);

    Assertions.assertEquals(3, list.size());

    this.service.rollback(second).get();

    Assertions.assertNotNull(this.gObjectService.getGeoObjectByCode(USATestData.USA.getCode(), USATestData.USA.getGeoObjectType().getCode()));
    Assertions.assertNull(this.gObjectService.getGeoObjectByCode(USATestData.CANADA.getCode(), USATestData.CANADA.getGeoObjectType().getCode(), false));
    Assertions.assertNull(this.gObjectService.getGeoObjectByCode(USATestData.CO_A_ONE.getCode(), USATestData.CO_A_ONE.getGeoObjectType().getCode(), false));
    Assertions.assertNull(this.gObjectService.getGeoObjectByCode(USATestData.CO_C_ONE.getCode(), USATestData.CO_C_ONE.getGeoObjectType().getCode(), false));

    Assertions.assertEquals(1, this.service.getCount());

    List<RollbackCheckpoint> checkpoints = this.service.getAll(10, 1);

    Assertions.assertEquals(1, checkpoints.size());
    Assertions.assertEquals(first.getOid(), checkpoints.get(0).getOid());

    Assertions.assertEquals(Long.valueOf(1), this.store.size());
  }

  @Test
  @Request
  public void testMultiCheckpointRollback_2() throws InterruptedException, ExecutionException
  {
    RollbackCheckpoint first = this.service.create(history);

    USATestData.USA.apply();

    RollbackCheckpoint second = this.service.create(history);

    USATestData.CANADA.apply();

    RollbackCheckpoint checkpoint = this.service.create(history);

    USATestData.CO_A_ONE.apply();

    this.service.create(history);

    USATestData.CO_C_ONE.apply();

    Assertions.assertNotNull(this.gObjectService.getGeoObjectByCode(USATestData.USA.getCode(), USATestData.USA.getGeoObjectType().getCode()));
    Assertions.assertNotNull(this.gObjectService.getGeoObjectByCode(USATestData.CANADA.getCode(), USATestData.CANADA.getGeoObjectType().getCode()));
    Assertions.assertNotNull(this.gObjectService.getGeoObjectByCode(USATestData.CO_A_ONE.getCode(), USATestData.CO_A_ONE.getGeoObjectType().getCode()));
    Assertions.assertNotNull(this.gObjectService.getGeoObjectByCode(USATestData.CO_C_ONE.getCode(), USATestData.CO_C_ONE.getGeoObjectType().getCode()));

    Assertions.assertEquals(Long.valueOf(4), this.store.size());

    Assertions.assertEquals(4, this.service.getCount());

    List<RollbackCheckpoint> list = this.service.getAfter(checkpoint);

    Assertions.assertEquals(2, list.size());

    this.service.rollback(checkpoint).get();

    Assertions.assertNotNull(this.gObjectService.getGeoObjectByCode(USATestData.USA.getCode(), USATestData.USA.getGeoObjectType().getCode()));
    Assertions.assertNotNull(this.gObjectService.getGeoObjectByCode(USATestData.CANADA.getCode(), USATestData.CANADA.getGeoObjectType().getCode(), false));
    Assertions.assertNull(this.gObjectService.getGeoObjectByCode(USATestData.CO_A_ONE.getCode(), USATestData.CO_A_ONE.getGeoObjectType().getCode(), false));
    Assertions.assertNull(this.gObjectService.getGeoObjectByCode(USATestData.CO_C_ONE.getCode(), USATestData.CO_C_ONE.getGeoObjectType().getCode(), false));

    Assertions.assertEquals(2, this.service.getCount());

    List<RollbackCheckpoint> checkpoints = this.service.getAll(10, 1);

    Assertions.assertEquals(2, checkpoints.size());
    Assertions.assertTrue(checkpoints.stream().anyMatch(c -> c.getOid().equals(second.getOid())));
    Assertions.assertTrue(checkpoints.stream().anyMatch(c -> c.getOid().equals(first.getOid())));

    Assertions.assertEquals(Long.valueOf(2), this.store.size());
  }

  @Test
  @Request
  public void testChunkLimit() throws InterruptedException, ExecutionException
  {
    RollbackCheckpoint checkpoint = this.service.create(history);

    int limit = RollbackEventService.ROLLBACK_CHUNK + 10;

    for (int i = 0; i < limit; i++)
    {
      TestGeoObjectInfo object = new TestGeoObjectInfo("TEST_00_" + i, USATestData.COUNTRY, USATestData.SOURCE);
      object.apply();
    }

    Assertions.assertEquals(Long.valueOf(RollbackEventService.ROLLBACK_CHUNK + 10), this.store.size());

    this.service.rollback(checkpoint).get();

    for (int i = 0; i < limit; i++)
    {
      Assertions.assertNull(this.gObjectService.getGeoObjectByCode("TEST_00_" + i, USATestData.COUNTRY.getCode(), false));
    }

    Assertions.assertEquals(Long.valueOf(0), this.store.size());
  }

}
