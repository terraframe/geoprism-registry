package net.geoprism.georegistry.io;

import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.ShapefileFunction;

public abstract class AbstractShapefileFunction implements ShapefileFunction
{
  @Override
  public String toJson()
  {
    return null;
  }

  @Override
  public Object getValue(FeatureRow feature)
  {
    return null;
  }
}
