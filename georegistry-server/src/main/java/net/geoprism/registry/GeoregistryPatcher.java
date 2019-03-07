/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Runway SDK(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry;

import com.runwaysdk.business.ontology.OntologyStrategyBuilderIF;
import com.runwaysdk.business.ontology.OntologyStrategyFactory;
import com.runwaysdk.business.ontology.OntologyStrategyIF;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdBusinessQuery;
import com.runwaysdk.system.metadata.ontology.DatabaseAllPathsStrategy;

import net.geoprism.GeoprismPatcher;
import net.geoprism.GeoprismPatcherIF;
import net.geoprism.registry.demo.ChangeRequestTestDataGenerator;

public class GeoregistryPatcher extends GeoprismPatcher implements GeoprismPatcherIF
{
  @Override
  @Transaction
  public void runWithTransaction()
  {
    super.runWithTransaction();
    
    // TODO : This is only for demos
    ChangeRequestTestDataGenerator.main(new String[] {});
  }
  
  @Override
  protected void importLocationData()
  {
    super.importLocationData();

    QueryFactory qf = new QueryFactory();
    MdBusinessQuery mbq = new MdBusinessQuery(qf);

    mbq.WHERE(mbq.getPackageName().EQ(RegistryConstants.UNIVERSAL_MDBUSINESS_PACKAGE));

    OIterator<? extends MdBusiness> it = mbq.getIterator();

    while (it.hasNext())
    {
      MdBusiness biz = it.next();

      System.out.println("Assigning default role permissions for [" + biz.definesType() + "].");

      AdapterUtilities.getInstance().assignDefaultRolePermissions(biz);
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
}
