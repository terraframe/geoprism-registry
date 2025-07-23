/**
 *
 */
package net.geoprism.registry.service;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.After;
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
import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.SchedulerManager;

import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.Commit;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.Publish;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.USADatasetTest;
import net.geoprism.registry.axon.aggregate.RunwayTransactionWrapper;
import net.geoprism.registry.axon.config.RegistryEventStore;
import net.geoprism.registry.axon.event.remote.RemoteEvent;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.EdgeDirection;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessEdgeTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessObjectBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.CommitBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.GraphRepoServiceIF;
import net.geoprism.registry.service.business.GraphTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.PublishBusinessServiceIF;
import net.geoprism.registry.service.business.PublishEventService;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.USATestData;
import net.geoprism.registry.view.PublishDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class PublishEventServiceTest extends USADatasetTest implements InstanceTestClassListener
{
  @Autowired
  private PublishEventService                       service;

  @Autowired
  private RegistryEventStore                        store;

  @Autowired
  private PublishBusinessServiceIF                  pService;

  @Autowired
  private CommitBusinessServiceIF                   cService;

  @Autowired
  private GeoObjectTypeSnapshotBusinessServiceIF    gSnapshotService;

  @Autowired
  private HierarchyTypeSnapshotBusinessServiceIF    hSnapshotService;

  private static BusinessType                       btype;

  private static BusinessEdgeType                   bEdgeType;

  private static BusinessEdgeType                   bGeoEdgeType;

  private BusinessObject                            pObject;

  private BusinessObject                            cObject;

  @Autowired
  private GraphTypeSnapshotBusinessServiceIF        graphSnapshotService;

  @Autowired
  private GeoObjectTypeBusinessServiceIF            oTypeService;

  @Autowired
  private GraphRepoServiceIF                        repoService;

  @Autowired
  private BusinessTypeBusinessServiceIF             bTypeService;

  @Autowired
  private BusinessTypeSnapshotBusinessServiceIF     bTypeSnapshotService;

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF         bEdgeService;

  @Autowired
  private BusinessEdgeTypeSnapshotBusinessServiceIF bEdgeSnapshotService;

  @Autowired
  private BusinessObjectBusinessServiceIF           bObjectService;

  @Override
  public void beforeClassSetup() throws Exception
  {
    super.beforeClassSetup();

    setUpInReq();

    if (!SchedulerManager.initialized())
    {
      SchedulerManager.start();
    }
  }

  @Request
  private void setUpInReq()
  {
    JsonObject object = new JsonObject();
    object.addProperty(BusinessType.CODE, "TEST_BUSINESS");
    object.addProperty(BusinessType.ORGANIZATION, USATestData.ORG_PPP.getCode());
    object.add(BusinessType.DISPLAYLABEL, new LocalizedValue("Test Business").toJSON());

    btype = this.bTypeService.apply(object);

    bEdgeType = this.bEdgeService.create(USATestData.ORG_PPP.getCode(), "TEST_B_EDGE", new LocalizedValue("TEST_B_EDGE"), new LocalizedValue("TEST_B_EDGE"), btype.getCode(), btype.getCode());

    bGeoEdgeType = this.bEdgeService.createGeoEdge(USATestData.ORG_PPP.getCode(), "TEST_GEO_EDGE", new LocalizedValue("TEST_GEO_EDGE"), new LocalizedValue("TEST_GEO_EDGE"), btype.getCode(), EdgeDirection.PARENT);

    this.repoService.refreshMetadataCache();
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
    if (bGeoEdgeType != null)
    {
      this.bEdgeService.delete(bGeoEdgeType);
    }

    if (bEdgeType != null)
    {
      this.bEdgeService.delete(bEdgeType);
    }

    if (btype != null)
    {
      this.bTypeService.delete(btype);
    }

    super.afterClassSetup();
  }

  @Before
  @Request
  public void setUp()
  {
    cleanUpExtra();

    testData.setUpInstanceData();

    testData.logIn(USATestData.USER_NPS_RA);

    pObject = this.bObjectService.newInstance(btype);
    pObject.setCode("P_CODE");

    this.bObjectService.apply(pObject);

    cObject = this.bObjectService.newInstance(btype);
    cObject.setCode("C_CODE");

    this.bObjectService.apply(cObject);

    this.bObjectService.addChild(pObject, bEdgeType, cObject);
    this.bObjectService.addGeoObject(pObject, bGeoEdgeType, USATestData.COLORADO.getServerObject(), EdgeDirection.PARENT, false);
  }

  @After
  @Request
  public void tearDown()
  {
    if (cObject != null)
    {
      this.bObjectService.delete(cObject);
    }

    if (pObject != null)
    {
      this.bObjectService.delete(pObject);
    }

    testData.logOut();

    cleanUpExtra();

    testData.tearDownInstanceData();
  }

  @Request
  public void cleanUpExtra()
  {
    TestDataSet.deleteAllListData();

    this.store.truncate();
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
        dto.addGeoObjectType(testData.getManagedGeoObjectTypes().stream().map(t -> t.getCode()).toArray(s -> new String[s]));
        dto.addHierarchyType(testData.getManagedHierarchyTypes().stream().map(t -> t.getCode()).toArray(s -> new String[s]));

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

          Assert.assertEquals(21, events.size());

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
