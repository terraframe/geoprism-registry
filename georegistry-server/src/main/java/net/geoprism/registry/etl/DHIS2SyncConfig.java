package net.geoprism.registry.etl;

import java.util.List;

import net.geoprism.registry.graph.DHIS2ExternalSystem;

public class DHIS2SyncConfig extends ExternalSystemSyncConfig
{
  private List<SyncLevel> levels;

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

}
