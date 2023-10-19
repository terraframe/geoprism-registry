package net.geoprism.registry.service.business;

import java.util.List;

import org.commongeoregistry.adapter.dataaccess.AlternateId;
import org.springframework.stereotype.Component;

import net.geoprism.registry.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

@Component
public interface GPRGeoObjectBusinessServiceIF extends GeoObjectBusinessServiceIF
{

  public String getExternalId(ServerGeoObjectIF sgo, ExternalSystem system);
  
  public void setAlternateIds(ServerGeoObjectIF sgo, List<AlternateId> alternateIds);

  public VertexServerGeoObject getByExternalId(String externalId, ExternalSystem system, ServerGeoObjectType type);

  public void createExternalId(ServerGeoObjectIF sgo, ExternalSystem system, String id, ImportStrategy importStrategy);

}
