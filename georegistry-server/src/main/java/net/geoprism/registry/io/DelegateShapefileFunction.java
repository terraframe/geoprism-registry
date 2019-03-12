package net.geoprism.registry.io;

import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.ShapefileFunction;

public class DelegateShapefileFunction implements ShapefileFunction
{
  private ShapefileFunction function;

  public DelegateShapefileFunction(ShapefileFunction function)
  {
    super();

    this.function = function;
  }

  @Override
  public String toJson()
  {
    return this.function.toJson();
  }

  @Override
  public Object getValue(FeatureRow feature)
  {
    return this.function.getValue(feature);
  }
}
