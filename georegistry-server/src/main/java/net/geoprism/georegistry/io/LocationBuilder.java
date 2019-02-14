package net.geoprism.georegistry.io;

import net.geoprism.data.importer.ShapefileFunction;

public interface LocationBuilder
{
  public Location build(ShapefileFunction function);
}
