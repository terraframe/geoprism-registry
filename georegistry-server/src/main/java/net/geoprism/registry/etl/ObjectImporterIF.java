package net.geoprism.registry.etl;

import net.geoprism.data.importer.FeatureRow;

public interface ObjectImporterIF
{

  public void validateRow(FeatureRow simpleFeatureRow);

  public void importRow(FeatureRow simpleFeatureRow);

  public void setFormatSpecificImporter(FormatSpecificImporterIF formatImporter);

}
