/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.model.graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;

import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.GraphType;

public class AbstractGraphStrategy
{
  protected static class EdgeComparator implements Comparator<EdgeObject>
  {
    @Override
    public int compare(EdgeObject o1, EdgeObject o2)
    {
      Date d1 = o1.getObjectValue(GeoVertex.START_DATE);
      Date d2 = o2.getObjectValue(GeoVertex.START_DATE);

      return d1.compareTo(d2);
    }
  }
  
  private GraphType type;

  public AbstractGraphStrategy(GraphType type)
  {
    this.type = type;
  }
  
  protected SortedSet<EdgeObject> getParentEdges(VertexServerGeoObject geoObject)
  {
    TreeSet<EdgeObject> set = new TreeSet<EdgeObject>(new EdgeComparator());

    String statement = "SELECT expand(inE('" + this.type.getMdEdgeDAO().getDBClassName() + "'))";
    statement += " FROM :child";

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement);
    query.setParameter("child", geoObject.getVertex().getRID());

    set.addAll(query.getResults());

    return set;
  }
  
  protected String wrapQueryWithBounds(String innerQuery, String inOrOut, Date date, String boundsWKT, Map<String, Object> parameters)
  {
    StringBuilder statement = new StringBuilder();
    
    statement.append("SELECT FROM (");
    
    statement.append(innerQuery);
    
    statement.append(") WHERE ");
    
    statement.append("(");
    
    String dateRestriction = "";
    if (date != null)
    {
      dateRestriction = "(:date BETWEEN startDate AND endDate) AND";
      parameters.put("date", date);
    }
    
    final String[] geometryTypes = new String[] { "shape_cot", "geoPoint_cot", "geoLine_cot", "geoMultiLine_cot", "geoMultiPoint_cot", "geoMultiPolygon_cot", "shape_cot", "geoPolygon_cot" };
    
    List<String> geometryRestrictions = new ArrayList<String>();
    
    for (String geometryType : geometryTypes)
    {
      geometryRestrictions.add(inOrOut + "." + geometryType + " CONTAINS ( " + dateRestriction + " ST_INTERSECTS(value, :bounds) = true )");
    }
    
    statement.append(StringUtils.join(geometryRestrictions, " OR "));
    
    statement.append(")");
    
    parameters.put("bounds", boundsWKT);
    
    return statement.toString();
  }
}
