package com.runwaysdk.build.domain;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.business.ontology.OntologyStrategyBuilderIF;
import com.runwaysdk.business.ontology.OntologyStrategyFactory;
import com.runwaysdk.business.ontology.OntologyStrategyIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.ontology.DatabaseAllPathsStrategy;

import net.geoprism.ontology.Classifier;
import net.geoprism.ontology.ClassifierIsARelationship;
import net.geoprism.registry.Organization;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.graph.VertexComponent;

public class PatchLastUpdateDate
{
  private static Logger logger = LoggerFactory.getLogger(AddWritePermissions.class);

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
