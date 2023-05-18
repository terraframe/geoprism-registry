package net.geoprism.registry.graph;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.locationtech.jts.geom.Geometry;

import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.system.metadata.MdVertex;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ServerGeoObjectIF;

public abstract class AbstractGraphVersionPublisher
{
  protected VertexObject publish(ServerGeoObjectIF object, MdVertex mdVertex, Date forDate)
  {
    return publish(mdVertex, object.toGeoObject(forDate));
  }

  @SuppressWarnings("unchecked")
  protected VertexObject publish(MdVertex mdVertex, GeoObject geoObject)
  {
    GeoObjectType type = geoObject.getType();

    VertexObject node = new VertexObject(mdVertex.definesType());

    node.setValue(DefaultAttribute.CODE.getName(), geoObject.getCode());
    node.setValue(RegistryConstants.UUID, geoObject.getUid());

    this.setGeometryValue(geoObject, type, node);

    Map<String, AttributeType> attributes = type.getAttributeMap();

    attributes.forEach((attributeName, attribute) -> {

      if (node.hasAttribute(attributeName))
      {
        if (attribute instanceof AttributeTermType)
        {
          Iterator<String> it = (Iterator<String>) geoObject.getValue(attributeName);

          if (it.hasNext())
          {
            String code = it.next();

            Term root = ( (AttributeTermType) attribute ).getRootTerm();
            String parent = TermConverter.buildClassifierKeyFromTermCode(root.getCode());

            String classifierKey = Classifier.buildKey(parent, code);
            Classifier classifier = Classifier.getByKey(classifierKey);

            node.setValue(attributeName, classifier.getOid());
          }
          else
          {
            node.setValue(attributeName, (String) null);
          }
        }
        else if (attribute instanceof AttributeClassificationType)
        {
          String value = (String) geoObject.getValue(attributeName);

          if (value != null)
          {
            Classification classification = Classification.get((AttributeClassificationType) attribute, value);

            node.setValue(attributeName, classification.getVertex());
          }
          else
          {
            node.setValue(attributeName, (String) null);
          }
        }
        else
        {
          Object value = geoObject.getValue(attributeName);

          if (value instanceof LocalizedValue)
          {
            LocalizedValueConverter.populate(node, attributeName, (LocalizedValue) value);
          }
          else
          {
            node.setValue(attributeName, value);
          }
        }
      }

    });

    node.apply();

    return node;
  }

  private void setGeometryValue(GeoObject object, GeoObjectType type, VertexObject node)
  {
    Geometry geometry = object.getGeometry();

    if (geometry != null)
    {
      node.setValue(DefaultAttribute.GEOMETRY.getName(), geometry);
    }
  }

}
