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

    long skip = 0;
    long pageSize = 1000;

    long count = this.getCount();

    while (skip < count)
    {

      StringBuilder builder = new StringBuilder();
      builder.append("SELECT FROM " + mdVertex.getDBClassName());
      builder.append(" WHERE " + mdAttribute.getColumnName() + " IS NULL");
      builder.append(" SKIP " + skip + " LIMIT " + pageSize);

      GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(builder.toString());

      List<VertexObject> results = query.getResults();

      for (VertexObject result : results)
      {
        result.setValue(GeoVertex.LASTUPDATEDATE, date);
        result.setValue(GeoVertex.CREATEDATE, date);
        result.apply();
      }

      skip += pageSize;
    }
  }

  public long getCount()
  {
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(GeoVertex.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(GeoVertex.LASTUPDATEDATE);

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT COUNT(*) FROM " + mdVertex.getDBClassName());
    builder.append(" WHERE " + mdAttribute.getColumnName() + " IS NULL");

    final GraphQuery<Long> query = new GraphQuery<Long>(builder.toString());

    return query.getSingleResult();
  }

}
