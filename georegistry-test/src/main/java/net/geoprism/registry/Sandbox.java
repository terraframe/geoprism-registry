/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Location.LocationMode;
import org.hl7.fhir.r4.model.Location.LocationPositionComponent;
import org.hl7.fhir.r4.model.Location.LocationStatus;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.SearchParameter;

import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.session.Request;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.geojson.GeoJsonWriter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IRestfulClientFactory;
import net.geoprism.registry.etl.FhirSyncImportConfig;
import net.geoprism.registry.etl.fhir.AbstractFhirResourceProcessor;
import net.geoprism.registry.etl.fhir.BasicFhirResourceProcessor;
import net.geoprism.registry.etl.fhir.Facility;
import net.geoprism.registry.etl.fhir.FhirConnection;
import net.geoprism.registry.etl.fhir.FhirConnectionFactory;
import net.geoprism.registry.etl.fhir.FhirResourceImporter;
import net.geoprism.registry.graph.FhirExternalSystem;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.query.graph.VertexGeoObjectQuery;

public class Sandbox
{
  private static class Context
  {
    private Bundle                        bundle;

    private AtomicInteger                 generator;

    private Map<String, IIdType>          idMap;

    private Map<String, List<JsonObject>> partOfQueue;

    private Set<String>                   processed;

    public Context()
    {
      this.bundle = new Bundle();
      this.bundle.setType(BundleType.TRANSACTION);
      this.generator = new AtomicInteger(1);
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

    public String nextId()
    {
      return Integer.toString(this.generator.getAndIncrement());
    }

  }

  public static void main(String[] args) throws Exception
  {
//    String url = "http://localhost:8082/gofr/fhir/Cgre9b41c35-7c85-46df-aeea-a4e8dbf0364e?_getpages=ee514824-fd7c-440e-b9ce-ec0c03a89179&_getpagesoffset=20&_count=20&_pretty=true&_include=Location%3Aorganization&_bundletype=searchset";
//    
//    String[] split = url.split("\\?");
//    
//    System.out.println("https://global/Cgre9b41c35-7c85-46df-aeea-a4e8dbf0364e" + "?" + split[1]);

    
    test();

  }

  @Request
  private static void test() throws Exception
  {
    String statement = "SELECT event.oid AS eventId, event.eventDate  AS eventDate, transitionType  AS eventType, event.description AS description\n" + 
        "  ,event.beforeTypeCode AS beforeType, source.code AS beforeCode, source.displayLabel_cot.value[0] AS beforeLabel\n" + 
        "  ,event.afterTypeCode AS afterType, target.code AS afterCode, target.displayLabel_cot.value[0] AS afterLabel\n" + 
        "FROM transition\n" + 
        "WHERE event.afterTypeCode = 'FASTProvince'\n" + 
        "OR event.beforeTypeCode = 'FASTProvince'\n";
    
    GraphQuery<Object> query = new GraphQuery<>(statement);
    List<Object> results = query.getRawResults();
    
    System.out.println(results.size());
    
    
//    // String url = "http://hapi.fhir.org/baseR4";
//    String url = "http://localhost:8080/fhir";
//    // String url = "https://fhir-gis-widget.terraframe.com:8080/fhir/";
//    // Create a client
//
//    // testImport(url);
//
//    // // Location parent =
//    // // client.read().resource(Location.class).withId("1").execute();
//    //
//    // Calendar cal = Calendar.getInstance();
//    // cal.clear();
//    // cal.set(2021, Calendar.MARCH, 15, 0, 0);
//    //
//    // Date date = cal.getTime();
//    //
//    // // exportType(client, date, ServerGeoObjectType.get("District"));
//    // // exportType(client, date, ServerGeoObjectType.get("Village"));
//    //
//    // exportJson(url, new
//    // File("/home/jsmethie/Documents/IntraHealth/4f0438970323fd5ee6ef42b1df668d46-d37e49daa036afb7284d194e5c9fe9de12d96143/dhis2play.json"));
//    exportBundle(url, new File("/home/jsmethie/Documents/IntraHealth/Bundle.json"));
  }

