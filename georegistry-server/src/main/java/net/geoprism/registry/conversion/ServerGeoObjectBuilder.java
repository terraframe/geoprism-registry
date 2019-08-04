package net.geoprism.registry.conversion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessEnumeration;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.attributes.entity.AttributeLocal;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.io.TermValueException;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerLeafGeoObject;
import net.geoprism.registry.model.ServerTreeGeoObject;
import net.geoprism.registry.service.RegistryIdService;
import net.geoprism.registry.service.ServiceFactory;

public class ServerGeoObjectBuilder extends AbstractBuilder
{
  public Term getTerm(String code)
  {
    return ServiceFactory.getAdapter().getMetadataCache().getTerm(code).get();
  }

  public Term geoObjectStatusToTerm(GeoObjectStatus gos)
  {
    return geoObjectStatusToTerm(gos.getEnumName());
  }

  public Term geoObjectStatusToTerm(String termCode)
  {
    if (termCode.equals(GeoObjectStatus.ACTIVE.getEnumName()))
    {
      return getTerm(DefaultTerms.GeoObjectStatusTerm.ACTIVE.code);
    }
    else if (termCode.equals(GeoObjectStatus.INACTIVE.getEnumName()))
    {
      return getTerm(DefaultTerms.GeoObjectStatusTerm.INACTIVE.code);
    }
    else if (termCode.equals(GeoObjectStatus.NEW.getEnumName()))
    {
      return getTerm(DefaultTerms.GeoObjectStatusTerm.NEW.code);
    }
    else if (termCode.equals(GeoObjectStatus.PENDING.getEnumName()))
    {
      return getTerm(DefaultTerms.GeoObjectStatusTerm.PENDING.code);
    }
    else
    {
      throw new ProgrammingErrorException("Unknown Status [" + termCode + "].");
    }
  }

  private Geometry getGeometry(GeoEntity geoEntity, GeometryType geometryType)
  {
    if (geometryType.equals(GeometryType.LINE))
    {
      return geoEntity.getGeoLine();
    }
    else if (geometryType.equals(GeometryType.MULTILINE))
    {
      return geoEntity.getGeoMultiLine();
    }
    else if (geometryType.equals(GeometryType.POINT))
    {
      return geoEntity.getGeoPoint();
    }
    else if (geometryType.equals(GeometryType.MULTIPOINT))
    {
      return geoEntity.getGeoMultiPoint();
    }
    else if (geometryType.equals(GeometryType.POLYGON))
    {
      return geoEntity.getGeoPolygon();
    }
    else if (geometryType.equals(GeometryType.MULTIPOLYGON))
    {
      return geoEntity.getGeoMultiPolygon();
    }

    throw new UnsupportedOperationException("Unsupported geometry type [" + geometryType.name() + "]");
  }

  public ServerGeoObjectIF build(ServerGeoObjectType type, GeoEntity geoEntity)
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Map<String, Attribute> attributeMap = GeoObject.buildAttributeMap(type.getType());

    GeoObject geoObj = new GeoObject(type.getType(), type.getGeometryType(), attributeMap);

    if (geoEntity.isNew())
    {
      geoObj.setUid(RegistryIdService.getInstance().next());

      geoObj.setStatus(ServiceFactory.getAdapter().getMetadataCache().getTerm(DefaultTerms.GeoObjectStatusTerm.NEW.code).get());
    }
    else
    {
      geoObj.setUid(RegistryIdService.getInstance().runwayIdToRegistryId(geoEntity.getOid(), geoEntity.getUniversal()));

      Business biz = ServiceFactory.getUtilities().getGeoEntityBusiness(geoEntity);

      Map<String, AttributeType> attributes = type.getAttributeMap();
      attributes.forEach((attributeName, attribute) -> {
        if (attributeName.equals(DefaultAttribute.STATUS.getName()))
        {
          BusinessEnumeration busEnum = biz.getEnumValues(attributeName).get(0);
          GeoObjectStatus gos = GeoObjectStatus.valueOf(busEnum.name());
          Term statusTerm = this.geoObjectStatusToTerm(gos);

          geoObj.setStatus(statusTerm);
        }
        else if (attributeName.equals(DefaultAttribute.TYPE.getName()))
        {
          // Ignore
        }
        else if (biz.hasAttribute(attributeName))
        {
          String value = biz.getValue(attributeName);

          if (value != null && value.length() > 0)
          {
            if (attribute instanceof AttributeTermType)
            {
              Classifier classifier = Classifier.get(value);

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
              try
              {
                geoObj.setValue(attributeName, format.parse(value));
              }
              catch (ParseException e)
              {
                throw new RuntimeException(e);
              }
            }
            else if (attribute instanceof AttributeBooleanType)
            {
              geoObj.setValue(attributeName, new Boolean(value));
            }
            else if (attribute instanceof AttributeFloatType)
            {
              geoObj.setValue(attributeName, new Double(value));
            }
            else if (attribute instanceof AttributeIntegerType)
            {
              geoObj.setValue(attributeName, new Long(value));
            }
            else
            {
              geoObj.setValue(attributeName, value);
            }
          }
        }
      });
    }

