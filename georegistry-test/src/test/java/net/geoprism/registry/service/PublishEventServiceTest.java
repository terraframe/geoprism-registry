/**
 *
 */
package net.geoprism.registry.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

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
import net.geoprism.graph.DirectedAcyclicGraphTypeSnapshot;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.GraphTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.UndirectedGraphTypeSnapshot;
import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.Commit;
import net.geoprism.registry.DirectedAcyclicGraphType;
import net.geoprism.registry.EventDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.Publish;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.UndirectedGraphType;
import net.geoprism.registry.axon.event.remote.RemoteEvent;
import net.geoprism.registry.axon.event.repository.ServerGeoObjectEventBuilder;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.graph.DataSource;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessEdgeTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.CommitBusinessServiceIF;
import net.geoprism.registry.service.business.DataSourceBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.GraphTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.PublishBusinessServiceIF;
import net.geoprism.registry.service.business.PublishEventService;
import net.geoprism.registry.test.USATestData;
import net.geoprism.registry.view.PublishDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class PublishEventServiceTest extends EventDatasetTest implements InstanceTestClassListener
{
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
  private BusinessTypeBusinessServiceIF             bTypeService;

  @Autowired
  private BusinessTypeSnapshotBusinessServiceIF     bTypeSnapshotService;

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF         bEdgeService;

  @Autowired
  private BusinessEdgeTypeSnapshotBusinessServiceIF bEdgeSnapshotService;

  @Autowired
  private DataSourceBusinessServiceIF               sourceService;

  private static boolean                            WRITE_FILES = false;

  @Test
  @Request
  public void testPublish() throws InterruptedException
  {
    String directory = "src/test/resources/commit";

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);

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

        JsonArray businessTypes = new JsonArray();

        dto.getBusinessTypes().forEach(code -> {
          BusinessTypeSnapshot snapshot = this.bTypeSnapshotService.get(commit, code);

          Assert.assertNotNull(snapshot);

          BusinessType type = this.bTypeService.getByCode(code);
          Assert.assertEquals(type.getSequence(), snapshot.getSequence());

          businessTypes.add(snapshot.toJSON());
        });

        JsonArray businessEdgeTypes = new JsonArray();

        dto.getBusinessEdgeTypes().forEach(code -> {
          BusinessEdgeTypeSnapshot snapshot = this.bEdgeSnapshotService.get(commit, code);

          Assert.assertNotNull(snapshot);

          BusinessEdgeType type = this.bEdgeService.getByCode(code).get();
          Assert.assertEquals(type.getSequence(), snapshot.getSequence());

          businessEdgeTypes.add(this.bEdgeSnapshotService.toJSON(snapshot));
        });

        JsonArray dagTypes = new JsonArray();

        dto.getDagTypes().forEach(code -> {
          DirectedAcyclicGraphTypeSnapshot snapshot = (DirectedAcyclicGraphTypeSnapshot) this.graphSnapshotService.get(commit, GraphTypeSnapshot.DIRECTED_ACYCLIC_GRAPH_TYPE, code);

          Assert.assertNotNull(snapshot);

          DirectedAcyclicGraphType type = this.dagService.getByCode(code).get();
          Assert.assertEquals(type.getSequence(), snapshot.getSequence());

          dagTypes.add(snapshot.toJSON());
        });

        JsonArray undirectedGraphTypes = new JsonArray();

        dto.getUndirectedTypes().forEach(code -> {
          UndirectedGraphTypeSnapshot snapshot = (UndirectedGraphTypeSnapshot) this.graphSnapshotService.get(commit, GraphTypeSnapshot.UNDIRECTED_GRAPH_TYPE, code);

          Assert.assertNotNull(snapshot);

          UndirectedGraphType type = this.undirectedService.getByCode(code).get();
          Assert.assertEquals(type.getSequence(), snapshot.getSequence());

          undirectedGraphTypes.add(snapshot.toJSON());
        });

        try (FileWriter writer = new FileWriter(new File(directory, "undirected-graph-types.json")))
        {
          gson.toJson(undirectedGraphTypes, writer);
        }

        List<DataSource> sources = this.cService.getSources(commit);

        Assert.assertEquals(1, sources.size());
        Assert.assertEquals(USATestData.SOURCE.getCode(), sources.get(0).getCode());

        List<RemoteEvent> events = this.cService.getRemoteEvents(commit).toList();

        Assert.assertEquals(47, events.size());

        if (WRITE_FILES)
        {
          mapper.writeValue(new File(directory, "publish.json"), dto);
          mapper.writeValue(new File(directory, "commit.json"), commit.toDTO(publish));
          mapper.writeValue(new File(directory, "sources.json"), sources.stream().map(this.sourceService::toDTO).toArray());

          try (FileWriter writer = new FileWriter(new File(directory, "geo-object-types.json")))
          {
            gson.toJson(geoObjectTypes, writer);
          }

          try (FileWriter writer = new FileWriter(new File(directory, "hierarchy-types.json")))
          {
            gson.toJson(hierarchyTypes, writer);
          }

          try (FileWriter writer = new FileWriter(new File(directory, "business-types.json")))
          {
            gson.toJson(businessTypes, writer);
          }

          try (FileWriter writer = new FileWriter(new File(directory, "business-edge-types.json")))
          {
            gson.toJson(businessEdgeTypes, writer);
          }

          try (FileWriter writer = new FileWriter(new File(directory, "dag-types.json")))
          {
            gson.toJson(dagTypes, writer);
          }

          mapper.writerFor(mapper.getTypeFactory().constructCollectionLikeType(List.class, RemoteEvent.class)).writeValue(new File(directory, "events.json"), events);
        }
      }
      catch (IOException e)
      {
        throw new RuntimeException(e);
      }
      finally
      {
        pService.delete(publish);
      }
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Test
  @Request
  public void testIncludeAllTypesFromHierarchy() throws InterruptedException
  {
    try
    {
      PublishDTO dto = new PublishDTO(USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
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

        Assert.assertEquals(41, events.size());
      }
      finally
      {
        pService.delete(publish);
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

        Assert.assertEquals(47, this.cService.getRemoteEvents(commit).toList().size());
        Assert.assertEquals(Long.valueOf(94), this.store.size());

        // Update a geo object
        ServerGeoObjectIF object = USATestData.COLORADO.getServerObject();
        object.setDisplayLabel(new LocalizedValue("ABCD"), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);

        ServerGeoObjectEventBuilder builder = new ServerGeoObjectEventBuilder(gObjectService);
        builder.setObject(object, false, false);
        builder.setAttributeUpdate(true);

        gateway.publish(builder.build().stream().map(GenericEventMessage::asEventMessage).toList());

        Assert.assertEquals(Long.valueOf(95), this.store.size());

        // Create a new commit with the new change
        Commit commit2 = this.service.createNewCommit(publish);

        Assert.assertEquals(2, this.cService.getCommits(publish).size());

        Assert.assertEquals(1, this.cService.getRemoteEvents(commit2).toList().size());

        List<Commit> dependencies = this.cService.getDependencies(commit2);

        Assert.assertEquals(1, dependencies.size());
        Assert.assertEquals(commit.getUid(), dependencies.get(0).getUid());
      }
      finally
      {
        pService.delete(publish);
      }
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }

}
