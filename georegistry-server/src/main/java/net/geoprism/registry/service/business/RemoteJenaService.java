package net.geoprism.registry.service.business;

import java.util.List;
import java.util.Optional;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.update.UpdateFactory;
import org.springframework.stereotype.Service;

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
         e.printStackTrace();
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
         e.printStackTrace();
      }
    });
  }
}
