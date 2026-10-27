package net.geoprism.registry.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.session.Request;

import net.geoprism.graph.BusinessTypeSnapshot;
import net.geoprism.graph.ConceptClassSnapshot;
import net.geoprism.graph.DirectedAcyclicGraphTypeSnapshot;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.GraphTypeReference;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.UndirectedGraphTypeSnapshot;
import net.geoprism.registry.Commit;
import net.geoprism.registry.EventDatasetTest;
import net.geoprism.registry.Publish;
import net.geoprism.registry.InstanceTestClassExtension;
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
import net.geoprism.registry.view.TypeClass;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
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
  @BeforeEach
  @Request
  public void setUp() throws Exception
  {
    super.setUp();

    publish = publishService.create(this.getPublishDTO());
    commit = this.commitService.create(publish, new CommitDTO(UUID.randomUUID().toString(), publish.getUid(), 1, 10L));
  }

  @Override
  @AfterEach
  @Request
  public void tearDown() throws Exception
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

    Assertions.assertNotNull(root);
    Assertions.assertEquals(GeoObjectTypeSnapshot.ROOT, root.getCode());
    Assertions.assertTrue(root.getIsRoot());

    GeoObjectTypeSnapshot snapshot = this.commitService.getRootType(commit);

    Assertions.assertNotNull(snapshot);
    Assertions.assertEquals(root.getOid(), snapshot.getOid());
  }

  @Test
  @Request
  public void testCreateGeoObjectSnapshot()
  {
    GeoObjectTypeSnapshot root = this.service.createRoot(commit);

    Assertions.assertNotNull(root);

    List<TestGeoObjectTypeInfo> types = Arrays.asList(USATestData.COUNTRY, USATestData.HEALTH_FACILITY, USATestData.HEALTH_POST);

    for (TestGeoObjectTypeInfo type : types)
    {
      ServerGeoObjectType object = type.getServerObject();

      GeoObjectTypeSnapshot snapshot = this.service.createSnapshot(commit, object, root);

      Assertions.assertNotNull(snapshot);
      Assertions.assertEquals(object.getCode(), snapshot.getCode());
      Assertions.assertEquals(object.getLabel().getValue(), snapshot.getDisplayLabel().getValue());
      Assertions.assertEquals(object.getDescription().getValue(), snapshot.getDescription().getValue());
      Assertions.assertEquals(object.getOrganizationCode(), snapshot.getOrgCode());
      Assertions.assertEquals(object.getOrigin(), snapshot.getOrigin());
      Assertions.assertEquals(object.getGeometryType().name(), snapshot.getGeometryType());
      Assertions.assertEquals(object.getIsAbstract(), snapshot.getIsAbstract());
      Assertions.assertEquals(object.getIsPrivate(), snapshot.getIsPrivate());
      Assertions.assertEquals(false, snapshot.getIsRoot());
      Assertions.assertEquals(object.getIsPrivate(), snapshot.getIsPrivate());

      if (type.equals(USATestData.HEALTH_POST))
      {
        Assertions.assertEquals(object.getSuperType().getCode(), snapshot.getParent().getCode());
      }

      List<AttributeType> attributeTypes = snapshot.getAttributeTypes();

      object.getAttributeMap().forEach((attributeName, attributeType) -> {
        if (!attributeName.equals(DefaultAttribute.GEOMETRY.getName()))
        {
          Optional<AttributeType> optional = attributeTypes.stream().filter(a -> a.getCode().equals(attributeName)).findFirst();

          Assertions.assertTrue(optional.isPresent(), "Unable to find attribute " + attributeName);
        }
      });
    }
  }

  @Test
  @Request
  public void testCreateBusinessObjectSnapshot()
  {
    BusinessTypeSnapshot snapshot = this.service.createSnapshot(commit, btype);

    Assertions.assertNotNull(snapshot);
    Assertions.assertEquals(btype.getCode(), snapshot.getCode());
    Assertions.assertEquals(btype.getLabel().getValue(), snapshot.getDisplayLabel().getValue());
    Assertions.assertEquals(btype.getOrganization().getCode(), snapshot.getOrgCode());
    Assertions.assertEquals(btype.getOrigin(), snapshot.getOrigin());
    Assertions.assertEquals(btype.getSequence(), snapshot.getSequence());

    List<AttributeType> attributeTypes = snapshot.getAttributeTypes();

    btype.getAttributeMap().forEach((attributeName, attributeType) -> {
      Optional<AttributeType> optional = attributeTypes.stream().filter(a -> a.getCode().equals(attributeName)).findFirst();

      Assertions.assertTrue(optional.isPresent(), "Unable to find attribute " + attributeName);
    });
  }

  @Test
  @Request
  public void testCreateConceptObjectSnapshot()
  {
    ConceptClassSnapshot snapshot = this.service.createSnapshot(commit, cClass);

    Assertions.assertNotNull(snapshot);
    Assertions.assertEquals(cClass.getCode(), snapshot.getCode());
    Assertions.assertEquals(cClass.getLabel().getValue(), snapshot.getDisplayLabel().getValue());
    Assertions.assertEquals(cClass.getOrganization().getCode(), snapshot.getOrgCode());
    Assertions.assertEquals(cClass.getOrigin(), snapshot.getOrigin());
    Assertions.assertEquals(cClass.getSequence(), snapshot.getSequence());

    List<AttributeType> attributeTypes = snapshot.getAttributeTypes();

    cClass.getAttributeMap().forEach((attributeName, attributeType) -> {
      Optional<AttributeType> optional = attributeTypes.stream().filter(a -> a.getCode().equals(attributeName)).findFirst();

      Assertions.assertTrue(optional.isPresent(), "Unable to find attribute " + attributeName);
    });
  }

  @Test
  @Request
  public void testCreateDirectedAcyclicGraphTypeSnapshot()
  {
    DirectedAcyclicGraphTypeSnapshot snapshot = (DirectedAcyclicGraphTypeSnapshot) this.service.createSnapshot(commit, new GraphTypeReference(TypeClass.DAG.getCode(), dagType.getCode()), null);

    Assertions.assertNotNull(snapshot);
    Assertions.assertEquals(dagType.getCode(), snapshot.getCode());
    Assertions.assertEquals(dagType.getLabel().getValue(), snapshot.getDisplayLabel().getValue());
    Assertions.assertEquals(dagType.getOrigin(), snapshot.getOrigin());
    Assertions.assertEquals(dagType.getSequence(), snapshot.getSequence());
  }

  @Test
  @Request
  public void testCreateUndirectedGraphTypeSnapshot()
  {
    UndirectedGraphTypeSnapshot snapshot = (UndirectedGraphTypeSnapshot) this.service.createSnapshot(commit, new GraphTypeReference(TypeClass.UNDIRECTED_GRAPH.getCode(), undirectedType.getCode()), null);

    Assertions.assertNotNull(snapshot);
    Assertions.assertEquals(undirectedType.getCode(), snapshot.getCode());
    Assertions.assertEquals(undirectedType.getLabel().getValue(), snapshot.getDisplayLabel().getValue());
    Assertions.assertEquals(undirectedType.getOrigin(), snapshot.getOrigin());
    Assertions.assertEquals(undirectedType.getSequence(), snapshot.getSequence());
  }

  @Test
  @Request
  public void testCreateHierarchyTypeSnapshot()
  {
    ServerHierarchyType hierarchy = USATestData.HIER_ADMIN.getServerObject();

    GeoObjectTypeSnapshot root = this.service.createRoot(commit);
    this.service.createSnapshot(commit, USATestData.COUNTRY.getServerObject(), root);

    HierarchyTypeSnapshot snapshot = this.service.createSnapshot(commit, hierarchy, root);

    Assertions.assertNotNull(snapshot);
    Assertions.assertEquals(hierarchy.getCode(), snapshot.getCode());
    Assertions.assertEquals(hierarchy.getLabel().getValue(), snapshot.getDisplayLabel().getValue());
    Assertions.assertEquals(hierarchy.getOrigin(), snapshot.getOrigin());
    Assertions.assertEquals(hierarchy.getSequence(), snapshot.getSequence());

    List<String> list = this.hService.getChildren(snapshot, root).stream().map(s -> s.getCode()).toList();

    hierarchy.getRootNodes().forEach(type ->
      Assertions.assertTrue(list.contains(type.getCode()), "Expected [" + type.getCode() + "]"));

  }
}
