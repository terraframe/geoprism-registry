/**
 *
 */
package net.geoprism.registry.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.axonframework.eventhandling.GenericEventMessage;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.runwaysdk.Pair;
import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.session.Request;

import net.geoprism.graph.BusinessEdgeTypeSnapshot;
import net.geoprism.graph.BusinessTypeSnapshot;
import net.geoprism.graph.ConceptSetSnapshot;
import net.geoprism.graph.DirectedAcyclicGraphTypeSnapshot;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.UndirectedGraphTypeSnapshot;
import net.geoprism.registry.Commit;
import net.geoprism.registry.EventDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.JsonCollectors;
import net.geoprism.registry.Publish;
import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.axon.event.remote.RemoteEvent;
import net.geoprism.registry.axon.event.repository.ServerGeoObjectEventBuilder;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.graph.BusinessEdgeType;
import net.geoprism.registry.graph.BusinessType;
import net.geoprism.registry.graph.DataSource;
import net.geoprism.registry.graph.DirectedAcyclicGraphType;
import net.geoprism.registry.graph.UndirectedGraphType;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.SourceAuthorityDTO;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessEdgeTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.CommitBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptEdgeTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptSetSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.DataSourceBusinessServiceIF;
import net.geoprism.registry.service.business.EdgeObjectBusinessService;
import net.geoprism.registry.service.business.EventBusinessService;
import net.geoprism.registry.service.business.GeoObjectTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.GraphTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.PublishBusinessServiceIF;
import net.geoprism.registry.service.business.PublishEventService;
import net.geoprism.registry.service.business.SourceAuthorityBusinessServiceIF;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.USATestData;
import net.geoprism.registry.view.CommitDTO;
import net.geoprism.registry.view.ConceptClassDTO;
import net.geoprism.registry.view.ConceptEdgeTypeDTO;
import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeClass;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
public class PublishEventServiceTest extends EventDatasetTest implements InstanceTestClassListener
{
  public static final String                        DEPENDENCY  = "111bb747-a068-458f-b261-9cde9b5a3937";

  public static final String                        MAIN        = "9eafd2b0-f427-4443-a535-07cef1ab47e7";

  @Autowired
  private PublishEventService                       service;

  @Autowired
  private PublishBusinessServiceIF                  pService;

  @Autowired
  private CommitBusinessServiceIF                   cService;

  @Autowired
  private GeoObjectTypeSnapshotBusinessServiceIF    gSnapshotService;

  @Autowired
  private HierarchyTypeSnapshotBusinessServiceIF    hSnapshotService;

  @Autowired
  private GraphTypeSnapshotBusinessServiceIF        graphSnapshotService;

  @Autowired
  private ConceptEdgeTypeSnapshotBusinessServiceIF  cEdgeSnapshotService;

  @Autowired
  private ConceptSetSnapshotBusinessServiceIF       cSetSnapshotService;

  @Autowired
  private BusinessTypeBusinessServiceIF             bTypeService;

  @Autowired
  private BusinessTypeSnapshotBusinessServiceIF     bTypeSnapshotService;

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF         bEdgeService;

  @Autowired
  private BusinessEdgeTypeSnapshotBusinessServiceIF bEdgeSnapshotService;

  @Autowired
  private DataSourceBusinessServiceIF               sourceService;

  @Autowired
  private SourceAuthorityBusinessServiceIF          authorityService;

  @Autowired
  private EventBusinessService                      eventService;

  @Autowired
  private EdgeObjectBusinessService                 eObjectService;

  @Autowired
  public ConcurrentTaskExecutor                     executor;

  private static boolean                            WRITE_FILES = false;

  private static String                             edgeUid;

  @Override
  protected String addDirectedAcyclicEdge(TestGeoObjectInfo source, TestGeoObjectInfo target)
  {
    edgeUid = super.addDirectedAcyclicEdge(source, target);

    return edgeUid;
  }

  @BeforeEach
  @Request
  public void setUp() throws Exception
  {
    super.setUp();

    EdgeObject edge = this.eObjectService.getByUid(dagType, edgeUid).get();

    this.eventService.remove(dagType, edge);
  }

