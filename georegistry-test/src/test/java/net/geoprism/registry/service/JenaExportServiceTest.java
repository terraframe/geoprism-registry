/**
 *
 */
package net.geoprism.registry.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.session.Request;

import net.geoprism.registry.EventDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.Publish;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.service.business.JenaExportBusinessService;
import net.geoprism.registry.service.business.PublishBusinessServiceIF;
import net.geoprism.registry.service.business.PublishEventService;
import net.geoprism.registry.view.PublishDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class JenaExportServiceTest extends EventDatasetTest implements InstanceTestClassListener
{
  @Autowired
  private PublishEventService       service;

  @Autowired
  private PublishBusinessServiceIF  pService;

  @Autowired
  private JenaExportBusinessService jenaService;

  @Test
  @Request
  public void testPublish() throws InterruptedException
  {
    try
    {
      PublishDTO dto = getPublishDTO();

      Publish publish = service.publish(dto);

      try
      {
        this.jenaService.export(publish);
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
