package net.geoprism.registry.query;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.runwaysdk.Pair;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.constants.ComponentInfo;
import com.runwaysdk.constants.EnumerationMasterInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.metadata.SupportedLocaleDAO;
import com.runwaysdk.query.AttributeLocal;
import com.runwaysdk.query.AttributeLocalIF;
import com.runwaysdk.query.LeftJoinEq;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.system.gis.geo.GeoEntityDisplayLabelQuery.GeoEntityDisplayLabelQueryStructIF;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.gis.geo.Universal;

import net.geoprism.ontology.ClassifierQuery;
import net.geoprism.registry.RegistryConstants;

public class GeoObjectQuery
{
  private GeoObjectType        type;

  private Universal            universal;

  private GeoObjectRestriction restriction;

  private Integer              limit;

  public GeoObjectQuery(GeoObjectType type, Universal universal)
  {
    this.type = type;
    this.universal = universal;
  }

  public GeoObjectType getType()
  {
    return type;
  }

  public void setType(GeoObjectType type)
  {
    this.type = type;
  }

  public Universal getUniversal()
  {
    return universal;
  }

  public void setUniversal(Universal universal)
  {
    this.universal = universal;
  }

  public GeoObjectRestriction getRestriction()
  {
    return restriction;
  }

  public void setRestriction(GeoObjectRestriction restriction)
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

  public ValueQuery getValueQuery()
  {
    QueryFactory factory = new QueryFactory();
    ValueQuery vQuery = new ValueQuery(factory);

    if (this.type.isLeaf())
    {
      BusinessQuery bQuery = new BusinessQuery(vQuery, universal.getMdBusiness().definesType());

      configureLeafQuery(vQuery, bQuery);
    }
    else
    {
      GeoEntityQuery geQuery = new GeoEntityQuery(vQuery);
      BusinessQuery bQuery = new BusinessQuery(vQuery, universal.getMdBusiness().definesType());

      configureEntityQuery(vQuery, geQuery, bQuery);
    }

    if (this.limit != null)
    {
      vQuery.restrictRows(this.limit, 1);
    }
    return vQuery;
  }

  protected void configureEntityQuery(ValueQuery vQuery, GeoEntityQuery geQuery, BusinessQuery bQuery)
  {
    vQuery.WHERE(geQuery.getUniversal().EQ(universal));
    vQuery.WHERE(bQuery.aReference(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME).EQ(geQuery));

    GeoEntityDisplayLabelQueryStructIF label = geQuery.getDisplayLabel();
    vQuery.SELECT(geQuery.getOid(ComponentInfo.OID, ComponentInfo.OID));
    vQuery.SELECT(geQuery.getGeoId(DefaultAttribute.CODE.getName(), DefaultAttribute.CODE.getName()));
    vQuery.SELECT(bQuery.aEnumeration(DefaultAttribute.STATUS.getName()).aCharacter(EnumerationMasterInfo.NAME, DefaultAttribute.STATUS.getName()));

    this.selectLabelAttribute(vQuery, label);

    if (this.type.getGeometryType().equals(GeometryType.LINE))
    {
      vQuery.SELECT(geQuery.getGeoLine(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));
    }
    else if (this.type.getGeometryType().equals(GeometryType.MULTILINE))
    {
      vQuery.SELECT(geQuery.getGeoMultiLine(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));
    }
    else if (this.type.getGeometryType().equals(GeometryType.POINT))
    {
      vQuery.SELECT(geQuery.getGeoPoint(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));
    }
    else if (this.type.getGeometryType().equals(GeometryType.MULTIPOINT))
    {
      vQuery.SELECT(geQuery.getGeoMultiPoint(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));
    }
    else if (this.type.getGeometryType().equals(GeometryType.POLYGON))
    {
      vQuery.SELECT(geQuery.getGeoPolygon(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));
    }
    else if (this.type.getGeometryType().equals(GeometryType.MULTIPOLYGON))
    {
      vQuery.SELECT(geQuery.getGeoMultiPolygon(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));
    }

    this.selectCustomAttributes(vQuery, bQuery);

    if (this.restriction != null)
    {
      this.restriction.restrict(vQuery, geQuery, bQuery);
    }

    vQuery.ORDER_BY_ASC(geQuery.getGeoId(DefaultAttribute.CODE.getName()));
  }

  protected void configureLeafQuery(ValueQuery vQuery, BusinessQuery bQuery)
  {
    AttributeLocal label = bQuery.aLocalCharacter(DefaultAttribute.DISPLAY_LABEL.getName());

    vQuery.SELECT(bQuery.aUUID(ComponentInfo.OID, ComponentInfo.OID));
    vQuery.SELECT(bQuery.aCharacter(DefaultAttribute.CODE.getName()));
    vQuery.SELECT(bQuery.aEnumeration(DefaultAttribute.STATUS.getName()).aCharacter(EnumerationMasterInfo.NAME, DefaultAttribute.STATUS.getName()));
    vQuery.SELECT(bQuery.get(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));

    this.selectLabelAttribute(vQuery, label);

    this.selectCustomAttributes(vQuery, bQuery);

    if (this.restriction != null)
    {
      this.restriction.restrict(vQuery, bQuery);
    }

    vQuery.ORDER_BY_ASC(bQuery.aCharacter(DefaultAttribute.CODE.getName()));
  }

  public GeoObjectIterator getIterator()
  {
    ValueQuery vQuery = this.getValueQuery();

    return new GeoObjectIterator(type, universal, vQuery.getIterator());
  }

  private void selectCustomAttributes(ValueQuery vQuery, BusinessQuery bQuery)
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
    vQuery.SELECT(classifierQuery.getClassifierId(mdAttribute.definesAttribute(), mdAttribute.definesAttribute()));
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

  public GeoObject getSingleResult()
  {
    OIterator<GeoObject> it = null;

    try
    {
      it = this.getIterator();

      if (it.hasNext())
      {
        GeoObject result = it.next();

        if (it.hasNext())
        {
          throw new NonUniqueResultException();
        }

        return result;
      }

      return null;
    }
    finally
    {
      if (it != null)
      {
        it.close();
      }
    }
  }

  public Pair<String, GeoObject> getSinglePair()
  {
    GeoObjectIterator it = null;

    try
    {
      it = this.getIterator();

      if (it.hasNext())
      {
        GeoObject geoObject = it.next();
        String oid = it.currentOid();

        if (it.hasNext())
        {
          throw new NonUniqueResultException();
        }

        return new Pair<String, GeoObject>(oid, geoObject);
      }

      return null;
    }
    finally
    {
      if (it != null)
      {
        it.close();
      }
    }
  }

  public Long getCount()
  {
    return this.getValueQuery().getCount();
  }
}