  public static void testImport(String url) throws Exception
  {
    List<FhirExternalSystem> systems = FhirExternalSystem.getAll();
    final FhirExternalSystem system = systems.get(0);

    try (FhirConnection connection = FhirConnectionFactory.get(system))
    {
      FhirResourceImporter synchronizer = new FhirResourceImporter(connection, new BasicFhirResourceProcessor(), null, null);

      synchronizer.synchronize();
    }
  }

  private static void exportBundle(IGenericClient client, File file) throws FileNotFoundException, IOException
  {
    client.transaction().withBundle(IOUtils.toString(new FileReader(file))).execute();
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

  private static BundleEntryComponent newEntry(Bundle bundle, Resource resource, IIdType resourceID)
  {
    BundleEntryComponent entry = bundle.addEntry();

    resource.setId(resourceID);
    entry.setFullUrl(resource.fhirType() + "/" + resourceID.getIdPart());
    entry.setResource(resource);

    BundleEntryRequestComponent request = entry.getRequest();
    request.setMethod(HTTPVerb.POST);
    request.setUrl(resource.getResourceType().name());
    entry.setRequest(request);

    return entry;
  }

  private static BundleEntryComponent newEntry(Context context, Resource resource)
  {
    IdDt resourceID = new IdDt(resource.getResourceType().name(), context.nextId());

    return newEntry(context.bundle, resource, resourceID);

  }

  private static void exportJson(String url, File file) throws JsonIOException, JsonSyntaxException, DataFormatException, IOException
  {
    FhirContext ctx = FhirContext.forR4();

    IRestfulClientFactory factory = ctx.getRestfulClientFactory();
    factory.setSocketTimeout(-1);

    IGenericClient client = factory.newGenericClient(url);

    JsonObject jobj = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();

    Context context = new Context();

    // Create the root org
    Organization rootOrganization = new Organization();
    rootOrganization.addIdentifier().setSystem("http://terraframe.com/code").setValue("MOH");
    rootOrganization.setName("MOH");

    // Create the resource on the server
    newEntry(context, rootOrganization);

    IdType rootId = rootOrganization.getIdElement();

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
      newEntry(context, organization);

      orgs.add(organization.getIdElement());
    }

    JsonArray results = jobj.get("entry").getAsJsonArray();

    // Process all organizations
    int index = 0;

    for (int i = 0; i < results.size(); i++)
    {
      JsonObject result = results.get(i).getAsJsonObject();
      JsonObject json = result.get("resource").getAsJsonObject();
      IBaseResource resource = FhirContext.forR4().newJsonParser().parseResource(json.toString());
      if (resource instanceof Organization)
      {
        index = index % 6;
        IIdType partOfIdType = orgs.get(index);

        Organization organization = (Organization) resource;
        organization.setPartOf(new Reference(partOfIdType.getResourceType() + "/" + partOfIdType.getIdPart()));

        String id = organization.getId();

        if (i % 2 == 0 || i % 3 == 0)
        {
          organization.addExtension(createExtension(partOfIdType, "Funder"));
        }

        if (i % 2 == 1 || i % 3 == 0)
        {
          organization.addExtension(createExtension(partOfIdType, "Operational"));
        }

        // Create the resource on the server
        newEntry(context, organization);

        IdType idType = organization.getIdElement();

        context.putId(id, idType);

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

    newEntry(context, location);

    IdType id = location.getIdElement();

    for (int i = 0; i < results.size(); i++)
    {
      JsonObject result = results.get(i).getAsJsonObject();

      JsonObject resource = result.get("resource").getAsJsonObject();

      processLocation(context, resource, id);
    }

    System.out.println("Creating services");

    createService(context, "GEN", "General Practice", "17", 1);
    createService(context, "ED", "Emergency Department", "14", 2);
    createService(context, "DEN", "Dental", "10", 4);
    createService(context, "MEN", "Mental Health", "22", 3);

    System.out.println("Creating SearchParameter");

    SearchParameter parameter = new SearchParameter();
    parameter.setUrl("http://ihe.net/fhir/StructureDefinition/IHE_mCSD_hierarchy_extension");
    parameter.setName("hierarchyExtension");
    parameter.setStatus(PublicationStatus.ACTIVE);
    parameter.setDescription("Hierarchy Extension");
    parameter.setCode("hierarchyExtension");
    parameter.setBase(Arrays.asList(new CodeType[] { new CodeType("Organization") }));
    parameter.setTarget(Arrays.asList(new CodeType[] { new CodeType("Organization") }));
    parameter.setType(SearchParamType.REFERENCE);
    parameter.setExpression("Organization.extension('http://ihe.net/fhir/StructureDefinition/IHE_mCSD_hierarchy_extension').extension('part-of')");

    newEntry(context, parameter);

    client.transaction().withBundle(context.bundle).execute();

    // FhirContext.forR4().newJsonParser().encodeResourceToWriter(context.bundle,
    // new FileWriter(new
    // File("/home/jsmethie/Documents/IntraHealth/demo.json")));
  }

  private static void exportBundle(String url, File file) throws JsonIOException, JsonSyntaxException, DataFormatException, IOException
  {
    FhirContext ctx = FhirContext.forR4();

    IRestfulClientFactory factory = ctx.getRestfulClientFactory();
    factory.setSocketTimeout(-1);

    IGenericClient client = factory.newGenericClient(url);

    IParser parser = ctx.newJsonParser();
    Bundle source = parser.parseResource(Bundle.class, new FileInputStream(file));

    Bundle target = new Bundle();
    target.setType(BundleType.TRANSACTION);

    List<BundleEntryComponent> components = source.getEntry();

    for (BundleEntryComponent component : components)
    {
      createEntries(target, component.getResource());
    }

    IParser newJsonParser = ctx.newJsonParser();
    newJsonParser.setPrettyPrint(true);

    System.out.println(newJsonParser.encodeResourceToString(target));

    client.transaction().withBundle(target).execute();
  }

  private static void createEntries(Bundle bundle, Resource resource)
  {
    IdType resourceID = resource.getIdElement();

    BundleEntryComponent entry = bundle.addEntry();
    entry.setFullUrl(resource.fhirType() + "/" + resourceID.getIdPart());
    entry.setResource(resource);

    BundleEntryRequestComponent request = entry.getRequest();
    request.setMethod(HTTPVerb.POST);
    request.setUrl(resource.getResourceType().name() + "/" + resourceID.getIdPart());
    entry.setRequest(request);
  }

  private static void createService(Context context, String code, String name, String category, int filter)
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
    newEntry(context, service);
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

  private static void processLocation(Context context, JsonObject json, IIdType rootId)
  {
    IBaseResource resource = FhirContext.forR4().newJsonParser().parseResource(json.toString());

    if (resource instanceof Location)
    {
      Location location = (Location) resource;

      String key = location.getId();
      String partOfReference = location.getPartOf().getReference();

      if (partOfReference != null)
      {
        if (context.hasProcessed(partOfReference))
        {
          IIdType partOfIdType = context.getId(partOfReference);

          // Update the partOf value
          location.setPartOf(new Reference(partOfIdType.getResourceType() + "/" + partOfIdType.getIdPart()));

          process(context, location, key, rootId);
        }
        else
        {
          context.addToQueue(partOfReference, json);
        }
      }
      else
      {
        location.setPartOf(new Reference(rootId.getResourceType() + "/" + rootId.getIdPart()));

        process(context, location, key, rootId);
      }
    }
  }

  private static IIdType process(Context context, Location location, String key, IIdType rootId)
  {
    Reference managingOrganization = location.getManagingOrganization();
    String reference = managingOrganization.getReference();

    if (reference != null)
    {
      IIdType organizationIdType = context.getId(reference);

      location.setManagingOrganization(new Reference(organizationIdType.getResourceType() + "/" + organizationIdType.getIdPart()));
    }

    // Create the resource on the server
    newEntry(context, location);

    // Log the ID that the server assigned
    IIdType response = location.getIdElement();

    context.processed(key);

    System.out.println("Location: " + response);

    context.putId(key, response);

    if (context.hasQueue(key))
    {
      List<JsonObject> children = context.getQueue(key);

      for (JsonObject child : children)
      {
        processLocation(context, child, rootId);
      }

      context.removeQueue(key);
    }

    return response;
  }
}