  @Test
  @Request
  public void testPublish() throws InterruptedException, ExecutionException
  {
    System.out.println("");
    System.out.println("");
    System.out.println("TEST PUBLISH START");
    System.out.println("");
    System.out.println("");

    Assertions.assertEquals(Long.valueOf(55L), this.store.size());

    try
    {
      PublishDTO dto = getPublishDTO();

      Publish publish = publishAndWait(dto);

      try
      {
        List<Commit> commits = this.cService.getCommits(publish);

        Assertions.assertEquals(1, commits.size());

        Commit commit = commits.get(0);

        GeoObjectTypeSnapshot root = this.gSnapshotService.getRoot(commit);

        Assertions.assertNotNull(root);

        JsonArray geoObjectTypes = new JsonArray();

        dto.getGeoObjectTypes().forEach(code -> {
          GeoObjectTypeSnapshot snapshot = this.gSnapshotService.get(commit, code);

          Assertions.assertNotNull(snapshot);

          ServerGeoObjectType type = ServerGeoObjectType.get(code);

          Assertions.assertEquals(type.getSequence(), snapshot.getSequence());

          geoObjectTypes.add(snapshot.toJSON());
        });

        Assertions.assertTrue(geoObjectTypes.size() > 0);

        JsonArray hierarchyTypes = new JsonArray();

        dto.getHierarchyTypes().forEach(code -> {
          HierarchyTypeSnapshot snapshot = this.hSnapshotService.get(commit, code);

          Assertions.assertNotNull(snapshot);

          ServerHierarchyType type = ServerHierarchyType.get(code);

          Assertions.assertEquals(type.getObject().getSequence(), snapshot.getSequence());

          List<GeoObjectTypeSnapshot> children = this.hSnapshotService.getChildren(snapshot, root);

          Assertions.assertTrue(children.size() > 0);

          hierarchyTypes.add(this.hSnapshotService.toJSON(snapshot, root));
        });

        dto.getBusinessTypes().forEach(code -> {
          BusinessTypeSnapshot snapshot = this.bTypeSnapshotService.get(commit, code);

          Assertions.assertNotNull(snapshot);

          BusinessType type = this.bTypeService.getByCodeOrThrow(code);
          Assertions.assertEquals(type.getSequence(), snapshot.getSequence());
        });

        dto.getBusinessEdgeTypes().forEach(code -> {
          BusinessEdgeTypeSnapshot snapshot = this.bEdgeSnapshotService.get(commit, code);

          Assertions.assertNotNull(snapshot);

          BusinessEdgeType type = this.bEdgeService.getByCode(code).get();
          Assertions.assertEquals(type.getSequence(), snapshot.getSequence());
        });

        dto.getDagTypes().forEach(code -> {
          DirectedAcyclicGraphTypeSnapshot snapshot = (DirectedAcyclicGraphTypeSnapshot) this.graphSnapshotService.get(commit, TypeClass.DAG.getCode(), code);

          Assertions.assertNotNull(snapshot);

          DirectedAcyclicGraphType type = this.dagService.getByCode(code).get();
          Assertions.assertEquals(type.getSequence(), snapshot.getSequence());
        });

        dto.getUndirectedTypes().forEach(code -> {
          UndirectedGraphTypeSnapshot snapshot = (UndirectedGraphTypeSnapshot) this.graphSnapshotService.get(commit, TypeClass.UNDIRECTED_GRAPH.getCode(), code);

          Assertions.assertNotNull(snapshot);

          UndirectedGraphType type = this.undirectedService.getByCode(code).get();
          Assertions.assertEquals(type.getSequence(), snapshot.getSequence());
        });

        List<DataSource> sources = this.cService.getSources(commit);

        Assertions.assertEquals(1, sources.size());
        Assertions.assertEquals(USATestData.SOURCE.getCode(), sources.get(0).getCode());

        List<SourceAuthorityDTO> authorities = getAuthorities(sources);

        Assertions.assertEquals(1, authorities.size());
        Assertions.assertEquals(USATestData.AUTHORITY.getCode(), authorities.get(0).getCode());

        Assertions.assertEquals(Long.valueOf(109L), this.store.size());

        List<RemoteEvent> events = this.cService.getRemoteEvents(commit).toList();

        Assertions.assertEquals(49, events.size());

        // Validate the concept set dependencies
        List<Commit> dependencies = this.cService.getDependencies(commit);

        Assertions.assertEquals(1, dependencies.size());

        Commit dependency = dependencies.get(0);

        Assertions.assertNotEquals(commit.getOid(), dependency.getOid());

        Assertions.assertEquals(0, this.cService.getDependencies(dependency).size());

        Publish conceptPublish = dependency.getPublish();

        Assertions.assertTrue(StringUtils.isNotBlank(conceptPublish.getConceptSet()));

        List<ConceptSetSnapshot> sets = this.cService.getConceptSets(dependency);

        Assertions.assertEquals(1, sets.size());

        ConceptSetSnapshot snapshot = sets.get(0);

        // Assert the set has concept classes
        List<ConceptClassDTO> cClasses = this.cSetSnapshotService.getConceptClasses(snapshot).stream().map(c -> c.toDTO()).toList();

        Assertions.assertEquals(1, cClasses.size());

        // Assert the set has concept edge types
        List<ConceptEdgeTypeDTO> cEdgeTypes = this.cSetSnapshotService.getConceptEdgeTypes(snapshot).stream().map(c -> this.cEdgeSnapshotService.toDTO(c)).toList();

        Assertions.assertEquals(1, cEdgeTypes.size());

        Assertions.assertEquals(5, this.cService.getRemoteEvents(dependency).toList().size());

        if (WRITE_FILES)
        {
          this.write(DEPENDENCY, conceptPublish);
          this.write(MAIN, publish);
        }
      }
      catch (IOException e)
      {
        throw new RuntimeException(e);
      }
      finally
      {
        delete(publish);
      }
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }

    System.out.println("");
    System.out.println("");
    System.out.println("TEST PUBLISH END");
    System.out.println("");
    System.out.println("");
  }

