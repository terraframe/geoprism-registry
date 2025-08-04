package net.geoprism.registry.service.business;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.registry.axon.event.remote.RemoteEvent;
import net.geoprism.registry.view.CommitDTO;
import net.geoprism.registry.view.PublishDTO;

public class MockRemoteClient implements RemoteClientIF
{
  public static String REMOTE_ORIGIN = "REMOTE";

  @Override
  public List<PublishDTO> getAll()
  {
    return new LinkedList<>();
  }

  @Override
  public List<CommitDTO> getDependencies(String commitId)
  {
    return new LinkedList<>();
  }

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
    return Optional.ofNullable(readPublish("/commit/publish.json"));
  }

  protected PublishDTO readPublish(String file)
  {
    try
    {
      ObjectMapper mapper = new ObjectMapper();

      PublishDTO dto = mapper.readValue(this.getClass().getResourceAsStream(file), PublishDTO.class);
      dto.setOrigin(REMOTE_ORIGIN);

      return dto;
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
      return process(stream);
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
      return process(stream);
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
      return process(stream);
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
      return process(stream);
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
      return process(stream);
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
      return process(stream);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<CommitDTO> getLatest(String publishId)
  {
    return Optional.ofNullable(readCommit());
  }

  protected CommitDTO readCommit()
  {
    return readCommit("/commit/commit.json");
  }

  protected CommitDTO readCommit(String file)
  {
    try
    {
      ObjectMapper mapper = new ObjectMapper();

      return mapper.readValue(this.getClass().getResourceAsStream(file), CommitDTO.class);
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

  protected JsonArray process(InputStream stream) throws IOException
  {
    try (InputStreamReader reader = new InputStreamReader(stream))
    {
      JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();

      array.forEach(element -> {
        process(element);
      });

      return array;
    }
  }

  protected JsonObject process(JsonElement element)
  {
    JsonObject object = element.getAsJsonObject();
    object.addProperty(GeoObjectTypeSnapshot.ORIGIN, REMOTE_ORIGIN);
    object.addProperty(GeoObjectTypeSnapshot.SEQUENCE, 20);
    return object;
  }

}
