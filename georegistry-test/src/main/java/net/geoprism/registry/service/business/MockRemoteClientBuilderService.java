package net.geoprism.registry.service.business;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.registry.axon.event.remote.RemoteEvent;
import net.geoprism.registry.view.CommitDTO;
import net.geoprism.registry.view.PublishDTO;

@Service
@Primary
public class MockRemoteClientBuilderService implements RemoteClientBuilderServiceIF
{
  @Override
  public RemoteClientIF open(String source)
  {
    return new RemoteClientIF()
    {

      @Override
      public List<RemoteEvent> getRemoteEvents(String uid, Integer chunk)
      {
        if (chunk == 0)
        {
          ObjectMapper mapper = new ObjectMapper();
          ObjectReader reader = mapper.readerFor(mapper.getTypeFactory().constructCollectionLikeType(List.class, RemoteEvent.class));

          try
          {
            return reader.readValue(this.getClass().getResourceAsStream("/commit/events.json"));
          }
          catch (IOException e)
          {
            throw new RuntimeException(e);
          }
        }

        return new LinkedList<>();
      }

      @Override
      public Optional<PublishDTO> getPublish(String publishId)
      {
        ObjectMapper mapper = new ObjectMapper();

        try
        {
          return Optional.ofNullable(mapper.readValue(this.getClass().getResourceAsStream("/commit/publish.json"), PublishDTO.class));
        }
        catch (IOException e)
        {
          throw new RuntimeException(e);
        }
      }

      @Override
      public JsonArray getHierarchyTypes(String uid)
      {
        try (InputStream stream = this.getClass().getResourceAsStream("/commit/hierarchy-types.json"))
        {
          return readAndConvert(stream);
        }
        catch (IOException e)
        {
          throw new RuntimeException(e);
        }
      }

      @Override
      public JsonArray getGeoObjectTypes(String commitId)
      {
        try (InputStream stream = this.getClass().getResourceAsStream("/commit/geo-object-types.json"))
        {
          return readAndConvert(stream);
        }
        catch (IOException e)
        {
          throw new RuntimeException(e);
        }
      }

      @Override
      public JsonArray getDirectedAcyclicGraphTypes(String uid)
      {
        try (InputStream stream = this.getClass().getResourceAsStream("/commit/dag-types.json"))
        {
          return readAndConvert(stream);
        }
        catch (IOException e)
        {
          throw new RuntimeException(e);
        }
      }

      @Override
      public JsonArray getUndirectedGraphTypes(String uid)
      {
        try (InputStream stream = this.getClass().getResourceAsStream("/commit/undirected-graph-types.json"))
        {
          return readAndConvert(stream);
        }
        catch (IOException e)
        {
          throw new RuntimeException(e);
        }
      }

      @Override
      public JsonArray getBusinessTypes(String commitId)
      {
        try (InputStream stream = this.getClass().getResourceAsStream("/commit/business-types.json"))
        {
          return readAndConvert(stream);
        }
        catch (IOException e)
        {
          throw new RuntimeException(e);
        }
      }

      @Override
      public JsonArray getBusinessEdgeTypes(String uid)
      {
        try (InputStream stream = this.getClass().getResourceAsStream("/commit/business-edge-types.json"))
        {
          return readAndConvert(stream);
        }
        catch (IOException e)
        {
          throw new RuntimeException(e);
        }
      }

      @Override
      public Optional<CommitDTO> getCommit(String uid, Integer versionNumber)
      {
        ObjectMapper mapper = new ObjectMapper();

        try
        {
          return Optional.ofNullable(mapper.readValue(this.getClass().getResourceAsStream("/commit/commit.json"), CommitDTO.class));
        }
        catch (IOException e)
        {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void close()
      {
      }

      protected JsonArray readAndConvert(InputStream stream) throws IOException
      {
        try (InputStreamReader reader = new InputStreamReader(stream))
        {
          JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();

          array.forEach(element -> {
            JsonObject object = element.getAsJsonObject();
            object.addProperty(GeoObjectTypeSnapshot.ORIGIN, "REMOTE");
          });

          return array;
        }
      }

    };
  }

}