    geoObj.setCode(geoEntity.getGeoId());
    geoObj.getDisplayLabel().setValue(geoEntity.getDisplayLabel().getValue());
    geoObj.setGeometry(this.getGeometry(geoEntity, type.getGeometryType()));

    Business business = ServerTreeGeoObject.getBusiness(geoEntity);

    return new ServerTreeGeoObject(type, geoObj, geoEntity, business);
  }

  public ServerGeoObjectIF build(ServerGeoObjectType type, Business business)
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Map<String, Attribute> attributeMap = GeoObject.buildAttributeMap(type.getType());

    GeoObject geoObj = new GeoObject(type.getType(), type.getGeometryType(), attributeMap);

    if (business.isNew())
    {
      geoObj.setUid(RegistryIdService.getInstance().next());

      geoObj.setStatus(ServiceFactory.getAdapter().getMetadataCache().getTerm(DefaultTerms.GeoObjectStatusTerm.NEW.code).get());
    }
    else
    {
      geoObj.setUid(RegistryIdService.getInstance().runwayIdToRegistryId(business.getOid(), type.getUniversal()));

      Map<String, AttributeType> attributes = type.getAttributeMap();
      attributes.forEach((attributeName, attribute) -> {
        if (attributeName.equals(DefaultAttribute.STATUS.getName()))
        {
          BusinessEnumeration busEnum = business.getEnumValues(attributeName).get(0);
          GeoObjectStatus gos = GeoObjectStatus.valueOf(busEnum.name());
          Term statusTerm = this.geoObjectStatusToTerm(gos);

          geoObj.setStatus(statusTerm);
        }
        else if (attributeName.equals(DefaultAttribute.TYPE.getName()))
        {
          // Ignore
        }
        else if (business.hasAttribute(attributeName))
        {
          String value = business.getValue(attributeName);

          if (value != null && value.length() > 0)
          {
            if (attribute instanceof AttributeTermType)
            {
              Classifier classifier = Classifier.get(value);

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
              try
              {
                geoObj.setValue(attributeName, format.parse(value));
              }
              catch (ParseException e)
              {
                throw new RuntimeException(e);
              }
            }
            else if (attribute instanceof AttributeBooleanType)
            {
              geoObj.setValue(attributeName, new Boolean(value));
            }
            else if (attribute instanceof AttributeFloatType)
            {
              geoObj.setValue(attributeName, new Double(value));
            }
            else if (attribute instanceof AttributeIntegerType)
            {
              geoObj.setValue(attributeName, new Long(value));
            }
            else
            {
              geoObj.setValue(attributeName, value);
            }
          }
        }
      });
    }

    geoObj.setCode(business.getValue(DefaultAttribute.CODE.getName()));

    String localizedValue = ( (AttributeLocal) BusinessFacade.getEntityDAO(business).getAttributeIF(DefaultAttribute.DISPLAY_LABEL.getName()) ).getValue(Session.getCurrentLocale());
    geoObj.getDisplayLabel().setValue(localizedValue);

    geoObj.setGeometry((Geometry) business.getObjectValue(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));

    return new ServerLeafGeoObject(type, geoObj, business);
  }
}
