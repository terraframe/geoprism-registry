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

import java.util.List;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;

import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.SearchService;

public class SearchTablePatch
{
  public static void main(String[] args)
  {
    new SearchTablePatch().doIt();
  }

  @Transaction
  private void doIt()
  {
    SearchService service = new SearchService();
    service.createSearchTable();

    createRecords(service);
  }

  @Transaction
  public void createRecords(SearchService service)
  {
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(GeoVertex.CLASS);

    long pageSize = 1000;
    long skip = 0;

    int count = 0;

    do
    {
      StringBuilder builder = new StringBuilder();
      builder.append("SELECT FROM " + mdVertex.getDBClassName());
      builder.append(" ORDER BY oid");
      builder.append(" SKIP " + skip + " LIMIT " + pageSize);

      GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(builder.toString());

      List<VertexObject> results = query.getResults();

      for (VertexObject result : results)
      {
        ServerGeoObjectType type = ServerGeoObjectType.get((MdVertexDAOIF) result.getMdClass());

        service.insert(new VertexServerGeoObject(type, result), true);
      }

      skip += pageSize;
      count = results.size();
    } while (count > 0);
  }
}
