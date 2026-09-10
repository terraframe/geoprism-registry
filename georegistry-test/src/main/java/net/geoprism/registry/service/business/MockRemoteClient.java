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
import net.geoprism.registry.model.DataSourceDTO;
import net.geoprism.registry.model.SourceAuthorityDTO;
import net.geoprism.registry.view.BusinessEdgeTypeDTO;
import net.geoprism.registry.view.BusinessTypeDTO;
import net.geoprism.registry.view.CommitDTO;
import net.geoprism.registry.view.ConceptClassDTO;
import net.geoprism.registry.view.ConceptEdgeTypeDTO;
import net.geoprism.registry.view.ConceptSetDTO;
import net.geoprism.registry.view.PublishDTO;

public class MockRemoteClient implements RemoteClientIF
{
  public static String REMOTE_ORIGIN = "REMOTE";

  protected String getCommitFolder(String uid)
  {
    return uid;
  }

  @Override
  public List<PublishDTO> getAll()
  {
    return new LinkedList<>();
  }

  @Override
  public List<CommitDTO> getDependencies(String uid)
  {
    ObjectMapper mapper = new ObjectMapper();
    ObjectReader reader = mapper.readerForListOf(CommitDTO.class);

    try
    {
      return reader.readValue(this.getClass().getResourceAsStream("/commit/" + getCommitFolder(uid) + "/dependencies.json"));
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<SourceAuthorityDTO> getSourceAuthorities(String uid)
  {
    ObjectMapper mapper = new ObjectMapper();
    ObjectReader reader = mapper.readerForListOf(SourceAuthorityDTO.class);

    try
    {
      return reader.readValue(this.getClass().getResourceAsStream("/commit/" + getCommitFolder(uid) + "/authorities.json"));
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<DataSourceDTO> getDataSources(String uid)
  {
    ObjectMapper mapper = new ObjectMapper();
    ObjectReader reader = mapper.readerForListOf(DataSourceDTO.class);

    try
    {
      return reader.readValue(this.getClass().getResourceAsStream("/commit/" + getCommitFolder(uid) + "/sources.json"));
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<RemoteEvent> getRemoteEvents(String uid, Integer chunk)
  {
    if (chunk == 0)
    {
      ObjectMapper mapper = new ObjectMapper();
      ObjectReader reader = mapper.readerForListOf(RemoteEvent.class);

      try (InputStream stream = this.getClass().getResourceAsStream("/commit/" + getCommitFolder(uid) + "/events.json"))
      {
        if (stream != null)
        {
          List<RemoteEvent> events = reader.readValue(stream);
          events.forEach(e -> e.setCommitId(uid));

          return events;
        }
      }
      catch (IOException e)
      {
        throw new RuntimeException(e);
      }
    }

    return new LinkedList<>();
  }

  @Override
  public Optional<PublishDTO> getPublish(String uid)
  {
    return Optional.ofNullable(readPublish("/commit/" + getCommitFolder(uid) + "/publish.json"));
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
    try (InputStream stream = this.getClass().getResourceAsStream("/commit/" + getCommitFolder(uid) + "/hierarchy-types.json"))
    {
      return process(stream);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public JsonArray getGeoObjectTypes(String uid)
  {
    try (InputStream stream = this.getClass().getResourceAsStream("/commit/" + getCommitFolder(uid) + "/geo-object-types.json"))
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
    try (InputStream stream = this.getClass().getResourceAsStream("/commit/" + getCommitFolder(uid) + "/dag-types.json"))
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
    try (InputStream stream = this.getClass().getResourceAsStream("/commit/" + getCommitFolder(uid) + "/undirected-graph-types.json"))
    {
      return process(stream);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<BusinessTypeDTO> getBusinessTypes(String uid)
  {
    ObjectMapper mapper = new ObjectMapper();
    ObjectReader reader = mapper.readerForListOf(BusinessTypeDTO.class);

    try (InputStream stream = this.getClass().getResourceAsStream("/commit/" + getCommitFolder(uid) + "/business-types.json"))
    {
      if (stream != null)
      {
        List<BusinessTypeDTO> value = reader.readValue(stream);

        value.stream().forEach(t -> {
          t.setOrigin(REMOTE_ORIGIN);
          t.setSequence(20L);
        });

        return value;
      }
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }

    return new LinkedList<>();
  }

  @Override
  public List<ConceptClassDTO> getConceptClasses(String uid)
  {
    ObjectMapper mapper = new ObjectMapper();
    ObjectReader reader = mapper.readerForListOf(ConceptClassDTO.class);

    try (InputStream stream = this.getClass().getResourceAsStream("/commit/" + getCommitFolder(uid) + "/concept-classes.json"))
    {
      if (stream != null)
      {
        List<ConceptClassDTO> value = reader.readValue(stream);

        value.stream().forEach(t -> {
          t.setOrigin(REMOTE_ORIGIN);
          t.setSequence(20L);
        });

        return value;
      }
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }

    return new LinkedList<>();
  }

  @Override
  public List<BusinessEdgeTypeDTO> getBusinessEdgeTypes(String uid)
  {
    ObjectMapper mapper = new ObjectMapper();
    ObjectReader reader = mapper.readerForListOf(BusinessEdgeTypeDTO.class);

    try (InputStream stream = this.getClass().getResourceAsStream("/commit/" + getCommitFolder(uid) + "/business-edge-types.json"))
    {
      if (stream != null)
      {
        List<BusinessEdgeTypeDTO> value = reader.readValue(stream);

        value.stream().forEach(t -> {
          t.setOrigin(REMOTE_ORIGIN);
          t.setSeq(20L);
        });

        return value;
      }
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }

    return new LinkedList<>();
  }

  @Override
  public List<ConceptSetDTO> getConceptSets(String uid)
  {
    ObjectMapper mapper = new ObjectMapper();
    ObjectReader reader = mapper.readerForListOf(ConceptSetDTO.class);

    try (InputStream stream = this.getClass().getResourceAsStream("/commit/" + getCommitFolder(uid) + "/concept-sets.json"))
    {
      if (stream != null)
      {
        List<ConceptSetDTO> value = reader.readValue(stream);

        value.stream().forEach(t -> {
          t.setOrigin(REMOTE_ORIGIN);
          t.setSequence(20L);
        });

        return value;
      }
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }

    return new LinkedList<>();
  }

  @Override
  public List<ConceptEdgeTypeDTO> getConceptEdgeTypes(String uid)
  {
    ObjectMapper mapper = new ObjectMapper();
    ObjectReader reader = mapper.readerForListOf(ConceptEdgeTypeDTO.class);

    try (InputStream stream = this.getClass().getResourceAsStream("/commit/" + getCommitFolder(uid) + "/concept-edge-types.json"))
    {
      if (stream != null)
      {

        List<ConceptEdgeTypeDTO> value = reader.readValue(stream);

        value.stream().forEach(t -> {
          t.setOrigin(REMOTE_ORIGIN);
          t.setSeq(20L);
        });

        return value;
      }
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }

    return new LinkedList<>();
  }

  @Override
  public Optional<CommitDTO> getLatest(String publishId)
  {
    return Optional.ofNullable(readCommit("/commit/" + getCommitFolder(publishId) + "/commit.json"));
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
