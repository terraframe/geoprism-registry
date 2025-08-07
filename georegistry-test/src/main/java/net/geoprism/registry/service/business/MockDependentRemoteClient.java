package net.geoprism.registry.service.business;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonArray;

import net.geoprism.registry.axon.event.remote.RemoteEvent;
import net.geoprism.registry.view.CommitDTO;
import net.geoprism.registry.view.PublishDTO;

public class MockDependentRemoteClient extends MockRemoteClient
{

  public static String DEPENDENT  = "c38a9fbd-46bc-4b00-8552-1cdf3c2bb200";

  public static String DEPENDENCY = "c38a9fbd-46bc-4b00-8552-1cdf3c2bb100";

  @Override
  public Optional<PublishDTO> getPublish(String publishId)
  {
    if (publishId.equals(DEPENDENT))
    {
      PublishDTO dto = readPublish("/commit/publish_2.json");
      dto.setUid(publishId);

      return Optional.ofNullable(dto);
    }

    PublishDTO dto = readPublish("/commit/publish_1.json");
    dto.setUid(publishId);

    return Optional.ofNullable(dto);
  }

  @Override
  public List<CommitDTO> getDependencies(String commitId)
  {
    if (commitId.equals(DEPENDENT))
    {
      CommitDTO dto = readCommit();
      dto.setUid(DEPENDENCY);
      dto.setPublishId(DEPENDENCY);

      return Arrays.asList(dto);
    }

    return super.getDependencies(commitId);
  }

  @Override
  public Optional<CommitDTO> getLatest(String publishId)
  {
    CommitDTO dto = this.readCommit();
    dto.setPublishId(publishId);
    dto.setUid(publishId);

    return Optional.of(dto);
  }

  @Override
  public JsonArray getBusinessEdgeTypes(String uid)
  {
    if (uid.equals(DEPENDENT))
    {
      return super.getBusinessEdgeTypes(uid);
    }

    return new JsonArray();
  }

  @Override
  public JsonArray getDirectedAcyclicGraphTypes(String uid)
  {
    if (uid.equals(DEPENDENT))
    {
      return super.getDirectedAcyclicGraphTypes(uid);
    }

    return new JsonArray();
  }

  @Override
  public JsonArray getBusinessTypes(String uid)
  {
    if (uid.equals(DEPENDENT))
    {
      return super.getBusinessTypes(uid);
    }

    return new JsonArray();
  }

  @Override
  public JsonArray getUndirectedGraphTypes(String uid)
  {
    if (uid.equals(DEPENDENT))
    {
      return super.getUndirectedGraphTypes(uid);
    }

    return new JsonArray();
  }

  @Override
  public List<RemoteEvent> getRemoteEvents(String uid, Integer chunk)
  {
    if (uid.equals(DEPENDENT))
    {
      return super.getRemoteEvents(uid, chunk);
    }

    return new LinkedList<>();
  }

  @Override
  public JsonArray getGeoObjectTypes(String uid)
  {
    if (uid.equals(DEPENDENCY))
    {
      return super.getGeoObjectTypes(uid);
    }

    return new JsonArray();
  }

  @Override
  public JsonArray getHierarchyTypes(String uid)
  {
    if (uid.equals(DEPENDENCY))
    {
      return super.getHierarchyTypes(uid);
    }

    return new JsonArray();
  }
}
