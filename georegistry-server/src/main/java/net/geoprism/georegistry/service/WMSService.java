package net.geoprism.georegistry.service;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.metadata.MetadataDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.gis.geo.Universal;

import net.geoprism.georegistry.RegistryConstants;
import net.geoprism.gis.geoserver.GeoserverFacade;
import net.geoprism.gis.geoserver.GeoserverService;

public class WMSService
{

  private static final String PREFIX  = "gs";

  GeoserverService            service = GeoserverFacade.getService();

  public void createAllWMSLayers(boolean forceGeneration)
  {
    GeoObjectType[] types = ServiceFactory.getAdapter().getMetadataCache().getAllGeoObjectTypes();

    for (GeoObjectType type : types)
    {
      this.createWMSLayer(type, forceGeneration);
    }
  }

  public void createWMSLayer(GeoObjectType type, boolean forceGeneration)
  {
    String viewName = this.createDatabaseView(type, forceGeneration);

    if (forceGeneration)
    {
      service.removeLayer(viewName);
    }

    // Now that the database transaction is complete we can create the geoserver
    // layer
    service.publishLayer(viewName, null);
  }

  public void deleteAllWMSLayers()
  {
    GeoObjectType[] types = ServiceFactory.getAdapter().getMetadataCache().getAllGeoObjectTypes();

    for (GeoObjectType type : types)
    {
      this.deleteWMSLayer(type);
    }
  }

  public void deleteWMSLayer(GeoObjectType type)
  {
    String viewName = this.getViewName(type);

    service.removeLayer(viewName);

    this.deleteDatabaseView(type);
  }

  private String getViewName(GeoObjectType type)
  {
    String viewName = MetadataDAO.convertCamelCaseToUnderscore(type.getCode()).toLowerCase();

    if (viewName.length() > Database.MAX_DB_IDENTIFIER_SIZE)
    {
      viewName = viewName.substring(0, Database.MAX_DB_IDENTIFIER_SIZE);
    }

    return PREFIX + "_" + viewName;
  }

  @Transaction
  public void deleteDatabaseView(GeoObjectType type)
  {
    String viewName = this.getViewName(type);

    Database.dropView(viewName, null, false);
  }

  @Transaction
  public String createDatabaseView(GeoObjectType type, boolean forceGeneration)
  {
    String viewName = this.getViewName(type);

    ValueQuery vQuery = this.generateQuery(type);

    Database.createView(viewName, vQuery.getSQL());

    return viewName;
  }

  public ValueQuery generateQuery(GeoObjectType type)
  {
    QueryFactory factory = new QueryFactory();
    ValueQuery vQuery = new ValueQuery(factory);
    Universal universal = ServiceFactory.getConversionService().getUniversalFromGeoObjectType(type);

    if (type.isLeaf())
    {
      BusinessQuery bQuery = new BusinessQuery(vQuery, universal.getMdBusiness().definesType());

      vQuery.SELECT(bQuery.aCharacter(DefaultAttribute.CODE.getName()));
      vQuery.SELECT(bQuery.aLocalCharacter(DefaultAttribute.LOCALIZED_DISPLAY_LABEL.getName()).localize(DefaultAttribute.LOCALIZED_DISPLAY_LABEL.getName()));
      vQuery.SELECT(bQuery.get(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));

      vQuery.ORDER_BY_ASC(bQuery.aCharacter(DefaultAttribute.CODE.getName()));
    }
    else
    {
      GeoEntityQuery geQuery = new GeoEntityQuery(vQuery);

      vQuery.SELECT(geQuery.getGeoId(DefaultAttribute.CODE.getName()));
      vQuery.SELECT(geQuery.getDisplayLabel().localize(DefaultAttribute.LOCALIZED_DISPLAY_LABEL.getName()));

      if (type.getGeometryType().equals(GeometryType.LINE))
      {
        vQuery.SELECT(geQuery.getGeoLine(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));
      }
      else if (type.getGeometryType().equals(GeometryType.MULTILINE))
      {
        vQuery.SELECT(geQuery.getGeoMultiLine(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));
      }
      else if (type.getGeometryType().equals(GeometryType.POINT))
      {
        vQuery.SELECT(geQuery.getGeoPoint(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));
      }
      else if (type.getGeometryType().equals(GeometryType.MULTIPOINT))
      {
        vQuery.SELECT(geQuery.getGeoMultiPoint(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));
      }
      else if (type.getGeometryType().equals(GeometryType.POLYGON))
      {
        vQuery.SELECT(geQuery.getGeoPolygon(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));
      }
      else if (type.getGeometryType().equals(GeometryType.MULTIPOLYGON))
      {
        vQuery.SELECT(geQuery.getGeoMultiPolygon(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));
      }

      vQuery.WHERE(geQuery.getUniversal().EQ(universal));
    }

    return vQuery;
  }

}
