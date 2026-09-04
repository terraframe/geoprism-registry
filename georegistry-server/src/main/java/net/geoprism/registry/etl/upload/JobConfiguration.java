package net.geoprism.registry.etl.upload;

import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
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
      ImportConfigurationDTO config = (ImportConfigurationDTO) dto;

      return (T) ImportConfiguration.build(config, config.getFormatType().equals(FormatImporterType.EXCEL));
    }

    return (T) new ExportConfiguration();
  }
}
