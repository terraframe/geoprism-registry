package net.geoprism.georegistry.service;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.constants.MdEntityInfo;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdLocalStructDAOIF;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.metadata.MetadataDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.Attribute;
import com.runwaysdk.query.AttributeCharacter;
import com.runwaysdk.query.AttributeLocal;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.SelectableChar;
import com.runwaysdk.query.SelectableGeometry;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.system.gis.geo.GeoEntityDisplayLabelQuery.GeoEntityDisplayLabelQueryStructIF;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.gis.geo.Universal;

import net.geoprism.georegistry.RegistryConstants;
import net.geoprism.gis.geoserver.GeoserverFacade;
import net.geoprism.gis.geoserver.GeoserverService;

public class WMSService
{

  private static final String PREFIX  = "gs";

  GeoserverService            service = GeoserverFacade.getService();

  @Request
  public void createAllWMSLayers(boolean forceGeneration)
  {
    GeoObjectType[] types = ServiceFactory.getAdapter().getMetadataCache().getAllGeoObjectTypes();

    for (GeoObjectType type : types)
    {
      if (!type.getCode().equals(Universal.ROOT_KEY))
      {
        this.createWMSLayer(type, forceGeneration);
      }
    }
  }

  public void deleteAllWMSLayers()
  {
    GeoObjectType[] types = ServiceFactory.getAdapter().getMetadataCache().getAllGeoObjectTypes();

    for (GeoObjectType type : types)
    {
      if (!type.getCode().equals(Universal.ROOT_KEY))
      {
        this.deleteWMSLayer(type);
      }
    }
  }
  
  @Request(RequestType.SESSION)
  public void createWMSLayer(GeoObjectType type, boolean forceGeneration)
  {
    this.createDatabaseView(type, forceGeneration);

    this.createGeoServerLayer(type, forceGeneration);
  }

  public void createGeoServerLayer(GeoObjectType type, boolean forceGeneration)
  {
    String viewName = this.getViewName(type.getCode());

    if (forceGeneration)
    {
      service.forceRemoveLayer(viewName);
    }

    // Now that the database transaction is complete we can create the geoserver
    // layer
    if (!service.layerExists(viewName))
    {
      service.publishLayer(viewName, null);
    }
  }
  
  public void deleteWMSLayer(String geoObjectTypeCode)
  {
    String viewName = this.getViewName(geoObjectTypeCode);

    service.forceRemoveLayer(viewName);

    this.deleteDatabaseView(geoObjectTypeCode);
  }
  
  public void deleteWMSLayer(GeoObjectType type)
  {
    this.deleteWMSLayer(type.getCode());
  }

  private String getViewName(String typeCode)
  {
    String viewName = MetadataDAO.convertCamelCaseToUnderscore(typeCode).toLowerCase();

    if (viewName.length() > Database.MAX_DB_IDENTIFIER_SIZE)
    {
      viewName = viewName.substring(0, Database.MAX_DB_IDENTIFIER_SIZE);
    }

    return PREFIX + "_" + viewName;
  }
  
  @Transaction
  public void deleteDatabaseView(GeoObjectType type)
  {
    this.deleteDatabaseView(type.getCode());
  }
  
  @Transaction
  public void deleteDatabaseView(String typeCode)
  {
    String viewName = this.getViewName(typeCode);

    Database.dropView(viewName, null, false);
  }

