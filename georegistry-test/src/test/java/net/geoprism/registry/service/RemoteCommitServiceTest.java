/**
 *
 */
package net.geoprism.registry.service;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.junit.After;
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
import net.geoprism.registry.axon.aggregate.RunwayRequestWrapper;
import net.geoprism.registry.axon.config.RegistryEventStore;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.graph.DataSource;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.EdgeDirection;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessEdgeTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessObjectBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.CommitBusinessServiceIF;
import net.geoprism.registry.service.business.DirectedAcyclicGraphTypeBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.GraphTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.MockDependentRemoteClient;
import net.geoprism.registry.service.business.MockRemoteClientBuilderService;
import net.geoprism.registry.service.business.PublishBusinessServiceIF;
import net.geoprism.registry.service.business.RemoteCommitService;
import net.geoprism.registry.service.business.UndirectedGraphTypeBusinessServiceIF;
import net.geoprism.registry.test.USATestData;
import net.geoprism.registry.view.TypeAndCode;
import net.geoprism.registry.view.TypeAndCode.Type;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class RemoteCommitServiceTest implements InstanceTestClassListener
{
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
  private GeoObjectBusinessServiceIF                gObjectService;

  @Autowired
  private BusinessObjectBusinessServiceIF           bObjectService;

  @Autowired
  private RemoteCommitService                       service;

  @Autowired
  private RegistryEventStore                        store;

  protected USATestData                             testData;

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
    testData.getManagedOrganizations().forEach(org -> org.apply());
    // testData.getManagedSources().forEach(source -> source.apply());

    // Delete all existing publishes
    this.publishService.getAll().stream().forEach(this.publishService::delete);
  }

  @After
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

    testData.tearDownMetadata();
  }

  @Test
  @Request
  public void testPull() throws InterruptedException
  {
    Assert.assertEquals(Long.valueOf(0), this.store.size());

    Commit commit = this.service.pull(MockRemoteClientBuilderService.SOURCE, "mock", new LinkedList<>());

    try
    {
      Assert.assertNotNull(commit);

      GeoObjectTypeSnapshot root = this.gSnapshotService.getRoot(commit);

      Assert.assertNotNull(root);

      List<DataSource> sources = this.commitService.getSources(commit);

      Assert.assertEquals(1, sources.size());
      Assert.assertEquals(USATestData.SOURCE.getCode(), sources.get(0).getCode());

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

      // Test Object values

      ServerGeoObjectIF object = this.gObjectService.getGeoObjectByCode(USATestData.COLORADO.getCode(), USATestData.STATE.getCode());

      Assert.assertNotNull(object);
      Assert.assertNotNull(object.getValue(DefaultAttribute.DATA_SOURCE.getName(), USATestData.DEFAULT_OVER_TIME_DATE));

      ServerParentTreeNode nodes = this.gObjectService.getParentGeoObjects(object, USATestData.HIER_ADMIN.getServerObject(), null, false, false, USATestData.DEFAULT_OVER_TIME_DATE);

      List<ServerParentTreeNode> parents = nodes.getParents();

      Assert.assertEquals(1, parents.size());

      ServerParentTreeNode node = parents.get(0);

      Assert.assertNotNull(node.getSource());
      Assert.assertEquals(USATestData.SOURCE.getCode(), node.getSource().getCode());
      Assert.assertNotNull(node.getUid());

      BusinessType bType = this.bTypeService.getByCode("TEST_BUSINESS");

      BusinessObject bObject = this.bObjectService.getByCode(bType, "C_CODE");

      Assert.assertNotNull(bObject);
      Assert.assertNotNull(bObject.getObjectValue(DefaultAttribute.DATA_SOURCE.getName()));

      BusinessEdgeType bEdgeType = this.bEdgeService.getByCode("TEST_B_EDGE").get();
      BusinessEdgeType bGeoEdgeType = this.bEdgeService.getByCode("TEST_GEO_EDGE").get();

      Assert.assertEquals(1, this.bObjectService.getParents(bObject, bEdgeType).size());
      Assert.assertEquals(1, this.bObjectService.getGeoObjects(bObject, bGeoEdgeType, EdgeDirection.PARENT).size());
    }
    finally
    {
      this.publishService.delete(commit.getPublish());
    }
  }

  @Test
  @Request
  public void testStaleMetadata() throws InterruptedException
  {
    Assert.assertEquals(Long.valueOf(0), this.store.size());

    Commit original = this.service.pull(MockRemoteClientBuilderService.SOURCE, "mock", new LinkedList<>());

    try
    {
      Assert.assertNotNull(original);

      this.service.pull(MockRemoteClientBuilderService.STALE_SOURCE, "mock", new LinkedList<>());

      GeoObjectTypeSnapshot root = this.gSnapshotService.getRoot(original);

      Assert.assertNotNull(root);

      testData.getManagedGeoObjectTypes().stream().map(t -> t.getCode()).forEach(code -> {
        ServerGeoObjectType type = ServerGeoObjectType.get(code, true);

        Assert.assertNotNull(type);
        Assert.assertEquals(Long.valueOf(20), type.getSequence());
        Assert.assertNotEquals(MockRemoteClientBuilderService.STALE_SOURCE, type.getLabel().getValue());
      });

      testData.getManagedHierarchyTypes().stream().map(t -> t.getCode()).forEach(code -> {
        ServerHierarchyType type = ServerHierarchyType.get(code, true);

        Assert.assertNotNull(type);
        Assert.assertEquals(Long.valueOf(20), type.getSequence());
        Assert.assertNotEquals(MockRemoteClientBuilderService.STALE_SOURCE, type.getLabel().getValue());
      });

      Arrays.asList("TEST_BUSINESS").forEach(code -> {
        BusinessType type = this.bTypeService.getByCode(code);

        Assert.assertNotNull(type);
        Assert.assertEquals(Long.valueOf(20), type.getSequence());
        Assert.assertNotEquals(MockRemoteClientBuilderService.STALE_SOURCE, type.getLabel().getValue());
      });

      Arrays.asList("TEST_B_EDGE", "TEST_GEO_EDGE").forEach(code -> {
        Optional<BusinessEdgeType> optional = this.bEdgeService.getByCode(code);

        Assert.assertTrue(optional.isPresent());

        BusinessEdgeType type = optional.get();
        Assert.assertEquals(Long.valueOf(20), type.getSequence());
        Assert.assertNotEquals(MockRemoteClientBuilderService.STALE_SOURCE, type.getLabel().getValue());
      });

      Arrays.asList("TEST_DAG").forEach(code -> {
        Optional<DirectedAcyclicGraphType> optional = DirectedAcyclicGraphType.getByCode(code);

        Assert.assertTrue(optional.isPresent());

        DirectedAcyclicGraphType type = optional.get();
        Assert.assertEquals(Long.valueOf(20), type.getSequence());
        Assert.assertNotEquals(MockRemoteClientBuilderService.STALE_SOURCE, type.getLabel().getValue());
      });

      Arrays.asList("TEST_UN").forEach(code -> {
        Optional<UndirectedGraphType> optional = UndirectedGraphType.getByCode(code);

        Assert.assertTrue(optional.isPresent());

        UndirectedGraphType type = optional.get();
        Assert.assertEquals(Long.valueOf(20), type.getSequence());
        Assert.assertNotEquals(MockRemoteClientBuilderService.STALE_SOURCE, type.getLabel().getValue());
      });

      Assert.assertEquals(Long.valueOf(94), this.store.size());
    }
    finally
    {
      this.publishService.delete(original.getPublish());
    }
  }

  @Test
  @Request
  public void testPullDependency() throws InterruptedException
  {
    Assert.assertEquals(Long.valueOf(0), this.store.size());

    Commit commit = this.service.pull(MockRemoteClientBuilderService.DEPENDENCY, MockDependentRemoteClient.DEPENDENT, new LinkedList<>());

    try
    {
      Assert.assertNotNull(commit);
      Assert.assertEquals(MockDependentRemoteClient.DEPENDENT, commit.getUid());

      List<Commit> dependencies = this.commitService.getDependencies(commit);
      Assert.assertEquals(MockDependentRemoteClient.DEPENDENCY, dependencies.get(0).getUid());

      try
      {
        Assert.assertEquals(1, dependencies.size());
        Assert.assertEquals(1, dependencies.size());

        testData.getManagedGeoObjectTypes().stream().map(t -> t.getCode()).forEach(code -> {
          Assert.assertNotNull(ServerGeoObjectType.get(code, true));
        });

        testData.getManagedHierarchyTypes().stream().map(t -> t.getCode()).forEach(code -> {
          Assert.assertNotNull(ServerHierarchyType.get(code, true));
        });

        Arrays.asList("TEST_BUSINESS").forEach(code -> {
          Assert.assertNotNull(this.bTypeService.getByCode(code));
        });

        Arrays.asList("TEST_B_EDGE", "TEST_GEO_EDGE").forEach(code -> {
          Assert.assertTrue(this.bEdgeService.getByCode(code).isPresent());
        });

        Arrays.asList("TEST_DAG").forEach(code -> {
          Assert.assertTrue(DirectedAcyclicGraphType.getByCode(code).isPresent());
        });

        Arrays.asList("TEST_UN").forEach(code -> {
          Assert.assertTrue(UndirectedGraphType.getByCode(code).isPresent());
        });

        Assert.assertEquals(Long.valueOf(47), this.store.size());
      }
      finally
      {
        dependencies.stream().map(m -> m.getPublish()).distinct().forEach(this.publishService::delete);
      }

    }
    finally
    {
      this.publishService.delete(commit.getPublish());
    }
  }

  @Test
  @Request
  public void testExclusions() throws InterruptedException
  {
    Assert.assertEquals(Long.valueOf(0), this.store.size());

    List<TypeAndCode> exclusions = Arrays.asList(TypeAndCode.build("TEST_UN", Type.UNDIRECTED));

    Commit commit = this.service.pull(MockRemoteClientBuilderService.SOURCE, "mock", exclusions);

    try
    {
      // Ensure that events for excluded types are not executed
      Assert.assertEquals(Long.valueOf(46), this.store.size());
    }
    finally
    {
      this.publishService.delete(commit.getPublish());
    }
  }

  @Test
  public void testRollback() throws InterruptedException
  {
    RunwayRequestWrapper.run(() -> {
      try
      {
        // Pull with failure at the end
        Commit commit = this.service.pull(MockRemoteClientBuilderService.ERROR, "mock", new LinkedList<>());

        Assert.assertNull(commit);

        Assert.fail();
      }
      catch (Exception e)
      {
        // This is expected
        // e.printStackTrace();
      }
    });

    RunwayRequestWrapper.run(() -> {
      // Validate that the failed commit had events
      Assert.assertNotNull(this.gObjectService.getGeoObjectByCode(USATestData.COLORADO.getCode(), USATestData.STATE.getCode()));

      // Pull again
      Commit commit = this.service.pull(MockRemoteClientBuilderService.SOURCE, "mock", new LinkedList<>());

      try
      {
        Assert.assertNotNull(commit);

        GeoObjectTypeSnapshot root = this.gSnapshotService.getRoot(commit);

        Assert.assertNotNull(root);

        List<DataSource> sources = this.commitService.getSources(commit);

        Assert.assertEquals(1, sources.size());
        Assert.assertEquals(USATestData.SOURCE.getCode(), sources.get(0).getCode());

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

        // Test Object values
        ServerGeoObjectIF object = this.gObjectService.getGeoObjectByCode(USATestData.COLORADO.getCode(), USATestData.STATE.getCode());

        Assert.assertNotNull(object);
        Assert.assertNotNull(object.getValue(DefaultAttribute.DATA_SOURCE.getName(), USATestData.DEFAULT_OVER_TIME_DATE));

        ServerParentTreeNode nodes = this.gObjectService.getParentGeoObjects(object, USATestData.HIER_ADMIN.getServerObject(), null, false, false, USATestData.DEFAULT_OVER_TIME_DATE);

        List<ServerParentTreeNode> parents = nodes.getParents();

        Assert.assertEquals(1, parents.size());

        ServerParentTreeNode node = parents.get(0);

        Assert.assertNotNull(node.getSource());
        Assert.assertEquals(USATestData.SOURCE.getCode(), node.getSource().getCode());
        Assert.assertNotNull(node.getUid());

        BusinessType bType = this.bTypeService.getByCode("TEST_BUSINESS");

        BusinessObject bObject = this.bObjectService.getByCode(bType, "C_CODE");

        Assert.assertNotNull(bObject);
        Assert.assertNotNull(bObject.getObjectValue(DefaultAttribute.DATA_SOURCE.getName()));

        BusinessEdgeType bEdgeType = this.bEdgeService.getByCode("TEST_B_EDGE").get();
        BusinessEdgeType bGeoEdgeType = this.bEdgeService.getByCode("TEST_GEO_EDGE").get();

        Assert.assertEquals(1, this.bObjectService.getParents(bObject, bEdgeType).size());
        Assert.assertEquals(1, this.bObjectService.getGeoObjects(bObject, bGeoEdgeType, EdgeDirection.PARENT).size());
      }
      finally
      {
        this.publishService.delete(commit.getPublish());
      }
    });
  }
}
