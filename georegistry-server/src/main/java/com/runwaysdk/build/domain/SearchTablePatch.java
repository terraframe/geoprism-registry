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
package com.runwaysdk.build.domain;

import java.util.List;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;

import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.EdgeConstant;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.business.SearchService;

public class SearchTablePatch
{

  @Transaction
  private void doIt(SearchService service)
  {
    service.createSearchTable();

    createRecords(service);
  }

  @Transaction
  public void createRecords(SearchService service)
  {
    try
    {

      MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(GeoVertex.CLASS);

      long pageSize = 1000;
      long skip = 0;

      int count = 0;

      do
      {
        // The geometries are not used so we don't need to include them they
        // query
        StringBuilder builder = new StringBuilder();
        builder.append("TRAVERSE out('" + EdgeConstant.HAS_VALUE.getDBClassName() + "') FROM (");
        builder.append(" SELECT FROM " + mdVertex.getDBClassName());
        builder.append(" ORDER BY oid");
        builder.append(" SKIP " + skip + " LIMIT " + pageSize);
        builder.append(")");

        GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(builder.toString());

        List<ServerGeoObjectIF> results = VertexServerGeoObject.processTraverseResults(query.getResults(), null);

        for (ServerGeoObjectIF result : results)
        {
          service.insert(result, true);
        }

        skip += pageSize;
        count = results.size();
      } while (count > 0);
    }
    catch (DataNotFoundException e)
    {
      // Ignore
    }
  }

  public static void main(String[] args)
  {
    try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("net.geoprism.spring.core", "net.geoprism.registry.service.business", "net.geoprism.registry.service.permission"))
    {
      SearchService service = context.getBean(SearchService.class);

      new SearchTablePatch().doIt(service);
    }

  }

}
