/**
 *
 */
package net.geoprism.registry.axon;

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

import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.session.Request;

import net.geoprism.registry.Commit;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.Publish;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.axon.aggregate.RunwayTransactionWrapper;
import net.geoprism.registry.axon.config.RegistryEventStore;
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
          List<Commit> commits = this.cService.getCommits(publish);

          Assert.assertEquals(1, commits.size());

          Commit commit = commits.get(0);

          dto.getGeoObjectTypes().forEach(code -> {
            Assert.assertNotNull(this.gSnapshotService.get(commit, code));
          });

          dto.getHierarchyTypes().forEach(code -> {
            Assert.assertNotNull(this.hSnapshotService.get(commit, code));
          });

          Assert.assertEquals(253, this.cService.getRemoteEvents(commit, 0).size());

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

      /*
       * WITH T AS ( SELECT *, ROW_NUMBER() OVER(PARTITION BY ID ORDER BY Date
       * DESC) AS rn FROM yourTable ) SELECT * FROM T WHERE rn = 1
       */

    });
  }
}
