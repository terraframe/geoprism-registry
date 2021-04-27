package net.geoprism.registry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Location.LocationMode;
import org.hl7.fhir.r4.model.Location.LocationPositionComponent;
import org.hl7.fhir.r4.model.Location.LocationStatus;

import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.runwaysdk.session.Request;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.geojson.GeoJsonWriter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.query.graph.VertexGeoObjectQuery;

public class Sandbox
{

  public static void main(String[] args) throws Exception
  {
    test();

  }

  @Request
  private static void test() throws Exception
  {
//    String url = "http://localhost:8080/fhir/";
    String url = "https://fhir-gis-widget.terraframe.com:8080/fhir/";
    // Create a client
    FhirContext ctx = FhirContext.forR4();
    IGenericClient client = ctx.newRestfulGenericClient(url);

    // Location parent =
    // client.read().resource(Location.class).withId("1").execute();

    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(2021, Calendar.MARCH, 15, 0, 0);

    Date date = cal.getTime();

//     exportType(client, date, ServerGeoObjectType.get("District"));
//     exportType(client, date, ServerGeoObjectType.get("Village"));

    exportJson(client, new File("/home/jsmethie/Documents/DSME/4f0438970323fd5ee6ef42b1df668d46-d37e49daa036afb7284d194e5c9fe9de12d96143/dhis2play.json"));
  }

  private static void exportType(IGenericClient client, Date date, ServerGeoObjectType sType)
  {
    VertexGeoObjectQuery query = new VertexGeoObjectQuery(sType, date);
    List<ServerGeoObjectIF> results = query.getResults();

    for (ServerGeoObjectIF result : results)
    {
      Geometry geometry = result.getGeometry();

      if (geometry != null)
      {
        Point centroid = geometry.getCentroid();

        GeoJsonWriter writer = new GeoJsonWriter();
        String geojson = writer.write(geometry);

        Encoder encoder = Base64.getEncoder();

        // Create a location
        Attachment attachment = new Attachment();
        attachment.setContentType("application/json");
        attachment.setDataElement(new Base64BinaryType(encoder.encodeToString(geojson.getBytes())));
        attachment.setTitle("Geojson");

        Extension extension = new Extension("http://hl7.org/fhir/StructureDefinition/location-boundary-geojson");
        extension.setValue(attachment);

        // Populate the patient with fake information
        Location location = new Location();
        location.addIdentifier().setSystem("http://terraframe.com/code").setValue(result.getCode());
        location.setStatus(LocationStatus.ACTIVE);
        location.setName(result.getDisplayLabel().getValue());
        // location.addAlias("AL1");
        // location.setDescription("Strange Location");
        location.setMode(LocationMode.INSTANCE);
        location.addType(new CodeableConcept(new Coding("http://terraframe.com/code", sType.getCode(), sType.getLabel().getValue())));
        location.setPosition(new LocationPositionComponent(new DecimalType(centroid.getX()), new DecimalType(centroid.getY())));
        location.addExtension(extension);
        // location.setPartOf(new Reference("Location/1"));

        // Create the resource on the server
        MethodOutcome outcome = client.create().resource(location).execute();

        // Log the ID that the server assigned
        IIdType id = outcome.getId();

        System.out.println(id);
      }
    }
  }

  private static void exportJson(IGenericClient client, File file) throws JsonIOException, JsonSyntaxException, FileNotFoundException
  {
    JsonObject jobj = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
    JsonArray results = jobj.get("entry").getAsJsonArray();

    Map<String, IIdType> idMap = new HashMap<String, IIdType>();
    Map<String, List<JsonObject>> partOfQueue = new HashMap<String, List<JsonObject>>();
    Set<String> processed = new TreeSet<String>();

    // Process all organizations
    for (int i = 0; i < results.size(); i++)
    {
      JsonObject result = results.get(i).getAsJsonObject();
      JsonObject resource = result.get("resource").getAsJsonObject();

      String resourceType = resource.get("resourceType").getAsString();
      String id = resource.get("id").getAsString();

      if (resourceType.equals("Organization"))
      {
        // Create the resource on the server
        MethodOutcome outcome = client.create().resource(resource.toString()).execute();

        // Log the ID that the server assigned
        IIdType idType = outcome.getId();

        idMap.put("Organization/" + id, idType);

        System.out.println(id + " - " + idType.getResourceType() + "/" + idType.getIdPart());
      }
    }

    for (int i = 0; i < results.size(); i++)
    {
      JsonObject result = results.get(i).getAsJsonObject();

      JsonObject resource = result.get("resource").getAsJsonObject();

      processLocation(client, idMap, partOfQueue, processed, resource);
    }
  }

  private static void processLocation(IGenericClient client, Map<String, IIdType> idMap, Map<String, List<JsonObject>> partOfQueue, Set<String> processed, JsonObject resource)
  {
    String resourceType = resource.get("resourceType").getAsString();

    if (!resourceType.equals("Organization"))
    {
      String id = resource.get("id").getAsString();

      String key = "Location/" + id;
      if (resource.has("partOf"))
      {
        JsonObject partOf = resource.get("partOf").getAsJsonObject();
        String partOfReference = partOf.get("reference").getAsString();

        if (processed.contains(partOfReference))
        {
          IIdType partOfIdType = idMap.get(partOfReference);

          // Update the partOf value
          partOf.addProperty("reference", partOfIdType.getResourceType() + "/" + partOfIdType.getIdPart());

          process(client, idMap, partOfQueue, processed, resource, key);
        }
        else
        {
          partOfQueue.putIfAbsent(partOfReference, new LinkedList<>());
          partOfQueue.get(partOfReference).add(resource);
        }
      }
      else
      {
        process(client, idMap, partOfQueue, processed, resource, key);
      }
    }
  }

  private static void process(IGenericClient client, Map<String, IIdType> idMap, Map<String, List<JsonObject>> partOfQueue, Set<String> processed, JsonObject resource, String key)
  {
    if (resource.has("managingOrganization"))
    {
      JsonObject managingOrganization = resource.get("managingOrganization").getAsJsonObject();
      String reference = managingOrganization.get("reference").getAsString();

      IIdType organizationIdType = idMap.get(reference);

      managingOrganization.addProperty("reference", organizationIdType.getResourceType() + "/" + organizationIdType.getIdPart());
    }

    // Create the resource on the server
    MethodOutcome outcome = client.create().resource(resource.toString()).execute();

    // Log the ID that the server assigned
    IIdType response = outcome.getId();

    processed.add(key);

    System.out.println("Location: " + response);

    idMap.put(key, response);

    if (partOfQueue.containsKey(key))
    {
      List<JsonObject> children = partOfQueue.get(key);

      for (JsonObject child : children)
      {
        processLocation(client, idMap, partOfQueue, processed, child);
      }

      partOfQueue.remove(key);
    }
  }
}
