package net.geoprism.registry.query.graph;

import java.util.Date;
import java.util.Map;

import com.runwaysdk.dataaccess.MdEdgeDAOIF;

import net.geoprism.registry.model.ServerHierarchyType;

public class VertexLookupRestriction extends AbstractVertexRestriction implements VertexGeoObjectRestriction
{
  private String              text;

  private String              parentCode;

  private ServerHierarchyType hierarchyType;

  private Date                date;

  public VertexLookupRestriction(String text, Date date)
  {
    this.text = text;
    this.date = date;
    this.parentCode = null;
    this.hierarchyType = null;
  }

  public VertexLookupRestriction(String text, Date date, String parentCode, ServerHierarchyType hierarchyType)
  {
    this.text = text;
    this.date = date;
    this.parentCode = parentCode;
    this.hierarchyType = hierarchyType;
  }

  @Override
  public void restrict(StringBuilder statement, Map<String, Object> parameters)
  {
    statement.append(",where: (displayLabel_cot CONTAINS (:date BETWEEN startDate AND endDate AND " + localize("value") + " LIKE '%' + :text + '%'))");

    parameters.put("text", this.text);
    parameters.put("date", this.date);

    if (this.parentCode != null && this.hierarchyType != null)
    {
      MdEdgeDAOIF mdEdge = this.hierarchyType.getMdEdge();

      statement.append("}.in('" + mdEdge.getDBClassName() + "'){where: (code=:code), while: ($depth < 1)");

      parameters.put("code", this.parentCode);
    }
  }

}
