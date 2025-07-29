/**
 *
 */
package net.geoprism.registry.service;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.session.Request;

import net.geoprism.graph.BusinessEdgeTypeSnapshot;
import net.geoprism.graph.BusinessTypeSnapshot;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.GraphTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.Commit;
import net.geoprism.registry.DirectedAcyclicGraphType;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.UndirectedGraphType;
import net.geoprism.registry.axon.config.RegistryEventStore;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessEdgeTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.CommitBusinessServiceIF;
import net.geoprism.registry.service.business.DirectedAcyclicGraphTypeBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.GraphTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.MockRemoteClientBuilderService;
import net.geoprism.registry.service.business.PublishBusinessServiceIF;
import net.geoprism.registry.service.business.RemoteClientBuilderServiceIF;
import net.geoprism.registry.service.business.RemoteClientIF;
import net.geoprism.registry.service.business.RemoteCommitService;
import net.geoprism.registry.service.business.UndirectedGraphTypeBusinessServiceIF;
import net.geoprism.registry.test.USATestData;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class RemoteCommitServiceTest implements InstanceTestClassListener
{
  @Autowired
  private RemoteClientBuilderServiceIF              builder;

  @Autowired
  private CommitBusinessServiceIF                   commitService;

  @Autowired
  private GeoObjectTypeSnapshotBusinessServiceIF    gSnapshotService;

  @Autowired
  private HierarchyTypeSnapshotBusinessServiceIF    hSnapshotService;

  @Autowired
  private BusinessTypeSnapshotBusinessServiceIF     bTypeSnapshotService;

  @Autowired
  private BusinessEdgeTypeSnapshotBusinessServiceIF bEdgeSnapshotService;

  @Autowired
  private GraphTypeSnapshotBusinessServiceIF        graphTypeSnapshotBusinessService;

  @Autowired
  private PublishBusinessServiceIF                  publishService;

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF         bEdgeService;

  @Autowired
  private BusinessTypeBusinessServiceIF             bTypeService;

  @Autowired
  private DirectedAcyclicGraphTypeBusinessServiceIF dagTypeService;

  @Autowired
  private UndirectedGraphTypeBusinessServiceIF      undirectedTypeService;

  @Autowired
  private RemoteCommitService                       service;

  @Autowired
  private RegistryEventStore                        store;

  protected static USATestData                      testData;

  @Override
  public void beforeClassSetup() throws Exception
  {

  }

  @Override
  public void afterClassSetup() throws Exception
  {
  }

  @Before
  @Request
  public void before()
  {
    this.store.truncate();

    testData = USATestData.newTestData();

    testData.getManagedOrganizations().forEach(org -> {
      org.apply();
    });

    // Delete the existing commit
    try (RemoteClientIF client = builder.open(MockRemoteClientBuilderService.SORUCE))
    {
      client.getPublish("").ifPresent(dto -> {
        this.publishService.getByUid(dto.getUid()).ifPresent(publish -> {
          this.publishService.delete(publish);
        });
      });
    }
  }

  @Before
  @Request
  public void after()
  {

    Arrays.asList("TEST_DAG").forEach(code -> {
      DirectedAcyclicGraphType.getByCode(code).ifPresent(this.dagTypeService::delete);
    });

    Arrays.asList("TEST_UN").forEach(code -> {
      UndirectedGraphType.getByCode(code).ifPresent(this.undirectedTypeService::delete);
    });

    Arrays.asList("TEST_B_EDGE", "TEST_GEO_EDGE").forEach(code -> {
      this.bEdgeService.getByCode(code).ifPresent(this.bEdgeService::delete);
    });

    Arrays.asList("TEST_BUSINESS").forEach(code -> {
      BusinessType type = this.bTypeService.getByCode(code);

      if (type != null)
      {
        this.bTypeService.delete(type);
      }
    });

    if (testData != null)
    {
      testData.tearDownMetadata();
    }
  }

  @Test
  @Request
  public void testPull() throws InterruptedException
  {
    Assert.assertEquals(Long.valueOf(0), this.store.size());

    Commit commit = this.service.pull(MockRemoteClientBuilderService.SORUCE, "mock", 1);

    try
    {
      Assert.assertNotNull(commit);

      GeoObjectTypeSnapshot root = this.gSnapshotService.getRoot(commit);

      Assert.assertNotNull(root);

      testData.getManagedGeoObjectTypes().stream().map(t -> t.getCode()).forEach(code -> {
        GeoObjectTypeSnapshot snapshot = this.gSnapshotService.get(commit, code);

        Assert.assertNotNull(snapshot);
        Assert.assertEquals(Long.valueOf(20), snapshot.getSequence());

        // Assert the actual type was created
        ServerGeoObjectType type = ServerGeoObjectType.get(code, true);

        Assert.assertNotNull(type);
        Assert.assertEquals(Long.valueOf(20), type.getSequence());
      });

      testData.getManagedHierarchyTypes().stream().map(t -> t.getCode()).forEach(code -> {
        HierarchyTypeSnapshot snapshot = this.hSnapshotService.get(commit, code);

        Assert.assertNotNull(snapshot);
        Assert.assertEquals(Long.valueOf(20), snapshot.getSequence());
        Assert.assertTrue(this.hSnapshotService.getChildren(snapshot, root).size() > 0);

        // Assert the actual type was created
        ServerHierarchyType type = ServerHierarchyType.get(code, true);

        Assert.assertNotNull(type);
        Assert.assertEquals(Long.valueOf(20), type.getSequence());
      });

      Arrays.asList("TEST_BUSINESS").forEach(code -> {
        BusinessTypeSnapshot snapshot = this.bTypeSnapshotService.get(commit, code);

        Assert.assertNotNull(snapshot);
        Assert.assertEquals(Long.valueOf(20), snapshot.getSequence());

        // Assert the actual type was created
        BusinessType type = this.bTypeService.getByCode(code);

        Assert.assertNotNull(type);
        Assert.assertEquals(Long.valueOf(20), type.getSequence());
      });

      Arrays.asList("TEST_B_EDGE", "TEST_GEO_EDGE").forEach(code -> {
        BusinessEdgeTypeSnapshot snapshot = this.bEdgeSnapshotService.get(commit, code);

        Assert.assertNotNull(snapshot);
        Assert.assertEquals(Long.valueOf(20), snapshot.getSequence());

        // Assert the actual type was created
        Optional<BusinessEdgeType> optional = this.bEdgeService.getByCode(code);

        Assert.assertTrue(optional.isPresent());

        BusinessEdgeType type = optional.get();
        Assert.assertEquals(Long.valueOf(20), type.getSequence());
      });

      Arrays.asList("TEST_DAG").forEach(code -> {
        GraphTypeSnapshot snapshot = this.graphTypeSnapshotBusinessService.get(commit, GraphTypeSnapshot.DIRECTED_ACYCLIC_GRAPH_TYPE, code);

        Assert.assertNotNull(snapshot);
        Assert.assertEquals(Long.valueOf(20), snapshot.getSequence());

        // Assert the actual type was created
        Optional<DirectedAcyclicGraphType> optional = DirectedAcyclicGraphType.getByCode(code);

        Assert.assertTrue(optional.isPresent());

        DirectedAcyclicGraphType type = optional.get();
        Assert.assertEquals(Long.valueOf(20), type.getSequence());
      });

      Arrays.asList("TEST_UN").forEach(code -> {
        GraphTypeSnapshot snapshot = this.graphTypeSnapshotBusinessService.get(commit, GraphTypeSnapshot.UNDIRECTED_GRAPH_TYPE, code);

        Assert.assertNotNull(snapshot);
        Assert.assertEquals(Long.valueOf(20), snapshot.getSequence());

        // Assert the actual type was created
        Optional<UndirectedGraphType> optional = UndirectedGraphType.getByCode(code);

        Assert.assertTrue(optional.isPresent());

        UndirectedGraphType type = optional.get();
        Assert.assertEquals(Long.valueOf(20), type.getSequence());
      });

      Assert.assertEquals(Long.valueOf(47), this.store.size());
    }
    finally
    {
      this.commitService.delete(commit);
    }
  }

  @Test
  @Request
  public void testSkipMetadata() throws InterruptedException
  {
    Assert.assertEquals(Long.valueOf(0), this.store.size());

    Commit original = this.service.pull(MockRemoteClientBuilderService.SORUCE, "mock", 1);

    try
    {
      Assert.assertNotNull(original);

      Commit outOfDate = this.service.pull(MockRemoteClientBuilderService.SKIP_METADATA, "mock", 2);

      try
      {

        GeoObjectTypeSnapshot root = this.gSnapshotService.getRoot(original);

        Assert.assertNotNull(root);

        testData.getManagedGeoObjectTypes().stream().map(t -> t.getCode()).forEach(code -> {
          ServerGeoObjectType type = ServerGeoObjectType.get(code, true);

          Assert.assertNotNull(type);
          Assert.assertEquals(Long.valueOf(20), type.getSequence());
          Assert.assertNotEquals(MockRemoteClientBuilderService.SKIP_METADATA, type.getLabel().getValue());
        });

        testData.getManagedHierarchyTypes().stream().map(t -> t.getCode()).forEach(code -> {
          ServerHierarchyType type = ServerHierarchyType.get(code, true);

          Assert.assertNotNull(type);
          Assert.assertEquals(Long.valueOf(20), type.getSequence());
          Assert.assertNotEquals(MockRemoteClientBuilderService.SKIP_METADATA, type.getLabel().getValue());
        });

        Arrays.asList("TEST_BUSINESS").forEach(code -> {
          BusinessType type = this.bTypeService.getByCode(code);

          Assert.assertNotNull(type);
          Assert.assertEquals(Long.valueOf(20), type.getSequence());
          Assert.assertNotEquals(MockRemoteClientBuilderService.SKIP_METADATA, type.getLabel().getValue());
        });

        Arrays.asList("TEST_B_EDGE", "TEST_GEO_EDGE").forEach(code -> {
          Optional<BusinessEdgeType> optional = this.bEdgeService.getByCode(code);

          Assert.assertTrue(optional.isPresent());

          BusinessEdgeType type = optional.get();
          Assert.assertEquals(Long.valueOf(20), type.getSequence());
          Assert.assertNotEquals(MockRemoteClientBuilderService.SKIP_METADATA, type.getLabel().getValue());
        });

        Arrays.asList("TEST_DAG").forEach(code -> {
          Optional<DirectedAcyclicGraphType> optional = DirectedAcyclicGraphType.getByCode(code);

          Assert.assertTrue(optional.isPresent());

          DirectedAcyclicGraphType type = optional.get();
          Assert.assertEquals(Long.valueOf(20), type.getSequence());
          Assert.assertNotEquals(MockRemoteClientBuilderService.SKIP_METADATA, type.getLabel().getValue());
        });

        Arrays.asList("TEST_UN").forEach(code -> {
          Optional<UndirectedGraphType> optional = UndirectedGraphType.getByCode(code);

          Assert.assertTrue(optional.isPresent());

          UndirectedGraphType type = optional.get();
          Assert.assertEquals(Long.valueOf(20), type.getSequence());
          Assert.assertNotEquals(MockRemoteClientBuilderService.SKIP_METADATA, type.getLabel().getValue());
        });

        Assert.assertEquals(Long.valueOf(94), this.store.size());
      }
      finally
      {
        this.commitService.delete(outOfDate);
      }
    }
    finally
    {
      this.commitService.delete(original);
    }
  }
}
