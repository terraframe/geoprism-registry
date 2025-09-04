/**
 *
 */
package net.geoprism.registry.etl;

import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.session.Request;

import net.geoprism.registry.EventDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.Publish;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.JenaExternalSystem;
import net.geoprism.registry.service.business.JenaSynchronizationService;
import net.geoprism.registry.service.business.PublishBusinessServiceIF;
import net.geoprism.registry.service.business.PublishEventService;
import net.geoprism.registry.service.business.RemoteJenaServiceIF;
import net.geoprism.registry.service.business.SynchronizationConfigBusinessServiceIF;
import net.geoprism.registry.test.USATestData;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class JenaSynchronizationServiceTest extends EventDatasetTest implements InstanceTestClassListener
{
  @Autowired
  private PublishEventService        eventService;

  @Autowired
  private PublishBusinessServiceIF   publishService;

  @Autowired
  private RemoteJenaServiceIF        remoteService;

  @Autowired
  private JenaSynchronizationService service;

  private static JenaExternalSystem  system;

  @Override
  @Request
  public void beforeClassSetup() throws Exception
  {
    super.beforeClassSetup();

    system = new JenaExternalSystem();
    system.setId("JenaExportTest");
    system.setOrganization(USATestData.ORG_NPS.getServerObject());
    system.getEmbeddedComponent(ExternalSystem.LABEL).setValue("defaultLocale", "Test");
    system.getEmbeddedComponent(ExternalSystem.DESCRIPTION).setValue("defaultLocale", "Test");
    system.setUrl("http://localhost:3030/test");
    system.apply();
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
    if (system != null)
    {
      system.delete();
    }

    super.afterClassSetup();
  }

  @Test
  @Request
  public void testPublish() throws InterruptedException
  {
    try
    {

      Publish publish = eventService.publish(getPublishDTO());

      JenaExportConfig config = new JenaExportConfig();
      config.setLabel(new LocalizedValue("FHIR Export Test Data"));
      config.setOrganization(USATestData.ORG_NPS.getServerObject());
      config.setPublishUid(publish.getUid());
      config.setSystem(system);

      try
      {
        String json = new GsonBuilder().create().toJson(config);

        SynchronizationConfig synchronization = new SynchronizationConfig();
        synchronization.setConfiguration(json);
        synchronization.setOrganization(config.getOrganization());
        synchronization.setSystem(system.getOid());
        synchronization.getLabel().setValue("FHIR Export Test");
        synchronization.setSynchronizationType(config.getSynchronizationType());
        synchronization.apply();

        config = synchronization.toConfiguration();

        this.service.execute(synchronization, config, null);

        // Test that the export only gets exported once
        this.service.execute(synchronization, config, null);

        StringBuilder statement = new StringBuilder();
        statement.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "\n");
        statement.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "\n");
        statement.append("SELECT * FROM <http://terraframe.com/g1> WHERE {" + "\n");
        statement.append("    <http://terraframe.com/USATestDataState-USATestDataColorado> ?pred ?obj ." + "\n");
        statement.append("  } LIMIT 10" + "\n");

        Optional<String> optional = this.remoteService.query(statement.toString(), config);

        Assert.assertTrue(optional.isPresent());

        String oResult = optional.get();

        Assert.assertFalse(StringUtils.isBlank(oResult));
        Assert.assertFalse(StringUtils.isBlank(oResult));

        JsonObject obj = JsonParser.parseString(oResult).getAsJsonObject();

        JsonObject results = obj.get("results").getAsJsonObject();

        JsonArray bindings = results.get("bindings").getAsJsonArray();

        Assert.assertEquals(10, bindings.size());
      }
      finally
      {
        try
        {
          this.remoteService.clear(JenaSynchronizationService.GRAPH_NAME, config);
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
