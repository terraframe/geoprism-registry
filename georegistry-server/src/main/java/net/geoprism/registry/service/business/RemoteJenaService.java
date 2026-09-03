package net.geoprism.registry.service.business;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import org.apache.commons.lang.StringUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.update.UpdateFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import net.geoprism.registry.etl.AWSSigV4HttpClient;
import net.geoprism.registry.etl.JenaExportConfig;
import net.geoprism.registry.etl.RemoteConnectionException;
import net.geoprism.registry.graph.ExternalSystem.AuthType;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;

@Service
public class RemoteJenaService implements RemoteJenaServiceIF
{
  
  private static final Logger logger = LoggerFactory.getLogger(RemoteJenaService.class);
  
  protected Optional<RDFConnectionRemoteBuilder> builder(JenaExportConfig config)
  {
    String baseUrl = config.getSystem().getUrl();

    if (StringUtils.isBlank(baseUrl))
    {
      return Optional.empty();
    }

    if (baseUrl.endsWith("/"))
    {
      baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
    }

    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.newBuilder();

    if (baseUrl.contains(".neptune.amazonaws.com"))
    {
      builder
          .gspEndpoint(baseUrl + "/sparql/gsp/")
          .queryEndpoint(baseUrl + "/sparql")
          .updateEndpoint(baseUrl + "/sparql");
    }
    else
    {
      builder
          .gspEndpoint(baseUrl + "/data")
          .queryEndpoint(baseUrl + "/sparql")
          .updateEndpoint(baseUrl + "/update");
    }

    if (AuthType.IAM.equals(config.getSystem().getAuthType()))
    {
      AwsCredentialsProvider credentialsProvider = DefaultCredentialsProvider.builder().build();

      Region region = DefaultAwsRegionProviderChain.builder()
          .build()
          .getRegion();

      HttpClient signingClient = new AWSSigV4HttpClient(region, credentialsProvider);

      builder.httpClient(signingClient);
    }

    return Optional.of(builder);
  }

  @Override
  public void load(Model model, JenaExportConfig config)
  {
    this.builder(config).ifPresent(configuration -> {

      // Connect to the remote RDF store
      try (RDFConnection conn = configuration.build())
      {
        // Add the model (containing the triple) to the remote store
        conn.load(config.getGraph(), model);
      }
      catch (Exception e)
      {
        throw new RemoteConnectionException(e);
      }
    });
  }

  @Override
  public void update(List<String> statements, JenaExportConfig config)
  {
    this.builder(config).ifPresent(configuration -> {

      // Connect to the remote RDF store
      try (RDFConnection conn = configuration.build())
      {
        // Execute the query
        for (String statement : statements)
        {
          conn.update(UpdateFactory.create(statement));
        }
      }
      catch (Exception e)
      {
        throw new RemoteConnectionException(e);
      }
    });
  }

  @Override
  public void clear(JenaExportConfig config)
  {
    this.builder(config).ifPresent(configuration -> {
      StringBuilder statement = new StringBuilder();
      statement.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "\n");
      statement.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "\n");
      statement.append("CLEAR GRAPH <" + config.getGraph() + ">" + "\n");

      // Connect to the remote RDF store
      try (RDFConnection conn = configuration.build())
      {
        conn.update(UpdateFactory.create(statement.toString()));
      }
    });
  }

  @Override
  public Optional<String> query(String statement, JenaExportConfig config)
  {
    return this.builder(config).map(configuration -> {

      // Connect to the remote RDF store
      try (RDFConnection conn = configuration.build())
      {
        // Execute the query
        QueryExecution query = conn.query(statement);
        ResultSet result = query.execSelect();

        try (ByteArrayOutputStream ostream = new ByteArrayOutputStream())
        {
          ResultSetFormatter.outputAsJSON(ostream, result);

          return ostream.toString();
        }
      }
      catch (Exception e)
      {
        throw new RemoteConnectionException(e);
      }
    });
  }
}
