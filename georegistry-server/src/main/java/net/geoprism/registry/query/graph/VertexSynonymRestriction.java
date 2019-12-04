package net.geoprism.registry.query.graph;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.runwaysdk.constants.MdAttributeLocalEmbeddedInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdGraphClassDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdGraphClassDAO;
import com.runwaysdk.session.Session;

import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerHierarchyType;

public class VertexSynonymRestriction implements VertexGeoObjectRestriction
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

  public String localize(String prefix)
  {
    final MdGraphClassDAOIF mdLocalStruct = MdGraphClassDAO.getMdGraphClassDAO(MdAttributeLocalEmbeddedInfo.EMBEDDED_LOCAL_VALUE);
    Locale locale = Session.getCurrentLocale();

    List<String> list = new ArrayList<String>();

    String localeString = locale.toString();

    for (int i = localeString.length(); i > 0; i = localeString.lastIndexOf('_', i - 1))
    {
      String subLocale = localeString.substring(0, i);

      for (MdAttributeConcreteDAOIF a : mdLocalStruct.definesAttributes())
      {
        if (a.definesAttribute().equalsIgnoreCase(subLocale))
        {
          list.add(subLocale);
        }
      }
    }

    list.add(MdAttributeLocalInfo.DEFAULT_LOCALE);

    StringBuilder builder = new StringBuilder();
    builder.append("COALESCE(");

    for (int i = 0; i < list.size(); i++)
    {
      if (i != 0)
      {
        builder.append(", ");
      }

      builder.append(prefix + "." + list.get(i));
    }

    builder.append(")");

    return builder.toString();
  }

}
