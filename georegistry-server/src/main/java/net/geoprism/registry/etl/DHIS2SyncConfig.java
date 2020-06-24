package net.geoprism.registry.etl;

import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.model.ServerGeoObjectType;

public class DHIS2SyncConfig extends ExternalSystemSyncConfig
{
  public static final String LEVELS          = "levels";

  public static final String GEO_OBJECT_TYPE = "geoObjectType";

  public static final String TYPE            = "type";

  private List<SyncLevel>    levels;

  public List<SyncLevel> getLevels()
  {
    return levels;
  }

  public void setLevels(List<SyncLevel> levels)
  {
    this.levels = levels;
  }

  @Override
  public DHIS2ExternalSystem getSystem()
  {
    return (DHIS2ExternalSystem) super.getSystem();
  }

  @Override
  public void populate(SynchronizationConfig config)
  {
    super.populate(config);

    JsonObject json = config.getConfigurationJson();

    LinkedList<SyncLevel> levels = new LinkedList<SyncLevel>();

    JsonArray lArray = json.get(LEVELS).getAsJsonArray();

    for (int i = 0; i < lArray.size(); i++)
    {
      JsonObject object = lArray.get(i).getAsJsonObject();

      String typeCode = object.get(GEO_OBJECT_TYPE).getAsString();
      String type = object.get(TYPE).getAsString();

      SyncLevel level = new SyncLevel();
      level.setGeoObjectType(ServerGeoObjectType.get(typeCode));
      level.setSyncType(SyncLevel.Type.valueOf(type));
      level.setLevel(i + 1);

      levels.add(level);
    }

    this.setLevels(levels);
  }

}
