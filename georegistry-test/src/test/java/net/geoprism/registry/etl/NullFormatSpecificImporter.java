/**
 *
 */
package net.geoprism.registry.etl;

import org.locationtech.jts.geom.Geometry;

import net.geoprism.data.importer.FeatureRow;
import net.geoprism.registry.etl.upload.FormatSpecificImporterIF;
import net.geoprism.registry.etl.upload.ObjectImporterIF;

public class NullFormatSpecificImporter implements FormatSpecificImporterIF
{
  @Override
  public Geometry getGeometry(FeatureRow row)
  {
    return null;
  }

  @Override
  public void setObjectImporter(ObjectImporterIF objectImporter)
  {
  }

  @Override
  public void run(ImportStage stage)
  {
  }

}
