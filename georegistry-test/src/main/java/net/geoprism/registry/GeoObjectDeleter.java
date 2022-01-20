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
package net.geoprism.registry;

import java.util.Iterator;

import org.commongeoregistry.adapter.constants.DefaultAttribute;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;

import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.permission.AllowAllGeoObjectPermissionService;
import net.geoprism.registry.query.ServerGeoObjectQuery;

public class GeoObjectDeleter
{
  public static void main(String[] args)
  {
    deleteAllGeoObjects("Focus");
  }
  
  @Request
  private static void deleteAllGeoObjects(String typeCode)
  {
    ServerGeoObjectQuery goq = new ServerGeoObjectService(new AllowAllGeoObjectPermissionService()).createQuery((ServerGeoObjectType.get(typeCode)), null);
    
    long count = goq.getCount();
    
    System.out.println("Program will delete " + count + " GeoObjects.");
    
    Iterator<ServerGeoObjectIF> it = goq.getResults().iterator();
    
    while (it.hasNext())
    {
      deleteGO(it.next().getCode(), typeCode);
    }
    
    System.out.println("Successfully deleted " + count + " GeoObjects.");
  }
  
  @Transaction
  private static void deleteGO(String code, String gotCode)
  {
    System.out.println("Deleting " + code);
    
    ServerGeoObjectType got = ServerGeoObjectType.get(gotCode);
    
    // Make sure we delete the business first, otherwise when we delete the
    // geoEntity it nulls out the reference in the table.
    if (got != null && got.getUniversal() != null)
    {
      QueryFactory qf = new QueryFactory();
      BusinessQuery bq = qf.businessQuery(got.getUniversal().getMdBusiness().definesType());
      bq.WHERE(bq.aCharacter(DefaultAttribute.CODE.getName()).EQ(code));
      OIterator<? extends Business> bit = bq.getIterator();
      try
      {
        while (bit.hasNext())
        {
          Business biz = bit.next();

          biz.delete();
        }
      }
      finally
      {
        bit.close();
      }
    }

    deleteGeoEntity(code);
    
    VertexObject vertex = VertexServerGeoObject.getVertexByCode(got, code);
    if (vertex != null)
    {
      vertex.delete();
    }
  }
  
  @Request
  public static void deleteGeoEntity(String key)
  {
    GeoEntityQuery geq = new GeoEntityQuery(new QueryFactory());
    geq.WHERE(geq.getKeyName().EQ(key));
    OIterator<? extends GeoEntity> git = geq.getIterator();
    try
    {
      while (git.hasNext())
      {
        GeoEntity ge = git.next();

        ge.delete();
      }
    }
    finally
    {
      git.close();
    }
  }
}
