package net.geoprism.registry.service.business;

import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

public interface GPRGeoObjectBusinessServiceIF
{

  String getExternalId(VertexServerGeoObject sgo, ExternalSystem system);

  VertexServerGeoObject getByExternalId(String externalId, DHIS2ExternalSystem system, ServerGeoObjectType type);

  void createExternalId(ServerGeoObjectIF sgo, ExternalSystem system, String id, ImportStrategy importStrategy);

}
