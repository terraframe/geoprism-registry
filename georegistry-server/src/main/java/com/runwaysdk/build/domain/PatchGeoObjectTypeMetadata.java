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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.UniversalQuery;

import net.geoprism.registry.model.GeoObjectTypeMetadata;
import net.geoprism.registry.model.GeoObjectTypeMetadataQuery;

public class PatchGeoObjectTypeMetadata
{
  private static final Logger logger = LoggerFactory.getLogger(PatchGeoObjectTypeMetadata.class);
  
  public static void main(String[] args)
  {
    new PatchGeoObjectTypeMetadata().doIt();
  }

  @Transaction
  private void doIt()
  {
    QueryFactory qf = new QueryFactory();
    UniversalQuery uq = new UniversalQuery(qf);
    OIterator<? extends Universal> it = uq.getIterator();

    try
    {
      while (it.hasNext())
      {
        Universal uni = it.next();

        if (uni.getKey().equals(Universal.ROOT_KEY))
        {
          continue;
        }

        GeoObjectTypeMetadataQuery query = new GeoObjectTypeMetadataQuery(new QueryFactory());
        query.WHERE(query.getKeyName().EQ(uni.getKey()));
        
        OIterator<? extends GeoObjectTypeMetadata> it2 = query.getIterator();
        
        try
        {
          if (!it2.hasNext())
          {
            GeoObjectTypeMetadata metadata = new GeoObjectTypeMetadata();
            metadata.setUniversal(uni);
            metadata.setIsPrivate(false);
            metadata.apply();
          }
        }
        finally
        {
          it2.close();
        }
      }
    }
    finally
    {
      it.close();
    }
  }
}
