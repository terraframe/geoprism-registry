package net.geoprism.registry.query.graph;

import java.util.Date;
import java.util.Map;

import com.runwaysdk.dataaccess.MdEdgeDAOIF;

import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerHierarchyType;

public class VertexSynonymRestriction extends AbstractVertexRestriction implements VertexGeoObjectRestriction
{
  private String              label;

  private ServerGeoObjectIF   parent;

  private ServerHierarchyType hierarchyType;

  private Date                date;

  public VertexSynonymRestriction(String label, Date date)
  {
    this.label = label;
    this.date = date;
    this.parent = null;
    this.hierarchyType = null;
  }

  public VertexSynonymRestriction(String label, Date date, ServerGeoObjectIF parent, ServerHierarchyType hierarchyType)
  {
    this.label = label;
    this.date = date;
    this.parent = parent;
    this.hierarchyType = hierarchyType;
  }

  @Override
  public void restrict(StringBuilder statement, Map<String, Object> parameters)
  {
    statement.append(",where: (code = :label");
    statement.append(" OR displayLabel_cot CONTAINS (:date BETWEEN startDate AND endDate AND " + localize("value") + " = :label)");
    statement.append(" OR out('geo_vertex_has_synonym').label CONTAINS :label)");

    parameters.put("label", this.label);
    parameters.put("date", this.date);

    if (this.parent != null && this.hierarchyType != null)
    {
      MdEdgeDAOIF mdEdge = this.hierarchyType.getMdEdge();

      statement.append("}.in('" + mdEdge.getDBClassName() + "'){where: (uuid=:uuid), while: (true)");

      parameters.put("uuid", this.parent.getUid());
    }
  }
}