  @Transaction
  public String createDatabaseView(GeoObjectType type, boolean forceGeneration)
  {
    String viewName = this.getViewName(type.getCode());

    ValueQuery vQuery = this.generateQuery(type);

    if (forceGeneration)
    {
      Database.dropView(viewName, null, false);
    }

    if (!GeoserverFacade.viewExists(viewName))
    {
      Database.createView(viewName, vQuery.getSQL());
    }

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
      AttributeLocal aLocalCharacter = bQuery.aLocalCharacter(DefaultAttribute.LOCALIZED_DISPLAY_LABEL.getName());
      MdLocalStructDAOIF mdLocalStruct = aLocalCharacter.getMdStructDAOIF();

      AttributeCharacter code = bQuery.aCharacter(DefaultAttribute.CODE.getName());
      code.setColumnAlias(DefaultAttribute.CODE.getName());

      vQuery.SELECT(code);

      for (MdAttributeConcreteDAOIF mdAttributeConcrete : mdLocalStruct.definesAttributes())
      {
        if (this.isValidLocale(mdAttributeConcrete))
        {
          Attribute attribute = aLocalCharacter.get(mdAttributeConcrete.definesAttribute());
          attribute.setColumnAlias(mdAttributeConcrete.getColumnName());

          vQuery.SELECT(attribute);
        }
      }

      Attribute geometry = bQuery.get(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);
      geometry.setColumnAlias(GeoserverFacade.GEOM_COLUMN);

      vQuery.SELECT(geometry);

      vQuery.ORDER_BY_ASC(code);
    }
    else
    {
      GeoEntityQuery geQuery = new GeoEntityQuery(vQuery);

      SelectableChar geoId = geQuery.getGeoId(DefaultAttribute.CODE.getName());
      geoId.setColumnAlias(DefaultAttribute.CODE.getName());

      vQuery.SELECT(geoId);

      GeoEntityDisplayLabelQueryStructIF aLocalCharacter = geQuery.getDisplayLabel();
      MdLocalStructDAOIF mdLocalStruct = aLocalCharacter.getMdStructDAOIF();

      for (MdAttributeConcreteDAOIF mdAttributeConcrete : mdLocalStruct.definesAttributes())
      {
        if (this.isValidLocale(mdAttributeConcrete))
        {
          Attribute attribute = aLocalCharacter.get(mdAttributeConcrete.definesAttribute());
          attribute.setColumnAlias(mdAttributeConcrete.getColumnName());

          vQuery.SELECT(attribute);
        }
      }

      if (type.getGeometryType().equals(GeometryType.LINE))
      {
        SelectableGeometry geometry = geQuery.getGeoLine(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);
        geometry.setColumnAlias(GeoserverFacade.GEOM_COLUMN);

        vQuery.SELECT(geometry);
      }
      else if (type.getGeometryType().equals(GeometryType.MULTILINE))
      {
        SelectableGeometry geometry = geQuery.getGeoMultiLine(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);
        geometry.setColumnAlias(GeoserverFacade.GEOM_COLUMN);

        vQuery.SELECT(geometry);
      }
      else if (type.getGeometryType().equals(GeometryType.POINT))
      {
        SelectableGeometry geometry = geQuery.getGeoPoint(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);
        geometry.setColumnAlias(GeoserverFacade.GEOM_COLUMN);

        vQuery.SELECT(geometry);
      }
      else if (type.getGeometryType().equals(GeometryType.MULTIPOINT))
      {
        SelectableGeometry geometry = geQuery.getGeoMultiPoint(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);
        geometry.setColumnAlias(GeoserverFacade.GEOM_COLUMN);

        vQuery.SELECT(geometry);
      }
      else if (type.getGeometryType().equals(GeometryType.POLYGON))
      {
        SelectableGeometry geometry = geQuery.getGeoPolygon(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);
        geometry.setColumnAlias(GeoserverFacade.GEOM_COLUMN);

        vQuery.SELECT(geometry);
      }
      else if (type.getGeometryType().equals(GeometryType.MULTIPOLYGON))
      {
        SelectableGeometry geometry = geQuery.getGeoMultiPolygon(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);
        geometry.setColumnAlias(GeoserverFacade.GEOM_COLUMN);

        vQuery.SELECT(geometry);
      }

      vQuery.WHERE(geQuery.getUniversal().EQ(universal));
    }

    return vQuery;
  }

  private boolean isValidLocale(MdAttributeConcreteDAOIF mdAttributeConcrete)
  {
    if (mdAttributeConcrete.isSystem() || mdAttributeConcrete.definesAttribute().equals(MdEntityInfo.KEY))
    {
      return false;
    }

    return true;
  }
}
