package net.geoprism.registry.etl.upload;

import net.geoprism.registry.io.view.HistoryConfigurationDTO;
import net.geoprism.registry.io.view.ImportConfigurationDTO;

public abstract class JobConfiguration
{
  public abstract void enforceCreatePermissions();

  public abstract void enforceExecutePermissions();

  @SuppressWarnings("unchecked")
  public static <T extends JobConfiguration> T build(HistoryConfigurationDTO dto)
  {
    if (dto instanceof ImportConfigurationDTO)
    {
      return (T) ImportConfiguration.build(dto);
    }

    return (T) new ExportConfiguration();
  }
}
