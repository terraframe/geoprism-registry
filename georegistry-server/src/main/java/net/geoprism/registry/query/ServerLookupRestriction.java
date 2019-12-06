package net.geoprism.registry.query;

import java.util.Date;

import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.query.graph.VertexGeoObjectQuery;
import net.geoprism.registry.query.graph.VertexGeoObjectRestriction;
import net.geoprism.registry.query.graph.VertexLookupRestriction;
import net.geoprism.registry.query.postgres.LeafGeoObjectQuery;
import net.geoprism.registry.query.postgres.LeafGeoObjectRestriction;
import net.geoprism.registry.query.postgres.LookupRestriction;
import net.geoprism.registry.query.postgres.TreeGeoObjectQuery;
import net.geoprism.registry.query.postgres.TreeGeoObjectRestriction;

public class ServerLookupRestriction implements ServerGeoObjectRestriction
{
  private String              text;

  private Date                startDate;

  private String              parentCode;

  private ServerHierarchyType hierarchyType;

  public ServerLookupRestriction(String text, Date startDate)
  {
    this.text = text;
    this.startDate = startDate;
    this.parentCode = null;
    this.hierarchyType = null;
  }

  public ServerLookupRestriction(String text, Date startDate, String parentCode, ServerHierarchyType hierarchyType)
  {
    this.text = text;
    this.startDate = startDate;
    this.parentCode = parentCode;
    this.hierarchyType = hierarchyType;
  }

  @Override
  public TreeGeoObjectRestriction create(TreeGeoObjectQuery query)
  {
    return new LookupRestriction(this.text, this.parentCode, this.hierarchyType.getCode());
  }

  @Override
  public LeafGeoObjectRestriction create(LeafGeoObjectQuery query)
  {
    return new LookupRestriction(this.text, this.parentCode, this.hierarchyType.getCode());
  }

  @Override
  public VertexGeoObjectRestriction create(VertexGeoObjectQuery query)
  {
    return new VertexLookupRestriction(this.text, this.startDate, this.parentCode, this.hierarchyType);
  }
}
