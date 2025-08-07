package net.geoprism.registry.service.business;

import java.util.Optional;
import java.util.UUID;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.registry.view.CommitDTO;

public class MockStaleRemoteClient extends MockRemoteClient
{
  @Override
  public Optional<CommitDTO> getLatest(String publishId)
  {
    return super.getLatest(publishId).map(commit -> {
      commit.setUid(UUID.randomUUID().toString());

      return commit;
    });
  }

  @Override
  protected JsonObject process(JsonElement element)
  {
    LocalizedValue value = new LocalizedValue("TEST");
    value.setValue(LocalizedValue.DEFAULT_LOCALE, "TEST");

    JsonObject object = super.process(element);
    object.addProperty(GeoObjectTypeSnapshot.SEQUENCE, 10);
    object.add(GeoObjectTypeSnapshot.DISPLAYLABEL, value.toJSON());

    return object;
  }
}
