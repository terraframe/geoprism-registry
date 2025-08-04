package net.geoprism.registry.service.business;

import java.util.List;
import java.util.Optional;

import org.apache.http.message.BasicNameValuePair;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.gson.JsonArray;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

import net.geoprism.registry.axon.event.remote.RemoteEvent;
import net.geoprism.registry.etl.RemoteConnectionException;
import net.geoprism.registry.lpg.adapter.RegistryBridge;
import net.geoprism.registry.lpg.adapter.RegistryConnectorFactory;
import net.geoprism.registry.lpg.adapter.RegistryConnectorIF;
import net.geoprism.registry.lpg.adapter.response.RegistryResponse;
import net.geoprism.registry.view.CommitDTO;
import net.geoprism.registry.view.PublishDTO;

@Service
public class RemoteClientBuilderService implements RemoteClientBuilderServiceIF
{
  public static class RemoteClient extends RegistryBridge implements RemoteClientIF
  {
    public static final String PUBLISH_API_PATH = "publish";

    public static final String COMMIT_API_PATH  = "commit";

    public RemoteClient(RegistryConnectorIF connector)
    {
      super(connector);
    }

    @Override
    public void close()
    {
      this.getConnector().close();
    }

    @Override
    public List<PublishDTO> getAll()
    {
      RegistryResponse response = this.apiGet(PUBLISH_API_PATH + "/get-all");

      if (response.isSuccess())
      {
        ObjectMapper mapper = new ObjectMapper();

        try
        {
          ObjectReader reader = mapper.readerForListOf(PublishDTO.class);

          return reader.readValue(response.getResponse());
        }
        catch (JsonProcessingException e)
        {
          throw new RemoteConnectionException(e);
        }
      }

      throw new RemoteConnectionException(response.getMessage());
    }

    @Override
    public Optional<PublishDTO> getPublish(String publishId)
    {
      RegistryResponse response = this.apiGet(PUBLISH_API_PATH + "/get", new BasicNameValuePair("uid", publishId));

      if (response.isSuccess())
      {
        ObjectMapper mapper = new ObjectMapper();

        try
        {
          return Optional.ofNullable(mapper.readValue(response.getResponse(), PublishDTO.class));
        }
        catch (JsonProcessingException e)
        {
          throw new RemoteConnectionException(e);
        }
      }

      return Optional.empty();
    }

    @Override
    public Optional<CommitDTO> getLatest(String publishId)
    {
      RegistryResponse response = this.apiGet(COMMIT_API_PATH + "/get-latest", new BasicNameValuePair("publishId", publishId));

      if (response.isSuccess())
      {
        ObjectMapper mapper = new ObjectMapper();

        try
        {
          return Optional.ofNullable(mapper.readValue(response.getResponse(), CommitDTO.class));
        }
        catch (JsonProcessingException e)
        {
          throw new RemoteConnectionException(e);
        }
      }

      return Optional.empty();
    }

    @Override
    public List<CommitDTO> getDependencies(String uid)
    {
      RegistryResponse response = this.apiGet(COMMIT_API_PATH + "/get-dependencies", new BasicNameValuePair("uid", uid));

      if (response.isSuccess())
      {
        ObjectMapper mapper = new ObjectMapper();

        try
        {
          ObjectReader reader = mapper.readerForListOf(CommitDTO.class);

          return reader.readValue(response.getResponse());
        }
        catch (JsonProcessingException e)
        {
          throw new RemoteConnectionException(e);
        }
      }

      throw new RemoteConnectionException(response.getMessage());
    }

    @Override
    public JsonArray getBusinessTypes(String uid)
    {
      RegistryResponse response = this.apiGet(COMMIT_API_PATH + "/business-types", new BasicNameValuePair("uid", uid));

      if (response.isSuccess())
      {
        return response.getJsonArray();
      }

      throw new RemoteConnectionException(response.getMessage());
    }

    @Override
    public JsonArray getGeoObjectTypes(String uid)
    {
      RegistryResponse response = this.apiGet(COMMIT_API_PATH + "/geo-object-types", new BasicNameValuePair("uid", uid));

      if (response.isSuccess())
      {
        return response.getJsonArray();
      }

      throw new RemoteConnectionException(response.getMessage());
    }

    @Override
    public JsonArray getBusinessEdgeTypes(String uid)
    {
      RegistryResponse response = this.apiGet(COMMIT_API_PATH + "/business-edge-types", new BasicNameValuePair("uid", uid));

      if (response.isSuccess())
      {
        return response.getJsonArray();
      }

      throw new RemoteConnectionException(response.getMessage());
    }

    @Override
    public JsonArray getHierarchyTypes(String uid)
    {
      RegistryResponse response = this.apiGet(COMMIT_API_PATH + "/hierarchy-types", new BasicNameValuePair("uid", uid));

      if (response.isSuccess())
      {
        return response.getJsonArray();
      }

      throw new RemoteConnectionException(response.getMessage());
    }

    @Override
    public JsonArray getDirectedAcyclicGraphTypes(String uid)
    {
      RegistryResponse response = this.apiGet(COMMIT_API_PATH + "/directed-acyclic-graph-types", new BasicNameValuePair("uid", uid));

      if (response.isSuccess())
      {
        return response.getJsonArray();
      }

      throw new RemoteConnectionException(response.getMessage());
    }

    @Override
    public JsonArray getUndirectedGraphTypes(String uid)
    {
      RegistryResponse response = this.apiGet(COMMIT_API_PATH + "/undirected-graph-types", new BasicNameValuePair("uid", uid));

      if (response.isSuccess())
      {
        return response.getJsonArray();
      }

      throw new RemoteConnectionException(response.getMessage());
    }

    @Override
    public List<RemoteEvent> getRemoteEvents(String uid, Integer chunk)
    {
      RegistryResponse response = this.apiGet(COMMIT_API_PATH + "/events", new BasicNameValuePair("uid", uid), new BasicNameValuePair("chunk", chunk.toString()));

      if (response.isSuccess())
      {
        ObjectMapper mapper = new ObjectMapper();

        try
        {
          ObjectReader reader = mapper.readerForListOf(RemoteEvent.class);

          return reader.readValue(response.getMessage());
        }
        catch (JsonProcessingException e)
        {
          throw new ProgrammingErrorException(e);
        }
      }

      throw new RemoteConnectionException(response.getMessage());
    }
  }

  @Override
  public RemoteClientIF open(String source)
  {
    return new RemoteClient(RegistryConnectorFactory.getConnector(source));
  }
}
