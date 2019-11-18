package net.geoprism.registry.conversion;

import org.commongeoregistry.adapter.dataaccess.GeoObject;

import com.runwaysdk.business.graph.VertexObject;

import net.geoprism.registry.InvalidRegistryIdException;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.query.ServerGeoObjectQuery;
import net.geoprism.registry.query.graph.VertexGeoObjectQuery;
import net.geoprism.registry.service.RegistryIdService;

public class VertexGeoObjectStrategy extends LocalizedValueConverter implements ServerGeoObjectStrategyIF
{
  private ServerGeoObjectType type;

  public VertexGeoObjectStrategy(ServerGeoObjectType type)
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
  public VertexServerGeoObject constructFromGeoObject(GeoObject geoObject, boolean isNew)
  {
    if (!isNew)
    {
      VertexObject vertex = VertexServerGeoObject.getVertex(type, geoObject.getUid());

      return new VertexServerGeoObject(type, vertex);
    }
    else
    {
      if (!RegistryIdService.getInstance().isIssuedId(geoObject.getUid()))
      {
        InvalidRegistryIdException ex = new InvalidRegistryIdException();
        ex.setRegistryId(geoObject.getUid());
        throw ex;
      }

      VertexObject vertex = VertexServerGeoObject.newInstance(type);

      return new VertexServerGeoObject(type, vertex);
    }
  }

  @Override
  public VertexServerGeoObject constructFromDB(Object dbObject)
  {
    VertexObject vertex = (VertexObject) dbObject;

    return new VertexServerGeoObject(type, vertex);
  }

  @Override
  public VertexServerGeoObject getGeoObjectByCode(String code)
  {
    VertexObject vertex = VertexServerGeoObject.getVertexByCode(type, code);

    if (vertex != null)
    {
      return new VertexServerGeoObject(type, vertex);
    }

    return null;
  }

  @Override
  public VertexServerGeoObject newInstance()
  {
    VertexObject vertex = VertexServerGeoObject.newInstance(type);

    return new VertexServerGeoObject(type, vertex);
  }

  @Override
  public ServerGeoObjectQuery createQuery()
  {
    return new VertexGeoObjectQuery(type);
  }
}
