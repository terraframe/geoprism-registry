/**
 *
 */
package net.geoprism.registry.service;

import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
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
import net.geoprism.registry.service.business.RemoteJenaServiceIF;
import net.geoprism.registry.view.PublishDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class JenaExportServiceTest extends EventDatasetTest implements InstanceTestClassListener
{
  @Autowired
  private PublishEventService       eventService;

  @Autowired
  private PublishBusinessServiceIF  publishService;

  @Autowired
  private RemoteJenaServiceIF       remoteService;

  @Autowired
  private JenaExportBusinessService service;

  @Test
  @Request
  public void testPublish() throws InterruptedException
  {
    try
    {
      PublishDTO dto = getPublishDTO();

      Publish publish = eventService.publish(dto);

      try
      {
        this.service.export(publish);

        StringBuilder statement = new StringBuilder();
        statement.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "\n");
        statement.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "\n");
        statement.append("SELECT * FROM <http://terraframe.com/g1> WHERE {" + "\n");
        statement.append("    <http://terraframe.com/USATestDataState-USATestDataColorado> ?pred ?obj ." + "\n");
        statement.append("  } LIMIT 10" + "\n");

        Optional<String> optional = this.remoteService.query(statement.toString());

        Assert.assertTrue(optional.isPresent());

        String result = optional.get();

        Assert.assertFalse(StringUtils.isBlank(result));

        System.out.println(result);
      }
      finally
      {
        try
        {
          this.remoteService.clear(JenaExportBusinessService.GRAPH_NAME);
        }
        finally
        {

          publishService.delete(publish);
        }
      }
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }
}
