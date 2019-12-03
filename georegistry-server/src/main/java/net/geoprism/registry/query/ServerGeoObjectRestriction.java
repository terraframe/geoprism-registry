package net.geoprism.registry.query;

import net.geoprism.registry.query.graph.VertexGeoObjectQuery;
import net.geoprism.registry.query.graph.VertexGeoObjectRestriction;
import net.geoprism.registry.query.postgres.LeafGeoObjectQuery;
import net.geoprism.registry.query.postgres.LeafGeoObjectRestriction;
import net.geoprism.registry.query.postgres.TreeGeoObjectQuery;
import net.geoprism.registry.query.postgres.TreeGeoObjectRestriction;

public interface ServerGeoObjectRestriction
{
  public TreeGeoObjectRestriction create(TreeGeoObjectQuery query);

  public LeafGeoObjectRestriction create(LeafGeoObjectQuery query);

  public VertexGeoObjectRestriction create(VertexGeoObjectQuery query);
}
