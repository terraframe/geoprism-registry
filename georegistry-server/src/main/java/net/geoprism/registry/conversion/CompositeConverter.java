package net.geoprism.registry.conversion;

import org.commongeoregistry.adapter.dataaccess.GeoObject;

import com.runwaysdk.business.graph.VertexObject;

import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.CompositeServerGeoObject;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.VertexServerGeoObject;

public class CompositeConverter implements ServerGeoObjectConverterIF
{
  private ServerGeoObjectConverterIF bConverter;

  private VertexGeoObjectConverter   vConverter;

  public CompositeConverter(ServerGeoObjectConverterIF converter)
  {
    this.bConverter = converter;
    this.vConverter = new VertexGeoObjectConverter(converter.getType());
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

    VertexObject dbVertex = GeoVertex.getVertex(this.getType(), business.getUid());

    VertexServerGeoObject vertex = this.vConverter.constructFromDB(dbVertex);

    return new CompositeServerGeoObject(business, vertex);
  }

}
