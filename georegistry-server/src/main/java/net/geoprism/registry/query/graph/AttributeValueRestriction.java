/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.query.graph;

import java.util.Date;
import java.util.Map;

import com.runwaysdk.dataaccess.MdAttributeClassificationDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdClassificationDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;

import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;

public class AttributeValueRestriction implements ComponentVertexRestriction
{
  private MdAttributeDAOIF mdAttribute;

  private String           operation;

  private Object           value;

  private Date             date;

  public AttributeValueRestriction(MdAttributeDAOIF mdAttribute, String operation, Object value, Date date)
  {
    this.mdAttribute = mdAttribute;
    this.operation = operation;
    this.value = value;
    this.date = date;
  }

  @Override
  public void restrict(StringBuilder statement, Map<String, Object> parameters)
  {
    statement.append("WHERE ");
    this.subquery(statement, parameters, this.mdAttribute.getColumnName());
  }

  @Override
  public void subquery(StringBuilder statement, Map<String, Object> parameters, String parameterName)
  {
    String columnName = mdAttribute.getColumnName() + "_cot";

    statement.append("(" + columnName + " CONTAINS (");
    statement.append(":date BETWEEN startDate AND endDate AND ");

    if (this.mdAttribute instanceof MdAttributeClassificationDAOIF && operation.equalsIgnoreCase("eq"))
    {
      MdClassificationDAOIF mdClassification = ( (MdAttributeClassificationDAOIF) this.mdAttribute ).getMdClassificationDAOIF();
      MdEdgeDAOIF mdEdge = mdClassification.getReferenceMdEdgeDAO();

      statement.append("value IN ( TRAVERSE out(\"" + mdEdge.getDBClassName() + "\") FROM :" + parameterName + ")");
    }
    else
    {
      statement.append("value = :" + parameterName);
    }

    // Close CONTAINS parentheses
    statement.append(")");
    // Close CONDITION parentheses
    statement.append(")");

    parameters.put(parameterName, this.value);
    parameters.put("date", this.date);
  }
}
