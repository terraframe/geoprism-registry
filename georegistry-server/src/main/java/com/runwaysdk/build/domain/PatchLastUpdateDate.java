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
package com.runwaysdk.build.domain;

import java.util.Date;
import java.util.List;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;

import net.geoprism.registry.graph.GeoVertex;

public class PatchLastUpdateDate
{
  public static void main(String[] args)
  {
    new PatchLastUpdateDate().doIt();
  }

  @Transaction
  private void doIt()
  {
    Date date = new Date();

    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(GeoVertex.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(GeoVertex.LASTUPDATEDATE);
    MdAttributeDAOIF createDate = mdVertex.definesAttribute(GeoVertex.CREATEDATE);

    long pageSize = 1000;

    long count = 0;

    do
    {
      StringBuilder builder = new StringBuilder();
      builder.append("SELECT FROM " + mdVertex.getDBClassName());
      builder.append(" WHERE " + mdAttribute.getColumnName() + " IS NULL");
      builder.append(" OR " + createDate.getColumnName() + " IS NULL");
      builder.append(" ORDER BY oid");
      builder.append(" SKIP " + 0 + " LIMIT " + pageSize);

      GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(builder.toString());

      List<VertexObject> results = query.getResults();

      for (VertexObject result : results)
      {
        result.setValue(GeoVertex.LASTUPDATEDATE, date);
        result.setValue(GeoVertex.CREATEDATE, date);
        result.apply();
      }

      count = this.getCount();
    } while (count > 0);
  }

  public long getCount()
  {
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(GeoVertex.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(GeoVertex.LASTUPDATEDATE);
    MdAttributeDAOIF createDate = mdVertex.definesAttribute(GeoVertex.CREATEDATE);

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT COUNT(*) FROM " + mdVertex.getDBClassName());
    builder.append(" WHERE " + mdAttribute.getColumnName() + " IS NULL");
    builder.append(" OR " + createDate.getColumnName() + " IS NULL");

    final GraphQuery<Long> query = new GraphQuery<Long>(builder.toString());

    return query.getSingleResult();
  }

}
