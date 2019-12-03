/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry;

import org.commongeoregistry.adapter.metadata.FrequencyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.ontology.OntologyStrategyBuilderIF;
import com.runwaysdk.business.ontology.OntologyStrategyFactory;
import com.runwaysdk.business.ontology.OntologyStrategyIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.dataaccess.metadata.graph.MdGeoVertexDAO;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.UniversalQuery;
import com.runwaysdk.system.graph.ChangeFrequency;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdClass;
import com.runwaysdk.system.metadata.MdClassQuery;
import com.runwaysdk.system.metadata.MdVertex;
import com.runwaysdk.system.metadata.ontology.DatabaseAllPathsStrategy;

import net.geoprism.GeoprismPatcher;
import net.geoprism.GeoprismPatcherIF;
import net.geoprism.registry.conversion.ServerGeoObjectTypeConverter;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.graph.GeoVertexType;
import net.geoprism.registry.model.ServerGeoObjectType;

public class GeoregistryPatcher extends GeoprismPatcher implements GeoprismPatcherIF
{
  private static final Logger logger = LoggerFactory.getLogger(GeoregistryPatcher.class);
  
  @Override
  @Transaction
  public void runWithTransaction()
  {
    super.runWithTransaction();

    // TODO : This is only for demos
    // ChangeRequestTestDataGenerator.main(new String[] {});
  }

  @Override
  protected void importLocationData()
  {
    super.importLocationData();

    QueryFactory qf = new QueryFactory();
    UniversalQuery mbq = new UniversalQuery(qf);

//    mbq.WHERE(mbq.getPackageName().EQ(RegistryConstants.UNIVERSAL_MDBUSINESS_PACKAGE));
    mbq.WHERE(mbq.getKeyName().NE("ROOT"));

    OIterator<? extends Universal> it = mbq.getIterator();

    while (it.hasNext())
    {
      Universal universal = it.next();
      MdBusiness mdBusiness = universal.getMdBusiness();
      ServerGeoObjectTypeConverter builder = new ServerGeoObjectTypeConverter();
      
      // Create the MdGeoVertexClass
      MdVertex mdv = (MdVertex) getMdClassIfExist(RegistryConstants.UNIVERSAL_GRAPH_PACKAGE, universal.getUniversalId());
      
      if (mdv == null)
      {
        logger.info("Creating Vertex objects for [" + mdBusiness.definesType() + "].");
        
        MdGeoVertexDAO mdVertex = GeoVertexType.create(universal.getUniversalId(), FrequencyType.DAILY);
        builder.createDefaultAttributes(universal, mdVertex);
  
        logger.info("Assigning default role permissions for [" + mdBusiness.definesType() + "].");
  
        builder.assignDefaultRolePermissions(mdBusiness);
  
        // Build the parent class term root if it does not exist.
        TermConverter.buildIfNotExistdMdBusinessClassifier(mdBusiness);
      }
    }
  }

  @Override
  protected void configureStrategies()
  {
    OntologyStrategyFactory.set(GeoEntity.CLASS, new OntologyStrategyBuilderIF()
    {
      @Override
      public OntologyStrategyIF build()
      {
        return DatabaseAllPathsStrategy.factory(GeoEntity.CLASS);
      }
    });
  }
  
  public MdClass getMdClassIfExist(String pack, String type)
  {
    MdClassQuery mbq = new MdClassQuery(new QueryFactory());
    mbq.WHERE(mbq.getPackageName().EQ(pack));
    mbq.WHERE(mbq.getTypeName().EQ(type));
    OIterator<? extends MdClass> it = mbq.getIterator();
    try
    {
      while (it.hasNext())
      {
        return it.next();
      }
    }
    finally
    {
      it.close();
    }

    return null;
  }
}
