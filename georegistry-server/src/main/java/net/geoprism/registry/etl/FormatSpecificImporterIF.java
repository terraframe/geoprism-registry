package net.geoprism.registry.etl;

import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.data.importer.FeatureRow;

public interface FormatSpecificImporterIF
{
  public Geometry getGeometry(FeatureRow row);

  public void setObjectImporter(ObjectImporterIF objectImporter);

  public void setStartIndex(Long workProgress);

  public void run(ImportStage stage);
}
