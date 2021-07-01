package net.geoprism.registry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
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
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Location.LocationMode;
import org.hl7.fhir.r4.model.Location.LocationPositionComponent;
import org.hl7.fhir.r4.model.Location.LocationStatus;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;

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
  private static class Context
  {
    private Map<String, IIdType>          idMap;

    private Map<String, List<JsonObject>> partOfQueue;

    private Set<String>                   processed;

    public Context()
    {
      this.idMap = new HashMap<String, IIdType>();
      this.partOfQueue = new HashMap<String, List<JsonObject>>();
      this.processed = new TreeSet<String>();
    }

    public void putId(String key, IIdType idType)
    {
      this.idMap.put(key, idType);
    }

    public IIdType getId(String reference)
    {
      return this.idMap.get(reference);
    }

    public boolean hasProcessed(String reference)
    {
      return this.processed.contains(reference);
    }

    public void addToQueue(String reference, JsonObject resource)
    {
      this.partOfQueue.putIfAbsent(reference, new LinkedList<>());
      this.partOfQueue.get(reference).add(resource);
    }

    public void processed(String reference)
    {
      this.processed.add(reference);
    }

    public boolean hasQueue(String key)
    {
      return this.partOfQueue.containsKey(key);
    }

    public List<JsonObject> getQueue(String key)
    {
      return this.partOfQueue.get(key);
    }

    public void removeQueue(String key)
    {
      this.partOfQueue.remove(key);
    }

  }

  public static void main(String[] args) throws Exception
  {
    test();

  }

  @Request
  private static void test() throws Exception
  {
    String url = "http://localhost:8080/fhir/";
    // String url = "https://fhir-gis-widget.terraframe.com:8080/fhir/";
    // Create a client
    FhirContext ctx = FhirContext.forR4();
    IGenericClient client = ctx.newRestfulGenericClient(url);

    // Location parent =
    // client.read().resource(Location.class).withId("1").execute();

    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(2021, Calendar.MARCH, 15, 0, 0);

    Date date = cal.getTime();

    // exportType(client, date, ServerGeoObjectType.get("District"));
    // exportType(client, date, ServerGeoObjectType.get("Village"));

    exportJson(client, new File("/home/jsmethie/Documents/IntraHealth/4f0438970323fd5ee6ef42b1df668d46-d37e49daa036afb7284d194e5c9fe9de12d96143/dhis2play.json"));
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

    Context context = new Context();

    // Create the root org
    Organization rootOrganization = new Organization();
    rootOrganization.addIdentifier().setSystem("http://terraframe.com/code").setValue("MOH");
    rootOrganization.setName("MOH");

    // Create the resource on the server
    IIdType rootId = client.create().resource(rootOrganization).execute().getId();

    System.out.println("Root org: " + rootId);

    ArrayList<IIdType> orgs = new ArrayList<IIdType>();

    for (int i = 0; i < 6; i++)
    {
      Organization organization = new Organization();
      organization.addIdentifier().setSystem("http://terraframe.com/code").setValue("MOH SUB ORG " + i);
      organization.setName("MOH SUB ORG " + i);
      organization.setPartOf(new Reference(rootId.getResourceType() + "/" + rootId.getIdPart()));

      if (i % 2 == 0 || i % 3 == 0)
      {
        organization.addExtension(createExtension(rootId, "Funder"));
      }

      if (i % 2 == 1 || i % 3 == 0)
      {
        organization.addExtension(createExtension(rootId, "Operational"));
      }

      // Create the resource on the server
      IIdType id = client.create().resource(organization).execute().getId();

      orgs.add(id);
    }

    // Process all organizations
    int index = 0;

    for (int i = 0; i < results.size(); i++)
    {
      JsonObject result = results.get(i).getAsJsonObject();
      JsonObject resource = result.get("resource").getAsJsonObject();

      String resourceType = resource.get("resourceType").getAsString();
      String id = resource.get("id").getAsString();

      if (resourceType.equals("Organization"))
      {
        index = index % 6;
        IIdType partOfIdType = orgs.get(index);

        JsonObject partOf = new JsonObject();
        partOf.addProperty("reference", partOfIdType.getResourceType() + "/" + partOfIdType.getIdPart());

        resource.add("partOf", partOf);

        JsonArray extensions = new JsonArray();

        if (i % 2 == 0 || i % 3 == 0)
        {
          extensions.add(createFunderExtension(partOfIdType, "Funder"));
        }

        if (i % 2 == 1 || i % 3 == 0)
        {
          extensions.add(createFunderExtension(partOfIdType, "Operational"));
        }

        resource.add("extension", extensions);

        // Create the resource on the server
        MethodOutcome outcome = client.create().resource(resource.toString()).execute();

        // Log the ID that the server assigned
        IIdType idType = outcome.getId();

        context.putId("Organization/" + id, idType);

        System.out.println(id + " - " + idType.getResourceType() + "/" + idType.getIdPart());

        index++;
      }
    }

    // Create the root type
    Location location = new Location();
    location.addIdentifier().setSystem("http://terraframe.com/code").setValue("ROOT");
    location.setStatus(LocationStatus.ACTIVE);
    location.setName("ROOT");
    location.setMode(LocationMode.INSTANCE);
    // location.setPartOf(new Reference("Location/1"));

    // Create the resource on the server
    MethodOutcome outcome = client.create().resource(location).execute();

    // Log the ID that the server assigned
    IIdType id = outcome.getId();

    for (int i = 0; i < results.size(); i++)
    {
      JsonObject result = results.get(i).getAsJsonObject();

      JsonObject resource = result.get("resource").getAsJsonObject();

      processLocation(client, context, resource, id);
    }

    createService(client, context, "GEN", "General Practice", "17", 1);
    createService(client, context, "ED", "Emergency Department", "14", 2);
    createService(client, context, "DEN", "Dental", "10", 4);
    createService(client, context, "MEN", "Mental Health", "22", 3);
  }

  private static void createService(IGenericClient client, Context context, String code, String name, String category, int filter)
  {
    HealthcareService service = new HealthcareService();
    service.addIdentifier().setSystem("http://terraframe.com/code").setValue(code);
    service.setName(name);
    service.addCategory(new CodeableConcept().setText(name).addCoding(new Coding("http://terminology.hl7.org/CodeSystem/service-category", category, name)));

    int i = 0;

    for (IIdType idType : context.idMap.values())
    {
      if (idType.getResourceType().equals("Location"))
      {

        if (i % filter == 0)
        {
          service.addLocation(new Reference(idType.getResourceType() + "/" + idType.getIdPart()));
        }

        i++;
      }
    }

    // Create the resource on the server
    client.create().resource(service).execute().getId();
  }

  private static Extension createExtension(IIdType rootId, String type)
  {
    CodeableConcept concept = new CodeableConcept();
    concept.setText(type);

    Extension rootExt = new Extension("http://ihe.net/fhir/StructureDefinition/IHE_mCSD_hierarchy_extension");
    rootExt.addExtension(new Extension("part-of", new Reference(rootId.getResourceType() + "/" + rootId.getIdPart())));
    rootExt.addExtension(new Extension("hierarchy-type", concept));
    return rootExt;
  }

  private static JsonObject createFunderExtension(IIdType referenceId, String type)
  {
    JsonObject funder = new JsonObject();
    funder.addProperty("reference", referenceId.getResourceType() + "/" + referenceId.getIdPart());

    JsonObject concept = new JsonObject();
    concept.addProperty("text", type);

    JsonObject typeExtension = new JsonObject();
    typeExtension.addProperty("url", "hierarchy-type");
    typeExtension.add("valueCodeableConcept", concept);

    JsonObject partOfExtension = new JsonObject();
    partOfExtension.addProperty("url", "part-of");
    partOfExtension.add("valueReference", funder);

    JsonArray extensions = new JsonArray();
    extensions.add(typeExtension);
    extensions.add(partOfExtension);

    JsonObject extension = new JsonObject();
    extension.addProperty("url", "http://ihe.net/fhir/StructureDefinition/IHE_mCSD_hierarchy_extension");
    extension.add("extension", extensions);
    return extension;
  }

  private static void processLocation(IGenericClient client, Context context, JsonObject resource, IIdType rootId)
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

        if (context.hasProcessed(partOfReference))
        {
          IIdType partOfIdType = context.getId(partOfReference);

          // Update the partOf value
          partOf.addProperty("reference", partOfIdType.getResourceType() + "/" + partOfIdType.getIdPart());

          process(client, context, resource, key, rootId);
        }
        else
        {
          context.addToQueue(partOfReference, resource);
        }
      }
      else
      {
        JsonObject partOf = new JsonObject();
        partOf.addProperty("reference", rootId.getResourceType() + "/" + rootId.getIdPart());

        // Update the partOf value
        resource.add("partOf", partOf);

        process(client, context, resource, key, rootId);
      }
    }
  }

  private static IIdType process(IGenericClient client, Context context, JsonObject resource, String key, IIdType rootId)
  {
    if (resource.has("managingOrganization"))
    {
      JsonObject managingOrganization = resource.get("managingOrganization").getAsJsonObject();
      String reference = managingOrganization.get("reference").getAsString();

      IIdType organizationIdType = context.getId(reference);

      managingOrganization.addProperty("reference", organizationIdType.getResourceType() + "/" + organizationIdType.getIdPart());
    }

    // Create the resource on the server
    MethodOutcome outcome = client.create().resource(resource.toString()).execute();

    // Log the ID that the server assigned
    IIdType response = outcome.getId();

    context.processed(key);

    System.out.println("Location: " + response);

    context.putId(key, response);

    if (context.hasQueue(key))
    {
      List<JsonObject> children = context.getQueue(key);

      for (JsonObject child : children)
      {
        processLocation(client, context, child, rootId);
      }

      context.removeQueue(key);
    }

    return response;
  }
}
