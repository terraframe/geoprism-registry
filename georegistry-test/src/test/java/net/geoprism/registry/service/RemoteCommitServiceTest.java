/**
 *
 */
package net.geoprism.registry.service;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.session.Request;

import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.GraphTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
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
  @Request
  public void beforeClassSetup() throws Exception
  {
    this.store.truncate();

    testData = USATestData.newTestData();

    testData.getManagedOrganizations().forEach(org -> {
      org.apply();
    });

    // Delete the existing commit
    try (RemoteClientIF client = builder.open(""))
    {
      client.getPublish("").ifPresent(dto -> {
        this.publishService.getByUid(dto.getUid()).ifPresent(publish -> {
          // Ensure that the commit has not already been pulled
          this.commitService.getCommit(publish, 1).ifPresent(commit -> {
            this.commitService.delete(commit);
          });
        });
      });
    }
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
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

  @Before
  @Request
  public void after()
  {
  }

  @Test
  @Request
  public void testPull() throws InterruptedException
  {
    Assert.assertEquals(Long.valueOf(0), this.store.size());

    Commit commit = this.service.pull("test", "mock", 1);

    try
    {
      Assert.assertNotNull(commit);

      GeoObjectTypeSnapshot root = this.gSnapshotService.getRoot(commit);

      Assert.assertNotNull(root);

      testData.getManagedGeoObjectTypes().stream().map(t -> t.getCode()).forEach(code -> {
        Assert.assertNotNull(this.gSnapshotService.get(commit, code));
        
        // Assert the actual type was created
        Assert.assertNotNull(ServerGeoObjectType.get(code, true));
      });

      testData.getManagedHierarchyTypes().stream().map(t -> t.getCode()).forEach(code -> {
        HierarchyTypeSnapshot type = this.hSnapshotService.get(commit, code);

        Assert.assertNotNull(type);

        List<GeoObjectTypeSnapshot> children = this.hSnapshotService.getChildren(type, root);

        Assert.assertTrue(children.size() > 0);
        
        // Assert the actual type was created
        Assert.assertNotNull(ServerHierarchyType.get(code, true));        
      });

      Arrays.asList("TEST_BUSINESS").forEach(code -> {
        Assert.assertNotNull(this.bTypeSnapshotService.get(commit, code));
        
        // Assert the actual type was created
        Assert.assertNotNull(this.bTypeService.getByCode(code));
      });

      Arrays.asList("TEST_B_EDGE", "TEST_GEO_EDGE").forEach(code -> {
        Assert.assertNotNull(this.bEdgeSnapshotService.get(commit, code));
        
        // Assert the actual type was created
        Assert.assertTrue(this.bEdgeService.getByCode(code).isPresent());
      });

      Arrays.asList("TEST_DAG").forEach(code -> {
        Assert.assertNotNull(this.graphTypeSnapshotBusinessService.get(commit, GraphTypeSnapshot.DIRECTED_ACYCLIC_GRAPH_TYPE, code));
        
        // Assert the actual type was created
        Assert.assertTrue(DirectedAcyclicGraphType.getByCode(code).isPresent());
      });

      Arrays.asList("TEST_UN").forEach(code -> {
        Assert.assertNotNull(this.graphTypeSnapshotBusinessService.get(commit, GraphTypeSnapshot.UNDIRECTED_GRAPH_TYPE, code));
                
        // Assert the actual type was created
        Assert.assertTrue(UndirectedGraphType.getByCode(code).isPresent());
      });

      Assert.assertEquals(Long.valueOf(47), this.store.size());

    }
    finally
    {
      this.commitService.delete(commit);
    }
  }
}
