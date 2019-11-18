package net.geoprism.registry.query.postgres;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;

import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.constants.ComponentInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.dataaccess.metadata.SupportedLocaleDAO;
import com.runwaysdk.query.AttributeLocalIF;
import com.runwaysdk.query.LeftJoinEq;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.ValueQuery;

import net.geoprism.ontology.ClassifierQuery;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.postgres.ValueObjectServerGeoObject;
import net.geoprism.registry.query.ServerGeoObjectQuery;
import net.geoprism.registry.query.ServerGeoObjectRestriction;

public abstract class AbstractGeoObjectQuery implements ServerGeoObjectQuery
{
  private ServerGeoObjectType        type;

  private ServerGeoObjectRestriction restriction;

  private Integer                    limit;

  public AbstractGeoObjectQuery(ServerGeoObjectType type)
  {
    this.type = type;
  }

  protected abstract ValueQuery getValueQuery();

  public ServerGeoObjectType getType()
  {
    return type;
  }

  public void setType(ServerGeoObjectType type)
  {
    this.type = type;
  }

  public ServerGeoObjectRestriction getRestriction()
  {
    return restriction;
  }

  public void setRestriction(ServerGeoObjectRestriction restriction)
  {
    this.restriction = restriction;
  }

  public Integer getLimit()
  {
    return limit;
  }

  public void setLimit(Integer limit)
  {
    this.limit = limit;
  }

  public OIterator<ValueObject> getIterator()
  {
    ValueQuery vQuery = this.getValueQuery();

    return vQuery.getIterator();
  }

  protected void selectCustomAttributes(ValueQuery vQuery, BusinessQuery bQuery)
  {
    Map<String, AttributeType> attributes = this.type.getAttributeMap();
    attributes.forEach((attributeName, attribute) -> {
      MdAttributeDAOIF mdAttribute = bQuery.getMdTableClassIF().definesAttribute(attributeName);

      if (mdAttribute != null && this.isValid(attributeName))
      {
        if (attribute instanceof AttributeTermType)
        {
          selectTermAttribute(vQuery, bQuery, mdAttribute);
        }
        else
        {
          vQuery.SELECT(bQuery.get(attributeName, attributeName));
        }
      }
    });
  }

  protected void selectTermAttribute(ValueQuery vQuery, BusinessQuery bQuery, MdAttributeDAOIF mdAttribute)
  {
    ClassifierQuery classifierQuery = new ClassifierQuery(vQuery);

    vQuery.WHERE(new LeftJoinEq(bQuery.get(mdAttribute.definesAttribute()), classifierQuery.getOid()));
    vQuery.SELECT(classifierQuery.getOid(mdAttribute.definesAttribute(), mdAttribute.definesAttribute()));
  }

  protected void selectLabelAttribute(ValueQuery vQuery, AttributeLocalIF label)
  {
    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();
    vQuery.SELECT(label.localize(DefaultAttribute.DISPLAY_LABEL.getName(), DefaultAttribute.DISPLAY_LABEL.getName()));
    vQuery.SELECT(label.get(MdAttributeLocalInfo.DEFAULT_LOCALE, MdAttributeLocalInfo.DEFAULT_LOCALE));
    for (Locale locale : locales)
    {
      vQuery.SELECT(label.get(locale.toString(), DefaultAttribute.DISPLAY_LABEL.getName() + "_" + locale.toString()));
    }
  }

  private boolean isValid(String attributeName)
  {
    if (attributeName.equals(ComponentInfo.OID))
    {
      return false;
    }
    else if (attributeName.equals(DefaultAttribute.CODE.getName()))
    {
      return false;
    }
    else if (attributeName.equals(DefaultAttribute.STATUS.getName()))
    {
      return false;
    }
    else if (attributeName.equals(DefaultAttribute.TYPE.getName()))
    {
      return false;
    }
    else if (attributeName.equals(DefaultAttribute.DISPLAY_LABEL.getName()))
    {
      return false;
    }

    return true;
  }

  public ServerGeoObjectIF getSingleResult()
  {

    try (OIterator<ValueObject> it = this.getIterator())
    {
      if (it.hasNext())
      {
        ValueObject result = it.next();

        if (it.hasNext())
        {
          throw new NonUniqueResultException();
        }

        return new ValueObjectServerGeoObject(this.type, result);
      }

      return null;
    }
  }

  public List<ServerGeoObjectIF> getResults()
  {
    LinkedList<ServerGeoObjectIF> results = new LinkedList<ServerGeoObjectIF>();

    try (OIterator<ValueObject> it = this.getIterator())
    {
      while (it.hasNext())
      {
        ValueObject result = it.next();

        results.add(new ValueObjectServerGeoObject(this.type, result));
      }
    }

    return results;
  }

  public Long getCount()
  {
    return this.getValueQuery().getCount();
  }

}
