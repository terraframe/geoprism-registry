package net.geoprism.registry.conversion;

import java.util.Date;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;

import com.runwaysdk.business.Business;

import net.geoprism.registry.InvalidRegistryIdException;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.postgres.LeafServerGeoObject;
import net.geoprism.registry.query.ServerGeoObjectQuery;
import net.geoprism.registry.query.postgres.LeafGeoObjectQuery;
import net.geoprism.registry.service.RegistryIdService;

public class LeafGeoObjectStrategy extends LocalizedValueConverter implements ServerGeoObjectStrategyIF
{
  private ServerGeoObjectType type;

  public LeafGeoObjectStrategy(ServerGeoObjectType type)
  {
    super();

    this.type = type;
  }

  @Override
  public ServerGeoObjectType getType()
  {
    return this.type;
  }

  @Override
  public LeafServerGeoObject constructFromGeoObject(GeoObject geoObject, boolean isNew)
  {
    if (!isNew)
    {
      String runwayId = RegistryIdService.getInstance().registryIdToRunwayId(geoObject.getUid(), geoObject.getType());

      Business business = Business.get(runwayId);

      return new LeafServerGeoObject(type, business);
    }
    else
    {
      if (!RegistryIdService.getInstance().isIssuedId(geoObject.getUid()))
      {
        InvalidRegistryIdException ex = new InvalidRegistryIdException();
        ex.setRegistryId(geoObject.getUid());
        throw ex;
      }

      Business business = new Business(type.definesType());

      return new LeafServerGeoObject(type, business);
    }
  }
  
  @Override
  public LeafServerGeoObject constructFromGeoObjectOverTime(GeoObjectOverTime goTime, boolean isNew)
  {
    if (!isNew)
    {
      String runwayId = RegistryIdService.getInstance().registryIdToRunwayId(goTime.getUid(), goTime.getType());

      Business business = Business.get(runwayId);

      return new LeafServerGeoObject(type, business);
    }
    else
    {
      if (!RegistryIdService.getInstance().isIssuedId(goTime.getUid()))
      {
        InvalidRegistryIdException ex = new InvalidRegistryIdException();
        ex.setRegistryId(goTime.getUid());
        throw ex;
      }

      Business business = new Business(type.definesType());

      return new LeafServerGeoObject(type, business);
    }
  }

  @Override
  public LeafServerGeoObject constructFromDB(Object dbObject)
  {
    Business business = (Business) dbObject;

    return new LeafServerGeoObject(type, business);
  }

  @Override
  public ServerGeoObjectIF getGeoObjectByCode(String code)
  {
    Business business = LeafServerGeoObject.getByCode(type, code);

    if (business != null)
    {
      return new LeafServerGeoObject(type, business);
    }

    return null;
  }

  @Override
  public ServerGeoObjectIF newInstance()
  {
    Business business = new Business(type.definesType());

    return new LeafServerGeoObject(type, business);
  }

  @Override
  public ServerGeoObjectQuery createQuery(Date date)
  {
    return new LeafGeoObjectQuery(type);
  }

}
