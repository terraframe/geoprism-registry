package net.geoprism.georegistry.query;

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
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.query.LeftJoinEq;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.gis.geo.Universal;

import net.geoprism.georegistry.RegistryConstants;
import net.geoprism.ontology.ClassifierQuery;

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

  public GeoObjectIterator getIterator()
  {
    QueryFactory factory = new QueryFactory();
    ValueQuery vQuery = new ValueQuery(factory);

    if (this.type.isLeaf())
    {
      BusinessQuery bQuery = new BusinessQuery(vQuery, universal.getMdBusiness().definesType());

      vQuery.SELECT(bQuery.aUUID(ComponentInfo.OID));
      vQuery.SELECT(bQuery.aCharacter(DefaultAttribute.CODE.getName()));
      vQuery.SELECT(bQuery.aLocalCharacter(DefaultAttribute.LOCALIZED_DISPLAY_LABEL.getName()).localize(DefaultAttribute.LOCALIZED_DISPLAY_LABEL.getName()));
      vQuery.SELECT(bQuery.aEnumeration(DefaultAttribute.STATUS.getName()).aCharacter(EnumerationMasterInfo.NAME, DefaultAttribute.STATUS.getName()));
      vQuery.SELECT(bQuery.get(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));

      this.selectCustomAttributes(vQuery, bQuery);

      if (this.restriction != null)
      {
        this.restriction.restrict(vQuery, bQuery);
      }

      vQuery.ORDER_BY_ASC(bQuery.aCharacter(DefaultAttribute.CODE.getName()));
    }
    else
    {
      GeoEntityQuery geQuery = new GeoEntityQuery(vQuery);
      BusinessQuery bQuery = new BusinessQuery(vQuery, universal.getMdBusiness().definesType());

      vQuery.WHERE(geQuery.getUniversal().EQ(universal));
      vQuery.WHERE(bQuery.aReference(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME).EQ(geQuery));

      vQuery.SELECT(geQuery.getOid(ComponentInfo.OID));
      vQuery.SELECT(geQuery.getGeoId(DefaultAttribute.CODE.getName()));
      vQuery.SELECT(geQuery.getDisplayLabel().localize(DefaultAttribute.LOCALIZED_DISPLAY_LABEL.getName()));
      vQuery.SELECT(bQuery.aEnumeration(DefaultAttribute.STATUS.getName()).aCharacter(EnumerationMasterInfo.NAME, DefaultAttribute.STATUS.getName()));

      if (this.type.getGeometryType().equals(GeometryType.LINE))
      {
        vQuery.SELECT(geQuery.getGeoLine(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));
      }
      else if (this.type.getGeometryType().equals(GeometryType.MULTILINE))
      {
        vQuery.SELECT(geQuery.getGeoMultiLine(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));
      }
      else if (this.type.getGeometryType().equals(GeometryType.POINT))
      {
        vQuery.SELECT(geQuery.getGeoPoint(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));
      }
      else if (this.type.getGeometryType().equals(GeometryType.MULTIPOINT))
      {
        vQuery.SELECT(geQuery.getGeoMultiPoint(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));
      }
      else if (this.type.getGeometryType().equals(GeometryType.POLYGON))
      {
        vQuery.SELECT(geQuery.getGeoPolygon(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));
      }
      else if (this.type.getGeometryType().equals(GeometryType.MULTIPOLYGON))
      {
        vQuery.SELECT(geQuery.getGeoMultiPolygon(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));
      }

      this.selectCustomAttributes(vQuery, bQuery);

      if (this.restriction != null)
      {
        this.restriction.restrict(vQuery, geQuery, bQuery);
      }

      vQuery.ORDER_BY_ASC(geQuery.getGeoId(DefaultAttribute.CODE.getName()));
    }

    if (this.limit != null)
    {
      vQuery.restrictRows(this.limit, 1);
    }

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
          ClassifierQuery classifierQuery = new ClassifierQuery(vQuery);

          vQuery.WHERE(new LeftJoinEq(bQuery.get(attributeName), classifierQuery.getOid()));
          vQuery.SELECT(classifierQuery.getClassifierId(attributeName));
        }
        else
        {
          vQuery.SELECT(bQuery.get(attributeName));
        }
      }
    });
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
    else if (attributeName.equals(DefaultAttribute.LOCALIZED_DISPLAY_LABEL.getName()))
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

}
