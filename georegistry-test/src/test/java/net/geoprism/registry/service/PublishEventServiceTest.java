/**
 *
 */
package net.geoprism.registry.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
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
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.session.Request;

import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.registry.Commit;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.Publish;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.axon.aggregate.RunwayTransactionWrapper;
import net.geoprism.registry.axon.config.RegistryEventStore;
import net.geoprism.registry.axon.event.remote.RemoteEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectSetParentEvent;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.service.business.CommitBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.PublishBusinessServiceIF;
import net.geoprism.registry.service.business.PublishEventService;
import net.geoprism.registry.view.PublishDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class PublishEventServiceTest implements InstanceTestClassListener
{
  @Autowired
  private PublishEventService                    service;

  @Autowired
  private PublishBusinessServiceIF               pService;

  @Autowired
  private CommitBusinessServiceIF                cService;

  @Autowired
  private GeoObjectTypeSnapshotBusinessServiceIF gSnapshotService;

  @Autowired
  private HierarchyTypeSnapshotBusinessServiceIF hSnapshotService;

  @Override
  public void beforeClassSetup() throws Exception
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void afterClassSetup() throws Exception
  {
    // TODO Auto-generated method stub

  }

  @Before
  @Request
  public void after()
  {
    Arrays.asList(RemoteGeoObjectEvent.class, RemoteGeoObjectSetParentEvent.class).forEach(cl -> {
      Database.deleteWhere(RegistryEventStore.DOMAIN_EVENT_ENTRY_TABLE, "payloadtype = '" + cl.getName() + "'");
    });
  }

  @Test
  public void test() throws InterruptedException
  {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);

    RunwayTransactionWrapper.run(() -> {
      // TrackingToken token = new GapAwareTrackingToken(0, null);
      Date date = new Date();

      try
      {
        PublishDTO dto = new PublishDTO(date, date, date);
        dto.addGeoObjectType("REG", "PRO", "CTY", "SHR");
        dto.addHierarchyType("ADM_H");

        Publish publish = service.publish(dto);

        try
        {
          mapper.writeValue(new File("publish.json"), dto);

          List<Commit> commits = this.cService.getCommits(publish);

          Assert.assertEquals(1, commits.size());

          Commit commit = commits.get(0);

          mapper.writeValue(new File("commit.json"), commit.toDTO(publish));

          GeoObjectTypeSnapshot root = this.gSnapshotService.getRoot(commit);

          Assert.assertNotNull(root);

          JsonArray geoObjectTypes = new JsonArray();

          dto.getGeoObjectTypes().forEach(code -> {
            GeoObjectTypeSnapshot snapshot = this.gSnapshotService.get(commit, code);

            Assert.assertNotNull(snapshot);

            geoObjectTypes.add(snapshot.toJSON());
          });

          Assert.assertTrue(geoObjectTypes.size() > 0);

          gson.toJson(geoObjectTypes, System.out);

          JsonArray hierarchyTypes = new JsonArray();

          dto.getHierarchyTypes().forEach(code -> {
            HierarchyTypeSnapshot type = this.hSnapshotService.get(commit, code);

            Assert.assertNotNull(type);

            hierarchyTypes.add(type.toJSON(root));
          });

          gson.toJson(hierarchyTypes, System.out);

          List<RemoteEvent> events = this.cService.getRemoteEvents(commit, 0);

          Assert.assertEquals(253, events.size());

          // serializer.typeFactory.constructCollectionLikeType(List::class.java,
          // SomeClass::class.java)

          mapper.writerFor(mapper.getTypeFactory().constructCollectionLikeType(List.class, RemoteEvent.class)).writeValue(new File("events.json"), events);

        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
        finally
        {
          pService.delete(publish);
        }
      }
      catch (InterruptedException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    });
  }
}
