package net.geoprism.registry.model;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.gis.geo.GeoEntity;

import net.geoprism.ontology.GeoEntityUtil;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.service.RegistryIdService;

public class ServerTreeGeoObject implements ServerGeoObjectIF
{
  private Logger              logger = LoggerFactory.getLogger(ServerTreeGeoObject.class);

  private ServerGeoObjectType type;

  private GeoObject           go;

  private GeoEntity           geoEntity;

  private Business            business;

  public ServerTreeGeoObject(ServerGeoObjectType type, GeoObject go, GeoEntity geoEntity, Business business)
  {
    this.type = type;
    this.go = go;
    this.geoEntity = geoEntity;
    this.business = business;
  }

  public ServerGeoObjectType getType()
  {
    return type;
  }

  @Override
  public String bbox()
  {
    String runwayId = RegistryIdService.getInstance().registryIdToRunwayId(go.getUid(), go.getType());

    return GeoEntityUtil.getEntitiesBBOX(new String[] { runwayId });
  }

  public static Business getBusiness(GeoEntity entity)
  {
    QueryFactory qf = new QueryFactory();
    BusinessQuery bq = qf.businessQuery(entity.getUniversal().getMdBusiness().definesType());
    bq.WHERE(bq.aReference(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME).EQ(entity));
    OIterator<? extends Business> bit = bq.getIterator();
    try
    {
      if (bit.hasNext())
      {
        return bit.next();
      }
    }
    finally
    {
      bit.close();
    }

    return null;
  }

}
