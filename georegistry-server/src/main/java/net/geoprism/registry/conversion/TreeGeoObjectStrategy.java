package net.geoprism.registry.conversion;

import java.util.Date;

import org.commongeoregistry.adapter.dataaccess.GeoObject;

import com.runwaysdk.business.Business;
import com.runwaysdk.system.gis.geo.GeoEntity;

import net.geoprism.registry.InvalidRegistryIdException;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.postgres.TreeServerGeoObject;
import net.geoprism.registry.query.ServerGeoObjectQuery;
import net.geoprism.registry.query.postgres.TreeGeoObjectQuery;
import net.geoprism.registry.service.RegistryIdService;

public class TreeGeoObjectStrategy extends LocalizedValueConverter implements ServerGeoObjectStrategyIF
{
  private ServerGeoObjectType type;

  public TreeGeoObjectStrategy(ServerGeoObjectType type)
  {
    super();

    this.type = type;
  }

  @Override
  public ServerGeoObjectType getType()
  {
    return this.type;
  }

  public TreeServerGeoObject constructFromGeoObject(GeoObject geoObject, boolean isNew)
  {
    if (!isNew)
    {
      String runwayId = RegistryIdService.getInstance().registryIdToRunwayId(geoObject.getUid(), type.getType());

      GeoEntity entity = GeoEntity.get(runwayId);
      Business business = TreeServerGeoObject.getBusiness(entity);

      return new TreeServerGeoObject(type, entity, business);
    }
    else
    {
      if (!RegistryIdService.getInstance().isIssuedId(geoObject.getUid()))
      {
        InvalidRegistryIdException ex = new InvalidRegistryIdException();
        ex.setRegistryId(geoObject.getUid());
        throw ex;
      }

      GeoEntity entity = new GeoEntity();
      entity.setUniversal(type.getUniversal());

      Business business = new Business(type.definesType());

      return new TreeServerGeoObject(type, entity, business);
    }
  }

  @Override
  public TreeServerGeoObject constructFromDB(Object dbObject)
  {
    GeoEntity geoEntity = (GeoEntity) dbObject;
    Business business = TreeServerGeoObject.getBusiness(geoEntity);

    return new TreeServerGeoObject(type, geoEntity, business);
  }

  @Override
  public TreeServerGeoObject getGeoObjectByCode(String code)
  {
    Business business = TreeServerGeoObject.getByCode(type, code);

    if (business != null)
    {
      String entityId = business.getValue(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME);
      GeoEntity entity = GeoEntity.get(entityId);

      return new TreeServerGeoObject(type, entity, business);
    }

    return null;
  }

  @Override
  public TreeServerGeoObject newInstance()
  {
    GeoEntity entity = new GeoEntity();
    entity.setUniversal(type.getUniversal());

    Business business = new Business(type.definesType());

    return new TreeServerGeoObject(type, entity, business);
  }

  @Override
  public ServerGeoObjectQuery createQuery(Date date)
  {
    return new TreeGeoObjectQuery(type);
  }
}
