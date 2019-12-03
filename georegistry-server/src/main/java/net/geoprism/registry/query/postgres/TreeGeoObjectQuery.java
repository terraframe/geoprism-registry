package net.geoprism.registry.query.postgres;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;

import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.constants.ComponentInfo;
import com.runwaysdk.constants.EnumerationMasterInfo;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.system.gis.geo.GeoEntityDisplayLabelQuery.GeoEntityDisplayLabelQueryStructIF;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;

import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.query.ServerGeoObjectQuery;

public class TreeGeoObjectQuery extends AbstractGeoObjectQuery implements ServerGeoObjectQuery
{
  public TreeGeoObjectQuery(ServerGeoObjectType type)
  {
    super(type);
  }

  public ValueQuery getValueQuery()
  {
    QueryFactory factory = new QueryFactory();
    ValueQuery vQuery = new ValueQuery(factory);

    GeoEntityQuery geQuery = new GeoEntityQuery(vQuery);
    BusinessQuery bQuery = new BusinessQuery(vQuery, this.getType().definesType());

    configureEntityQuery(vQuery, geQuery, bQuery);

    if (this.getLimit() != null)
    {
      vQuery.restrictRows(this.getLimit(), 1);
    }

    return vQuery;
  }

  protected void configureEntityQuery(ValueQuery vQuery, GeoEntityQuery geQuery, BusinessQuery bQuery)
  {
    vQuery.WHERE(geQuery.getUniversal().EQ(this.getType().getUniversal()));
    vQuery.WHERE(bQuery.aReference(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME).EQ(geQuery));

    GeoEntityDisplayLabelQueryStructIF label = geQuery.getDisplayLabel();
    vQuery.SELECT(geQuery.getOid(ComponentInfo.OID, ComponentInfo.OID));
    vQuery.SELECT(geQuery.getGeoId(DefaultAttribute.CODE.getName(), DefaultAttribute.CODE.getName()));
    vQuery.SELECT(bQuery.aUUID(RegistryConstants.UUID, RegistryConstants.UUID));
    vQuery.SELECT(bQuery.aEnumeration(DefaultAttribute.STATUS.getName()).aCharacter(EnumerationMasterInfo.NAME, DefaultAttribute.STATUS.getName()));

    this.selectLabelAttribute(vQuery, label);

    if (this.getType().getGeometryType().equals(GeometryType.LINE))
    {
      vQuery.SELECT(geQuery.getGeoLine(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));
    }
    else if (this.getType().getGeometryType().equals(GeometryType.MULTILINE))
    {
      vQuery.SELECT(geQuery.getGeoMultiLine(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));
    }
    else if (this.getType().getGeometryType().equals(GeometryType.POINT))
    {
      vQuery.SELECT(geQuery.getGeoPoint(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));
    }
    else if (this.getType().getGeometryType().equals(GeometryType.MULTIPOINT))
    {
      vQuery.SELECT(geQuery.getGeoMultiPoint(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));
    }
    else if (this.getType().getGeometryType().equals(GeometryType.POLYGON))
    {
      vQuery.SELECT(geQuery.getGeoPolygon(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));
    }
    else if (this.getType().getGeometryType().equals(GeometryType.MULTIPOLYGON))
    {
      vQuery.SELECT(geQuery.getGeoMultiPolygon(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));
    }

    this.selectCustomAttributes(vQuery, bQuery);

    if (this.getRestriction() != null)
    {
      this.getRestriction().create(this).restrict(vQuery, geQuery, bQuery);
    }

    vQuery.ORDER_BY_ASC(geQuery.getGeoId(DefaultAttribute.CODE.getName()));
  }
}
