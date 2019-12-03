package net.geoprism.registry.conversion;

import java.util.Date;

import org.commongeoregistry.adapter.dataaccess.GeoObject;

import com.runwaysdk.business.graph.VertexObject;

import net.geoprism.registry.model.CompositeServerGeoObject;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.query.ServerGeoObjectQuery;

/**
 * Strategy used for creating CompositeServerGeoObjects
 * 
 * @author terraframe
 */
public class CompositeGeoObjectStrategy implements ServerGeoObjectStrategyIF
{
  private ServerGeoObjectStrategyIF bConverter;

  private VertexGeoObjectStrategy   vConverter;

  public CompositeGeoObjectStrategy(ServerGeoObjectStrategyIF converter)
  {
    this.bConverter = converter;
    this.vConverter = new VertexGeoObjectStrategy(converter.getType());
  }

  @Override
  public ServerGeoObjectType getType()
  {
    return this.bConverter.getType();
  }

  @Override
  public ServerGeoObjectIF constructFromGeoObject(GeoObject geoObject, boolean isNew)
  {
    ServerGeoObjectIF business = this.bConverter.constructFromGeoObject(geoObject, isNew);
    VertexServerGeoObject vertex = this.vConverter.constructFromGeoObject(geoObject, isNew);

    return new CompositeServerGeoObject(business, vertex);
  }

  @Override
  public ServerGeoObjectIF constructFromDB(Object dbObject)
  {
    ServerGeoObjectIF business = this.bConverter.constructFromDB(dbObject);

    VertexObject dbVertex = VertexServerGeoObject.getVertex(this.getType(), business.getUid());

    VertexServerGeoObject vertex = this.vConverter.constructFromDB(dbVertex);

    return new CompositeServerGeoObject(business, vertex);
  }

  @Override
  public ServerGeoObjectIF getGeoObjectByCode(String code)
  {
    ServerGeoObjectIF business = this.bConverter.getGeoObjectByCode(code);

    if (business != null)
    {
      VertexServerGeoObject vertex = this.vConverter.getGeoObjectByCode(code);

      return new CompositeServerGeoObject(business, vertex);
    }

    return null;
  }

  @Override
  public ServerGeoObjectIF newInstance()
  {
    ServerGeoObjectIF business = this.bConverter.newInstance();
    VertexServerGeoObject vertex = this.vConverter.newInstance();

    return new CompositeServerGeoObject(business, vertex);
  }

  @Override
  public ServerGeoObjectQuery createQuery(Date date)
  {
    return this.bConverter.createQuery(date);
  }

}
