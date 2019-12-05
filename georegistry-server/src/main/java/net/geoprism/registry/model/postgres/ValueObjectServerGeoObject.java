package net.geoprism.registry.model.postgres;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.DefaultTerms;
import org.commongeoregistry.adapter.dataaccess.Attribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.UnknownTermException;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.json.JSONArray;
import org.json.JSONException;

import com.runwaysdk.constants.ComponentInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTimeCollection;
import com.runwaysdk.dataaccess.metadata.SupportedLocaleDAO;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.io.GeoObjectUtil;
import net.geoprism.registry.io.TermValueException;
import net.geoprism.registry.model.AbstractServerGeoObject;
import net.geoprism.registry.model.LocationInfo;
import net.geoprism.registry.model.ServerChildTreeNode;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.model.ServerParentTreeNodeOverTime;
import net.geoprism.registry.service.RegistryIdService;
import net.geoprism.registry.service.ServiceFactory;

public class ValueObjectServerGeoObject extends AbstractServerGeoObject implements ServerGeoObjectIF
{
  private ServerGeoObjectType type;

  private ValueObject         valueObject;

  public ValueObjectServerGeoObject(ServerGeoObjectType type, ValueObject valueObject)
  {
    this.type = type;
    this.valueObject = valueObject;
  }

  public ServerGeoObjectType getType()
  {
    return type;
  }

  public void setType(ServerGeoObjectType type)
  {
    this.type = type;
  }

  public ValueObject getValue()
  {
    return valueObject;
  }

  public void setValue(ValueObject valueObject)
  {
    this.valueObject = valueObject;
  }

  @Override
  public GeoObjectStatus getStatus()
  {
    String value = this.valueObject.getValue(DefaultAttribute.STATUS.getName());

    return GeoObjectStatus.get(value);
  }

