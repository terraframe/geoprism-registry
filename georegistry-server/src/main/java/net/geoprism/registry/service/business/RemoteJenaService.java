package net.geoprism.registry.service.business;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.update.UpdateFactory;
import org.springframework.stereotype.Service;

import net.geoprism.registry.etl.RemoteConnectionException;

@Service
public class RemoteJenaService implements RemoteJenaServiceIF
{

  protected Optional<RDFConnectionRemoteBuilder> configuration()
  {
    // String url = GeoregistryProperties.getBaseJenaUrl();
    //
    // if (!StringUtils.isBlank(url))
    // {
    // RDFConnectionRemoteBuilder builder =
    // RDFConnectionRemote.newBuilder().gspEndpoint(null);
    //
    // return Optional.of(builder);
    // }
    // return Optional.empty();
    String baseUrl = "http://localhost:3030/test";

    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.newBuilder()//
        .gspEndpoint(baseUrl + "/data") //
        .queryEndpoint(baseUrl + "/sparql")//
        .updateEndpoint(baseUrl + "/update");

    return Optional.of(builder);

  }

  @Override
  public void load(String graphName, Model model)
  {
    this.configuration().ifPresent(configuration -> {

      // Connect to the remote RDF store
      try (RDFConnection conn = configuration.build())
      {
        // Add the model (containing the triple) to the remote store
        conn.load(graphName, model);
      }
      catch (Exception e)
      {
        throw new RemoteConnectionException(e);
      }
    });
  }

  @Override
  public void update(List<String> statements)
  {
    this.configuration().ifPresent(configuration -> {

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
  public void clear(String graphName)
  {
    this.configuration().ifPresent(configuration -> {
      StringBuilder statement = new StringBuilder();
      statement.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "\n");
      statement.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "\n");
      statement.append("CLEAR GRAPH <" + graphName + ">" + "\n");

      // Connect to the remote RDF store
      try (RDFConnection conn = configuration.build())
      {
        conn.update(UpdateFactory.create(statement.toString()));
      }
    });
  }

  @Override
  public Optional<String> query(String statement)
  {
    return this.configuration().map(configuration -> {

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
