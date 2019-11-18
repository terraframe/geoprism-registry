package net.geoprism.registry.model.postgres;

import java.util.Locale;

import org.commongeoregistry.adapter.constants.DefaultAttribute;

import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.system.gis.geo.GeoEntity;

import net.geoprism.registry.model.LocationInfo;

public class ValueObjectContainer implements LocationInfo
{
  private ValueObject object;

  public ValueObjectContainer(ValueObject object)
  {
    super();
    this.object = object;
  }

  @Override
  public String getCode()
  {
    return object.getValue(GeoEntity.GEOID);
  }

  @Override
  public String getLabel() 
  {
    return object.getValue(DefaultAttribute.DISPLAY_LABEL.getName());
  }

  @Override
  public String getLabel(Locale locale)
  {
    return object.getValue(DefaultAttribute.DISPLAY_LABEL.getName() + "_" + locale.toString());
  }

}
