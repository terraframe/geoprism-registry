package net.geoprism.registry.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.session.Request;

import net.geoprism.graph.BusinessTypeSnapshot;
import net.geoprism.graph.DirectedAcyclicGraphTypeSnapshot;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.GraphTypeReference;
import net.geoprism.graph.GraphTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.UndirectedGraphTypeSnapshot;
import net.geoprism.registry.Commit;
import net.geoprism.registry.EventDatasetTest;
import net.geoprism.registry.Publish;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.business.CommitBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.PublishBusinessServiceIF;
import net.geoprism.registry.service.business.SnapshotBusinessService;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.USATestData;
import net.geoprism.registry.view.CommitDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class SnapshotBusinessServiceTest extends EventDatasetTest
{
  @Autowired
  private CommitBusinessServiceIF                commitService;

  @Autowired
  private PublishBusinessServiceIF               publishService;

  @Autowired
  private HierarchyTypeSnapshotBusinessServiceIF hService;

  @Autowired
  private SnapshotBusinessService                service;

  protected static Publish                       publish;

  protected static Commit                        commit;

  @Override
  @Before
  @Request
  public void setUp()
  {
    super.setUp();

    publish = publishService.create(this.getPublishDTO());
    commit = this.commitService.create(publish, new CommitDTO(UUID.randomUUID().toString(), publish.getUid(), 1, 10L));
  }

  @Override
  @After
  @Request
  public void tearDown()
  {
    this.commitService.delete(commit);
    this.publishService.delete(publish);

    super.tearDown();
  }

  @Test
  @Request
  public void testCreateRootSnapshot()
  {
    GeoObjectTypeSnapshot root = this.service.createRoot(commit);

    Assert.assertNotNull(root);
    Assert.assertEquals(GeoObjectTypeSnapshot.ROOT, root.getCode());
    Assert.assertTrue(root.getIsRoot());

    GeoObjectTypeSnapshot snapshot = this.commitService.getRootType(commit);

    Assert.assertNotNull(snapshot);
    Assert.assertEquals(root.getOid(), snapshot.getOid());
  }

  @Test
  @Request
  public void testCreateGeoObjectSnapshot()
  {
    GeoObjectTypeSnapshot root = this.service.createRoot(commit);

    Assert.assertNotNull(root);

    List<TestGeoObjectTypeInfo> types = Arrays.asList(USATestData.COUNTRY, USATestData.HEALTH_FACILITY, USATestData.HEALTH_POST);

    for (TestGeoObjectTypeInfo type : types)
    {
      ServerGeoObjectType object = type.getServerObject();

      GeoObjectTypeSnapshot snapshot = this.service.createSnapshot(commit, object, root);

      Assert.assertNotNull(snapshot);
      Assert.assertEquals(object.getCode(), snapshot.getCode());
      Assert.assertEquals(object.getLabel().getValue(), snapshot.getDisplayLabel().getValue());
      Assert.assertEquals(object.getDescription().getValue(), snapshot.getDescription().getValue());
      Assert.assertEquals(object.getOrganizationCode(), snapshot.getOrgCode());
      Assert.assertEquals(object.getOrigin(), snapshot.getOrigin());
      Assert.assertEquals(object.getGeometryType().name(), snapshot.getGeometryType());
      Assert.assertEquals(object.getIsAbstract(), snapshot.getIsAbstract());
      Assert.assertEquals(object.getIsPrivate(), snapshot.getIsPrivate());
      Assert.assertEquals(false, snapshot.getIsRoot());
      Assert.assertEquals(object.getIsPrivate(), snapshot.getIsPrivate());

      if (type.equals(USATestData.HEALTH_POST))
      {
        Assert.assertEquals(object.getSuperType().getCode(), snapshot.getParent().getCode());
      }

      List<AttributeType> attributeTypes = snapshot.getAttributeTypes();

      object.getAttributeMap().forEach((attributeName, attributeType) -> {
        if (!attributeName.equals(DefaultAttribute.GEOMETRY.getName()))
        {
          Optional<AttributeType> optional = attributeTypes.stream().filter(a -> a.getName().equals(attributeName)).findFirst();

          Assert.assertTrue("Unable to find attribute " + attributeName, optional.isPresent());
        }
      });
    }
  }

  @Test
  @Request
  public void testCreateBusinessObjectSnapshot()
  {
    BusinessTypeSnapshot snapshot = this.service.createSnapshot(commit, btype);

    Assert.assertNotNull(snapshot);
    Assert.assertEquals(btype.getCode(), snapshot.getCode());
    Assert.assertEquals(btype.getLabel().getValue(), snapshot.getDisplayLabel().getValue());
    Assert.assertEquals(btype.getOrganization().getCode(), snapshot.getOrgCode());
    Assert.assertEquals(btype.getOrigin(), snapshot.getOrigin());
    Assert.assertEquals(btype.getSequence(), snapshot.getSequence());

    List<AttributeType> attributeTypes = snapshot.getAttributeTypes();

    btype.getAttributeMap().forEach((attributeName, attributeType) -> {
      Optional<AttributeType> optional = attributeTypes.stream().filter(a -> a.getName().equals(attributeName)).findFirst();

      Assert.assertTrue("Unable to find attribute " + attributeName, optional.isPresent());
    });
  }

  @Test
  @Request
  public void testCreateDirectedAcyclicGraphTypeSnapshot()
  {
    DirectedAcyclicGraphTypeSnapshot snapshot = (DirectedAcyclicGraphTypeSnapshot) this.service.createSnapshot(commit, new GraphTypeReference(GraphTypeSnapshot.DIRECTED_ACYCLIC_GRAPH_TYPE, dagType.getCode()), null);

    Assert.assertNotNull(snapshot);
    Assert.assertEquals(dagType.getCode(), snapshot.getCode());
    Assert.assertEquals(dagType.getLabel().getValue(), snapshot.getDisplayLabel().getValue());
    Assert.assertEquals(dagType.getOrigin(), snapshot.getOrigin());
    Assert.assertEquals(dagType.getSequence(), snapshot.getSequence());
  }

  @Test
  @Request
  public void testCreateUndirectedGraphTypeSnapshot()
  {
    UndirectedGraphTypeSnapshot snapshot = (UndirectedGraphTypeSnapshot) this.service.createSnapshot(commit, new GraphTypeReference(GraphTypeSnapshot.UNDIRECTED_GRAPH_TYPE, undirectedType.getCode()), null);

    Assert.assertNotNull(snapshot);
    Assert.assertEquals(undirectedType.getCode(), snapshot.getCode());
    Assert.assertEquals(undirectedType.getLabel().getValue(), snapshot.getDisplayLabel().getValue());
    Assert.assertEquals(undirectedType.getOrigin(), snapshot.getOrigin());
    Assert.assertEquals(undirectedType.getSequence(), snapshot.getSequence());
  }

  @Test
  @Request
  public void testCreateHierarchyTypeSnapshot()
  {
    ServerHierarchyType hierarchy = USATestData.HIER_ADMIN.getServerObject();

    GeoObjectTypeSnapshot root = this.service.createRoot(commit);
    this.service.createSnapshot(commit, USATestData.COUNTRY.getServerObject(), root);

    HierarchyTypeSnapshot snapshot = this.service.createSnapshot(commit, hierarchy, root);

    Assert.assertNotNull(snapshot);
    Assert.assertEquals(hierarchy.getCode(), snapshot.getCode());
    Assert.assertEquals(hierarchy.getLabel().getValue(), snapshot.getDisplayLabel().getValue());
    Assert.assertEquals(hierarchy.getOrigin(), snapshot.getOrigin());
    Assert.assertEquals(hierarchy.getSequence(), snapshot.getSequence());

    List<String> list = this.hService.getChildren(snapshot, root).stream().map(s -> s.getCode()).toList();

    hierarchy.getRootNodes().forEach(type -> {
      Assert.assertTrue("Expected [" + type.getCode() + "]", list.contains(type.getCode()));
    });

  }
}