  @Override
  public void setCode(String code)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setGeometry(Geometry geometry)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setStatus(GeoObjectStatus status)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setUid(String uid)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setDisplayLabel(LocalizedValue label)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setValue(String attributeName, Object value)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void populate(GeoObject geoObject)
  {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public void populate(GeoObjectOverTime goTime)
  {
    throw new UnsupportedOperationException();
  }

  public Map<String, ServerHierarchyType> getHierarchyTypeMap(String[] relationshipTypes)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, LocationInfo> getAncestorMap(ServerHierarchyType hierarchy)
  {
    return GeoObjectUtil.getAncestorMap(this.toGeoObject(), hierarchy);
  }

  protected boolean isValidGeometry(Geometry geometry)
  {
    return false;
  }

  @Override
  public String getCode()
  {
    return (String) this.valueObject.getObjectValue(DefaultAttribute.CODE.getName());
  }

  @Override
  public String getUid()
  {
    return (String) this.valueObject.getObjectValue(RegistryConstants.UUID);
  }

  @Override
  public String getRunwayId()
  {
    return (String) this.valueObject.getObjectValue(ComponentInfo.OID);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<? extends MdAttributeConcreteDAOIF> getMdAttributeDAOs()
  {
    return (List<? extends MdAttributeConcreteDAOIF>) this.valueObject.getMdAttributeDAOs();
  }

  @Override
  public Object getValue(String attributeName)
  {
    AttributeType attributeType = this.type.getAttribute(attributeName).get();

    if (attributeType instanceof AttributeLocalType)
    {
      return this.getLocalValue(attributeName);
    }
    else if (attributeType instanceof AttributeTermType)
    {
      String value = this.valueObject.getValue(attributeName);

      if (value != null && value.length() > 0)
      {
        return Classifier.get(value);
      }

      return null;
    }

    return this.valueObject.getValue(attributeName);
  }

  @Override
  public Object getValue(String attributeName, Date date)
  {
    return this.getValue(attributeName);
  }

  @Override
  public ValueOverTimeCollection getValuesOverTime(String attributeName)
  {
    throw new UnsupportedOperationException("Value over time operations are only supported on Vertex GeoObjects.");
  }

  @Override
  public void setValuesOverTime(String attributeName, ValueOverTimeCollection collection)
  {
    throw new UnsupportedOperationException("Value over time operations are only supported on Vertex GeoObjects.");
  }

  @Override
  public void lock()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void apply(boolean isImport)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public String bbox()
  {
    Geometry geometry = this.getGeometry();

    if (geometry != null)
    {
      try
      {
        Envelope e = geometry.getEnvelopeInternal();

        JSONArray bboxArr = new JSONArray();
        bboxArr.put(e.getMinX());
        bboxArr.put(e.getMinY());
        bboxArr.put(e.getMaxX());
        bboxArr.put(e.getMaxY());

        return bboxArr.toString();
      }
      catch (JSONException ex)
      {
        throw new ProgrammingErrorException(ex);
      }
    }

    return null;
  }

  public void removeChild(ServerGeoObjectIF child, String hierarchyCode)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public ServerParentTreeNode addChild(ServerGeoObjectIF child, ServerHierarchyType hierarchy)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public ServerParentTreeNode addChild(ServerGeoObjectIF child, ServerHierarchyType hierarchy, Date startDate, Date endDate)
  {
    return this.addChild(child, hierarchy);
  }

  @Override
  public ServerParentTreeNode addParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public ServerParentTreeNode addParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType, Date startDate, Date endDate)
  {
    return this.addParent(parent, hierarchyType);
  }

  @Override
  public ServerChildTreeNode getChildGeoObjects(String[] childrenTypes, Boolean recursive)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public ServerParentTreeNode getParentGeoObjects(String[] parentTypes, Boolean recursive)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public ServerParentTreeNodeOverTime getParentsOverTime(String[] parentTypes, Boolean recursive)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType)
  {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public GeoObjectOverTime toGeoObjectOverTime()
  {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  @Override
  public GeoObject toGeoObject()
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Map<String, Attribute> attributeMap = GeoObject.buildAttributeMap(type.getType());

    GeoObject geoObj = new GeoObject(type.getType(), type.getGeometryType(), attributeMap);

    if (valueObject.isNew())// && !valueObject.isAppliedToDB())
    {
      geoObj.setUid(RegistryIdService.getInstance().next());

      geoObj.setStatus(ServiceFactory.getAdapter().getMetadataCache().getTerm(DefaultTerms.GeoObjectStatusTerm.NEW.code).get());
    }
    else
    {
      Map<String, AttributeType> attributes = type.getAttributeMap();
      attributes.forEach((attributeName, attribute) -> {
        if (attributeName.equals(DefaultAttribute.STATUS.getName()))
        {
          // String oid = valueObject.getValue(attributeName);
          // GeoObjectStatus gos = GeoObjectStatus.valueOf(busEnum.name());
          // TODO
          GeoObjectStatus gos = GeoObjectStatus.ACTIVE;
          Term statusTerm = ServiceFactory.getConversionService().geoObjectStatusToTerm(gos);

          geoObj.setStatus(statusTerm);
        }
        else if (attributeName.equals(DefaultAttribute.TYPE.getName()))
        {
          // Ignore
        }
        else if (valueObject.hasAttribute(attributeName))
        {
          String value = valueObject.getValue(attributeName);

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

    geoObj.setUid(this.getUid());
    geoObj.setCode(this.getCode());
    geoObj.setGeometry(this.getGeometry());
    geoObj.setDisplayLabel(this.getDisplayLabel());

    return geoObj;
  }

  public LocalizedValue getDisplayLabel()
  {
    return getLocalValue(DefaultAttribute.DISPLAY_LABEL.getName());

  }

  private LocalizedValue getLocalValue(String attributeName)
  {
    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

    String value = this.valueObject.getValue(attributeName);

    HashMap<String, String> map = new HashMap<String, String>();
    map.put(MdAttributeLocalInfo.DEFAULT_LOCALE, this.valueObject.getValue(MdAttributeLocalInfo.DEFAULT_LOCALE));

    for (Locale locale : locales)
    {
      map.put(locale.toString(), this.valueObject.getValue(DefaultAttribute.DISPLAY_LABEL.getName() + "_" + locale.toString()));
    }

    return LocalizedValueConverter.convert(value, map);
  }

  public Geometry getGeometry()
  {
    return (Geometry) this.valueObject.getObjectValue(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);
  }

  @Override
  public void setStatus(GeoObjectStatus status, Date startDate, Date endDate)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setGeometry(Geometry geometry, Date startDate, Date endDate)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setValue(String attributeName, Object value, Date startDate, Date endDate)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setDisplayLabel(LocalizedValue value, Date startDate, Date endDate)
  {
    throw new UnsupportedOperationException();
  }
}
