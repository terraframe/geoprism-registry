package net.geoprism.registry.conversion;

import java.util.Date;
import java.util.Map;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.DefaultTerms;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.Attribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.UnknownTermException;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;

import com.runwaysdk.business.BusinessEnumeration;
import com.runwaysdk.business.graph.GraphObject;
import com.runwaysdk.business.graph.VertexObject;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.InvalidRegistryIdException;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.io.TermValueException;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.VertexServerGeoObject;
import net.geoprism.registry.service.RegistryIdService;

public class VertexGeoObjectConverter extends LocalizedValueConverter implements ServerGeoObjectConverterIF
{
  private ServerGeoObjectType type;

  public VertexGeoObjectConverter(ServerGeoObjectType type)
  {
    super();
    this.type = type;
  }

  @Override
  public ServerGeoObjectType getType()
  {
    return this.type;
  }

  @Override
  public VertexServerGeoObject constructFromGeoObject(GeoObject geoObject, boolean isNew)
  {
    if (!isNew)
    {
      VertexObject vertex = GeoVertex.getVertex(type, geoObject.getUid());

      return new VertexServerGeoObject(type, geoObject, vertex);
    }
    else
    {
      if (!RegistryIdService.getInstance().isIssuedId(geoObject.getUid()))
      {
        InvalidRegistryIdException ex = new InvalidRegistryIdException();
        ex.setRegistryId(geoObject.getUid());
        throw ex;
      }

      VertexObject vertex = GeoVertex.newInstance(type);

      return new VertexServerGeoObject(type, geoObject, vertex);
    }
  }

  @Override
  public VertexServerGeoObject constructFromDB(Object dbObject)
  {
    VertexObject vertex = (VertexObject) dbObject;
    GraphObject graphObject = vertex.getEmbeddedComponent(DefaultAttribute.DISPLAY_LABEL.getName());

    Map<String, Attribute> attributeMap = GeoObject.buildAttributeMap(type.getType());

    GeoObject geoObj = new GeoObject(type.getType(), type.getGeometryType(), attributeMap);

    if (vertex.isNew())// && !vertex.isAppliedToDB())
    {
      geoObj.setUid(RegistryIdService.getInstance().next());

      geoObj.setStatus(this.getTerm(DefaultTerms.GeoObjectStatusTerm.NEW.code));
    }
    else
    {
      Map<String, AttributeType> attributes = type.getAttributeMap();
      attributes.forEach((attributeName, attribute) -> {
        if (attributeName.equals(DefaultAttribute.STATUS.getName()))
        {
          BusinessEnumeration busEnum = vertex.getEnumValues(attributeName).get(0);
          GeoObjectStatus gos = GeoObjectStatus.valueOf(busEnum.name());
          Term statusTerm = this.geoObjectStatusToTerm(gos);

          geoObj.setStatus(statusTerm);
        }
        else if (attributeName.equals(DefaultAttribute.TYPE.getName()))
        {
          // Ignore
        }
        else if (vertex.hasAttribute(attributeName))
        {
          Object value = vertex.getObjectValue(attributeName);

          if (value != null)
          {
            if (attribute instanceof AttributeTermType)
            {
              Classifier classifier = Classifier.get((String) value);

              try
              {
                geoObj.setValue(attributeName, classifier.getClassifierId());
              }
              catch (UnknownTermException e)
              {
                TermValueException ex = new TermValueException();
                ex.setAttributeLabel(e.getAttribute().getLabel().getValue());
                ex.setCode(e.getCode());

                throw e;
              }
            }
            else if (attribute instanceof AttributeDateType)
            {
              geoObj.setValue(attributeName, (Date) value);
            }
            else if (attribute instanceof AttributeBooleanType)
            {
              geoObj.setValue(attributeName, (Boolean) value);
            }
            else if (attribute instanceof AttributeFloatType)
            {
              geoObj.setValue(attributeName, (Double) value);
            }
            else if (attribute instanceof AttributeIntegerType)
            {
              geoObj.setValue(attributeName, (Long) value);
            }
            else
            {
              geoObj.setValue(attributeName, value);
            }
          }
        }
      });
    }

    geoObj.setUid((String) vertex.getObjectValue(RegistryConstants.UUID));
    geoObj.setCode((String) vertex.getObjectValue(DefaultAttribute.CODE.getName()));
    geoObj.setGeometry(this.getGeometry(vertex, type.getGeometryType()));
    geoObj.setDisplayLabel(populate(graphObject));

    return new VertexServerGeoObject(type, geoObj, vertex);
  }

  private Geometry getGeometry(VertexObject vertex, GeometryType geometryType)
  {
    if (geometryType.equals(GeometryType.LINE))
    {
      return (Geometry) vertex.getObjectValue(GeoVertex.GEOLINE);
    }
    else if (geometryType.equals(GeometryType.MULTILINE))
    {
      return (Geometry) vertex.getObjectValue(GeoVertex.GEOMULTILINE);
    }
    else if (geometryType.equals(GeometryType.POINT))
    {
      return (Geometry) vertex.getObjectValue(GeoVertex.GEOPOINT);
    }
    else if (geometryType.equals(GeometryType.MULTIPOINT))
    {
      return (Geometry) vertex.getObjectValue(GeoVertex.GEOMULTIPOINT);
    }
    else if (geometryType.equals(GeometryType.POLYGON))
    {
      return (Geometry) vertex.getObjectValue(GeoVertex.GEOPOLYGON);
    }
    else if (geometryType.equals(GeometryType.MULTIPOLYGON))
    {
      return (Geometry) vertex.getObjectValue(GeoVertex.GEOMULTIPOLYGON);
    }

    throw new UnsupportedOperationException("Unsupported geometry type [" + geometryType.name() + "]");
  }
}
