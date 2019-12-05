package net.geoprism.registry.service;

import java.util.Date;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;

import com.runwaysdk.business.Business;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.gis.geo.GeoEntity;

import net.geoprism.registry.conversion.CompositeGeoObjectStrategy;
import net.geoprism.registry.conversion.LeafGeoObjectStrategy;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.conversion.ServerGeoObjectStrategyIF;
import net.geoprism.registry.conversion.TreeGeoObjectStrategy;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.query.ServerGeoObjectQuery;
import net.geoprism.registry.query.graph.VertexGeoObjectQuery;

public class ServerGeoObjectService extends LocalizedValueConverter
{
  @Transaction
  public ServerGeoObjectIF apply(GeoObject object, boolean isNew, boolean isImport)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(object.getType());
    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);

    ServerGeoObjectIF geoObject = strategy.constructFromGeoObject(object, isNew);

    if (!isNew)
    {
      geoObject.lock();
    }

    geoObject.populate(object);
    geoObject.apply(isImport);

    // Return the refreshed copy of the geoObject
    return this.build(type, geoObject.getRunwayId());
  }
  
  @Transaction
  public ServerGeoObjectIF apply(GeoObjectOverTime goTime, boolean isNew, boolean isImport)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(goTime.getType());
    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);

    ServerGeoObjectIF goServer = strategy.constructFromGeoObjectOverTime(goTime, isNew);

    if (!isNew)
    {
      goServer.lock();
    }

    goServer.populate(goTime);
    goServer.apply(isImport);

    // Return the refreshed copy of the geoObject
    return this.build(type, goServer.getRunwayId());
  }

  public ServerGeoObjectIF newInstance(ServerGeoObjectType type)
  {
    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);

    return strategy.newInstance();
  }

  public ServerGeoObjectIF getGeoObject(GeoObject go)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(go.getType());

    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);

    return strategy.constructFromGeoObject(go, false);
  }
  
  public ServerGeoObjectIF getGeoObject(GeoObjectOverTime timeGO)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(timeGO.getType());

    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);

    return strategy.constructFromGeoObjectOverTime(timeGO, false);
  }

  public ServerGeoObjectIF getGeoObjectByCode(String code, String typeCode)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);
    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);

    return strategy.getGeoObjectByCode(code);
  }

  public ServerGeoObjectIF getGeoObjectByCode(String code, ServerGeoObjectType type)
  {
    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);

    return strategy.getGeoObjectByCode(code);
  }

  public ServerGeoObjectIF getGeoObject(String uid, String typeCode)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);

    String runwayId = RegistryIdService.getInstance().registryIdToRunwayId(uid, type);
    Business business = Business.get(runwayId);

    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);
    return strategy.constructFromDB(business);
  }

  public ServerGeoObjectIF getGeoObjectByEntityId(String entityId)
  {
    GeoEntity entity = GeoEntity.get(entityId);

    ServerGeoObjectType type = ServerGeoObjectType.get(entity.getUniversal());

    return this.build(type, entity);
  }

  public ServerGeoObjectStrategyIF getStrategy(ServerGeoObjectType type)
  {
    if (type.isLeaf())
    {
      return new CompositeGeoObjectStrategy(new LeafGeoObjectStrategy(type));
    }

    return new CompositeGeoObjectStrategy(new TreeGeoObjectStrategy(type));
  }

  public ServerGeoObjectIF build(GeoEntity entity)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(entity.getUniversal());

    return this.build(type, entity);
  }

  public ServerGeoObjectIF build(ServerGeoObjectType type, String runwayId)
  {
    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);
    Business business = Business.get(runwayId);

    return strategy.constructFromDB(business);
  }

  public ServerGeoObjectIF build(ServerGeoObjectType type, Object dbObject)
  {
    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);
    return strategy.constructFromDB(dbObject);
  }

  public ServerGeoObjectQuery createQuery(ServerGeoObjectType type, Date date)
  {
     return new VertexGeoObjectQuery(type, date);
//    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);
//    return strategy.createQuery(date);
  }

}
