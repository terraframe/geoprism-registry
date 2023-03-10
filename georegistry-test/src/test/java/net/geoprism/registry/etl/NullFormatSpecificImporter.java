/**
 *
 */
package net.geoprism.registry.etl;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.locationtech.jts.geom.Geometry;

import net.geoprism.data.importer.FeatureRow;
import net.geoprism.registry.etl.upload.FormatSpecificImporterIF;
import net.geoprism.registry.etl.upload.ObjectImporterIF;

public class NullFormatSpecificImporter implements FormatSpecificImporterIF
{
  @Override
  public Geometry getGeometry(FeatureRow row, GeometryType geometryType)
  {
    return null;
  }

  @Override
  public void setObjectImporter(ObjectImporterIF objectImporter)
  {
  }

  @Override
  public void setStartIndex(Long workProgress)
  {
  }

  @Override
  public void run(ImportStage stage)
  {
  }

}
