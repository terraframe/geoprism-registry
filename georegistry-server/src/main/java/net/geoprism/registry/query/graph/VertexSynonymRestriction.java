package net.geoprism.registry.query.graph;

import java.util.Date;
import java.util.Map;

import com.runwaysdk.dataaccess.MdEdgeDAOIF;

import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerHierarchyType;

public class VertexSynonymRestriction implements VertexGeoObjectRestriction
{
  private String              label;

  private ServerGeoObjectIF   parent;

  private ServerHierarchyType hierarchyType;

  private Date                startDate;
  
  private Date                endDate;
  
  public VertexSynonymRestriction(String label, Date startDate, Date endDate)
  {
    this.label = label;
    this.startDate = startDate;
    this.endDate = endDate;
    this.parent = null;
    this.hierarchyType = null;
  }

  public VertexSynonymRestriction(String label, Date startDate, Date endDate, ServerGeoObjectIF parent, ServerHierarchyType hierarchyType)
  {
    this.label = label;
    this.startDate = startDate;
    this.endDate = endDate;
    this.parent = parent;
    this.hierarchyType = hierarchyType;
  }

  @Override
  public void restrict(StringBuilder statement, Map<String, Object> parameters)
  {
    /*
     * MATCH {class: district0, as: location, where:
     * (coalesce(displayLabel.defaultLocale) = 'Anlong Veaeng')}
     * .in('located_in0'){as: parent, where:
     * (uuid='85b678c2-df93-4e5b-b6c4-b264b82f9f2c'), while: ($depth < 10)}
     * RETURN location, parent ORDER BY location.code LIMIT 10
     */

    // TODO auto generate coalesce localization
    statement.append(",where: (coalesce(displayLabel.defaultLocale) = :label)");

    parameters.put("label", this.label);

    if (this.parent != null && this.hierarchyType != null)
    {
      MdEdgeDAOIF mdEdge = this.hierarchyType.getMdEdge();

      statement.append(".in('" + mdEdge.getDBClassName() + "'){as: parent, where: (uuid=:uuid), while: ($depth < 10)}");

      parameters.put("uuid", this.parent.getUid());
    }
  }

}
