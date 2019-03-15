package net.geoprism.registry.query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.Attribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.UnknownTermException;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.runwaysdk.constants.ComponentInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.dataaccess.metadata.SupportedLocaleDAO;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.system.gis.geo.Universal;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.io.TermValueException;
import net.geoprism.registry.service.RegistryIdService;
import net.geoprism.registry.service.ServiceFactory;

public class GeoObjectIterator implements OIterator<GeoObject>
{
  private GeoObjectType          type;

  private Universal              universal;

  private OIterator<ValueObject> iterator;

  private SimpleDateFormat       format;

  private String                 oid;

  public GeoObjectIterator(GeoObjectType type, Universal universal, OIterator<ValueObject> iterator)
  {
    this.type = type;
    this.universal = universal;
    this.iterator = iterator;
    this.format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  }

  @Override
  public Iterator<GeoObject> iterator()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public GeoObject next()
  {
    ValueObject vObject = this.iterator.next();

    return this.convert(vObject);
  }

  public String currentOid()
  {
    return this.oid;
  }

  private GeoObject convert(ValueObject vObject)
  {
    this.oid = vObject.getValue(ComponentInfo.OID);

    Map<String, Attribute> attributeMap = GeoObject.buildAttributeMap(this.type);
    GeoObject gObject = new GeoObject(this.type, this.type.getGeometryType(), attributeMap);

    gObject.setUid(RegistryIdService.getInstance().runwayIdToRegistryId(this.oid, universal));

    Map<String, AttributeType> attributes = this.type.getAttributeMap();
    attributes.forEach((attributeName, attribute) -> {
      if (attributeName.equals(DefaultAttribute.TYPE.getName()))
      {
        // Ignore
      }
      else if (attributeName.equals(DefaultAttribute.STATUS.getName()))
      {
        String name = vObject.getValue(attributeName);
        GeoObjectStatus gos = GeoObjectStatus.valueOf(name);
        Term statusTerm = ServiceFactory.getConversionService().geoObjectStatusToTerm(gos);

        gObject.setStatus(statusTerm);
      }
      else if (vObject.hasAttribute(attributeName))
      {
        String value = vObject.getValue(attributeName);

        if (value != null && value.length() > 0)
        {
          if (attribute instanceof AttributeTermType)
          {
            try
            {
              gObject.setValue(attributeName, value);
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
              gObject.setValue(attributeName, format.parse(value));
            }
            catch (ParseException e)
            {
              throw new RuntimeException(e);
            }
          }
          else if (attribute instanceof AttributeBooleanType)
          {
            gObject.setValue(attributeName, new Boolean(value.equals("1")));
          }
          else if (attribute instanceof AttributeFloatType)
          {
            gObject.setValue(attributeName, new Double(value));
          }
          else if (attribute instanceof AttributeIntegerType)
          {
            gObject.setValue(attributeName, new Long(value));
          }
          else
          {
            gObject.setValue(attributeName, value);
          }
        }
      }
    });

    LocalizedValue label = new LocalizedValue(vObject.getValue(DefaultAttribute.DISPLAY_LABEL.getName()));
    label.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, vObject.getValue(MdAttributeLocalInfo.DEFAULT_LOCALE));

    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

    for (Locale locale : locales)
    {
      String value = vObject.getValue(DefaultAttribute.DISPLAY_LABEL.getName() + "_" + locale.toString());

      label.setValue(locale, value);
    }

    gObject.setCode(vObject.getValue(DefaultAttribute.CODE.getName()));
    gObject.setValue(DefaultAttribute.DISPLAY_LABEL.getName(), label);
    gObject.setGeometry((Geometry) vObject.getObjectValue(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));

    return gObject;
  }

  @Override
  public void remove()
  {
    this.iterator.remove();
  }

  @Override
  public boolean hasNext()
  {
    return this.iterator.hasNext();
  }

  @Override
  public void close()
  {
    this.iterator.close();
  }

  @Override
  public List<GeoObject> getAll()
  {
    List<ValueObject> vObjects = this.iterator.getAll();

    List<GeoObject> list = new LinkedList<GeoObject>();

    for (ValueObject vObject : vObjects)
    {
      list.add(this.convert(vObject));
    }

    return list;
  }

}
