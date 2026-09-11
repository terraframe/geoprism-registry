/**
 *
 */
package net.geoprism.registry.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.axonframework.eventhandling.GenericEventMessage;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
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
import net.geoprism.registry.SpringInstanceTestClassRunner;
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
import net.geoprism.registry.service.business.GeoObjectTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.GraphTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.PublishBusinessServiceIF;
import net.geoprism.registry.service.business.PublishEventService;
import net.geoprism.registry.service.business.SourceAuthorityBusinessServiceIF;
import net.geoprism.registry.test.USATestData;
import net.geoprism.registry.view.CommitDTO;
import net.geoprism.registry.view.ConceptClassDTO;
import net.geoprism.registry.view.ConceptEdgeTypeDTO;
import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeClass;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
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

  private static boolean                            WRITE_FILES = false;

  @Test
  @Request
  public void testPublish() throws InterruptedException
  {
    System.out.println("");
    System.out.println("");
    System.out.println("TEST PUBLISH START");
    System.out.println("");
    System.out.println("");

    Assert.assertEquals(Long.valueOf(53L), this.store.size());

    try
    {
      PublishDTO dto = getPublishDTO();

      Publish publish = service.publish(dto);

      try
      {
        List<Commit> commits = this.cService.getCommits(publish);

        Assert.assertEquals(1, commits.size());

        Commit commit = commits.get(0);

        GeoObjectTypeSnapshot root = this.gSnapshotService.getRoot(commit);

        Assert.assertNotNull(root);

        JsonArray geoObjectTypes = new JsonArray();

        dto.getGeoObjectTypes().forEach(code -> {
          GeoObjectTypeSnapshot snapshot = this.gSnapshotService.get(commit, code);

          Assert.assertNotNull(snapshot);

          ServerGeoObjectType type = ServerGeoObjectType.get(code);

          Assert.assertEquals(type.getSequence(), snapshot.getSequence());

          geoObjectTypes.add(snapshot.toJSON());
        });

        Assert.assertTrue(geoObjectTypes.size() > 0);

        JsonArray hierarchyTypes = new JsonArray();

        dto.getHierarchyTypes().forEach(code -> {
          HierarchyTypeSnapshot snapshot = this.hSnapshotService.get(commit, code);

          Assert.assertNotNull(snapshot);

          ServerHierarchyType type = ServerHierarchyType.get(code);

          Assert.assertEquals(type.getObject().getSequence(), snapshot.getSequence());

          List<GeoObjectTypeSnapshot> children = this.hSnapshotService.getChildren(snapshot, root);

          Assert.assertTrue(children.size() > 0);

          hierarchyTypes.add(this.hSnapshotService.toJSON(snapshot, root));
        });

        dto.getBusinessTypes().forEach(code -> {
          BusinessTypeSnapshot snapshot = this.bTypeSnapshotService.get(commit, code);

          Assert.assertNotNull(snapshot);

          BusinessType type = this.bTypeService.getByCodeOrThrow(code);
          Assert.assertEquals(type.getSequence(), snapshot.getSequence());
        });

        dto.getBusinessEdgeTypes().forEach(code -> {
          BusinessEdgeTypeSnapshot snapshot = this.bEdgeSnapshotService.get(commit, code);

          Assert.assertNotNull(snapshot);

          BusinessEdgeType type = this.bEdgeService.getByCode(code).get();
          Assert.assertEquals(type.getSequence(), snapshot.getSequence());
        });

        dto.getDagTypes().forEach(code -> {
          DirectedAcyclicGraphTypeSnapshot snapshot = (DirectedAcyclicGraphTypeSnapshot) this.graphSnapshotService.get(commit, TypeClass.DAG.getCode(), code);

          Assert.assertNotNull(snapshot);

          DirectedAcyclicGraphType type = this.dagService.getByCode(code).get();
          Assert.assertEquals(type.getSequence(), snapshot.getSequence());
        });

        dto.getUndirectedTypes().forEach(code -> {
          UndirectedGraphTypeSnapshot snapshot = (UndirectedGraphTypeSnapshot) this.graphSnapshotService.get(commit, TypeClass.UNDIRECTED_GRAPH.getCode(), code);

          Assert.assertNotNull(snapshot);

          UndirectedGraphType type = this.undirectedService.getByCode(code).get();
          Assert.assertEquals(type.getSequence(), snapshot.getSequence());
        });

        List<DataSource> sources = this.cService.getSources(commit);

        Assert.assertEquals(1, sources.size());
        Assert.assertEquals(USATestData.SOURCE.getCode(), sources.get(0).getCode());

        List<SourceAuthorityDTO> authorities = getAuthorities(sources);

        Assert.assertEquals(1, authorities.size());
        Assert.assertEquals(USATestData.AUTHORITY.getCode(), authorities.get(0).getCode());

        Assert.assertEquals(Long.valueOf(106L), this.store.size());

        List<RemoteEvent> events = this.cService.getRemoteEvents(commit).toList();

        Assert.assertEquals(48, events.size());

        // Validate the concept set dependencies
        List<Commit> dependencies = this.cService.getDependencies(commit);

        Assert.assertEquals(1, dependencies.size());

        Commit dependency = dependencies.get(0);

        Assert.assertNotEquals(commit.getOid(), dependency.getOid());

        Assert.assertEquals(0, this.cService.getDependencies(dependency).size());

        Publish dependentPublish = dependency.getPublish();

        Assert.assertTrue(StringUtils.isNotBlank(dependentPublish.getConceptSet()));

        List<ConceptSetSnapshot> sets = this.cService.getConceptSets(dependency);

        Assert.assertEquals(1, sets.size());

        ConceptSetSnapshot snapshot = sets.get(0);

        // Assert the set has concept classes
        List<ConceptClassDTO> cClasses = this.cSetSnapshotService.getConceptClasses(snapshot).stream().map(c -> c.toDTO()).toList();

        Assert.assertEquals(1, cClasses.size());

        // Assert the set has concept edge types
        List<ConceptEdgeTypeDTO> cEdgeTypes = this.cSetSnapshotService.getConceptEdgeTypes(snapshot).stream().map(c -> this.cEdgeSnapshotService.toDTO(c)).toList();

        Assert.assertEquals(1, cEdgeTypes.size());

        Assert.assertEquals(5, this.cService.getRemoteEvents(dependency).toList().size());

        if (WRITE_FILES)
        {
          this.write(DEPENDENCY, dependentPublish);
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

  public List<SourceAuthorityDTO> getAuthorities(List<DataSource> sources)
  {
    List<SourceAuthorityDTO> authorities = sources.stream().map(source -> {
      return this.authorityService.get(source.getObjectValue(DataSource.AUTHORITY));
    }) //
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
  public void testIncludeAllTypesFromHierarchy() throws InterruptedException
  {

    try
    {
      Assert.assertEquals(Long.valueOf(53L), this.store.size());

      PublishDTO dto = new PublishDTO("USA Geospatial Graph", USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      dto.addHierarchyType(testData.getManagedHierarchyTypes().stream().map(t -> t.getCode()).toArray(s -> new String[s]));

      Publish publish = service.publish(dto);

      try
      {
        List<Commit> commits = this.cService.getCommits(publish);

        Assert.assertEquals(1, commits.size());
        Assert.assertTrue(dto.getGeoObjectTypes().count() > 0);

        Commit commit = commits.get(0);

        GeoObjectTypeSnapshot root = this.gSnapshotService.getRoot(commit);

        Assert.assertNotNull(root);

        dto.getGeoObjectTypes().forEach(code -> {
          GeoObjectTypeSnapshot snapshot = this.gSnapshotService.get(commit, code);

          Assert.assertNotNull(snapshot);

          ServerGeoObjectType type = ServerGeoObjectType.get(code);

          Assert.assertEquals(type.getSequence(), snapshot.getSequence());
        });

        dto.getHierarchyTypes().forEach(code -> {
          HierarchyTypeSnapshot snapshot = this.hSnapshotService.get(commit, code);

          Assert.assertNotNull(snapshot);

          ServerHierarchyType type = ServerHierarchyType.get(code);

          Assert.assertEquals(type.getObject().getSequence(), snapshot.getSequence());

          List<GeoObjectTypeSnapshot> children = this.hSnapshotService.getChildren(snapshot, root);

          Assert.assertTrue(children.size() > 0);
        });

        List<DataSource> sources = this.cService.getSources(commit);

        Assert.assertEquals(1, sources.size());
        Assert.assertEquals(USATestData.SOURCE.getCode(), sources.get(0).getCode());

        List<RemoteEvent> events = this.cService.getRemoteEvents(commit).toList();

        Assert.assertEquals(42, events.size());
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
  public void testNewCommit() throws InterruptedException
  {
    try
    {
      PublishDTO dto = getPublishDTO();

      Publish publish = service.publish(dto);

      try
      {
        List<Commit> commits = this.cService.getCommits(publish);

        Assert.assertEquals(1, commits.size());

        Commit commit = commits.get(0);

        List<Commit> dependencies = this.cService.getDependencies(commit);

        Assert.assertEquals(1, dependencies.size());

        Assert.assertEquals(48, this.cService.getRemoteEvents(commit).toList().size());
        Assert.assertEquals(Long.valueOf(106), this.store.size());

        // Update a geo object
        ServerGeoObjectIF object = USATestData.COLORADO.getServerObject();
        object.setDisplayLabel(new LocalizedValue("ABCD"), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);

        ServerGeoObjectEventBuilder builder = new ServerGeoObjectEventBuilder(gObjectService);
        builder.setObject(object, false, false);
        builder.setAttributeUpdate(true);

        gateway.publish(builder.build().stream().map(GenericEventMessage::asEventMessage).toList());

        Assert.assertEquals(Long.valueOf(107), this.store.size());

        // Create a new commit with the new change
        Commit commit2 = this.service.createNewCommit(publish);

        Assert.assertEquals(2, this.cService.getCommits(publish).size());

        Assert.assertEquals(1, this.cService.getRemoteEvents(commit2).toList().size());

        List<Commit> results = this.cService.getDependencies(commit2);

        Assert.assertEquals(2, results.size());
        Assert.assertTrue(results.contains(dependencies.get(0)));
        Assert.assertTrue(results.contains(commit));
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
    try
    {
      // Delete all of the publishes that were generated because of
      // dependencies
      this.cService.getCommits(publish) //
          .stream() //
          .flatMap(c -> this.cService.getDependencies(c).stream()) //
          .map(d -> d.getPublish()) //
          .distinct() //
          .filter(p -> !p.getOid().equals(publish.getOid())) //
          .forEach(p -> pService.delete(p));
    }
    finally
    {
      try
      {
        pService.delete(publish);
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  public void write(String uid, Publish publish) throws IOException
  {
    String directory = "src/test/resources/commit/" + uid + "/";

    new File(directory).mkdirs();

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);

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
