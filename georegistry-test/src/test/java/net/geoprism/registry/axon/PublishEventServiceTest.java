/**
 *
 */
package net.geoprism.registry.axon;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.axon.aggregate.RunwayTransactionWrapper;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.service.business.PublishEventService;
import net.geoprism.registry.view.EventPublishingConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class PublishEventServiceTest implements InstanceTestClassListener
{
  @Autowired
  private PublishEventService service;

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

  @Test
  public void test() throws InterruptedException
  {
    RunwayTransactionWrapper.run(() -> {
      // TrackingToken token = new GapAwareTrackingToken(0, null);
      Date date = new Date();

      try
      {
        service.publish(new EventPublishingConfiguration(date, date, date), null);
      }
      catch (InterruptedException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    });
  }
}
