/**
 *
 */
package net.geoprism.registry.axon;

import java.util.Arrays;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.session.Request;

import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.Publish;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.axon.aggregate.RunwayTransactionWrapper;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectSetParentEvent;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.service.business.PublishBusinessServiceIF;
import net.geoprism.registry.service.business.PublishEventService;
import net.geoprism.registry.view.EventPublishingConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class PublishEventServiceTest implements InstanceTestClassListener
{
  @Autowired
  private PublishEventService      service;

  @Autowired
  private PublishBusinessServiceIF pService;

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
      Database.deleteWhere(PublishEventService.DOMAIN_EVENT_ENTRY_TABLE, "payloadtype = '" + cl.getName() + "'");
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
        Publish publish = service.publish(new EventPublishingConfiguration(date, date, date));

        pService.delete(publish);
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
