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
package net.geoprism.registry.build;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdClass;
import com.runwaysdk.system.metadata.MdClassQuery;

import net.geoprism.build.GeoprismDatabaseBuilder;
import net.geoprism.build.GeoprismDatabaseBuilderIF;

public class GeoregistryDatabaseBuilder extends GeoprismDatabaseBuilder implements GeoprismDatabaseBuilderIF
{
  private static final Logger logger = LoggerFactory.getLogger(GeoregistryDatabaseBuilder.class);
  
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
  }
  
//  private void patchInGraphMetadata()
//  {
//    QueryFactory qf = new QueryFactory();
//    UniversalQuery mbq = new UniversalQuery(qf);
//
//    mbq.WHERE(mbq.getKeyName().NE("ROOT"));
//
//    OIterator<? extends Universal> it = mbq.getIterator();
//
//    while (it.hasNext())
//    {
//      Universal universal = it.next();
//      MdBusiness mdBusiness = universal.getMdBusiness();
//      ServerGeoObjectTypeConverter builder = new ServerGeoObjectTypeConverter();
//      
//      // Create the MdGeoVertexClass
//      MdVertex mdv = (MdVertex) getMdClassIfExist(RegistryConstants.UNIVERSAL_GRAPH_PACKAGE, universal.getUniversalId());
//      
//      if (mdv == null)
//      {
//        logger.info("Creating Vertex objects for [" + mdBusiness.definesType() + "].");
//
//        MdGeoVertexDAO mdVertex = GeoVertexType.create(universal.getUniversalId(), universal.getOwnerOid());
//        builder.createDefaultAttributes(universal, mdVertex);
//  
//        logger.info("Assigning default role permissions for [" + mdBusiness.definesType() + "].");
//  
//        builder.assignSRAPermissions(mdBusiness);
//  
//        // Build the parent class term root if it does not exist.
//        TermConverter.buildIfNotExistdMdBusinessClassifier(mdBusiness);
//      }
//    }
//  }

  @Override
  protected void configureStrategies()
  {
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
