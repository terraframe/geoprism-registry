/**
 *
 */
package net.geoprism.registry.etl;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.lib.IRILib;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.Pair;
import com.runwaysdk.session.Request;

import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.Publish;
import net.geoprism.registry.PublishDatasetTest;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.JenaExternalSystem;
import net.geoprism.registry.service.business.InMemoryJenaService;
import net.geoprism.registry.service.business.JenaSynchronizationService;
import net.geoprism.registry.service.business.PublishEventService;
import net.geoprism.registry.test.USATestData;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
public class JenaSynchronizationServiceTest extends PublishDatasetTest implements InstanceTestClassListener
{
  @Autowired
  private PublishEventService        eventService;

  @Autowired
  private InMemoryJenaService        remoteService;

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
  public void testPublish() throws InterruptedException, ExecutionException
  {
    try
    {
      Pair<Publish, Future<?>> response = eventService.publish(getPublishDTO());

      Publish publish = response.getFirst();

      // Wait for the commit to finish publishing
      response.getSecond().get();

      JenaExportConfig config = new JenaExportConfig();
      config.setLabel(new LocalizedValue("Test Synchronization"));
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

        final String ns = config.getNamespace() + "#";

        JsonArray bindings = querySubject(ns, USATestData.COLORADO.getGeoObjectType().getCode(), USATestData.COLORADO.getCode(), config);

        Assertions.assertEquals(12, bindings.size());
        final String xsdBoolean = "http://www.w3.org/2001/XMLSchema#boolean";

        // Type and default attributes
        assertUri(bindings, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", ns + "USATestDataState");
        assertLiteral(bindings, "http://www.w3.org/2000/01/rdf-schema#label", "USATestDataColorado", null);
        assertLiteral(bindings, ns + "GeoObject-code", "USATestDataColorado", null);
        assertUri(bindings, ns + "GeoObject-dataSource", ns + "DataSource-UsaSource");

        // Attributes excluded by JenaSynchronizationService.isValid
        assertExcludedAttributes(bindings, ns);

        // Classification attributes
        assertUri(bindings, ns + "USATestDataState-classification", ns + "TEST_C_CLASS-Child%20Concept");
        assertUri(bindings, ns + "USATestDataState-testClassification", ns + "TEST_C_CLASS-Parent%20Concept");

        // Geometry
        assertUri(bindings, "http://www.opengis.net/ont/geosparql#hasGeometry", ns + "USATestDataState-USATestDataColoradoGeometry");

        // Hierarchy parent
        assertUri(bindings, ns + "USATestDataAdminCode", ns + "USATestDataCountry-USATestDataUSA");

        // Graph edges
        assertUri(bindings, ns + "TEST_DAG", ns + "USATestDataCountry-USATestDataCANADA");
        assertUri(bindings, ns + "TEST_DAG", ns + "USATestDataCountry-USATestDataUSA");
        assertUri(bindings, ns + "TEST_UN", ns + "USATestDataCountry-USATestDataCANADA");
        assertUri(bindings, ns + "TEST_GEO_EDGE", ns + "TEST_BUSINESS-C_CODE");

        // Concept object
        JsonArray concept = querySubject(ns, parentConcept.getType().getCode(), parentConcept.getCode(), config);

        Assertions.assertEquals(5, concept.size());

        assertUri(concept, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", ns + "TEST_C_CLASS");
        assertLiteral(concept, "http://www.w3.org/2000/01/rdf-schema#label", "Flatbread", null);
        assertLiteral(concept, ns + "GeoObject-code", "Parent Concept", null);
        assertUri(concept, ns + "GeoObject-dataSource", ns + "DataSource-UsaSource");
        assertUri(concept, ns + "TEST_CONCEPT_EDGE", ns + "TEST_C_CLASS-Child%20Concept");
        assertExcludedAttributes(concept, ns);

        // Business object
        JsonArray business = querySubject(ns, pObject.getType().getCode(), pObject.getCode(), config);

        Assertions.assertEquals(6, business.size());

        assertUri(business, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", ns + "TEST_BUSINESS");
        assertLiteral(business, ns + "GeoObject-code", "P_CODE", null);
        assertUri(business, ns + "GeoObject-dataSource", ns + "DataSource-UsaSource");
        assertLiteral(business, ns + "TEST_BUSINESS-testBoolean", "false", xsdBoolean);
        assertUri(business, ns + "TEST_BUSINESS-testClassification", ns + "TEST_C_CLASS-Parent%20Concept");
        assertUri(business, ns + "TEST_B_EDGE", ns + "TEST_BUSINESS-C_CODE");
        assertExcludedAttributes(business, ns);

      }
      finally
      {
        try
        {
          this.remoteService.clear(config);
        }
        finally
        {
          delete(publish);
        }
      }
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }

  private JsonArray querySubject(String namespace, String type, String code, JenaExportConfig config)
  {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT *" + "\n");
    statement.append("FROM <" + config.getGraph() + ">" + "\n");
    statement.append("WHERE {" + "\n");
    statement.append("    <" + namespace + type + "-" + IRILib.encodeUriComponent(code) + "> ?pred ?obj ." + "\n");
    statement.append("}" + "\n");

    Optional<String> optional = this.remoteService.query(statement.toString(), config);

    Assertions.assertTrue(optional.isPresent());
    Assertions.assertFalse(StringUtils.isBlank(optional.get()));

    JsonObject obj = JsonParser.parseString(optional.get()).getAsJsonObject();

    return obj.get("results").getAsJsonObject().get("bindings").getAsJsonArray();
  }

  /**
   * Asserts that no triples exist for the default attributes which are
   * filtered out by JenaSynchronizationService.isValid
   */
  private static void assertExcludedAttributes(JsonArray bindings, String ns)
  {
    Stream.of(DefaultAttribute.TYPE, DefaultAttribute.UID, DefaultAttribute.INVALID, DefaultAttribute.EXISTS).forEach(attribute -> {
      String predicate = ns + "GeoObject-" + attribute.getName();

      Assertions.assertEquals(0, getBindings(bindings, predicate).count(), "Unexpected triple for excluded attribute [" + predicate + "]");
    });
  }

  private static Stream<JsonObject> getBindings(JsonArray bindings, String predicate)
  {
    return StreamSupport.stream(bindings.spliterator(), false) //
        .map(JsonElement::getAsJsonObject) //
        .filter(b -> b.get("pred").getAsJsonObject().get("value").getAsString().equals(predicate));
  }

  private static JsonObject getBinding(JsonArray bindings, String predicate)
  {
    return getBindings(bindings, predicate).findFirst() //
        .orElseThrow(() -> new AssertionError("Missing triple with predicate [" + predicate + "]"));
  }

  private static void assertUri(JsonArray bindings, String predicate, String expected)
  {
    boolean found = getBindings(bindings, predicate) //
        .map(b -> b.get("obj").getAsJsonObject()) //
        .anyMatch(o -> o.get("type").getAsString().equals("uri") && o.get("value").getAsString().equals(expected));

    Assertions.assertTrue(found, "Missing triple [" + predicate + "] -> <" + expected + ">");
  }

  private static void assertLiteral(JsonArray bindings, String predicate, String expected, String datatype)
  {
    JsonObject o = getBinding(bindings, predicate).get("obj").getAsJsonObject();

    Assertions.assertEquals("literal", o.get("type").getAsString());
    Assertions.assertEquals(expected, o.get("value").getAsString());

    if (datatype != null)
    {
      Assertions.assertEquals(datatype, o.get("datatype").getAsString());
    }
  }
}