  public Publish publishAndWait(PublishDTO dto) throws InterruptedException, ExecutionException
  {
    Pair<Publish, Future<?>> response = service.publish(dto);

    Publish publish = response.getFirst();

    // Wait for the commit to finish publishing
    response.getSecond().get();

    return publish;
  }

  public List<SourceAuthorityDTO> getAuthorities(List<DataSource> sources)
  {
    List<SourceAuthorityDTO> authorities = sources.stream().map(source -> this.authorityService.get(source.getObjectValue(DataSource.AUTHORITY))) //
        .filter(o -> o.isPresent()) //
        .map(o -> o.get()) //
        .distinct() //
        .map(this.authorityService::toDTO) //
        .map(o -> {
          o.setOid(null);

          return o;
        }) //
        .toList();
    return authorities;
  }

  @Test
  @Request
  public void testIncludeAllTypesFromHierarchy() throws InterruptedException, ExecutionException
  {

    try
    {
      Assertions.assertEquals(Long.valueOf(55L), this.store.size());

      PublishDTO dto = new PublishDTO("USA Geospatial Graph", USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      dto.addHierarchyType(testData.getManagedHierarchyTypes().stream().map(t -> t.getCode()).toArray(s -> new String[s]));

      Publish publish = publishAndWait(dto);

      try
      {
        List<Commit> commits = this.cService.getCommits(publish);

        Assertions.assertEquals(1, commits.size());
        Assertions.assertTrue(dto.getGeoObjectTypes().count() > 0);

        Commit commit = commits.get(0);

        GeoObjectTypeSnapshot root = this.gSnapshotService.getRoot(commit);

        Assertions.assertNotNull(root);

        dto.getGeoObjectTypes().forEach(code -> {
          GeoObjectTypeSnapshot snapshot = this.gSnapshotService.get(commit, code);

          Assertions.assertNotNull(snapshot);

          ServerGeoObjectType type = ServerGeoObjectType.get(code);

          Assertions.assertEquals(type.getSequence(), snapshot.getSequence());
        });

        dto.getHierarchyTypes().forEach(code -> {
          HierarchyTypeSnapshot snapshot = this.hSnapshotService.get(commit, code);

          Assertions.assertNotNull(snapshot);

          ServerHierarchyType type = ServerHierarchyType.get(code);

          Assertions.assertEquals(type.getObject().getSequence(), snapshot.getSequence());

          List<GeoObjectTypeSnapshot> children = this.hSnapshotService.getChildren(snapshot, root);

          Assertions.assertTrue(children.size() > 0);
        });

        List<DataSource> sources = this.cService.getSources(commit);

        Assertions.assertEquals(1, sources.size());
        Assertions.assertEquals(USATestData.SOURCE.getCode(), sources.get(0).getCode());

        List<RemoteEvent> events = this.cService.getRemoteEvents(commit).toList();

        Assertions.assertEquals(42, events.size());
      }
      finally
      {
        this.delete(publish);
      }
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
    // });
  }

  @Test
  @Request
  public void testNewCommit() throws InterruptedException, ExecutionException
  {
    try
    {
      PublishDTO dto = getPublishDTO();

      Publish publish = publishAndWait(dto);

      try
      {
        List<Commit> commits = this.cService.getCommits(publish);

        Assertions.assertEquals(1, commits.size());

        Commit commit = commits.get(0);

        List<Commit> dependencies = this.cService.getDependencies(commit);

        Assertions.assertEquals(1, dependencies.size());

        Assertions.assertEquals(49, this.cService.getRemoteEvents(commit).toList().size());
        Assertions.assertEquals(Long.valueOf(109), this.store.size());

        // Update a geo object
        ServerGeoObjectIF object = USATestData.COLORADO.getServerObject();
        object.setDisplayLabel(new LocalizedValue("ABCD"), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);

        ServerGeoObjectEventBuilder builder = new ServerGeoObjectEventBuilder(gObjectService);
        builder.setObject(object, false, false);
        builder.setAttributeUpdate(true);

        gateway.publish(builder.build().stream().map(GenericEventMessage::asEventMessage).toList());

        Assertions.assertEquals(Long.valueOf(110), this.store.size());

        // Create a new commit with the new change
        Commit commit2 = this.service.createNewCommit(publish);

        Assertions.assertEquals(2, this.cService.getCommits(publish).size());

        Assertions.assertEquals(1, this.cService.getRemoteEvents(commit2).toList().size());

        List<Commit> results = this.cService.getDependencies(commit2);

        Assertions.assertEquals(2, results.size());
        Assertions.assertTrue(results.contains(dependencies.get(0)));
        Assertions.assertTrue(results.contains(commit));
      }
      finally
      {
        this.delete(publish);
      }
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }

  public void delete(Publish publish)
  {
    // Delete all of the publishes that were generated because of
    // dependencies
    List<Publish> dependencies = this.cService.getCommits(publish) //
        .stream() //
        .flatMap(c -> this.cService.getDependencies(c).stream()) //
        .map(d -> d.getPublish()) //
        .distinct() //
        .filter(p -> !p.getOid().equals(publish.getOid())).toList();

    try
    {
      pService.delete(publish);
    }
    finally
    {
      dependencies.forEach(pService::delete);
    }
  }

  public void write(String uid, Publish publish) throws IOException
  {
    String directory = "src/test/resources/commit/" + uid + "/";

    new File(directory).mkdirs();

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    JsonMapper mapper = JsonMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build();

    PublishDTO pDto = publish.toDTO();
    pDto.setUid(uid);

    mapper.writeValue(new File(directory, "publish.json"), pDto);

    List<Commit> commits = this.cService.getCommits(publish);

    if (commits.size() > 0)
    {
      Commit commit = commits.get(0);

      CommitDTO cDTO = commit.toDTO(publish);
      cDTO.setPublishId(uid);
      cDTO.setUid(uid);

      mapper.writeValue(new File(directory, "commit.json"), cDTO);

      List<Commit> dependencies = this.cService.getDependencies(commit);

      mapper.writeValue(new File(directory, "dependencies.json"), dependencies.stream().map(dependency -> {
        CommitDTO dto = dependency.toDTO(publish);
        dto.setPublishId(DEPENDENCY);
        dto.setUid(DEPENDENCY);

        return dto;
      }).toList());

      List<DataSource> sources = this.cService.getSources(commit);

      if (sources.size() > 0)
      {
        mapper.writeValue(new File(directory, "sources.json"), sources.stream().map(this.sourceService::toDTO).toArray());

        List<SourceAuthorityDTO> authorities = getAuthorities(sources);

        mapper.writeValue(new File(directory, "authorities.json"), authorities);
      }

      List<ConceptSetSnapshot> sets = this.cService.getConceptSets(commit);

      if (sets.size() > 0)
      {
        ConceptSetSnapshot set = sets.get(0);

        mapper.writeValue(new File(directory, "concept-sets.json"), Arrays.asList(this.cSetSnapshotService.toDTO(set)));

        List<ConceptClassDTO> cClasses = this.cSetSnapshotService.getConceptClasses(set).stream().map(c -> c.toDTO()).toList();

        mapper.writeValue(new File(directory, "concept-classes.json"), cClasses);

        List<ConceptEdgeTypeDTO> cEdgeTypes = this.cSetSnapshotService.getConceptEdgeTypes(set).stream().map(c -> this.cEdgeSnapshotService.toDTO(c)).toList();

        mapper.writeValue(new File(directory, "concept-edge-types.json"), cEdgeTypes);
      }

      mapper.writeValue(new File(directory, "business-types.json"), this.cService.getBusinessTypes(commit).stream().map(type -> type.toDTO()).toList());
      mapper.writeValue(new File(directory, "business-edge-types.json"), this.cService.getBusinessEdgeTypes(commit).stream().map(type -> this.bEdgeSnapshotService.toDTO(type)).toList());

      GeoObjectTypeSnapshot rootType = this.cService.getRootType(commit);

      JsonArray geoObjectTypes = this.cService.getTypes(commit).stream().filter(s -> !s.isRoot()).map(s -> s.toJSON()).collect(JsonCollectors.toJsonArray());
      JsonArray hierarchyTypes = this.cService.getHiearchyTypes(commit).stream().map(s -> this.hSnapshotService.toJSON((HierarchyTypeSnapshot) s, rootType)).collect(JsonCollectors.toJsonArray());
      JsonArray dagTypes = this.cService.getDirectedAcyclicGraphTypes(commit).stream().map(s -> ( (DirectedAcyclicGraphTypeSnapshot) s ).toJSON()).collect(JsonCollectors.toJsonArray());
      JsonArray undirectedGraphTypes = this.cService.getUndirectedGraphTypes(commit).stream().map(s -> ( (UndirectedGraphTypeSnapshot) s ).toJSON()).collect(JsonCollectors.toJsonArray());

      try (FileWriter writer = new FileWriter(new File(directory, "geo-object-types.json")))
      {
        gson.toJson(geoObjectTypes, writer);
      }

      try (FileWriter writer = new FileWriter(new File(directory, "hierarchy-types.json")))
      {
        gson.toJson(hierarchyTypes, writer);
      }

      try (FileWriter writer = new FileWriter(new File(directory, "dag-types.json")))
      {
        gson.toJson(dagTypes, writer);
      }

      try (FileWriter writer = new FileWriter(new File(directory, "undirected-graph-types.json")))
      {
        gson.toJson(undirectedGraphTypes, writer);
      }

      List<RemoteEvent> events = this.cService.getRemoteEvents(commit).toList();

      mapper.writerFor(mapper.getTypeFactory().constructCollectionLikeType(List.class, RemoteEvent.class)).writeValue(new File(directory, "events.json"), events);
    }
  }

}
