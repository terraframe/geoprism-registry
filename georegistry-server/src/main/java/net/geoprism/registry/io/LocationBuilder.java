package net.geoprism.registry.io;

import net.geoprism.data.importer.ShapefileFunction;

public interface LocationBuilder
{
  public Location build(ShapefileFunction function);
}
