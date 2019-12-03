package net.geoprism.registry.query;

import java.util.List;

import net.geoprism.registry.model.ServerGeoObjectIF;

public interface ServerGeoObjectQuery
{

  public void setRestriction(ServerGeoObjectRestriction restriction);

  public ServerGeoObjectIF getSingleResult();

  public List<ServerGeoObjectIF> getResults();

  public Long getCount();
}
