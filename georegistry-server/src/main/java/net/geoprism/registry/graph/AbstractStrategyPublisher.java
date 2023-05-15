package net.geoprism.registry.graph;

import java.util.Map;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeType;

import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.system.metadata.MdVertex;

import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;

public abstract class AbstractStrategyPublisher implements StrategyPublisher
{
  protected VertexObject publish(ServerGeoObjectIF object, MdVertex mdVertex)
  {
    ServerGeoObjectType type = object.getType();

    VertexObject node = new VertexObject(mdVertex.definesType());

    node.setValue(DefaultAttribute.CODE.getName(), object.getCode());

    this.setGeometryValue(object, type, node);

    Map<String, AttributeType> attributes = type.getAttributeMap();

    attributes.forEach((attributeName, attributeType) -> {

      if (node.hasAttribute(attributeName))
      {
        Object value = object.getValue(attributeName);

        if (value instanceof LocalizedValue)
        {
          LocalizedValueConverter.populate(node, attributeName, (LocalizedValue) value);
        }
        else
        {
          node.setValue(attributeName, value);
        }
      }

    });

    node.apply();

    return node;
  }

  private void setGeometryValue(ServerGeoObjectIF object, ServerGeoObjectType type, VertexObject node)
  {
    GeometryType geometryType = type.getGeometryType();

    if (geometryType.equals(GeometryType.LINE) || geometryType.equals(GeometryType.MULTILINE))
    {
      node.setValue(GeoVertex.GEOMULTILINE, object.getGeometry());
    }
    else if (geometryType.equals(GeometryType.POINT) || geometryType.equals(GeometryType.MULTIPOINT))
    {
      node.setValue(GeoVertex.GEOMULTIPOINT, object.getGeometry());
    }
    else if (geometryType.equals(GeometryType.MULTIPOLYGON) || geometryType.equals(GeometryType.MULTIPOLYGON))
    {
      node.setValue(GeoVertex.GEOMULTIPOLYGON, object.getGeometry());
    }
    else if (geometryType.equals(GeometryType.MIXED))
    {
      node.setValue(GeoVertex.SHAPE, object.getGeometry());
    }
  }

}
