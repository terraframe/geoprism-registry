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
import net.geoprism.registry.Commit;
import net.geoprism.registry.ConceptDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.axon.aggregate.RunwayRequestWrapper;
import net.geoprism.registry.axon.config.RegistryEventStore;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.graph.BusinessEdgeType;
import net.geoprism.registry.graph.BusinessType;
import net.geoprism.registry.graph.ConceptClass;
import net.geoprism.registry.graph.ConceptEdgeType;
import net.geoprism.registry.graph.ConceptSet;
import net.geoprism.registry.graph.DataSource;
import net.geoprism.registry.graph.DirectedAcyclicGraphType;
import net.geoprism.registry.graph.UndirectedGraphType;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.ConceptObject;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentGraphNode;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessEdgeTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessObjectBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.CommitBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptClassBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptObjectBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptSetBusinessServiceIF;
import net.geoprism.registry.service.business.DirectedAcyclicGraphTypeBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.GraphTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.MockRemoteClient;
import net.geoprism.registry.service.business.MockRemoteClientBuilderService;
import net.geoprism.registry.service.business.PublishBusinessServiceIF;
import net.geoprism.registry.service.business.RemoteCommitService;
import net.geoprism.registry.service.business.UndirectedGraphTypeBusinessServiceIF;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.USATestData;
import net.geoprism.registry.view.DiscreteType;
import net.geoprism.registry.view.TypeClass;
import net.geoprism.registry.view.TypeInfo;

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
  private ConceptClassBusinessServiceIF             cClassService;

  @Autowired
  private ConceptEdgeTypeBusinessServiceIF          cEdgeTypeService;

  @Autowired
  private ConceptSetBusinessServiceIF               cSetService;

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
  private ConceptObjectBusinessServiceIF            cObjectService;

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
      this.dagTypeService.getByCode(code).ifPresent(this.dagTypeService::delete);
    });

    Arrays.asList("TEST_UN").forEach(code -> {
      this.undirectedTypeService.getByCode(code).ifPresent(this.undirectedTypeService::delete);
    });

    Arrays.asList("TEST_B_EDGE", "TEST_GEO_EDGE").forEach(code -> {
      this.bEdgeService.getByCode(code).ifPresent(this.bEdgeService::delete);
    });

    Arrays.asList("TEST_BUSINESS").forEach(code -> {
      this.bTypeService.getByCode(code).ifPresent(this.bTypeService::delete);
    });

    testData.tearDownMetadata();

    Arrays.asList("TEST_CONCEPT_SET").forEach(code -> {
      this.cSetService.getByCode(code).ifPresent(this.cSetService::delete);
    });

    Arrays.asList("TEST_CONCEPT_EDGE").forEach(code -> {
      this.cEdgeTypeService.getByCode(code).ifPresent(this.cEdgeTypeService::delete);
    });

    Arrays.asList("TEST_C_CLASS").forEach(code -> {
      this.cClassService.getByCode(code).ifPresent(this.cClassService::delete);
    });

    // Delete all existing publishes
    this.publishService.getAll().stream().forEach(this.publishService::delete);
  }

  @Test
  @Request
  public void testRootDependency() throws InterruptedException
  {
    Assert.assertEquals(Long.valueOf(0), this.store.size());

    Commit commit = this.service.pull(MockRemoteClientBuilderService.SOURCE, PublishEventServiceTest.DEPENDENCY, new LinkedList<>());

    Assert.assertNotNull(commit);

    GeoObjectTypeSnapshot root = this.gSnapshotService.getRoot(commit);

    Assert.assertNotNull(root);

    List<DataSource> sources = this.commitService.getSources(commit);

    Assert.assertEquals(1, sources.size());
    Assert.assertEquals(USATestData.SOURCE.getCode(), sources.get(0).getCode());

    ConceptClass conceptClass = this.cClassService.getByCodeOrThrow(ConceptDatasetTest.CONCEPT_CLASS_CODE);

    Assert.assertEquals(MockRemoteClient.REMOTE_ORIGIN, conceptClass.getOrigin());
    Assert.assertEquals(Long.valueOf(20), conceptClass.getSequence());

    ConceptEdgeType conceptEdge = this.cEdgeTypeService.getByCodeOrThrow(ConceptDatasetTest.CONCEPT_EDGE_CODE);

    Assert.assertEquals(MockRemoteClient.REMOTE_ORIGIN, conceptEdge.getOrigin());
    Assert.assertEquals(Long.valueOf(20), conceptEdge.getSequence());

    ConceptObject concept = this.cObjectService.getByCode(conceptClass, ConceptDatasetTest.PARENT_CONCEPT).orElse(null);

    Assert.assertNotNull(concept);
    Assert.assertNotNull(concept.getValue(DefaultAttribute.DATA_SOURCE.getName()));

    List<ConceptObject> parents = this.cObjectService.getParents(concept, conceptEdge, TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertEquals(1, parents.size());
    Assert.assertEquals(ConceptDatasetTest.ROOT_CONCEPT, parents.get(0).getCode());

    ConceptSet set = this.cSetService.getByCodeOrThrow(ConceptDatasetTest.CONCEPT_SET_CODE);

    Assert.assertEquals(MockRemoteClient.REMOTE_ORIGIN, set.getOrigin());
    Assert.assertEquals(Long.valueOf(20), set.getSequence());

    Assert.assertNotNull(set);
    Assert.assertEquals(DiscreteType.TAXONOMY.name(), set.getDiscreteType());
    Assert.assertEquals(1, this.cSetService.getConceptClasses(set).size());
    Assert.assertEquals(1, this.cSetService.getConceptEdgeTypeEdges(set).size());

    Assert.assertEquals(Long.valueOf(5), this.store.size());
  }

  @Test
  @Request
  public void testPull() throws InterruptedException
  {
    Assert.assertEquals(Long.valueOf(0), this.store.size());

    Commit commit = this.service.pull(MockRemoteClientBuilderService.SOURCE, PublishEventServiceTest.MAIN, new LinkedList<>());

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
      BusinessType type = this.bTypeService.getByCodeOrThrow(code);

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
      GraphTypeSnapshot snapshot = this.graphTypeSnapshotBusinessService.get(commit, TypeClass.DAG.getCode(), code);

      Assert.assertNotNull(snapshot);
      Assert.assertEquals(Long.valueOf(20), snapshot.getSequence());

      // Assert the actual type was created
      Optional<DirectedAcyclicGraphType> optional = this.dagTypeService.getByCode(code);

      Assert.assertTrue(optional.isPresent());

      DirectedAcyclicGraphType type = optional.get();
      Assert.assertEquals(Long.valueOf(20), type.getSequence());
    });

    Arrays.asList("TEST_UN").forEach(code -> {
      GraphTypeSnapshot snapshot = this.graphTypeSnapshotBusinessService.get(commit, TypeClass.UNDIRECTED_GRAPH.getCode(), code);

      Assert.assertNotNull(snapshot);
      Assert.assertEquals(Long.valueOf(20), snapshot.getSequence());

      // Assert the actual type was created
      Optional<UndirectedGraphType> optional = this.undirectedTypeService.getByCode(code);

      Assert.assertTrue(optional.isPresent());

      UndirectedGraphType type = optional.get();
      Assert.assertEquals(Long.valueOf(20), type.getSequence());
    });

    Assert.assertEquals(Long.valueOf(54), this.store.size());

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

    // Assert DAG values
    DirectedAcyclicGraphType dagType = this.dagTypeService.getByCode("TEST_DAG").get();

    ServerParentGraphNode dNode = this.gObjectService.getGraphParentGeoObjects(object, dagType, false, false, TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertEquals(1, dNode.getParents().size());

    BusinessType bType = this.bTypeService.getByCodeOrThrow("TEST_BUSINESS");

    BusinessObject bObject = this.bObjectService.getByCode(bType, "C_CODE").orElse(null);

    Assert.assertNotNull(bObject);
    Assert.assertNotNull(bObject.getValue(DefaultAttribute.DATA_SOURCE.getName()));

    BusinessEdgeType bEdgeType = this.bEdgeService.getByCode("TEST_B_EDGE").get();
    BusinessEdgeType bGeoEdgeType = this.bEdgeService.getByCode("TEST_GEO_EDGE").get();

    Assert.assertEquals(1, this.bObjectService.getParents(bObject, bEdgeType, USATestData.DEFAULT_OVER_TIME_DATE).size());
    Assert.assertEquals(1, this.bObjectService.getParents(bObject, bGeoEdgeType, USATestData.DEFAULT_OVER_TIME_DATE).size());

    ConceptClass conceptClass = this.cClassService.getByCodeOrThrow(ConceptDatasetTest.CONCEPT_CLASS_CODE);
    ConceptObject concept = this.cObjectService.getByCode(conceptClass, ConceptDatasetTest.PARENT_CONCEPT).orElse(null);

    Assert.assertNotNull(concept);
    Assert.assertNotNull(concept.getValue(DefaultAttribute.DATA_SOURCE.getName()));
  }

  @Test
  @Request
  public void testStaleMetadata() throws InterruptedException
  {
    Assert.assertEquals(Long.valueOf(0), this.store.size());

    Commit original = this.service.pull(MockRemoteClientBuilderService.SOURCE, PublishEventServiceTest.MAIN, new LinkedList<>());

    Assert.assertNotNull(original);

    this.service.pull(MockRemoteClientBuilderService.STALE_SOURCE, PublishEventServiceTest.MAIN, new LinkedList<>());

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
      BusinessType type = this.bTypeService.getByCodeOrThrow(code);

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
      Optional<DirectedAcyclicGraphType> optional = this.dagTypeService.getByCode(code);

      Assert.assertTrue(optional.isPresent());

      DirectedAcyclicGraphType type = optional.get();
      Assert.assertEquals(Long.valueOf(20), type.getSequence());
      Assert.assertNotEquals(MockRemoteClientBuilderService.STALE_SOURCE, type.getLabel().getValue());
    });

    Arrays.asList("TEST_UN").forEach(code -> {
      Optional<UndirectedGraphType> optional = this.undirectedTypeService.getByCode(code);

      Assert.assertTrue(optional.isPresent());

      UndirectedGraphType type = optional.get();
      Assert.assertEquals(Long.valueOf(20), type.getSequence());
      Assert.assertNotEquals(MockRemoteClientBuilderService.STALE_SOURCE, type.getLabel().getValue());
    });

    Assert.assertEquals(Long.valueOf(103), this.store.size());
  }

  @Test
  @Request
  public void testExclusions() throws InterruptedException
  {
    Assert.assertEquals(Long.valueOf(0), this.store.size());

    List<TypeInfo> exclusions = Arrays.asList(TypeInfo.build("TEST_UN", TypeClass.UNDIRECTED_GRAPH));

    this.service.pull(MockRemoteClientBuilderService.SOURCE, PublishEventServiceTest.MAIN, exclusions);

    // Ensure that events for excluded types are not executed
    Assert.assertEquals(Long.valueOf(53), this.store.size());
  }

  @Test
  public void testRollback() throws InterruptedException
  {
    RunwayRequestWrapper.run(() -> {
      try
      {
        Assert.assertEquals(Long.valueOf(0), this.store.size());

        // Pull with failure at the end
        Commit commit = this.service.pull(MockRemoteClientBuilderService.ERROR, PublishEventServiceTest.DEPENDENCY, new LinkedList<>());

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
      this.cClassService.getByCodeOrThrow(ConceptDatasetTest.CONCEPT_CLASS_CODE);

      Assert.assertEquals(Long.valueOf(0), this.store.size());

      // Pull again
      Commit commit = this.service.pull(MockRemoteClientBuilderService.SOURCE, PublishEventServiceTest.DEPENDENCY, new LinkedList<>());

      List<DataSource> sources = this.commitService.getSources(commit);

      Assert.assertEquals(1, sources.size());
      Assert.assertEquals(USATestData.SOURCE.getCode(), sources.get(0).getCode());

      ConceptClass conceptClass = this.cClassService.getByCodeOrThrow(ConceptDatasetTest.CONCEPT_CLASS_CODE);

      Assert.assertEquals(MockRemoteClient.REMOTE_ORIGIN, conceptClass.getOrigin());
      Assert.assertEquals(Long.valueOf(20), conceptClass.getSequence());

      ConceptEdgeType conceptEdge = this.cEdgeTypeService.getByCodeOrThrow(ConceptDatasetTest.CONCEPT_EDGE_CODE);

      Assert.assertEquals(MockRemoteClient.REMOTE_ORIGIN, conceptEdge.getOrigin());
      Assert.assertEquals(Long.valueOf(20), conceptEdge.getSequence());

      ConceptObject concept = this.cObjectService.getByCode(conceptClass, ConceptDatasetTest.PARENT_CONCEPT).orElse(null);

      Assert.assertNotNull(concept);
      Assert.assertNotNull(concept.getValue(DefaultAttribute.DATA_SOURCE.getName()));

      List<ConceptObject> parents = this.cObjectService.getParents(concept, conceptEdge, TestDataSet.DEFAULT_OVER_TIME_DATE);

      Assert.assertEquals(1, parents.size());
      Assert.assertEquals(ConceptDatasetTest.ROOT_CONCEPT, parents.get(0).getCode());

      ConceptSet set = this.cSetService.getByCodeOrThrow(ConceptDatasetTest.CONCEPT_SET_CODE);

      Assert.assertEquals(MockRemoteClient.REMOTE_ORIGIN, set.getOrigin());
      Assert.assertEquals(Long.valueOf(20), set.getSequence());

      Assert.assertNotNull(set);
      Assert.assertEquals(DiscreteType.TAXONOMY.name(), set.getDiscreteType());
      Assert.assertEquals(1, this.cSetService.getConceptClasses(set).size());
      Assert.assertEquals(1, this.cSetService.getConceptEdgeTypeEdges(set).size());

      Assert.assertEquals(Long.valueOf(5), this.store.size());
    });
  }
}
