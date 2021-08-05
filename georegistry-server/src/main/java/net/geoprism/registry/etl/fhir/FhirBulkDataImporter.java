package net.geoprism.registry.etl.fhir;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.codesystems.ResourceTypes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.client.apache.ResourceEntity;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import net.geoprism.registry.graph.FhirExternalSystem;

public class FhirBulkDataImporter
{
  private FhirExternalSystem    system;

  private FhirResourceProcessor processor;

  public FhirBulkDataImporter(FhirExternalSystem system, FhirResourceProcessor processor)
  {
    super();
    this.system = system;
    this.processor = processor;
  }

  public void synchronize()
  {
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(5000, TimeUnit.MILLISECONDS);

    HttpClientBuilder builder = HttpClientBuilder.create();
    builder.setConnectionManager(connectionManager);

    CloseableHttpClient myClient = builder.build();

    FhirContext ctx = FhirContext.forR4();

    String statusUrl = initiateBulkExport(myClient, ctx);

    if (statusUrl != null)
    {
      final List<String> outputs = getExportResults(myClient, statusUrl);

      IGenericClient client = ctx.newRestfulGenericClient(this.system.getUrl());

      for (String binaryUrl : outputs)
      {
        Binary binary = client.fetchResourceFromUrl(Binary.class, binaryUrl);

        String base64 = binary.getContentAsBase64();

        byte[] result = Base64.getDecoder().decode(base64);

        IParser parser = ctx.newJsonParser();
        String message = new String(result);

        try (BufferedReader reader = new BufferedReader(new StringReader(message)))
        {
          String line = null;

          while ( ( line = reader.readLine() ) != null)
          {
            IBaseResource resource = parser.parseResource(line);

            IIdType id = resource.getIdElement();
            String resourceType = id.getResourceType();

            if (resourceType.equals(ResourceTypes.LOCATION.toCode()))
            {
              Location location = (Location) resource;

              this.processor.process(location);
            }
            else if (resourceType.equals(ResourceTypes.ORGANIZATION.toCode()))
            {
              Organization organization = (Organization) resource;

              this.processor.process(organization);
            }
          }
        }
        catch (IOException e)
        {
          throw new ProgrammingErrorException(e);
        }
      }
    }

  }

  private List<String> getExportResults(CloseableHttpClient myClient, final String statusUrl)
  {
    final List<String> outputs = new LinkedList<>();

    try
    {

      boolean complete = false;

      while (!complete)
      {
        try (CloseableHttpResponse response = myClient.execute(new HttpGet(statusUrl)))
        {
          if (response.getStatusLine().getStatusCode() == 202)
          {
            int retry = Integer.parseInt(response.getFirstHeader(Constants.HEADER_RETRY_AFTER).getValue());

            // Wait for response to be ready
            Thread.sleep( ( retry * 1000 ));
          }
          else if (response.getStatusLine().getStatusCode() == 200)
          {
            try
            {
              StringWriter writer = new StringWriter();
              IOUtils.copy(response.getEntity().getContent(), writer, "utf-8");
              String content = writer.toString();

              JsonObject object = JsonParser.parseString(content).getAsJsonObject();

              if (object.has("output"))
              {
                JsonArray output = object.get("output").getAsJsonArray();

                for (int i = 0; i < output.size(); i++)
                {
                  JsonObject item = output.get(i).getAsJsonObject();
                  String url = item.get("url").getAsString();

                  outputs.add(url);
                }
              }
            }
            finally
            {
              complete = true;
            }
          }
          else
          {
            // ERROR
            StringWriter writer = new StringWriter();
            IOUtils.copy(response.getEntity().getContent(), writer, "utf-8");
            String content = writer.toString();

            throw new ProgrammingErrorException(content);
          }
        }
      }
    }
    catch (IOException | InterruptedException e)
    {
      throw new ProgrammingErrorException(e);
    }

    return outputs;
  }

  private String initiateBulkExport(CloseableHttpClient myClient, FhirContext ctx)
  {
    try
    {
      Parameters params = new Parameters();
      params.addParameter().setName("_type").setValue(new StringType("Organization,Location"));

      HttpPost post = new HttpPost(system.getUrl() + "/" + "$export");
      post.addHeader(Constants.HEADER_PREFER, Constants.HEADER_PREFER_RESPOND_ASYNC);
      post.setEntity(new ResourceEntity(ctx, params));

      try (CloseableHttpResponse response = myClient.execute(post))
      {
        if (response.getStatusLine().getStatusCode() == 202)
        {
          return response.getFirstHeader(Constants.HEADER_CONTENT_LOCATION).getValue();
        }
        else
        {
          System.out.println(response.getStatusLine().getStatusCode());

          StringWriter writer = new StringWriter();
          IOUtils.copy(response.getEntity().getContent(), writer, "utf-8");
          String message = writer.toString();

          throw new ProgrammingErrorException(message);
        }
      }
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
}
