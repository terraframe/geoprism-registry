package net.geoprism.registry.service;

import org.commongeoregistry.adapter.dataaccess.GeoObject;

import com.runwaysdk.business.Business;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.gis.geo.GeoEntity;

import net.geoprism.registry.conversion.CompositeConverter;
import net.geoprism.registry.conversion.LeafGeoObjectConverter;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.conversion.ServerGeoObjectConverterIF;
import net.geoprism.registry.conversion.TreeGeoObjectConverter;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;

public class ServerGeoObjectService extends LocalizedValueConverter
{
  @Transaction
  public ServerGeoObjectIF apply(GeoObject object, boolean isNew, String statusCode, boolean isImport)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(object.getType());
    ServerGeoObjectConverterIF converter = this.getConverter(type);

    ServerGeoObjectIF geoObject = converter.constructFromGeoObject(object, isNew);
    geoObject.apply(statusCode, isImport);

    // Return the refreshed copy of the geoObject
    return this.build(type, geoObject.getRunwayId());
  }

  public ServerGeoObjectIF getGeoObject(GeoObject go)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(go.getType());

    ServerGeoObjectConverterIF converter = this.getConverter(type);

    return converter.constructFromGeoObject(go, false);
  }

  public ServerGeoObjectIF getGeoObject(String uid, String typeCode)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);

    String runwayId = RegistryIdService.getInstance().registryIdToRunwayId(uid, type);
    Business business = Business.get(runwayId);

    ServerGeoObjectConverterIF converter = this.getConverter(type);
    return converter.constructFromDB(business);
  }

  public ServerGeoObjectIF getGeoObjectByEntityId(String entityId)
  {
    GeoEntity entity = GeoEntity.get(entityId);

    ServerGeoObjectType type = ServerGeoObjectType.get(entity.getUniversal());

    return this.build(type, entity);
  }

  public ServerGeoObjectConverterIF getConverter(ServerGeoObjectType type)
  {
    if (type.isLeaf())
    {
      return new CompositeConverter(new LeafGeoObjectConverter(type));
    }

    return new CompositeConverter(new TreeGeoObjectConverter(type));
  }

  public ServerGeoObjectIF build(GeoEntity entity)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(entity.getUniversal());

    return this.build(type, entity);
  }

  public ServerGeoObjectIF build(ServerGeoObjectType type, String runwayId)
  {
    ServerGeoObjectConverterIF converter = this.getConverter(type);
    Business business = Business.get(runwayId);

    return converter.constructFromDB(business);
  }

  public ServerGeoObjectIF build(ServerGeoObjectType type, Object dbObject)
  {
    ServerGeoObjectConverterIF converter = this.getConverter(type);
    return converter.constructFromDB(dbObject);
  }
}
