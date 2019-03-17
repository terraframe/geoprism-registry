package net.geoprism.registry.excel;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.jaitools.jts.CoordinateSequence2D;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.registry.io.GeoObjectConfiguration;

public class GeoObjectConverter extends FeatureRowImporter
{
  private GeometryFactory factory;

  public GeoObjectConverter(GeoObjectConfiguration configuration)
  {
    super(configuration);

    this.factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
  }

  @Override
  protected void setValue(GeoObject entity, AttributeType attributeType, String attributeName, Object value)
  {
    if (attributeType instanceof AttributeTermType)
    {
      this.setTermValue(entity, attributeType, attributeName, value);
    }
    else if (attributeType instanceof AttributeIntegerType)
    {
      entity.setValue(attributeName, new Long((String) value));
    }
    else if (attributeType instanceof AttributeFloatType)
    {
      entity.setValue(attributeName, new Double((String) value));
    }
    else if (attributeType instanceof AttributeCharacterType)
    {
      entity.setValue(attributeName, value.toString());
    }
    else if (attributeType instanceof AttributeBooleanType)
    {
      entity.setValue(attributeName, value);
    }
    else
    {
      entity.setValue(attributeName, value);
    }
  }

  @Override
  protected Geometry getGeometry(FeatureRow row)
  {
    ShapefileFunction latitudeFunction = this.getConfiguration().getFunction(GeoObjectConfiguration.LATITUDE);
    ShapefileFunction longitudeFunction = this.getConfiguration().getFunction(GeoObjectConfiguration.LONGITUDE);

    if (latitudeFunction != null && longitudeFunction != null)
    {
      Object latitude = latitudeFunction.getValue(row);
      Object longitude = longitudeFunction.getValue(row);

      if (latitude != null && longitude != null)
      {
        Double lat = new Double(latitude.toString());
        Double lon = new Double(longitude.toString());

        return new Point(new CoordinateSequence2D(lat, lon), factory);
      }
    }

    return null;
  }
}
