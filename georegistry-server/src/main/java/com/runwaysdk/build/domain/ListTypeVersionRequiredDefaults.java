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

import com.runwaysdk.dataaccess.BusinessDAO;
import com.runwaysdk.dataaccess.BusinessDAOIF;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.BusinessDAOQuery;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeVersion;

public class ListTypeVersionRequiredDefaults
{
  private static final Logger logger = LoggerFactory.getLogger(ListTypeVersionRequiredDefaults.class);
  
  public static void main(String[] args)
  {
    new ListTypeVersionRequiredDefaults().doItInReq();
  }
  
  @Request
  private void doItInReq()
  {
    dotItInTrans();
  }
  
  @Transaction
  private void dotItInTrans()
  {
    BusinessDAOQuery query = new QueryFactory().businessDAOQuery(ListTypeVersion.CLASS);
    
    query.WHERE(query.aBoolean(ListTypeVersion.WORKING).EQ(true));
    query.AND(query.aCharacter(ListTypeVersion.LISTVISIBILITY).EQ((String) null).OR(query.aCharacter(ListTypeVersion.GEOSPATIALVISIBILITY).EQ((String) null)));
    
    logger.info("About to patch " + query.getCount() + " ListTypeVersions with default values for visiblity.");
    
    OIterator<BusinessDAOIF> it = query.getIterator();
    
    try
    {
      while (it.hasNext())
      {
        BusinessDAO version = (BusinessDAO) it.next();
        
        if (version.getValue(ListTypeVersion.LISTVISIBILITY) == null || version.getValue(ListTypeVersion.LISTVISIBILITY).length() == 0)
        {
          version.setValue(ListTypeVersion.LISTVISIBILITY, ListType.PRIVATE);
        }
        
        if (version.getValue(ListTypeVersion.GEOSPATIALVISIBILITY) == null || version.getValue(ListTypeVersion.GEOSPATIALVISIBILITY).length() == 0)
        {
          version.setValue(ListTypeVersion.GEOSPATIALVISIBILITY, ListType.PRIVATE);
        }
        
        version.apply();
      }
    }
    finally
    {
      it.close();
    }
  }

//  @Transaction
//  private void dotItInTrans()
//  {
//    ListTypeVersionQuery query = new ListTypeVersionQuery(new QueryFactory());
//    
//    query.WHERE(query.getWorking().EQ(true));
//    query.AND(query.getListVisibility().EQ((String) null).OR(query.getGeospatialVisibility().EQ((String) null)));
//    
//    OIterator<? extends ListTypeVersion> it = query.getIterator();
//    
//    try
//    {
//      while (it.hasNext())
//      {
//        ListTypeVersion version = it.next();
//        
//        if (version.getListVisibility() == null || version.getListVisibility().length() == 0)
//        {
//          version.setListVisibility(ListType.PRIVATE);
//        }
//        
//        if (version.getGeospatialVisibility() == null || version.getGeospatialVisibility().length() == 0)
//        {
//          version.setGeospatialVisibility(ListType.PRIVATE);
//        }
//        
//        version.apply();
//      }
//    }
//    finally
//    {
//      it.close();
//    }
//  }
}
