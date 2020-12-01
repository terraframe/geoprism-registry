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

        service.insert(new VertexServerGeoObject(type, result));
      }

      skip += pageSize;
      count = results.size();
    } while (count > 0);
  }
}
