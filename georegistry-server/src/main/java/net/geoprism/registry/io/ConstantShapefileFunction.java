package net.geoprism.registry.io;

import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.ShapefileFunction;

public class ConstantShapefileFunction implements ShapefileFunction
{

  private String constant;

  public ConstantShapefileFunction(String constant)
  {
    super();
    this.constant = constant;
  }

  @Override
  public Object getValue(FeatureRow feature)
  {
    return this.constant;
  }

  @Override
  public String toJson()
  {
    return this.constant;
  }
}
